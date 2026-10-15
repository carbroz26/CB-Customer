# 36 — W2 Bootstrap/Splash Source Reconciliation and Lifecycle Slice

**Status:** source/caller/test audit plus one focused App lifecycle change committed. **No local compilation on GitHub; tests pending.** Branch `architecture/dynamic-runtime-v2-discussion-freeze`.

## Authoritative contracts read
- `20-PLAN-FROZEN-V2-IMPLEMENTATION-CONTRACT.md` §1,§5–6 and draft manifest `17-PASS6-EXACT-IMPLEMENTATION-BLUEPRINT-DRAFT.md` as amended by 19/20.
- `23-W0-ADDENDA-FROZEN-KDOC-LOGGING-ENVIRONMENTS.md` hygiene/logging; `02-BOOTSTRAP-NETWORK-AND-CONTRACTS.md`; `09-NETWORK-BOOTSTRAP-SPLASH-SOURCE-AUDIT.md`; `docs/features/splash-bootstrap/SPLASH-BOOTSTRAP.md`.
- Production `BootstrapDtos.kt`, `BootstrapMapper.kt`, `BootstrapRepositoryImpl.kt`, `BootstrapRemoteDataSource.kt`, `BootstrapModule.kt`, `BootstrapOutput.kt`, `BootstrapFailure.kt`, `SplashStore.kt`, `SplashScreen.kt`, `ApplicationContent.kt` and mapper/repository/store test files.

## Actual source path and gaps
```
ApplicationContent -> SplashStore -> ApplicationBootstrap (anonymous delegator)
 -> BootstrapRepositoryImpl -> BootstrapRemoteDataSource
 -> RequestHandler -> GET /api/v1/partner/config/bootstrap
 -> BootstrapResponseDto -> BootstrapMapper -> BootstrapOutput
 -> (any successful response) DynamicContent
```
1. **P0 behavior gap:** BootstrapMapper validates `maintenance.enabled`, `update.required`, `update.optional` and maps those values into BootstrapOutput, yet SplashStore's successful result always becomes `SplashState.Success` and ApplicationContent always transitions to DynamicContent. Maintenance and required-update flags are **not enforced**; needs explicit startup policy, priority/order, safe UI/callback tests before implementing (don't invent app-store URL behavior).
2. **Lifecycle gap corrected narrowly:** ApplicationContent remembered `SplashStore` without calling `close()` on app composition disposal. Added Compose `DisposableEffect(splashStore) { onDispose { splashStore.close() } }` to ensure cleanup. This disposes at App lifecycle end, **not immediately** at splash-to-Dynamic handoff; session/lifecycle cleanup still needs W2 final design. `SplashScreen` already starts bootstrap in LaunchedEffect(Unit); `SplashStore` checks active job to avoid duplicate loading.
3. `ApplicationBootstrap` in BootstrapModule currently anonymous one-method forwarding adapter into BootstrapRepository; draft 17 and doc 09 recommend removing trivial wrapper **only after complete consumer/test migration**; docs/features/splash-bootstrap currently calls Option B/frozen boundary with that abstraction. This is a **document authority conflict** to reconcile before deletion. No deletion now.
4. `BootstrapRepositoryImpl` has nested catch + `runCatching` error conversions, likely duplicate serialization/transport handling; preserve existing failures until equivalence tests and cleanup scope fixed.
5. `HttpClientBuilder` and serialization module configure Json separately, see doc 09 N02; transport and application decode ownership to be reconciled with W1.
6. Current `SplashState.Failure` displays error.message, which can contain server/transport text; must implement sanitized typed error display and logs to avoid secrets. No new sensitive logging.
7. Current `authenticated` flag only maps to BootstrapOutput; no secure credential provider exists and backend not yet issuing tokens. Bootstrap must **not** treat boolean as proof of authenticated session; see migration doc 34.

## Exact files changed
- `app/src/commonMain/kotlin/com/carbroz/cbpartner/app/ApplicationContent.kt`:
  - new import `androidx.compose.runtime.DisposableEffect`;
  - after `val splashState by splashStore.state.collectAsState()`, new effect:
```kotlin
DisposableEffect(splashStore) {
    onDispose { splashStore.close() }
}
```
  - All existing UI decisions unchanged; no added module/class/dependency/redundant import.
- This document only; no Kotlin change in Bootstrap DTO, mapper, repository, DI, SplashStore or SplashScreen.

## W2 next vertical tests/implementation plan
A. Add SplashStore tests for maintenance/required update and priority; implement explicit state derived from BootstrapOutput and UI that cannot accidentally navigate (requires approved UX handling of optional/required update and maintenance).
B. Ensure current App success handoff closes startup jobs and preserves session lifecycle; no double cancellation or shared-scope disposal. Add Compose/app lifecycle test where practical.
C. Simplify Bootstrap adapter, repository catch layers, duplicate Json configuration only after tracing full consumers and test ownership; avoid broad rewrite.
D. Test malformed/missing config, non-success codes, cancellation, store close, duplicate requests, retry, maintenance/required/optional update combinations, app launch handoff.
E. Per 23 audit KDoc, no raw metadata/response/token logs, remove unused imports and DI/dependencies, run `:network:jvmTest :data:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest`, check Android/Web; iOS macOS gate separate.

## Owner local validation
```powershell
cd D:\CarBroz\CB-Partner
git status --short
git fetch origin --prune
git merge --ff-only origin/architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :network:jvmTest :data:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest
git diff --check
```

**Do not mark W2 complete based on this slice.** No backend edits performed.
