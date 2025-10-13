FROM gradle:8.5-jdk21-alpine AS builder
WORKDIR /app

# Copy Gradle wrapper and make it executable
COPY gradlew ./
COPY gradle gradle/
RUN chmod +x gradlew

# Copy project files
COPY settings.gradle build.gradle ./

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src src/

# Build the application
RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Allow overriding runtime options and Spring profile
ENV SPRING_PROFILES_ACTIVE=aws \
    JAVA_OPTS="-Xmx512m -Xms256m"

# Copy the built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
