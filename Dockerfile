# --- build ---
FROM gradle:8.10.2-jdk17 AS build
WORKDIR /app

# 의존성 레이어를 소스와 분리해 캐시 적중률을 높인다
COPY settings.gradle build.gradle gradle.properties ./
RUN gradle dependencies --no-daemon --no-configuration-cache > /dev/null 2>&1 || true

COPY src ./src
RUN gradle bootJar --no-daemon --no-configuration-cache

# --- run ---
# alpine 태그는 arm64(Apple Silicon) 이미지가 없어 jammy 사용
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN useradd --system --create-home app
USER app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
