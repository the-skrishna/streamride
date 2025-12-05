# Multi-stage build for Producer Service
FROM maven:3.9-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /build

# Copy parent pom
COPY pom.xml .

# Copy all modules (required by parent POM)
COPY commons ./commons
COPY producer-service ./producer-service
COPY processor-service ./processor-service
COPY dashboard-service ./dashboard-service

# Build all modules and repackage producer-service in one command
RUN mvn clean install spring-boot:repackage -DskipTests -pl producer-service -am

# Runtime stage
FROM amazoncorretto:17-alpine

# Set working directory
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /build/producer-service/target/*.jar app.jar

# Expose the service port
EXPOSE 8081

# Set JVM options
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
