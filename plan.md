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

- [x] **2.3. TDD Setup: Lead Time Calculator**
  - [x] **Write Failing Tests First:**
    - [x] Create `LeadTimeCalculatorTest.java` in `src/test/java/.../domain`.
    - [x] Write a test method: `givenCommitsAndDeployments_whenCalculateLeadTime_thenReturnsCorrectAverage()`.
    - [x] This test will not compile yet, as `LeadTimeCalculator` does not exist.
  - [x] **Create Domain Service:**
    - [x] Create the `LeadTimeCalculator.java` class in the `domain` package.
    - [x] Implement the `calculate` method to make the test pass.

## Phase 3: Infrastructure Layer (Adapters)

- [x] **3.1. Persistence Adapter (JPA)**
  - [x] Create JPA Entities in `infrastructure/persistence/entity`:
    - [x] `JpaDeploymentEntity.java`
    - [x] `JpaChangeEntity.java`
    - [x] `JpaIncidentEntity.java`
  - [x] Create a `MetricsMapper` to map between domain records (`Deployment`, `Change`, `Incident`) and JPA Entities.
  - [x] Create Spring Data Repositories in `infrastructure/persistence/repository`:
    - [x] `SpringDataDeploymentRepository`
    - [x] `SpringDataChangeRepository`
    - [x] `SpringDataIncidentRepository`
  - [x] Create `MetricsRepositoryAdapter` that implements `MetricsRepositoryPort` and uses the Spring Data repositories.

- [x] **3.2. Source Control Adapter (GitHub)**
  - [x] Create a `GitHubAdapter` in `infrastructure/github` that implements `SourceControlPort`.
  - [x] Use a REST client (e.g., `RestTemplate` or `WebClient`) to call the GitHub API.
  - [x] Add configuration for the GitHub token in `application.properties`.

## Phase 4: Application Layer & API

- [x] **4.1. Application Service (Orchestration)**
  - [x] Create `DoraMetricsService.java` in the `application` package.
  - [x] This service will use the ports (`SourceControlPort`, `MetricsRepositoryPort`) and the domain services (`LeadTimeCalculator`).
  - [x] Method: `calculateMetrics(repoUrl, timeWindow)` which fetches data, calculates metrics, and saves/returns results.

- [x] **4.2. Web Adapter (REST Controller)**
  - [x] Create `DoraMetricsController.java` in `infrastructure/web`.
  - [x] Expose a POST endpoint `/api/metrics` that takes a repository URL.
  - [x] Use `DoraMetricsService` to get the calculated metrics.
  - [x] Return the metrics as a DTO.

## Phase 5: Frontend Visualization

- [x] **5.1. Data Models & Service**
  - [x] Create TypeScript interfaces for the API DTOs.
  - [x] Create an `ApiService` to fetch the metrics from the backend.

- [x] **5.2. Metrics Dashboard Component**
  - [x] Create a `DashboardComponent`.
  - [x] Add a form to input the GitHub repository URL.
  - [x] On submit, call the `ApiService`.

## Phase 6: Completing the Metrics (Current Focus)

- [x] **6.1. Backend: Implement Remaining Metrics**
  - [x] Update `DoraMetricsResult.java` to include:
    - `deploymentFrequency` (Double - deployments/day)
    - `changeFailureRate` (Double - percentage)
    - `timeToRestoreService` (Duration - average time)
  - [x] Implement `DeploymentFrequencyCalculator` (Logic in Service).
  - [x] Implement `ChangeFailureRateCalculator` (Logic in Service).
  - [x] Implement `TimeToRestoreCalculator` (Logic in Service).
  - [x] Update `DoraMetricsService` to populate the new fields.

- [x] **6.2. Frontend: Connect Real Data**
  - [x] Update `dora-metrics.model.ts` to match the backend DTO.
  - [x] Update `dashboard.component.ts` to replace hardcoded values with calculated data from the API.

- [ ] **6.3. Verification & Debugging**
  - [ ] Verify using the "Test Mode" Golden Dataset (as defined in `testing_strategy.md`).
  - [x] **Debug Production Data:**
      - [x] Investigate why `google/guava` returned no data (Hypothesis: Time window or Rate Limit).
      - [x] **Action:** Test with `microsoft/vscode` (High activity repo).
        - **Result:** Successfully fetched 1 deployment (freq: 0.14/day). Lead time 0 due to naive matching logic. Backend works.