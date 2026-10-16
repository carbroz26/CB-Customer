# 64 — Request Action: OWNER-APPROVED PLAN FROZEN

**Status: PLAN_FROZEN — Kotlin implementation NOT YET AUTHORIZED.**
**Owner approval:** 2026-10-16 owner approved R1, R2, R4, R5, R6 of freeze candidate 63 and specified that Request mode pop/back is not required now. R3 is frozen as push/replace only. This is a narrow supersession of Request-specific behavior, not a backend migration. Branch `architecture/dynamic-runtime-v2-discussion-freeze`.

## Frozen decisions

**R1 — responseMode:** Only `none` and `destination` supported. Missing -> `none` (matching Backend action author's default). Explicit null, wrong type or unsupported value -> validation error before HTTP. `none` returns Completed/StateChanged, no navigation. `destination` requires a backend-returned destination and emits one navigation effect.

**R2 — validate:** Missing -> false; literal or resolved Boolean true/false supported. Explicit null, wrong type, unresolved/non-Boolean -> error before HTTP. Store alone owns pre-request form validation using current document and bindings. `validate:true` with unavailable screen -> reject, not skip. Request Handler resolves/forwards the typed flag in existing DynamicRequest but does not introduce UI-form dependency.

**R3 — Request mode (owner clarification):** Missing -> Push for `responseMode:destination`; explicit `push` and `replace` supported. Explicit `pop`/`back`, unknown value, null or wrong type -> invalid Request action *before the HTTP request*. Navigation's separate `navigate` action may still handle pop/back. For `responseMode:none`, reject an explicitly supplied navigation mode rather than accept an ignored field. Do not add Request pop/back. Do not alter NavigateActionHandler.

**R4 — retain:** Missing -> no retention. Present retain must be object with valid nonblank target keys and nonblank string source paths. Missing source path causes controlled failure; present explicit null remains `DynamicValue.NullValue`. Never silently skip bad mappings. Failure after HTTP means no new retained state or navigation emitted; completed network operations are not rolled back. No invented optional mapping syntax.

**R5 — errors:** Failed Request after a screen has loaded must retain the existing screen, bindings and usable form; display sanitized recoverable error in-place with dismiss/retry affordance, and clear on new attempt/success. Never render raw exception, credentials, URI or backend error message. Initial load failure without loaded screen remains distinct.

**R6 — architecture:** Keep existing Domain DynamicRequest/Response and Repository interface pending separately justified mismatch; preserve Typed Result with optional navigation effect, state-before-effect, session-owned consumer and existing one NavigationState. Backend `CB-Backend/development` unchanged. Sequence remains independent inert placeholder; future cross-element and timer contract deferred. No ViewModel, extra managers, second router, new modules.

## Frozen exact implementation scope (file-by-file)

1. `feature/dynamic/action/RequestActionHandler.kt` — strict payload/response mode/request mode/retain contract; keep HTTP via DynamicRepository; no direct navigation.
2. `feature/dynamic/store/DynamicStore.kt` — request-only prevalidation for literal/resolved `validate` flag, safe in-place action errors without discarding form; preserve cancellation and state-first result handoff.
3. `feature/dynamic/runtime/DynamicReferenceResolver.kt` — minimal presence-aware resolution *only if required* to distinguish retain source missing/null.
4. `feature/dynamic/action/RequestActionHandlerTest.kt`, `feature/dynamic/store/DynamicStoreTest.kt` — add tests before fixes for R1–R5 including no API call on malformed configuration/invalid form, backend destination Push/Replace, explicit null and missing retain, failure and cancellation.
5. `data` mapper/DTO tests — only minimal additions if source-backed mismatch arises. Keep Domain DynamicRequest/Response, DynamicActionResult, DynamicRepositoryImpl, DynamicModule, ApplicationContent, remaining handlers, Sequence placeholder unchanged without specific gated evidence.

## Implementation/test gates
- Tests first, minimal source changes in coherent commits. Show each real Kotlin change and short Hindi explanation in chat.
- Local owner verification: `./gradlew.bat :feature:dynamic:jvmTest :domain:jvmTest :data:jvmTest :navigation:jvmTest :webApp:wasmJsBrowserDevelopmentWebpack`, `git diff --check`, then Android/Web smoke. No false claim of remote builds.
- If Backend actual response contradicts destination/retain schema, stop and report, do not invent adapter or migrate backend.
- **Freeze is planning approval only; do not edit Kotlin until separate explicit implementation instruction.**
