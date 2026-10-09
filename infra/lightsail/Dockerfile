FROM eclipse-temurin:17-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/* && useradd --system --uid 10001 app
WORKDIR /app
COPY --chown=app:app application.jar app.jar
USER app
ENV SERVER_PORT=5000
EXPOSE 5000
HEALTHCHECK --interval=30s --timeout=5s --start-period=120s --retries=5 CMD curl -fsS http://localhost:5000/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
