# Dynamic UI Implementation Status

**Status:** IMPLEMENTED — POST-REFACTOR JVM VERIFICATION PENDING
**Date:** 2026-10-08
**Authority:** `docs/dynamic/DYNAMIC-ARCHITECTURE.md`
**Plan:** `docs/dynamic/DYNAMIC-IMPLEMENTATION-PLAN.md`

## 1. Current truth

The previously verified runtime/store/action/navigation implementation has now been extended with the frozen UI contract reconciliation. The UI refactor is implemented on `feature/dynamic-ui`; the post-refactor Gradle JVM matrix still must be run on the owner Windows environment before this status can return to VERIFIED.

## 2. Implemented runtime contract

### Runtime and references

- `DynamicValue` preserves String, Number, Boolean, Object, Array and Null.
- Numeric-looking JSON strings remain `StringValue`.
- `DynamicRuntimeState` is generic retained flow state.
- Nested dotted state paths are supported.
- `DynamicReferenceResolver` is the single path-traversal boundary.
- Supported references are only `$binding`, `$context`, `$state`, and `$literal`.
- `$response` and unknown namespaces are rejected.
- Object and array references preserve their DynamicValue types.

### Screen state and Store

- `DynamicScreenState` is entry-scoped.
- Bindings are typed.
- RuntimeState is not copied into screen state.
- `DynamicStore` uses the injected form validator.
- One-shot effects use the buffered effect mechanism.
- Store does not own navigation, HTTP, rendering, URI launching or business logic.
- No persistent previous-response state remains.

### Actions

- `DynamicActionExecutor` is the single action dispatch/sequence boundary.
- Duplicate handler registration fails deterministically.
- Sequence depth is bounded.
- Sequence state changes propagate to subsequent actions.
- Request actions use the existing repository boundary.
- Response retention is explicit and generic.
- `responseMode = none` can complete without a destination.
- Navigate, Present and Dismiss return navigation instructions.
- External URI returns an effect; the application performs the actual launch.
- No `ExecuteDynamicActionUseCase` exists.

### Navigation

- `DynamicNavigationEntry` owns only destination identity.
- `DynamicNavigationState` owns the stack.
- Push is the default operation.
- Replace and Pop are explicit.
- Equal destinations can be pushed as independent entries.
- Back pops the current entry and reveals the previous entry.
- Dynamic does not create a second navigation stack or cache.

### Rendering and registries

- Root rendering dispatches through `DynamicRegistry`.
- No hardcoded form-template business branch exists in the generic root renderer.
- Root identity validates screen ID, template ID and template type.
- The frozen hierarchy remains Template → Component → Section → Group → Element.
- Concrete element models no longer inherit from a generic `DynamicElement` abstraction.
- Concrete element mapper/renderer boundaries are separated for Text, Image, Input, Button and Icon.
- Duplicate normalized registrations fail deterministically.
- Rendering does not perform network, navigation, action execution or RuntimeState mutation.
- Content remains a separate capability.
- Template and Section horizontal/vertical arrangement contracts are explicit and no vertical property is reused for horizontal semantics.
- Node-owned padding/size/fill properties are mapped and applied at the owning node.
- Input weight is mapped and honored inside its Row scope.
- Button styling remains inside the Button renderer rather than the common layout helper.
- Responsive behavior uses normal Compose constraints such as fill/maxWidth/weight; no ConstraintLayout or custom responsive engine was introduced.

### Data and mapping

- `DynamicResponseMapper` uses the injected kotlinx.serialization Json.
- JSON values preserve semantic types.
- Object references are not stringified.
- Screen payload and transient response payload are kept separate.
- Explicit response retention maps response data into generic RuntimeState.
- No business-specific Dynamic repository/network/domain layer was introduced.

### Composition and DI

- One RuntimeState is created per active Dynamic flow.
- Navigation entries receive entry-specific Stores.
- There is no global DynamicStore map or destination-keyed cache.
- Dynamic dependencies are composed through Koin.
- Action handlers are collected through the existing DI boundary.
- No ViewModel was introduced.

## 3. Source-level audit findings resolved

The final audit corrected and verified:

1. Mapper tests use the injected Json constructor.
2. Generic contract tests use frozen `$state` references.
3. Reference-rejection tests use literal dollar-prefixed namespaces.
4. Store retention tests load the entry before dispatch.
5. RuntimeState coverage includes Object and Null.
6. Sequence-depth guard coverage exists.
7. Navigation Replace coverage exists.
8. Renderer template-type identity validation exists.
9. Generic fixture assertions were aligned with the authoritative fixture; the fixture itself was not weakened.

## 4. Forbidden-pattern audit

The Dynamic source/test sweep found no remaining implementation uses of:

- `previousResponse`;
- `previousResponseData`;
- persistent `ResponseState`;
- business Dynamic namespaces;
- `DynamicScreenCache`;
- `DynamicNavigationStack`;
- `DynamicBackStack`;
- Dynamic-specific `HttpClient`;
- String-only Dynamic runtime contracts;
- generic root hardcoding of the form template type.

The remaining `$response` occurrences are intentional tests asserting deterministic rejection of the forbidden namespace.

## 5. Backend JSON reconciliation

The attached Config/Login/Send-OTP/OTP JSON is reference input only.

The current frontend contract requires:

- explicit response retention into generic RuntimeState;
- `$state` for retained challenge/phone values;
- `$binding` for current screen input;
- `$context` only for generic environment metadata;
- `$literal` for fixed backend-provided literals.

The frontend must not add `$response` or business-specific `authFlow.*` context to accommodate stale JSON.

The generic JSON type policy also preserves phone numbers and OTP values as strings when the backend supplies them as JSON strings.

## 6. Verification

Pre-refactor runtime verification remains valid as historical evidence. The UI contract refactor itself has not yet been verified by the owner-side Gradle run.

Post-refactor required matrix:

```powershell
.\\gradlew.bat :data:jvmTest :network:jvmTest :navigation:jvmTest :feature:dynamic:jvmTest
```

Run this after pulling the latest `feature/dynamic-ui` head. If the matrix fails, fix only genuine failures within the frozen plan, then repeat the source audit.

Historical focused regression set:

```text
BUILD SUCCESSFUL
18 actionable tasks: 6 executed, 12 up-to-date
```

Broader Dynamic JVM matrix:

```powershell
.\gradlew.bat :data:jvmTest :network:jvmTest :navigation:jvmTest :feature:dynamic:jvmTest
```

Result:

```text
BUILD SUCCESSFUL
42 actionable tasks: 11 executed, 31 up-to-date
```

The Windows warning that `iosSimulatorArm64Test` is disabled because iOS simulator tests require macOS is expected and non-blocking.

## 7. Remaining platform verification

These are separate platform gates and do not reopen the frozen Dynamic architecture:

- Web/Wasm runtime verification;
- Android runtime verification;
- iOS/Xcode runtime verification on macOS.

Backend local OTP delivery/provider work is also separate from the frontend Dynamic contract.

## 8. Final state

The Dynamic UI implementation is **IMPLEMENTED** against the frozen source contract. Final verification is blocked only on the owner-side Gradle run of the post-refactor source. Do not declare final feature freeze until that matrix is green and the final diff/source audit is rechecked. No Dynamic architecture redesign is pending.

## 9. Authoritative Dynamic documents

1. `DYNAMIC-ARCHITECTURE.md`
2. `DYNAMIC-IMPLEMENTATION-PLAN.md`
3. this status document

Superseded Dynamic discussion/frozen-plan/reconciliation documents have been removed so they cannot compete with the current authority.
