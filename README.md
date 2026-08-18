# timmy-todo-app

Java 17 / Spring Boot 3.3.6 / Gradle (Kotlin DSL) / PostgreSQL 16

## 실행

```bash
./gradlew bootRun            # postgres 자동 기동 + local 프로파일
./gradlew test               # Testcontainers로 일회용 DB 사용, docker 필요
./gradlew build              # 컴파일 + 테스트 + jar
```

| 주소 | 설명 |
|---|---|
| `http://localhost:8080/todo` | API |
| `http://localhost:8080/swagger-ui/index.html` | API 문서 (local 전용) |
| `http://localhost:8080/actuator/health` | 헬스체크 |

`spring-boot-docker-compose`가 `compose.yaml`을 감지해 `bootRun` 시 DB를 자동 기동하고
접속 정보도 자동 주입한다. 직접 관리하고 싶으면 build.gradle.kts에서 해당 의존성만 지우면 된다.

## 패키지 구조

```
timmy.todo.server
├── TodoApplication.java
├── global/
│   ├── config/JpaConfig.java   # @EnableJpaAuditing
│   ├── error/                  # (비어 있음) 예외 처리 넣을 자리
│   └── response/               # (비어 있음) 공통 응답 래퍼 넣을 자리
└── todo/
    ├── controller/             # TodoController
    ├── service/                # TodoService (@Transactional 경계)
    ├── domain/                 # TodoEntity, TodoRepository
    └── dto/                    # TodoRequest, TodoResponse (record)
```

의존 방향: `controller → service → domain`, `dto`는 controller/service가 공유,
`global`은 아무 곳에서나 참조 가능.

## 프로파일

| | local (기본) | prod |
|---|---|---|
| DB 접속 정보 | localhost 기본값 있음 | 환경변수 필수, 없으면 부팅 실패 |
| SQL 로그 | 출력 | 미출력 |
| actuator | 전체 개방 | health만 |
| Swagger UI | 열림 | 닫힘 |

공통 설정은 `application.yml`, 프로파일별 설정은 `application-{profile}.yml`에 있다.
개인 설정만 따로 두고 싶으면 `application-secret.yml`(gitignore 처리됨)을 쓰면 된다.

## 스키마 관리

Flyway가 단일 소스다. `ddl-auto: validate`라서 엔티티와 테이블이 어긋나면 부팅이 실패한다.

1. 엔티티 수정
2. `src/main/resources/db/migration/V2__xxx.sql` 추가 (기존 파일 수정 금지 — 체크섬이 깨진다)
3. 실행 → Flyway가 적용, Hibernate가 검증

## Docker

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
- **Testcontainers 1.21.3 + `api.version=1.44`**: Boot 3.3.6이 관리하는 1.19.8은 docker-java가
  API 1.32로 붙는데, 설치된 Docker Engine 29의 최소 지원 API는 1.40이라 `/info`가 400을 준다.
- **springdoc 2.6.0**: 2.7 이상은 Boot 3.4 라인용이다.
- **Dockerfile 런타임 이미지**: `eclipse-temurin:17-jre-alpine`은 arm64 이미지가 없어 jammy를 쓴다.
