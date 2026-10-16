# 65 — Request Action post-JVM source reconciliation and next Action SRP audit

**Status:** READ-ONLY SOURCE REVIEW / ACCEPTANCE PENDING. Frontend branch `architecture/dynamic-runtime-v2-discussion-freeze`, inspected through commit `cbf64f06`. **No Kotlin edits or build execution in this review.** Backend migration remains deferred; Sequence remains inert.

## Evidence
Owner ran `:feature:dynamic:jvmTest` at `cbf64f06` and reported BUILD SUCCESSFUL. Full `:domain:jvmTest :data:jvmTest :navigation:jvmTest :feature:dynamic:jvmTest :webApp:wasmJsBrowserDevelopmentWebpack`, `git diff --check`, and Android/Web smoke **not yet evidenced for this commit**.

## Source review of frozen R1–R6
- R1: `RequestActionHandler` missing responseMode -> none; explicitly non-string/null and unsupported modes rejected pre-HTTP. Verified in source and focused tests.
- R2: Store resolves Request validate via `DynamicReferenceResolver`; true requires loaded screen data, then validator. Handler separately resolves validate for Domain Request. Risk: separate resolutions / duplicated validity checks; still no duplicate form validation. Existing `resolvedTrueValidationWithoutLoadedFormPreventsRequest` test instead supplies an unresolved `$context.mustValidate` and checks zero Request calls; it **does not prove resolved-true validation**. Add a real true-context fixture with invalid form to test the intended case and explicit null/rejected types.
- R3: Handler permits only explicit push/replace and rejects pop/back/unknown/null/wrong type before fetch. Missing -> Push. Explicit mode with responseMode none rejected. Source verified.
- R4: Retain validates mappings and uses `resolveExistingPath`, so explicit null is distinct from missing. However retained mapping shape is only checked **after** HTTP. Contract says missing response path is post-HTTP, but malformed static retain map might be rejected before sending request to avoid side effects; review the frozen wording and tests. No new schema.
- R5: Action failure now keeps `DynamicState.Success` screen and bindings and shows generic message with Dismiss via `DynamicContainer`. This is guarded by positive test. However `DynamicIntent.EffectFailed` still sets `DynamicState.Failure`, and `DynamicContainer` renders full-screen raw `Text(current.message)` for all Failure states. Hence effect failures can still replace loaded screen; classify as separate existing W4 effect error regression. Request in-place error currently shares `validationMessage` field with form-validation errors, and generic `ClearMessage` clears both; this may be a minimal tradeoff but is not a clean typed distinction. Request retry currently means pressing original button again; there is no explicit error-banner Retry control. Do not assert full R5 UX acceptance until this is reconciled.
- R6: Handler returns `DynamicActionResult.Navigate(destination, operation, stateChanges)`; Store commits state then publishes effect list; no direct navigation mutation. Source verified. No backend modifications.

## Other safety gaps identified
- `DynamicStore.handleAction` checks `actionJob` before launching and processes validation inside scope, preserving one action execution at a time. `contextProvider()` is called before action executor `runCatching`: a throwing provider may cancel coroutine without presenting safe message. Consider precise tests before change.
- `DynamicStore.load` still exposes `error.message` as full-screen load error; freeze R5 governs recoverable action errors, not automatically initial load errors.
- `DynamicReferenceResolver.resolveExistingPath` prefers an exact dotted root key over hierarchical lookup. Preserve this intentional behavior; guard explicit null semantics in tests.
- No change to inert Sequence or other handlers needed for these Request findings.

## Remaining Request acceptance gates
1. Run full JVM/Web task set on at least `cbf64f06`, and `git diff --check`.
2. Add correctly named resolved-true context validation test (one invalid form document); verify API not called. Add explicit false and invalid-type cases if missing.
3. Reconcile W4 `EffectFailed` / loaded UI behavior separately, avoiding broad rewrite during Request cleanup; decide whether R5 means only Request errors.
4. Confirm request destination with retained state is committed before effect, under existing Store test callback.
5. Mark Request acceptance only after tests and source reconciliation; no auto-freeze other actions.

## Next Action SRP audit — READ ONLY
- Navigation/Presentation: `NavigateActionHandler`, `PresentActionHandler`, `DismissActionHandler`, `DynamicNavigationState`, `ApplicationContent` effect application, DTO destination/mode; separate explicit navigate from request-derived Navigate effect and modal-only semantics.
- State: `StateActionHandler`, `DynamicRuntimeState`, Backend `stateAction` wire shape mismatch (`targetId/property/operation` vs `key/value`). No backend migration or new cross-element contract.
- Shared: `DynamicActionExecutor`, `DynamicActionResult`, `DynamicActionValueResolver`, `DynamicReferenceResolver`, registries/module and tests; determine if list-of-effects and forwarding adapter are proven unnecessary. Never remove without caller inventory.
- Plan exact file-by-file and freeze separately; no new ActionManager, ViewModel, second NavigationState or speculative abstractions.
