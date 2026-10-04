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
action-specific runtime behavior
```

There is no `ExecuteDynamicActionUseCase`.

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
Dynamic state
  ↓
$binding resolution at action execution
```

Bindings never travel through navigation arguments.

## 3. Completed / verified baseline

The previous Bootstrap/DYNAMIC-08 source reconciliation and JVM verification were green:

```text
:domain:compileKotlinJvm
:data:compileKotlinJvm
:feature:splash:compileKotlinJvm
:feature:dynamic:compileKotlinJvm
:navigation:compileKotlinJvm
:app:compileKotlinJvm
```

and the affected JVM verification set completed successfully according to the previous reconciliation record.

## 4. Current implementation state

The documentation has now been reconciled so the implementation target is unambiguous:

- one canonical four-field `DynamicDestination`;
- existing `:navigation` remains the application navigation module;
- one permanent Dynamic Container;
- Template Registry resolves `templateType` only;
- `LoadDynamicScreenUseCase` is the only Dynamic screen-loading use case;
- API response provides the actual template definition/content;
- direct navigate and request-generated destinations converge;
- request metadata stays on request actions;
- bindings/references remain generic;
- no business data is passed through navigation;
- no `ExecuteDynamicActionUseCase`.

## 5. Remaining implementation work

| Priority | Work | State |
|---:|---|---|
| 1 | Verify current source matches the reconciled navigation/action/use-case contract | **NEXT** |
| 2 | Complete Dynamic state/reference/action behavior | PENDING |
| 3 | Verify request metadata/authentication integration | PENDING/VERIFY |
| 4 | Complete frozen network error taxonomy | PENDING |
| 5 | Complete DYNAMIC-07 fallback/recovery/observability | PENDING |
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
- Do not let Dynamic Store own Navigation 3 state.
- Do not introduce speculative manager/coordinator/router layers.

## 7. Next action

Inspect the current source against the reconciled documents and identify exact implementation gaps. Only then proceed with implementation changes covered by the frozen plan.