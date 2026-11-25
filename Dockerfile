# ==============================================================================
# Stage 1: Build the React Frontend
# ==============================================================================
FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend

# Install dependencies using clean install (fallback to install if lockfile diverges)
COPY frontend/package*.json ./
RUN npm ci || npm install

# Copy frontend source code and build production assets
COPY frontend/ ./
RUN npm run build

# ==============================================================================
# Stage 2: Build the Spring Boot Backend JAR
# ==============================================================================
FROM maven:3.9.6-eclipse-temurin-17 AS backend-builder
WORKDIR /app/backend

# Copy Maven POM and download dependencies to maximize layer caching
COPY backend/pom.xml ./
RUN mvn dependency:go-offline -B || true

# Copy backend source code
COPY backend/src ./src

# Bundle React frontend production build into Spring Boot static resources
COPY --from=frontend-builder /app/frontend/dist ./src/main/resources/static

# Build the executable Spring Boot JAR (skipping test suite for deployment)
RUN mvn clean package -DskipTests -B

# ==============================================================================
# Stage 3: Lightweight Production JRE Runtime
# ==============================================================================
FROM eclipse-temurin:17-jre-jammy AS runner
WORKDIR /app

# Run as a dedicated non-root user for security
RUN groupadd -r appgroup && useradd -r -g appgroup appuser
USER appuser

# Copy the built jar from the backend builder stage
COPY --from=backend-builder --chown=appuser:appgroup /app/backend/target/*.jar app.jar

# Render exposes dynamic port via $PORT (defaults to 8080)
ENV PORT=8080
EXPOSE ${PORT}

# JVM tuning for cloud containers (optimized for Render 512MB RAM free tier limit)
ENV JAVA_OPTS="-XX:+UseSerialGC -Xmx320m -XX:MaxMetaspaceSize=128m -XX:+ExitOnOutOfMemoryError"

# Start the Spring Boot application binding to $PORT
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -jar app.jar"]
