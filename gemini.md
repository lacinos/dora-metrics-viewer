# Project Context: DORA Metrics Viewer

## 1. Project Identity & Goal
**Name:** DORA Metrics Viewer
**Core Purpose:** A web application that ingests a GitHub Repository URL, scans its history, and visualizes the 4 DORA Metrics to help teams understand their delivery performance.

## 2. Technology Stack (Strict Versions)
### Backend
- **Language:** Java 21
- **Framework:** Spring Boot 3.4+
- **Build Tool:** Maven 3.9+ (strictly no Gradle)
- **Database:** H2 In-Memory (Dev/MVP), JPA/Hibernate
- **API Documentation:** Springdoc OpenAPI (Swagger)
- **External integration:** GitHub REST API

### Frontend
- **Framework:** Angular 19+ (Standalone Components, Signals, strictly no Modules)
- **Build Tool:** Yarn
- **Language:** TypeScript 5.x
- **Styling:** TailwindCSS
- **Visualization:** `ngx-charts` (preferred) or `Chart.js`

## 3. Architecture: Hexagonal (Ports & Adapters)
We strictly follow **Hexagonal Architecture** to decouple the "GitHub Scanner" from "Metric Math".

### Folder Structure (Mental Model)
- `src/main/java/com/app/domain` (THE CORE)
    - **Rules:** Pure Java. NO Spring annotations. NO JPA annotations. NO GitHub dependencies.
    - **Contents:** Domain Entities (`Deployment`, `Incident`), Business Logic (`LeadTimeCalculator`), Ports (Interfaces like `SourceControlPort`, `MetricsRepositoryPort`).
- `src/main/java/com/app/application` (THE GLUE)
    - **Rules:** Orchestrates flow. Uses Ports.
    - **Contents:** Service classes (e.g., `RepoScanningService`).
- `src/main/java/com/app/infrastructure` (THE OUTSIDE WORLD)
    - **Rules:** All Frameworks & Libraries go here.
    - **Contents:**
        - `adapter/github`: Implements `SourceControlPort` using GitHub API.
        - `adapter/persistence`: Implements `MetricsRepositoryPort` using Spring Data JPA.
        - `adapter/web`: Spring RestControllers.

## 4. Domain & Data Strategy (The "GitHub Strategy")
The system maps GitHub concepts to DORA entities as follows:

| DORA Concept | GitHub Artifact (MVP Rule) |
| :--- | :--- |
| **Deployment** | A **GitHub Release** or a **Tag** on `main`. |
| **Change** | A **Pull Request** merged to `main`. |
| **Failure/Incident** | An **Issue** created with the label `"bug"` or `"incident"`. |
| **Restoration** | The closing time of that specific "bug" Issue. |

### Metric Logic (Domain Rules)
1.  **Deployment Frequency:** Count of `Deployment` entities per day/week.
2.  **Lead Time for Changes:** Average duration between `PR.createdAt` (or first commit) and `Deployment.createdAt`.
3.  **Change Failure Rate:** (Count of `Deployments` followed immediately by a `Bug Issue`) / (Total `Deployments`).
4.  **Time to Restore:** Average duration between `Issue.createdAt` and `Issue.closedAt` for bug issues.

## 5. Coding Standards & Best Practices

### Design Principles
- **SOLID:** STRICTLY enforce **SRP**. The class that fetches data from GitHub MUST NOT be the same class that calculates the Lead Time average.
- **YAGNI:** Do not build abstraction layers for GitLab or Bitbucket. Hardcode logic for GitHub for the MVP.
- **DRY:** Use a shared `TimeWindowUtils` for date filtering (e.g., "Last 30 Days").

### Backend (Java)
- **DTOs:** Use Java `record` for all Data Transfer Objects.
- **Lombok:** Use `@Builder`, `@RequiredArgsConstructor` (for constr. injection).
- **Controller:** Return `ResponseEntity<ProblemDetails>` for errors.

### Frontend (Angular)
- **State Management:** Use **Angular Signals** for local component state.
- **API Calls:** Use `toSignal` or RxJS with `AsyncPipe`. Avoid manual `.subscribe()` in components.
- **Typing:** Strict mode enabled. No `any`.

## 6. Testing Strategy (TDD/BDD)
**Mandatory Workflow:** You must define the test case *before* writing the implementation.

- **Naming:** Use BDD style: `givenGitHubReturns5Releases_whenCalculatingFrequency_thenReturnsWeeklyAverage()`.
- **Scope:**
    - **Domain Tests (High Priority):** Test the math of DORA metrics using pure unit tests (no Spring context).
    - **Integration Tests:** Use `@SpringBootTest` only for the GitHub Scanner -> DB flow.

## 7. Workflow Commands
- When asked to "Start": Verify this file is read, then propose the `plan.md`.
- When asked to "Implement Feature X": Start by generating the **JUnit 5 Test Interface** for that feature to confirm understanding of the logic.

## 8. Tool Usage & Intelligence (MCP)
**You have access to an LSP Bridge (Language Server Protocol) via MCP.**

### Rules for using LSP:
1.  **Stop Hallucinating Methods:** Before you use a method from a class you didn't just write, use the LSP tool (e.g., `read_symbol` or `go_to_definition`) to check the actual method signature.
2.  **Verify Imports:** If you are unsure about an import path, use the LSP to resolve the symbol.
3.  **Refactoring:** When asked to refactor, use the LSP `find_references` tool first to ensure you don't break consumers of that class.
4.  **Diagnostics:** If a user reports a compile error, use the LSP `get_diagnostics` tool to see exactly what the Java compiler is complaining about.

## 9. Version Control & Git Strategy
**You have permission to run `git` commands when explicitly asked.**

### Rules of Engagement
1.  **Zero Broken Commits:** NEVER commit code that does not compile.
2.  **Atomic Commits:** Do not combine refactors, features, and bug fixes in one commit. Split them.
3.  **Branching:**
    - Assume you are already on the correct feature branch.
    - DO NOT switch branches unless explicitly instructed.
    - DO NOT push to `main` or `master`.

### Commit Message Standard (Conventional Commits)
Format: `<type>(<scope>): <description>`

- **feat**: New feature (e.g., `feat(parser): add github release parsing`)
- **fix**: Bug fix (e.g., `fix(api): handle 404 from github`)
- **refactor**: Code change that neither fixes a bug nor adds a feature
- **test**: Adding missing tests or correcting existing tests
- **chore**: Changes to the build process or auxiliary tools (e.g., `chore(pom): update spring version`)

### Example Workflow
When I say "Checkpoint", you should:
1.  Run `git status` to see changes.
2.  Run `git add .` (or specific files).
3.  Generate a conventional commit message describing the work done.
4.  Execute `git commit -m "..."`.

### Branching & Merging Strategy
- **Role Definition:** You are the Contributor. I am the Maintainer.
- **Your Boundary:** You work ON branches. You push TO branches. You NEVER merge into `main`.
- **Definition of Done:** A feature is "Done" when:
    1. Tests pass.
    2. Code is committed.
    3. Branch is pushed to `origin`.
    4. You inform me: "Branch `feat/xyz` is pushed and ready for review."