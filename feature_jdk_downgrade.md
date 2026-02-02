# JDK Downgrade/Verification Plan (Target: JDK 21)

## Current Status Observation
- **`backend/pom.xml`**: `<java.version>` is already set to `21`.
- **`README.md`**: States `Java: 21`.
- **Conclusion**: The project configuration appears to already be targeting JDK 21. The "downgrade" might have already happened or the project never moved to 25.

## Objective
Ensure the project is fully compatible with JDK 21 and remove any accidental dependencies on JDK 25 features or configuration artifacts if they exist.

## Analysis
- **Target State:** Full JDK 21 (LTS) compatibility.
- **Potential Issues:**
    - Although `pom.xml` says 21, a developer might have been running JDK 25 locally and used newer API features (e.g., from preview) that `javac` might have allowed if strict release flags weren't fully enforced or if the local environment overrode them.
    - Maven Wrapper might be on a version that defaults to a newer JDK if available? (Unlikely, wrapper just wraps maven).

## Migration/Verification Steps

1.  **Verify Configuration (Already Done - Passed)**
    - `backend/pom.xml` sets `<java.version>21</java.version>`.
    - Compiler plugin release/source/target should be derived from this property.

2.  **Verify Source Code Compatibility**
    - **Action:** Run a clean build using a strict JDK 21 environment if possible, or ensure the Maven compiler plugin is enforcing `--release 21`.
    - **Command:** `./mvnw clean verify` (This will compile and run tests).
    - **Check:** Look for any "invalid source release" or "cannot find symbol" errors that would indicate usage of post-21 APIs.

3.  **Dependency Check**
    - Ensure all dependencies (Spring Boot 3.4.1, etc.) are compatible with Java 21. (Spring Boot 3.4.x supports Java 17-23, so 21 is fully supported).

4.  **Documentation**
    - `README.md` is already correct.
    - `feature_jdk_downgrade.md`: This file serves as the record of this check.

## Execution Plan
1.  Run `./mvnw clean verify` in the `backend` directory.
2.  If build passes, the project is successfully confirmed as JDK 21 compliant.
3.  If build fails, analyze errors to identify JDK 25isms and refactor.