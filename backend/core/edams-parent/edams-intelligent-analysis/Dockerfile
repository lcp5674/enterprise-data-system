# Multi-stage build for EDAMS Intelligent Analysis Service
FROM maven:3.9.5-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="EDAMS Team <edams@example.com>"
LABEL description="EDAMS Intelligent Analysis Service - AI-powered data governance"

# Install required tools
RUN apk add --no-cache \
    curl \
    bash \
    mysql-client \
    postgresql-client \
    tzdata

# Set timezone
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Create non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Create application directory
WORKDIR /app

# Copy the built JAR file
COPY --from=builder /build/target/*.jar app.jar

# Copy configuration
COPY --from=builder /build/src/main/resources/application.yml /app/config/application.yml

# Create directories for logs and data
RUN mkdir -p /app/logs /app/data && \
    chown -R appuser:appgroup /app

# Change ownership
RUN chown -R appuser:appgroup /app

USER appuser

# Expose ports
EXPOSE 8088

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8088/actuator/health || exit 1

# JVM optimization for containers
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError"

# Application startup
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --spring.config.location=/app/config/"]
