<div align="center">
  <h1>💼 JobTrail</h1>
  **Intelligent Full-Stack Job Application Tracker**
  
  <p>
    <a href="https://github.com/VallabhPatil3078/JobTrail/actions/workflows/backend-ci.yml">
      <img src="https://github.com/VallabhPatil3078/JobTrail/actions/workflows/backend-ci.yml/badge.svg" alt="Backend CI" />
    </a>
    <img src="https://img.shields.io/badge/Spring_Boot-3.4.3-6DB33F?style=flat-square&logo=spring-boot" alt="Spring Boot" />
    <img src="https://img.shields.io/badge/PostgreSQL-16-316192?style=flat-square&logo=postgresql" alt="PostgreSQL" />
    <img src="https://img.shields.io/badge/OAuth2.0-Google-4285F4?style=flat-square&logo=google" alt="OAuth2" />
  </p>
</div>

---

## 🚀 Overview

JobTrail is a modern, automated job application tracking system designed to take the friction out of managing your job search. Built with a robust Spring Boot backend and PostgreSQL, it automatically syncs with your Gmail to detect application statuses, interview requests, and offers, tracking your progress using a strict state machine.

---

## ✨ Key Features

- **Automated Email Syncing**: Securely authenticate via Google OAuth 2.0. JobTrail automatically scans your inbox in the background to log new applications and updates.
- **Strict State Machine Pipeline**: Applications flow through a strictly validated pipeline (`APPLIED` ➡️ `OA` ➡️ `INTERVIEW` ➡️ `OFFER`), maintaining a complete history of all status transitions.
- **Robust REST API**: Well-documented, deeply tested endpoints for comprehensive CRUD operations.
- **Test-Driven Architecture**: Backed by `Testcontainers`, ensuring resilient data layers and flawless integration testing.

---

## 💻 Tech Stack

| Domain | Technologies |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot (Web, Security, Data JPA), Google API Client |
| **Database** | PostgreSQL, Flyway Migrations |
| **Testing** | JUnit 5, Mockito, Testcontainers, Spring Boot Test |
| **DevOps** | Docker, Docker Compose, GitHub Actions, Orbit Orchestrator |
| **Frontend** | React, TailwindCSS *(Coming Soon)* |

---

## 🛠️ Local Development Setup

Follow these steps to get the backend running smoothly on your local machine.

### 1. Prerequisites
- **Docker Desktop**: Required for Testcontainers and local PostgreSQL.
- **Java 21+** and **Maven**.
- A **Google Cloud Console** account (for OAuth 2.0 Credentials).

### 2. Environment Configuration
Copy the sample environment file to configure your local credentials:
```bash
cp backend/.env.example backend/.env
```
Fill in your Google Client ID and Secret in `backend/.env`.

### 3. Spin Up Local Database
Use Docker Compose to start the local PostgreSQL instance on port `5433`:
```bash
docker-compose up -d
```

### 4. Build and Test
Run the test suite using Orbit (or Maven directly). Testcontainers will spin up isolated databases automatically.
```bash
# Using Orbit DAG Orchestrator
orbit run test

# Or using Maven directly
cd backend
./mvnw clean test
```

### 5. Run the Application
Start the Spring Boot backend server:
```bash
cd backend
./mvnw spring-boot:run
```
The API will be available at `http://localhost:8080`.

---

## 📚 API Endpoints

### 🔐 OAuth & Gmail Integration
* `GET /api/gmail/connect` - Redirects to Google Consent screen for authentication.
* `GET /api/gmail/callback` - Handles the OAuth callback and stores refresh tokens.
* `POST /api/gmail/sync` - Manually triggers an email sync job.

### 📝 Application Management
* `GET /api/applications` - Fetch all tracked applications.
* `POST /api/applications` - Manually create a new application entry.
* `PATCH /api/applications/{id}/status` - Safely transition an application's status.
* `GET /api/applications/{id}/history` - View the chronological status transition timeline.

---

<div align="center">
  <p>🚧 <b>Note:</b> This project is currently in active development.</p>
  <i>Built with ❤️ for a seamless job hunt.</i>
</div>