# CB-Partner Dynamic UI — Implementation Status

**Status:** IMPLEMENTATION IN PROGRESS  
**Updated:** 2026-10-05  
**Branch:** `feature/dynamic-ui`

This file records implementation state only. It does not redefine or override frozen architecture.

## 1. Governing documentation

Follow these documents during implementation:

1. `00-FEATURE-DISCUSSION.md` — frozen Dynamic architecture.
2. `03-NAVIGATION-CONTAINER-FREEZE.md` — navigation/container boundary.
3. `04-DYNAMIC-08-UNIFIED-DESTINATION-DISCUSSION.md` — destination/action/request/binding contract.
4. `05-DYNAMIC-08-IMPLEMENTATION-PLAN.md` — DYNAMIC-08 implementation contract.
5. `01-IMPLEMENTATION-PLAN.md` — overall implementation sequence.

`00-MODULE-IMPLEMENTATION-PLAN.md` is superseded.

## 2. Frozen runtime contract

### Destination

```text
DynamicDestination
├── screenId
├── templateId
├── templateType
└── endpoint
```

### Template loading

```text
DynamicDestination
  ↓
Dynamic Container
  ↓
Template Registry(templateType)
  ↓
LoadDynamicScreenUseCase(endpoint)
  ↓
Dynamic API
  ↓
complete template definition
  ↓
render
```

There is no empty template/instance stage.

### Actions

```text
Event
  ↓
Action
  ↓
dedicated action handler
```

There is no `ExecuteDynamicActionUseCase`.

Current handlers are separated for `navigate`, `request`, `external_uri`, `dismiss`, `present`, `state`, and `sequence`, with a small dispatcher only for selecting the handler.

### Request destination

```text
request
  ↓
API
  ↓
data.destination
  ↓
DynamicDestination
  ↓
same navigation/container/loading flow
```

### Binding

```text
input/value change
  ↓
binding key
  ↓
Dynamic Store state
  ↓
$binding resolution at action execution
```

Bindings never travel through navigation arguments.

## 3. Implemented in this pass

- Removed `ExecuteDynamicActionUseCase` and its obsolete test.
- Reduced Dynamic DI to `LoadDynamicScreenUseCase` plus dedicated action handlers/dispatcher.
- Kept `LoadDynamicScreenUseCase` as the only Dynamic screen-loading use case.
- Connected Dynamic actions to the existing `:navigation` `DynamicNavigationState`.
- Added replace/push navigation behavior for Dynamic destinations.
- Added request response-destination convergence into the same navigation flow.
- Added generic binding/context/response resolution for request payloads.
- Kept navigation state outside `DynamicStore`; the Dynamic Container supplies the existing navigation state when an action is executed.
- Kept the single Dynamic Container model.

## 4. Verification status

The implementation changes are committed on `feature/dynamic-ui`.

Repository-side source inspection was completed, but a local Gradle/JVM test run could not be executed from the remote editing environment because it has no network access to clone the repository. Therefore the implementation is **not yet marked test-verified**.

## 5. Remaining implementation work

| Priority | Work | State |
|---:|---|---|
| 1 | Run `:feature:dynamic:jvmTest` and affected JVM compilation locally | **NEXT** |
| 2 | Fix any compile/test issues from the action-handler refactor | PENDING |
| 3 | Verify request metadata/authentication integration | PENDING/VERIFY |
| 4 | Verify Template Registry → screen loading → rendering lifecycle | PENDING |
| 5 | Verify binding/reference behavior with multiple values | PENDING |
| 6 | Backend-shaped Dynamic navigation/request integration tests | PENDING |
| 7 | Push/replace/pop/back runtime verification | PENDING |
| 8 | Android verification | PENDING |
| 9 | iOS/macOS-hosted verification | PENDING |
| 10 | Web/Wasm verification | PENDING |
| 11 | Literal plan-to-source compliance review | PENDING |
| 12 | Final Dynamic review and code freeze | PENDING |

## 6. Continuation rules

- Do not reopen frozen DYNAMIC decisions unless an explicit new discussion is started.
- Do not restore `DynamicScreen.nextScreen` or `DynamicNextScreen`.
- Do not add `method` or `authentication` to `DynamicDestination`.
- Do not restore `ExecuteDynamicActionUseCase`.
- Do not create business-specific screens.
- Do not create another navigation framework.
- Do not create an empty template/instance abstraction.
- Do not pass business values through navigation.
- Do not let `:navigation` call Dynamic APIs.
- Do not let `DynamicStore` own Navigation 3 state.
- Do not introduce speculative manager/coordinator/router layers.

## 7. Next action

Run the local JVM test/compile verification against the implementation, then fix only implementation defects revealed by the tests. Do not reopen the frozen architecture.
