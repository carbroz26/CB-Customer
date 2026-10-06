# CB-Partner Dynamic UI — Architecture Frozen

**Status:** FROZEN — CANONICAL  
**Date:** 2026-10-06  
**Branch:** `feature/dynamic-ui`

This document is the single authoritative source for already-frozen Dynamic architecture and contracts.

## 1. Scope

Dynamic is a generic, backend-driven UI system. Business names such as Login, OTP, Dashboard and Profile are backend data, not frontend architecture concepts.

Base architecture, authentication business logic and backend implementation remain outside this document.

## 2. Runtime model

```
Application
   ↓
:navigation
   ↓
one permanent Dynamic Container
   ↓
DynamicDestination
   ↓
Template Registry(templateType)
   ↓
LoadDynamicScreenUseCase(endpoint)
   ↓
Dynamic API
   ↓
Complete Dynamic Screen Response
   ↓
typed Dynamic model
   ↓
DynamicRenderer
   ↓
Template / Component / Section / Group / Element
```

There is no empty template or empty template-instance stage. The destination already identifies the concrete template instance, supported template type and endpoint. The API response supplies the complete actual template content.

## 3. Canonical destination

```
DynamicDestination
├── screenId
├── templateId
├── templateType
└── endpoint
```

- `templateId` identifies the concrete Dynamic template instance/back-stack entry.
- `templateType` selects the supported frontend template capability.
- `endpoint` is used to load the complete screen definition.
- Destination contains no method, authentication, phone, OTP, challenge ID or other business/runtime value.

Bootstrap, direct navigation and request-generated navigation all use this same destination model.

## 4. Dynamic hierarchy

The current typed hierarchy is:

```
Screen
└── Template
    └── Component(s)
        ├── Element(s)
        └── Section(s)
            ├── Element(s)
            └── Group(s)
                └── Element(s)
```

Backend ordering is significant and must be preserved.

Concrete frozen models:

- `FormTemplate`
- `StackComponent`
- `StackSection`
- `StackGroup`
- `TextElement`
- `ImageElement`
- `InputElement`
- `ButtonElement`
- `IconElement`
- `DividerElement`

A minimal technical `DynamicElement` common type may exist only because heterogeneous element collections require a Kotlin common type. It is not a product-level generic UI abstraction.

Do not introduce generic product concepts such as `DynamicTemplate`, `DynamicComponent`, `DynamicSection`, `DynamicGroup`, or `DynamicElementDto`.

## 5. Theme and values

Theme is screen-level and remains separate from the template:

```
DynamicScreen
├── screen identity
├── template
└── theme
```

`DynamicValue` represents raw/backend dynamic values used where the contract is intentionally generic, such as arbitrary response data and action payloads. It is not a generic UI property bag.

## 6. Registries

Registration is explicit. No reflection, package scanning or automatic discovery.

The registry responsibilities remain capability lookup only:

- TemplateRegistry → template capability
- ElementRegistry → element renderer capability

Registry code does not own API loading, navigation history, action execution or canonical Dynamic state.

## 7. Screen loading

`LoadDynamicScreenUseCase` is the Dynamic screen-loading use case.

It loads the endpoint from `DynamicDestination` and obtains the complete Dynamic response through the existing data/network boundary.

There is no `ExecuteDynamicActionUseCase`.

## 8. Action boundary

The canonical action flow is:

```
User interaction
    ↓
DynamicStore
    ↓
DynamicActionExecutor
    ↓
DynamicActionHandler
    ↓
DynamicActionResult
    ↓
DynamicStore
```

Frozen action categories:

```
request
navigate
present
dismiss
state
external_uri
sequence
```

Sequence actions recursively use the same `DynamicActionExecutor`; they do not create another execution pipeline.

Do not recreate the removed action use case under another name such as ActionManager, ActionCoordinator, PreparedAction or Command layer.

## 9. Request boundary

Request execution metadata belongs to the request/action path:

```
method
endpoint
authentication
validation
headers/query where supported
body
responseMode
```

`DynamicDestination` remains independent of request metadata.

For `responseMode = destination`, the response destination is read from `data.destination` and becomes the same `DynamicDestination` used by direct navigation.

For `responseMode = none`, no destination navigation occurs.

## 10. Binding and references

Bindings are generic backend-defined runtime state:

```
input/value change
   ↓
binding key
   ↓
DynamicScreenState / Dynamic runtime state
```

References:

```
$literal
$binding
$context
$response
```

`DynamicValueResolver` and `DynamicActionValueResolver` remain separate responsibilities.

Bindings never become navigation arguments.

## 11. Store/state ownership

`DynamicStore` owns Dynamic lifecycle, state, binding state, interaction/action orchestration and effects.

`DynamicScreenState` remains the canonical mutable binding/state owner.

No second FormStore, ViewModel, element-local canonical state store or duplicate Dynamic state owner is allowed.

## 12. Navigation boundary

Existing `:navigation` owns application navigation history/mechanics.

Dynamic does not introduce a second navigation framework.

Dynamic Store does not directly own the Navigation 3 back stack.

`templateType` never decides push versus replace.

`external_uri` is an external capability/effect, not an internal Dynamic destination.

## 13. Renderer/runtime boundary

Runtime resolves templates, values, validation and rendering context.

Renderer translates typed Dynamic models into Compose UI.

SDUI elements render their own UI and emit interaction intents; they do not perform network calls, repository operations, navigation implementation or action orchestration.

## 14. Data/network boundary

The dependency direction remains:

```
feature:dynamic
      ↓
domain contracts
      ↓
data repository
      ↓
network transport
```

Dynamic UI code does not absorb HTTP transport or repository implementation.

## 15. Absolute rules

1. One permanent Dynamic Container.
2. Backend response is authoritative for actual Dynamic UI content.
3. No empty template/instance stage.
4. One four-field DynamicDestination.
5. No business/runtime values in destinations.
6. Existing :navigation is reused.
7. No ExecuteDynamicActionUseCase.
8. DynamicActionExecutor is the single action-dispatch boundary.
9. LoadDynamicScreenUseCase is the screen-loading use case.
10. Bindings are generic runtime state, never navigation arguments.
11. No reflection/package scanning.
12. No silent type substitution or property downgrade.
13. No duplicate validation/state/action architecture.
14. Sensitive values, OTPs, credentials and tokens are never logged.
15. Frozen architecture is not changed silently; contradictions require discussion/decision/freeze before implementation.
