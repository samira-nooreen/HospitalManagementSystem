# Step 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
# Change directory to where the pom.xml actually is
RUN cd YourFolderName && mvn clean package -DskipTests 

# Step 2: Run
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# Adjust this path to find the jar in the subfolder
COPY --from=build /app/YourFolderName/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
