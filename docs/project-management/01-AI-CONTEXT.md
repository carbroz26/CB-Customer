# CB-Partner — AI Context

**Status:** AUTHORITATIVE STABLE CONTEXT
**Scope:** Frontend repository
**Last Updated:** 2026-10-07

## Project identity

CB-Partner is the CarBroz partner/service-provider frontend for a doorstep car-washing platform. The backend is a separate repository and documentation system.

## Technical direction

- Kotlin Multiplatform
- Compose Multiplatform
- Clean Architecture
- Pure Store/MVI/UDF
- No ViewModel
- Multi-module Gradle
- Gradle Convention Plugins
- Koin
- Ktor Client
- kotlinx.serialization
- kotlinx.coroutines
- Kermit
- Navigation 3

These are binding where recorded by frozen architecture decisions.

## Current platform topology

```text
Android → :androidApp
Web/Wasm → :webApp
iOS → iosApp/ native Xcode boundary
```

The current Dynamic branch does not expose a Gradle `:desktopApp` project. Do not add/remove platform structure without an explicit architecture decision.

## Current feature state

- BASE-ARCH-001 → BASE-ARCH-017: frozen.
- BASE-ARCH-014: code-frozen.
- BASE-ARCH-017: verified/review-accepted/code-frozen.
- Dynamic architecture: frozen.
- Dynamic implementation plan: frozen and implemented.
- Dynamic source deep audit: complete.
- Latest Dynamic JVM matrix: passing on 2026-10-07.
- Current branch: `feature/dynamic-ui`.
- Latest remote commit after Dynamic test reconciliation: `b7aab20`.
- Documentation consolidation is the current project-management task.
- Backend local OTP provider work remains separate.

## Stable recovery chain

```text
AI_START_HERE
  ↓
Project Master State
  ↓
Decision Log
  ↓
Relevant architecture / feature / API / build document
  ↓
Git + source + tests
```

## Do not assume

Do not assume DI details, networking behavior, navigation behavior, persistence, API behavior, product rules, module names, package names, platform behavior or backend behavior unless documented and accepted.

## Documentation rule

There is one current-state document. There is no separate active Current Status, Project Tracker, Checkpoints or Master State copy.

Feature-specific behavior belongs to its feature document. Dynamic behavior belongs only in `docs/dynamic/`. Architecture belongs only in `docs/architecture/`.
