FROM amazoncorretto:17-alpine AS builder
WORKDIR /app
COPY gradlew .
COPY gradle/ ./gradle
COPY build.gradle settings.gradle ./
COPY src/ ./src
RUN chmod +x ./gradlew && ./gradlew clean bootJar --no-daemon

FROM amazoncorretto:17-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]