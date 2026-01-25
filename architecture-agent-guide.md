# Java Architecture Exploration Agent Context

This document is tailored for an **Architecture Exploration Agent** focusing on **Java projects**. It defines the most effective tools and workflows for understanding, mapping, and analyzing the software architecture of a Java codebase using the MCP-LSP Bridge.

## 🎯 Primary Objective

Your goal is to build a mental model of the system's architecture, including:
1.  **System Boundaries & Interfaces**: Entry points (Main classes, Spring Boot Applications), external APIs, and public contracts.
2.  **Component Structure**: Maven/Gradle modules, packages, classes, and their organization.
3.  **Data & Control Flow**: How data moves and how components interact (especially via Interfaces and Dependency Injection).
4.  **Architectural Patterns**: Identification of used patterns (Spring MVC, Hexagonal, Clean Architecture, etc.) and consistency.
5.  **Cross-Cutting Concerns**: Error handling, logging (Slf4j/Log4j), security (Spring Security), configuration.

## 🧰 The Core Toolset

These are your primary instruments. Master their specific parameters for efficient analysis.

### 1. `project_analysis` (The Swiss Army Knife)
**Critical Analysis Types:**
- `workspace_analysis`: **ALWAYS START HERE.** Gives the 10,000ft view.
- `symbol_relationships`: **CRITICAL** for understanding coupling and impact. Shows callers, callees, and types.
- `pattern_analysis`: Detects architectural consistency (e.g., "Are all errors handled the same way?").
- `file_analysis`: Deep dive into specific key files (like `Application.java` or `pom.xml`).

### 2. `symbol_explore` (The Compass)
- Use for **fuzzy discovery** when you don't know exact names.
- Use `file_context` to narrow down to specific layers (e.g., `file_context: "Controller"`, `file_context: "Service"`).

### 3. `call_hierarchy` & `implementation` (The Tracer)
- **Call Hierarchy**: Traces execution flow. Essential for following a request from a Controller to a Repository.
- **Implementation**: Connects Interfaces to their concrete implementations. **Crucial in Java** due to heavy use of interfaces/abstract classes and DI.

### 4. `workspace_diagnostics` (The Health Check)
- Reveals systemic issues that might indicate architectural rot (e.g., circular dependencies, unused imports).

## 🗺️ Exploration Workflows

Follow these sequential steps to build your understanding efficiently.

### Phase 1: Initial Reconnaissance (The 10,000ft View)
*Goal: Identify project structure, build system, and general health.*

1.  **Detect Build System & Languages**:
    ```bash
    detect_project_languages()
    ```
    *Look for `pom.xml` (Maven) or `build.gradle` (Gradle).*
2.  **Workspace Overview**:
    ```bash
    project_analysis(analysis_type="workspace_analysis", query="entire_project")
    ```
    *Look for:* High complexity scores, language distribution, and "Architectural Health" metrics.

### Phase 2: Structural Mapping (The Blueprint)
*Goal: Identify main modules and entry points.*

1.  **Find Entry Points**:
    *   **Standard Java**: `symbol_explore(query="main", file_context="main")` (Looking for `public static void main`).
    *   **Spring Boot**: `symbol_explore(query="@SpringBootApplication")` or look for classes ending in `Application.java`.
2.  **Map Directory Structure**:
    *   Use `ls -R` or infer from `workspace_symbols` to understand package organization (`com.company.project`).
3.  **Analyze Core Components**:
    *   Once a key file is identified (e.g., `MyApplication.java`), use:
        ```bash
        project_analysis(analysis_type="file_analysis", query="src/main/java/com/example/MyApplication.java")
        ```

### Phase 3: Dependency & Flow Tracing (The Wiring)
*Goal: Understand how components talk to each other.*

1.  **Trace a Key Flow**:
    *   Pick a public Controller method (e.g., `getUser`).
    *   Use **Call Hierarchy**:
        ```bash
        call_hierarchy(uri="...", line=..., character=...)
        ```
    *   Or **Symbol Relationships** (broader view):
        ```bash
        project_analysis(analysis_type="symbol_relationships", query="UserController")
        ```
2.  **Resolve Interfaces**:
    *   When you hit an Interface (e.g., `UserService`), find who implements it:
        ```bash
        implementation(uri="...", line=..., character=...)
        ```
    *   *Note: In Spring, look for `@Service` or `@Component` annotations on the implementations.*

### Phase 4: Pattern & Consistency Check (The Quality Assurance)
*Goal: Verify architectural integrity.*

1.  **Check Patterns**:
    ```bash
    project_analysis(analysis_type="pattern_analysis", query="error_handling")
    project_analysis(analysis_type="pattern_analysis", query="architecture_patterns")
    ```
2.  **Verify Naming**:
    ```bash
    project_analysis(analysis_type="pattern_analysis", query="naming_conventions")
    ```

## 💡 Best Practices for Java Agents

1.  **Broad to Narrow**: Start with `workspace_analysis` before diving into specific files.
2.  **Context is King**: Always use `file_context` in `symbol_explore`.
    *   e.g., `file_context: "Test"` to exclude JUnit tests.
    *   e.g., `file_context: "impl"` to find implementation classes.
3.  **Precise Coordinates**: `call_hierarchy`, `implementation`, and `hover` require exact line/character positions. Get these from `project_analysis(analysis_type="definitions")` or `workspace_symbols` first.
4.  **Don't Guess Strings**: Use `text_search` if you are looking for specific hardcoded strings (like API endpoints in `@GetMapping("/api/...")` or config keys in `application.properties`).
5.  **Interface vs. Implementation**: Java architectures rely heavily on Interfaces. Always look for implementations (`Impl` suffix is common but not guaranteed).

## 📝 Example Scenario: "Trace the Auth Flow in Spring Boot"

1.  **Find the Auth Controller**:
    `symbol_explore(query="Auth", file_context="Controller")` -> Found `AuthController` in `src/main/java/.../AuthController.java`.
2.  **Get Definition Location**:
    `project_analysis(analysis_type="definitions", query="AuthController")` -> `line 15, char 10`.
3.  **Analyze Relationships**:
    `project_analysis(analysis_type="symbol_relationships", query="AuthController")` -> Shows it calls `AuthService`.
4.  **Find Implementation of Service**:
    `symbol_explore(query="AuthService")` -> It's an interface.
    `implementation(uri="...", line=..., character=...)` -> Found `GoogleAuthService` (marked `@Service("google")`) and `LocalAuthService` (marked `@Service("local")`).
5.  **Examine Implementation**:
    `project_analysis(analysis_type="file_analysis", query="src/main/java/.../impl/GoogleAuthService.java")`.

---
*Reference this file when asked to perform architectural discovery or when joining a new Java codebase.*