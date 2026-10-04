# CB-Partner Dynamic UI — Implementation Plan

**Status:** PLAN FROZEN — IMPLEMENTATION AUTHORIZED  
**Documentation reconciliation:** 2026-10-05

This is the single implementation source of truth for the Dynamic feature. It must be read together with the frozen discussion and navigation records below. Older implementation wording that conflicts with these contracts is superseded.

## 1. Governing documents

Implementation must follow this order of authority:

1. `00-FEATURE-DISCUSSION.md` — all frozen Dynamic architecture decisions.
2. `03-NAVIGATION-CONTAINER-FREEZE.md` — navigation/container boundary.
3. `04-DYNAMIC-08-UNIFIED-DESTINATION-DISCUSSION.md` — destination/action/request/binding contract.
4. `05-DYNAMIC-08-IMPLEMENTATION-PLAN.md` — concrete DYNAMIC-08 implementation steps.
5. This file — overall implementation sequence and verification.
6. `02-IMPLEMENTATION-STATUS.md` — current implementation state/next task only; it never overrides a frozen contract.

`00-MODULE-IMPLEMENTATION-PLAN.md` is superseded and must not be used as an implementation mandate.

## 2. Frozen runtime architecture

```text
Platform Entry
    ↓
Shared App Composition
    ↓
Splash / Bootstrap
    ↓
startup.destination
    ↓
DynamicDestination
    ↓
existing :navigation / Navigation 3
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
actual template definition/content
    ↓
DynamicRenderer
    ↓
Component / Section / Group / Element
```

There are no business-specific Dynamic screens.

## 3. Canonical destination

```text
DynamicDestination
├── screenId
├── templateId
├── templateType
└── endpoint
```

`method` and `authentication` remain request-action metadata.

The destination never carries business/runtime values.

## 4. Destination lifecycle

The destination already contains the template instance identity, template type, and API endpoint.

```text
Destination
  ↓
Dynamic Container
  ↓
Template Registry(templateType)
  ↓
LoadDynamicScreenUseCase(endpoint)
  ↓
Dynamic API
  ↓
Complete response
  ↓
actual template definition
  ↓
render
```

There is no `EmptyTemplateInstance` stage.

The Template Registry resolves the frontend capability for `templateType`; it does not fetch or invent the backend template definition.

The API response is authoritative for the actual template content and its properties, children, events, actions, bindings, references, etc.

## 5. Bootstrap

Bootstrap is one startup flow:

```text
ApplicationContent
  ↓
Splash
  ↓
ApplicationBootstrap
  ↓
BootstrapRepository
  ↓
BootstrapRepositoryImpl
  ↓
BootstrapRemoteDataSource
  ↓
RequestHandler
  ↓
BootstrapOutput
  ↓
DynamicDestination
```

The startup destination enters the same Dynamic navigation/container flow as all other destinations.

## 6. Action architecture

Events describe when something happens. Actions describe what happens.

```text
Element
  ↓
Event
  ↓
Action
  ↓
action-specific runtime behavior
```

Supported generic action categories:

```text
request
navigate
present
dismiss
state
external_uri
sequence
```

There is **no `ExecuteDynamicActionUseCase`**. Action execution remains in Dynamic runtime/action infrastructure and action-specific handlers/capabilities.

## 7. Direct navigate

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
LoadDynamicScreenUseCase(endpoint)
  ↓
Dynamic API
  ↓
complete response
  ↓
render
```

## 8. Request action

Request-owned metadata:

```text
method
endpoint
authentication
validation
headers/query where supported
body
responseMode
```

References and bindings are resolved before request construction.

For `responseMode = destination`:

```text
request
  ↓
API
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
LoadDynamicScreenUseCase(endpoint)
  ↓
Dynamic API
  ↓
complete screen response
  ↓
render
```

For `responseMode = none`, no destination navigation occurs.

Direct navigation and request-generated navigation therefore use exactly one destination/loading flow.

## 9. Binding and references

Binding is generic backend-keyed state:

```text
input/value change
  ↓
binding key
  ↓
Dynamic state
```

Request/action execution resolves:

```text
$literal
$binding
$context
$response
```

Binding values are never navigation arguments. Multiple bindings are supported and must be assembled into the backend-defined JSON structure rather than concatenated arbitrarily.

## 10. Module ownership

```text
:feature:dynamic
    ↓
Dynamic UI / Store / Registry / Renderer / action runtime / LoadDynamicScreenUseCase
    ↓
:domain
    ↓
repository contracts / canonical models
    ↓
:data
    ↓
repository implementations / remote sources / DTOs / mappers
    ↓
:network / existing RequestHandler + Ktor infrastructure
```

`:navigation` owns application navigation mechanics only.

`:navigation` does not call Dynamic APIs or render Dynamic UI.

Dynamic Store does not own application navigation history and does not call Navigation 3 directly.

## 11. Bootstrap implementation boundary

Use the frozen Option B path:

```text
ApplicationBootstrap       :domain
        ↓
BootstrapRepository        :domain
        ↓
BootstrapRepositoryImpl    :data
        ↓
BootstrapRemoteDataSource  :data
        ↓
RequestHandler             :network
```

There must be exactly one production Bootstrap HTTP path.

## 12. Dynamic screen-loading use case

Only one Dynamic screen-loading use case exists:

```text
LoadDynamicScreenUseCase
```

Its responsibility is to load the complete Dynamic screen response from a supplied destination endpoint through the repository boundary.

It does not own:

- navigation;
- registry registration;
- action dispatch;
- rendering;
- business-specific behavior.

## 13. Implementation sequence

### Phase A — Bootstrap

1. Keep the single Bootstrap repository/data-source path.
2. Verify `startup.destination` maps to `DynamicDestination`.
3. Verify Splash hands the destination to existing navigation.
4. Verify no legacy Bootstrap destination models remain.

### Phase B — Destination/navigation

5. Verify exactly four destination fields.
6. Verify Navigation 3 adapter uses the canonical destination without a second destination model.
7. Verify one Dynamic Container.
8. Verify push/replace/pop/back behavior.
9. Verify Dynamic Store does not own navigation state.

### Phase C — Dynamic loading/rendering

10. Verify Template Registry resolves `templateType` only.
11. Verify `LoadDynamicScreenUseCase` loads `endpoint`.
12. Verify API response supplies actual template definition/content.
13. Verify renderer starts from the loaded response.
14. Verify component/section/group/element ordering and rendering.

### Phase D — Actions/binding

15. Remove any remaining `ExecuteDynamicActionUseCase` contract.
16. Verify event → action-specific runtime behavior.
17. Verify direct `navigate` produces `DynamicDestination`.
18. Verify `request` builds its body from generic references/bindings.
19. Verify `responseMode = destination` resolves `data.destination` and reuses the same loading flow.
20. Verify binding never enters navigation arguments.
21. Verify `external_uri` remains separate from internal navigation.

### Phase E — Reliability/observability

22. Complete frozen request error model.
23. Complete frozen Dynamic fallback/recovery behavior.
24. Complete frozen observability boundary.

### Phase F — Verification

25. Backend-shaped Dynamic screen fixtures.
26. Direct navigate fixture.
27. Request-to-destination fixture.
28. Multi-binding request-body fixtures.
29. Template push/replace/pop/back tests.
30. Android verification.
31. iOS/macOS-hosted verification where supported.
32. Web/Wasm verification where supported.
33. Literal plan-to-source compliance review.
34. Final Dynamic review and code freeze.

## 14. Explicit prohibitions

Do not introduce:

- `ExecuteDynamicActionUseCase`;
- business-specific screen classes;
- a second navigation framework;
- a second Dynamic Container;
- an empty template/instance abstraction;
- API calls from `:navigation`;
- business data in navigation arguments;
- `method`/`authentication` on `DynamicDestination`;
- template-type-based push/replace logic;
- a separate navigation flow for request destinations;
- screen-specific binding classes;
- speculative manager/coordinator/router layers.

## 15. Definition of Done

- [ ] Bootstrap single-path architecture verified.
- [ ] Four-field `DynamicDestination` verified.
- [ ] Existing `:navigation` reused.
- [ ] One Dynamic Container verified.
- [ ] Template Registry is lookup/capability only.
- [ ] `LoadDynamicScreenUseCase` is the only Dynamic screen-loading use case.
- [ ] Complete API response drives actual template rendering.
- [ ] Direct navigate and request-generated destinations converge.
- [ ] Generic binding/reference resolution complete.
- [ ] No `ExecuteDynamicActionUseCase` remains.
- [ ] Navigation push/replace/pop/back verified.
- [ ] Full Dynamic tests pass.
- [ ] Platform verification passes where supported.
- [ ] Literal plan-to-source review passes.
- [ ] Final Dynamic code freeze accepted.
