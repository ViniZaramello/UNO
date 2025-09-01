FROM gradle:8.5-jdk21 AS build

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src

RUN gradle build --no-daemon

FROM eclipse-temurin:21-jdk-alpine

LABEL authors="vzaramello"

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs /app/

EXPOSE 8080

CMD ["java", "-jar", "/app/uno-api-1.0.0.jar"]