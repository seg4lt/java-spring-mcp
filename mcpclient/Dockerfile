# Only for development purpose
# I wanted to have one command to run all services

FROM openjdk:21-jdk-slim AS base

RUN apt update && apt install -y curl

WORKDIR /app

COPY ./mvnw ./
COPY ./.mvn/ ./.mvn
COPY ./pom.xml ./

RUN chmod +x mvnw && ./mvnw dependency:go-offline

COPY ./src/ ./src/

EXPOSE 4280

CMD ["./mvnw", "spring-boot:run"]
