# Build Stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
# Builds the JAR from your source files
RUN mvn clean package -DskipTests

# Run Stage
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# Copies the compiled JAR
COPY --from=build /app/target/*-jar-with-dependencies.jar app.jar
EXPOSE 8080
# Runs the application
ENTRYPOINT ["java", "-jar", "app.jar"]
