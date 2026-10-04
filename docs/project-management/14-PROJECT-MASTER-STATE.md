# CB-Partner — Project Master State

**Status:** ACTIVE / RECOVERY SOURCE
**Last Updated:** 2026-10-04
**Scope:** Frontend repository only

## Purpose

This is the compact cross-session recovery document. It records what is frozen, what has actually been implemented, what has been verified, what remains, the current code ownership map, and which documents must be followed. It does not replace detailed decision records; it prevents a new AI session from reconstructing the project from historical chat.

## 1. Current Project State

- Repository: `carbroz26/CB-Partner`
- Active implementation branch: `feature/dynamic-ui`
- Current feature: Dynamic UI vertical slice
- Dynamic discussion: **DYNAMIC-01 through DYNAMIC-07 FROZEN**
- Dynamic implementation plan: **PLAN FROZEN / IMPLEMENTATION AUTHORIZED**
- Dynamic implementation: **IN PROGRESS**
- Latest verification supplied by owner: compile checks and required JVM tests **BUILD SUCCESSFUL** on 2026-10-04.
- BASE-ARCH-014: **CODE FROZEN**
- BASE-ARCH-017: **CODE FROZEN / VERIFIED / REVIEW ACCEPTED**
- No ViewModel.
- Pure Store + MVI/UDF.
- Backend-driven Dynamic navigation and screen rendering.

## 2. Frozen Base Architecture

The base direction remains:

```text
Kotlin Multiplatform
Compose Multiplatform
Clean Architecture
Pure Store / MVI / UDF
No ViewModel
Multi-module Gradle project
Convention plugins in build-logic
Koin DI
Ktor Client
kotlinx.serialization
Kermit logging
Navigation 3
```

Frozen base records:

- `BASE-ARCH-001` through `BASE-ARCH-017` — decision frozen.
- `BASE-ARCH-014` — implementation/code frozen.
- `BASE-ARCH-017` — implementation verified, review accepted, code frozen.

**Important:** `BASE-ARCHITECTURE.md` contains the historical/frozen base record. Current implementation topology is reconciled in `docs/architecture/ARCHITECTURE-CURRENT-STATE.md`; do not silently rewrite a frozen base decision because the implementation evolved.

## 3. Current Gradle / Module Topology

Verified from the current branch:

```text
:core
:domain
:network
:data
:navigation
:app
:feature:splash
:feature:dynamic
:androidApp
:webApp
build-logic (included build)
iosApp/ (native Xcode boundary; not a Gradle module)
```

Current `settings.gradle.kts` is the implementation source of truth for registered Gradle projects.

The older base record still contains the earlier `:desktopApp` foundation wording. This is a documentation reconciliation item, not permission to silently change architecture. Current implementation support is Android + iOS + Web/Wasm; Desktop is not part of the current Dynamic implementation target.

## 4. Startup / Bootstrap — Implemented Contract

Current startup direction:

```text
Platform entry
  ↓
App
  ↓
ApplicationContent
  ↓
SplashStore / SplashScreen
  ↓
ApplicationBootstrap
  ↓
RequestHandler
  ↓
Ktor HttpClient
  ↓
BootstrapResponseDto
  ↓
BootstrapMapper
  ↓
BootstrapOutput
  ↓
DynamicDestination
```

Current important classes:

- `domain/bootstrap/ApplicationBootstrap.kt` — domain bootstrap contract.
- `domain/model/bootstrap/BootstrapConfig.kt` — bootstrap config model.
- `domain/model/bootstrap/BootstrapOutput.kt` — bootstrap result.
- `domain/model/bootstrap/StartupConfig.kt` — startup state including canonical `DynamicDestination`.
- `domain/model/bootstrap/BootstrapFailure.kt` — bootstrap failure model.
- `data/bootstrap/DefaultApplicationBootstrap.kt` — Ktor-independent data-facing bootstrap orchestration through `RequestHandler`; owns endpoint/header details and maps transport/serialization failures.
- `data/bootstrap/BootstrapMapper.kt` — validates/maps DTO to domain and creates `DynamicDestination`.
- `feature/splash` Store/UI — startup state and splash rendering.
- `app/ApplicationContent.kt` — application-level transition from Splash to Dynamic.

Known bootstrap endpoint currently implemented:

`GET /api/v1/partner/config/bootstrap`

Known request headers currently tested:

```text
X-CarBroz-Platform: ANDROID
X-CarBroz-App-Version: 1.0.0
X-CarBroz-Build-Number: 1
```

The success response contains config plus `startup.nextScreen`, which is mapped directly to `DynamicDestination`.

## 5. Dynamic Destination — Canonical Model

```text
DynamicDestination
├── screenId
├── templateId
├── templateType
├── endpoint
├── method
└── authentication
```

`templateType` selects rendering capability. It never decides business navigation.

The complete `DynamicDestination` is the navigation/back-stack entry.

## 6. Dynamic Frozen Architecture — DYNAMIC-01 → DYNAMIC-07

### DYNAMIC-01 — Container + Screen Lifecycle

- One permanent Dynamic Container.
- Backend destination/navigation-stack model.
- Full `DynamicDestination` is the stack entry.
- Destination is loaded, then screen JSON is fetched and rendered.
- No native Login/OTP/Dashboard screen classes.

### DYNAMIC-02 — Screen Hierarchy

```text
Screen
└── exactly 1 Template
    └── 1..N Components
        ├── Elements
        └── Sections
            ├── Elements
            └── Groups
                └── Elements
```

Initial vocabulary is:

```text
stack_template
stack_component
stack_section
stack_group
text
image
```

Backend ordering is preserved.

### DYNAMIC-03 — Registry

One public `DynamicRegistry`, with category-specific registries internally. Registration is explicit. No reflection, package scanning, automatic discovery, or generic plugin framework. Duplicate registration is an error. Unknown types produce controlled unsupported results.

### DYNAMIC-04 — Layout / Rendering Direction

- Definition-owned Compose rendering.
- Parent owns container/layout and child placement.
- Child owns its content and legal self-sizing.
- Responsive behavior uses Compose constraints.
- Supported size concepts: WRAP, FILL, FIXED, FRACTION plus applicable min/max constraints.
- Theme is Screen-level.
- Common visual/layout capabilities are reused without a giant property framework.

### DYNAMIC-05 — Recursive Renderer

- `DynamicRenderer` resolves definitions through the registry.
- Definitions own their own rendering.
- Parents delegate legal children back to `DynamicRenderer`.
- Element is terminal.
- Unknown types and illegal structures are handled explicitly; no silent substitution.

### DYNAMIC-06 — Actions / Interaction

Frozen actions:

```text
request
navigate
present
dismiss
state
external_uri
sequence
```

Frozen references:

```text
$literal
$binding
$context
$response
```

Core rules:

- Event = when; Action = what.
- Dynamic state is destination-scoped and separate from business/domain state.
- Request action supports method, endpoint, authentication, validate, headers/query/body where supported, and responseMode.
- Request errors are structured; they do not automatically navigate.
- `responseMode = destination` produces a `DynamicDestination` on success.
- `responseMode = none` completes without a destination.
- Destination response schema is fixed to the six fields above.

### DYNAMIC-07 — Validation / Compatibility / Recovery / Observability

Frozen runtime boundary:

```text
PARSE → RESOLVE → RENDER → DIAGNOSE → FALLBACK → RECOVER SAFELY
```

- No duplicate backend SDUI structural validator.
- Unknown Definition and Unsupported Capability remain distinct.
- No guessing/substitution/downgrade.
- Localized fallbacks; Template failure uses screen-level fallback.
- Dynamic failures must not crash the application.
- No implicit/infinite retry.
- Existing project logger is reused.
- Structured diagnostics use safe canonical identifiers.
- Sensitive values, OTPs, tokens and credentials are never logged.
- Debug mode changes diagnostic visibility only.
- Production observability is minimal, safe and actionable.
- No separate Dynamic telemetry architecture.

## 7. Dynamic Implementation — What Exists Now

### Application / container

- `app/App.kt` — thin composition entry point.
- `app/ApplicationContent.kt` — Splash → Dynamic composition, Dynamic registry/store/navigation host wiring.
- `feature/dynamic/DynamicContainer.kt` — renders the current Dynamic destination through the Store/renderer boundary.
- `feature/dynamic/DynamicModule.kt` — Dynamic DI definitions.

### Navigation

- `navigation/DynamicNavigationState.kt` — full `DynamicDestination` back stack with root/push/replace/pop.
- `navigation/DynamicNavigationHost.kt` — Dynamic navigation host boundary.
- Navigation remains separate from Dynamic rendering and action execution.

### Dynamic Store / action

- `feature/dynamic/store/DynamicStore.kt` — Pure MVI/UDF orchestration: state, intents, destination, load/use-case calls, action/use-case calls and effects. It does not own Ktor or Navigation 3.
- `feature/dynamic/action/DynamicActionHandler.kt` — action capability boundary.
- `feature/dynamic/domain/usecase/LoadDynamicScreenUseCase.kt` — loads a Dynamic screen from the current destination.
- `feature/dynamic/domain/usecase/ExecuteDynamicActionUseCase.kt` — resolves binding/context values and coordinates frozen action execution.

### Dynamic registry / renderer / definitions

- `feature/dynamic/registry/DynamicRegistry.kt` — central registration/lookup.
- `feature/dynamic/renderer/DynamicRenderer.kt` — recursive resolution/rendering.
- `feature/dynamic/sdui/template/StackTemplate.kt` — `stack_template` implementation.
- `feature/dynamic/sdui/component/StackComponent.kt` — `stack_component` implementation.
- `feature/dynamic/sdui/section/StackSection.kt` — `stack_section` implementation.
- `feature/dynamic/sdui/group/StackGroup.kt` — `stack_group` implementation.
- `feature/dynamic/sdui/element/TextElement.kt` — `text` implementation.
- `feature/dynamic/sdui/element/ImageElement.kt` — `image` implementation.

### Domain

- `domain/model/dynamic/DynamicModels.kt` — canonical Dynamic domain models, including destination/action/reference/response models.
- `domain/repository/DynamicRepository.kt` — Dynamic repository contract.

### Data

- `data/dynamic/DynamicRepositoryImpl.kt` — repository implementation.
- `data/dynamic/remote/DynamicRemoteDataSource.kt` — Dynamic HTTP source through shared `RequestHandler`.
- `data/dynamic/model/DynamicResponseDto.kt` — transport DTO.
- `data/dynamic/mapper/DynamicResponseMapper.kt` — explicit DTO → domain mapping.

### Network

- `network/http/RequestHandler.kt` — common HTTP request boundary.
- `network/http/HttpClientBuilder.kt` — configured Ktor client construction.
- `network/http/DefaultError.kt` — default transport error model.
- `network/NetworkException.kt` / `NetworkResult.kt` / `Response.kt` / `Paged.kt` — generic network contracts.
- `network/di/NetworkModule.kt` — owns network Koin definitions and shared configured `HttpClient`/`RequestHandler`.

## 8. Login → OTP Contract Already Established

The Dynamic architecture is generic; Login and OTP are not special frontend architecture cases.

Known destination identities:

```text
Login
screenId      = partner_login
templateId    = tpl_7K2M9Q
templateType = form_template
endpoint      = /api/v1/partner/screen/auth_login
method        = GET
authentication = NONE

OTP
screenId      = partner_otp
templateId    = tpl_P6X8N3
templateType = form_template
endpoint      = /api/v1/partner/screen/auth_otp
method        = GET
authentication = NONE
```

Known OTP action contract:

```text
Resend OTP
POST /api/v1/partner/auth/send_otp
body:
  phoneNumber = $context(authFlow.phoneNumber)
  deviceId    = $context(deviceId)
responseMode = none

Verify OTP
POST /api/v1/partner/auth/verify_otp
body:
  challengeId = $response(data.challengeId)
  phoneNumber = $context(authFlow.phoneNumber)
  otp         = $binding(otp)
  deviceId    = $context(deviceId)
responseMode = destination
```

The OTP screen is backend Dynamic UI. No `OtpScreen`, `LoginStore`, or OTP-specific renderer is part of the architecture.

## 9. Verification — Latest Owner Result

On 2026-10-04 the owner pulled `feature/dynamic-ui` through commit `d85974c` and ran:

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

This resolved the earlier `BootstrapRemoteDataSourceTest.nonSuccessfulHttpStatusIsMapped` failure by configuring the test `HttpClient` with `expectSuccess = true`.

Non-blocking Windows warnings remain for disabled iOS simulator tests and Android host-test configuration. Do not treat them as failures or use them to justify unrelated changes.

## 10. What Is Frozen vs Implemented vs Verified

| Area | State |
|---|---|
| Base Architecture 001–017 | FROZEN |
| BASE-ARCH-014 implementation | CODE FROZEN |
| BASE-ARCH-017 implementation | VERIFIED / REVIEW ACCEPTED / CODE FROZEN |
| DYNAMIC-01 → DYNAMIC-07 discussion | FROZEN |
| Dynamic implementation plan | PLAN FROZEN / AUTHORIZED |
| Dynamic registry / initial definitions | IMPLEMENTED |
| Dynamic renderer | IMPLEMENTED |
| Dynamic Store / use cases | IMPLEMENTED |
| Network extraction / shared RequestHandler | IMPLEMENTED + JVM VERIFIED |
| Bootstrap → DynamicDestination mapping | IMPLEMENTED + TESTED |
| Bootstrap compile/tests | VERIFIED by latest JVM gate |
| Full Login → OTP → Dashboard real-backend flow | NOT YET VERIFIED |
| Full Dynamic DYNAMIC-07 observability implementation | NOT YET COMPLETE |
| Production-grade Dynamic error/fallback UI | NOT YET COMPLETE |
| Full platform verification (Android/iOS/Web) | PENDING |
| Final Dynamic feature review/freeze | PENDING |

## 11. Remaining Work — Ordered

1. Reconcile documentation/topology conflict between historical Base Architecture wording and current `:network/:app/:webApp` implementation. Do not silently reopen frozen decisions.
2. Complete Dynamic data/repository cleanup only where required by the frozen DYNAMIC-01→07 contract.
3. Complete request metadata/authentication integration through the frozen network/runtime boundary.
4. Complete Dynamic state/reference/action behavior required by the actual backend contract.
5. Complete localized fallback/recovery and structured observability implementation required by DYNAMIC-07.
6. Add/finish Login → Send OTP → OTP → Verify OTP integration tests using real backend-shaped fixtures.
7. Verify full backend-driven navigation/back-stack behavior.
8. Verify Android, iOS/macOS-hosted Native tests, and Web/Wasm where host-supported.
9. Perform literal discussion → plan → source compliance review.
10. Final Dynamic feature review and code freeze.
11. Only after Dynamic is frozen, move to the next feature/business capability.

## 12. Forbidden / Do Not Reintroduce

- No ViewModel.
- No hardcoded Login/OTP/Dashboard screen branching.
- No navigation decisions based on `templateType`.
- No second Dynamic state owner for bound fields.
- No giant SDUI validator/compatibility engine.
- No reflection/package scanning for Dynamic registry.
- No silent type substitution or property downgrade.
- No generic manager/coordinator/router/factory framework without a concrete responsibility.
- No fake backend/JSON to hide missing contracts.
- No new module merely because a class feels large.
- No changes to frozen architecture without reopening the relevant decision.

## 13. New-Chat Recovery Order

Read in this order:

1. `AI_START_HERE.md`
2. `docs/project-management/14-PROJECT-MASTER-STATE.md`
3. `docs/project-management/04-CURRENT-STATUS.md`
4. `docs/project-management/12-PROJECT-TRACKER.md`
5. `docs/architecture/ARCHITECTURE-CURRENT-STATE.md`
6. `docs/architecture/BASE-ARCHITECTURE.md` only for frozen base decisions
7. `docs/features/dynamic/00-FEATURE-DISCUSSION.md`
8. `docs/features/dynamic/01-IMPLEMENTATION-PLAN.md`
9. `docs/features/dynamic/02-IMPLEMENTATION-STATUS.md`
10. relevant source/tests only after the state is understood

Do not start from historical chat.

## 14. Next Valid Action

**Continue Dynamic implementation against the frozen DYNAMIC-01 → DYNAMIC-07 contract, using the current source map and test-green baseline. Do not reopen frozen discussion points.**
