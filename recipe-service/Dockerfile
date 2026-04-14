# Start with a base image containing Java 17
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the compiled JAR file into the container
COPY target/user-service-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your app runs on
EXPOSE 8086

# Command to execute the application
ENTRYPOINT ["java", "-jar", "app.jar"]