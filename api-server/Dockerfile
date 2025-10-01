# Only for development purpose
# I wanted to have one command to run all services

FROM openjdk:21-jdk-slim AS base

WORKDIR /app

RUN mkdir -p api-server && mkdir -p gradle && mkdir -p api-server/src

# Copy gradle wrapper and build files
COPY ./gradlew ./
COPY ./gradle/ ./gradle
COPY ./settings.gradle.kts ./
COPY ./api-server/build.gradle.kts ./api-server/

# Make gradlew executable and run any gradle task to cache gradle wrapper download
RUN chmod +x gradlew && ./gradlew tasks --no-daemon


# Copy source code
COPY ./api-server/src/ ./api-server/src/

# Expose port
EXPOSE 4280

# Run the application with continuous mode for faster restarts
CMD ["./gradlew", "bootRun", "--continuous"]