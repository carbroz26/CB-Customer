# CB-Partner Dynamic UI — Implementation Status

**Status:** IMPLEMENTATION IN PROGRESS — DOCUMENTATION RECONCILED 2026-10-04  
**Branch:** `feature/dynamic-ui`

DYNAMIC-01 → DYNAMIC-07 discussion remains frozen. This document records what is actually implemented, what is only a target, and what must be implemented next. A green JVM build is not treated as architectural completion.

## 1. Frozen decisions now reflected

- Splash and Bootstrap are one startup flow.
- Bootstrap Option B is the required implementation boundary.
- `DynamicDestination` is the single canonical destination for Bootstrap `nextScreen`, Dynamic response destinations and Dynamic `navigate` actions.
- `DynamicDestination` is a shared domain model under `domain.model`, not a Dynamic-specific or Bootstrap-specific model.
- `DynamicNextScreen` is not a domain model.
- Dynamic response `nextScreen` belongs inside response `data`; it is not a root-level response field.
- `StartupConfig`, `NextScreenConfig`, and standalone `BootstrapConfig` remain legacy Bootstrap artifacts until the Bootstrap model cleanup is completed.
- Dynamic feature use cases remain feature orchestration; they are not moved into top-level `:domain`.
- Network errors are classified by actual HTTP/transport/protocol cause rather than collapsed into `UnknownException`.

Final reconciliation record: `docs/features/dynamic/03-FINAL-RECONCILIATION-2026-10-04.md`.

## 2. Last verified JVM baseline

The owner ran on 2026-10-04 before the latest shared-destination/model refactor:

```text
:domain:compileKotlinJvm
:data:compileKotlinJvm
:feature:splash:compileKotlinJvm
:feature:dynamic:compileKotlinJvm
:app:compileKotlinJvm

:data:jvmTest
:domain:jvmTest
:feature:splash:jvmTest
:feature:dynamic:jvmTest
```

Result:

```text
BUILD SUCCESSFUL
```

This confirms the previous JVM baseline only. The latest model changes must be rerun locally before being considered verified.

## 3. Implemented areas

### Application / Navigation

- `App.kt` is the application composition entry point.
- `ApplicationContent.kt` composes Splash and Dynamic.
- `DynamicNavigationState` owns the Dynamic destination stack.
- `DynamicContainer` is the permanent Dynamic rendering host.
- `DynamicDestination` is now a shared domain model.

### Dynamic Store / feature orchestration

- `DynamicStore` exists as the Pure Store/MVI/UDF orchestration boundary.
- `LoadDynamicScreenUseCase` exists.
- `ExecuteDynamicActionUseCase` exists.
- These use cases remain feature-owned.

### Registry / Renderer

- `DynamicRegistry` exists with explicit category registration.
- Initial frozen vocabulary is implemented.
- `DynamicRenderer` exists as the recursive rendering boundary.

### Dynamic data / network

- `DynamicRepository` and `DynamicRepositoryImpl` exist.
- `DynamicRemoteDataSource` exists.
- Dynamic DTO/model and mapper exist.
- Shared `:network` Ktor/RequestHandler infrastructure exists.
- Dynamic response mapping now places `nextScreen` inside `DynamicScreen.data` and maps it directly to the shared `DynamicDestination`.
- `DynamicNextScreenDto` / `DynamicNextScreen` have been removed in favor of the shared destination contract.

### Bootstrap baseline

- Bootstrap API contract is known.
- Bootstrap mapping to the canonical destination exists.
- Bootstrap repository/data-source structure exists in source.
- Bootstrap tests exist and were green in the last owner-provided JVM run.

## 4. Current Bootstrap implementation gap — NEXT

The source still requires final Option B reconciliation.

Required production path:

```text
ApplicationBootstrap
    ↓
BootstrapRepository
    ↓
BootstrapRepositoryImpl
    ↓
BootstrapRemoteDataSource
    ↓
RequestHandler
```

Required changes:

1. Remove/rework `DefaultApplicationBootstrap` so it is not a direct HTTP implementation.
2. Complete the canonical Bootstrap domain result model.
3. Remove legacy `StartupConfig`, `NextScreenConfig`, and standalone `BootstrapConfig` domain contracts.
4. Keep DTO → domain conversion in `BootstrapMapper`.
5. Keep exactly one production Bootstrap HTTP path.
6. Update Bootstrap DI and tests after the model cleanup.

## 5. Canonical destination / response-data status

The canonical runtime relationship is now:

```text
Bootstrap response data.startup.nextScreen ──┐
Dynamic response data.nextScreen ────────────┼──→ DynamicDestination
Dynamic navigate action payload ─────────────┘
```

This is one destination concept and one navigation mechanism. The three lines are inputs, not separate destination systems.

The Dynamic response contract is now:

```text
DynamicResponse
  └── data: DynamicScreen
        └── nextScreen: DynamicDestination?
```

Arbitrary non-screen response data remains in `DynamicResponse.dataPayload`.

Remaining verification:

- [ ] Compile all affected modules after latest model refactor.
- [ ] Run Dynamic mapper tests.
- [ ] Verify action-request navigation reads `response.data.nextScreen`.
- [ ] Verify Bootstrap and Dynamic destinations use the exact same domain type.
- [ ] Verify push/replace/pop/back operate on the same destination stack.

## 6. Domain / feature package cleanup

Current historical package:

```text
feature/dynamic/.../domain/usecase/
```

Target:

```text
feature/dynamic/.../usecase/
```

This is a package-ownership cleanup only. The use cases remain feature-level and are not moved to `:domain`.

## 7. Network exception implementation gap

Frozen classification:

```text
BadRequestException
UnauthorizedException
ForbiddenException
NotFoundException
ConflictException
ValidationException
RateLimitException
ServerException
NetworkConnectionException
TimeoutException
SerializationException
ProtocolException
UnknownNetworkException
```

Implementation must first inspect existing `NetworkException`, `DefaultError`, `RequestHandler` and response/error types so equivalent classes are not duplicated.

## 8. Remaining Dynamic work

| Priority | Work | State |
|---:|---|---|
| 1 | Complete Bootstrap Option B implementation | OPEN / NEXT |
| 2 | Remove legacy Bootstrap domain model duplicates | OPEN |
| 3 | Complete Bootstrap DI and tests after model cleanup | OPEN |
| 4 | Verify shared DynamicDestination runtime hand-off | OPEN |
| 5 | Clean Dynamic use-case package ownership | OPEN |
| 6 | Complete request metadata/authentication | OPEN |
| 7 | Complete frozen Dynamic state/reference/action behavior | IN PROGRESS |
| 8 | Implement frozen network exception taxonomy | OPEN |
| 9 | Complete DYNAMIC-07 fallback/recovery/diagnostic/observability behavior | OPEN |
| 10 | Login → Send OTP → OTP → Verify OTP integration tests | OPEN |
| 11 | Full destination push/replace/pop/back runtime verification | OPEN |
| 12 | Android verification | OPEN |
| 13 | iOS/macOS-hosted verification | OPEN |
| 14 | Web/Wasm verification | OPEN |
| 15 | Literal plan-to-source compliance review | OPEN |
| 16 | Final Dynamic review and code freeze | OPEN |

## 9. Explicitly not complete

- Bootstrap Option B is not yet source-complete.
- Legacy Bootstrap domain model cleanup is not yet source-complete.
- Final Bootstrap DI/test reconciliation is not complete.
- Canonical destination runtime verification is not complete.
- Latest shared-destination/model refactor is not yet locally build-verified.
- Network exception taxonomy is frozen but not yet implemented/verified.
- Login → OTP → Dashboard end-to-end verification is pending.
- DYNAMIC-07 production implementation is pending.
- Full platform verification is pending.
- Final Dynamic review/freeze is pending.

## 10. Implementation rules for the next work

- Do not reopen DYNAMIC-01 → DYNAMIC-07.
- Do not introduce a Bootstrap-specific navigation system.
- Do not create Login/OTP/Dashboard-specific screen classes.
- Do not create a separate `DynamicNextScreen` model.
- Do not place Dynamic response `nextScreen` at the response-envelope root.
- Do not move feature orchestration use cases into top-level `:domain`.
- Do not add network exception classes without checking existing error types first.
- Do not consider compilation success equivalent to architecture completion.
- Update this status after each implementation unit and verification gate.

## 11. Next action

**Continue Bootstrap Option B implementation and legacy Bootstrap model cleanup. After the current shared-destination/model refactor passes compile/tests, complete DI and tests, then verify the Bootstrap → Dynamic navigation hand-off before continuing the remaining Dynamic gaps.**
