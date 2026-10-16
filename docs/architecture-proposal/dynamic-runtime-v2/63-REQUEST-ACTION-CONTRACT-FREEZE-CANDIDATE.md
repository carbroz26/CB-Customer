# 63 — Request Action Contract Freeze Candidate

**State: FREEZE CANDIDATE / OWNER REVIEW REQUIRED — NOT PLAN_FROZEN; NO KOTLIN CHANGES**
**Scope:** Request Action only. Backend CB-Backend/development stays unchanged; inert Sequence unchanged; other Action handlers unchanged unless a later separately frozen scope explicitly requires it.
**Basis:** source-level Request audit and exact file plan doc62, frontend implementation, backend development branch `sdui/engine/src/core/Action.ts`, PartnerLoginScreen and PartnerOtpScreen.

## Candidate decisions (R1–R6)

**R1 Response mode** — backend author emits explicit `none` by default and explicit `destination` for Login/Verify OTP. Candidate: optional/missing `responseMode` means `none`; explicit allowed `none` or `destination`; any other value/type/null is rejected before HTTP. Preserve `destination` requiring returned destination; `none` does not navigate. Only after owner approval alter frontend fallback; do not change backend.

**R2 Validate** — candidate: absent validate => false, explicitly true/false literal or successfully resolved Boolean; null/wrong type => invalid action before HTTP. Store owns form validation because handler has no screen; handler resolves its own typed flag for DynamicRequest without duplicating validation execution. Clarify when loaded screen is unavailable: fail closed for validate:true, not silently skip.

**R3 Mode** — backend RequestActionOptions currently has no Request mode field. Candidate: if missing, `push` for destination-bearing Request compatibility; if supplied, support `push/replace/pop/back` with Navigate W4 strict typing. However `pop/back` with response destination is semantically dubious: require owner decision whether to forbid such modes on Request until backend contract exposes them. No silent fallback on malformed values.

**R4 Retain** — candidate: absent retain => empty; present must be an object map with nonblank target keys and nonblank string paths; missing source path => controlled failure; existing explicit `null` remains `DynamicValue.NullValue`. Atomic result: if retain invalid, do not commit retained values or navigate; already-executed HTTP cannot be rolled back. No optional retain syntax invented.

**R5 Errors** — failed Request before a usable document load can show load-error UI; failed action after screen successfully loaded must preserve existing form and bindings and show a generic safe recoverable message. Clear on retry/new attempt or explicit dismiss; do not display exception.message or network token. Avoid introducing a second error manager or new ViewModel.

**R6 Domain/wire boundaries** — preserve `DynamicRequest` shape and `DynamicRepository` boundary until usage reconciliation. Current backend Request author requires method/endpoint/authentication and explicitly emits responseMode. Candidate: do not silently change unrelated domain defaults. Confirm destination mapper four frontend fields versus backend route destination extra method/auth fields separately and STOP if actual payload cannot be mapped; do not invent fields. Backend source unchanged.

## Implementation wave and exact files

A. Targeted failing unit tests: `feature/dynamic/action/RequestActionHandlerTest.kt`, `feature/dynamic/store/DynamicStoreTest.kt`, and minimal data fixture coverage only when warranted.
B. `feature/dynamic/action/RequestActionHandler.kt` — validate request payload and response interpretation in one handler, no actual Navigation side effect; keep other action handlers untouched.
C. `feature/dynamic/store/DynamicStore.kt` — one request-aware form-validation precondition; generic in-place safe action error; no repeated validators or new manager.
D. `feature/dynamic/runtime/DynamicReferenceResolver.kt` — only minimal presence-aware path API if R4 approved; use same resolver. `DynamicActionValueResolver` cleanup is NOT part of Request gate.
E. `DynamicRequest.kt`, `DynamicActionResult.kt`, `DynamicRepositoryImpl.kt`, mapper/DTO, `DynamicModule.kt`, `ApplicationContent.kt` — KEEP unless a failing source-backed test demonstrates required localized adaptation.
F. Test gates `:feature:dynamic:jvmTest :data:jvmTest :domain:jvmTest :navigation:jvmTest :webApp:wasmJsBrowserDevelopmentWebpack`, diff --check; Android/Web smoke separately. Report actual code changes and snippets in Hindi after each coherent commit.

## Stop/approval gate
This is NOT a freeze and does not authorize Kotlin edits. Owner must accept R1–R6 including Request mode Pop/Back scope and the error presentation policy; then mark plan frozen and authorize implementation. Do not mark tests passed from source reading. Do not modify Backend or resurrect Sequence child execution.
