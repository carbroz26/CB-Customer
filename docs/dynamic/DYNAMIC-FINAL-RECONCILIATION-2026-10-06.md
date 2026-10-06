# Dynamic UI — Final Architecture Reconciliation

**Date:** 2026-10-06  
**Branch:** `feature/dynamic-ui`  
**Implementation reconciliation:** completed after the authorized targeted changes recorded in the implementation status document.

## 1. Purpose

This document records the final reconciliation between the current Dynamic implementation and the latest frozen Dynamic architecture decisions.

It is **not a new architecture proposal**.

It does not reopen already-frozen decisions and does not authorize code changes.

The purpose is to establish one current interpretation of:

- Dynamic screen lifecycle and state ownership
- binding ownership
- action execution
- request actions
- navigation
- present/dismiss
- external URI
- rendering/runtime responsibilities
- use-case responsibilities
- domain/data/network boundaries
- stale or superseded terminology

## 2. Authority

For the current Dynamic implementation, the latest reconciled Dynamic documentation and frozen discussion decisions are authoritative.

Historical discussion documents remain useful for decision history, but a historical statement must not override a later frozen decision.

When documentation and implementation disagree:

1. identify the exact contradiction;
2. do not silently reinterpret a frozen decision;
3. reconcile the documentation and implementation through the normal implementation-plan/authorization workflow.

## 3. Frozen ownership model

### DynamicStore

`DynamicStore` is the owner of:

- Dynamic screen lifecycle
- Dynamic screen state
- loading/retry/refresh lifecycle
- binding state
- interaction-to-action orchestration
- applying action-produced state changes
- Dynamic effects

There is one canonical mutable owner for dynamic field/binding state.

### DynamicScreenState

`DynamicScreenState` contains the current runtime state, including bindings.

A renderer must not create a second canonical form/binding store.

The following are not allowed as competing state owners:

- element-local canonical form state
- a separate FormStore
- a second Dynamic state container
- a ViewModel-based Dynamic state owner

## 4. Action boundary

The canonical action path is:

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

`DynamicActionExecutor` is the single Dynamic action-dispatch boundary.

### Explicitly rejected

`ExecuteDynamicActionUseCase` must not be reintroduced.

Do not recreate the removed action chain through alternative names such as:

- ActionManager
- ActionCoordinator
- PreparedAction
- Command layer
- second action executor

unless a future frozen decision explicitly requires one.

## 5. Action capabilities

The current protocol-level action vocabulary remains:

- `request`
- `navigate`
- `present`
- `dismiss`
- `state`
- `external_uri`
- `sequence`

Handlers remain capability-specific.

### Sequence

`sequence` is a composite action and uses the same action executor for child actions.

It must not create a second execution pipeline.

## 6. Request boundary

Request metadata is separate from destination identity.

### DynamicDestination

The destination contract is:

```
screenId
templateId
templateType
endpoint
```

### DynamicRequest

Request execution metadata belongs to `DynamicRequest`, including:

- endpoint
- method
- body

Authentication/request metadata remains request-level information and must not be folded back into destination identity.

The recent `DynamicRequest` endpoint-boundary change is therefore consistent with the frozen separation.

## 7. Screen loading use case

`LoadDynamicScreenUseCase` currently remains in the codebase as the screen-loading use case.

It is **not** an action executor and must not absorb action execution.

This is now an explicit documented boundary.

Therefore the current reconciliation does **not** classify `LoadDynamicScreenUseCase` as an automatically removable stale class.

Any future removal would require a separate implementation decision based on actual usage and the frozen contract.

## 8. Navigation boundary

Dynamic action execution does not infer navigation from `templateType`.

`templateType` answers:

> How is this destination rendered?

It does not answer:

> Where should the application navigate?

The backend-provided destination is the navigation input.

Navigation remains behind the navigation boundary.

## 9. Present and dismiss

`present` and `dismiss` are Dynamic runtime presentation operations.

They operate through Dynamic runtime/navigation state rather than becoming ordinary screen-navigation decisions.

The Dynamic feature must not directly create platform-specific presentation implementations.

## 10. External URI

External URI is deliberately separate from Dynamic navigation.

The action handler resolves the URI and returns an external-URI result/effect.

The platform/application boundary consumes that effect.

The Dynamic action handler must not directly invoke a platform URI API.

Current implementation at HEAD follows this boundary: `ExternalUriActionHandler` resolves the URI and returns `DynamicActionResult.ExternalUri`.

## 11. Runtime and renderer boundary

Runtime/rendering responsibilities are separated from state orchestration.

### Store

Owns:

- lifecycle
- state
- bindings
- action orchestration
- effects

### Runtime

Owns:

- template resolution
- dynamic value resolution
- validation
- render context
- layout/runtime interpretation

### Renderer

Owns:

- translating the selected Dynamic model into Compose UI
- delegating template/element rendering

### SDUI elements

Own:

- element-specific UI rendering
- emitting interaction intents/actions

They do not own:

- network execution
- repository access
- navigation implementation
- canonical field state
- action orchestration

## 12. Resolver boundary

The existence of separate render/action resolution concepts is intentional.

### DynamicValueResolver

Responsible for runtime/render value resolution.

### DynamicActionValueResolver

Responsible for action-time value resolution.

They must not evolve into two competing generic resolution frameworks.

## 13. Registry boundary

Registry and resolver responsibilities remain distinct.

### TemplateRegistry

Knows registered template implementations.

### TemplateResolver

Determines which template applies.

### ElementRegistry

Knows registered element renderer capabilities.

### ElementRenderer

Renders the selected element.

No additional generic registry framework should be introduced without a concrete protocol requirement.

## 14. Data and network boundaries

The intended dependency direction is:

```
feature:dynamic
      ↓
domain contracts
      ↓
data repository
      ↓
network transport
```

The Dynamic feature must not absorb:

- HTTP transport
- serialization infrastructure
- platform networking
- repository implementation

The network layer must not know about:

- DynamicStore
- Compose rendering
- SDUI elements
- Dynamic navigation

## 15. Superseded terminology

Historical Dynamic material may contain terminology that no longer represents the current contract.

The following must not be treated as current architecture when they conflict with the frozen decisions:

- destination models that mix destination identity with request metadata
- `ExecuteDynamicActionUseCase`
- duplicate Command/PreparedAction execution chains
- templateType-driven navigation
- multiple canonical binding/form state owners
- renderer-owned navigation/network side effects

Historical documents should be marked as superseded where they present these structures as current requirements.

## 16. Current reconciliation result

| Area | Result |
|---|---|
| DynamicStore lifecycle ownership | ALIGNED |
| Binding ownership | ALIGNED |
| DynamicActionExecutor boundary | ALIGNED |
| ExecuteDynamicActionUseCase | REMOVED / PROHIBITED |
| LoadDynamicScreenUseCase | RETAINED AS SCREEN-LOADING USE CASE |
| DynamicRequest separation | ALIGNED |
| Destination identity | ALIGNED |
| Navigation boundary | ALIGNED |
| Present/dismiss boundary | ALIGNED |
| External URI boundary | ALIGNED |
| Runtime/rendering separation | ALIGNED |
| Registry/resolver separation | ALIGNED |
| Domain/data boundary | ALIGNED |
| Data/network boundary | ALIGNED |
| Historical terminology | REQUIRES SUPERSEDED LABELS WHERE CONTRADICTORY |

## 17. Implementation implications

The reconciliation identifies only targeted implementation verification/correction areas.

It does **not** authorize:

- architecture redesign
- new layers
- new use cases
- new state owners
- new action engines
- speculative warning cleanup

The implementation phase must work only from this reconciled contract.

## 18. Final implementation reconciliation

The authorized implementation pass has been reconciled against this frozen architecture.

Result: ALIGNED.

- The canonical Store/action boundary remains intact.
- Recursive sequence execution uses the same `DynamicActionExecutor`.
- `DynamicRequest` remains separate from destination identity.
- The prohibited action-use-case and duplicate orchestration structures are absent.
- Renderer/SDUI/runtime/network/navigation boundaries remain aligned.
- Login/OTP contract coverage is present.
- No unrelated architecture redesign was introduced.

The remaining Windows Gradle commands are an environment-dependent build-verification step and are not represented as passed by this reconciliation.

## 19. Final gate

```
ARCHITECTURE AUDIT                 COMPLETE
DOCUMENTATION RECONCILIATION       COMPLETE
DOCUMENTATION FREEZE               COMPLETE
IMPLEMENTATION PLAN                FROZEN
PLAN FREEZE                        COMPLETE
IMPLEMENTATION AUTHORIZATION       COMPLETE
TARGETED CODE RECONCILIATION       COMPLETE
FINAL IMPLEMENTATION RECONCILIATION COMPLETE
DYNAMIC IMPLEMENTATION FREEZE      COMPLETE*
WINDOWS GRADLE VERIFICATION        PENDING
```

*Source/architecture freeze is complete; Windows build verification remains a required developer-environment acceptance step.
