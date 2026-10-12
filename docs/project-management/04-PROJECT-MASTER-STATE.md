# CB-Partner — Project Master State

**Status:** AUTHORITATIVE / CURRENT RECOVERY SOURCE
**Last Updated:** 2026-10-12
**Scope:** Frontend repository only

## 1. Repository State

- Repository: `carbroz26/CB-Partner`
- Current working branch: `feature/dynamic-ui`
- Current product slice: backend-driven Dynamic UI through Login → Send OTP → OTP → Verify
- Frontend and backend remain separate repositories.
- `iosApp/` is the native Xcode boundary, not a Gradle `:iosApp` module.
- Integration target: `feature/dynamic-ui`. Frontend content-owned spacing implementation is on PR #6, branch `feature/dynamic-content-spacing-contract`, head `c08a6729186590eb094e79176f34daa119aa8a4a`. The JVM, Android unit/compile/assemble, and Web/Wasm build/browser-test gates passed on that head. Final line-by-line source/diff audit and actual Web visual inspection remain open; no final freeze or merge is claimed.

## 2. Frozen Technical Foundation

The following remain frozen:

- Kotlin Multiplatform
- Compose Multiplatform
- Clean Architecture
- Pure Store-based MVI/UDF
- No ViewModel
- Multi-module Gradle architecture
- Gradle convention plugins
- Koin
- Ktor Client
- kotlinx.serialization
- kotlinx.coroutines
- Kermit
- Navigation 3

Base architecture decisions through BASE-ARCH-017 are not reopened by Dynamic or product work.

## 3. Documentation Authority

The durable documentation chain is:

```text
AI_START_HERE.md
      ↓
04-PROJECT-MASTER-STATE.md
      ↓
03-DECISION-LOG.md
      ↓
relevant architecture / feature / API / build document
      ↓
source + tests
```

There is one current-state document. Older Current Status, Project Tracker, Checkpoints and Master State documents have been consolidated into this record.

## 4. Architecture State

### Base architecture

**BASE-ARCH-001 → BASE-ARCH-017: FROZEN**

BASE-ARCH-014 and BASE-ARCH-017 are code-frozen and verified.

### Runtime infrastructure

BASE-ARCH-015/016/017 are frozen. BASE-ARCH-017 was implemented, verified and review-accepted on 2026-10-02.

### Startup

Splash and Bootstrap are one startup flow.

```text
Platform entry
  ↓
App / ApplicationContent
  ↓
Splash Store + UI
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
BootstrapMapper
  ↓
BootstrapOutput
  ↓
DynamicDestination
  ↓
DynamicNavigationState
```

Bootstrap Option B is the frozen target boundary.

Legacy parallel Bootstrap contracts must not be revived:
- `DefaultApplicationBootstrap` as a second HTTP path
- `StartupConfig`
- `NextScreenConfig`
- standalone `BootstrapConfig`

### Canonical destination

One `DynamicDestination` represents a destination from Bootstrap, a Dynamic API response, or a Dynamic action.

```text
screenId
templateId
templateType
endpoint
```

The template ID is opaque destination identity. `templateType` selects rendering; it never decides business navigation.

## 5. Dynamic UI State

Dynamic documentation is authoritative in:

1. `docs/dynamic/DYNAMIC-ARCHITECTURE.md`
2. `docs/dynamic/DYNAMIC-IMPLEMENTATION-PLAN.md`
3. `docs/dynamic/DYNAMIC-IMPLEMENTATION-STATUS.md`

Frozen Dynamic rules include:

- backend-driven screen structure;
- one permanent Dynamic Container;
- entry-scoped Dynamic Store;
- generic `DynamicValue` state;
- explicit RuntimeState retention;
- supported references: `$binding`, `$context`, `$state`, `$literal`;
- `$response` is forbidden;
- no business-specific state namespaces;
- explicit registries;
- renderer/registry/action/navigation separation;
- no ViewModel;
- no Dynamic-specific HTTP client;
- no second navigation stack/cache;
- external URI is a first-class action capability.

## 6. Dynamic Implementation State — 2026-10-11

The original Dynamic runtime/UI and local asset fallback changes are implemented. The latest accessory-owned padding amendment is also implemented within its frozen scope. The owner-environment JVM matrix and targeted compilation passed on 2026-10-11; final diff/obsolete-property review and Web visual verification remain open, so the branch is not yet FINAL FROZEN.

Verified implementation areas include:

- typed DynamicValue mapping;
- numeric-looking JSON strings preserved as strings;
- generic RuntimeState and nested state references;
- binding/context/state/literal reference resolution;
- entry-scoped Store lifecycle;
- action executor/handler separation;
- request retention;
- sequence depth protection;
- navigation push/replace/pop;
- registry and renderer separation;
- explicit content rendering;
- Koin composition;
- no ViewModel/global Dynamic Store/cache;
- forbidden-pattern sweep.

The latest focused and broader Dynamic JVM test matrices passed after the final UI contract corrections.

Latest verified broad command:

```powershell
.\gradlew.bat :data:jvmTest :network:jvmTest :navigation:jvmTest :feature:dynamic:jvmTest
```

Result:

```text
BUILD SUCCESSFUL
42 actionable tasks: 11 executed, 31 up-to-date
```

A focused regression set also passed:

```text
BUILD SUCCESSFUL
18 actionable tasks: 6 executed, 12 up-to-date
```

Expected Windows warning: iOS simulator tasks require macOS. This is not a Dynamic failure.

### Numeric-looking string policy

JSON semantic type is preserved generically:

- JSON string → `DynamicValue.StringValue`
- JSON number → `DynamicValue.NumberValue`
- JSON boolean → `DynamicValue.BooleanValue`

Therefore phone numbers, OTPs, IDs and other identifiers remain strings even when they contain only digits. This is not a phoneNumber-specific workaround.

## 7. Backend Login → OTP Contract

The frontend consumes the backend as a generic Dynamic protocol.

Current verified backend-shaped flow:

```text
Bootstrap
  ↓
partner_login
  ↓
Send OTP
  ↓
retain challengeId + phoneNumber into generic RuntimeState
  ↓
partner_otp
  ↓
display phoneNumber from $state
  ↓
resend using $state + $context
  ↓
verify using $state + $binding + $context
  ↓
partner_dashboard destination
```

The backend's current production-shaped JSON uses explicit retention for the values required by the next screen. The frontend must not reintroduce `$response` or an `authFlow.*` context namespace to accommodate stale examples.

The attached Config/Login/Send-OTP/OTP JSON is reference input, not a frontend architecture authority.

## 8. API / Network Boundary

- `:network` owns Ktor infrastructure and generic network handling.
- `:data` owns DTOs, mappers, repositories and remote data sources.
- Domain owns contracts/models/repository interfaces.
- Features do not perform raw HTTP.
- No business API configuration is introduced into generic runtime infrastructure.
- Network exception classification remains centralized and generic.

Frozen exception categories:

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

## 9. Platform / Run State

### Web

Primary quick runtime verification:

```powershell
.\gradlew.bat :webApp:wasmJsBrowserDevelopmentRun
```

Browser-side logs belong in browser Developer Tools; the Gradle process terminal is not a guaranteed receiver for browser `println`.

### Android

Android is a Gradle application boundary under `:androidApp`. Run through Android Studio or verified Gradle Android tasks.

### iOS

`iosApp/` is the native Xcode boundary. iOS/Xcode verification requires macOS.

### Windows limitation

iOS simulator tests cannot run on Windows. Do not classify that expected disabled task as a project failure.

## 10. Current Verification State

Completed before the UI contract refactor:

- Dynamic focused JVM tests: PASS
- Dynamic/data/network/navigation JVM matrix: PASS
- Generic numeric-looking JSON string regression: PASS
- Runtime/store/action/navigation source-level Dynamic deep audit: PASS
- Backend Login → OTP contract inspection: PASS at contract/source level

Implemented after the 2026-10-08 frozen UI reconciliation:

- Generic `DynamicElement` abstraction removed.
- Centralized `DynamicElementMapper` replaced by concrete element mappers.
- Concrete element renderer contracts and registry categories separated.
- Template/Section directional property semantics corrected.
- Node-owned element padding/sizing/fill properties implemented.
- Input weight honoring implemented.
- UI contract tests updated.

**Prior post-refactor verification:** PASS

- `:data:jvmTest`
- `:network:jvmTest`
- `:navigation:jvmTest`
- `:feature:dynamic:jvmTest`

**Latest accessory-padding verification on 2026-10-11:** PASS

- `:core:jvmTest`
- `:domain:jvmTest`
- `:data:jvmTest`
- `:network:jvmTest`
- `:navigation:jvmTest`
- `:feature:splash:jvmTest`
- `:feature:dynamic:jvmTest`
- `:domain:compileKotlinJvm`
- `:data:compileKotlinJvm`
- `:feature:dynamic:compileKotlinJvm`
- final source/diff/contract audit

Broad matrix result:

```text
BUILD SUCCESSFUL
42 actionable tasks: 18 executed, 24 up-to-date
```

Expected Windows warning: `iosSimulatorArm64Test` requires macOS and is non-blocking.

Still separate from the Dynamic JVM gate:

- final owner-side Web runtime verification;
- Android runtime verification;
- native iOS/Xcode runtime verification on macOS;
- backend local OTP delivery/provider decision and any separately authorized development-only provider implementation.

These are platform/backend work, not reasons to reopen the frozen Dynamic architecture.

## 11. Current Open Work

Ordered:

1. **Complete the final review for accessory-owned padding.** The frozen contract is implemented and the owner-environment seven-module JVM matrix plus targeted JVM compilation passed on 2026-10-11. Complete the line-by-line diff and repository-wide obsolete-property audit before any final freeze.
2. Complete owner-side Web runtime/visual verification when available. Android and iOS runtime verification remain separate platform gates.
4. Finish backend local OTP provider inspection and, only if explicitly authorized, add a development-only random OTP provider that logs the generated OTP without exposing it through the API.
5. Keep production SMS provider/security behavior unchanged.
6. Start the next product work only after the current authorized work is completed and reviewed.

## 12. Explicitly Closed / Do Not Reopen

- Base architecture through BASE-ARCH-017.
- Pure Store/MVI/UDF and no ViewModel.
- Canonical four-field DynamicDestination.
- Dynamic reference model excluding `$response`.
- Generic RuntimeState model.
- Entry-scoped Dynamic Store lifecycle.
- Explicit registry/renderer/action separation.
- Generic DynamicValue type preservation.
- No business-specific Dynamic architecture for Login/OTP.
- No duplicate Dynamic HTTP client/navigation stack/cache.
- No architecture changes merely to accommodate stale backend JSON examples.

## 13. Recovery Rule

For a new session:

1. Read `AI_START_HERE.md`.
2. Read this document.
3. Read `03-DECISION-LOG.md` for the relevant frozen decision.
4. Read only the relevant feature/architecture/API/build document.
5. Inspect source/tests only after the durable state is understood.

Historical chat is context, not authority.

## 14. Definition of Current Truth

If this document conflicts with an older Current Status, Project Tracker, Checkpoint, or Master State file, those older files are superseded and must not be used. If it conflicts with an explicitly frozen decision or the current explicit user instruction, stop and reconcile the conflict rather than silently choosing one.



## 15. Content-owned spacing contract — decision record (2026-10-11)

Historical decision snapshot: the contract was documentation-frozen before implementation authorization. Its initial 'not started' state below is superseded by section 16 and the current Dynamic implementation-status document.

- Decision: `DYNAMIC-CONTRACT-REVISION-CONTENT-SPACING-001`.
- Frontend authority: `docs/dynamic/DYNAMIC-ARCHITECTURE.md`, section 21; implementation plan section 22.26.
- Backend authority: `sdui/SDUI-COMPOSE-CONTRACT-IMPLEMENTATION-PLAN.md` and `sdui/PARTNER-AUTH-SDUI-CONTRACT.md` revision sections.
- Contract: shared `ElementContent.spacing: DynamicSpacing?` with start/top/end/bottom external layout spacing for IconContent and DividerContent. Backend content uses `properties.spacing`.
- Remove content-level padding and parent `contentSpacing` / `trailingContentSpacing` as competing mechanisms. Preserve ordinary node/container `DynamicPadding` and text `letterSpacing`.
- At the time this decision snapshot was written, source still implemented the previous accessory-padding contract and implementation had not yet been authorized. The owner subsequently authorized implementation on 2026-10-12; see section 16.


## 16. Content-owned spacing implementation and verification — 2026-10-12

- Contract: `DYNAMIC-CONTRACT-REVISION-CONTENT-SPACING-001`; architecture section 21 and implementation plan section 22.26 remain authoritative and frozen.
- Implementation: `ElementContent.spacing: DynamicSpacing?` shared by IconContent and DividerContent; mapper reads backend `properties.spacing`; existing accessory renderers apply four-sided external spacing. Ordinary node/container `DynamicPadding` remains unchanged.
- Verified on PR #6 head `c08a6729186590eb094e79176f34daa119aa8a4a`: seven-module JVM test matrix; Android unit tests, Kotlin compilation, and debug assembly; Web/Wasm development webpack and browser tests all passed.
- GitHub reported no PR-triggered workflow runs and zero commit check-runs for this head. No CI pass is claimed.
- Remaining before merge/final freeze: complete line-by-line diff and obsolete-property audit, plus actual Web visual inspection. Do not update the backend repository or its PR as part of this frontend task.
