# Dynamic UI — Navigation & Dynamic Container Freeze

## Status

**DISCUSSION FROZEN — IMPLEMENTATION AUTHORIZED**

This record freezes the final navigation/container decisions after repository and Navigation 3 review. It is the governing navigation/container boundary for Dynamic implementation.

## 1. Supported platforms

```text
Android
 iOS
 Web (Wasm)
```

There is **no `desktopApp`** and no Desktop-specific Dynamic or navigation implementation.

The Dynamic Container, Dynamic runtime, and navigation host are shared Compose Multiplatform code. Platform entry points remain thin and exist only at genuine platform boundaries.

## 2. Navigation technology

Use the already-selected Navigation 3 dependencies in the repository. Do not introduce another navigation framework and do not change the pinned versions as part of this implementation.

Navigation 3 is a good fit because the application owns the observable back stack and `NavDisplay` resolves keys to content. Its multiplatform implementation supports Android, iOS, and Web, with serializable keys for persistent navigation state.

## 3. Canonical Dynamic destination

`DynamicDestination` remains the canonical **domain destination payload** and contains the complete backend-defined information:

```text
screenId
templateId
templateType
endpoint
method
authentication
```

Meaning:

```text
templateType
    → which registered Template definition renders the destination

templateId
    → concrete Dynamic destination/template instance identity

endpoint + method + authentication
    → how the Dynamic screen document is loaded
```

The complete `DynamicDestination` must be what is stored in navigation history. It must never be reduced to a route string, template type, or template ID alone.

## 4. Clean Architecture boundary for Navigation 3 keys

`domain` must remain independent of the Navigation 3 framework.

Therefore the domain `DynamicDestination` does **not** depend directly on `NavKey`.

`:navigation` provides the minimal adapter/key required by Navigation 3, containing the complete canonical `DynamicDestination` payload.

Conceptually:

```text
Domain
└── DynamicDestination
        │
        │ complete payload
        ▼
:navigation
└── DynamicDestinationNavKey : NavKey
        │
        ▼
Navigation 3 back stack
```

This is not a second destination model. `DynamicDestinationNavKey` is only the framework adapter required to keep the domain independent of Navigation 3.

The adapter must not duplicate destination fields outside the wrapped canonical `DynamicDestination`.

## 5. One permanent Dynamic Container

There is exactly **one permanent shared Dynamic Container**.

Mental model only:

```text
DynamicContainer ≈ stable Activity-like host
Template         ≈ replaceable Fragment-like content
```

This is a conceptual analogy. The implementation does not create Android Activities/Fragments for Dynamic screens.

The container remains stable while the current backend-defined destination/template changes.

```text
Navigation
   ↓
current DynamicDestination
   ↓
Dynamic Container
   ↓
current template definition
   ↓
Dynamic screen content
```

There are no native LoginScreen, OtpScreen, DashboardScreen, or equivalent business-screen classes.

## 6. Navigation ownership

`:navigation` owns navigation history and Navigation 3 mechanics.

Minimum operations:

```text
setRoot(destination)
push(destination)
replace(destination)
pop()
```

These operations modify navigation state only. They do not call the backend and do not render SDUI.

Do not create a `NavigationManager`, `NavigationCoordinator`, `NavigationRouter`, or framework wrapper hierarchy without a concrete single responsibility.

## 7. Navigation host

Use Navigation 3 `NavDisplay` and its entry mechanism.

A `DynamicDestinationNavKey` resolves to the one Dynamic Container. The container receives the wrapped canonical `DynamicDestination` and loads/renders it through the Dynamic feature boundary.

There are no business-specific navigation entries.

## 8. Back and refresh

System/application back removes the current complete Dynamic destination and exposes the previous destination.

Refresh reloads the current complete destination and does not add another navigation entry.

The Dynamic Store does not mutate the navigation stack directly.

## 9. Dynamic action vs navigation capability

Backend actions and navigation mechanics remain separate.

```text
navigate
  ↓
Dynamic navigation effect/result
  ↓
:navigation
```

```text
external_uri
  ↓
external URI capability/effect
  ↓
platform boundary
```

`external_uri` is never represented as an internal navigation destination.

Future backend actions such as `dismiss`, `present`, `deeplink`, or additional capabilities can be added at their correct boundary when their contract is required. They are not speculatively implemented now.

## 10. Dynamic Store boundary

`DynamicStore` does not own navigation state and does not import or call Navigation 3 APIs.

It can emit a navigation effect/result when Dynamic action execution requires navigation:

```text
DynamicStore
  ↓
use case / action execution
  ↓
navigation effect
  ↓
:navigation boundary
```

The Store owns Dynamic screen/runtime state, not application navigation history.

## 11. Dynamic screen loading

Navigation does not load the Dynamic screen API.

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

The returned screen document is rendered according to the destination's registered `templateType`.

## 12. Platform composition

```text
Android entry ─┐
iOS entry ────┼──→ shared App → shared Navigation → Dynamic Container
Web entry ─────┘
```

Platform-specific code is limited to genuine platform boundaries. No platform duplicates the Dynamic Container or navigation implementation.

## 13. Explicit prohibitions

The implementation must not introduce:

- `desktopApp`;
- a second navigation framework;
- domain dependency on Navigation 3;
- duplicate destination models;
- string-based Dynamic business routes;
- one navigation class per business screen;
- navigation state inside `DynamicStore`;
- API calls from `:navigation`;
- template-type-based navigation rules;
- external URI as an internal destination;
- one Dynamic Container per template;
- Login/OTP/Dashboard native screen classes;
- speculative manager/coordinator/router abstractions.

## 14. Frozen target flow

```text
Platform Entry
      ↓
Shared App Composition
      ↓
:navigation
      ↓
Navigation 3 back stack
      ↓
DynamicDestinationNavKey
      ↓
canonical DynamicDestination
      ↓
one Dynamic Container
      ↓
Dynamic Store / LoadDynamicScreenUseCase
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
DynamicDestination / navigation effect
      ↓
:navigation
      ↓
Dynamic Container shows the new current destination
```

This is the frozen navigation/container boundary for implementation.