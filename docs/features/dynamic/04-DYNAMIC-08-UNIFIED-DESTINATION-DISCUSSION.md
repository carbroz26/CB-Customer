# DYNAMIC-08 — Unified Destination, Action, Request & Template Loading Contract

## Status

**FROZEN — AUTHORITATIVE**

This document is the canonical DYNAMIC-08 contract. It supersedes older destination/action/navigation wording.

## 08.1 — One Destination Model

```text
DynamicDestination
├── screenId
├── templateId
├── templateType
└── endpoint
```

Meaning:

```text
screenId
    → backend destination identity

templateId
    → concrete Dynamic template instance/back-stack identity

templateType
    → supported template type/capability used by TemplateRegistry

endpoint
    → API used by LoadDynamicScreenUseCase to load the complete screen definition
```

`method` and `authentication` are request-action metadata, not destination fields.

The destination never carries phone, OTP, name, customer ID, arbitrary business data, or other runtime values.

## 08.2 — Bootstrap Destination

Bootstrap uses the same destination:

```text
bootstrap
  ↓
startup.destination
  ↓
DynamicDestination
```

Example:

```json
{
  "startup": {
    "authenticated": false,
    "destination": {
      "screenId": "partner_login",
      "templateId": "tpl_P101L",
      "templateType": "form_template",
      "endpoint": "/api/v1/partner/screen/auth_login"
    }
  }
}
```

## 08.3 — Destination → Template → API Lifecycle

The destination already contains `templateId`, `templateType`, and `endpoint`. There is no empty template or empty instance stage.

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
      ├── properties
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

The Template Registry only resolves the supported frontend capability for `templateType`. It does not call the API and does not contain the actual backend template definition.

The API response is authoritative for the complete template definition. The response may repeat `templateId` and `templateType` and supplies the remaining data required for rendering.

There is no `EmptyTemplateInstance` concept.

## 08.4 — API Response Destination

The common response envelope remains:

```text
status
code
message
data
traceId
```

When an API result produces navigation, the destination is inside `data.destination`.

```json
{
  "data": {
    "message": "Request completed successfully",
    "destination": {
      "screenId": "some_screen",
      "templateId": "tpl_xxx",
      "templateType": "form_template",
      "endpoint": "/api/v1/partner/screen/some_screen"
    }
  }
}
```

Screen-fetch responses remain screen documents. They do not need a `nextScreen` property.

## 08.5 — Event and Action

```text
Event = when something happens
Action = what should happen
```

Example:

```text
Element
  ↓
onClick
  ↓
Action
```

The action remains completely generic. Dynamic never contains login/OTP/profile-specific action classes.

Supported action categories:

```text
request
navigate
present
dismiss
state
external_uri
sequence
```

Each action category owns its own behavior.

There is **no `ExecuteDynamicActionUseCase`**. `LoadDynamicScreenUseCase` is the only Dynamic screen-loading use case. Action execution is handled by the Dynamic runtime/action infrastructure and action-specific handlers/capabilities.

## 08.6 — Direct Navigate Action

A direct navigation action contains its destination directly beside the action type:

```json
{
  "type": "navigate",
  "destination": {
    "screenId": "some_screen",
    "templateId": "tpl_xxx",
    "templateType": "form_template",
    "endpoint": "/api/v1/partner/screen/some_screen"
  }
}
```

The flow is:

```text
Event
  ↓
Navigate action
  ↓
DynamicDestination
  ↓
:navigation
  ↓
Dynamic Container
  ↓
Template Registry(templateType)
  ↓
LoadDynamicScreenUseCase(endpoint)
  ↓
Dynamic API
  ↓
complete response
  ↓
render
```

## 08.7 — Request Action

`request` is an API action. Request-specific metadata stays with the request action:

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

Before execution, references and bindings are resolved.

```text
Event
  ↓
request action
  ↓
resolve $binding / $context / $response / $literal
  ↓
construct request JSON according to backend contract
  ↓
Dynamic request infrastructure
  ↓
API
```

The Dynamic action layer does not perform raw HTTP itself; existing data/network infrastructure remains responsible for transport.

## 08.8 — Request Response Modes

For:

```text
responseMode = none
```

the request completes without destination navigation.

For:

```text
responseMode = destination
```

the request response is inspected for `data.destination`:

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
  ↓
Dynamic API
  ↓
complete screen definition
  ↓
render
```

There is no separate screen-specific request-then-navigate architecture.

## 08.9 — Direct Navigate and Request Destination Unification

Both paths produce exactly the same destination type:

```text
navigate ───────────────────→ DynamicDestination

request(responseMode=destination)
        ↓
response.data.destination
        ─────────────────────→ DynamicDestination
```

They then use the same navigation/container/template-loading flow.

## 08.10 — Binding Boundary

Binding is generic and backend-defined.

```text
Input/value changes
      ↓
binding key
      ↓
Dynamic runtime state
```

When a request needs bound data:

```text
request body
      ↓
reference resolution
      ↓
$binding(key)
      ↓
current bound value
      ↓
JSON body according to backend contract
```

Bindings are never navigation arguments.

Multiple bindings are supported. The runtime builds the backend-defined JSON object/array/value structure; it does not concatenate unrelated binding values merely because there are multiple bindings.

The frontend never assigns business meaning to a binding key.

## 08.11 — Generic References

```text
$literal
$binding
$context
$response
```

References read values. Actions perform effects/mutations.

Values needed from a previous request or runtime context are resolved through these generic references rather than being placed into `DynamicDestination`.

## 08.12 — Template Stack / Navigation Semantics

The single Dynamic Container may host a navigation stack of Dynamic destinations.

```text
replace
    current destination → new destination

push/add
    previous destinations + new destination

pop/dismiss/back
    current destination removed
```

`templateType` does not decide push/replace behavior. Navigation/action semantics do.

## 08.13 — External URI

`external_uri` is not an internal Dynamic destination:

```text
external_uri
  ↓
external URI capability/effect
  ↓
platform boundary
```

Future capabilities such as `present`, `dismiss`, `deeplink`, or other action types remain separate capabilities and are not folded into `DynamicDestination`.

## 08.14 — Frozen Principles

1. `DynamicDestination` is the only destination domain model.
2. Destination fields are exactly `screenId`, `templateId`, `templateType`, and `endpoint`.
3. `templateId` is the concrete Dynamic template instance/back-stack identity.
4. `templateType` selects a registered frontend template capability; it does not contain the actual template definition.
5. `endpoint` is used by `LoadDynamicScreenUseCase` to load the complete screen definition.
6. There is no empty template/empty instance stage.
7. The API response is authoritative for the actual template definition/content.
8. Rendering begins from the loaded Dynamic response, including its template properties and child hierarchy.
9. Bootstrap, direct navigate, and request-generated destinations use the same model.
10. Direct navigate carries `destination` beside `type`.
11. Request-specific method/authentication remain in the request action, not the destination.
12. `request(responseMode=destination)` resolves `data.destination` and follows the same destination flow.
13. Binding is generic and never travels through navigation.
14. No business-specific data is passed as destination/navigation arguments.
15. There is no `ExecuteDynamicActionUseCase`.
16. `LoadDynamicScreenUseCase` is the only Dynamic screen-loading use case.
17. Dynamic does not create business-specific native screens.
18. Existing `:navigation` remains the application navigation module.
19. Dynamic does not introduce another navigation framework.
20. `templateType` never determines push/replace behavior.
21. External URI is not an internal destination.
22. Backend destination selection remains authoritative; frontend resolves and executes the supplied contract.
