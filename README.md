<div align="center">
  <h1>💼 JobTrail</h1>
  **Intelligent Full-Stack Job Application Tracker**
  
  <p>
    <a href="https://github.com/VallabhPatil3078/JobTrail/actions/workflows/backend-ci.yml">
      <img src="https://github.com/VallabhPatil3078/JobTrail/actions/workflows/backend-ci.yml/badge.svg" alt="Backend CI" />
    </a>
    <a href="https://github.com/VallabhPatil3078/JobTrail/actions/workflows/frontend-ci.yml">
      <img src="https://github.com/VallabhPatil3078/JobTrail/actions/workflows/frontend-ci.yml/badge.svg" alt="Frontend CI" />
    </a>
    <img src="https://img.shields.io/badge/React-18-61DAFB?style=flat-square&logo=react" alt="React" />
    <img src="https://img.shields.io/badge/Spring_Boot-3.4.3-6DB33F?style=flat-square&logo=spring-boot" alt="Spring Boot" />
    <img src="https://img.shields.io/badge/PostgreSQL-16-316192?style=flat-square&logo=postgresql" alt="PostgreSQL" />
  </p>
</div>

---

## 🚀 Overview

JobTrail is a modern, automated job application tracking system designed to take the friction out of managing your job search. Built with a robust Spring Boot backend, a lightning-fast Vite + React frontend, and PostgreSQL, it automatically syncs with your Gmail to detect application statuses, interview requests, and offers, tracking your progress using a strict state machine.

---

## ✨ Key Features

- **Automated Email Syncing**: Securely authenticate via Google OAuth 2.0. JobTrail automatically scans your inbox in the background to log new applications and updates.
- **Review Queue (Inbox Zero)**: A dedicated triage queue for suggested applications parsed from your emails. Features fast, vim-style keyboard shortcuts (`J`/`K` to navigate, `C` to confirm, `R` to reject).
- **Strict State Machine Pipeline**: Applications flow through a strictly validated pipeline (`APPLIED` ➡️ `OA` ➡️ `INTERVIEW` ➡️ `OFFER`), maintaining a complete history of all status transitions.
- **Interactive Dashboard**: Visualize your job search funnel dynamically with `recharts`, allowing you to see your conversion rates at a glance.
- **Test-Driven Architecture**: Backed by `Testcontainers` for the backend and `Vitest/MSW` for the frontend, ensuring resilient layers and flawless integration testing.

---

## 💻 Tech Stack

| Domain | Technologies |
| :--- | :--- |
| **Frontend** | React, TypeScript, Vite, TailwindCSS v4, shadcn/ui, TanStack Query, Recharts |
| **Backend** | Java 21, Spring Boot (Web, Security, Data JPA), Google API Client |
| **Database** | PostgreSQL, Flyway Migrations |
| **Testing** | JUnit 5, Mockito, Testcontainers, Vitest, MSW, React Testing Library |
| **DevOps** | Docker, GitHub Actions CI (Frontend & Backend), Orbit Orchestrator |

---

## 🛠️ Local Development Setup

Follow these steps to get the full stack running smoothly on your local machine.

### 1. Prerequisites
- **Node.js** 20+ and **npm**
- **Docker Desktop**: Required for Testcontainers and local PostgreSQL.
- **Java 21+** and **Maven**.
- A **Google Cloud Console** account (for OAuth 2.0 Credentials).

### 2. Environment Configuration
Copy the sample environment file to configure your local credentials:
```bash
cp backend/.env.example backend/.env
```
Fill in your Google Client ID, Client Secret, JWT Secret, and Encryption Key in `backend/.env`.

### 3. Spin Up Local Database
Use Docker Compose to start the local PostgreSQL instance on port `5433`:
```bash
docker-compose up -d
```

### 4. Build and Test
Run the test suites using Orbit (which orchestrates both frontend and backend tasks):
```bash
orbit run test
```
*Note: Testcontainers will automatically spin up isolated databases for backend tests, while Vitest and MSW will handle the frontend testing.*

### 5. Run the Application
Start the Spring Boot backend server:
```bash
cd backend
./mvnw spring-boot:run
```
In a new terminal window, start the Vite frontend development server:
```bash
cd frontend
npm run dev
```
The App will be available at `http://localhost:5173` and the API at `http://localhost:8080`.

---

## 📚 Core Application Flow

1. **Sign In**: Start by creating a local account.
2. **Connect Gmail**: Authenticate your Google account in the Settings tab.
3. **Sync**: Click "Sync Now" to let JobTrail's background parser scan for job-related emails.
4. **Review Queue**: Navigate through the detected suggestions and confirm valid applications.
5. **Dashboard**: Track your progress and update the status of your applications as you advance through interview stages!

---

<div align="center">
  <i>Built with ❤️ for a seamless job hunt.</i>
</div>