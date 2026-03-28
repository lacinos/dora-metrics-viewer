# Feature: Improve Test Quality and Coverage

## Goal
Address the most significant gaps in test coverage identified through codebase analysis, working from highest to lowest priority.

---

## Tasks

### 1. LeadTimeCalculator — Test all three matching strategies and edge cases
**File:** `backend/src/test/java/com/gemini/dorametricsviewer/domain/LeadTimeCalculatorTest.java`

- [ ] Test `ReleaseBodyStrategy`: change whose PR number appears in the release description body is matched to that deployment
- [ ] Test `TimeWindowStrategy`: change with no SHA/body match is matched to the first deployment after its `mergedAt` timestamp
- [ ] Test empty changes list → returns 0
- [ ] Test empty deployments list → returns 0
- [ ] Test no strategy finds any match → returns 0
- [ ] Test multiple changes matched across different strategies → average is correct

---

### 2. GitHubAdapter — Error path tests
**File:** `backend/src/test/java/com/gemini/dorametricsviewer/infrastructure/github/GitHubAdapterTest.java`

- [ ] 401 Unauthorized response → method throws or returns empty list (document expected behaviour)
- [ ] 404 Not Found response → method throws `IllegalArgumentException` (or similar meaningful exception)
- [ ] 403 Rate-limited response → appropriate exception/empty result
- [ ] Malformed JSON response → safe failure, no NPE
- [ ] `fetchChanges` with merged PR having null `mergedAt` → entry is excluded from results

---

### 3. DoraMetricsController — Exception handler tests
**File:** `backend/src/test/java/com/gemini/dorametricsviewer/infrastructure/web/DoraMetricsControllerTest.java`

- [ ] Service throws `IllegalArgumentException` → HTTP 400 with meaningful error body
- [ ] Service throws a generic `RuntimeException` → HTTP 500 with meaningful error body
- [ ] Request body missing `repoUrl` → HTTP 400 (validation error)

---

### 4. ApiService (Frontend) — Unit tests
**File:** `frontend/src/app/core/services/api.service.spec.ts` *(create new)*

- [ ] `scanRepository()` issues a POST to `/api/metrics/scan`
- [ ] Request body matches the `ScanRequest` shape (`repoUrl`, `timeWindow.start`, `timeWindow.end`)
- [ ] Returns the parsed `DoraMetricsResult` on a 200 response
- [ ] Propagates an error `Observable` on a 4xx/5xx HTTP response

---

### 5. DoraMetricsService — Edge-case tests
**File:** `backend/src/test/java/com/gemini/dorametricsviewer/application/DoraMetricsServiceTest.java`

- [ ] Zero deployments in the time window → `deploymentFrequency` is 0, no division-by-zero
- [ ] Zero incidents → `changeFailureRate` is 0
- [ ] `SourceControlPort` throws an exception mid-scan → exception propagates cleanly (no partial save)

---

### 6. MetricsRepositoryAdapter — Cover all entities and bounded queries
**File:** `backend/src/test/java/com/gemini/dorametricsviewer/infrastructure/persistence/MetricsRepositoryAdapterTest.java`

- [ ] Save and retrieve `Change` entities
- [ ] Save and retrieve `Incident` entities
- [ ] `findDeploymentsByRepositoryAndTimeWindow` only returns records within the time window bounds (not before, not after)
- [ ] Same bounded-query assertion for Changes and Incidents

---

### 7. DashboardComponent (Frontend) — Behaviour tests
**File:** `frontend/src/app/dashboard/dashboard.component.spec.ts`

- [ ] `parseDuration('PT2H30M')` returns `2.5`
- [ ] `parseDuration('PT0S')` returns `0`
- [ ] `parseDuration('PT45M')` returns `0.75`
- [ ] Loading signal is `true` while scan is in progress and `false` after completion
- [ ] Error signal is set when `ApiService` returns an HTTP error
- [ ] Computed `leadTimeData` signal produces correct ngx-charts array format given a mock `DoraMetricsResult`

---

### 8. HealthCheckComponent (Frontend) — Behaviour tests
**File:** `frontend/src/app/health-check/health-check.component.spec.ts`

- [ ] Component calls `GET /api/health` on `ngOnInit`
- [ ] Status displays "UP" when the endpoint returns `{ status: 'UP' }`
- [ ] Status remains "DOWN" when the HTTP call errors

---

## Execution Order
Work through tasks **1 → 2 → 3 → 4 → 5 → 6 → 7 → 8**.
Each task should result in green tests before moving to the next.
Run the full backend suite with `cd backend && ./mvnw test` and the frontend suite with `cd frontend && npm test -- --watch=false --browsers=ChromeHeadless` after each task.
