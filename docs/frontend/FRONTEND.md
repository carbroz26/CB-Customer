# CB-Partner — Frontend Engineering Guide

**Status:** ACTIVE ENGINEERING GUIDE
**Scope:** Frontend implementation conventions
**Last Updated:** 2026-10-04

## 1. UI Architecture

Compose Multiplatform is the UI technology. Composables render state and emit user events. They do not own business workflows, generic networking, persistence or application orchestration.

Dynamic UI is backend-driven. Generic Dynamic rendering must not contain hardcoded Login/OTP/Dashboard branching.

## 2. State

Feature state follows Pure Store + MVI + UDF. There is no ViewModel layer.

State, intents and externally visible effects must be explicit. A Dynamic destination owns its Dynamic interaction state; business/domain state remains separate.

## 3. Dynamic UI Rules

- `templateType` selects rendering capability only.
- `DynamicDestination` is the navigation/back-stack entry.
- Registry registration is explicit.
- Definitions own their own rendering.
- `DynamicRenderer` handles recursive definition resolution/delegation.
- Actions execute outside renderer definitions.
- No reflection/package scanning.
- No silent unsupported-type substitution.

## 4. Feature Boundaries

A feature must have a clear responsibility and public contract. Avoid creating a common abstraction until multiple real consumers justify it.

Current primary feature boundaries:

- `:feature:splash` — startup/splash state and UI.
- `:feature:dynamic` — Dynamic screen lifecycle, Store, registry, renderer and Dynamic definitions.

## 5. Domain / Data / Network

Typical flow:

```text
UI → Store → Use Case → Repository → Remote Data Source → Network
Network → DTO → Mapper → Domain → Store → UI
```

`:network` owns generic Ktor infrastructure. `:data` owns transport DTOs, mapping, repositories and feature-specific remote sources. Domain remains independent of Data and Ktor.

## 6. Navigation

Navigation remains separate from business state. Dynamic navigation is destination-driven. Never infer where to navigate from `templateType` or screen-name conditionals.

## 7. Design System / Visuals

The project should evolve one shared design system from real product needs. Do not create a giant component library before concrete consumers justify it.

Dynamic visual capabilities should reuse small common helpers rather than a generic property framework.

## 8. Resources / Platform

Prefer common KMP code when behavior is genuinely common. Platform-specific behavior remains explicit. `iosApp/` is a native Xcode boundary, not a Gradle module.

## 9. Accessibility

Features should account for semantics, touch targets, readable contrast, dynamic text where supported, keyboard interaction where relevant, and screen-reader behavior.

## 10. Performance

Avoid unnecessary recomposition, uncontrolled collection, excessive main-thread work and unnecessary state duplication. Optimize from evidence, not speculation.

## 11. Testing

Test state transitions, mapping, action behavior, registry behavior and repository behavior independently where practical. Platform compilation is part of KMP verification.

Current Windows JVM baseline is documented in `docs/build/GRADLE-SETUP-AND-WORKFLOW.md`.

## 12. Code Review

Review for:

- single responsibility;
- state ownership;
- lifecycle safety;
- platform correctness;
- accessibility;
- testability;
- explicit dependencies;
- unnecessary abstraction;
- duplicate ownership;
- frozen architecture compliance.

## 13. Current Dynamic Code-Quality Rule

```text
one responsibility
small public API
constructor-injected dependencies
obvious data flow
no hidden work
no unnecessary abstraction
no duplicate ownership
```

## 14. Related Authority

- Architecture: `docs/architecture/ARCHITECTURE-CURRENT-STATE.md`
- Frozen base architecture: `docs/architecture/BASE-ARCHITECTURE.md`
- Dynamic discussion: `docs/features/dynamic/00-FEATURE-DISCUSSION.md`
- Dynamic implementation plan/status: `docs/features/dynamic/01-IMPLEMENTATION-PLAN.md`, `02-IMPLEMENTATION-STATUS.md`
- Current cross-session map: `docs/project-management/14-PROJECT-MASTER-STATE.md`
