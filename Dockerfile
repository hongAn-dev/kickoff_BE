# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy maven wrapper and pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Download dependencies (to cache layers)
RUN ./mvnw dependency:go-offline

# Copy the source code and build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Port configuration
EXPOSE 8080

# Environment variables (Railway provides these or uses from Variables tab)
ENV SERVER_PORT=8080

ENTRYPOINT ["java", "-jar", "app.jar"]
