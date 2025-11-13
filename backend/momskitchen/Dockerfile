# syntax=docker/dockerfile:1

# --- Build stage ---
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

# Copy project files (Maven wrapper included)
COPY .mvn/ .mvn/
COPY mvnw mvnw
COPY pom.xml pom.xml
COPY src/ src/

# Build the Spring Boot fat jar (skip tests)
RUN chmod +x mvnw \
    && ./mvnw -DskipTests package \
    && ls -l target

# --- Runtime stage ---
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy jar from build stage
COPY --from=build /workspace/target/momskitchen-0.0.1-SNAPSHOT.jar /app/app.jar

# Default environment (overridable via compose)
ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8081 \
    DB_HOST=mysql \
    DB_PORT=3306 \
    DB_NAME=moms_kitchen \
    DB_USERNAME=root \
    DB_PASSWORD= \
    ADMIN_API_KEY=changeme

EXPOSE 8081

ENTRYPOINT ["java","-jar","/app/app.jar"]

