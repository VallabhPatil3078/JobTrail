<h1><img src="docs/images/logo.jpg" alt="JobTrail Logo" width="45" align="absmiddle"/> JobTrail</h1>

<img src="https://github.com/VallabhPatil3078/JobTrail/actions/workflows/backend-ci.yml/badge.svg" alt="Backend CI" />

A full-stack job application tracker built with Spring Boot, React, and PostgreSQL.

## Status
🚧 Under active development

## Architecture
```mermaid
graph LR
    Client[React Frontend] -->|REST API| API[Spring Boot Backend]
    API -->|JPA/Hibernate| DB[(PostgreSQL)]
    DB -.->|Flyway| Migrations[Schema Migrations]
    API -.->|Testcontainers| Docker[Temporary Test DB]
```

## Tech Stack

### Backend
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Testcontainers](https://img.shields.io/badge/Testcontainers-000000?style=for-the-badge&logo=docker&logoColor=white)

### Frontend (Coming Soon)
![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)

### DevOps
![Docker](https://img.shields.io/badge/Docker-2CA5E0?style=for-the-badge&logo=docker&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white)
![Orbit](https://img.shields.io/badge/Orbit-Local_DAG_Orchestrator-blue?style=for-the-badge)

## Features Completed
- [x] **Day 1: Base Application CRUD & Auth Skeleton** - Core endpoints, database enumerations, and Testcontainers integration.
- [x] **Day 2: Status Pipeline** - Strict state machine validation for application status transitions, full history tracking, and REST endpoints.

## Local Setup

### Backend (Spring Boot)
1. **Start Docker Desktop**: Testcontainers requires Docker to be running in the background to automatically spin up a temporary PostgreSQL database for testing.
2. Run the tests (Orbit `test` task equivalent):
   ```bash
   cd backend
   ./mvnw clean test
   ```
3. Start the application locally (Coming soon: Local PostgreSQL setup via Docker Compose).

## API Endpoints (Core)
*   `GET /api/applications` - List all applications
*   `POST /api/applications` - Create a new application
*   `PATCH /api/applications/{id}/status` - Safely transition application status
*   `GET /api/applications/{id}/history` - View the entire status transition timeline