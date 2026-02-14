# Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/HospitalManagementSystem-1.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
