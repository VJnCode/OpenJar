# Stage 1: Build
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY . .
RUN mvn clean package -DskipTests -Dmaven.test.skip=true

# Stage 2: Runtime
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# Only the JAR file is copied here
COPY --from=build /app/target/*.jar app.jar

# Only the RSA keys are copied here
RUN mkdir -p src/main/resources/certs
COPY --from=build /app/src/main/resources/certs/ ./src/main/resources/certs/

EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]