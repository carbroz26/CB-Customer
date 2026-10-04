# CB-Partner Dynamic UI — Implementation Status

**Status:** IMPLEMENTATION IN PROGRESS — DOCUMENTATION RECONCILED 2026-10-04  
**Branch:** `feature/dynamic-ui`

DYNAMIC-01 → DYNAMIC-07 discussion remains frozen. This document records what is actually implemented, what is only a target, and what must be implemented next. A green JVM build is not treated as architectural completion.

## 1. Frozen decisions now reflected

- Splash and Bootstrap are one startup flow.
- Bootstrap Option B is the required implementation boundary.
- `DynamicDestination` is the single canonical destination for Bootstrap `nextScreen`, Dynamic API destinations and Dynamic action destinations.
- `StartupConfig`, `NextScreenConfig`, and standalone `BootstrapConfig` are legacy domain artifacts and must not remain as parallel contracts.
- Dynamic feature use cases remain feature orchestration; they are not moved into top-level `:domain`.
- Network errors are classified by actual HTTP/transport/protocol cause rather than collapsed into `UnknownException`.

Final reconciliation record: `docs/features/dynamic/03-FINAL-RECONCILIATION-2026-10-04.md`.

## 2. Verified implementation baseline

The owner ran on 2026-10-04:

```powershell
.\gradlew.bat :network:compileKotlinJvm
.\gradlew.bat :data:compileKotlinJvm
.\gradlew.bat :feature:splash:compileKotlinJvm
.\gradlew.bat :feature:dynamic:compileKotlinJvm
.\gradlew.bat :app:compileKotlinJvm

.\gradlew.bat :network:jvmTest :data:jvmTest :domain:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest
```

Result:

```text
BUILD SUCCESSFUL
54 actionable tasks: 15 executed, 39 up-to-date
```

This confirms the current JVM baseline only.

## 3. Implemented areas

### Application / Navigation

- `App.kt` is the application composition entry point.
- `ApplicationContent.kt` composes Splash and Dynamic.
- `DynamicNavigationState` owns the Dynamic destination stack.
- `DynamicContainer` is the permanent Dynamic rendering host.

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

### Bootstrap baseline

- Bootstrap API contract is known.
- Bootstrap mapping to the canonical destination has been implemented/tested at the current baseline.
- Bootstrap tests are green at the current JVM baseline.

## 4. Bootstrap implementation gap — NEXT

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
2. Implement/verify `BootstrapRepository` as the domain repository contract.
3. Implement/verify `BootstrapRepositoryImpl` as the data orchestration layer.
4. Implement/verify `BootstrapRemoteDataSource` as the Bootstrap transport boundary.
5. Keep DTO → domain conversion in `BootstrapMapper`.
6. Remove legacy `StartupConfig`, `NextScreenConfig`, and standalone `BootstrapConfig` domain contracts.
7. Keep exactly one production Bootstrap HTTP path.
8. Update Bootstrap DI and tests.

## 5. Canonical destination gap check

The final model is:

```text
Bootstrap nextScreen ───────┐
Dynamic API nextScreen ─────┼──→ DynamicDestination
Dynamic action destination ─┘
```

No separate destination models or navigation implementations are authorized.

Required verification:

- Bootstrap creates `DynamicDestination`.
- Dynamic response destination creates the same type.
- `navigate` action creates the same type.
- Push/replace/pop/back operate on the same stack entry type.
- `templateType` never decides business navigation.

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

Classification must distinguish:

- HTTP status failure;
- connection failure;
- timeout;
- serialization/decode failure;
- protocol/malformed response;
- genuinely unknown failure.

Business/API outcomes remain separate from transport exceptions.

## 8. Remaining Dynamic work

| Priority | Work | State |
|---:|---|---|
| 1 | Bootstrap Option B repository/remote-source implementation | OPEN / NEXT |
| 2 | Remove/rework legacy Bootstrap domain models and direct bootstrap path | OPEN |
| 3 | Bootstrap DI and tests | OPEN |
| 4 | Verify Bootstrap → DynamicDestination runtime hand-off | OPEN |
| 5 | Clean Dynamic use-case package | OPEN |
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

- Bootstrap Option B implementation is not yet source-complete.
- Legacy Bootstrap domain model cleanup is not yet source-complete.
- Final Bootstrap DI/test reconciliation is not complete.
- Canonical destination runtime verification is not complete.
- Network exception taxonomy is frozen but not yet implemented/verified.
- Login → OTP → Dashboard end-to-end verification is pending.
- DYNAMIC-07 production implementation is pending.
- Full platform verification is pending.
- Final Dynamic review/freeze is pending.

## 10. Implementation rules for the next work

- Do not reopen DYNAMIC-01 → DYNAMIC-07.
- Do not introduce a Bootstrap-specific navigation system.
- Do not create Login/OTP/Dashboard-specific screen classes.
- Do not move feature orchestration use cases into top-level `:domain`.
- Do not add network exception classes without checking existing error types first.
- Do not consider compilation success equivalent to architecture completion.
- Update this status after each implementation unit and verification gate.

## 11. Next action

**Implement Bootstrap Option B completely, including legacy domain-model cleanup, DI and tests. Then verify the shared `DynamicDestination` runtime hand-off before continuing the remaining Dynamic gaps.**
