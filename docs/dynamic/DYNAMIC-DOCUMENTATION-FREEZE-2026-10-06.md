# Dynamic UI — Documentation Freeze

**Date:** 2026-10-06  
**Branch:** `feature/dynamic-ui`  
**Freeze scope:** Dynamic architecture documentation after final reconciliation

## 1. Freeze declaration

The Dynamic documentation reconciliation is now **FROZEN**.

This freeze establishes the current reconciled documentation as the authoritative basis for the next implementation-plan phase.

This is a documentation freeze only.

It does **not** authorize source-code changes.

## 2. Authoritative reconciliation

The authoritative reconciliation record is:

`docs/dynamic/DYNAMIC-FINAL-RECONCILIATION-2026-10-06.md`

The Dynamic implementation status record is:

`docs/dynamic/DYNAMIC-IMPLEMENTATION-STATUS.md`

Historical Dynamic documents remain decision history. Where historical wording conflicts with the frozen reconciliation, the frozen reconciliation takes precedence.

## 3. Frozen boundaries

The following boundaries are frozen for the next implementation-plan phase:

- `DynamicStore` owns Dynamic lifecycle, runtime state, canonical binding state, action orchestration, and effects.
- `DynamicScreenState` remains the canonical mutable owner of Dynamic binding state.
- `DynamicActionExecutor` is the single Dynamic action-dispatch boundary.
- `ExecuteDynamicActionUseCase` is removed/prohibited and must not be recreated under another name.
- `LoadDynamicScreenUseCase` remains the screen-loading use case and is not an action executor.
- `DynamicDestination` remains the four-field destination identity contract: `screenId`, `templateId`, `templateType`, `endpoint`.
- `DynamicRequest` owns request execution metadata such as endpoint, method, body, and request-level authentication metadata.
- `templateType` does not determine navigation.
- `navigate` remains behind the navigation boundary.
- `present` and `dismiss` remain Dynamic runtime presentation operations.
- `external_uri` remains separate from Dynamic navigation and is surfaced as an external effect/result for the platform/application boundary.
- Runtime, renderer, SDUI elements, domain, data, and network responsibilities remain separated according to the reconciled architecture.
- Separate render-value and action-value resolution responsibilities remain intentional.
- Template/element registry and resolver/renderer responsibilities remain distinct.
- No second canonical binding/form state owner, generic command layer, prepared-action pipeline, or duplicate action executor is introduced without a future explicit frozen decision.

## 4. Superseded terminology rule

Any historical document that presents one of the following as the current architecture is superseded by this freeze:

- `ExecuteDynamicActionUseCase`
- destination contracts that mix destination identity with request metadata
- templateType-driven navigation
- multiple canonical binding/form state owners
- renderer-owned network/navigation side effects
- duplicate command/prepared-action/action-execution pipelines

Historical documents are not deleted as part of this freeze.

## 5. Implementation-plan boundary

The next phase is:

**Implementation Plan**

The implementation plan must:

1. use this frozen documentation as its source of truth;
2. identify only the targeted implementation changes required by the reconciled contract;
3. list exact files/classes expected to change;
4. identify verification commands and acceptance criteria;
5. avoid architecture redesign and unrelated warning cleanup;
6. stop for user plan-freeze/implementation authorization before source-code changes.

## 6. Gate status

```
ARCHITECTURE AUDIT             COMPLETE
DOCUMENTATION RECONCILIATION   COMPLETE
DOCUMENTATION FREEZE           COMPLETE
IMPLEMENTATION PLAN            NEXT
PLAN FREEZE                    AFTER PLAN
IMPLEMENTATION AUTHORIZATION   REQUIRED BEFORE CODE
```

**Status: DOCUMENTATION FROZEN.**
