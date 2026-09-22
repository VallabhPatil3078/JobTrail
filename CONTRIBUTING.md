# Contributing to JobTrail

First off, thank you for considering contributing to JobTrail! It's people like you that make JobTrail a great tool for everyone.

## Development Setup

JobTrail is composed of a Spring Boot backend and a React/Vite frontend.

### Prerequisites
- Node.js (v18+)
- Java (JDK 25)
- Maven
- Docker (for PostgreSQL database)

### Local Development

1. **Start the database**
   ```bash
   docker-compose up -d
   ```

2. **Start the Backend**
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   The backend will run on `http://localhost:8080`.

3. **Start the Frontend**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   The frontend will run on `http://localhost:5173`.

## Pull Request Process

1. **Branch Naming**: Use the format `feat/feature-name`, `fix/bug-name`, or `chore/task-name`.
2. **Commit Messages**: Follow [Conventional Commits](https://www.conventionalcommits.org/).
3. **Tests**: Ensure all existing tests pass and add new tests for your features.
   - Backend: `mvn test`
   - Frontend: `npm run test`
4. **Linting**: Ensure code adheres to existing style guidelines.
   - Frontend: `npm run lint`

## Reporting Issues

If you find a bug or have a feature request, please use the issue templates provided in the repository.

Thank you for contributing!
