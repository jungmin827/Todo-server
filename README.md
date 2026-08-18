# timmy-todo-app

Java 17 / Spring Boot 3.3.6 / Gradle (Groovy DSL) / PostgreSQL 16
트루밸류 백엔드 표준 정렬 완료 — 근거는 `docs/review-*.md` 참조.

## 사전 준비: 로컬 PostgreSQL

docker-compose 연동은 제거됐다. 로컬에 직접 설치한 PostgreSQL에 붙는다.

```bash
brew install postgresql@16
brew services start postgresql@16

createuser -s timmy
psql -d postgres -c "ALTER USER timmy WITH PASSWORD 'timmy';"
createdb -O timmy timmy_todo
createdb -O timmy timmy_todo_test    # 테스트 전용 (개발 DB와 분리)
```

접속 정보는 전부 환경변수로 덮어쓸 수 있다: `DB_HOST` `DB_PORT` `DB_NAME` `DB_USERNAME` `DB_PASSWORD`.

## 실행

```bash
./gradlew bootRun            # local 프로파일
./gradlew test               # timmy_todo_test DB 사용
./gradlew build              # 컴파일 + 테스트 + jar
```

| 주소 | 설명 |
|---|---|
| `http://localhost:8080/api/todos` | API (요청 헤더에 `X-API-VERSION: 1` 필수) |
| `http://localhost:8080/timmy-todo-app.html` | Swagger UI (local 전용) |
| `http://localhost:8080/actuator/health` | 헬스체크 |

### API

| 동작 | 요청 | 응답 |
|---|---|---|
| 등록 | `POST /api/todos` | **201** + `TodoResponseDto` |
| 단건 조회 | `GET /api/todos/{idx}` | 200 + `TodoResponseDto` |
| 목록 조회 | `GET /api/todos?completed=true` | 200 + `List<TodoResponseDto>` (조건 없으면 **전체**) |
| 수정 | `PUT /api/todos/{idx}` | 200 + `TodoResponseDto` (null 필드는 기존 값 유지) |
| 삭제 | `DELETE /api/todos/{idx}` | **200** + `ResponseData<Void>` (204 아님) |

```bash
curl -X POST http://localhost:8080/api/todos \
  -H 'Content-Type: application/json' -H 'X-API-VERSION: 1' \
  -d '{"title":"장보기","body":"우유"}'
```

## 패키지 구조

레이어별 폴더(`controller/`, `service/`, `repository/`)로 쪼개지 않는다.
도메인 패키지 직속에 두고 `dto/`만 하위 분리한다.

```
timmy.todo.server
├── TodoApplication.java
├── config/                     # ★ 표준 영역
│   ├── JpaConfig.java          # @EnableJpaAuditing + JPAQueryFactory
│   └── OpenApiConfig.java
├── common/                     # ★ 표준 영역
│   ├── AspectLogger.java       # AOP 자동 로깅
│   ├── BaseCustomRepository.java
│   ├── ResponseData.java / ResponseDataType.java
│   └── TraceIdFilter.java      # MDC traceId
├── exception/                  # ★ 표준 영역
│   ├── ExceptionController.java    # 전역 핸들러 + 출구 로깅
│   ├── ExceptionResponseDTO.java
│   └── NotFoundException.java / DuplicatedException.java / ...
└── todo/
    ├── TodoController.java
    ├── TodoService.java
    ├── TodoRepository.java
    ├── TodoCustomRepository.java
    ├── TodoCustomRepositoryImpl.java
    ├── Todo.java               # Entity (Entity 접미사 안 붙임)
    ├── TodoMapper.java         # MapStruct
    └── dto/                    # TodoInsertDto / UpdateDto / QueryDto / ResponseDto
```

`config/` `common/` `exception/`은 **표준 영역**이라 임의 수정하지 않는다 (수정 시 팀 합의).

## 규칙 요약

- **PK는 `Long idx`** — `id`는 PK가 아니라 비즈니스 식별자 전용
- 감사 컬럼은 `register_date` / `modify_date`
- Entity↔DTO 변환은 **MapStruct `TodoMapper`만** — DTO/Entity에 변환 메서드를 두지 않는다
- 메서드명은 `동사 + 도메인명 + ByIdx` (`updateTodoByIdx`), Controller와 Service가 같은 이름
- 로그는 `AspectLogger`(AOP)와 `ExceptionController`(전역) 두 곳에서만.
  개별 메서드의 `log.info`/`log.error` 금지 (`log.debug`만 허용)
- 예외는 커스텀 예외로 throw하고 그대로 위로 흘린다. try/catch 금지

### 표준에서 의도적으로 벗어난 부분

**목록 조회는 페이징하지 않고 전체를 반환한다.** `04_CONTROLLER_GUIDE.md`·`05_SERVICE_GUIDE.md`의
목록 템플릿은 `Page<XxxResponseDto>` + `Pageable`을 요구하지만, 이 프로젝트는
`List<TodoResponseDto>`를 반환한다 (요청에 따른 결정). `backend-review`가 위반으로 잡는 항목이니
리뷰 결과를 볼 때 감안한다.

`TodoQueryDto` 검색 조건은 그대로 살아 있어서, 파라미터를 주면 필터링되고 안 주면 전체가 나온다.
건수가 늘면 전체 행을 메모리에 올리므로, 규모가 커지면 페이징 복원을 검토해야 한다.

> `build.gradle`에 `lombok-mapstruct-binding:0.2.0`이 반드시 있어야 한다.
> 빠지면 Lombok 게터 생성 전에 MapStruct가 돌아 **매퍼 구현체가 빈 껍데기로 생성된다.**

## 프로파일

| | local (기본) | prod |
|---|---|---|
| DB 접속 정보 | localhost 기본값 있음 | 환경변수 필수, 없으면 부팅 실패 |
| SQL 로그 | 출력 | 미출력 |
| actuator | 전체 개방 | health만 |
| Swagger UI | 열림 | 닫힘 |
| logback | `logback-local.xml` | `logback-prod.xml` (일자별 롤링) |

로그 패턴에 `[%X{traceId}]`가 박혀 있다. `TraceIdFilter`가 요청마다 MDC에 심고
응답 헤더 `X-Request-Id`로 돌려준다 — 클라이언트가 보낸 값이 있으면 그대로 이어받는다.

## 스키마 관리

Flyway가 단일 소스다. `ddl-auto: validate`라서 엔티티와 테이블이 어긋나면 부팅이 실패한다.

1. 엔티티 수정
2. `src/main/resources/db/migration/V3__xxx.sql` 추가 (기존 파일 수정 금지 — 체크섬이 깨진다)
3. 실행 → Flyway가 적용, Hibernate가 검증
4. 엔티티 변경 후 `./gradlew compileJava`로 QueryDSL Q-class 재생성

`V2__align_todo_to_truevalue_standard.sql`이 `todo_entity` → `todo`, `id` → `idx`,
`created_date`/`modified_date` → `register_date`/`modify_date` 리네임을 수행한다.

## Docker (앱 패키징 전용)

DB 연동용 docker-compose는 제거했다. Dockerfile은 앱 이미지 빌드에만 쓴다.

```bash
docker build -t timmy-todo-app .
docker run -p 8080:8080 \
  -e DB_HOST=... -e DB_NAME=... -e DB_USERNAME=... -e DB_PASSWORD=... \
  timmy-todo-app
```

멀티스테이지 빌드(gradle:8.10.2-jdk17 → eclipse-temurin:17-jre-jammy)이고
`SPRING_PROFILES_ACTIVE=prod`가 기본으로 박혀 있다.

## 환경 메모 (이 머신에 맞춰 조정한 것)

- **Gradle 8.10.2**: Boot 3.3의 Gradle 플러그인은 7.6.4~8.x만 지원한다. IntelliJ가 깔아둔 9.6으로는 빌드가 안 된다.
- **springdoc 2.6.0**: 2.7 이상은 Boot 3.4 라인용이다.
- **Dockerfile 런타임 이미지**: `eclipse-temurin:17-jre-alpine`은 arm64 이미지가 없어 jammy를 쓴다.
