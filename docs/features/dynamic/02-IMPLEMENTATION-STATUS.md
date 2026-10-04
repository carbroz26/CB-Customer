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

## 4. Phase B — Dynamic use-case package ownership

**Status: VERIFIED**

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

The latest owner-provided full compile/JVM verification is green.

## 5. Phase C-16 — Request metadata/authentication integration

**Status: IMPLEMENTED / VERIFICATION PENDING**

Implemented strictly as request metadata. `DynamicDestination` remains unchanged.

The request path now carries:

```text
DynamicAction payload
├── endpoint
├── method
└── authentication
        ↓
ExecuteDynamicActionUseCase
        ↓
DynamicRepository
        ↓
DynamicRepositoryImpl
        ↓
DynamicRemoteDataSource
        ↓
RequestHandler
```

Rules implemented:

- `authentication` defaults to `NONE` when omitted.
- `NONE` and `BEARER` are the currently supported authentication metadata values.
- Unsupported authentication values fail at the network request boundary instead of being silently ignored.
- No token store, credential provider, or new authentication architecture was introduced.
- Existing Ktor `Auth` infrastructure remains the transport mechanism for bearer authentication when credentials are later available.
- `method` and `authentication` remain request metadata and are not added to `DynamicDestination`.
- Dynamic action tests now verify authentication propagation and the `NONE` default.

## 6. Remaining work

| Priority | Work | State |
|---:|---|---|
| 1 | Verify Phase C-16 request metadata/authentication integration | **NEXT** |
| 2 | Complete frozen Dynamic state/reference/action behavior | PENDING |
| 3 | Implement frozen network exception taxonomy | PENDING |
| 4 | Complete DYNAMIC-07 production fallback/recovery/observability | PENDING |
| 5 | Login → Send OTP → OTP → Verify OTP integration tests | PENDING |
| 6 | Push/replace/pop/back runtime verification | PENDING |
| 7 | Android verification | PENDING |
| 8 | iOS/macOS-hosted verification | PENDING |
| 9 | Web/Wasm verification | PENDING |
| 10 | Literal plan-to-source compliance review | PENDING |
| 11 | Final Dynamic review and code freeze | PENDING |

## 7. Continuation rules

- Do not reopen DYNAMIC-01 → DYNAMIC-08.
- Do not restore `DynamicScreen.nextScreen`.
- Do not restore legacy Bootstrap domain contracts.
- Do not restore `DefaultApplicationBootstrap` as a second production HTTP path.
- Do not add method/authentication to `DynamicDestination`.
- Do not create Login/OTP/Dashboard-specific screen architecture.
- No speculative cleanup or architecture changes outside the frozen plan.

## 8. Next action

Run the full affected compile/JVM verification for Phase C-16 request metadata/authentication integration. If green, immediately continue to the next ordered implementation item: **frozen Dynamic state/reference/action behavior**.
