# DORA Metrics Viewer: Implementation Plan

This plan outlines the steps to build the DORA Metrics Viewer, adhering to the Hexagonal Architecture and TDD principles defined in `gemini.md`.

## Phase 1: Project Skeleton & Foundation

- [ ] **1. Project Skeleton: Backend (Maven)**
    - [ ] Generate Spring Boot 3.4+ project using `spring initializr`.
    - [ ] Dependencies: `web`, `data-jpa`, `h2`, `springdoc-openapi`, `lombok`.
    - [ ] Create the core package structure: `com.app.domain`, `com.app.application`, `com.app.infrastructure`.

- [ ] **2. Project Skeleton: Frontend (Angular)**
    - [ ] Generate Angular 19+ workspace using the Angular CLI (`ng new`).
    - [ ] Use `yarn` as the package manager.
    - [ ] Configure TailwindCSS.
    - [ ] Add `ngx-charts` as a dependency.

## Phase 2: Domain Layer (The Core Logic)

- [ ] **3. Domain Entities**
    - [ ] Create `Deployment.java` record.
    - [ ] Create `Change.java` record.
    - [ ] Create `Incident.java` record.

- [ ] **4. Domain Ports (Interfaces)**
    - [ ] `SourceControlPort.java`: Interface with methods like `findDeployments(repoUrl, timeWindow)`.
    - [ ] `MetricsRepositoryPort.java`: Interface for database persistence (e.g., `saveAllDeployments`).

- [ ] **5. TDD Setup: LeadTimeCalculator**
    - [ ] Create `LeadTimeCalculatorTest.java` in `src/test/java/com/app/domain`.
    - [ ] **Write a failing test:** `givenCommitsAndDeployments_whenCalculatingLeadTime_thenReturnsCorrectAverage()`.
    - [ ] This test will use mock `Change` and `Deployment` data and will not compile until the calculator is created.

- [ ] **6. Domain Logic Implementation**
    - [ ] Create `LeadTimeCalculator.java` to make the test pass.
    - [ ] Implement other metric calculators (`DeploymentFrequencyCalculator`, etc.) following the same TDD process.

## Phase 3: Infrastructure Layer (The Outside World)

- [ ] **7. GitHub Adapter (Input)**
    - [ ] Create `GitHubAdapter.java` implementing `SourceControlPort`.
    - [ ] Use a GitHub API client library to fetch Releases, PRs, and Issues.
    - [ ] Implement the logic to map GitHub artifacts to our Domain Entities (`Deployment`, `Change`).
    - [ ] Implement Rate Limit handling (Log warning or pause if 403 Forbidden is returned).

- [ ] **8. Persistence Adapter (Output)**
    - [ ] Create `JPAMetricsRepository.java` implementing `MetricsRepositoryPort`.
    - [ ] Use Spring Data JPA and H2.
    - [ ] Define JPA Entities and Repositories.

- [ ] **9. Web Adapter (API)**
    - [ ] Create `MetricsController.java` (Spring REST Controller).
    - [ ] Define endpoints (e.g., `POST /api/scan-repository`, `GET /api/metrics`).
    - [ ] Use `record` DTOs for API requests and responses.

## Phase 4: Application Layer & Frontend

- [ ] **10. Application Service**
    - [ ] Create `RepoScanningService.java`.
    - [ ] This service will use the `SourceControlPort` to get data and the `MetricsRepositoryPort` to save it.

- [ ] **11. Frontend UI**
    - [ ] Create a "Repository Input" component (form with a single URL input).
    - [ ] Create a "Dashboard" component to display the metrics.
    - [ ] Use `ngx-charts` to visualize the four DORA metrics.
    - [ ] Implement API calls to the backend using Angular Signals.
