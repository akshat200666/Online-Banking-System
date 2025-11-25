# Multi-stage Dockerfile to build the fat JAR
FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . /app
RUN mvn -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/atm-management-1.0-SNAPSHOT-shaded.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
