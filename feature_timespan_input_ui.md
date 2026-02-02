# Feature Plan: Time Span Selection for DORA Metrics

## Objective
Enable users to specify a custom time range (Start Date and End Date) in the web interface for calculating DORA metrics. The application currently defaults to a fixed or implicit time window. This feature will allow for more flexible analysis.

## Scope
- **Frontend (Angular):** Add date pickers to the dashboard, capture user input, and pass these parameters to the API.
- **Backend (Spring Boot):** Update the REST API to accept `startDate` and `endDate` parameters and filter data retrieval/calculation logic accordingly.

## Analysis & Design

### 1. Backend Changes (Spring Boot)
- **API Endpoint:** Update `DoraMetricsController` to accept optional `startDate` and `endDate` query parameters.
    - Path: `GET /api/dora-metrics` (Existing)
    - New Params: `?startDate=ISO8601&endDate=ISO8601`
- **DTO:** `ScanRequest` (or similar input DTO if used) might need updates, or the controller method signature directly.
- **Service Layer:** `DoraMetricsService` needs to accept `TimeWindow` or start/end dates.
- **Repository/Data Access:**
    - `MetricsRepositoryPort` and its implementation `MetricsRepositoryAdapter` need to support filtering by date range.
    - JPA Repositories (`SpringDataDeploymentRepository`, `SpringDataChangeRepository`, `SpringDataIncidentRepository`) need custom queries or method names like `findAllByCreatedAtBetween(...)`.

### 2. Frontend Changes (Angular)
- **UI Component:** Add a Date Range Picker (or two Date Pickers) to `DashboardComponent`.
- **State Management:** Store selected dates in the component state.
- **Service Layer:** Update `ApiService.getMetrics()` to accept the date range arguments and append them to the HTTP request.
- **UX:**
    - Default to a reasonable range (e.g., Last 30 days).
    - Trigger a re-fetch of metrics when the "Analyze" or "Update" button is clicked.

## Implementation Steps

### Phase 1: Backend Implementation
1.  **Repository Updates:**
    - Add `findByCreatedAtBetween(Instant start, Instant end)` methods to `SpringDataChangeRepository`, `SpringDataDeploymentRepository`, and `SpringDataIncidentRepository`.
2.  **Adapter Update:**
    - Update `MetricsRepositoryAdapter` to implement the filtering logic using the new repository methods.
3.  **Service Update:**
    - Modify `DoraMetricsService.calculateMetrics()` to accept the time range and pass it down to the repository port.
4.  **Controller Update:**
    - Update `DoraMetricsController` endpoint to parse `startDate` and `endDate` from query parameters.
5.  **Tests:**
    - Add unit tests for the filtered queries and service logic.

### Phase 2: Frontend Implementation
1.  **Service Update:**
    - Update `ApiService` (frontend) to accept `startDate` and `endDate`.
2.  **UI Update (`dashboard.component.html`):**
    - Add input fields for Start Date and End Date (using standard HTML5 date inputs or Angular Material if available - assuming standard for now to minimize dependencies).
    - Add a "Refresh" button.
3.  **Component Logic (`dashboard.component.ts`):**
    - Initialize default dates (e.g., past 30 days).
    - Handle button click to call `ApiService` with selected dates.
4.  **Tests:**
    - Update frontend tests to verify the parameters are sent correctly.

## Verification Plan
1.  **Backend Test:**
    - Run `mvn test` to verify backend logic with date filters.
    - Manual API test using `curl` or Postman: `GET /api/dora-metrics?startDate=2023-01-01&endDate=2023-01-31`.
2.  **Frontend Test:**
    - Run `ng test`.
    - Launch app, change dates, click refresh, verify network request includes correct query params.

## Dependencies
- No new libraries expected. Native HTML date inputs and Java's `java.time` package should suffice.
