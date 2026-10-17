# W6-A — Keyboard Dismiss / Focus Behavior Extension

**Owner approval:** 2026-10-17 explicitly authorized freeze and focused implementation.
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`, source baseline `e3be45446b8b616a213e26f78e85ac4406c42f82`.

## Frozen behavior
1. Explicit IME Done/Go/Send clears input focus, hides soft keyboard where supported, then dispatches existing `onSubmit` once when declared. Last segmented OTP digit does not auto-submit or auto-dismiss.
2. Tapping outside input on otherwise unhandled screen content clears focus. The screen-level gesture modifier must not consume child tap/click or introduce a second child action. Child controls retain their own click handlers.
3. Input may regain focus after dismissal, emitting one focus-gain action on a genuine new transition; blur itself does not emit `onFocus`.
4. Web hardware keyboard cannot be hidden by Compose; clear focus/blur is the cross-platform contract, while software-keyboard hide is best effort on platforms supporting it.
5. No backend, DTO, Store, Executor, Session, Navigation or action semantics changes.

## Implementation boundaries and limitations
- `InputElement.kt`: Compose `LocalFocusManager` and `LocalSoftwareKeyboardController` on existing submit callback. Preserve existing submit dispatch order apart from explicitly approved focus/keyboard dismissal.
- `DynamicRenderer.kt`: existing root `Box` adds screen-level `detectTapGestures(onTap)` for unconsumed/background taps. This does **not** guarantee that clicking every independently consuming child control also clears focus; that interaction requires manual verification and potentially a separate targeted decision, not a broad gesture manager. The implementation must not swallow Button/Text actions.
- Existing isolated `W6EventPreview.kt` remains temporarily enabled to check Android/Web blur, refocus, Done and other hooks.
- No new manager, bridge or UI-state layer.

## Required verification
Owner: `:feature:dynamic:jvmTest :app:compileKotlinWasmJs :androidApp:assembleDebug`; Android/Web manually verify Done clears focus/soft keyboard where applicable, background taps blur, refocusing triggers `onFocus` once again, children remain clickable, and OTP completion does not submit. Do not claim Web soft-keyboard hide if keyboard is physical, nor claim completion solely from compilation.
