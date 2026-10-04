# Dynamic UI — Final Architecture / Implementation Reconciliation

**Date:** 2026-10-04  
**State:** DISCUSSION FROZEN / IMPLEMENTATION SOURCE OF TRUTH  
**Scope:** Dynamic UI vertical slice, Bootstrap/Splash boundary, shared destination, network error classification

This record closes the current reconciliation discussion. It supersedes conflicting historical implementation wording and incorporates the frozen DYNAMIC-08 unified destination contract. It does not reopen DYNAMIC-01 → DYNAMIC-07 or BASE-ARCH-001 → BASE-ARCH-017.

## 1. Splash and Bootstrap are one flow

Splash and Bootstrap are not two product features.

```text
ApplicationContent
    ↓
Splash UI / Splash Store
    ↓
ApplicationBootstrap
    ↓
BootstrapRepository
    ↓
BootstrapRepositoryImpl
    ↓
BootstrapRemoteDataSource
    ↓
RequestHandler / HttpClient
    ↓
Bootstrap DTO
    ↓
BootstrapMapper
    ↓
BootstrapOutput
    ↓
DynamicDestination
```

Splash is the visible startup UI. Bootstrap is the startup/config API executed while Splash is shown.

There is no second independent Splash networking architecture.

## 2. Bootstrap Option B is frozen for implementation

The selected boundary is:

```text
ApplicationBootstrap       :domain
        ↓
BootstrapRepository        :domain
        ↓
BootstrapRepositoryImpl    :data
        ↓
BootstrapRemoteDataSource  :data
        ↓
RequestHandler             :network
```

Responsibilities:

- `ApplicationBootstrap`: startup-facing domain contract consumed by Splash/application composition.
- `BootstrapRepository`: domain repository abstraction.
- `BootstrapRepositoryImpl`: data orchestration.
- `BootstrapRemoteDataSource`: Bootstrap HTTP request and transport DTO acquisition.
- `BootstrapMapper`: explicit transport-to-domain mapping.

`DefaultApplicationBootstrap` is transitional and must not remain as a second production HTTP path.

## 3. Canonical Bootstrap domain result

The old `StartupConfig`, `NextScreenConfig`, and separate `BootstrapConfig` domain classes are legacy artifacts and must not remain as parallel domain contracts.

`ApplicationBootstrap` returns the canonical `BootstrapOutput`.

`BootstrapOutput` represents the Bootstrap result and contains the Bootstrap configuration/state required by startup plus the canonical `DynamicDestination` for the next screen. The destination is not wrapped in a Bootstrap-specific destination/config class.

## 4. One destination concept for the whole Dynamic system

Bootstrap startup destination, Dynamic API destination, and Dynamic action destination are all the same canonical `DynamicDestination` concept.

```text
Bootstrap startup destination ───┐
Dynamic API destination ─────────┼──→ DynamicDestination
Dynamic action destination ──────┘
                                      ↓
                            Dynamic Navigation State
                                      ↓
                              Dynamic Container
```

Canonical fields:

```text
screenId
templateId
templateType
endpoint
```

`method` and `authentication` are explicitly **not** part of destination identity. They are request metadata handled at the request boundary.

Rules:

- Bootstrap creates the initial `DynamicDestination`.
- A Dynamic response may provide another `DynamicDestination`.
- A Dynamic action may provide another `DynamicDestination`.
- All destinations enter the same navigation mechanism.
- There is one destination/back-stack representation.
- There are no `BootstrapDestination`, `NextScreenConfig`, or action-specific destination models.
- `templateType` selects rendering; it never determines business navigation.

## 5. Repository layering is symmetric where the responsibility is the same

Dynamic already follows:

```text
DynamicRepository
    ↓
DynamicRepositoryImpl
    ↓
DynamicRemoteDataSource
    ↓
RequestHandler
```

Bootstrap follows the same Data layering:

```text
BootstrapRepository
    ↓
BootstrapRepositoryImpl
    ↓
BootstrapRemoteDataSource
    ↓
RequestHandler
```

## 6. Dynamic use-case ownership

`LoadDynamicScreenUseCase` and `ExecuteDynamicActionUseCase` are feature orchestration use cases.

They must remain outside top-level `:domain` because `ExecuteDynamicActionUseCase` coordinates Dynamic feature-owned action capabilities.

The current package cleanup target is:

```text
feature/dynamic/.../usecase/
```

No new `:domain/usecase` layer is required.

## 7. Network exception taxonomy — frozen classification

The network layer must distinguish actual transport/protocol/HTTP problems rather than collapsing everything into `UnknownException`.

Canonical classification:

```text
NetworkException
├── BadRequestException          → HTTP 400
├── UnauthorizedException        → HTTP 401
├── ForbiddenException           → HTTP 403
├── NotFoundException            → HTTP 404
├── ConflictException            → HTTP 409
├── ValidationException          → HTTP 422
├── RateLimitException           → HTTP 429
├── ServerException              → HTTP 5xx
├── NetworkConnectionException   → connection cannot be established
├── TimeoutException             → connect/request/socket timeout
├── SerializationException       → response cannot be decoded
├── ProtocolException            → malformed/unexpected HTTP/protocol response
└── UnknownNetworkException      → genuinely unclassified failure
```

Classification rules:

- HTTP status classification is performed centrally in the network boundary.
- Connection failures are not HTTP errors.
- Timeout failures are distinct from connection failures.
- Serialization failures are distinct from HTTP status failures.
- Protocol failures are distinct from application/business errors.
- Unknown is the final fallback only when no more precise classification applies.
- Business-level API outcomes are not automatically converted into transport exceptions.

This taxonomy is an implementation contract for `:network` and must not duplicate equivalent existing types.

## 8. Bootstrap API contract

```text
GET /api/v1/partner/config/bootstrap
```

Headers:

```text
X-CarBroz-Platform: ANDROID
X-CarBroz-App-Version: 1.0.0
X-CarBroz-Build-Number: 1
```

The response contains `config` and `startup`, with the startup destination mapped to the canonical four-field `DynamicDestination`.

## 9. Verification completed — 2026-10-04

The affected source/test reconciliation has now passed the owner-provided JVM verification.

Compile set:

```text
:domain:compileKotlinJvm
:data:compileKotlinJvm
:feature:splash:compileKotlinJvm
:feature:dynamic:compileKotlinJvm
:navigation:compileKotlinJvm
:app:compileKotlinJvm
```

Result: **BUILD SUCCESSFUL — 43 actionable tasks: 43 executed**.

JVM verification set:

```text
:core:jvmTest
:domain:jvmTest
:data:jvmTest
:navigation:jvmTest
:feature:splash:jvmTest
:feature:dynamic:jvmTest
:app:compileKotlinJvm
```

Result: **BUILD SUCCESSFUL — 62 actionable tasks: 62 executed**.

This closes the DYNAMIC-08 source/test alignment gate. It does **not** mean the complete Dynamic vertical slice is finished.

## 10. Implementation order after this reconciliation

1. Implement Bootstrap Option B completely.
2. Reconcile/remove legacy `StartupConfig`, `NextScreenConfig`, and `BootstrapConfig` domain contracts.
3. Update Bootstrap DI and tests so exactly one production Bootstrap request path exists.
4. Verify canonical `DynamicDestination` runtime hand-off.
5. Clean Dynamic feature use-case package ownership.
6. Implement/verify the frozen network exception taxonomy.
7. Complete request metadata/authentication integration.
8. Complete remaining frozen Dynamic state/reference/action behavior.
9. Complete DYNAMIC-07 fallback/recovery/diagnostic/observability implementation.
10. Verify Login → Send OTP → OTP → Verify OTP using backend-shaped fixtures.
11. Verify push/replace/pop/back using the same four-field `DynamicDestination` model.
12. Run platform verification where supported.
13. Perform literal plan-to-source review.
14. Final Dynamic review and code freeze.

## 11. Definition of done for the current reconciliation

- [x] Splash and Bootstrap treated as one startup flow.
- [x] Bootstrap Option B selected and frozen.
- [x] One canonical four-field `DynamicDestination` selected for Bootstrap, Dynamic responses and actions.
- [x] No separate navigation concept for Bootstrap/API/action destinations.
- [x] Feature use-case ownership clarified.
- [x] Production-oriented network exception classification frozen.
- [x] DYNAMIC-08 source/test alignment completed.
- [x] Affected JVM compile/test verification passed.
- [ ] Bootstrap Option B full implementation completed.
- [ ] Full Dynamic vertical slice verification completed.

## 12. Important documentation rule

Any older active implementation document that says the Bootstrap endpoint is unknown, that `DefaultApplicationBootstrap` is the final architecture, that `StartupConfig`/`NextScreenConfig` are canonical domain contracts, or that Bootstrap/Dynamic/action destinations are separate navigation concepts is stale and must be treated as superseded by this record.
