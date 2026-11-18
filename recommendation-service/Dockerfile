# Stage 1: Build
FROM maven:3.9-eclipse-temurin-26 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime (JRE only, non-root user)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S -g 10001 app && adduser -S -u 10001 -G app app
COPY --from=build /app/target/*.jar app.jar
USER 10001
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]
