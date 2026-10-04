# CB-Partner — Current Status

**Status:** ACTIVE  
**Last Updated:** 2026-10-04

## Current Phase

Phase 1 — Technical Foundation → Dynamic UI vertical slice implementation.

## Current Objective

Complete the backend-driven Dynamic UI runtime and verify the first real product flow through Login → Send OTP → OTP → Verify OTP without introducing business-specific frontend screen architecture.

## Frozen State

- BASE-ARCH-001 → BASE-ARCH-017: **DECISION_FROZEN**
- BASE-ARCH-014: **CODE_FROZEN**
- BASE-ARCH-017: **VERIFIED / REVIEW ACCEPTED / CODE_FROZEN**
- DYNAMIC-01 → DYNAMIC-07: **DISCUSSION FROZEN**
- DYNAMIC-08 unified destination contract: **FROZEN**
- Dynamic implementation plan: **PLAN_FROZEN / IMPLEMENTATION AUTHORIZED**
- Pure Store + MVI/UDF: frozen direction
- No ViewModel: frozen
- Backend-driven Dynamic navigation/rendering: frozen
- Bootstrap Option B: **FROZEN AS IMPLEMENTATION BOUNDARY**
- One canonical four-field `DynamicDestination`: **FROZEN**
- Network exception classification: **FROZEN FOR IMPLEMENTATION**

## Current Work Item

**DYNAMIC-UI-001 — Dynamic UI vertical slice**

Branch:

```text
feature/dynamic-ui
```

## Latest Verification — 2026-10-04

The latest owner-provided verification passed after the DYNAMIC-08 destination-contract reconciliation.

Compile verification:

```powershell
.\gradlew.bat `
  :domain:compileKotlinJvm `
  :data:compileKotlinJvm `
  :feature:splash:compileKotlinJvm `
  :feature:dynamic:compileKotlinJvm `
  :navigation:compileKotlinJvm `
  :app:compileKotlinJvm `
  --rerun-tasks
```

Result: **BUILD SUCCESSFUL — 43 actionable tasks: 43 executed**.

JVM test verification:

```powershell
.\gradlew.bat `
  :core:jvmTest `
  :domain:jvmTest `
  :data:jvmTest `
  :navigation:jvmTest `
  :feature:splash:jvmTest `
  :feature:dynamic:jvmTest `
  :app:compileKotlinJvm `
  --rerun-tasks
```

Result: **BUILD SUCCESSFUL — 62 actionable tasks: 62 executed**.

Therefore the currently selected JVM compile/test set is green. The Gradle output contains known Windows-only warnings for disabled iOS simulator tests and Android host-test configuration; these are warnings, not failures.

## Completed Since Previous Verification

- Restored the `DynamicActionDto` mapper import and resolved the response-mapper compile ambiguity.
- Aligned response-mapper tests with the unified destination contract.
- Aligned the Bootstrap repository test with the unified destination contract.
- Removed stale test expectations for `nextScreen`, `method`, and `authentication` from the affected destination/screen assertions.
- Verified the affected modules compile successfully.
- Verified the selected common/JVM test suites pass.

## Startup / Bootstrap State

Splash and Bootstrap remain one startup flow:

```text
Splash UI / Store
 → ApplicationBootstrap
 → BootstrapRepository
 → BootstrapRepositoryImpl
 → BootstrapRemoteDataSource
 → RequestHandler
 → Bootstrap DTO
 → BootstrapMapper
 → BootstrapOutput
 → DynamicDestination
```

Selected implementation boundary: **Option B**.

The remaining Bootstrap work is implementation cleanup/integration, not architecture discussion.

Transitional code still requiring implementation cleanup:

```text
DefaultApplicationBootstrap → RequestHandler
```

Legacy domain contracts that must not remain as parallel contracts:

```text
StartupConfig
NextScreenConfig
standalone BootstrapConfig
```

`BootstrapOutput` is the canonical Bootstrap result, and its startup destination is the canonical `DynamicDestination`.

Actual Bootstrap request:

```text
GET /api/v1/partner/config/bootstrap

X-CarBroz-Platform: ANDROID
X-CarBroz-App-Version: 1.0.0
X-CarBroz-Build-Number: 1
```

## Canonical Dynamic Destination

Bootstrap startup destinations, Dynamic response destinations, and Dynamic action destinations all produce the same four-field model:

```text
DynamicDestination
├── screenId
├── templateId
├── templateType
└── endpoint
```

HTTP method is request metadata and is not part of `DynamicDestination`. Authentication is likewise not part of the canonical destination model.

There is one navigation/back-stack mechanism. `templateType` selects rendering only; it never selects business navigation.

## Dynamic State

Implemented baseline includes:

- `DynamicDestination`
- `DynamicNavigationState`
- `DynamicContainer`
- `DynamicStore`
- `LoadDynamicScreenUseCase`
- `ExecuteDynamicActionUseCase`
- `DynamicRegistry`
- `DynamicRenderer`
- initial frozen definitions
- Dynamic repository/data source/DTO/mapper
- shared `:network` RequestHandler/Ktor infrastructure
- DYNAMIC-08 destination mapping across Bootstrap, Dynamic response and action paths

Feature use cases remain feature orchestration. Their package cleanup target is `feature/dynamic/.../usecase/`; they must not be moved into top-level `:domain`.

## Network Error State

The classification contract is frozen, but implementation remains pending:

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

## Pending Work — Ordered

1. Implement Bootstrap Option B completely.
2. Remove/rework legacy Bootstrap domain models and the transitional direct HTTP path.
3. Update Bootstrap DI and tests.
4. Verify Bootstrap → canonical `DynamicDestination` runtime hand-off.
5. Clean Dynamic use-case package ownership.
6. Complete request metadata/authentication integration.
7. Complete the remaining frozen Dynamic state/reference/action behavior.
8. Implement and test the frozen network exception taxonomy.
9. Complete DYNAMIC-07 fallback/recovery/diagnostic/observability implementation.
10. Add backend-shaped Login → Send OTP → OTP → Verify OTP integration tests.
11. Verify push/replace/pop/back using the same four-field `DynamicDestination`.
12. Verify Android, iOS/macOS-hosted and Web/Wasm where supported.
13. Perform literal plan-to-source compliance review.
14. Final Dynamic review and code freeze.

## Explicitly Not Done

- Bootstrap Option B final source reconciliation.
- Legacy Bootstrap domain model cleanup.
- Final Bootstrap DI/test reconciliation.
- Full canonical destination runtime verification.
- Network exception implementation/verification.
- Full Login → OTP → Dashboard verification.
- Full DYNAMIC-07 production implementation.
- Full platform verification.
- Final Dynamic feature freeze.

## Next Valid Action

**Start the next already-authorized implementation item: complete Bootstrap Option B, including legacy-domain cleanup and DI/test reconciliation. Do not reopen DYNAMIC-01 → DYNAMIC-08 architecture decisions.**
