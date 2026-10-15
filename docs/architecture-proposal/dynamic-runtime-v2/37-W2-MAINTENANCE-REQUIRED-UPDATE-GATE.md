# 37 — W2 Maintenance and Required Update Startup Gate

**Status:** Implementation and focused tests committed, local JVM compilation/tests pending. Branch `architecture/dynamic-runtime-v2-discussion-freeze`. Frozen V2 §6 W2; 02/09 startup audit; 23 source hygiene and safe logging; 36 lifecycle trace.

## Exact production changes and behavior
- `feature/splash/.../SplashStore.kt`: add `SplashState.Maintenance(title,message)` and `SplashState.RequiredUpdate(storeUrl)`; on successful Bootstrap result, evaluate **maintenance.enabled first**, then **update.required**, otherwise Success. Existing domain models and Bootstrap DTO unchanged. `update.optional` does not block startup. This state mapping is deliberately simple and lives in startup Store, not Network or Domain.
- `feature/splash/.../SplashScreen.kt`: existing Splash UI renders maintenance title/message (with localized fallback if blank), or localized required-update description; `SplashStatus` displays neither spinner nor retry for blocked states. No dynamic screen created from these states because `ApplicationContent` already transitions only on SplashState.Success.
- `feature/splash/src/commonMain/composeResources/values/strings.xml`: four new fallback localized source strings.
- `feature/splash/.../SplashStoreTest.kt`: test maintenance beats required update, required update blocks ready, optional update allows normal ready.
- `app/.../ApplicationContent.kt` unchanged in this change, retains Success-only transition and previously tested DisposableEffect store close.

## Priority/policy limitations to resolve
- **Maintenance over required update** is an explicit implemented default for this slice, not documented as owner-frozen order. Product may prefer required update first; if so freeze the precedence and adjust one decision branch and test.
- `storeUrl` is preserved in state but an external-store-open button is deliberately not implemented until backend URL trust validation, supported platform link opening, and approved update UX are defined. Required update is a hard gate, not a stuck loading indicator.
- No scheduled auto-retry/polling for maintenance; relaunch/retry policy remains open. Failure still exposes current error.message; sanitized failure UI belongs later W2 hardening.
- Backend still needs accurate flags; no inferred minimumVersion comparison was added.
- Authenticated boolean is not treated as a secure session.

## SRP/cleanup
No new manager, use case, transport, repository, DTO, DI registration, Gradle dependency, or logger. All added properties are read by Store/UI/test; resource imports used. No raw Bootstrap body, store URL or token logged. UI strings in resource file, no new KDoc on unchanged abstraction. W2 is **not complete**: startup lifecycle handoff, error sanitization, possible pass-through wrapper removal, error translation dedup, platform store link integration and cross-target verification remain.

## Local verification
```powershell
cd D:\CarBroz\CB-Partner
git status --short
git fetch origin --prune
git merge --ff-only origin/architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :network:jvmTest :data:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest
git diff --check
```
Only owner device run can establish JVM test success; Windows iOS-disabled warning does not mean iOS validated.
