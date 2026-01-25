# Dora Metrics Viewer

This application calculates and visualizes DORA metrics (Deployment Frequency, Lead Time for Changes, Change Failure Rate, Time to Restore Service) to help teams measure software delivery performance.

## Tech Stack

*   **Java:** 21
*   **Spring Boot:** 3.4.1
*   **Angular:** 19

## Architecture

Please refer to the following documents for detailed architectural information and development status:

*   [Architecture Diagrams & Design](./architecture.md)
*   [Development Plan & Status](./plan.md)

## Quick Start

### Backend
Navigate to the backend directory and run the application:

```bash
cd backend
./mvnw spring-boot:run
```

### Frontend
Navigate to the frontend directory and start the development server:

```bash
cd frontend
ng serve
```

## For AI Agents

When working on this codebase, you **MUST** read and understand the following context files to ensure alignment with the project's architecture and conventions:

*   [gemini.md](./gemini.md) - Project-specific instructions and conventions.
*   [plan.md](./plan.md) - Current development plan and task status.
*   [architecture.md](./architecture.md) - Architectural decisions, diagrams, and domain model.
