# Testing Strategy

## Backend Testing (Java/Spring Boot)

### Unit Tests
- **Framework:** JUnit 5 + Mockito
- **Scope:** Domain logic (Strategies, Calculators), Service layer (orchestration), Mappers.
- **Goal:** Verify business logic in isolation.
- **Command:** `./mvnw test`

### Integration Tests
- **Framework:** Spring Boot Test (`@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest`)
- **Scope:** Controllers, Repository Adapters, External API Adapters (using WireMock or similar if needed, currently mocked manually).
- **Goal:** Verify interaction between Spring components and external systems (DB, GitHub API).
- **Command:** `./mvnw verify` (runs integration tests if configured correctly, typically part of the build).

## Frontend Testing (Angular)

### Unit Tests
- **Framework:** Jasmine + Karma (Standard Angular)
- **Scope:** Components, Services.
- **Goal:** Verify UI logic and service integration.
- **Command:** `npm test` (often runs in watch mode, use `npm test -- --watch=false --browsers=ChromeHeadless` for CI-like run).

### End-to-End (E2E) Tests
- **Framework:** Playwright
- **Scope:** Critical user flows (Dashboard loading, triggering a scan).
- **Goal:** Verify the application works from a user's perspective.
- **Command:** `npx playwright test`

## Manual Verification / Sanity Check
1. Start Backend: `./mvnw spring-boot:run`
2. Start Frontend: `ng serve` (or `npm start`)
3. Navigate to `http://localhost:4200`
4. Enter a GitHub repository (public) and token (optional/if required).
5. Verify metrics are displayed.

## Current Focus
- Ensure `LeadTimeCalculator` handles edge cases (missing dates, unsorted events).
- Verify `GitHubAdapter` correctly maps API responses to Domain models.
- validate `DoraMetricsController` endpoints return correct JSON structure.
