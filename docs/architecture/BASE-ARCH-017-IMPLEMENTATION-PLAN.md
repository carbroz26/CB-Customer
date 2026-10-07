# BASE-ARCH-017 — Runtime Infrastructure Implementation Plan

**State:** CODE_FROZEN  
**Owner approval:** 2026-09-30  
**Review accepted:** 2026-10-02  
**Scope:** Frontend repository only  
**Implementation authorization:** GRANTED — 2026-09-30  
**Verification:** PASSED — owner Windows environment  
**Review:** ACCEPTED — 2026-10-02

## 1. Purpose

This document is the frozen implementation plan for BASE-ARCH-017 — Runtime Infrastructure Implementation Boundary.

It translates the already-frozen BASE-ARCH-015, BASE-ARCH-016, and BASE-ARCH-017 contracts into an exact implementation scope.

Implementation was authorized against this frozen plan. The implementation has now been verified and reviewed. This work item is code-frozen; no further implementation changes are authorized under BASE-ARCH-017.

## 2. Frozen Inputs

The implementation follows:

1. `docs/architecture/BASE-ARCHITECTURE.md`
2. `docs/architecture/BASE-ARCH-015-DECISION-RECORD.md`
3. `docs/architecture/BASE-ARCH-016-DECISION-RECORD.md`
4. `docs/architecture/BASE-ARCH-017-DECISION-RECORD.md`
5. `AI_START_HERE.md`
6. `docs/project-management/04-PROJECT-MASTER-STATE.md`
7. `docs/project-management/04-PROJECT-MASTER-STATE.md`

Historical chat is not an implementation source.

## 3. Scope

### In scope

- Runtime dependency catalog entries required by the frozen contracts.
- Core coroutine, serialization, and logging runtime dependencies.
- Core generic networking capability required by the frozen contract.
- Data Koin definitions required to construct the concrete HTTP client.
- Ktor HttpClient construction.
- Ktor multiplatform engine-default strategy.
- Navigation 3 runtime/UI dependencies and integration foundation.
- Runtime infrastructure tests.
- Reconciliation of the pre-existing `feature/base-arch-017-runtime-infrastructure` changes.
- Required documentation/status updates after implementation and verification.

### Out of scope

- Authentication, OTP, booking, payment, dashboard.
- SDUI internals or dynamic JSON architecture.
- Backend API contracts.
- Bootstrap DTOs, schemas, or real bootstrap implementation.
- Business repositories/use cases.
- Feature Stores.
- ViewModel.
- A standalone `:di` module.
- A global Store.
- A universal `NetworkClient` abstraction without a concrete need.
- Business-specific HTTP configuration.
- Authentication headers or token handling.
- Retry policy.
- Business error mapping.
- Unrelated refactoring or cleanup.

## 4. Existing 017 Branch Reconciliation

The pre-existing branch `feature/base-arch-017-runtime-infrastructure` was created before the BASE-ARCH-017 decision freeze and contained implementation commits that were not retroactively authorized.

Implementation was performed on the new branch `feature/base-arch-017-runtime-infrastructure-implementation`, created directly from the current `development` head. Reconciliation was therefore by resulting implementation state rather than by merging or replaying the pre-freeze branch history.

### Final reconciliation

| Change | Action | Final rule |
|---|---|---|
| Runtime dependency catalog | RETAIN / MODIFY | Keep only dependencies required by this plan |
| Core serialization plugin | RETAIN | Required for serialization usage |
| Core coroutines | RETAIN | Required runtime infrastructure |
| Core Kermit | RETAIN | Required logging infrastructure |
| Koin in Core | REMOVE | Core must remain DI-free |
| Koin Compiler Plugin in Data | REMOVE | No concrete 017 need exists |
| Koin Core in Data | RETAIN | Data owns its Koin definitions |
| Ktor Client Core | RETAIN | Data constructs the concrete client |
| Ktor engine defaults | RETAIN | Selected multiplatform engine strategy |
| Data NetworkModule | RETAIN | Final concrete client definition |
| NetworkModule tests | RETAIN / MODIFY | Verify the frozen client contract |
| Core RuntimeFoundationTest | RETAIN / MODIFY | Verify actual Core runtime contracts |
| Navigation 3 runtime | RETAIN | Previously frozen technology |
| Navigation 3 UI | RETAIN | Required Navigation 3 integration foundation |
| Coroutines test dependency | RETAIN | Required by tests |
| Koin test dependency | REMOVE | No remaining 017 test requirement |

No automatic merge, rebase, or cherry-pick of the pre-freeze branch was part of this implementation.

## 5. Exact Dependency Catalog

The implementation retains the following BASE-ARCH-017 dependency baseline:

- Kotlin: `2.4.20`
- AGP: `9.3.0`
- Compose Multiplatform: `1.12.0`
- Koin: `4.2.0`
- Ktor: `3.6.0`
- kotlinx.coroutines: `1.11.0`
- kotlinx.serialization: `1.11.0`
- Kermit: `2.1.0`
- Navigation 3: `1.2.0-alpha04`

Required libraries:

- `io.insert-koin:koin-core`
- `io.ktor:ktor-client-core`
- `io.ktor:ktor-client-engine-defaults`
- `io.ktor:ktor-client-mock` for tests
- `org.jetbrains.kotlinx:kotlinx-coroutines-core`
- `org.jetbrains.kotlinx:kotlinx-coroutines-test` for tests
- `org.jetbrains.kotlinx:kotlinx-serialization-json`
- `co.touchlab:kermit`
- Navigation 3 runtime/UI artifacts selected by the catalog entries.

The Koin Compiler Plugin is not applied to `:data` and no catalog entry is retained solely for this work item.

## 6. Koin Compiler Plugin

### Decision

**REMOVE from `:data` for BASE-ARCH-017.**

The Data implementation uses ordinary Koin DSL:

`module { single { HttpClient() } }`

There is no annotation-driven or generated Koin implementation requiring the compiler plugin.

### Rules

- `:core` remains Koin-free.
- `:data` uses `koin-core`.
- `:data` does not apply `alias(libs.plugins.koinCompiler)`.
- No annotations are introduced solely to justify the plugin.
- Compiler-plugin use remains available for a later implementation unit if an actual concrete need is established.

## 7. Ktor HttpClient Ownership

### Ownership

- Core owns the generic networking capability/infrastructure boundary.
- Data owns construction of the concrete application HttpClient.
- Application Composition assembles Data's network module.
- Feature and Domain code do not construct production HttpClient instances.

### Initial concrete definition

The Data module provides one application-scoped client:

```kotlin
val networkModule = module {
    single { HttpClient() }
}
```

### Configuration boundary

BASE-ARCH-017 deliberately does not add business/API configuration.

The client must not acquire:

- API base URL;
- authentication;
- authorization/token handling;
- backend-specific headers;
- API error mapping;
- retry policy;
- business interceptors;
- bootstrap endpoint behavior.

Generic client configuration may only be added if required to satisfy the frozen runtime infrastructure contract and must remain generic.

## 8. Ktor Multiplatform Engine Strategy

### Decision

Use Ktor's `ktor-client-engine-defaults` dependency and construct the client through the common `HttpClient()` API.

No project-owned engine abstraction is introduced.

### Target strategy

| Target | Strategy |
|---|---|
| Android | Ktor curated default engine |
| Desktop/JVM | Ktor curated default engine |
| iOS/Native | Ktor curated default engine |
| Shared Data code | `HttpClient()` |
| Manual expect/actual engine factory | PROHIBITED |
| Manual platform engine abstraction | PROHIBITED |

No manually selected platform engine dependency is added by BASE-ARCH-017 unless a later concrete platform requirement explicitly reopens this decision.

The native iOS application remains the Xcode `iosApp` boundary and is not converted into a Gradle module.

## 9. Core Implementation Scope

### `:core/build.gradle.kts`

Add/retain:

- Kotlin serialization plugin.
- kotlinx.coroutines core.
- kotlinx.serialization JSON.
- Kermit.

Add coroutine test support only to `commonTest`.

No Koin dependency or Koin plugin.

### Core tests

`RuntimeFoundationTest.kt` verifies:

- serialization round-trip;
- coroutine test execution.

Tests do not create business behavior or architecture abstractions merely for coverage.

## 10. Data Implementation Scope

### `:data/build.gradle.kts`

Add/retain:

- Kotlin serialization plugin.
- `:domain`.
- `:core`.
- Koin Core.
- Ktor Client Core.
- Ktor engine defaults.
- Ktor MockEngine for tests.
- kotlinx.coroutines test for tests.

Do not apply the Koin Compiler Plugin.

### `data/di/NetworkModule.kt`

Own the concrete HttpClient definition.

The application composition root assembles this module; Data does not start Koin itself.

### Data tests

`NetworkModuleTest.kt` verifies:

1. The network module resolves an HttpClient through Koin.
2. A MockEngine can execute a request without real network access.
3. Test-created clients are closed.
4. No real backend/API endpoint is contacted.

## 11. Navigation 3 Implementation Scope

### `:navigation/build.gradle.kts`

Add/retain the frozen Navigation 3 runtime/UI dependencies.

BASE-ARCH-017 does not re-decide Navigation 3.

Implementation remains limited to the runtime integration foundation.

Do not introduce:

- business destinations;
- feature-specific routes beyond what is required for compilation/integration;
- authentication navigation;
- booking/payment navigation;
- SDUI navigation;
- a second navigation framework.

## 12. Application Composition Scope

The runtime infrastructure is assembled through the already-frozen application composition boundary.

Inspection of the current `development` composition showed that the 017 scope could be completed without adding an application-composition source file: the concrete Data network module is defined but not started by Data itself, preserving the composition boundary without introducing speculative root wiring at this stage.

No new `:di` module is created.

No feature is allowed to own global runtime infrastructure.

## 13. Exact Files Changed

The implementation changes exactly these seven source/build files:

```text
gradle/libs.versions.toml
core/build.gradle.kts
core/src/commonTest/kotlin/com/carbroz/cbpartner/core/RuntimeFoundationTest.kt
data/build.gradle.kts
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/di/NetworkModule.kt
data/src/commonTest/kotlin/com/carbroz/cbpartner/data/di/NetworkModuleTest.kt
navigation/build.gradle.kts
```

No additional source file was required for the 017 implementation scope.

## 14. Verification Record — 2026-10-02

Owner-environment verification was executed on Windows from `development` after the required build-system fixes were reconciled.

The first verification exposed two dependency/test-configuration issues:

- `:core` common tests lacked `kotlin.test` support.
- `:data` tests required the Ktor 3.6 MockEngine handler API and Kotlin test dependency.

These were resolved using the evidence from the existing Splash branch's relevant build/test fixes. The Splash feature implementation itself was not merged wholesale and BASE-ARCH-017 was not restarted.

The `development` branch was then rebased onto the updated remote `development` and the reconciled build fixes were pushed.

Final verification command:

```powershell
.\gradlew.bat :core:jvmTest :domain:jvmTest :data:jvmTest :navigation:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest --no-daemon
```

Final result:

```text
BUILD SUCCESSFUL
33 actionable tasks: 14 executed, 19 up-to-date
```

Known non-blocking Windows warning:

```text
iosSimulatorArm64Test ... cannot run on the current host (windows-x86_64)
Reason: simulator tests require macOS
```

This is an established host limitation and does not invalidate the JVM verification.

A non-blocking `ExperimentalCoroutinesApi` opt-in warning was also reported by `RuntimeFoundationTest.kt`. It does not fail the build and does not require a BASE-ARCH-017 implementation change.

## 15. Acceptance Criteria

BASE-ARCH-017 implementation is accepted because all required criteria were satisfied:

1. Resulting code conforms to BASE-ARCH-015/016/017.
2. Core contains no Koin dependency or Koin compiler plugin.
3. Data contains Koin Core but does not apply the Koin Compiler Plugin.
4. Data owns the concrete HttpClient construction.
5. Ktor engine defaults are used for the multiplatform engine strategy.
6. No project-owned expect/actual networking abstraction is introduced.
7. No business/API configuration is introduced.
8. Navigation 3 dependencies are present in `:navigation` without introducing another navigation framework.
9. No standalone `:di` module is created.
10. No ViewModel or global Store is introduced.
11. Existing 017 branch changes were reconciled rather than blindly merged.
12. Required tests pass.
13. Targeted JVM verification passes.
14. Existing foundation tests remain passing.
15. No unrelated architecture scope was introduced.
16. Documentation/status records are synchronized after verification and review.
17. Review found no frozen-boundary violation.

## 16. Prohibited Changes

The implementation must stop and report rather than silently introduce:

- ViewModel.
- Global Store.
- `:di` Gradle module.
- Koin inside Core.
- blanket Koin Compiler Plugin application.
- universal NetworkClient abstraction without concrete need.
- manual expect/actual HTTP engine abstraction.
- manually selected platform engines without approved requirement.
- authentication/API configuration.
- business repositories/use cases.
- SDUI/dynamic JSON implementation.
- bootstrap implementation.
- unrelated refactoring.
- speculative platform abstractions.
- technology replacement.

## 17. Final Implementation Gate

```text
BASE-ARCH-017 DECISION_FROZEN
        ↓
PLAN_FROZEN
        ↓
IMPLEMENTATION AUTHORIZATION  ← GRANTED 2026-09-30
        ↓
RECONCILED IMPLEMENTATION  ← COMPLETED
        ↓
TEST / VERIFICATION  ← PASSED 2026-10-02
        ↓
REVIEW  ← ACCEPTED 2026-10-02
        ↓
CODE_FREEZE  ← CURRENT
```

**Implementation branch:** `feature/base-arch-017-runtime-infrastructure-implementation`

**Integrated state:** The reconciled BASE-ARCH-017 implementation and required build/test fixes are present in `development`.

**Current state:** CODE_FROZEN. No further BASE-ARCH-017 implementation changes are authorized.

## 18. Freeze Record — 2026-10-02

The project owner accepted the BASE-ARCH-017 review after successful owner-environment verification.

The implementation is now **CODE_FROZEN**.

Freeze means:

- no further BASE-ARCH-017 source changes;
- no restart or redesign of the implementation;
- no wholesale merge of the Splash feature branch;
- no cleanup iteration merely for the non-blocking warnings recorded above;
- future changes require a new separately authorized work item.

The code-freeze record does not reopen BASE-ARCH-015, BASE-ARCH-016, or BASE-ARCH-017 decisions.

## 19. Implementation Record — 2026-09-30 / Finalized 2026-10-02

Implementation completed on `feature/base-arch-017-runtime-infrastructure-implementation`, created directly from the current `development` head.

Resulting implementation state:

- Runtime catalog reconciled to the frozen dependency scope.
- Core runtime dependencies wired.
- Data Koin network module added.
- Data uses Koin Core without the Koin Compiler Plugin.
- Data uses Ktor Client Core plus engine defaults.
- Navigation 3 runtime/UI dependencies added.
- Core runtime foundation tests added.
- Data network/Ktor tests added.
- Pre-existing 017 branch was not merged, rebased, or cherry-picked.
- No application-composition source file was added.
- No business, bootstrap, SDUI, ViewModel, Store, or new module scope was introduced.
- Verification passed on the owner's Windows environment.
- Review accepted by the project owner on 2026-10-02.
- BASE-ARCH-017 is code-frozen.
