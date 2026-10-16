# 69 — Navigation / Presentation: Owner-Approved Plan Frozen

**Status: PLAN_FROZEN — Kotlin implementation NOT YET AUTHORIZED.**
**Owner decision:** 2026-10-16 — explicitly approved all N1–N6 recommendations from freeze candidate 68.
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`.
**Sources:** audits 66–67 and freeze candidate 68. Backend `CB-Backend/development` remains **read-only**. `SequenceActionHandler` remains registered, inert, and unchanged.

## Frozen decisions
- **N1 Dismiss:** Explicit `dismiss` closes only an active presentation; never pops the ordinary screen stack. With no active presentation, existing controlled effect failure behavior applies. Distinct from Nav3 Back or `navigate(mode:pop/back)`, which keep `DynamicNavigationState.pop()` behavior.
- **N2 Present:** Backend valid wire `{type:'present',targetId,payload:{presentation}}` is not representable by current frontend `DynamicAction` / destination-only `PresentActionHandler`. Defer implementation until a source-backed target contract is separately approved; never synthesize destination or edit backend.
- **N3 Back alias:** Retain existing ActionExecutor top-level `back` → `dismiss` alias temporarily as frozen, pending compatibility inventory. This is not permission to change explicit Navigate back behavior.
- **N4 Presentation styles:** Do not add bottom-sheet/popup rendering or imply they are supported. Existing Compose Dialog host remains unchanged pending presentation contract.
- **N5 Events:** Keep existing five mapped frontend event hooks. Do not broaden event mapping without supported contract.
- **N6 Effect batching:** Keep `List<DynamicActionEffect>` and session channel unchanged. Investigate during separate Shared Infrastructure audit; no speculative simplification.

## Exact file-by-file implementation plan
1. **`app/src/commonMain/kotlin/com/carbroz/cbpartner/app/ApplicationContent.kt` — MODIFY (only eligible immediate production edit):**
   - In `DynamicActionEffect.Dismiss` effect branch, replace `check(navigationState.dismissPresentation() || navigationState.pop())` with `check(navigationState.dismissPresentation())`.
   - Preserve existing effect error sanitization, navigation `Pop`, `Push`, `Replace`, `Present`, URI handling, single session channel, ordering, and scope.
2. **Tests — ADD/EXTEND, TESTS FIRST:**
   - Cover explicit Dismiss with active modal, no modal at root, and no modal at non-root (must not pop); ensure Navigate Pop/Back remains independent and may dismiss an active modal.
   - Prefer focused executable effect-consumer test if supported by existing architecture. If `ApplicationContent` branch cannot be directly exercised without a large extraction, report limitation and use existing `DynamicNavigationState` tests plus targeted manual smoke; **do not** introduce a new manager/abstraction only for a test.
   - Tests must be honest about the tested layer: testing `dismissPresentation()` alone is not proof of `ApplicationContent` effect-routing behavior.
3. **KEEP — no edits:** `NavigateActionHandler.kt`, `PresentActionHandler.kt`, `DismissActionHandler.kt`, `DynamicActionExecutor.kt`, `DynamicActionResult.kt`, `DynamicNavigationState.kt`, `DynamicNavigationHost.kt`, `DynamicActionDto.kt`, `DynamicActionMapper.kt`, `DynamicAction.kt`, `DynamicDestinationDto.kt`, `DynamicDestination.kt`, Request Action implementation, and inert Sequence handler.
4. **DEFER:** Backend Present targetId resolution, presentation style-specific host, event extensibility, action back compatibility removal, effect-list / channel simplification; each requires separate decision and frozen plan.

## Gates
1. Await **separate explicit owner authorization to implement Kotlin**. Plan approval alone does not grant source-edit permission.
2. Add focused test(s) before production changes where feasible; demonstrate RED if test genuinely exercises faulty routing. Do not claim RED from a test that merely exercises navigation state independently.
3. Perform minimal single-branch frontend code edit, no temporary branches or backend changes.
4. Verify local `./gradlew.bat :navigation:jvmTest :feature:dynamic:jvmTest :data:jvmTest :domain:jvmTest :webApp:wasmJsBrowserDevelopmentWebpack` plus `git diff --check`. Android/Web smoke remains separate and must be reported accurately.
5. Source reconciliation and owner review precede implementation status/acceptance freeze.
