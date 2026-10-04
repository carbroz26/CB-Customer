# Dynamic UI — Feature Discussion

## Status

**FROZEN — CANONICAL DISCUSSION SOURCE OF TRUTH**  
**Reconciled:** 2026-10-05

This document is the single source of truth for frozen Dynamic architecture decisions. Any older wording, exploratory approach, superseded action/navigation proposal, or previous implementation idea that conflicts with this document is not authoritative.

Implementation must also follow the specialized frozen records:

```text
03-NAVIGATION-CONTAINER-FREEZE.md
04-DYNAMIC-08-UNIFIED-DESTINATION-DISCUSSION.md
05-DYNAMIC-08-IMPLEMENTATION-PLAN.md
01-IMPLEMENTATION-PLAN.md
02-IMPLEMENTATION-STATUS.md (status only)
```

`00-MODULE-IMPLEMENTATION-PLAN.md` is superseded.

The Dynamic feature is fully backend-driven. Login, OTP, Profile, Dashboard and similar business names are examples of backend data only. They are never frontend Dynamic architecture concepts or native screen classes.

---

# DYNAMIC-01 — Dynamic Container + Screen Lifecycle

**Status: FROZEN**

There is one permanent Dynamic Container. Backend-defined templates are replaceable content inside that container. Dynamic does not create business-specific native screens.

```text
Application
   ↓
existing :navigation
   ↓
one Dynamic Container
   ↓
current Dynamic destination/template
```

## Canonical destination

```text
DynamicDestination
├── screenId
├── templateId
├── templateType
└── endpoint
```

Meaning:

```text
templateId
    = concrete Dynamic template instance/destination identity

templateType
    = supported Dynamic template type/capability

endpoint
    = API endpoint used to load the complete Dynamic screen definition
```

`method` and `authentication` are request-action metadata and are not destination fields.

The destination never contains phone, OTP, name, ID, arbitrary business payload, or other runtime values.

## Destination → Template → API

The destination already contains the identity, type and endpoint. There is no empty template or empty instance stage.

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
Complete Dynamic Screen Response
      ↓
actual template definition/content
      ↓
DynamicRenderer
      ↓
Component / Section / Group / Element
```

The Template Registry resolves the frontend-supported template capability only. It does not call the API and does not contain the actual backend template definition.

The API response is authoritative for the actual template data, including properties, padding, title, components, sections, groups, elements, events, actions, bindings and references. Rendering starts only after the response is available.

`templateId` identifies the instance; the API response supplies the complete definition for that same instance.

## Dynamic template navigation

Template navigation occurs inside the one Dynamic Container. It is not a second application navigation framework.

```text
Template A
   ↓
navigation/action produces Destination B
   ↓
:navigation
   ↓
Dynamic Container
   ↓
load B
   ↓
render B
```

`replace`, `push/add`, `pop/dismiss/back` and other navigation semantics are independent of `templateType`.

### Frozen DYNAMIC-01 rules

1. One permanent Dynamic Container.
2. No business-specific native Dynamic screens.
3. One generic `DynamicDestination`.
4. `templateId` identifies the concrete template instance.
5. `templateType` identifies the supported template capability.
6. `endpoint` loads the complete screen definition.
7. Destination metadata is known before the screen API call.
8. No empty template/instance abstraction.
9. Registry does not load or invent template content.
10. `LoadDynamicScreenUseCase` is the only Dynamic screen-loading use case.
11. API response is authoritative for actual template content.
12. Actual rendering begins from the loaded response.
13. Dynamic template navigation remains inside the single container.
14. Existing `:navigation` remains the application navigation module.
15. No business/runtime values are passed through navigation.

---

# DYNAMIC-02 — Screen → Template → Component → Section → Group → Element

**Status: FROZEN**

The backend response defines a generic hierarchical UI tree:

```text
Screen
└── Template
    └── Component(s)
        ├── Element(s)
        └── Section(s)
            ├── Element(s)
            └── Group(s)
                └── Element(s)
```

Screen owns one Template. Template contains Components. Components may contain Elements and/or Sections. Sections may contain Elements and/or Groups. Groups contain Elements. Elements are terminal.

Backend ordering is significant and must be preserved.

No node is tied to a business screen name.

---

# DYNAMIC-03 — Registration System

**Status: FROZEN**

Registration answers only whether a backend type is supported and resolves the corresponding frontend capability.

```text
DynamicRegistry
├── TemplateRegistry
├── ComponentRegistry
├── SectionRegistry
├── GroupRegistry
└── ElementRegistry
```

Registration is explicit. No reflection, package scanning, automatic discovery, or generic plugin framework.

The Template Registry resolves `templateType`. It does not own API loading, navigation, action execution, binding state, or rendered UI state.

---

# DYNAMIC-04 — Layout, Visual Capabilities & Rendering Direction

**Status: FROZEN**

Registered Dynamic definitions own applicable Compose rendering. Parents own layout/container and child placement. Children own their content and legal self-layout within constraints. Elements are terminal.

Generic capabilities include applicable:

```text
WRAP / FILL / FIXED / FRACTION
min/max constraints
padding / alignment / arrangement
orientation / spacing / aspectRatio
background / shape / border / shadow / alpha
```

Responsive behavior is constraint-based and Compose-native. Actions are separate from visual capabilities. Theme is a screen-level concern.

---

# DYNAMIC-05 — Definition, Registry & Parent → Child Rendering

**Status: FROZEN**

Rendering remains:

```text
DynamicRegistry
      ↓
resolve registered capability
      ↓
Dynamic Definition
      ↓
Compose rendering
      ↓
DynamicRenderer
      ↓
legal child rendering in backend order
```

Definitions do not own API, navigation, repositories, action execution, or global application state.

Parents do not directly instantiate concrete child definitions. Rendering remains separate from actions, state, API and navigation.

---

# DYNAMIC-06 — Events, Actions, Binding & References

**Status: FROZEN**

Dynamic is completely generic.

## Event vs Action

```text
Event = when something happens
Action = what should happen
```

Examples:

```text
onClick
onLongClick
onValueChange
onFocus
onSubmit
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

Rendering definitions do not execute actions.

There is **no `ExecuteDynamicActionUseCase`**. Action execution belongs to the Dynamic runtime/action infrastructure and action-specific handlers/capabilities.

## Generic action flow

```text
Dynamic Element / Definition
      ↓
Event
      ↓
Action
      ↓
action-specific runtime behavior
```

No action contains business-specific assumptions.

## Binding

Binding connects backend-defined keys to current Dynamic runtime state.

```text
input/value changes
      ↓
binding key
      ↓
Dynamic state
```

When an action/request needs a bound value:

```text
$binding(key)
      ↓
current Dynamic value
      ↓
backend-defined request JSON/value
```

Binding keys are semantically opaque. Dynamic does not know whether a key represents phone, OTP, name, address, ID, or any other business field.

Multiple bindings are supported. Request bodies preserve the backend-defined JSON object/array/value structure; values are not arbitrarily comma-concatenated.

Bindings are never navigation arguments.

## References

```text
$literal
$binding
$context
$response
```

References read values. Actions perform effects/mutations.

---

# DYNAMIC-06.5 — Request Action

**Status: FROZEN**

`request` is a generic API action. Request-owned metadata may include:

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

Transport remains in existing data/network infrastructure.

### Request with no destination

```text
request
  ↓
API
  ↓
responseMode = none
  ↓
request completes
```

### Request producing destination

```text
request
  ↓
API
  ↓
responseMode = destination
  ↓
data.destination
  ↓
DynamicDestination
```

The resulting destination follows the exact same navigation/container/template-loading flow as direct navigation.

---

# DYNAMIC-06.6 — Destination Unification

**Status: FROZEN**

Bootstrap, direct navigation and request-generated navigation all use one destination model.

```text
Bootstrap startup.destination ──────┐
Direct navigate action.destination ──┼──→ DynamicDestination
Request response.data.destination ──┘
```

There is no `DynamicNextScreen` model and no separate destination model for Bootstrap, requests or actions.

---

# DYNAMIC-06.7 — Navigation / Container Contract

**Status: FROZEN**

The existing `:navigation` module is the application navigation module. Dynamic does not create another navigation framework.

`:navigation` owns navigation history/mechanics. Dynamic owns the Dynamic Container, Dynamic runtime state, registry, rendering and action capabilities.

The Dynamic Store does not own Navigation 3 state and does not directly manipulate the application navigation stack.

Application-level flow:

```text
:navigation
    ↓
DynamicDestination
    ↓
one Dynamic Container
```

Dynamic destination loading:

```text
DynamicDestination
    ↓
Template Registry(templateType)
    ↓
LoadDynamicScreenUseCase(endpoint)
    ↓
Dynamic API
    ↓
complete Dynamic response
    ↓
render
```

No empty template instance is created.

### Navigation semantics

```text
push/add
replace
pop/back/dismiss
```

`templateType` never decides push/replace behavior.

`external_uri` is an external capability/effect, not an internal destination.

Future capabilities such as `present`, `dismiss`, `deeplink`, or other action types remain separate capabilities and are not folded into `DynamicDestination`.

---

# DYNAMIC-07 — Parse, Resolve, Fallback, Recovery & Observability

**Status: FROZEN**

The previously frozen DYNAMIC-07 contract remains authoritative. It covers:

```text
parse
resolve
unknown/unsupported definitions
fallback
recovery
structured diagnostics
sensitive-data protection
production observability
```

No DYNAMIC-07 behavior may introduce a second navigation, action, state, or validation architecture.

---

# Final Frozen Runtime Model

```text
                    BACKEND
                       │
                       ↓
                DynamicDestination
                       │
          ┌────────────┼─────────────┐
          │            │             │
     templateId  templateType    endpoint
          │            │             │
          │            ↓             │
          │     Template Registry    │
          │            │             │
          └────────────┼─────────────┘
                       ↓
                Dynamic Container
                       │
                       ↓
             LoadDynamicScreenUseCase
                       │
                       ↓
                  Dynamic API
                       │
                       ↓
            Complete Screen Response
                       │
                       ↓
                 DynamicRenderer
                       │
                       ↓
        Template / Component / Section /
             Group / Element
```

Action path:

```text
Element
  ↓
Event
  ↓
Action
  ↓
┌───────────────┬───────────────────┐
│ navigate      │ request           │
│               │                   │
│ destination   │ API               │
│               │ ↓                 │
│               │ data.destination  │
└───────┬───────┴─────────┬─────────┘
        │                 │
        └────────┬────────┘
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
           Dynamic API
                 ↓
        Complete Response
                 ↓
              Render
```

## Absolute architectural rules

1. Dynamic is completely generic and backend-driven.
2. One Dynamic Container hosts all Dynamic templates.
3. Existing `:navigation` is reused; no second navigation framework.
4. `DynamicDestination` has exactly four fields: `screenId`, `templateId`, `templateType`, `endpoint`.
5. `templateId` identifies the template instance.
6. `templateType` selects the supported template capability.
7. `endpoint` is loaded by `LoadDynamicScreenUseCase`.
8. API response supplies the actual complete template definition/content.
9. No empty template/instance stage exists.
10. `LoadDynamicScreenUseCase` is the only Dynamic screen-loading use case.
11. `ExecuteDynamicActionUseCase` does not exist.
12. Actions are handled by Dynamic action runtime/capabilities.
13. Direct navigate and request-generated destination use the same destination/loading flow.
14. Request method/authentication remain request metadata.
15. Bindings are generic runtime state and never navigation arguments.
16. `$literal`, `$binding`, `$context`, `$response` are generic references.
17. No business-specific screen/action/binding architecture.
18. `:navigation` never calls Dynamic APIs.
19. Dynamic Store never owns application navigation history.
20. `templateType` never decides push versus replace.
21. External URI is not an internal destination.
22. Backend response is authoritative for actual Dynamic UI content.
23. No speculative manager/coordinator/router/use-case layer is introduced without a separately frozen decision.
