# Use an official JDK 17 runtime as a parent image
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the executable JAR file from the target folder to the container
COPY target/*.jar app.jar

# Expose the port the Social Service runs on
EXPOSE 8087

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]