# syntax=docker/dockerfile:1

FROM docker.m.daocloud.io/library/maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /src

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY flydeer-struct-mind-common/pom.xml flydeer-struct-mind-common/
COPY flydeer-struct-mind-contract/pom.xml flydeer-struct-mind-contract/
COPY flydeer-struct-mind-repository/pom.xml flydeer-struct-mind-repository/
COPY flydeer-struct-mind-service/pom.xml flydeer-struct-mind-service/
COPY flydeer-struct-mind-api/pom.xml flydeer-struct-mind-api/
COPY flydeer-struct-mind-task/pom.xml flydeer-struct-mind-task/
COPY flydeer-struct-mind-controller/pom.xml flydeer-struct-mind-controller/

RUN chmod +x mvnw \
  && ./mvnw -B -pl flydeer-struct-mind-controller -am dependency:go-offline -DskipTests

COPY flydeer-struct-mind-common flydeer-struct-mind-common
COPY flydeer-struct-mind-contract flydeer-struct-mind-contract
COPY flydeer-struct-mind-repository flydeer-struct-mind-repository
COPY flydeer-struct-mind-service flydeer-struct-mind-service
COPY flydeer-struct-mind-api flydeer-struct-mind-api
COPY flydeer-struct-mind-controller flydeer-struct-mind-controller

RUN ./mvnw -B -pl flydeer-struct-mind-controller -am package -DskipTests -q

FROM docker.m.daocloud.io/library/eclipse-temurin:21-jre
WORKDIR /app

RUN apt-get update \
  && apt-get install -y --no-install-recommends curl \
  && rm -rf /var/lib/apt/lists/*

COPY --from=build /src/flydeer-struct-mind-controller/target/flydeer-struct-mind-controller-0.0.1-SNAPSHOT.jar app.jar

ENV JAVA_OPTS="" \
    SPRING_DOCKER_COMPOSE_ENABLED=false \
    SERVER_PORT=8080

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --spring.docker.compose.enabled=false"]
