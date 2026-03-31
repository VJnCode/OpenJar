markdown
# OpenJar — The Intelligent Recipe Ecosystem

OpenJar is a high-performance, microservices-based platform designed for food enthusiasts to discover, organize, and share culinary masterpieces. This repository serves as the central orchestrator for the OpenJar backend services and frontend applications.

## 🌟 About OpenJar
OpenJar is more than just a digital cookbook; it is a scalable social ecosystem built to bridge the gap between inspiration and creation. By leveraging a modular microservices architecture, OpenJar ensures high availability and specialized processing for diverse domains such as user identity, recipe indexing, and social engagement.

### Key Pillars:
* **Digital Gastronomy:** Detailed recipe management with structured instructions and ingredient metrics.
* **Personalized Collections:** User-curated "Cookbooks" for organizing recipes into meaningful themes.
* **Social Pulse:** Real-time interactions through likes, shares, saves, and community-driven comments.
* **Global Profiles:** Rich user identities that showcase culinary contributions and preferences.

---

## 🏗️ System Architecture
OpenJar is built on a **Shared-Nothing Architecture**, where each domain is isolated into its own service to ensure independent scalability and fault tolerance.

### Service Domains:
1. **Identity & Profile Management:** Handles secure authentication (BCrypt), role-based access control, and public culinary profiles.
2. **Recipe Engine:** Manages the core content lifecycle, including SEO-friendly slugs and multi-media assets.
3. **Social Infrastructure:** Orchestrates community interactions, feedback loops, and engagement metrics.
4. **Collection Logic:** Facilitates the "Cookbook" experience, allowing users to build private or public recipe archives.

---

## 🛠️ Global Tech Stack
* **Runtime:** Java 17 (Spring Boot)
* **Databases:** Relational (MySQL) with domain-specific schemas.
* **Security:** Spring Security with JWT/BCrypt.
* **Documentation:** OpenAPI 3 / Swagger.
* **Infrastructure:** Docker, Docker Compose.

---

## 🚀 Quick Start (Whole System)

### Prerequisites
* Docker Desktop & Docker Compose
* Maven 3.9+ (for building individual services)

### One-Command Deployment
To spin up the entire OpenJar ecosystem (All databases and services):
```bash
docker-compose up --build
```

---

## 📂 Repository Layout
```text
OpenJar/ (Root)
 ├── docker-compose.yml     # Central orchestration
 ├── README.md              # Project overview
 ├── user-service/          # Identity & Profiles
 ├── recipe-service/        # Core Recipe logic (Next Phase)
 ├── social-service/        # Interactions & Comments (Next Phase)
 └── collection-service/    # Cookbooks & Lists (Next Phase)
```
