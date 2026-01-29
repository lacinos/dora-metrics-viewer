# Feature Plan: Robust Metrics & Heuristics

**Objective:** Enhance the `LeadTimeCalculator` to correctly link Changes (PRs) to Deployments (Releases) in complex, real-world repositories where strict SHA matching fails (due to squashing, rebasing, or release branching).

## 1. The Problem
Currently, `LeadTimeCalculator` relies on:
```java
if (change.commitSha().equals(deployment.commitSha()))
```
**Why this fails:**
- **Squash Merges:** The PR commit SHA differs from the commit on the main branch.
- **Release Branches:** The Release tag points to a commit on a branch, not the direct merge commit of a feature.
- **Result:** `LeadTime = 0` (No matches found).

## 2. TDD Workflow: "Capture & Replay"

To solve this without hitting API rate limits or dealing with changing live data, we will use a **Data-Driven TDD** approach.

### Step 2.1: Create Data Capturer (Test Utility)
Create a temporary utility (test-only) to fetch real data from GitHub and dump it to JSON files in `src/test/resources/datasets/vscode`.
- `deployments.json`: Real response from `GET /repos/microsoft/vscode/releases`
- `changes.json`: Real response from `GET /repos/microsoft/vscode/pulls`

### Step 2.2: Create `ScenarioBasedTest`
Create a new test class `ComplexHeuristicsTest.java`.
- **Setup:** Load `deployments.json` and `changes.json` into a `MockSourceControlAdapter`.
- **Test Case 1 (Baseline):** Assert that current logic returns 0 matches (validating the problem).
- **Test Case 2 (Robust):** Assert that we find > 50% matches using better heuristics.

## 3. Implementation Strategy: The Heuristic Ladder

We will implement a "Chain of Responsibility" or a prioritized list of matching strategies.

### Heuristic A: Exact Match (Existing)
*Keep as high priority.*
- Logic: `PR.mergeCommitSha == Release.targetCommitish`

### Heuristic B: Release Body Parsing (High Precision)
GitHub often auto-generates release notes containing PR links.
- Logic: Parse `Release.body`. Look for patterns like `#123` or `https://github.com/.../pull/123`.
- Action: If `Release.body` contains the PR number, link them.

### Heuristic C: Time-Window Bracketing (Fallback)
If a PR is merged *before* a release, and *after* the previous release, it belongs to that release bucket.
- Logic:
    1. Sort Releases by Date.
    2. Sort Changes by `mergedAt`.
    3. Iterate Releases. A Change belongs to Release `R(i)` if:
       `R(i-1).date < Change.mergedAt <= R(i).date`

## 4. Execution Steps

1.  **Harness:** Create the JSON capture script and capture `microsoft/vscode` data.
2.  **Red Test:** Create the test harness that loads these JSONs and asserts the current failure (0 lead time).
3.  **Green (Heuristic B):** Implement Release Body parsing. Run test.
4.  **Green (Heuristic C):** Implement Time-Window logic. Run test.
5.  **Refactor:** Clean up the `LeadTimeCalculator` to be clean and readable.

## 5. Definition of Done
- [ ] `ComplexHeuristicsTest` passes with the `vscode` dataset.
- [ ] `LeadTimeCalculator` is decoupled from the specific matching logic (Strategy Pattern).
- [ ] Existing tests still pass.
