# CB-Partner Dynamic UI — Implementation Status

**Status:** IMPLEMENTATION IN PROGRESS  
**Updated:** 2026-10-04  
**Branch:** `feature/dynamic-ui`

DYNAMIC-01 → DYNAMIC-07 remain frozen. DYNAMIC-08 remains frozen as the unified four-field `DynamicDestination` contract.

## 1. Phase A — Bootstrap Option B

**Status: VERIFIED**

Completed and verified against the frozen implementation plan/reconciliation:

- `BootstrapRepository` domain contract.
- `BootstrapRepositoryImpl` data orchestration.
- `BootstrapRemoteDataSource` transport boundary.
- Bootstrap DTOs under `data/bootstrap/model`.
- `BootstrapMapper` under `data/bootstrap/mapper`.
- Startup destination mapped to canonical `DynamicDestination`.
- `BootstrapOutput` as the canonical Bootstrap result.
- Legacy `StartupConfig`, `NextScreenConfig`, and standalone `BootstrapConfig` removed from domain.
- Transitional `DefaultApplicationBootstrap` removed.
- DI uses the single `BootstrapRepository` production path.
- Splash/application composition consumes `BootstrapOutput.destination`.
- Bootstrap mapper/repository/Splash tests reconciled.

## 2. Phase A verification — 2026-10-04

Compile:

```text
:domain:compileKotlinJvm
:data:compileKotlinJvm
:feature:splash:compileKotlinJvm
:feature:dynamic:compileKotlinJvm
:navigation:compileKotlinJvm
:app:compileKotlinJvm
--rerun-tasks
```

**BUILD SUCCESSFUL — 43 actionable tasks: 43 executed**

JVM verification:

```text
:core:jvmTest
:domain:jvmTest
:data:jvmTest
:navigation:jvmTest
:feature:splash:jvmTest
:feature:dynamic:jvmTest
:app:compileKotlinJvm
--rerun-tasks
```

**BUILD SUCCESSFUL — 62 actionable tasks: 62 executed**

Known Windows iOS-simulator and Android-host-test warnings are non-fatal.

## 3. Phase B — Shared destination/navigation boundary

The frozen DYNAMIC-08 contract remains:

```text
DynamicDestination
├── screenId
├── templateId
├── templateType
└── endpoint
```

Bootstrap startup destination, Dynamic API response destination, and direct Dynamic navigate action all use this same destination model. No navigation redesign was introduced.

The existing source/test alignment for this contract is already JVM verified. Runtime/platform verification remains a later verification gate.

## 4. Next implementation — Dynamic use-case package ownership

**Status: IMPLEMENTED / VERIFICATION PENDING**

Per the frozen implementation plan, Dynamic feature orchestration use cases were moved from the misleading `domain/usecase` package to the feature-level `usecase` package without creating a second domain layer.

Moved:

```text
feature/dynamic/.../domain/usecase/LoadDynamicScreenUseCase.kt
    → feature/dynamic/.../usecase/LoadDynamicScreenUseCase.kt

feature/dynamic/.../domain/usecase/ExecuteDynamicActionUseCase.kt
    → feature/dynamic/.../usecase/ExecuteDynamicActionUseCase.kt

feature/dynamic/.../domain/usecase/ExecuteDynamicActionUseCaseTest.kt
    → feature/dynamic/.../usecase/ExecuteDynamicActionUseCaseTest.kt
```

Updated consumers:

- `DynamicModule.kt`
- `DynamicStore.kt`

No use-case behavior was intentionally changed.

## 5. Remaining work

| Priority | Work | State |
|---:|---|---|
| 1 | Verify Dynamic use-case package cleanup | **NEXT** |
| 2 | Complete request metadata/authentication | PENDING |
| 3 | Complete frozen Dynamic state/reference/action behavior | PENDING |
| 4 | Implement frozen network exception taxonomy | PENDING |
| 5 | Complete DYNAMIC-07 production fallback/recovery/observability | PENDING |
| 6 | Login → Send OTP → OTP → Verify OTP integration tests | PENDING |
| 7 | Push/replace/pop/back runtime verification | PENDING |
| 8 | Android verification | PENDING |
| 9 | iOS/macOS-hosted verification | PENDING |
| 10 | Web/Wasm verification | PENDING |
| 11 | Literal plan-to-source compliance review | PENDING |
| 12 | Final Dynamic review and code freeze | PENDING |

## 6. Continuation rules

- Do not reopen DYNAMIC-01 → DYNAMIC-08.
- Do not restore `DynamicScreen.nextScreen`.
- Do not restore legacy Bootstrap domain contracts.
- Do not restore `DefaultApplicationBootstrap` as a second production HTTP path.
- Do not add method/authentication to `DynamicDestination`.
- Do not create Login/OTP/Dashboard-specific screen architecture.
- No speculative cleanup or architecture changes outside the frozen plan.

## 7. Next action

Run the full affected compile/JVM verification for the use-case package move. If green, immediately continue to the next ordered implementation item: **request metadata/authentication**.
