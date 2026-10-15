# 38 — W2 Startup Error Sanitization (Focused Fix)

**Status:** source and regression test committed; local test execution pending. Branch: `architecture/dynamic-runtime-v2-discussion-freeze`.

## Contract and source review
Frozen V2 doc 20 W2 and safe-logging addendum 23 require failure UI to be recoverable without exposing raw payload, server errors, credentials or URLs. W2 audit docs 36–37 identify `SplashStore` as the startup UI-state owner. Existing failure taxonomy is already defined as `BootstrapFailure` (API/Transport/Serialization/etc.) and `NetworkException`; no additional exception hierarchy is justified. Actual `BootstrapRepositoryImpl` can propagate network exception messages and backend `BootstrapFailure.Api.message` to the Store.

## Narrow implementation
- **Before:** `SplashStore` used `error.message ?: "Unable to start the application"` to construct `SplashState.Failure`. `SplashScreen` displays `state.message` directly. Server/URL/error details could reach the UI.
- **After:** SplashStore converts any non-cancellation bootstrap failure to the known, safe, non-sensitive fallback `SplashState.Failure("Unable to start the application")`. Coroutine cancellation still propagates, and retry still works. The existing `Failure` UI state is reused; no new status types, DTOs, handler, dependency or DI registration. Screen consumes the same state. This is intentionally a generic message, not a claimed detailed user-action diagnosis.
- New `SplashStoreTest.bootstrapFailureDoesNotExposeServerOrExceptionDetails` checks `BootstrapFailure.Api`, `BootstrapFailure.Serialization` and an unexpected exception with URL/token-like strings. Asserts exact generic failure state for each; no raw details.
- No new logging and no raw response/URL/token output. Existing BootstrapFailure/NetworkException types remain available to repository/tests and future safe telemetry; no domain-to-network dependency added.

## Remaining considerations (not silently included)
- The literal default UI message is preexisting in SplashStore; moving it to Compose localized string resources can be handled in a deliberate UI-localization slice without creating another error abstraction.
- Maintenance title/message are *intended backend-authored copy*, not exception text; HTML/script and unsafe text presentation still depend on backend trust, but this patch does not rewrite maintenance messaging.
- Consider per-category safe localized messages later only if product UX actually needs them, using existing error types, not a new failure framework.
- No fix to startup job handoff, required-update store button, duplicate Bootstrap adapter, or serialization pipeline; W2 not complete.
- Do not confuse a passing JVM suite with Android/Web/iOS integration verification.

## Local verification
```powershell
cd D:\CarBroz\CB-Partner
git status --short
git fetch origin --prune
git merge --ff-only origin/architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :network:jvmTest :data:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest
git diff --check
```
