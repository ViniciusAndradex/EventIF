FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system eventif && useradd --system --gid eventif --home-dir /app eventif
COPY --from=build --chown=eventif:eventif /workspace/target/eventif-0.0.1-SNAPSHOT.jar app.jar
USER eventif
EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=UTC", "-jar", "/app/app.jar"]
