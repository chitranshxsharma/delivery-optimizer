# Use Java 17 (or your version)
FROM eclipse-temurin:17-jdk-alpine

# Jar file path
ARG JAR_FILE=target/*.jar

# Copy jar
COPY ${JAR_FILE} app.jar

# Run app
ENTRYPOINT ["java","-jar","/app.jar"]