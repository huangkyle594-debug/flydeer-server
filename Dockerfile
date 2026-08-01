# syntax=docker/dockerfile:1

FROM docker.m.daocloud.io/library/maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /src

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY flydeer-common/pom.xml flydeer-common/
COPY flydeer-contract/pom.xml flydeer-contract/
COPY flydeer-repository/pom.xml flydeer-repository/
COPY flydeer-service/pom.xml flydeer-service/
COPY flydeer-api/pom.xml flydeer-api/
COPY flydeer-task/pom.xml flydeer-task/
COPY flydeer-controller/pom.xml flydeer-controller/

RUN chmod +x mvnw \
  && ./mvnw -B -pl flydeer-controller -am dependency:go-offline -DskipTests

COPY flydeer-common flydeer-common
COPY flydeer-contract flydeer-contract
COPY flydeer-repository flydeer-repository
COPY flydeer-service flydeer-service
COPY flydeer-api flydeer-api
COPY flydeer-controller flydeer-controller

RUN ./mvnw -B -pl flydeer-controller -am package -DskipTests -q

FROM docker.m.daocloud.io/library/eclipse-temurin:21-jre
WORKDIR /app

RUN apt-get update \
  && apt-get install -y --no-install-recommends curl \
  && rm -rf /var/lib/apt/lists/*

COPY --from=build /src/flydeer-controller/target/flydeer-controller-0.0.1-SNAPSHOT.jar app.jar

ENV JAVA_OPTS="" \
    SPRING_DOCKER_COMPOSE_ENABLED=false \
    SERVER_PORT=8080

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --spring.docker.compose.enabled=false"]
