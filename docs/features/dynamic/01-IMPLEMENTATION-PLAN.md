# CB-Partner Dynamic UI — Implementation Plan

## Status

**PLAN FROZEN — IMPLEMENTATION AUTHORIZED**

This document is the complete implementation contract for the Dynamic UI refactor on `feature/dynamic-ui`.

The implementation rule is strict:

> **Implementation MUST match this plan 100%. No item may be silently omitted, added, renamed, moved, or redesigned. If implementation reveals a material conflict with this plan or a frozen Dynamic decision, STOP → REPORT → DISCUSS → DECIDE → UPDATE THIS PLAN → CONTINUE.**

A better idea discovered during implementation is not permission to change the architecture.

## 1. Authority and Inputs

Authoritative inputs, in order:

1. Current user instruction.
2. `AI_START_HERE.md`.
3. Frozen Dynamic feature discussion: `docs/features/dynamic/00-FEATURE-DISCUSSION.md`.
4. Frozen SDUI contract and current source implementation.
5. This implementation plan.
6. Current source code only as the forensic baseline.

The fixed backend JSON contract is not redesigned by this implementation.

The implementation reuses the canonical SDUI concepts already established by the project. Dynamic must not create a second SDUI language, validator, or backend contract.

## 2. Current Forensic Baseline

Current branch:

```text
feature/dynamic-ui
```

Current observed branch head when this plan was created:

```text
e3c97b8ba5903396ccf187acb3fce09860179236
```

Current Dynamic module contains these primary areas:

```text
feature/dynamic/
└── src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/
    ├── DynamicScreen.kt
    ├── action/
    │   └── DynamicActionHandler.kt
    ├── registry/
    │   └── DynamicRegistry.kt
    ├── renderer/
    │   └── DynamicRenderer.kt
    ├── sdui/
    │   ├── component/
    │   ├── element/
    │   ├── group/
    │   ├── section/
    │   └── template/
    └── store/
        └── DynamicStore.kt
```

Current code has responsibilities that must be separated. In particular, `DynamicStore` currently performs loading, request construction, binding resolution, device-id injection, request execution, destination construction, action routing, state mutation and effect emission. This is the primary refactor target.

Current `DynamicRegistry` registers `form_template`, `input`, and `button` in addition to the currently agreed initial vocabulary. The initial Dynamic implementation scope is intentionally only:

```text
Template  -> stack_template
Component -> stack_component
Section   -> stack_section
Group     -> stack_group
Elements  -> text, image
```

Therefore `form_template`, `input`, and `button` are not part of this implementation scope and must not remain registered as current Dynamic definitions.

`DefaultApplicationBootstrap` is part of the Bootstrap/data boundary, not the Dynamic rendering system. It is not to become a Dynamic orchestration class. Its Dynamic-facing responsibility is limited to producing bootstrap data whose `startup.nextScreen` can become the initial Dynamic destination.

## 3. Frozen Product Model

Dynamic is the runtime UI system after Bootstrap.

There are no frontend business-screen classes such as LoginScreen, OtpScreen, DashboardScreen, etc.

The generic lifecycle is:

```text
Bootstrap
   ↓
initial DynamicDestination
   ↓
Dynamic Navigation Stack
   ↓
Dynamic Container
   ↓
load destination endpoint
   ↓
Dynamic Screen JSON
   ↓
canonical Dynamic renderer
   ↓
Compose UI
```

A current Dynamic screen may produce a response containing the descriptor for the next Dynamic destination.

The important distinction is:

```text
templateType
    = which registered template renderer/definition is used

templateId
    = concrete Dynamic destination/template instance identity

endpoint/method/authentication
    = how the actual screen document is loaded
```

The frontend does not infer the next business screen from `templateType`.

## 4. Target Architecture

```text
                    ┌──────────────────────────┐
                    │ Bootstrap / startup       │
                    └────────────┬─────────────┘
                                 │
                                 ▼
                    ┌──────────────────────────┐
                    │ DynamicDestination       │
                    └────────────┬─────────────┘
                                 │
                                 ▼
                    ┌──────────────────────────┐
                    │ Dynamic Navigation       │
                    │ Stack / destination ID   │
                    └────────────┬─────────────┘
                                 │
                                 ▼
                    ┌──────────────────────────┐
                    │ Dynamic Container/Screen │
                    │ Pure MVI Store           │
                    └────────────┬─────────────┘
                                 │
                           intent/use case
                                 │
                 ┌───────────────┴───────────────┐
                 │                               │
                 ▼                               ▼
       LoadDynamicScreenUseCase       ExecuteDynamicActionUseCase
                 │                               │
                 └───────────────┬───────────────┘
                                 ▼
                    ┌──────────────────────────┐
                    │ DynamicRepository        │
                    └────────────┬─────────────┘
                                 │
                                 ▼
                    ┌──────────────────────────┐
                    │ RemoteDataSource / HTTP  │
                    └────────────┬─────────────┘
                                 │
                                 ▼
                              Backend
                                 │
                    ┌────────────┴─────────────┐
                    │                          │
                    ▼                          ▼
              screen document             nextScreen
                    │                          │
                    ▼                          ▼
          canonical SDUI render       DynamicDestination
```

Cross-cutting request concerns remain below Dynamic:

```text
Common Request Infrastructure
├── platform
├── app version
├── build number
├── device ID
└── authentication/session metadata
```

Dynamic business/input data is separate:

```text
Dynamic request data
├── $binding  → current Dynamic input/form state
└── $context  → controlled application/flow context
```

No response-history/challenge-ID subsystem is introduced by this plan.

## 5. Exact Target Responsibilities

### 5.1 DynamicScreen

`DynamicScreen.kt` remains the Compose entry/container for the Dynamic feature.

It will:

- observe Dynamic Store state;
- dispatch the initial load intent for the supplied destination;
- render loading/success/failure states;
- delegate actual SDUI rendering to `DynamicRenderer`;
- forward user actions and binding changes to the Store.

It will not:

- call repositories directly;
- construct HTTP requests;
- resolve bindings itself;
- construct destinations from raw JSON;
- execute external URIs;
- contain business-screen logic.

### 5.2 DynamicStore

`DynamicStore.kt` remains the Pure MVI/UDF Store.

It will own only:

- current Dynamic state;
- current Dynamic interaction/binding state required by the screen;
- intent handling;
- invocation of meaningful use cases;
- state/effect emission.

It will not own:

- Ktor/HTTP calls;
- JSON request serialization;
- endpoint parsing;
- device ID hardcoding;
- request-key alias mapping;
- navigation mechanics;
- external URI launching;
- template registry lookup;
- recursive rendering;
- a generic response-history store.

The hardcoded `bruno-test-device-123` value must be removed from the Store.

### 5.3 LoadDynamicScreenUseCase

Create one meaningful use case for loading a Dynamic screen document.

Responsibility:

```text
DynamicDestination
      ↓
DynamicRepository
      ↓
Dynamic screen document
```

It does not render, navigate, or mutate Store state.

### 5.4 ExecuteDynamicActionUseCase

Create one meaningful use case for executing a Dynamic action.

It coordinates the action execution boundary and returns an application-level result/effect to the Store.

It does not become a workflow engine.

Do not create one use-case class per action type.

### 5.5 DynamicRepository

The existing domain repository contract remains the application data boundary.

It will expose meaningful operations for:

- loading a screen from a `DynamicDestination`;
- executing a Dynamic request action using resolved request data.

The repository does not own Compose rendering or navigation UI.

### 5.6 DynamicDestination

`DynamicDestination` remains the domain model for a concrete Dynamic destination:

```text
screenId
 templateId
 templateType
 endpoint
 method
 authentication
```

`templateType` selects the registered rendering definition.

`templateId` identifies the concrete destination/back-stack instance.

The destination descriptor is created from the current response/bootstrap response, not inferred from business-screen names.

### 5.7 DynamicAction

Keep the backend action model generic.

Frozen action vocabulary:

```text
request
navigate
present
dismiss
state
external_uri
sequence
```

Current implementation only implements the action behavior required by the current fixed contract. The architecture must keep the action boundary extensible without speculative implementations.

Request-dependent navigation remains a request result/destination flow, not a Store-level `request + navigate` duplication.

### 5.8 DynamicReference

Keep the generic reference vocabulary:

```text
$binding
$context
$response
$literal
```

For this implementation:

- `$binding` resolves current Dynamic input/form state;
- `$context` resolves controlled application/flow context;
- `$literal` remains a direct literal value;
- `$response` is not expanded into a response-history subsystem unless required by the already frozen contract implementation.

Do not create a `ResponseStore`, `PreviousResponseStore`, or challenge-specific context.

## 6. Exact Project Structure After Refactor

The target Dynamic feature structure is:

```text
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/
├── DynamicScreen.kt
├── action/
│   └── DynamicActionHandler.kt
├── domain/
│   ├── model/
│   │   ├── DynamicDestination.kt
│   │   ├── DynamicAction.kt
│   │   └── DynamicReference.kt
│   └── usecase/
│       ├── LoadDynamicScreenUseCase.kt
│       └── ExecuteDynamicActionUseCase.kt
├── registry/
│   └── DynamicRegistry.kt
├── renderer/
│   └── DynamicRenderer.kt
├── sdui/
│   ├── component/
│   │   └── StackComponent.kt
│   ├── element/
│   │   ├── TextElement.kt
│   │   └── ImageElement.kt
│   ├── group/
│   │   └── StackGroup.kt
│   ├── section/
│   │   └── StackSection.kt
│   └── template/
│       └── StackTemplate.kt
└── store/
    └── DynamicStore.kt
```

The exact package placement of domain models will follow the existing project domain ownership. If the current domain module already owns these models, they remain there rather than being duplicated under the feature module. The implementation must choose one canonical owner and remove duplicates; it must not create two copies.

The plan does not require a new Gradle module.

The plan does not introduce a new SDUI module.

## 7. Existing File Disposition

### KEEP AND REFACTOR

```text
feature/dynamic/.../DynamicScreen.kt
feature/dynamic/.../store/DynamicStore.kt
feature/dynamic/.../registry/DynamicRegistry.kt
feature/dynamic/.../renderer/DynamicRenderer.kt
feature/dynamic/.../action/DynamicActionHandler.kt
feature/dynamic/.../sdui/template/StackTemplate.kt
feature/dynamic/.../sdui/component/StackComponent.kt
feature/dynamic/.../sdui/section/StackSection.kt
feature/dynamic/.../sdui/group/StackGroup.kt
feature/dynamic/.../sdui/element/TextElement.kt
feature/dynamic/.../sdui/element/ImageElement.kt
```

### DELETE FROM CURRENT DYNAMIC IMPLEMENTATION

These are outside the agreed initial vocabulary:

```text
feature/dynamic/.../sdui/template/FormTemplate.kt
feature/dynamic/.../sdui/element/InputElement.kt
feature/dynamic/.../sdui/element/ButtonElement.kt
```

Their registry entries and renderer branches must also be removed.

They are not replaced with alternative form/button/input abstractions in this implementation.

### ADD

Only these new application-level classes are planned unless an already-existing canonical class can satisfy the same responsibility:

```text
LoadDynamicScreenUseCase.kt
ExecuteDynamicActionUseCase.kt
```

`DynamicDestination`, `DynamicAction`, and related Dynamic models already exist in the domain model and must be reused/refactored rather than duplicated.

### BOOTSTRAP

```text
data/.../bootstrap/DefaultApplicationBootstrap.kt
```

is retained as the Bootstrap data implementation. It is not moved into `feature:dynamic`.

Its Dynamic-facing output is the bootstrap `nextScreen` destination descriptor.

Common request metadata currently hardcoded in Bootstrap must not be duplicated inside Dynamic. If the existing Runtime Infrastructure already provides the appropriate common request metadata boundary, Bootstrap and Dynamic must consume that boundary rather than defining separate header logic.

## 8. Registry Refactor

`DynamicRegistry` becomes a small explicit registry for only the currently agreed types:

```text
Templates
└── stack_template

Components
└── stack_component

Sections
└── stack_section

Groups
└── stack_group

Elements
├── text
└── image
```

Required behavior:

- explicit registration;
- category-specific lookup;
- duplicate registration rejected rather than silently replaced;
- unknown type produces controlled unsupported handling;
- no reflection;
- no package scanning;
- no plugin framework;
- no generic factory hierarchy;
- registry owns lookup only.

The registry must not own Store state, navigation, HTTP, or action execution.

## 9. Renderer Refactor

`DynamicRenderer` remains the single recursive rendering coordinator.

It will:

1. receive the validated Dynamic screen/document;
2. select the destination `templateType` consistently with the loaded document identity;
3. resolve the template through `DynamicRegistry`;
4. delegate rendering to the registered definition;
5. recursively render children in backend order;
6. report unsupported definitions through the existing controlled UI behavior.

It will not contain a giant `when` for every future definition.

The renderer must not directly own navigation, request execution, binding storage, or API calls.

The current direct `is FormTemplate`, `is InputElement`, and `is ButtonElement` branches are removed with those definitions.

## 10. Action Refactor

`DynamicActionHandler` is retained only if its final responsibility remains small and useful.

It must not duplicate the Store's request execution logic.

The action boundary becomes:

```text
DynamicStore
    ↓
ExecuteDynamicActionUseCase
    ↓
Dynamic action execution
    ├── request
    ├── navigate
    ├── external_uri
    └── controlled unsupported handling for future actions
```

Navigation and external URI are separate capabilities.

Conceptually:

```text
navigate
    → Dynamic navigation effect/destination

external_uri
    → external URI effect

request
    → repository request
    → response
    → nextScreen when responseMode is destination
```

Do not combine external URI with navigation.

Do not add separate classes merely to represent every future action before the contract requires them.

## 11. DynamicDestination Flow

### Initial destination

Bootstrap returns `startup.nextScreen`.

That maps directly to `DynamicDestination`.

### Current screen action

A current Dynamic screen may execute a request whose response contains `nextScreen`.

The response is mapped to `DynamicDestination`.

### Navigation

Navigation receives the destination descriptor.

The destination descriptor carries:

```text
screenId
templateId
templateType
endpoint
method
authentication
```

### Loading

The endpoint is called to obtain the actual Dynamic screen document.

### Identity verification

The loaded screen/template identity must remain consistent with the destination descriptor. A mismatched loaded identity is an error, not an instruction to silently replace the destination identity.

## 12. Binding and Context Plan

Binding is generic and is not designed around OTP or phone numbers.

The runtime model is:

```text
Backend input definition
        ↓
backend binding key
        ↓
Dynamic binding state
        ↓
request body resolution
```

It must support arbitrary future input data such as:

```text
firstName
lastName
email
phone
image
address
etc.
```

No field names are hardcoded into Dynamic architecture.

### `$context`

Controlled application/flow values are resolved separately from user input.

Examples can include:

```text
deviceId
platform
appVersion
buildNumber
flow/application values
```

Only values intentionally exposed to Dynamic are resolvable.

### Common transport metadata

Common headers and transport metadata are infrastructure concerns. Dynamic must not manually add the same common headers to every action body/request definition.

For example:

```text
X-CarBroz-Platform
X-CarBroz-App-Version
X-CarBroz-Build-Number
common device/session metadata
```

belong to request infrastructure when they are transport metadata.

If a backend contract explicitly requires a value as a business-body field, the controlled context resolver may provide it.

## 13. Store Contract

The final Store contract is intentionally small:

```text
DynamicIntent
├── Load
├── Retry
├── Action
└── ValueChanged
```

The exact existing intent names may be retained where useful.

The Store performs:

```text
Intent
  ↓
UseCase
  ↓
State/Effect
```

The Store does not perform:

```text
HTTP
JSON serialization
binding resolution
request-key aliasing
hardcoded device IDs
navigation construction
URI launching
renderer selection
recursive rendering
```

The Store may retain only the minimum binding state required by the current Dynamic screen interaction.

## 14. Error Model

Errors must cross boundaries as application/domain failures rather than leaking Ktor/serialization implementation details into Store/UI.

Minimum categories:

```text
screen load failure
request failure
invalid/missing destination data
unsupported Dynamic definition/action
invalid Dynamic screen/document
```

The implementation must preserve cancellation semantics.

No catch-all error should silently convert cancellation into a normal UI failure.

## 15. `DefaultApplicationBootstrap` Decision

`DefaultApplicationBootstrap` is not a Dynamic class.

It remains responsible for the Bootstrap API operation:

```text
GET /api/v1/partner/config/bootstrap
        ↓
Bootstrap response
        ↓
Bootstrap domain output
```

The confusing part is not its existence but its boundary. The refactor must make the boundary obvious through types and dependencies.

It must not:

- render Dynamic UI;
- own Dynamic navigation;
- construct Dynamic Store state;
- execute Dynamic screen actions;
- become a generic application orchestrator.

If common request headers are centralized by the already-frozen Runtime Infrastructure during implementation, Bootstrap will use that common infrastructure instead of retaining duplicated transport metadata logic.

## 16. Dependency Direction

Required direction:

```text
Presentation / DynamicScreen / Store
            ↓
          UseCase
            ↓
      Repository interface
            ↓
     Data / RemoteDataSource
            ↓
          Ktor
```

Rendering depends on Dynamic domain models and registry/definitions.

Navigation is an effect/consumer boundary, not a repository concern.

The domain layer must not depend on Compose UI or Ktor.

## 17. Implementation Sequence

The complete feature is implemented in this order, without separate approval gates between units.

### Unit 1 — Baseline and contract lock

- verify branch and current source state;
- preserve user changes;
- verify current Dynamic discussion is frozen;
- create this implementation plan;
- use this plan as the implementation contract.

### Unit 2 — Model ownership cleanup

- inspect existing Dynamic models;
- keep one canonical owner;
- remove duplicates only when proven duplicated;
- ensure `DynamicDestination` contains the canonical destination fields;
- ensure next-screen mapping is represented consistently.

### Unit 3 — Registry vocabulary cleanup

- remove `form_template` registration;
- remove `input` registration;
- remove `button` registration;
- retain only stack template/component/section/group and text/image;
- enforce explicit category lookup and duplicate rejection.

### Unit 4 — Definition cleanup

- retain only the agreed initial definition implementations;
- delete FormTemplate/InputElement/ButtonElement;
- preserve parent/child rendering rules;
- keep definitions free from navigation/network/business state.

### Unit 5 — Use-case boundary

- add/refactor `LoadDynamicScreenUseCase`;
- add/refactor `ExecuteDynamicActionUseCase`;
- move orchestration out of Store;
- keep use cases meaningful rather than method-sized abstractions.

### Unit 6 — Repository/data boundary

- keep Dynamic API access behind `DynamicRepository`;
- move request construction/serialization out of Store;
- use existing `RemoteDataSource`/Runtime Infrastructure rather than a second HTTP client boundary;
- preserve common request metadata responsibility at the infrastructure boundary.

### Unit 7 — Binding/context boundary

- remove hardcoded `deviceId` from Store;
- implement generic binding resolution at the correct request/application boundary;
- separate `$binding` from `$context`;
- do not add challenge/previous-response infrastructure.

### Unit 8 — Action boundary

- simplify `DynamicActionHandler`;
- separate request, navigation, and external URI semantics;
- ensure request `responseMode = destination` maps backend `nextScreen` to `DynamicDestination`;
- keep future action types extensible but unimplemented until required.

### Unit 9 — Store reduction

- reduce `DynamicStore` to MVI/UDF orchestration;
- remove request/HTTP/JSON/navigation/business logic;
- retain only state, intents, effects, use-case invocation, and minimal binding interaction state.

### Unit 10 — DynamicScreen integration

- keep `DynamicScreen` as the permanent Dynamic container;
- make destination changes restart/reload the correct screen operation;
- ensure renderer receives the correct destination template identity;
- keep Compose-only rendering responsibilities in presentation/renderer.

### Unit 11 — Bootstrap integration

- verify Bootstrap `startup.nextScreen` becomes the initial `DynamicDestination`;
- keep `DefaultApplicationBootstrap` outside Dynamic;
- remove duplicated common transport metadata only where the existing Runtime Infrastructure contract supports it;
- do not change Bootstrap business behavior.

### Unit 12 — End-to-end flow verification

Prove:

```text
Bootstrap
 ↓
startup.nextScreen
 ↓
DynamicDestination
 ↓
Dynamic navigation
 ↓
Dynamic screen endpoint
 ↓
Dynamic screen JSON
 ↓
stack_template
 ↓
stack_component
 ↓
stack_section/group
 ↓
text/image
```

Also prove a current-screen request can produce a `nextScreen` destination and that the next destination uses its own `templateId` while sharing a reusable `templateType` where applicable.

## 18. Exact Non-Goals

The implementation must NOT:

- add ViewModel;
- create business-specific screen classes;
- create a second SDUI engine;
- create a second NodeDefinition registry;
- create a second validator;
- create a generic workflow engine;
- create response history merely for challenge IDs;
- add speculative state-machine frameworks;
- add reflection/package scanning;
- add generic plugin architecture;
- add factories/factory-factories without a concrete requirement;
- implement `form_template`, `input`, or `button` in the initial Dynamic vocabulary;
- add unrelated feature functionality;
- redesign the fixed backend JSON contract;
- redesign Bootstrap architecture outside the required boundary cleanup;
- introduce a new Gradle module.

## 19. Tests

Focused tests must cover:

### Destination

- destination mapping from bootstrap `nextScreen`;
- destination mapping from action response `nextScreen`;
- template ID preserved;
- template type preserved;
- endpoint/method/authentication preserved;
- invalid/missing destination data fails clearly.

### Registry

- all six initial definitions register correctly;
- category-specific lookup works;
- duplicate registration fails;
- unknown type produces controlled unsupported result;
- forbidden current definitions are not registered.

### Renderer

- stack template renders;
- stack component renders;
- stack section renders;
- stack group renders;
- text renders;
- image renders;
- backend ordering is preserved;
- unsupported definitions do not silently substitute another type.

### Binding/context

- arbitrary binding keys can be captured;
- multiple bindings can be submitted;
- binding values are distinct from context values;
- common device/platform metadata is not hardcoded into Store;
- no challenge-specific state is required.

### Actions

- request action executes through the use-case/repository boundary;
- request destination response maps to `DynamicDestination`;
- navigate produces navigation effect/destination;
- external URI produces external URI effect;
- unsupported future action is controlled.

### Store

- intents invoke use cases;
- state transitions are correct;
- effects are emitted correctly;
- Store contains no direct HTTP/request-building logic;
- Store contains no renderer/registry logic.

### Integration

At minimum prove the generic bootstrap → destination → screen load → render path and destination-to-destination transition path.

## 20. Verification Commands

After implementation, use the repository's documented Gradle workflow and current KMP verification commands.

Minimum relevant verification:

```text
./gradlew.bat projects
./gradlew.bat :feature:dynamic:jvmTest
./gradlew.bat :feature:dynamic:compileKotlinJvm
```

Run broader repository gates required by the implementation/review protocol when the final change crosses module boundaries.

Do not weaken tests or schemas to make verification pass.

## 21. Acceptance Criteria

The Dynamic refactor is accepted only when all are true:

1. Dynamic Store is small and MVI/UDF-only.
2. Meaningful use cases exist and own application operations.
3. Dynamic API access is behind repository/data boundaries.
4. DynamicDestination is the canonical destination contract.
5. `templateType` selects rendering behavior.
6. `templateId` identifies the concrete destination/back-stack instance.
7. Current screen responses can produce the next destination descriptor.
8. Bootstrap `startup.nextScreen` uses the same destination model.
9. Navigation and external URI are separate capabilities.
10. Binding is generic for arbitrary form/input data.
11. Context is separate from binding.
12. Common transport metadata is not duplicated in Dynamic request logic.
13. No challenge-ID/previous-response architecture is introduced.
14. Only `stack_template`, `stack_component`, `stack_section`, `stack_group`, `text`, and `image` are registered in the initial Dynamic implementation.
15. No ViewModel exists.
16. No business-screen-specific Dynamic classes exist.
17. Canonical SDUI engine/contract remains authoritative.
18. No second validator/registry/SDUI language is introduced.
19. Focused tests pass.
20. Required build/verification gates pass.
21. Final source matches this plan exactly.

## 22. Strict Plan-Compliance Gate

Before declaring implementation complete, perform a literal plan-to-source audit.

For every plan item:

```text
PLAN ITEM
   ↓
SOURCE EVIDENCE
   ↓
TEST/VERIFICATION EVIDENCE
   ↓
PASS / FAIL
```

The final review must explicitly verify:

```text
No planned file missing.
No required responsibility missing.
No forbidden responsibility retained.
No unplanned abstraction added.
No unplanned module added.
No current definition outside the frozen initial vocabulary registered.
No Store network/request construction remains.
No business-screen class introduced.
No ViewModel introduced.
No silent architecture deviation.
```

A single unresolved plan item means the feature is **not complete**.

## 23. Documentation After Implementation

After the complete implementation and tests:

- create/update `docs/features/dynamic/02-IMPLEMENTATION-STATUS.md`;
- record implemented files and final responsibilities;
- record test/build evidence;
- record any deviations — expected result is none;
- record final review findings;
- record final feature-freeze state.

Do not create separate plan/freeze documents for individual implementation units.

## 24. Final Implementation Rule

```text
FROZEN DISCUSSION
      ↓
THIS COMPLETE PLAN
      ↓
IMPLEMENT ALL ITEMS
      ↓
TEST COMPLETE FEATURE
      ↓
LITERAL PLAN-TO-SOURCE AUDIT
      ↓
REVIEW
      ↓
FEATURE FROZEN
```

No silent deviation is permitted.
