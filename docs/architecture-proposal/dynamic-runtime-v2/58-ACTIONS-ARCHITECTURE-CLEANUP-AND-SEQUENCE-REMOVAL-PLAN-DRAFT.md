# 58 — Action Architecture Cleanup and Sequence Removal Plan (DRAFT)

State: OWNER DECISION RECORDED / PLAN DRAFT — NOT PLAN_FROZEN. Do not edit Kotlin under this plan until review and freeze. Branch: architecture/dynamic-runtime-v2-discussion-freeze.

## Owner decision — supersedes previous interpretations
- **Remove the existing multi-child `sequence` behavior in full.** The owner explicitly rejects performing multiple actions one after another on one UI event, rejects the existing implementation as agent-introduced overengineering, and does not want dead compatibility logic kept. Do not infer that `sequence` is a valid future action contract.
- A user event has one action; `request`, `navigate`, `present`, `dismiss`, `external_uri`, and `state` have independent single responsibilities. Future element-to-element reactive behavior is a **separate later backend + frontend contract discussion**; no timer/visibility/sequence design now.
- Backend Request responses can carry destination, producing a navigation effect; this does NOT mean invoking a separate Navigate handler. Navigation authority applies it, Request handler does not own Nav3.
- This owner correction requires explicit supersession of old frozen-doc sequence semantics before Kotlin changes; retain historical documents with supersession notes, not silent history rewriting.

## Verified existing implementation from source audit
- `DynamicActionExecutor.kt`: `executeInternal` special-cases `sequence`, recursively decodes nested action `payload.actions`, uses `MAX_SEQUENCE_DEPTH=32`, `executeSequence`, `stateDelta`, `collectStateDelta`, map mutation helper, and `toDestination` conversion. Ordinary handler dispatch registry is otherwise simple and useful.
- `DynamicActionResult.kt`: `Sequence` result and `effects: List<DynamicActionEffect>` on every result.
- `DynamicStore.kt`: commits state then `deliverEffects(result.effects.toList())`; validation only checks literal top-level `validate:true`; action exceptions become full-screen state Failure containing raw message.
- `app/ApplicationContent.kt`: session-scoped `Channel<List<DynamicActionEffect>>` with effect consumer; attempted UI-safe failure handling.
- `data/.../DynamicActionDto.kt`, `DynamicActionMapper.kt`, domain `DynamicAction`: represent one Action per UI event (5 event hooks), independent of sequence implementation; normally keep.
- `RequestActionHandler.kt`: repository call and backend destination to typed Navigate result; unknown request mode currently falls back to Push, no strict response-mode validation.
- `NavigateActionHandler.kt`: W4-A strict mode handling; leave unchanged unless common helper proven needed.
- `PresentActionHandler.kt`, `DismissActionHandler.kt`, `ExternalUriActionHandler.kt`, `StateActionHandler.kt`, `DynamicActionHandler.kt`: small independent handlers.
- Existing test files cover recursive sequence and accepted multi-effect batches; must remove/replace obsolete assertions, not leave dead tests.
- `DynamicActionValueResolver.kt` mostly delegates to `DynamicReferenceResolver`; investigate all callers for safe deletion.
- Frozen W4 D3 multi-effect wording and V2 sequence clauses are now incompatible with owner decision. Must reconcile them explicitly.

## Clean architecture invariant
UI event -> one typed action -> Store -> ActionExecutor dispatch -> one independent handler -> typed result; Store commits any state changes -> session consumer applies effect through existing NavigationState/URI host. No handler calls another handler. No second router, action manager, ViewModel, Gradle module or speculative abstraction.

## Exact file-by-file plan
1. `feature/dynamic/action/DynamicActionExecutor.kt`: remove special recursive sequence branch, nested decoding, depth guard, sequence-specific state delta functions and conversion helper; keep trimmed/normalized handler registry duplicate-type rejection and CancellationException behavior. Treat `sequence` as unsupported, with generic safe user-facing error; do not run child actions.
2. `feature/dynamic/action/DynamicActionResult.kt`: remove `Sequence`; assess simplifying list-of-effects to zero-or-one effect. Single-effect semantics must cover Request's optional navigation and StateChanged; minimize representation duplication without breaking consumers.
3. `feature/dynamic/store/DynamicStore.kt`: adjust effect publication to one-result contract; keep state-before-effect, source disposal safety and cancellation, and ensure validation belongs to Request semantics (including request `validate` flags) without duplicating responsibility. Preserve usable UI on action failure.
4. `app/ApplicationContent.kt`: session-owned effect consumer remains, but replace unnecessary effect-batch collection if result becomes at most one effect. Retain rejected navigation transition signal and safe dismissal/clearing of error. Do not add second navigation manager.
5. `feature/dynamic/action/RequestActionHandler.kt`: preserve standalone Request with destination-bearing response; validate `responseMode`/`mode` and retention contracts after source/backend comparison. Never invoke Navigate handler or mutate navigation.
6. `feature/dynamic/action/NavigateActionHandler.kt`: preserve explicit Navigate push/replace/pop/back support and W4-A strict errors.
7. `feature/dynamic/action/PresentActionHandler.kt`, `DismissActionHandler.kt`, `ExternalUriActionHandler.kt`, `StateActionHandler.kt`: keep independent handlers; make dismiss modal-only only after explicit owner contract confirmation (currently session fallback pops).
8. `feature/dynamic/action/DynamicActionValueResolver.kt` and `runtime/DynamicReferenceResolver.kt`: inspect every consumer, remove forwarding wrapper only if behavior and tests remain identical.
9. `feature/dynamic/DynamicModule.kt`: retain direct registration of existing independent handlers, no `sequence` registration.
10. `domain/.../DynamicAction.kt`, `data/.../DynamicActionDto.kt`, `data/.../DynamicActionMapper.kt`: keep generic action DTO/domain; reject or surface unsupported type consistently in runtime and tests (avoid unnecessary special DTO type hierarchy).
11. `feature/dynamic/.../action/DynamicActionExecutorTest.kt`, `DynamicStoreTest.kt`, `RequestActionHandlerTest.kt`, `NavigateActionHandlerTest.kt`, `data/.../mapper/DynamicResponseMapperTest.kt`: delete/replace obsolete sequence expectations with focused dispatch, Request->destination navigation, state commit before effect, source disposal, cancellation, error recovery, unknown sequence rejection, mode validation, and entry retention regression coverage.
12. `docs/architecture-proposal/dynamic-runtime-v2/20-PLAN-FROZEN-V2-IMPLEMENTATION-CONTRACT.md`, `51-W4-PLAN-FREEZE-REVIEW-AND-IMPLEMENTATION-GATES.md`, `56-...`, `57-...`: append or link supersession decision in new authoritative revision, preserve history.

## Required implementation sequence AFTER explicit freeze
A. Inventory exact sequence references across all source, tests, JSON fixtures, docs and backend contract (read only backend). Confirm if backend currently sends sequence; report conflict rather than silently breaking it.
B. Freeze contract removal and resulting `DynamicActionResult` effect cardinality (0..1) with separate plan signoff.
C. Add new tests and remove stale sequence tests in a coherent minimal diff.
D. Remove executor sequence machinery and result subclass; simplify session delivery only when justified by agreed single-effect contract.
E. Request/dismiss/error SRP fixes in separate small commits.
F. Full JVM `:domain:jvmTest :data:jvmTest :navigation:jvmTest :feature:dynamic:jvmTest`, Web webpack, `git diff --check`, source reconciliation; Android/Web smoke. No iOS simulator assertion on Windows.

## Open contract questions (do not infer)
- Should `dismiss` strictly close only a modal/dialog (owner language suggests yes), and should `navigate(mode=pop/back)` cover screen pop?
- Should action Result always contain at most one effect, or does Request require any independently justified compound result (current Request retention state + one navigation effect is not multi-action)?
- What should happen if backend sends unsupported `sequence` before simultaneous contract migration? Explicit error vs backend migration gate; no silent execution.
- Precise Request `validate` semantics (true/false/default) and action-error UI behavior.

No Kotlin code changed by creating this plan document. 
