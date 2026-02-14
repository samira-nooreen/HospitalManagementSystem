# Build Stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run Stage
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Use the PORT variable provided by Render
ENTRYPOINT ["java", "-Xmx384m", "-jar", "app.jar", "--server.port=${PORT:8080}"]
