# Dynamic UI Implementation Status

Status: IMPLEMENTATION RECONCILED — final local Gradle verification pending
Date: 2026-10-07
Authority: `docs/dynamic/DYNAMIC-ARCHITECTURE.md`
Plan: `docs/dynamic/DYNAMIC-IMPLEMENTATION-PLAN.md`

## Current truth

The Dynamic implementation has been reconciled source-by-source against the frozen architecture and implementation plan on `feature/dynamic-ui`.

The implementation is **not yet finally verified** because the required Gradle test/build commands have not been executed in this environment. Therefore this document does not mark the Dynamic implementation as finally complete.

## Implemented and verified at source level

### Runtime and references

- `DynamicValue` preserves String, Number, Boolean, Object, Array and Null.
- `DynamicRuntimeState` is the generic retained flow-state capability.
- Nested dotted state paths are supported with atomic change application.
- `DynamicReferenceResolver` is the single path-traversal boundary.
- Supported namespaces are only `$binding`, `$context`, `$state`, and `$literal`.
- `$response` and unknown namespaces are deterministically rejected.
- Object and array references preserve their DynamicValue types.

### Screen state and Store

- `DynamicScreenState` is entry-scoped canonical screen state.
- Bindings are typed and stored in the canonical screen state.
- RuntimeState is not copied into screen state.
- `DynamicStore` uses injected `DynamicFormValidator`.
- Store effects use a buffered one-shot Channel/Flow.
- Store does not own navigation, HTTP, rendering, URI launching, or business logic.
- No persistent previous-response state remains.
- Action results are reduced into RuntimeState and one-shot effects.

### Actions

- `DynamicActionExecutor` is the single action dispatch/sequence boundary.
- Duplicate handler registration fails deterministically.
- Sequence depth is bounded.
- Sequence state changes propagate to subsequent actions.
- Terminal navigation/external-URI results preserve state changes.
- Request actions use the existing `DynamicRepository` boundary.
- Request response retention is explicit and generic.
- `responseMode = none` can complete without a destination.
- Navigate, Present and Dismiss return navigation instructions rather than mutating the stack.
- External URI returns an effect; the application/container performs the actual launch.
- No `ExecuteDynamicActionUseCase` was introduced.

### Navigation

- `DynamicNavigationEntry` is identity-only and owns only `DynamicDestination`.
- `DynamicNavigationState` owns the stack.
- Push is the default destination operation.
- Replace and Pop are explicit.
- Two pushes of the same destination create independent entries.
- Normal back pops the current entry and reveals the previous entry.
- Dynamic does not create a second navigation stack or cache.

### Rendering and registries

- Root rendering dispatches through `DynamicRegistry`.
- No hardcoded `form_template` branch exists in `DynamicRenderer`.
- Root identity validation checks screen ID, template ID and template type.
- Registry responsibilities remain separated by template/component/section/group/element/content.
- Duplicate normalized registrations fail deterministically.
- Rendering does not perform network, navigation, action execution or RuntimeState mutation.
- Element content remains a separate capability; Divider is content, not a first-class element.

### Data and mapping

- `DynamicResponseMapper` uses injected kotlinx.serialization `Json`.
- Dynamic JSON values preserve their original types.
- Object references are not stringified.
- Screen payloads and transient response payloads are classified separately.
- Explicit response retention maps response data into generic RuntimeState.
- No business-specific Dynamic repository/network/domain layer was introduced.

### Composition and DI

- Application composition creates one RuntimeState per active Dynamic flow.
- Each Navigation 3 entry receives an entry-specific Store.
- There is no global DynamicStore map or destination-keyed screen cache.
- Dynamic dependencies are provided through Koin.
- Dynamic action handlers are collected through `getAll<DynamicActionHandler>()`.
- No ViewModel was introduced.

## Source-level audit findings fixed in this pass

1. Updated mapper tests to use the injected `Json` constructor.
2. Updated the generic contract test to assert the frozen `$state` reference rather than `$response`.
3. Corrected reference-rejection tests so literal dollar-prefixed namespaces are tested rather than Kotlin string interpolation.
4. Updated the Store retention test to load its entry before dispatching the action.
5. Added RuntimeState coverage for Object and Null values.
6. Added sequence-depth guard coverage.
7. Added navigation Replace coverage.
8. Added DynamicRenderer template-type identity validation.

## Forbidden-pattern audit

The targeted Dynamic source/test sweep found no remaining implementation uses of:

- `previousResponse`
- `previousResponseData`
- persistent `ResponseState`
- business Dynamic namespaces such as `auth.*`, `login.*`, `otp.*`, or `booking.*`
- `DynamicScreenCache`
- `DynamicNavigationStack`
- `DynamicBackStack`
- Dynamic-specific `HttpClient`
- `Map<String, String>` for typed Dynamic runtime contracts
- `LogLevel.ALL`
- hardcoded root `FORM_TEMPLATE_TYPE`

The only remaining `$response` occurrences are intentional tests asserting deterministic rejection of the forbidden namespace.

## Attached JSON response reconciliation

The attached Config/Login/Send-OTP/OTP examples are reference input only.

### Compatible with the frozen contract

- Bootstrap destination contains `screenId`, `templateId`, `templateType`, and `endpoint`.
- Screen responses contain the Template → Component → Section → Group → Element hierarchy.
- Bindings are represented by generic binding keys.
- Actions use generic action types such as request, navigate and external URI.
- `responseMode` can map to the generic transient-action-result model.
- Request response data can be explicitly retained into generic RuntimeState.

### Requires backend JSON migration

The attached examples still contain:

- `$response`
- `authFlow.phoneNumber`
- `legal.termsUri`
- `legal.privacyUri`

These must not be added to the frontend architecture.

The OTP `challengeId` currently uses `$response` in the example. Under the frozen model, the server must explicitly retain the required response value into a generic RuntimeState key and the next screen must read it through `$state`.

The phone number currently uses the business-specific `authFlow.phoneNumber` context path. Under the frozen model it must become generic retained flow state, populated explicitly from the current binding/request flow.

The legal URI context paths are also not part of the currently frozen runtime metadata contract. They must be migrated to a generic supported source such as explicitly retained state or an explicit literal/action payload according to the backend contract; they must not expand the Dynamic Context contract with business-specific namespaces.

The frontend architecture is therefore **not changed to accommodate these legacy JSON fields**.

## Network boundary

The existing network boundary remains outside this Dynamic reconciliation. The current localhost base URL is an environment-configuration concern already identified by the architecture/status documents and is intentionally not changed as part of this Dynamic implementation.

The Dynamic feature continues to use the existing `RequestHandler`/repository/network boundary and does not create a Dynamic-specific HTTP client or authentication mechanism.

## Required final verification

Run from the project root:

```powershell
.\gradlew.bat :navigation:jvmTest :feature:dynamic:jvmTest
```

Then run the applicable project-wide verification commands from the project operating-system documentation.

Windows not running iOS simulator tests is expected and is not a Dynamic architecture failure.

Only after those commands pass should the status be changed to final implementation complete/frozen.
