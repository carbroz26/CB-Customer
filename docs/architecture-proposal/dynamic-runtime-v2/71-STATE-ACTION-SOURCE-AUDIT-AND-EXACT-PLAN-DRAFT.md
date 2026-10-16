# 71 — State Action Source Audit and Exact Plan Draft

**Status:** AUDIT / DISCUSSION — NOT FROZEN. NO IMPLEMENTATION AUTHORIZED.
Frontend `CB-Partner/architecture/dynamic-runtime-v2-discussion-freeze`. Backend `CB-Backend/development` READ ONLY. Existing Request and N1 changes retained. Sequence registered inert.

## Sources checked
Frontend: `feature/dynamic/action/StateActionHandler.kt`, `DynamicActionResult.kt`, `DynamicActionExecutorTest.kt`, `feature/dynamic/runtime/DynamicRuntimeState.kt`, `DynamicReferenceResolver.kt`, `DynamicRenderContext.kt`, `DynamicRuntimeStateTest.kt`, `feature/dynamic/store/DynamicStore.kt`, `feature/dynamic/DynamicModule.kt`, `data/dynamic/model/action/DynamicActionDto.kt`, `data/dynamic/mapper/DynamicActionMapper.kt`, `app/ApplicationContent.kt`.
Backend: `sdui/engine/src/core/Action.ts`, `SduiModel.ts`, tests `Action.spec.ts`, `SduiModel.spec.ts`; partner dashboard authoring inspected.

## Confirmed wire mismatch (do not silently adapt)
Backend `stateAction({targetId:'field',operation:'set',property:'enabled',value:false})` emits:
```json
{"type":"state","targetId":"field","payload":{"operation":"set","property":"enabled","value":false}}
```
Backend schema requires top-level nonblank `targetId`, `set|toggle`, one property of `visible,enabled,selected,expanded,checked,loading,value`. Set **must** include `value`, including explicit JSON null; toggle **must not** include `value` and cannot target `value` property. Backend tests enforce both cases.

Frontend `DynamicActionDto` has `type,payload,destination` only; Kotlin Domain `DynamicAction` likewise. `DynamicActionMapper` maps payload and destination, thus top-level `targetId` is lost (depending on JSON config unknown fields may be ignored or decoding may reject; this audit does not assume one).
`StateActionHandler` reads `payload.key`, resolves optional `payload.value` and falls back to `DynamicValue.NullValue`; no targetId, operation, property, bool-toggle, target existence, property/type validation.
Backend-authorable State actions therefore cannot directly run through frontend handler as written.

## Ownership and SRP
- `DynamicRuntimeState` is a StateFlow-backed map of typed values. `setAll` applies nested dot-path assignments, preserves sibling fields, and tests cover nested values / null / typed arrays and objects.
- `DynamicStore.applyActionResult` owns committing `result.stateChanges`; action handlers produce results, do not directly mutate UI or navigation. Preserve this pure orchestration boundary and state-before-effects behavior.
- `DynamicRenderContext` passes `stateValues` into general `DynamicReferenceResolver`, enabling explicit `$state` resolution in renderer properties; **does not itself implement targetId/property UI mutation**. A global `state["field.enabled"]` convention alone cannot guarantee a component's enabled state changes. Check each relevant renderer and binding behavior before designing contract.
- `DynamicReferenceResolver.resolvePath` returns NullValue both for missing and explicit null; `resolveExistingPath` is presence-aware but currently used for Request retain. Do not redesign it for State without proven requirement.
- `DynamicRuntimeState.setPath` ignores empty paths and drops blank segments; on dotted nested set, intermediate nonobject values are replaced with objects. This is valid current generic behavior but requires documented safety policy if targetId/property becomes structured mutation.
- `DynamicActionExecutor` dispatches State handler via explicit registry; no extra StateManager or ActionManager warranted.
- `StateActionHandler` has no dedicated focused test found in examined test inventory; Executor test covers a stub StateChanged handler, and RuntimeState tests cover storage but not wire contract.

## Key unresolved contract questions — OWNER DISCUSSION
S1. Does State Action mutate the **rendered node with id=targetId**, or a generic runtime-state namespace? Backend clearly authors targetId/property, but actual target evaluation/rendering behavior must be specified. Should not invent path string normalization without agreement.
S2. What happens for unknown targetId, unsupported property on target element, nonboolean toggle state, and missing initialized property? Controlled reject/noop/default? Decide explicitly.
S3. Should `value` property mutate Element's bound input value, render property, or retained runtime state? Distinguish Element binding from runtime-state properties.
S4. Is state scoped per screen/navigation entry or session-global? Existing `DynamicRuntimeState` instance is remembered at app session level and shared across entries; may be incorrect for two different screens using the same node ids. Do not change store lifetime speculatively.
S5. How should explicit JSON null behave on set for boolean properties and `value`? Backend allows null for set, but target property type restrictions are not yet established.
S6. Does Renderer own applying overlays from state, or do individual element renderers each interpret a typed target mutation? Choose simplest existing structure; no second registry or cross-element infrastructure without source-backed necessity.
S7. Backend authoring includes `$response` reference in its generic ref schema; Frontend rejects it. Existing Request freeze disallows it; State audit must not revive `$response` implicitly.

## Exact file-by-file plan DRAFT (not approved)
1. `data/.../DynamicActionDto.kt` — CONDITIONAL MODIFY: add top-level targetId field if S1 resolves to node-targeted action; required for parity. Do not change shared DTO for State before contract freeze.
2. `domain/.../DynamicAction.kt` + `data/.../DynamicActionMapper.kt` — CONDITIONAL MODIFY: preserve targetId across mapper/domain. Avoid Action-specific subclass hierarchy absent a proven reason.
3. `feature/dynamic/action/StateActionHandler.kt` — MODIFY AFTER DECISIONS: validate operation/property/value, perform pure calculation of StateChanged, explicitly typed toggle and missing-value behavior; do not directly change renderer/nav.
4. `feature/dynamic/runtime/DynamicRuntimeState.kt` — PREFER KEEP: already single typed StateFlow owner, but change only if S4 and target state encoding force it.
5. `feature/dynamic/store/DynamicStore.kt` — PREFER KEEP: existing state-before-effects orchestration. Any pre-action validation of node existence only if S1-S3 require Store-owned document access; no cross-element logic in handler.
6. `feature/dynamic/runtime/DynamicRenderContext.kt` and existing element renderers — CONDITIONAL, TARGETED: only after exact supported property overlay mapping and owner contract; do not invent generic UI mutation manager.
7. `StateActionHandlerTest.kt` (new), `DynamicRuntimeStateTest.kt`, `DynamicStoreTest.kt`, data mapping tests — ADD tests first: backend JSON -> DTO -> Domain -> Handler -> Store -> relevant renderer; missing target, bad operation/property, set null, toggling bool/invalid, screen-state scoping and render update where specified.
8. `DynamicActionResult.kt`, `DynamicActionExecutor.kt`, `DynamicModule.kt`, `DynamicNavigationState.kt` — KEEP unless real compile/caller problem documented.
9. Backend `Action.ts`, `SduiModel.ts` and Backend screens/tests — NO EDIT. Inert `SequenceActionHandler` — NO EDIT.

## Tests and freeze gates
Require owner decisions S1-S6, source-level renderer follow-up, exact implementation plan update, PLAN_FROZEN, then separate implementation authorization. First RED tests, small single-branch commits, local `:data:jvmTest :domain:jvmTest :navigation:jvmTest :feature:dynamic:jvmTest :webApp:wasmJsBrowserDevelopmentWebpack`, `git diff --check`, Android/Web runtime smoke. No claim backend smoke/tests run.
