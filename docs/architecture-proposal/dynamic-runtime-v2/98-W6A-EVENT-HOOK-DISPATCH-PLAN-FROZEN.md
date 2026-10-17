# 98 — W6-A Event Hook Dispatch — Exact Plan Frozen

**Status:** PLAN FROZEN; Kotlin production implementation NOT YET AUTHORIZED.
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`. **Source audit baseline:** `de06b41bf918abfdc1cd3fe1a5acfe2830188ae2`.
**Owner decision:** Continue W6-A by fixing exact native event semantics and freezing bounded implementation scope. Do not reopen S1–G3, W5, backend State/Present wire contracts, or inert Sequence.

## Authority and current source truth

- V2 doc 20 declares exactly five hooks: `onClick`, `onLongClick`, `onValueChange`, `onFocus`, `onSubmit`. Later doc 69 N5 preserves those five and forbids speculative event extensions.
- `DynamicActionsDto` and `DynamicActionMapper` map all five. Renderers currently dispatch Button `onClick`, Content `onClick`, Input `onValueChange`, and Text clickable span actions. Image, Icon, plain Text element-level action hooks lack click wiring. No renderer dispatches `onLongClick`, `onFocus` or `onSubmit`.
- Input `changed()` calls `context.onValueChange(binding, newValue)` before `context.onAction(...)`; verify latest binding snapshot through Store with a focused test before assuming it is safe. Do not invent extra state coordinator.

## Frozen native event semantics (W6-A only)

1. **onClick:** One action for an actual click/tap when declared. If absent, never fabricate an action. Element-level click must not obscure independently clickable Text spans.
2. **onLongClick:** One declared action on recognized long press. When both click and long-click exist, one long-press gesture must not additionally emit click. When only click exists, retain ordinary click behavior. Do not enable gestures in elements without supported actions.
3. **onValueChange:** On user edit, first update the current binding then dispatch declared action. Action must observe edited typed value, not previous snapshot. Avoid duplicate action on recomposition/restore. Do not change current max-length policy.
4. **onFocus:** Dispatch declared action only on transition to focused (`false → true`); not on blur, unrelated recomposition or restoration. The first genuine user focus gain counts; do not eagerly synthesize an action during initial composition. When action absent, no dispatch.
5. **onSubmit:** Dispatch declared action on explicit keyboard IME Done/Go/Send submission, not on focus loss and not when an OTP segment merely becomes complete. Segmented OTP may submit through its underlying BasicTextField's explicit IME action when configured. No timer/implicit auto-submission. Avoid double submit for a single IME gesture.
6. Maintain exactly one declared action per hook and existing `DynamicAction` → `DynamicIntent.Action` → `DynamicStore` → `DynamicActionExecutor` chain. No new payload keys/DTO category.
7. Event semantics apply only where a renderer provides a meaningful native event. **Do not** invent focus/submission behavior for decorative elements. Text span links remain independently clickable.

## Exact production targets and restrictions

- `feature/dynamic/.../sdui/element/ButtonElement.kt`: declared click + long click, use an existing Compose combined-click primitive with proper opt-in if required; preserve color/layout/background and button interaction.
- `feature/dynamic/.../sdui/element/TextElement.kt`: enable element-level click/long-click around text without suppressing clickable span links; if event precedence cannot be demonstrated, stop for narrow decision before editing.
- `feature/dynamic/.../sdui/element/ImageElement.kt` and `DecorationElements.kt`: declared click/long-click on image/icon surfaces; preserve image loading, content and spacing.
- `feature/dynamic/.../sdui/content/ElementContent.kt`: augment existing click decorator with declared long-click, preserve icon/divider behavior.
- `feature/dynamic/.../sdui/element/InputElement.kt`: onFocus gain, explicit IME onSubmit for regular and segmented input, current onValueChange. Review focus targets so keyboard focus applies to the actual BasicTextField.
- Existing `feature/dynamic/src/commonTest` and `data/src/commonTest` tests: map five hooks; event-to-store ordering, single-dispatch and no duplicated action. UI interaction tests require appropriate Compose host test capabilities; JVM unit tests alone do not establish native gesture/IME behavior.
- `DynamicStore.kt`: **keep by default**. If tests demonstrate old binding snapshot despite ordered callbacks, propose the smallest reproducible fix separately; no speculative Store rewrite.
- Unchanged: `DynamicActionExecutor`, action handlers, `DynamicSession`, `DynamicNavigationState`, `ApplicationContent`, DTO/domain wire schema, backend, W5 rendering, and all deferred State/Present/Sequence semantics.

## Test and acceptance cases (tests first)

1. DTO/domain maps each of the five hooks unchanged.
2. Declared click only; absent hook emits no action; Text span click still dispatches its own span action.
3. Declared long click dispatches once and does not also dispatch click for same long-press gesture.
4. Input edit updates binding before executing `onValueChange` action; no repeated dispatch on redraw.
5. Focus gain fires once; loss and recomposition do not fire `onFocus`.
6. IME Done/Go/Send submits once only when declared. Four/six-digit completion alone is not submit; segmented input explicit IME submit dispatches when declared.
7. Existing OTP interaction/keyboard behavior and body rendering remain unchanged.
8. Owner verification after production changes: relevant JVM tests + `:app:compileKotlinWasmJs :androidApp:assembleDebug` once; manual Android/Web interactions for native gesture, focus and IME; iOS native verification deferred to macOS. Never claim runtime PASS from JVM-only tests.

**No Kotlin production edits, additional modules, backend migration, new branches or builds authorized by this plan document.** Next step is owner review and separate explicit implementation approval. If platform Compose gesture/IME semantics conflict with the above plan, document the exact conflict before coding further.
