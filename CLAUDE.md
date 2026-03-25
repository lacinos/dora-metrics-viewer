# CLAUDE.md — DORA Metrics Viewer

This file provides AI assistants with context about the codebase, architecture, conventions, and workflows for the DORA Metrics Viewer project.

---

## Project Overview

A full-stack web application that accepts a GitHub repository URL, scans its history via the GitHub REST API, and visualizes the four DORA (DevOps Research and Assessment) metrics:

1. **Deployment Frequency** — releases per day
2. **Lead Time for Changes** — PR creation to deployment
3. **Change Failure Rate** — incidents relative to deployments
4. **Time to Restore Service** — incident open to close duration

---

## Technology Stack

### Backend
| Concern | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4.1 |
| Build tool | Maven 3.9+ (`./mvnw`) — no Gradle |
| Database | H2 in-memory (JPA/Hibernate) |
| API docs | Springdoc OpenAPI 2.6.0 (Swagger UI) |
| Utilities | Lombok 1.18.38 |
| External API | GitHub REST API |

### Frontend
| Concern | Technology |
|---|---|
| Framework | Angular 19 (standalone components, no NgModules) |
| Language | TypeScript 5.7 (strict mode) |
| Build/package | npm |
| Styling | TailwindCSS 3 |
| Charts | `@swimlane/ngx-charts` 23 |
| Reactivity | Angular Signals |
| Unit tests | Jasmine + Karma |
| E2E tests | Playwright 1.58 |

---

## Repository Structure

```
dora-metrics-viewer/
├── backend/                        # Spring Boot application
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/gemini/dorametricsviewer/
│       │   ├── domain/             # Pure Java — NO Spring/JPA/external deps
│       │   │   ├── model/          # Deployment, Change, Incident (records)
│       │   │   ├── port/           # SourceControlPort, MetricsRepositoryPort (interfaces)
│       │   │   ├── LeadTimeCalculator.java
│       │   │   ├── MatchStrategy.java
│       │   │   ├── ExactMatchStrategy.java
│       │   │   ├── ReleaseBodyStrategy.java
│       │   │   └── TimeWindowStrategy.java
│       │   ├── application/        # Orchestration layer — uses ports only
│       │   │   ├── DoraMetricsService.java
│       │   │   └── DoraMetricsResult.java (record)
│       │   └── infrastructure/     # All framework code lives here
│       │       ├── configuration/
│       │       ├── github/         # GitHubAdapter (SourceControlPort impl)
│       │       ├── persistence/    # MetricsRepositoryAdapter, JPA entities, mappers
│       │       └── web/            # DoraMetricsController, HealthController
│       └── test/                   # Mirrors main structure; datasets in resources/
│
├── frontend/                       # Angular application
│   ├── package.json
│   ├── angular.json
│   ├── playwright.config.ts
│   ├── proxy.conf.json             # /api → http://localhost:8080
│   └── src/app/
│       ├── dashboard/              # Main feature component
│       ├── health-check/           # Backend health indicator component
│       └── core/
│           ├── models/             # TypeScript interfaces
│           └── services/           # ApiService (HttpClient wrapper)
│
├── start_app.sh                    # Kills ports 8080/4200 and starts both servers
├── gemini.md                       # Original AI agent guide (detailed conventions)
├── architecture.md                 # C4 context + class + sequence diagrams (Mermaid)
├── plan.md                         # Phase-by-phase development plan (all phases complete)
├── testing_strategy.md
├── architecture-agent-guide.md     # Deep-dive guide for architecture exploration
└── development-agent-guide-java.md # Guide for implementing/refactoring Java code
```

---

## Architecture: Hexagonal (Ports & Adapters)

The domain layer is completely isolated from frameworks. The flow is:

```
HTTP Request
    → DoraMetricsController (infrastructure/web)
    → DoraMetricsService (application)
    → SourceControlPort → GitHubAdapter (infrastructure/github)
    → MetricsRepositoryPort → MetricsRepositoryAdapter (infrastructure/persistence)
    → LeadTimeCalculator (domain)
    → DoraMetricsResult
    → HTTP Response
```

### Layer Rules
- **`domain/`** — Pure Java only. No `@Component`, no JPA, no HTTP. Contains entities, ports, and business logic.
- **`application/`** — Orchestrates via ports. No framework-specific annotations beyond `@Service`.
- **`infrastructure/`** — All Spring, JPA, and HTTP client code. Implements domain ports.

### Domain → GitHub Mapping
| DORA Concept | GitHub Artifact |
|---|---|
| Deployment | GitHub Release |
| Change | Merged Pull Request |
| Incident | Issue with label `bug` or `incident` |
| Restoration | Closing time of that issue |

---

## Lead Time Calculation — Chain of Responsibility

`LeadTimeCalculator` tries three `MatchStrategy` implementations in order to pair a `Change` with a `Deployment`:

1. **ExactMatchStrategy** — direct `commitSha` equality
2. **ReleaseBodyStrategy** — extracts PR numbers from release description and matches by ID
3. **TimeWindowStrategy** — finds earliest deployment after the change's merge time (fallback)

---

## Development Commands

### Backend
```bash
cd backend
./mvnw spring-boot:run       # Start dev server on :8080
./mvnw clean package         # Build JAR
./mvnw test                  # Unit tests
./mvnw verify                # Unit + integration tests
```

### Frontend
```bash
cd frontend
npm start                    # ng serve on :4200 (proxies /api → :8080)
npm run build                # Production build
npm test                     # Jasmine/Karma unit tests
npx playwright test          # Playwright E2E tests (requires running app)
```

### Both at once
```bash
bash start_app.sh            # Kills existing processes, starts backend + frontend
                             # Logs: backend.log, frontend.log
```

### Environment
```bash
# Required for GitHub API access (optional — anonymous rate limits apply)
export GITHUB_TOKEN=<your-pat>
```

---

## REST API

Base URL: `http://localhost:8080`

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/metrics/scan` | Scan a repo and return DORA metrics |
| `GET` | `/actuator/health` | Health check |
| `GET` | `/swagger-ui.html` | OpenAPI UI |

### Scan Request/Response
```json
// POST /api/metrics/scan
{
  "repoUrl": "https://github.com/owner/repo",
  "timeWindow": {
    "start": "2024-01-01T00:00:00Z",
    "end": "2024-12-31T23:59:59Z"
  }
}

// Response
{
  "leadTimeForChanges": "PT48H",
  "deploymentFrequency": 0.28,
  "changeFailureRate": 12.5,
  "timeToRestoreService": "PT4H"
}
```

---

## Coding Conventions

### Java
- Use Java `record` for all DTOs and domain models (immutable by design).
- Use Lombok `@RequiredArgsConstructor` for constructor injection; `@Builder` where needed.
- Controllers return `ResponseEntity<ProblemDetails>` for error responses.
- BDD-style test names: `givenX_whenY_thenZ()`.
- No `System.out.println` — use SLF4J `log.debug()` / `log.info()`.
- Keep the domain layer free of Spring/JPA annotations — enforce this strictly.

### TypeScript / Angular
- Strict TypeScript — no `any`.
- Use Angular Signals for component state (`signal()`, `computed()`).
- Avoid manual `.subscribe()` in components; prefer `toSignal()` or `AsyncPipe`.
- All components are standalone (no NgModules).
- No `console.log` left in committed code.

### Git / Commits
- Conventional Commits format: `<type>(<scope>): <description>`
- Types: `feat`, `fix`, `refactor`, `test`, `chore`, `docs`
- Never commit code that does not compile.
- Atomic commits — one concern per commit.
- Never push directly to `main`.

---

## Testing Strategy

### Backend
| Layer | Tool | Annotation |
|---|---|---|
| Domain unit tests | JUnit 5 + Mockito | none (pure Java) |
| Service unit tests | JUnit 5 + Mockito | none |
| Controller tests | Spring MVC Test | `@WebMvcTest` |
| Persistence tests | Spring Data Test | `@DataJpaTest` |
| Full integration | Spring Boot Test | `@SpringBootTest` |

- `FakeGitHubAdapter` in `test/` provides an in-memory stub for `SourceControlPort`.
- `DataCapturer` snapshots real GitHub API responses into `test/resources/datasets/` for replay in `ComplexHeuristicsTest`.
- TDD is mandatory: write test first, then implement.

### Frontend
- Unit tests: Jasmine + Karma (`*.spec.ts` files alongside each component).
- E2E tests: Playwright scripts in `frontend/tests/` targeting `http://localhost:4200`.
- Playwright config: runs on Chromium, Firefox, WebKit; retries 2× on CI.

---

## Key Design Decisions

- **H2 in-memory** database is intentional for this MVP. Scanned data persists only for the session.
- `GitHubAdapter` paginates pull requests up to 8 pages × 100 items to balance completeness vs. rate limits.
- Incident detection (`fetchIncidents()`) is currently a placeholder returning an empty list. The domain mapping for GitHub Issues with `bug`/`incident` labels is defined but not yet implemented.
- The Angular `proxy.conf.json` routes all `/api` calls to the backend, so no CORS configuration is required in development.
- `ngx-charts` requires `BrowserAnimationsModule` — it is provided via `provideAnimations()` in `app.config.ts`.

---

## Known Issues / In-Progress Areas

- `LeadTimeCalculator` and `DoraMetricsService` contain debug `System.out.println` statements that should be converted to proper logging.
- Incident fetching returns an empty list — Change Failure Rate and Time to Restore will always be `0` until GitHub Issues integration is implemented.

---

## Reference Documents

For deeper context, read these files in order:

1. `architecture.md` — C4 context diagram, class hierarchy, sequence diagram
2. `gemini.md` — Full AI agent guidelines including Playwright MCP browser usage
3. `architecture-agent-guide.md` — Component mapping and flow tracing for architecture work
4. `development-agent-guide-java.md` — Safe refactoring and implementation workflow
5. `plan.md` — Completed phase plan; useful for understanding what was built and why
6. `testing_strategy.md` — Testing priorities and manual verification steps
