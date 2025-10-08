# Stage 1: Build the application
FROM gradle:8.x.x-jdk21-jammy AS build
LABEL authors="trungnguyenhoang"
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src src

RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

# Stage 2: Create the final runtime image
FROM openjdk:21-jdk-slim-bullseye
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]