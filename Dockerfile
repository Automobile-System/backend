# Multi-stage Dockerfile for building and running the Spring Boot backend
# Build stage: uses Maven + JDK 21 to build the fat jar
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /workspace/app

# copy maven wrapper and pom first to leverage caching
COPY mvnw pom.xml ./
COPY .mvn .mvn

# copy source and build
COPY src ./src

# Make the mvnw executable just in case (Windows host won't affect Linux container)
RUN chmod +x mvnw || true

# Build the application (skip tests by default for image builds)
RUN ./mvnw -B -DskipTests package

# Run stage: smaller JRE image
FROM eclipse-temurin:21-jre
WORKDIR /app

# Expose the default Spring Boot port
EXPOSE 8080

# Copy the built jar from the build stage. The build creates target/*.jar
COPY --from=build /workspace/app/target/*.jar /app/app.jar

# Allow override of JVM options at runtime
ENV JAVA_OPTS=""

# Healthcheck (optional) - uses curl if present; not all base images provide it
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD [ "sh", "-c", "if [ $(pgrep -f 'java.*app.jar' >/dev/null; echo $?) -eq 0 ]; then exit 0; else exit 1; fi" ]

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
