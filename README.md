# timmy-todo-app

Java 17 / Spring Boot 3.3.6 / Gradle (Groovy DSL) / PostgreSQL 16
트루밸류 백엔드 표준 정렬 완료 — 근거는 `docs/review-*.md` 참조.

## 사전 준비: 로컬 PostgreSQL

로컬에 직접 설치한 PostgreSQL에 붙는다.

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
| 목록 조회 | `GET /api/todos?page=0&size=20&completed=true` | 200 + `Page<TodoResponseDto>` |
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
│   ├── OpenApiConfig.java
│   └── WebConfig.java          # Page 직렬화(VIA_DTO) + 페이지 크기 기본/상한
├── common/                     # ★ 표준 영역
│   ├── AspectLogger.java       # AOP 자동 로깅
│   ├── BaseCustomRepository.java
│   └── ResponseData.java / ResponseDataType.java
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

### 목록 조회 페이징 · 정렬

`page` / `size` / `sort`로 제어한다.

```
GET /api/todos?page=0&size=20&sort=title,asc&completed=true
```

응답은 `PagedModel` 형태다 (`config/WebConfig`의 `PageSerializationMode.VIA_DTO`):

```json
{"content":[...], "page":{"size":20,"number":0,"totalElements":5,"totalPages":1}}
```

- 기본값 `page=0`, `size=20`. `size` 상한은 **2000** (`config/WebConfig`에서 명시)
- 검색 조건은 페이징 **전에** 적용되므로 `totalElements`는 조건에 맞는 전체 건수다
- **정렬 가능 필드**: `idx`, `title`, `completed`, `registerDate`, `modifyDate`.
  기본은 `idx` 내림차순(최신순)
- 목록에 없는 필드로 정렬 요청하면 **조용히 무시하지 않고 400**을 낸다.
  무시하면 클라이언트는 정렬이 먹은 줄 알고 서버는 다른 순서를 주는 상태가 되기 때문
- 필드를 늘리려면 `TodoCustomRepositoryImpl.SORTABLE_PATHS`에 추가한다

> 페이지를 넘길 때마다 `COUNT(*)`가 함께 나간다. 데이터가 커지면
> `PageableExecutionUtils`로 count 생략을 검토할 것 (현재 규모에선 불필요).

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

> **표준 이탈**: `10_LOGGING_AND_EXCEPTION.md`는 `common/TraceIdFilter.java`와
> 로그 패턴의 `[%X{traceId}]`, 에러 응답의 `traceId` 필드를 요구하지만 이 프로젝트는 제거했다
> (요청에 따른 결정). `backend-review`가 위반으로 잡는 항목이다.
> 동시 요청이 늘면 한 요청이 남긴 로그(AspectLogger·Hibernate SQL·파라미터 바인딩)를
> 서로 묶을 수단이 없다는 점은 감안해야 한다.

## 스키마 관리

Flyway가 단일 소스다. `ddl-auto: validate`라서 엔티티와 테이블이 어긋나면 부팅이 실패한다.

1. 엔티티 수정
2. `src/main/resources/db/migration/V3__xxx.sql` 추가 (기존 파일 수정 금지 — 체크섬이 깨진다)
3. 실행 → Flyway가 적용, Hibernate가 검증
4. 엔티티 변경 후 `./gradlew compileJava`로 QueryDSL Q-class 재생성

`V2__align_todo_to_truevalue_standard.sql`이 `todo_entity` → `todo`, `id` → `idx`,
`created_date`/`modified_date` → `register_date`/`modify_date` 리네임을 수행한다.

## 배포

Docker 관련 파일(`Dockerfile`, `.dockerignore`, `compose.yaml`)은 모두 제거했다.
배포는 `./gradlew bootJar`로 만든 jar를 직접 실행한다.

```bash
./gradlew bootJar
SPRING_PROFILES_ACTIVE=prod \
DB_HOST=... DB_NAME=... DB_USERNAME=... DB_PASSWORD=... \
java -jar build/libs/timmy-todo-app-0.0.1-SNAPSHOT.jar
```

## 환경 메모 (이 머신에 맞춰 조정한 것)

- **Gradle 8.10.2**: Boot 3.3의 Gradle 플러그인은 7.6.4~8.x만 지원한다. IntelliJ가 깔아둔 9.6으로는 빌드가 안 된다.
- **springdoc 2.6.0**: 2.7 이상은 Boot 3.4 라인용이다.
