# Dynamic UI — Final Architecture / Implementation Reconciliation

**Date:** 2026-10-05 reconciliation update  
**State:** DISCUSSION FROZEN / IMPLEMENTATION SOURCE OF TRUTH  
**Scope:** Dynamic UI vertical slice, Bootstrap/Splash boundary, destination, navigation/container, actions, bindings, screen loading, network boundary

This record supersedes conflicting historical implementation wording. It does not reopen already frozen Dynamic architecture; it reconciles the implementation records to the latest frozen action/navigation discussion.

## 1. Splash and Bootstrap are one flow

```text
ApplicationContent
    ↓
Splash UI / Splash Store
    ↓
ApplicationBootstrap
    ↓
BootstrapRepository
    ↓
BootstrapRepositoryImpl
    ↓
BootstrapRemoteDataSource
    ↓
RequestHandler / HttpClient
    ↓
Bootstrap DTO
    ↓
BootstrapMapper
    ↓
BootstrapOutput
    ↓
DynamicDestination
```

## 2. Canonical destination

There is exactly one destination model:

```text
DynamicDestination
├── screenId
├── templateId
├── templateType
└── endpoint
```

`method` and `authentication` are request metadata, not destination fields.

No business/runtime values are destination arguments.

## 3. One Dynamic Container

There is one permanent Dynamic Container. It is the host for all backend-defined Dynamic templates.

No Login/OTP/Profile/Dashboard native screen classes are created.

Application navigation uses the existing `:navigation` module. Dynamic does not introduce another navigation framework.

## 4. Canonical destination → template → API lifecycle

The destination already identifies the concrete template instance, template type, and API endpoint.

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

There is no empty template/instance stage.

The Template Registry only resolves the supported frontend template capability. It does not fetch the screen API and does not invent the backend definition.

The API response is authoritative for the actual template definition, properties, children, events, actions, bindings and references.

## 5. Dynamic use-case boundary

There is only one Dynamic screen-loading use case:

```text
LoadDynamicScreenUseCase
```

There is **no `ExecuteDynamicActionUseCase`**.

Action execution remains in Dynamic runtime/action infrastructure and action-specific handlers/capabilities. This does not create another generic use-case layer.

## 6. Direct navigation

```text
Dynamic Event
  ↓
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

## 7. Request → destination

```text
Dynamic Event
  ↓
request action
  ↓
resolve references/bindings
  ↓
request API
  ↓
response
  ↓
responseMode = destination
  ↓
data.destination
  ↓
DynamicDestination
  ↓
same :navigation → Dynamic Container → Template Registry → LoadDynamicScreenUseCase flow
```

For `responseMode = none`, no destination navigation occurs.

This unifies direct navigation and request-generated navigation without a second navigation architecture.

## 8. Binding/reference boundary

Bindings are generic backend-defined runtime values:

```text
input/value change
  ↓
binding key
  ↓
Dynamic state
```

Actions resolve:

```text
$literal
$binding
$context
$response
```

Bindings never become navigation arguments. Multiple bindings are supported and request JSON follows the backend-defined object/array/value structure.

## 9. Navigation stack behavior

The existing navigation mechanism provides root/push/replace/pop behavior.

Dynamic template semantics are represented through these operations. `templateType` never decides push versus replace.

Back/pop removes the current destination. Refresh reloads the current destination without adding a new navigation entry.

## 10. External URI boundary

`external_uri` is an external capability/effect, not an internal Dynamic destination.

Future capabilities such as `present`, `dismiss`, or `deeplink` remain separate action capabilities and are not folded into the destination model.

## 11. Network boundary

Dynamic screen loading remains:

```text
LoadDynamicScreenUseCase
    ↓
DynamicRepository
    ↓
DynamicRepositoryImpl
    ↓
DynamicRemoteDataSource
    ↓
RequestHandler
```

Request actions use the existing request/network infrastructure. Navigation never performs Dynamic API calls.

## 12. Documentation authority

During implementation, follow:

```text
00-FEATURE-DISCUSSION.md
        ↓
03-NAVIGATION-CONTAINER-FREEZE.md
        ↓
04-DYNAMIC-08-UNIFIED-DESTINATION-DISCUSSION.md
        ↓
05-DYNAMIC-08-IMPLEMENTATION-PLAN.md
        ↓
01-IMPLEMENTATION-PLAN.md
        ↓
02-IMPLEMENTATION-STATUS.md  (status only)
```

`00-MODULE-IMPLEMENTATION-PLAN.md` is superseded and is not an implementation source.

## 13. Reconciliation rule

Any older wording that describes any of the following is stale and must not be implemented:

- destination containing `method` or `authentication`;
- `ExecuteDynamicActionUseCase`;
- an empty template/empty instance stage;
- separate business-specific screens;
- a second navigation framework;
- navigation state owned by Dynamic Store;
- API calls performed by `:navigation`;
- business data passed through destination/navigation;
- request-generated destinations using a different navigation model.

## 14. Verification baseline

The previous DYNAMIC-08 source/test reconciliation passed the owner-provided JVM compile/test baseline. The current documentation reconciliation is a contract update; source verification of the newly reconciled action/navigation/use-case boundary is the next implementation gate.