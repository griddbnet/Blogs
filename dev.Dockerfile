FROM maven:3.9.8-eclipse-temurin-22-alpine
RUN mkdir /app
WORKDIR /app

COPY pom.xml ./
RUN mvn dependency:go-offline

COPY docker-entrypoint-dev.sh ./
COPY src ./src