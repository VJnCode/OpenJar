
# OpenJar - Advanced User Microservice

An industry-level User Management Microservice built with **Spring Boot 4.0.5**, designed for high scalability, security, and automated deployment.

---

## 🚀 Key Features

* **Secure Authentication:** Industry-standard password hashing using **BCrypt**.
* **Database Scalability:** Implemented **Server-side Pagination** and Sorting to handle millions of records without memory overhead.
* **Data Integrity:** Strict input validation using `jakarta.validation` (Email formats, password strength, etc.).
* **Custom Performance:** Optimized data access using **Native SQL Queries** in Spring Data JPA.
* **Centralized Error Handling:** Global Exception Handler for clean, consistent REST API error responses.
* **Interactive Documentation:** Fully documented with **Swagger UI / OpenAPI 3**.
* **Reliability:** Automated Unit Testing with **JUnit 5** and **Mockito** for 100% logic verification.
* **DevOps Ready:** Fully containerized environment using **Docker** and **Docker Compose**.

---

## 🛠️ Tech Stack

| Component         | Technology                        |
|-------------------|-----------------------------------|
| **Language** | Java 17                           |
| **Framework** | Spring Boot 4.0.5                 |
| **Database** | MySQL 8.0                         |
| **Security** | Spring Security (BCrypt)          |
| **Documentation** | SpringDoc OpenAPI (Swagger)       |
| **Container** | Docker & Docker Compose           |
| **Build Tool** | Maven 3.9+                        |

---

## 📦 Getting Started

### Prerequisites
* **Docker Desktop** installed and running.
* **Maven 3.9+** (to build the application).

### 1. Build the Application
Compile the code and package it into a JAR file:
```bash
mvn clean package -DskipTests
````

### 2\. Run with Docker Compose

Spin up the MySQL database and the Spring Boot application simultaneously:

```bash
docker-compose up --build
```

### 3\. Access the API

  * **API Base URL:** `http://localhost:8086/api/users`
  * **Swagger Documentation:** [http://localhost:8086/swagger-ui/index.html](https://www.google.com/search?q=http://localhost:8086/swagger-ui/index.html)

-----

## 🧪 Automated Testing

To run the Mockito unit tests and verify the business logic:

```bash
mvn test
```

-----

## 📂 Project Structure

```text
src/
 ├── main/java/com/openjar/user_service/
 │    ├── config/       # Security & Swagger Config
 │    ├── controller/   # REST Endpoints
 │    ├── dto/          # Data Transfer Objects (Validation)
 │    ├── exception/    # Custom Exceptions & Global Handler
 │    ├── models/       # Entity Models
 │    ├── repository/   # Native SQL Repositories
 │    └── service/      # Business Logic (BCrypt & Pagination)
 └── test/java/         # JUnit & Mockito Unit Tests
```
