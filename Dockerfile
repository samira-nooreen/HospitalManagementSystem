# Build Stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run Stage
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# This picks up the JAR created by Step 1
COPY --from=build /app/target/*-jar-with-dependencies.jar app.jar
EXPOSE 8080
# Runs the app and forces it to stay within Render's memory limits
ENTRYPOINT ["java", "-Xmx384m", "-jar", "app.jar"]
