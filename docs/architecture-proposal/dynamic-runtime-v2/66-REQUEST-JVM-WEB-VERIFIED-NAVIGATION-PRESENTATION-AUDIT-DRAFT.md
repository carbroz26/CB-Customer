# 66 — Request JVM/Web Verification and Navigation/Presentation Source Audit

**Status:** Request JVM/Web acceptance GATES VERIFIED BY OWNER LOG; Android/Web runtime smoke pending. Navigation/Presentation AUDIT DRAFT — NO PLAN FREEZE, NO KOTLIN CHANGES. Frontend `architecture/dynamic-runtime-v2-discussion-freeze`. Backend migration deferred, Sequence inert.

## Verification evidence (owner-run)
At frontend revision `9d76a2eb`, owner performed fast-forward and ran `./gradlew.bat :feature:dynamic:jvmTest :domain:jvmTest :data:jvmTest :navigation:jvmTest :webApp:wasmJsBrowserDevelopmentWebpack` successfully: BUILD SUCCESSFUL in 1m19s, 137 tasks (28 executed, 109 up-to-date). `git diff --check` yielded no reported errors. Windows iOS simulator disable and npm/webpack warnings were non-blocking. This is not a claim of on-device Android or browser smoke.

## Request action reconciled
- R1 responseMode none/destination and default none; R3 Request push/replace only; R4 retain mapping validated before HTTP and source path after HTTP; typed explicit null retained; R2 Store validates resolved flag and fails closed without loaded form; R5 Request failure preserves screen/bindings and displays dismissible safe message; EffectFailed also preserves loaded screen; R6 single Request result can contain retained state and navigation effect, state committed before effect delivery.
- Tests covering invalid mode/no HTTP, retained path missing/null, malformed retain/no HTTP, Store reference validation with true context and no loaded form, request failure usable state, effect failure usable state, and state-before-navigation effect succeeded in owner JVM suite.
- Existing tests for a **real populated invalid form with resolved validate true** and browser/Android smoke may still be useful; do not overstate complete visual/runtime coverage.
- Backend source/contract not changed. `SequenceActionHandler` remains independently registered inert Completed.
- No additional Request refactor approved.

## Navigation and Presentation source audit — scoped initial inventory
Read-only files examined: `feature/dynamic/action/NavigateActionHandler.kt`, `PresentActionHandler.kt`, `DismissActionHandler.kt`, `DynamicActionExecutor.kt`, `DynamicActionResult.kt`, `navigation/DynamicNavigationState.kt`, `app/ApplicationContent.kt`.

### Observations
1. `NavigateActionHandler` uses missing mode => Push, explicitly supports push/replace/pop/back, invalid unknown mode rejected, Push/Replace require destination; Pop does not. It returns typed result and does not mutate navigation stack. SRP good.
2. `PresentActionHandler` requires `action.destination`, returns Present result; `DismissActionHandler` returns Dismiss. SRP good at handler boundary.
3. `DynamicNavigationState` is single stack/modal authority and uses entry identity; present rejects second modal; dismissPresentation rejects absent modal. `pop()` dismisses modal before popping stack; this is an existing navigation policy, not owned by handlers.
4. `ApplicationContent.DynamicContent` session effect consumer applies `Dismiss -> dismissPresentation() || pop()`. Thus an explicit `dismiss` may pop a screen when no modal, contrary to owner's expressed intent that dismiss closes a dialog. **Do not change until specific owner approval**. Also top-level `back` is currently remapped to `dismiss` in ActionExecutor, which can conflate navigation with presentation; frozen W4 held this alias pending compatibility evidence. Need source/fixture tests and separate decision.
5. Session consumer has `Channel<List<DynamicActionEffect>>` and a nested effect iteration. With inert Sequence and single-result effect lists of zero/one, may be unnecessary complexity. Keep until complete producer/consumer and cancellation/source-disposal audit; no second queue or manager.
6. Session effect errors are surfaced in `ApplicationContent.effectError`; Store's `DynamicIntent.EffectFailed` is separately available but not clearly emitted by this consumer. Clarify actual error authority to avoid duplicate reporting and raw messages.
7. Existing Request may produce Navigate result after HTTP, while NavigateHandler represents explicit navigate: no handler-to-handler call; architecture good.
8. Backend `presentAction(targetId,presentation)` and `dismissAction(targetId?)` authoring differs from frontend handlers requiring destination on Present; **wire mismatch** requires source/fixtures reconciliation before any fix. Backend stays unchanged.

## Next audit plan (NOT FROZEN)
- Read actual `DynamicActionDto`, mapper, backend Present/Navigation contracts and fixtures, `DynamicNavigationHost`, navigation/feature tests and exact session effect ownership.
- Map cases: explicit Navigate(push/replace/pop/back), Present/Dismiss with/without modal, top-level `back`, root rejection, Request-returned destination, URI side effects, effect handoff after entry disposal.
- Deliver exact file-by-file MODIFY/KEEP/REMOVE plan for owner's review, preserving strict Clean Architecture/Pure MVI/SRP, no ViewModel/second navigator, and no Backend migration.
- After Navigation/Presentation scope is frozen, audit State wire mismatch, then shared Executor/Result/Resolver separate scope.
