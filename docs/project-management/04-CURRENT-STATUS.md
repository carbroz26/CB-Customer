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
- Dynamic implementation plan: **PLAN_FROZEN / IMPLEMENTATION AUTHORIZED**
- Pure Store + MVI/UDF: frozen direction
- No ViewModel: frozen
- Backend-driven Dynamic navigation/rendering: frozen
- Bootstrap Option B: **FROZEN AS IMPLEMENTATION BOUNDARY**
- One canonical `DynamicDestination`: **FROZEN**
- Network exception classification: **FROZEN FOR IMPLEMENTATION**

## Current Work Item

**DYNAMIC-UI-001 — Dynamic UI vertical slice**

Branch:

```text
feature/dynamic-ui
```

## Latest Verification — 2026-10-04

Owner-provided JVM verification:

```powershell
.\gradlew.bat :network:compileKotlinJvm
.\gradlew.bat :data:compileKotlinJvm
.\gradlew.bat :feature:splash:compileKotlinJvm
.\gradlew.bat :feature:dynamic:compileKotlinJvm
.\gradlew.bat :app:compileKotlinJvm

.\gradlew.bat :network:jvmTest :data:jvmTest :domain:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest
```

Result: **BUILD SUCCESSFUL — 54 actionable tasks: 15 executed, 39 up-to-date**.

This is a baseline only; it does not certify all architecture/runtime gaps.

## Startup / Bootstrap State

Splash and Bootstrap are one flow:

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

The current source still requires removal/rework of the transitional direct `DefaultApplicationBootstrap → RequestHandler` path.

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

Bootstrap `nextScreen`, Dynamic API destinations and Dynamic action destinations all produce the same:

```text
DynamicDestination
├── screenId
├── templateId
├── templateType
├── endpoint
├── method
└── authentication
```

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

Feature use cases remain feature orchestration. Their package cleanup target is `feature/dynamic/.../usecase/`; they must not be moved into top-level `:domain`.

## Network Error State

The implementation contract is:

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

Implementation must classify the actual cause and preserve the distinction between transport/protocol errors and business/API outcomes.

## Pending Work — Ordered

1. Implement Bootstrap Option B completely.
2. Remove/rework legacy Bootstrap domain models and direct HTTP path.
3. Update Bootstrap DI and tests.
4. Verify Bootstrap → canonical `DynamicDestination` runtime hand-off.
5. Clean Dynamic use-case package ownership.
6. Complete request metadata/authentication.
7. Complete frozen Dynamic state/reference/action behavior.
8. Implement the frozen network exception taxonomy.
9. Complete DYNAMIC-07 fallback/recovery/diagnostic/observability implementation.
10. Add Login → Send OTP → OTP → Verify OTP integration tests.
11. Verify push/replace/pop/back using the same `DynamicDestination`.
12. Verify Android, iOS/macOS-hosted and Web/Wasm where supported.
13. Perform literal plan-to-source compliance review.
14. Final Dynamic review and code freeze.

## Explicitly Not Done

- Bootstrap Option B source implementation.
- Legacy Bootstrap domain model cleanup.
- Final Bootstrap DI/test reconciliation.
- Full canonical destination runtime verification.
- Network exception implementation/verification.
- Full Login → OTP → Dashboard verification.
- Full DYNAMIC-07 production implementation.
- Full platform verification.
- Final Dynamic feature freeze.

## Recovery Documents

1. `AI_START_HERE.md`
2. `docs/project-management/14-PROJECT-MASTER-STATE.md`
3. `docs/project-management/12-PROJECT-TRACKER.md`
4. `docs/architecture/ARCHITECTURE-CURRENT-STATE.md`
5. `docs/features/dynamic/00-FEATURE-DISCUSSION.md`
6. `docs/features/dynamic/03-FINAL-RECONCILIATION-2026-10-04.md`
7. `docs/features/dynamic/01-IMPLEMENTATION-PLAN.md`
8. `docs/features/dynamic/02-IMPLEMENTATION-STATUS.md`

## Next Valid Action

**Implement Bootstrap Option B completely, including legacy domain-model cleanup, DI and tests. Then verify the shared `DynamicDestination` runtime hand-off before continuing the remaining Dynamic gaps.**
