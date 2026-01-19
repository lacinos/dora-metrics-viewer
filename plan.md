# DORA Metrics Viewer: Development Plan

This plan follows the strict Hexagonal Architecture and TDD principles outlined in `gemini.md`.

## Phase 1: Project Skeleton & Initial Setup

- [x] **1.1. Backend: Generate Spring Boot Project**
  - [x] Use `spring initializr` (via `curl` or web) to generate the Maven project.
  - [x] **Configuration:**
    - Group: `com.gemini`
    - Artifact: `dora-metrics-viewer`
    - Language: `Java 21`
    - Packaging: `Jar`
    - Dependencies: `Spring Web`, `Spring Data JPA`, `H2 Database`, `Lombok`, `Springdoc OpenAPI UI`.
  - [x] Unzip and commit the initial structure.

- [x] **1.2. Backend: Create Core Package Structure**
  - [x] Create the three primary packages inside `src/main/java/com/gemini/dorametricsviewer`:
    - [x] `domain`
    - [x] `application`
    - [x] `infrastructure`

- [x] **1.3. Frontend: Generate Angular Project**
  - [x] Use Angular CLI: `ng new dora-metrics-viewer --standalone --style=css --routing`
  - [x] Move the generated project into a `frontend` sub-directory.
  - [x] `cd frontend`
  - [x] Add TailwindCSS: `ng add @angular-tailwind/schematics`
  - [x] Add ngx-charts: `npm install @swimlane/ngx-charts --save`

- [x] **1.4. Verification: End-to-End "Hello World"**
  - [x] **Backend:** Create `HealthController.java` in `infrastructure/web` with a GET endpoint at `/api/health` that returns a simple JSON object `{"status": "UP"}`.
  - [x] **Frontend:**
    - [x] Create a `HealthCheckComponent` in Angular.
    - [x] Make it the default route (`/`).
    - [x] In the component, fetch data from `/api/health`.
    - [x] Display the text: `DORA Metrics Viewer is running. API Status: UP` based on the API response.
  - [x] **Proxy:** Configure Angular's proxy (`proxy.conf.json`) to forward `/api` requests to the Spring Boot backend (`localhost:8080`).

## Phase 2: Domain Layer (The Core Logic)

- [x] **2.1. Domain Entities (Pure Java)**
  - [x] Create `Deployment.java` record in the `domain` package.
  - [x] Create `Change.java` record in the `domain` package.
  - [x] Create `Incident.java` record in the `domain` package.
  - [x] **Rule:** These classes must be pure Java objects with no Spring or JPA annotations.

- [x] **2.2. Domain Ports (Interfaces)**
  - [x] Create `SourceControlPort.java` interface in the `domain` package.
    - `fetchDeployments(repoUrl, timeWindow)`
    - `fetchChanges(repoUrl, timeWindow)`
    - `fetchIncidents(repoUrl, timeWindow)`
  - [x] Create `MetricsRepositoryPort.java` interface in the `domain` package.
    - `saveDeployment(Deployment d)`, `saveChange`, `saveIncident`
    - `findDeployments`, `findChanges`, `findIncidents`

- [ ] **2.3. TDD Setup: Lead Time Calculator**
  - [ ] **Write Failing Tests First:**
    - [ ] Create `LeadTimeCalculatorTest.java` in `src/test/java/.../domain`.
    - [ ] Write a test method: `givenCommitsAndDeployments_whenCalculateLeadTime_thenReturnsCorrectAverage()`.
    - [ ] This test will not compile yet, as `LeadTimeCalculator` does not exist.
  - [ ] **Create Domain Service:**
    - [ ] Create the `LeadTimeCalculator.java` class in the `domain` package.
    - [ ] Implement the `calculate` method to make the test pass.

## Phase 3: Infrastructure Layer (Adapters)

- [ ] **3.1. Persistence Adapter (JPA)**
  - [ ] Create a `JpaDeploymentEntity.java` in `infrastructure/persistence`.
  - [ ] Create a `DeploymentMapper` to map between `domain.Deployment` and `JpaDeploymentEntity`.
  - [ ] Create a `SpringDataDeploymentRepository` interface extending `JpaRepository`.
  - [ ] Create `MetricsRepositoryAdapter` that implements `MetricsRepositoryPort` and uses the Spring Data repository.

- [ ] **3.2. Source Control Adapter (GitHub)**
  - [ ] Create a `GitHubAdapter` in `infrastructure/github` that implements `SourceControlPort`.
  - [ ] Use a REST client (e.g., `RestTemplate` or `WebClient`) to call the GitHub API.
  - [ ] Add configuration for the GitHub token in `application.properties`.

## Phase 4: Application Layer & API

- [ ] **4.1. Application Service (Orchestration)**
  - [ ] Create `DoraMetricsService.java` in the `application` package.
  - [ ] This service will use the ports (`SourceControlPort`, `MetricsRepositoryPort`) and the domain services (`LeadTimeCalculator`).
  - [ ] Method: `calculateMetrics(repoUrl, timeWindow)` which fetches data, calculates metrics, and saves/returns results.

- [ ] **4.2. Web Adapter (REST Controller)**
  - [ ] Create `DoraMetricsController.java` in `infrastructure/web`.
  - [ ] Expose a POST endpoint `/api/metrics` that takes a repository URL.
  - [ ] Use `DoraMetricsService` to get the calculated metrics.
  - [ ] Return the metrics as a DTO.

## Phase 5: Frontend Visualization

- [ ] **5.1. Data Models & Service**
  - [ ] Create TypeScript interfaces for the API DTOs.
  - [ ] Create an `ApiService` to fetch the metrics from the backend.

- [ ] **5.2. Metrics Dashboard Component**
  - [ ] Create a `DashboardComponent`.
  - [ ] Add a form to input the GitHub repository URL.
  - [ ] On submit, call the `ApiService`.