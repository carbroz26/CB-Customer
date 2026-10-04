# CB-Partner — AI Context

**Status:** AUTHORITATIVE STABLE CONTEXT
**Scope:** Frontend repository
**Repository:** `carbroz26/CB-Partner`
**Last Updated:** 2026-10-04

## Project Identity

CB-Partner is the partner/service-provider frontend for a doorstep car-washing platform. It is a standalone frontend project. The backend has a separate repository, documentation system and Trello workspace.

## Product Context

The application serves service partners who perform doorstep car-washing services. Exact product workflows are discovered and frozen feature-by-feature. AI must not invent business workflows.

## Technical Direction

- Kotlin Multiplatform
- Compose Multiplatform
- Clean Architecture
- Pure Store-based MVI
- UDF
- No ViewModel
- Multi-module architecture
- Koin dependency injection
- Ktor Client
- kotlinx.serialization
- Kermit logging
- Navigation 3
- Gradle Convention Plugins

These are the current frozen direction where recorded by the relevant architecture decisions. Feature-specific contracts are authoritative only after their documented freeze.

## Current Platform Topology

Current Gradle/application topology:

```text
Android → :androidApp
Web/Wasm → :webApp
iOS → iosApp/ native Xcode boundary
```

There is currently no `:desktopApp` Gradle project on `feature/dynamic-ui`. Older Base Architecture wording contains historical Desktop direction; do not treat it as a reason to change the current topology without an explicit architecture decision.

## Current Feature State

- Base Architecture 001–017: frozen.
- BASE-ARCH-014: code frozen.
- BASE-ARCH-017: verified/review accepted/code frozen.
- Dynamic DYNAMIC-01 → DYNAMIC-07: discussion frozen.
- Dynamic implementation: in progress.
- Latest owner-provided focused JVM compile/test gate: BUILD SUCCESSFUL on 2026-10-04.

## Session Entry

A new AI session reads `AI_START_HERE.md`, the Master State, Current Status, Project Tracker, relevant frozen architecture/feature documents, and actual Git state before acting.

## Authority

The current user's explicit decision is authoritative for active work. Accepted/frozen project decisions are durable technical authority. Previous conversations are historical context and cannot override current documentation.

## Do Not Assume

Do not assume DI details, networking behavior, navigation behavior, persistence, API behavior, product rules, module names, package names, platform behavior or backend contracts unless documented and accepted.

## Stable Recovery Chain

```text
AI_START_HERE
  ↓
Project Master State
  ↓
Project Tracker
  ↓
Current Status
  ↓
Decision / Architecture records
  ↓
Feature Discussion
  ↓
Implementation Plan
  ↓
Implementation Status
  ↓
Git / Trello / source verification
```

Historical chat is not part of the authoritative recovery chain.
