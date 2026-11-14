FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN adduser -S -u 1001 appuser
WORKDIR /app
COPY --from=build --chown=appuser /app/target/app.jar ./app.jar
USER appuser
ENTRYPOINT ["java","-jar","app.jar"]