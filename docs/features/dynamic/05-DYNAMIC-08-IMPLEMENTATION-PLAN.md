# DYNAMIC-08 — Unified Destination / Action / Request Implementation Plan

## Status

**PLAN FROZEN — IMPLEMENTATION AUTHORIZED**

The governing discussion is `04-DYNAMIC-08-UNIFIED-DESTINATION-DISCUSSION.md`.
The governing navigation/container boundary is `03-NAVIGATION-CONTAINER-FREEZE.md`.
The global Dynamic implementation plan is `01-IMPLEMENTATION-PLAN.md`.

## 1. Canonical destination

Implement exactly one domain destination:

```text
DynamicDestination
├── screenId
├── templateId
├── templateType
└── endpoint
```

Do not add request `method` or `authentication` to this model.

## 2. Canonical screen-loading flow

```text
DynamicDestination
      ↓
Dynamic Container
      ↓
Template Registry(templateType)
      ↓
LoadDynamicScreenUseCase(endpoint)
      ↓
DynamicRepository
      ↓
DynamicRemoteDataSource
      ↓
Dynamic API
      ↓
Complete Dynamic Screen Response
      ↓
Template definition/content
      ↓
DynamicRenderer
      ↓
Component / Section / Group / Element
```

The destination already supplies `templateId`, `templateType`, and `endpoint`. Do not create an empty template/instance abstraction.

The Template Registry resolves the supported frontend template capability only. The API response is authoritative for the actual template definition and all of its properties/children/actions/bindings/references.

## 3. Bootstrap

Map:

```text
startup.destination → DynamicDestination
```

Bootstrap must enter the same navigation/container flow as every other Dynamic destination.

## 4. Direct navigate action

Map:

```text
action.type = navigate
action.destination = DynamicDestination
```

Then:

```text
navigate action
  ↓
DynamicDestination
  ↓
:navigation
  ↓
Dynamic Container
  ↓
Template Registry
  ↓
LoadDynamicScreenUseCase
```

## 5. Request action

Request-specific data remains request-owned:

```text
request payload
├── method
├── endpoint
├── authentication
├── validation
├── headers/query where supported
├── body
└── responseMode
```

Before transport, resolve generic references and bindings. Build the JSON body according to the backend contract. Do not create screen-specific request builders.

For `responseMode = destination`:

```text
request
  ↓
API response
  ↓
data.destination
  ↓
DynamicDestination
  ↓
:navigation
  ↓
Dynamic Container
  ↓
Template Registry
  ↓
LoadDynamicScreenUseCase(destination.endpoint)
```

For `responseMode = none`, do not navigate.

## 6. Action architecture

Do not introduce `ExecuteDynamicActionUseCase`.

The implementation must keep action execution in the existing Dynamic runtime/action infrastructure and use action-specific handlers/capabilities for:

```text
request
navigate
present
dismiss
state
external_uri
sequence
```

Actions must remain generic. No Login/OTP/Profile-specific action classes or logic.

## 7. Binding architecture

Binding is backend-keyed runtime state:

```text
input/value change
  ↓
binding key
  ↓
Dynamic state
```

At action/request execution:

```text
$binding(key)
  ↓
current Dynamic value
  ↓
backend-defined JSON body/value
```

Multiple bindings are supported. Do not concatenate unrelated values with commas. Preserve the backend-defined JSON object/array/value structure.

Bindings are never navigation arguments.

## 8. Generic references

Support the frozen vocabulary:

```text
$literal
$binding
$context
$response
```

References read values. Actions perform effects.

## 9. Navigation/container implementation boundary

`:navigation` owns Navigation 3 history/mechanics.

Dynamic owns:

```text
Dynamic Container
Template Registry
Template rendering
Dynamic state
Action behavior
LoadDynamicScreenUseCase orchestration
```

`:navigation` must not call Dynamic APIs or render Dynamic UI.

Dynamic Store must not directly mutate Navigation 3 state.

## 10. Template stack semantics

Use the existing navigation mechanics for:

```text
root
push/add
replace
pop/back/dismiss
```

`templateType` never determines push vs replace.

## 11. Required source changes

1. Remove any remaining `ExecuteDynamicActionUseCase` production/test contract if it is still present.
2. Keep only `LoadDynamicScreenUseCase` as the Dynamic screen-loading use case.
3. Ensure `DynamicDestination` contains exactly four fields.
4. Remove stale `nextScreen`/`DynamicNextScreen` contracts.
5. Ensure direct navigate actions use `action.destination`.
6. Ensure request `responseMode = destination` resolves `data.destination`.
7. Ensure request method/authentication stay on request metadata.
8. Ensure bindings resolve from generic Dynamic state at execution time.
9. Ensure destination never carries business values.
10. Ensure Dynamic Container uses one template host and does not create business-screen classes.
11. Ensure Template Registry is lookup/capability resolution only.
12. Ensure API loading occurs through `LoadDynamicScreenUseCase`.
13. Ensure API response content is the source of the actual template definition.
14. Ensure existing `:navigation` is reused; no second navigation framework is introduced.

## 12. Tests

Add/maintain tests for:

- Bootstrap `startup.destination` mapping.
- Direct `navigate` destination mapping.
- Request `data.destination` mapping.
- `responseMode = destination` convergence.
- Four-field destination contract.
- No business data in destination.
- Template Registry lookup by `templateType`.
- Screen loading by destination `endpoint`.
- Complete response rendered only after API response is available.
- Binding resolution with one binding.
- Binding resolution with multiple bindings.
- Backend-defined JSON request body construction.
- Missing/invalid binding behavior.
- Template replace/push/pop behavior.
- No action execution through `ExecuteDynamicActionUseCase`.

## 13. Verification

Run the affected compile/JVM tests plus the existing Dynamic/navigation tests.

Required assertions:

```text
DynamicDestination = exactly four fields
LoadDynamicScreenUseCase = only Dynamic screen-loading use case
Template Registry ≠ API loader
Dynamic Container ≠ business screen
:navigation ≠ Dynamic API caller
binding ≠ navigation argument
request destination == navigate destination model
```

No implementation may proceed against an older conflicting action/navigation contract.