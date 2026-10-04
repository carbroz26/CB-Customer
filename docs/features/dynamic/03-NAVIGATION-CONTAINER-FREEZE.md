# Dynamic UI — Navigation & Dynamic Container Freeze

## Status

**DISCUSSION FROZEN — IMPLEMENTATION AUTHORIZED THROUGH `01-IMPLEMENTATION-PLAN.md`**

This record freezes the final navigation/container decisions made after forensic review of the current repository. These decisions supersede any earlier implementation detail that conflicts with them. They do not change the fixed backend JSON contract or the already-frozen DYNAMIC-01 through DYNAMIC-07 SDUI vocabulary.

## 1. Supported application platforms

CB-Partner supports:

```text
Android
 iOS
 Web (Wasm)
```

There is **no `desktopApp`** and no desktop-specific Dynamic or navigation implementation will be created.

The shared Compose Multiplatform application architecture is the common implementation. Platform entry points remain thin and exist only at genuine platform boundaries.

## 2. Navigation technology

The shared `:navigation` module will use the already-selected Navigation 3 dependencies in the repository. Navigation 3 is the navigation foundation; a second navigation framework is not introduced.

Navigation 3 is appropriate because its model gives the application ownership of the back stack and represents entries as keys, which matches backend-driven `DynamicDestination` values. The current version-catalog versions remain unchanged by this decision unless a separate dependency decision is explicitly approved.

## 3. DynamicDestination is the navigation key

The complete backend-defined `DynamicDestination` is the identity of a navigable Dynamic destination.

It contains the information required by the frozen contract, including:

```text
screenId
templateId
templateType
endpoint
method
authentication
```

The destination must not be reduced to a route string, template type, or template ID alone.

`templateType` answers:

> Which registered template definition renders this destination?

`templateId` identifies the concrete destination/template instance.

`endpoint`, `method`, and `authentication` identify how the actual Dynamic screen document is loaded.

No business-screen route names are hardcoded into the navigation layer.

## 4. One permanent Dynamic Container

There is exactly one shared Dynamic Container for the Dynamic feature.

Mental model:

```text
DynamicContainer ≈ stable Activity-like host
Template         ≈ replaceable Fragment-like content
```

This is a conceptual analogy only. The implementation is platform-independent Compose Multiplatform and does not create Activities/Fragments for Dynamic screens.

The container remains stable while the current backend-defined destination/template changes.

```text
Application
  ↓
Navigation
  ↓
Dynamic Container
  ↓
current DynamicDestination
  ↓
current template definition
  ↓
Dynamic screen content
```

There are no native `LoginScreen`, `OtpScreen`, `DashboardScreen`, or equivalent business-screen classes.

## 5. Navigation stack ownership

Navigation owns navigation history.

The stack contains complete `DynamicDestination` entries.

Example:

```text
[
  Login DynamicDestination,
  OTP DynamicDestination
]
```

Back removes the current destination and exposes the previous complete destination.

Refresh reloads the current destination without adding another stack entry.

Navigation does not call the backend and does not render SDUI nodes.

## 6. Navigation operations

The minimum navigation boundary supports only operations that have a concrete current requirement:

```text
setRoot(destination)
push(destination)
replace(destination)
pop()
```

These operations manipulate navigation state. They do not execute Dynamic API requests.

`replace` is required because a backend-driven flow may need to replace the current destination rather than add another history entry.

No generic `NavigationManager`, `NavigationCoordinator`, `NavigationRouter`, or framework wrapper hierarchy is created without a concrete responsibility.

## 7. Dynamic action vs navigation capability

Backend actions and navigation mechanics remain separate.

```text
navigate
  ↓
Dynamic navigation destination/effect
  ↓
Navigation boundary
```

```text
external_uri
  ↓
External URI capability
```

`external_uri` is **not** represented as an internal navigation destination.

Future action types such as `dismiss`, `present`, `deeplink`, or other backend-defined actions can be added at their correct capability boundary when the backend contract requires them. They are not preimplemented speculatively.

## 8. Dynamic Store boundary

`DynamicStore` does not own navigation state or Navigation 3 APIs.

It may emit a navigation effect/result when Dynamic action execution requires navigation.

```text
DynamicStore
  ↓
use case / action execution
  ↓
navigation effect
  ↓
Navigation boundary
```

The Store owns Dynamic screen/runtime state, not application navigation history.

## 9. Dynamic screen loading boundary

The navigation boundary does not load the Dynamic screen API.

The Dynamic Container/Store flow uses the current destination:

```text
DynamicDestination
  ↓
LoadDynamicScreenUseCase
  ↓
DynamicRepository
  ↓
RemoteDataSource
  ↓
backend
```

The resulting screen document is rendered according to its registered `templateType`.

## 10. Platform composition

The intended composition is:

```text
Android entry ─┐
iOS entry ────┼──→ shared App → shared Navigation → Dynamic Container
Web entry ─────┘
```

Navigation and Dynamic Container are common code.

Platform-specific implementations are introduced only for genuine platform boundaries such as native iOS hosting, browser/platform capabilities, or platform-specific external URI launching.

## 11. Explicit prohibitions

The implementation must not introduce:

- `desktopApp`;
- a second navigation framework;
- string-based business routes for Dynamic screens;
- one navigation class per business screen;
- navigation logic inside `DynamicStore`;
- API calls from the navigation module;
- template-type-based navigation rules;
- external URI as an internal destination;
- one Dynamic Container per template;
- Login/OTP/Dashboard native screen classes;
- speculative `NavigationManager`/`Coordinator`/`Router` abstractions;
- duplicate Dynamic destination models.

## 12. Frozen target flow

```text
Platform Entry
      ↓
Shared App Composition
      ↓
Navigation 3
      ↓
DynamicDestination back stack
      ↓
Dynamic Container
      ↓
Dynamic Screen Store
      ↓
LoadDynamicScreenUseCase
      ↓
DynamicRepository
      ↓
RemoteDataSource
      ↓
Backend Dynamic Screen API
      ↓
Dynamic Screen JSON
      ↓
DynamicRenderer
      ↓
registered Template
      ↓
Component / Section / Group / Element
```

Navigation-changing action:

```text
Dynamic UI interaction
      ↓
DynamicStore
      ↓
ExecuteDynamicActionUseCase
      ↓
request/action result
      ↓
DynamicDestination
      ↓
Navigation boundary
      ↓
Dynamic Container shows new current destination
```

This is the frozen navigation/container boundary for implementation.