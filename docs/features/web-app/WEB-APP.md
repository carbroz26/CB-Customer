# Web App

**Status:** CURRENT PLATFORM DOCUMENT
**Scope:** Web/Wasm application boundary and developer verification

## 1. Boundary

`webApp/` is the Web/Wasm application boundary.

It hosts the shared application root and platform-specific Web entry point. Web-specific concerns must remain at this boundary rather than leaking into shared Dynamic/domain architecture.

## 2. Run

From the repository root:

```powershell
.\gradlew.bat :webApp:wasmJsBrowserDevelopmentRun
```

On macOS/Linux:

```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

Stop with `Ctrl+C`.

## 3. Verification

Before claiming Web verification:

1. Confirm the WASM compile task succeeds.
2. Start the browser development server.
3. Load the application.
4. Verify the startup → Dynamic flow that is in scope.
5. Use browser Developer Tools for browser-side Console and Network inspection.

The Gradle terminal is the dev-server process; browser `println()` output is not guaranteed to appear there.

## 4. Non-goals

This document does not define:
- Dynamic architecture;
- API contracts;
- authentication architecture;
- navigation architecture;
- shared Store/MVI rules;
- Gradle version policy.

Those belong to their respective authoritative documents.

## 5. Current state

Web is an application/platform boundary. Runtime verification remains a platform verification task and must be recorded in the Project Master State rather than in a duplicate Web status history.

## 6. Related authority

- Build/setup: `docs/build/GRADLE-SETUP-AND-WORKFLOW.md`
- Architecture: `docs/architecture/ARCHITECTURE.md`
- Dynamic: `docs/dynamic/DYNAMIC-ARCHITECTURE.md`
- Current state: `docs/project-management/04-PROJECT-MASTER-STATE.md`
