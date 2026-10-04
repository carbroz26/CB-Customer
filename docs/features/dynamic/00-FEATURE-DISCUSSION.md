# Dynamic UI — Feature Discussion

## Status

- DYNAMIC-01 — FROZEN
- DYNAMIC-02 — FROZEN
- DYNAMIC-03 — FROZEN
- DYNAMIC-04 — FROZEN
- DYNAMIC-05 — FROZEN
- DYNAMIC-06 — FROZEN
- DYNAMIC-07 — FROZEN
- Action / Binding / Destination refinement — FROZEN

> **Canonical document rule:** This file is the single source of truth for Dynamic UI discussion decisions. Any earlier wording, exploratory approach, or superseded navigation/action/binding proposal is no longer authoritative. New chats and implementation planning must follow only the current frozen contract in this document.

The Dynamic feature is fully backend-driven. Login, OTP, Profile, Dashboard, and similar business names are examples of backend data only; they are not frontend Dynamic concepts or static screen classes.

The fixed backend JSON contract remains authoritative. This document defines the frontend architecture and runtime boundaries around that contract; it does not redesign the backend JSON.

---

# DYNAMIC-01 — Dynamic Container + Dynamic Screen Lifecycle

## Status

**FROZEN**

Dynamic uses **one permanent Dynamic Container** after Bootstrap. The application does not create separate native/frontend screens for different backend templates.

```text
Application
   ↓
Bootstrap / existing :navigation
   ↓
Dynamic Container
   ↓
Dynamic Template flow
```

The Dynamic Container is the single host for backend-defined Dynamic templates. Dynamic navigation changes the template instance shown inside this container rather than navigating between business-specific frontend screen classes.

A destination identifies the Dynamic template instance to be loaded:

```text
DynamicDestination
├── screenId
├── templateId
├── templateType
├── endpoint
├── method
└── authentication
```

The meaning of the fields is intentionally generic:

```text
templateId
    = identity of the concrete Dynamic template instance/destination

templateType
    = type of Dynamic template that the frontend must support/host

endpoint
    = API endpoint from which the complete definition/content of that
      template instance is loaded
```

### Canonical destination → template → API flow

The destination already contains the identity, type, and API information. Therefore there is **no empty-template stage** and no second step that discovers the template identity.

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
Resolve/prepare the registered template capability
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
Actual Template Definition / Content
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
Render complete Dynamic UI
```

`templateType` is known before the screen API is called, but it does **not** contain the actual template UI data. The registry only resolves the supported frontend template capability/type. The actual template definition, properties, children, events, actions, bindings, references, etc. come from the screen API response and rendering starts from that response.

There is **no `EmptyTemplateInstance` concept**. The destination already identifies the instance through `templateId`. The API response supplies the complete definition/data for that same destination/template.

The response normally confirms the destination identity/type and supplies the remaining complete screen definition. The frontend does not reconstruct or invent the definition from destination metadata.

### Dynamic template navigation

Dynamic template navigation is internal to the single Dynamic Container. It is not a second application navigation framework.

```text
Current Template A
      ↓
Dynamic action produces Destination B
      ↓
Dynamic Container activates B
      ↓
load B using B.endpoint
      ↓
render B
```

Template-stack operations such as `replace`, `push/add`, `dismiss/back`, and `present` are Dynamic runtime navigation behaviors. `templateType` does not decide whether an operation is push or replace; navigation/action semantics decide that.

The existing `:navigation` module remains the application's navigation module. Dynamic must use it for application-level navigation into/out of the Dynamic Container and must not create another application navigation system.

`screenId` and `templateId` are backend/runtime identities. Neither implies a static frontend screen class.

### Frozen DYNAMIC-01 decisions

1. One permanent Dynamic Container hosts Dynamic UI.
2. There are no LoginScreen/OtpScreen/ProfileScreen/etc. frontend classes for backend-defined screens.
3. Dynamic destinations are backend-driven and generic.
4. `templateId` identifies the concrete Dynamic template instance/destination.
5. `templateType` identifies the supported Dynamic template type/capability.
6. `endpoint` identifies where the complete Dynamic screen definition/content is loaded.
7. Destination metadata is known before the screen API is called.
8. There is no empty-template/empty-instance stage.
9. The Template Registry resolves the supported template capability; it does not load API data or render the complete backend definition.
10. `LoadDynamicScreenUseCase` is the only Dynamic screen-loading use case.
11. `LoadDynamicScreenUseCase` loads the complete Dynamic response using the destination endpoint.
12. The API response is authoritative for the actual template definition/content.
13. Rendering of the actual template/component/section/group/element tree begins after the Dynamic response is available.
14. Dynamic template navigation occurs inside the one Dynamic Container.
15. `replace`, `push/add`, `dismiss/back`, and `present` are Dynamic template-stack behaviors and are separate from `templateType`.
16. The existing `:navigation` module remains the application navigation module; Dynamic does not introduce a second application navigation framework.
17. No business-specific data such as phone, OTP, name, customer ID, or other arbitrary values is passed as navigation data.
18. Destination metadata is not used to reconstruct or invent backend UI content.

---

# DYNAMIC-02 — Screen → Template → Component → Section → Group → Element

## Status

**FROZEN**

The backend Dynamic response is a generic hierarchical UI definition. No node is tied to a business screen name.

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

Screen owns exactly one Template. Template can contain multiple Components. Component can contain Elements and/or Sections. Section can contain Elements and/or Groups. Group contains Elements only. Element is terminal.

Backend ordering is significant and must be preserved.

Frozen structural decisions:

1. Screen has exactly one Template.
2. Template has one or more Components.
3. Component may contain Elements, Sections, or both.
4. Section may contain Elements, Groups, or both.
5. Group contains Elements only.
6. Element is terminal.
7. Structural hierarchy and rendering behavior remain separate.
8. Template/Component/Section/Group/Element are generic Dynamic concepts, never business-screen classes.
9. Theme, Header, Footer, and other screen-level configuration remain Screen-level concerns when provided by the fixed backend contract.
10. Backend ordering is preserved.

---

# DYNAMIC-03 — Registration System

## Status

**FROZEN**

Registration answers only:

```text
Backend type
    ↓
Is this type supported by the frontend?
    ↓
registered / not registered
```

There is one public `DynamicRegistry` with category-specific registries:

```text
DynamicRegistry
├── TemplateRegistry
├── ComponentRegistry
├── SectionRegistry
├── GroupRegistry
└── ElementRegistry
```

Registration is explicit. No reflection, package scanning, automatic discovery, or generic plugin framework is introduced.

The Template Registry is specifically responsible for resolving the supported frontend capability for `templateType`. It does not own API loading, navigation, actions, bindings, state, or rendered UI state.

Frozen decisions:

1. One central public DynamicRegistry.
2. Category-specific registries remain separate internally.
3. Registration and lookup are explicit and category-specific.
4. Duplicate registration is an error.
5. Unknown types produce a controlled unsupported/not-registered result.
6. Registry owns registration and lookup only.
7. Registry does not own API calls, navigation, business logic, action execution, state, or rendered UI state.
8. Registry resolution happens before actual template rendering but does not replace the screen API response as the source of template content.

---

# DYNAMIC-04 — Layout, Visual Capabilities & Rendering Direction

## Status

**FROZEN**

Registered Dynamic definitions own their Compose rendering behavior. Parent/child layout responsibility is explicit and Compose-native.

Parents own their container/layout and child placement. Children own their content and legal size/alignment within available constraints. Elements are terminal.

Supported common layout concepts include:

```text
WRAP
FILL
FIXED
FRACTION
min/max constraints
padding
alignment
arrangement
orientation
spacing
aspectRatio
```

Common visual capabilities include applicable:

```text
background
shape
border
shadow / elevation
alpha / opacity
```

Responsive behavior is constraint-based and Compose-native. Dynamic does not create a second responsive layout engine.

Common capabilities are reused without creating a property-class explosion. Definitions opt into only capabilities that make semantic sense for their type. Actions remain separate from visual capabilities. Theme is a Screen-level concern.

Frozen decisions:

1. Each registered definition owns its applicable Compose rendering.
2. Parents own layout/container behavior and child placement.
3. Children own content and legal self-layout behavior.
4. Elements are terminal.
5. Size and visual capabilities remain generic and capability-based.
6. Responsive behavior is constraint-based.
7. Actions are not visual properties.
8. Theme is Screen-level.
9. Fixed backend JSON remains authoritative.

---

# DYNAMIC-05 — Definition, Registry & Parent → Child Rendering

## Status

**FROZEN**

The rendering architecture remains intentionally small:

```text
DynamicRegistry
      ↓
resolve registered type
      ↓
Dynamic Definition
      ↓
Compose rendering
      ↓
DynamicRenderer for legal children
```

A registered Definition represents one frontend-supported Dynamic type. A central `DynamicRenderer` recursively delegates child rendering while preserving backend order.

The renderer/definitions do not own API, navigation, business logic, repositories, action execution, or global application state.

Frozen decisions:

1. DynamicRegistry is the single registration entry point.
2. Category registries remain separate.
3. Each registered Definition represents one supported Dynamic type.
4. Definitions own their applicable Compose rendering.
5. DynamicRenderer handles recursive child delegation.
6. Parents do not directly instantiate concrete child Definitions.
7. Child data is passed through the rendering flow.
8. Parent layout controls child placement.
9. Child layout/content controls its own behavior within parent constraints.
10. Unknown types and illegal structures use controlled runtime handling.
11. Rendering remains separate from actions, state, API, and navigation.

---

# DYNAMIC-06 — Dynamic Actions, Events, Binding & Interaction

## Status

**FROZEN**

DYNAMIC-06 is the canonical contract for backend-driven events/actions and interaction. Dynamic remains completely generic and does not contain Login/OTP/Profile-specific concepts.

## 06.1 — Event vs Action

```text
Event = WHEN something happens
Action = WHAT should happen
```

Examples of events:

```text
onClick
onLongClick
onValueChange
onFocus
onSubmit
```

A button/image/text/other interactive element may expose a backend-defined event such as `onClick`. The event does not itself know how to perform navigation or a request; it resolves to an Action.

Supported action categories remain generic and independently handled:

```text
request
navigate
present
 dismiss
state
external_uri
sequence
```

Each action category owns its own behavior. Rendering Definitions do not execute actions.

There is **no `ExecuteDynamicActionUseCase`**. Dynamic does not create a second generic use-case layer merely to dispatch actions. `LoadDynamicScreenUseCase` remains the Dynamic screen-loading use case. Action execution belongs to the Dynamic runtime/action infrastructure and delegates to the appropriate action handler/behavior.

## 06.2 — Generic Action Flow

The canonical flow is:

```text
Dynamic Element / Definition
        ↓
Event (for example onClick)
        ↓
Action
        ↓
Action-specific runtime behavior
```

The same action model is used regardless of what backend screen/template happens to contain the event.

An action must not contain business-specific assumptions such as `login`, `otp`, `profile`, `sendOtp`, or `verifyOtp` in the Dynamic architecture.

## 06.3 — Binding

Binding is a generic mechanism for connecting backend-defined input/value keys to the current Dynamic interaction state.

The backend can define a binding key for an input/value source. The frontend does not assign semantic meaning to the key.

Conceptually:

```text
Dynamic State
└── values
      ├── <backend-defined key> → current value
      ├── <backend-defined key> → current value
      └── ...
```

When an input changes:

```text
Input value changes
      ↓
Binding key
      ↓
Dynamic State updated
```

When an action needs bound data:

```text
Action request body
      ↓
reference/binding resolution
      ↓
resolve binding key against current Dynamic state
      ↓
value
```

Bindings are not navigation arguments. Dynamic must not hardcode or understand whether a key represents a phone number, OTP, name, address, ID, or any other business field.

Multiple bindings are supported generically. A request body may contain multiple resolved binding values and must be constructed according to the backend request/body contract. The runtime must not concatenate unrelated values with comma separators merely because multiple bindings exist; the resulting JSON must preserve the backend-defined object/array/value structure.

A binding may be used by multiple consumers where the backend contract permits it. Binding resolution is centralized so the same key resolves consistently throughout the Dynamic runtime.

Frozen binding decisions:

1. Binding keys are backend-defined and semantically opaque to Dynamic.
2. Dynamic state stores current values by binding key/path according to the fixed contract.
3. Input/value changes update the corresponding binding state.
4. Actions resolve bindings at execution time from current state.
5. Binding values are not passed through navigation destinations.
6. Multiple bindings are supported generically.
7. Request bodies are built from resolved values according to the backend JSON contract, not by screen-specific code.
8. Dynamic must not contain phone/OTP/name/profile-specific binding classes or logic.
9. Missing/invalid bindings are structured Dynamic errors; the runtime does not invent values.

## 06.4 — References

The frozen generic reference vocabulary is:

```text
$literal
$binding
$context
$response
```

Reference resolution is centralized, deterministic, recursive, and type-safe.

```text
$literal  → literal value
$binding  → current Dynamic binding value
$context  → current generic runtime/application context value
$response → relevant request response value
```

References read values. Actions perform effects/mutations.

No reference is interpreted as Login/OTP/Profile-specific data. If the backend needs a value from an earlier response or runtime context, it uses the generic reference contract rather than navigation arguments containing business fields.

## 06.5 — Request Action

`request` is a generic API action. It carries the fixed backend request information, such as method, endpoint, authentication, validation, headers/query/body where supported, and response mode.

Before execution, references—including bindings—are resolved against the appropriate runtime state/context/response.

The Dynamic runtime coordinates the action; actual HTTP, authentication, serialization, and network infrastructure remain in the existing data/core infrastructure.

A successful request can produce:

```text
responseMode = destination
    ↓
DynamicDestination
```

or:

```text
responseMode = none
    ↓
request completes without destination navigation
```

## 06.6 — Request → Destination Unification

A request action and a direct navigate action converge on the same generic destination model.

```text
request
  ↓
API
  ↓
response
  ↓
DynamicDestination
```

and:

```text
navigate
  ↓
DynamicDestination
```

Both then use the same Dynamic template activation/loading flow.

## 06.7 — DynamicDestination

The canonical destination model is:

```text
DynamicDestination
├── screenId
├── templateId
├── templateType
├── endpoint
├── method
└── authentication
```

A backend `nextScreen` object maps to this internal model.

The destination is a navigation/fetch instruction, not a rendered screen and not a business-data transport object.

The destination contains no:

```text
phone
otp
name
customerId
arbitrary business payload
```

The destination's `endpoint` is passed through the Dynamic destination/template-loading flow and is the input used by `LoadDynamicScreenUseCase` to load the complete Dynamic screen response.

The destination's `templateType` is used to resolve the supported template capability through the Template Registry. The destination's `templateId` identifies the concrete instance. The API response supplies the complete definition/content for that same instance.

## 06.8 — Canonical Destination Loading Flow

```text
DynamicDestination
        │
        ├── templateId ─────────────┐
        ├── templateType            │
        └── endpoint                │
                │                   │
                ↓                   │
        Dynamic Container           │
                │                   │
                ↓                   │
        Template Registry            │
                │                   │
                │ templateType      │
                ↓                   │
        resolve supported type      │
                │                   │
                └──────────┐        │
                           ↓        │
                 LoadDynamicScreenUseCase
                           │
                           │ endpoint
                           ↓
                      Dynamic API
                           ↓
                 Complete screen response
                           ↓
                 actual template definition
                           ↓
                 Template/Component/
                 Section/Group/Element rendering
```

There is no `EmptyTemplateInstance` step. There is no second API-discovery step. The destination already identifies the template instance and endpoint.

## 06.9 — Request Error Model

Request results are explicitly success or structured error. Transport/runtime failures are normalized. Backend code/message are preserved where available. HTTP status is metadata rather than the complete Dynamic error model.

Errors do not automatically navigate. A failed request leaves the current valid Dynamic destination active unless an explicit action changes navigation.

## DYNAMIC-06 — Frozen Decisions

1. Event means WHEN; Action means WHAT.
2. Actions are generic and backend-driven.
3. Each action category has its own handler/behavior.
4. There is no `ExecuteDynamicActionUseCase`.
5. `LoadDynamicScreenUseCase` is the Dynamic screen-loading use case.
6. Rendering Definitions do not execute actions.
7. Binding is generic and backend-defined.
8. Dynamic does not know what a binding key semantically represents.
9. Input changes update binding state.
10. Actions resolve binding values at execution time.
11. Multiple bindings are supported generically.
12. Request JSON is built from resolved values according to the backend contract.
13. Bindings are not navigation arguments.
14. References are `$literal`, `$binding`, `$context`, and `$response`.
15. Reference resolution is centralized and deterministic.
16. Request and navigate can converge on the same DynamicDestination.
17. DynamicDestination contains only destination/loading metadata in the frozen base model.
18. DynamicDestination never carries business-specific values.
19. `templateId` identifies the Dynamic template instance.
20. `templateType` identifies the supported template type/capability.
21. `endpoint` identifies the API used to load the complete template definition/content.
22. The Template Registry resolves capability/type; it does not load the screen API or render backend content by itself.
23. `LoadDynamicScreenUseCase` loads the complete response from the destination endpoint.
24. The API response is authoritative for the actual template definition/content.
25. There is no empty-template-instance stage.
26. The existing `:navigation` module remains the application navigation module.
27. Dynamic template navigation occurs inside the single Dynamic Container.
28. No business-specific data is passed through navigation.
29. Request failures use the structured Dynamic error model.
30. Action, binding, reference, request, destination, loading, navigation, and rendering responsibilities remain separate.

---

# DYNAMIC-07 — Runtime Validation, Compatibility, Safety & Observability

## Status

**FROZEN**

DYNAMIC-07 remains the runtime safety/diagnostic boundary.

```text
PARSE
  ↓
RESOLVE
  ↓
LOAD / RENDER
  ↓
DIAGNOSE
  ↓
FALLBACK
  ↓
RECOVER SAFELY
```

The frontend does not create a second SDUI validation/migration engine. It parses the fixed contract, resolves registered definitions, renders, handles unsupported/runtime failures, uses localized fallback, and recovers deterministically.

Unknown Definition and Unsupported Capability remain distinct. No guessing or silent substitution is allowed.

Fallback is localized:

```text
Unknown Template   → screen-level fallback
Unknown Component  → component-level fallback
Unknown Section    → section-level fallback
Unknown Group      → group-level fallback
Unknown Element    → element-level fallback
```

Dynamic failures must not crash the application. Action/request failures remain isolated from rendering. Navigation/loading failure preserves the current valid destination when safe. No implicit infinite retry is introduced.

Observability uses the existing project logger. Diagnostics are structured and correlated using safe identifiers such as `flowId`, `screenId`, `templateId`, `componentId`, `sectionId`, `groupId`, `elementId`, `actionId`, `requestId`, and `traceId` where available. Sensitive data, credentials, OTPs, tokens, and full payloads are not logged by default or merely for debugging convenience.

Debug filtering/verbosity affects diagnostic visibility only. Production uses the same runtime and minimal actionable safe diagnostics. Observability never changes rendering, state, actions, requests, navigation, resolution, fallback, or recovery.

Frozen DYNAMIC-07 decisions:

1. Backend structure remains authoritative.
2. No duplicate frontend SDUI validation engine.
3. Unknown definitions and unsupported capabilities remain distinct.
4. No silent substitution or automatic repair.
5. Runtime errors are structured and diagnosable.
6. Existing logging infrastructure is reused.
7. Fallback is localized.
8. Dynamic failures are contained and recovery is deterministic.
9. Request failures follow DYNAMIC-06 error semantics.
10. Recovery cannot mutate registry definitions or application architecture.
11. Observability is observational only.
12. Sensitive data protection applies in all environments.
13. Debug mode changes visibility, not runtime behavior.
14. Production observability remains minimal and actionable.

---

# Canonical Dynamic Architecture — Current Frozen Model

The complete Dynamic flow is now:

```text
                APPLICATION START
                       │
                       ↓
                    Splash
                       │
                       ↓
                 Bootstrap API
                       │
                       ↓
              DynamicDestination
             ┌─────────┼─────────┐
             │         │         │
        templateId templateType endpoint
             │         │         │
             │         ↓         │
             │   TemplateRegistry│
             │         │         │
             └─────────┼─────────┘
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
             Complete Dynamic Response
                       │
                       ↓
               Actual UI Definition
                       │
                       ↓
       Template → Component → Section/Group
                       ↓
                    Elements

USER INTERACTION
       │
       ↓
     Event
       │
       ↓
     Action
       │
       ├──────── request ───────→ API
       │                            │
       │                            ↓
       │                      destination?
       │                            │
       └──────── navigate ─────────┘
                                    ↓
                           DynamicDestination
                                    ↓
                           same flow as above
```

## Non-negotiable boundaries

```text
: navigation
    = existing application navigation only

Dynamic Container
    = single host for Dynamic templates

Template Registry
    = resolve supported template type/capability

LoadDynamicScreenUseCase
    = load complete Dynamic screen response

Dynamic Renderer / Definitions
    = render response data

Action Runtime
    = interpret and execute backend-defined actions

Binding / Reference Resolver
    = resolve generic runtime values

Dynamic State
    = destination-scoped interaction/binding state
```

No business-specific screen classes, business-specific Dynamic actions, business-specific bindings, or business-specific navigation arguments are introduced.

---

# Discussion Workflow Rule

For each Dynamic point:

```text
Discussion
   ↓
Freeze
   ↓
Update this document
   ↓
Next Dynamic point
```

The document must be updated immediately after a decision is frozen. Superseded proposals must be removed or explicitly replaced so that a future chat cannot treat an old proposal as an active architecture.

After the current Dynamic discussion contract is complete:

```text
Implementation Plan
   ↓
Freeze Plan
   ↓
Implementation
   ↓
Testing
   ↓
Final Reconciliation / Freeze
```

No implementation code is introduced merely because a discussion decision has been frozen.