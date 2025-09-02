FROM gradle:8.5-jdk21 AS build

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src

RUN gradle buildFatJar --no-daemon

FROM eclipse-temurin:21-jdk-alpine

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs /app/

EXPOSE 8080

CMD ["java", "-jar", "/app/uno-api-all.jar"]