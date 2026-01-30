# Feature Plan: Robust Metrics & Heuristics

**Objective:** Enhance the `LeadTimeCalculator` to correctly link Changes (Pull Requests) to Deployments (Releases) in complex, real-world repositories where strict SHA matching fails. This is a critical step for "Production Readiness".

## 1. The Challenge
Real-world repositories (like `microsoft/vscode` or `google/guava`) often have complex release processes:
- **Squash Merges:** The commit SHA on the main branch differs from the PR's merge commit SHA.
- **Release Branches:** Releases are tagged on a separate branch, not the direct `main` branch commit.
- **Manual Releases:** Releases might be triggered manually after a batch of PRs.

**Current State:** `LeadTimeCalculator` only links if `Change.commitSha == Deployment.commitSha`. This results in **0 metrics** for most real repos.

## 2. Proposed TDD Workflow (The "Capture & Replay" Strategy)

We will use a data-driven TDD approach to implement richer heuristics without flakiness or rate-limiting issues.

### Phase 7.1: Test Harness Setup
1.  **Data Capture:** Create a utility `DataCapturer` to fetch and save real JSON responses from GitHub API for:
    -   `microsoft/vscode` (High volume, complex)
    -   `google/guava` (Standard Java lib)
    -   Save these as `src/test/resources/datasets/vscode/deployments.json` and `changes.json`.
2.  **Mocking:** Create `ComplexHeuristicsTest` that uses `MockSourceControlAdapter` to load these JSON files instead of hitting the API.
3.  **Baseline Failure:** Write a test case that asserts `LeadTimeCalculator` returns 0 duration/matches with the current logic. This confirms the problem.

### Phase 7.2: Iterative Heuristic Implementation
We will implement a **Chain of Responsibility** (or Strategy List) for matching. The calculator will try them in order.

#### Strategy A: Exact Match (Existing)
- **Logic:** `Change.sha == Deployment.sha`
- **Status:** Already implemented.
- **Priority:** 1 (Highest Confidence)

#### Strategy B: Release Body Parser (The "Linker")
- **Logic:** Parse the `Deployment.description` (Release Notes). Look for GitHub auto-generated links like `.../pull/123` or `#123`.
- **Implementation:**
    -   Regex match `/(?:#|pull\/)(\d+)/`.
    -   Find the corresponding `Change` by PR number.
- **Priority:** 2 (High Confidence)

#### Strategy C: Time Window & Graph Reachability (The "Net")
- **Logic:** If a PR was merged *before* the deployment and *after* the previous deployment.
- **Refinement:**
    -   Strict: `PreviousDeployment.date < Change.mergedAt < CurrentDeployment.date`
    -   Graph (Optional/Advanced): Verify `Change.sha` is an ancestor of `Deployment.sha`. (Requires fetching commits, maybe Phase 8).
- **Priority:** 3 (Medium Confidence - Fallback)

### Phase 7.3: Verification
1.  Run `ComplexHeuristicsTest`.
2.  Assert that match rate improves from 0% to > 50%.
3.  Verify that `LeadTime` is a reasonable positive value (e.g., hours or days, not 0).

## 3. Implementation Plan (Checklist)

- [x] **Create `DataCapturer.java`** (Temporary main class to fetch & save JSONs).
- [x] **Capture Data:** Run capturer for `microsoft/vscode`.
- [x] **Create `ComplexHeuristicsTest.java`**:
    -   [x] Load JSONs.
    -   [x] Assert `calculate()` returns valid Duration (not null/zero).
- [x] **Implement `ReleaseBodyStrategy`**:
    -   [x] Add to `LeadTimeCalculator`.
    -   [x] Verify test passes (Green).
- [x] **Implement `TimeWindowStrategy`**:
    -   [x] Add to `LeadTimeCalculator`.
    -   [x] Handle edge cases (first deployment).
- [x] **Refactor**: Ensure `LeadTimeCalculator` remains clean.

## 4. Dependencies
- No new libraries needed. Use standard Java `regex` and `java.time`.
