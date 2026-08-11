FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -f pom.xml -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN mkdir -p /app/uploads && useradd --system --create-home --home-dir /app appuser && chown -R appuser:appuser /app
COPY --from=build --chown=appuser:appuser /workspace/target/backend-0.0.1-SNAPSHOT.jar /app/app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
