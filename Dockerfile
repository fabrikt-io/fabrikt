# Build stage
FROM gradle:8-jdk21 AS builder

# Install OpenJDK 17 for Gradle toolchain detection
USER root
RUN apt-get update \
  && apt-get install -y openjdk-17-jdk ca-certificates --no-install-recommends \
  && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

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
