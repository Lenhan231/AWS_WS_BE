FROM gradle:8.5-jdk21-alpine AS builder
WORKDIR /app

COPY settings.gradle* build.gradle* gradlew ./
COPY gradle/wrapper ./gradle/wrapper
RUN chmod +x gradlew && ./gradlew --no-daemon help

COPY src ./src
RUN ./gradlew --no-daemon clean bootJar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar /app/app.jar

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

ENTRYPOINT ["sh","-c","java -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
