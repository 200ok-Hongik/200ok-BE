    FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY src ./src
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon && cp build/libs/*-SNAPSHOT.jar /app.jar

FROM eclipse-temurin:17-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/* && useradd --system --uid 10001 app
WORKDIR /app
COPY --from=build --chown=app:app /app.jar app.jar
USER app
ENV SERVER_PORT=5000
EXPOSE 5000
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 CMD curl -fsS http://localhost:5000/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
