# Java Development & Refactoring Agent Context

This document is tailored for a **Development and Refactoring Agent** focusing on **Java projects**. It defines the most effective tools and workflows for writing, modifying, and improving Java code using the MCP-LSP Bridge.

## 🎯 Primary Objective

Your goal is to efficiently modify the codebase while maintaining high quality, safety, and adherence to existing conventions. This includes:
1.  **Code Creation**: implementing features, adding classes/interfaces, and writing tests.
2.  **Refactoring**: Renaming symbols, extracting methods, and improving structure.
3.  **Maintenance**: Fixing bugs, resolving diagnostics, and formatting code.

## 🧰 The Core Toolset

### 1. `code_actions` (The Fixer)
**Primary Uses:**
- Quick fixes for compiler errors (e.g., "Add unimplemented methods", "Organize imports").
- Refactoring operations (e.g., "Extract variable", "Extract method").
- Generating boilerplate (e.g., "Generate getters and setters", "Generate constructor").

### 2. `rename` (The Safety Net)
- **CRITICAL**: Never manually rename symbols (variables, methods, classes) using search/replace.
- Use `rename` with `apply="false"` first to preview changes across the entire codebase.

### 3. `format_document` (The Standardizer)
- Run this on any file you modify to ensure it meets project style guidelines.
- Use `apply="false"` to check for drift before committing changes.

### 4. `hover` & `signature_help` (The Guide)
- **Hover**: Check types and documentation before using a variable or method.
- **Signature Help**: Ensure correct parameter order when writing method calls.

### 5. `workspace_diagnostics` & `document_diagnostics` (The Linter)
- Check for errors and warnings *before* and *after* making changes.

## 🛠️ Development Workflows

### Phase 1: Preparation (Before You Edit)
*Goal: Understand the context and requirements.*

1.  **Analyze Local Context**:
    ```bash
    get_range_content(uri="...", start_line=..., end_line=...)
    ```
    *Read the code you are about to change.*
2.  **Check for Existing Issues**:
    ```bash
    document_diagnostics(uri="...")
    ```
    *Don't start working on a file that already has broken build errors unless your task is to fix them.*

### Phase 2: Implementation (Making Changes)
*Goal: Write idiomatic Java code.*

#### A. Creating New Classes/Interfaces
1.  **Create File**: Use `write_file` to create the `.java` file.
2.  **Scaffold**: Write the package declaration and class structure.
3.  **Check Imports**: Use `code_actions` to "Organize Imports" or "Add missing imports".

#### B. Modifying Existing Logic
1.  **Understand Dependencies**:
    `hover` on variables to know their types.
    `project_analysis(analysis_type="definitions")` to see where helper methods are defined.
2.  **Apply Changes**: Use `replace` (if available via shell/editor) or `write_file`.
3.  **Refactor**:
    If a method gets too long, use `code_actions` -> "Extract Method".

### Phase 3: Verification (Quality Control)
*Goal: Ensure the code compiles and runs correctly.*

1.  **Verify Syntax & Types**:
    ```bash
    document_diagnostics(uri="...")
    ```
    *Fix any red squiggles immediately.*
2.  **Format Code**:
    ```bash
    format_document(uri="...", apply="true")
    ```
3.  **Run Tests**:
    *   Identify the test framework (JUnit, TestNG).
    *   Run relevant tests using `run_shell_command` (e.g., `mvn test -Dtest=MyClassTest`, `./gradlew test --tests MyClassTest`).

## ♻️ Refactoring Workflows

### safe_rename_symbol
1.  **Locate Symbol**: Use `project_analysis` or `symbol_explore` to find exact coordinates.
2.  **Preview Rename**:
    ```bash
    rename(uri="...", line=..., character=..., new_name="NewName", apply="false")
    ```
3.  **Review Impact**: Check the diffs. Are they what you expected?
4.  **Apply**:
    ```bash
    rename(uri="...", line=..., character=..., new_name="NewName", apply="true")
    ```

### fix_compilation_errors
1.  **Get Diagnostics**: `document_diagnostics`.
2.  **Iterate**:
    *   For each error, get the line/char.
    *   Call `code_actions(uri="...", line=..., character=...)`.
    *   Review available actions (e.g., "Import class", "Change type").
    *   If no code action fits, manually edit.

## 💡 Best Practices for Java Development

1.  **Imports**: Java is picky about imports. Always run an "Organize Imports" action or check `document_diagnostics` after adding code that uses new types.
2.  **Types**: Be strict. Don't use `var` unless the project conventions allow it.
3.  **Tests**: If you add a feature, add a corresponding `*Test.java`. Locate existing tests (`symbol_explore(query="ExistingTest", file_context="test")`) to mimic their setup.
4.  **Lombok**: If the project uses Lombok (`@Data`, `@Getter`), be aware that source code might not show getters/setters, but `symbol_explore` and `hover` will see them.
5.  **Generics**: Pay attention to compiler warnings about raw types.

---
*Reference this file when asked to perform software development, bug fixing, or refactoring tasks in a Java codebase.*
