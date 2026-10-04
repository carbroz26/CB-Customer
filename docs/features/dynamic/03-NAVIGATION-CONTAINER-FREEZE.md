# Dynamic UI — Navigation & Dynamic Container Freeze

## Status

**FROZEN — AUTHORITATIVE**

This document is the governing navigation/container contract. Any older navigation wording in Dynamic documentation is superseded by this document.

## 1. One application navigation module

Use the existing `:navigation` module and its already-selected Navigation 3 implementation. Do not introduce another navigation framework or a second application navigation system.

`:navigation` owns application navigation history/mechanics. Dynamic does not own Navigation 3 APIs and `DynamicStore` does not mutate the navigation stack directly.

## 2. One permanent Dynamic Container

There is exactly one shared Dynamic Container.

```text
Application
   ↓
:navigation
   ↓
Dynamic Container
   ↓
current Dynamic template
```

Dynamic does not create native/business screens such as LoginScreen, OtpScreen, ProfileScreen, DashboardScreen, etc.

The Dynamic Container is the stable host. Backend-defined template instances are the replaceable content represented by the navigation destination.

## 3. Canonical DynamicDestination

The canonical domain destination contains exactly:

```text
DynamicDestination
├── screenId
├── templateId
├── templateType
└── endpoint
```

No `method` or `authentication` belongs in `DynamicDestination`. Those are request-action metadata and remain on the request path.

Meaning:

```text
templateId
    → identity of the concrete Dynamic template instance/destination

templateType
    → supported Dynamic template type used to resolve the template capability

endpoint
    → API endpoint used to load the complete Dynamic screen definition
```

No phone number, OTP, name, ID, arbitrary business data, or other runtime value is passed through the destination.

## 4. Navigation 3 boundary

`domain` remains independent of Navigation 3.

```text
Domain
└── DynamicDestination
        ↓
:navigation
└── Navigation 3 adapter/key
        ↓
Navigation 3 back stack
```

The Navigation 3 adapter may wrap the complete canonical `DynamicDestination`; it must not create a second destination model or duplicate destination semantics.

## 5. Destination activation and screen loading

The destination already tells Dynamic the template instance, template type, and endpoint. There is no empty-template or empty-instance stage.

The correct lifecycle is:

```text
DynamicDestination
      │
      ├── templateId
      ├── templateType
      └── endpoint
      │
      ↓
Dynamic Container
      │
      ↓
Template Registry
      │
      │ templateType
      ↓
resolve supported template capability/host
      │
      ↓
LoadDynamicScreenUseCase
      │
      │ endpoint
      ↓
Dynamic API
      │
      ↓
Complete Dynamic Screen Response
      │
      ↓
actual template definition/content
      │
      ├── template properties
      ├── components
      ├── sections
      ├── groups
      ├── elements
      ├── events
      ├── actions
      ├── bindings
      └── references
      │
      ↓
render complete Dynamic UI
```

The Template Registry does not call the API and does not render the backend definition. It only resolves the frontend-supported template capability for `templateType`.

`LoadDynamicScreenUseCase` is the only Dynamic screen-loading use case. It loads the complete screen response using the destination endpoint.

The API response is authoritative for the actual template definition. The frontend does not invent missing UI data from destination metadata.

## 6. Navigation operations

Application navigation exposes only the existing navigation semantics required by the selected Navigation 3 implementation, including root/push/replace/pop behavior where applicable.

Dynamic template-stack behavior is represented through those navigation operations; `templateType` never decides whether navigation is push or replace.

Conceptually:

```text
replace
    current destination → new destination

push/add
    current destination → previous + new destination

pop/dismiss/back
    current destination removed → previous destination
```

These are navigation-state operations. They do not perform HTTP themselves.

## 7. Action → navigation boundary

Actions and navigation mechanics remain separate.

```text
Dynamic UI event
      ↓
Action
      ↓
action-specific runtime behavior
      ↓
navigation effect/result
      ↓
:navigation
      ↓
Dynamic Container
```

There is **no `ExecuteDynamicActionUseCase`**. Action execution belongs to the Dynamic runtime/action infrastructure and its action-specific behavior/handlers. `LoadDynamicScreenUseCase` remains the only Dynamic screen-loading use case.

The Dynamic Store owns Dynamic runtime state/effects, not application navigation history.

## 8. Direct navigate action

A direct `navigate` action supplies a complete `DynamicDestination`.

```text
navigate
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
   ↓
Dynamic API
   ↓
complete response
   ↓
render
```

## 9. Request action → destination

A request action is different from direct navigation because it first performs its request.

```text
request action
   ↓
resolve references/bindings
   ↓
request API
   ↓
response
   ↓
if responseMode = destination
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
   ↓
Dynamic API
   ↓
complete response
   ↓
render
```

If `responseMode = none`, the request completes without destination navigation.

Direct `navigate` and `request(responseMode = destination)` therefore converge on the same `DynamicDestination` and the same template activation/loading flow.

## 10. Bootstrap → Dynamic

Bootstrap uses the same destination contract:

```text
Bootstrap API
   ↓
startup.destination
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
   ↓
endpoint
   ↓
Dynamic API
   ↓
complete Dynamic response
   ↓
render
```

Bootstrap, direct navigation, and request-generated navigation do not have separate destination mechanisms.

## 11. External URI and future capabilities

`external_uri` is not an internal Dynamic destination.

```text
external_uri
   ↓
external URI capability/effect
   ↓
platform boundary
```

Future capabilities such as `dismiss`, `present`, `deeplink`, or other action types remain separate capabilities and must be added only when their backend contract is required.

## 12. Back / refresh

Back/pop removes the current complete destination and exposes the previous destination in navigation history.

Refresh reloads the current destination and does not add another navigation entry.

The Dynamic Store must not directly manipulate Navigation 3 state.

## 13. Explicit prohibitions

Do not introduce:

- a second navigation framework;
- a second application navigation abstraction without a concrete responsibility;
- business-specific navigation classes;
- one Dynamic Container per template;
- Login/OTP/Profile/Dashboard native screen classes;
- navigation state inside `DynamicStore`;
- API calls from `:navigation`;
- `method` or `authentication` inside `DynamicDestination`;
- template-type-based push/replace decisions;
- business data as navigation arguments;
- an `EmptyTemplateInstance` abstraction;
- `ExecuteDynamicActionUseCase`;
- external URI as an internal destination;
- speculative NavigationManager/Coordinator/Router layers.

## 14. Frozen target flow

```text
Platform/App
      ↓
:navigation / Navigation 3
      ↓
DynamicDestination
      ↓
one Dynamic Container
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

This is the authoritative navigation/container contract for implementation.