# Build stage
FROM gradle:8-jdk21 AS builder

WORKDIR /build

# Copy gradle configuration files
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle

# Copy git directory (required for git-version plugin)
COPY .git ./.git

# Copy source code
COPY src ./src
COPY end2end-tests ./end2end-tests
COPY playground ./playground

# Build the executable jar
RUN ./gradlew clean shadowJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Copy the built jar from builder stage
COPY --from=builder /build/build/libs/fabrikt-*.jar /app/fabrikt.jar

# Set working directory to where files will be mounted
WORKDIR /workspace

# Set the entrypoint to run the CLI
ENTRYPOINT ["java", "-jar", "/app/fabrikt.jar"]

# Default command shows help
CMD ["--help"]
