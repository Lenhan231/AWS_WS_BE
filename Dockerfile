FROM gradle:8.5-jdk21-alpine AS builder
WORKDIR /home/gradle/project

# Copy Gradle wrapper files first (order matters!)
COPY gradlew ./
COPY gradle ./gradle
RUN chmod +x gradlew

# Copy project metadata to leverage build cache
COPY settings.gradle build.gradle ./

# Copy application source
COPY src ./src

# Build executable jar
RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Allow overriding runtime options and Spring profile
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS=""

COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
