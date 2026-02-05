# JDK Downgrade/Verification Plan (Target: JDK 21)

## Current Status Observation
- **`backend/pom.xml`**: `<java.version>` is set to `21`.
- **`README.md`**: States `Java: 21`.
- **System Java**: `openjdk version "21.0.9"` verified.
- **Verification Status**: **PASSED** (2026-02-05)

## Objective
Ensure the project is fully compatible with JDK 21 and remove any accidental dependencies on JDK 25 features or configuration artifacts if they exist.

## Analysis
- **Target State:** Full JDK 21 (LTS) compatibility.
- **Potential Issues:**
    - Although `pom.xml` says 21, a developer might have been running JDK 25 locally and used newer API features (e.g., from preview) that `javac` might have allowed if strict release flags weren't fully enforced or if the local environment overrode them.
    - Maven Wrapper might be on a version that defaults to a newer JDK if available? (Unlikely, wrapper just wraps maven).

## Migration/Verification Steps

1.  **Verify Configuration (Done)**
    - `backend/pom.xml` sets `<java.version>21</java.version>`.
    - Compiler plugin release/source/target derived from this property.

2.  **Verify Source Code Compatibility (Done)**
    - **Action:** Run a clean build using a strict JDK 21 environment.
    - **Command:** `./mvnw clean verify`
    - **Result:** BUILD SUCCESS. No "invalid source release" or "cannot find symbol" errors.

3.  **Dependency Check (Done)**
    - Dependencies (Spring Boot 3.4.1) are compatible with Java 21.

4.  **Documentation (Done)**
    - `README.md` is correct.
    - `feature_jdk_downgrade.md`: Updated with verification results.

## Execution Plan
1.  Run `./mvnw clean verify` in the `backend` directory. -> **DONE**
2.  If build passes, the project is successfully confirmed as JDK 21 compliant. -> **CONFIRMED**
3.  If build fails, analyze errors to identify JDK 25isms and refactor. -> **N/A**
