# Testing Strategy & Mocking Analysis

## User Constraint
The user wants to test the application end-to-end (GUI -> Backend -> External APIs) but **does not** want to pollute `src/main` with mock data or test classes.

## Options Analysis

### Option 1: `SpringApplication.from(...)` (Recommended)
Spring Boot 3.1+ introduced a way to run the application with test-specific configurations from the `main` method of a class in `src/test`.
- **Pros:** Keeps all mock logic, test data seeds, and Testcontainer configurations strictly in `src/test`. Zero pollution in `src/main`.
- **How:** We create a `TestDoraMetricsViewerApplication.java` in `src/test/java`. It defines a main method that uses `SpringApplication.from(DoraMetricsViewerApplication::main).with(TestConfig.class).run(args)`.
- **Mechanism:** We can define a `TestConfig` that replaces the real `GitHubAdapter` with a `FakeGitHubAdapter` (defined in `src/test`) or configures a WireMock server via Testcontainers.

### Option 2: WireMock (External Service Mocking)
Run a standalone WireMock server (or via Testcontainers in Option 1) that mimics the GitHub API.
- **Pros:** The application code is identical to production; it just points to a different URL (localhost vs api.github.com).
- **Cons:** Requires managing the WireMock stub mappings JSON files.

### Option 3: Spring Profiles with "Demo" Adapters
Create a specific implementation of `SourceControlPort` (e.g., `InMemoryGitHubAdapter`) annotated with `@Profile("demo")`.
- **Pros:** Easy to run standalone without test harness.
- **Cons:** Technically places "demo" code in `src/main`, which the user explicitly wanted to avoid.

## Recommendation
**Option 1 with a bespoke `FakeGitHubAdapter` in `src/test`.**
This allows us to start the backend locally, having it serve the frontend and API, but answering with controlled fake data defined entirely within the test scope.

## Testing Loop Plan

1.  **Setup Test Runner:** Create `TestDoraMetricsViewerApplication` in `src/test`.
2.  **Implement Fake Adapter:** Create `FakeGitHubAdapter` in `src/test` implementing `SourceControlPort`. 
    - **Crucial:** Populate it with a "Golden Dataset" where metrics are known in advance.
3.  **Start Backend:** Run this test application.
4.  **Start Frontend:** Run `ng serve` (or build and serve via Spring Boot if integrated).
5.  **Browser Loop:**
    *   **Navigate:** Use `browser_navigate` to open the app.
    *   **Inspect:** Use `browser_snapshot` to inspect the accessibility tree and text content.
    *   **Verify Data:** Check that the displayed numbers match the Golden Dataset calculations.
    *   **Debug:** Use `browser_console_messages` to check for errors.
    *   **Fix & Repeat:** Refactor code based on findings and restart/reload.

## Data & Visual Verification Strategy

To ensure diagrams and summary cards show the *correct* values, we will use a "Golden Dataset" in the `FakeGitHubAdapter`.

### 1. The Golden Dataset
We will seed the `FakeGitHubAdapter` with a specific scenario designed to produce exact integer metrics:
*   **Time Window:** Last 7 days.
*   **Deployments:** Exactly 2 (e.g., Monday and Thursday).
*   **Changes (PRs):** Exactly 4.
    *   2 PRs -> Deployed Monday.
    *   2 PRs -> Deployed Thursday.
    *   **Lead Time:** 2 PRs took 1 day, 2 PRs took 3 days. Average = 2 days.
*   **Incidents:** Exactly 1.
    *   Occurred immediately after Monday's deployment.
    *   Resolved in 4 hours.

### 2. Expected Metrics (The Assertions)
*   **Deployment Frequency:** "2 per week" (or "0.29/day").
*   **Lead Time for Changes:** "48 hours" (or "2 days").
*   **Change Failure Rate:** "50%" (1 failure out of 2 deployments).
*   **Time to Restore:** "4 hours".

### 3. Verification Method
We will use the **Playwright MCP Tools** to verify these values:

*   **Summary Cards:**
    *   Use `browser_snapshot` to find the "Lead Time" card.
    *   Assert that the text content contains "2 days" or "48h".
*   **Charts (Visual Check):**
    *   Since reading canvas/SVG charts programmatically is hard, we rely on **summary tooltips** or **legend values** if available in the snapshot.
    *   **Fallback:** We use `browser_take_screenshot` and visually confirm that the "Change Failure Rate" pie chart is split 50/50.