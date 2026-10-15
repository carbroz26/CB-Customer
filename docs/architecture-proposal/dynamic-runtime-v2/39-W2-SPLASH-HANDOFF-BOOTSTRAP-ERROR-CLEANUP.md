# 39 — W2 Startup Handoff and Bootstrap Repository Cleanup

**Status:** production changes and a focused regression test committed; JVM/platform tests NOT RUN in this GitHub session. Branch `architecture/dynamic-runtime-v2-discussion-freeze`.

## Authority and source inspected

Follow frozen `20-PLAN-FROZEN-V2-IMPLEMENTATION-CONTRACT.md` W2 as amended by 19, `17-PASS6-EXACT-IMPLEMENTATION-BLUEPRINT-DRAFT.md`, `23-W0-ADDENDA-FROZEN-KDOC-LOGGING-ENVIRONMENTS.md`, bootstrap source audit 09 and W2 records 36–38. Inspected current SplashStore, SplashScreen, SplashModule, ApplicationContent, BootstrapRepositoryImpl, ApplicationBootstrap, BootstrapModule and repository test source.

**Existing chain:** ApplicationContent remembers SplashStore from Koin; SplashScreen starts Bootstrap via LaunchedEffect(Unit). SplashStore owns a private cancellable scope; successful bootstrap state is exposed as Success to ApplicationContent, which builds DynamicContent. Previously DisposableEffect closed startup Store *only when the whole App composition was disposed*, even after successful Dynamic transition.

## Changed production files

1. `app/src/commonMain/kotlin/com/carbroz/cbpartner/app/ApplicationContent.kt`: Add a short `LaunchedEffect(splashStore, splashState)` that calls `splashStore.close()` when state is `SplashState.Success`. Existing `DisposableEffect` is retained for app teardown during Loading/Failure/Maintenance/RequiredUpdate; close/cancel on success is idempotent under existing scope cancellation. Success-only DynamicContent gate unchanged. No new startup controller, session manager or lifecycle class. **Caveat:** Compose ordering and post-transition process lifecycle still require Android/Web smoke validation, not solely JVM unit tests.
2. `data/src/commonMain/kotlin/com/carbroz/cbpartner/data/bootstrap/BootstrapRepositoryImpl.kt`: Remove duplicate `mapResponse` helper with inner `runCatching/fold`; decode and map once within existing `fetch()` try/catch. Handle `CancellationException` first and rethrow, `SerializationException` to existing `BootstrapFailure.Serialization`, other Throwable to existing `BootstrapFailure.Transport`, preserve downstream `NetworkResult.Error` unchanged and mapper's typed failure result. No raw exception messages copied into new error reasons. Bootstrap response/status/config/destination contract untouched.
3. `data/src/commonTest/kotlin/com/carbroz/cbpartner/data/bootstrap/BootstrapRepositoryImplTest.kt`: add cancellation propagation test using MockEngine CancellationException; existing success/malformed/null-data/503 tests remain.

## Explicit deferred cleanup decisions

- `ApplicationBootstrap` wrapper currently one-line delegates to BootstrapRepository, but `docs/features/splash-bootstrap/SPLASH-BOOTSTRAP.md` calls that boundary frozen (Option B), while 17/09 suggest remove trivial forwarding only when owner agrees. **No deletion or DI relocation without resolving that authority conflict.**
- `BootstrapRemoteDataSource` is a small feature endpoint wrapper; preserve until verified caller/responsibility tests and same-authority review. Do not duplicate client.
- `SplashState.Success` carries output and Dynamic entry remains four-field destination. No token/session is inferred from `authenticated` boolean.
- Required update store URL opening, optional update UX, safe failure localization and W7 environments remain pending.
- No new dependencies, runtime registration, logging or generated model. Added imports are used. No new unused classes. No backend changes.

## Validation required

```powershell
cd D:\CarBroz\CB-Partner
git status --short
git fetch origin --prune
git merge --ff-only origin/architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :network:jvmTest :data:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest
git diff --check
```

After JVM pass run Android + Web smoke for successful startup, blocked maintenance/update and failure/retry, plus iOS on macOS eventually. Do **not** mark W2 complete until user-supplied build/verification.
