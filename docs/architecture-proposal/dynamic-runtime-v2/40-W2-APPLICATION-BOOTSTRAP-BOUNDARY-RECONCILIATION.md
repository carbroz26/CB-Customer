# 40 — W2 ApplicationBootstrap Boundary Reconciliation

**Status:** Source-level decision record; **NO production source changes authorized by this audit**. Branch: `architecture/dynamic-runtime-v2-discussion-freeze`.

## Evidence reviewed

- `domain/bootstrap/ApplicationBootstrap.kt`: one-method interface `suspend operator fun invoke(): Result<BootstrapOutput>`.
- `domain/repository/BootstrapRepository.kt`: one-method domain interface `suspend fun fetch(): Result<BootstrapOutput>`.
- `data/di/BootstrapModule.kt`: `single<ApplicationBootstrap>` is an anonymous object delegating `invoke()` directly to `repository.fetch()`.
- `feature/splash/SplashStore.kt`: sole production caller of `ApplicationBootstrap` in inspected startup code; consumes Result, owns loading/retry and maintenance/update gate.
- `feature/splash/SplashModule.kt`: Koin `factory { SplashStore(get()) }`.
- `feature/splash/SplashStoreTest.kt`: tests create anonymous ApplicationBootstrap fakes (success, retry, maintenance priority, required update, optional update, safe failure).
- `app/ApplicationContent.kt`: composes and closes SplashStore on app teardown and successful bootstrap handoff, transitions only on Success.
- `docs/features/splash-bootstrap/SPLASH-BOOTSTRAP.md` §2: calls Domain ApplicationBootstrap + BootstrapRepository boundary **frozen Option B**.
- `docs/architecture-proposal/dynamic-runtime-v2/17-PASS6-EXACT-IMPLEMENTATION-BLUEPRINT-DRAFT.md` W2: remove trivial use-case only if no policy, with caller/test migration.
- `20-PLAN-FROZEN-V2-IMPLEMENTATION-CONTRACT.md` §5(6): same evidence-based removal rule for trivial Bootstrap remote source / conditional use case; no speculative classes.
- W2 audits `09`, `36`, `39`: characterize anonymous use-case forwarding as possible overengineering; defer deletion until conflicting authority resolved.

## Finding and recommended decision

```text
Current:
SplashStore -> ApplicationBootstrap.invoke() -> BootstrapRepository.fetch()
Proposed:
SplashStore -> BootstrapRepository.fetch()
```

**Source finding:** `ApplicationBootstrap` has no policy, mapping, persistence, coordination or other independent responsibility. Its Koin anonymous implementation is a pure delegation and duplicates the domain repository's single operation. Startup decisions (maintenance, required update, generic error presentation) are already in SplashStore, and transport/DTO mapping are in Data. A direct Domain repository dependency does **not** couple Splash to Data/Ktor.

**Recommendation (requires owner confirmation of frozen boundary revision):** Remove `domain/bootstrap/ApplicationBootstrap.kt` and its `single<ApplicationBootstrap>` DI binding; inject `BootstrapRepository` in SplashStore and call `bootstrap.fetch()`; migrate all SplashStoreTest fakes to BootstrapRepository. Do not add a replacement wrapper, named use case, adapter, extra Koin binding or an equivalent interface. The public `BootstrapRepository`, `BootstrapOutput`, existing four-field destination, maintenance/update gating and retry semantics remain unchanged.

**Why not delete immediately:** `docs/features/splash-bootstrap/SPLASH-BOOTSTRAP.md` explicitly freezes the current use-case→repository boundary; owner must confirm whether V2 conditional cleanup supersedes that exact boundary before deleting a production Domain interface. Existing 20 plan's conditional cleanup is not unconditional authorization. Document the supersession or preservation decision once approved.

## Exact expected migration edits if approved

1. `feature/splash/src/commonMain/.../SplashStore.kt`: `import ...domain.repository.BootstrapRepository`; parameter type `BootstrapRepository`; call `bootstrap.fetch()`.
2. `data/src/commonMain/.../di/BootstrapModule.kt`: remove `ApplicationBootstrap` import and anonymous `single<ApplicationBootstrap>` registration; keep existing single BootstrapRepository, mapper and source.
3. `domain/src/commonMain/.../bootstrap/ApplicationBootstrap.kt`: delete only after repo-wide caller check.
4. `feature/splash/src/commonTest/.../SplashStoreTest.kt`: replace anonymous ApplicationBootstrap with BootstrapRepository fakes and `invoke()` with `fetch()`; preserve all regression tests.
5. `docs/features/splash-bootstrap/SPLASH-BOOTSTRAP.md`, implementation manifest/state: explicitly reconcile previous Option B frozen call graph with approved direct repository form; no parallel stale contract.

## Remaining W2 scope / evidence gates

- **No Android/Web smoke executed:** successful startup and blocked maintenance/update and retry need real platform verification.
- **Required Update:** `storeUrl` is currently held in RequiredUpdate state but no trusted, platform-supported external store action is implemented. Verify allowed HTTPS host(s), app store scheme(s), fallback/error UX and approve link-launch behavior before binding it. Never launch arbitrary server URL.
- **Lifecycle:** `LaunchedEffect(splashStore,splashState)` calls close on Success; `DisposableEffect` closes on composition disposal. Verify compositional timing and no cancelled navigation/runtime work on Android/Web; no proof from SplashStore JVM tests alone.
- **Startup error:** generic UI failure avoids exception-message leaks; narrow safe telemetry and message localization remain options, not an excuse to add another error hierarchy.
- **W2 hygiene:** safe KDoc/log policy (23), imports, dependencies, unused DI and test callers; keep one Ktor client.
- If cleanup approved, run `:network:jvmTest :data:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest`, `git diff --check`, platform smoke; Windows disabled iOS task is not an iOS verification.

## Decision state

**Current outcome: AUDITED, RECOMMEND REMOVE, PENDING OWNER APPROVAL TO SUPERSEDE FROZEN SPLASH BOUNDARY.** No source rewrite, class deletion, API migration or tests falsely claimed by this record.
