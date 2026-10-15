# 41 — W2 Owner-Approved Direct BootstrapRepository Migration

**Decision:** OWNER APPROVED removal of the redundant `ApplicationBootstrap` interface/forwarder (2026-10-16). **Status:** Kotlin and docs committed, local build/tests pending.

## Why
`ApplicationBootstrap.invoke()` contained no policy, simply forwarded to `BootstrapRepository.fetch()`. Its extra Domain interface, anonymous Koin binding, and test doubles duplicated the same contract. The SplashStore already owns Loading/Failure/Retry/Maintenance/RequiredUpdate decisions. Domain's BootstrapRepository remains the correct interface; no new wrapper is introduced.

## Exact changes
- `feature/splash/.../SplashStore.kt`: change constructor parameter type to Domain `BootstrapRepository`, call `bootstrap.fetch()`; keep cancellation, maintenance/update gating and safe UI failure unchanged.
- `feature/splash/.../SplashStoreTest.kt`: migrate all fake ApplicationBootstrap implementations to BootstrapRepository and `fetch()`; preserve existing test assertions.
- `data/.../di/BootstrapModule.kt`: delete anonymous `single<ApplicationBootstrap>` forwarding registration, retain `single<BootstrapRepository>`, remote and mapper.
- **Delete** `domain/.../bootstrap/ApplicationBootstrap.kt` with approval.
- `docs/features/splash-bootstrap/SPLASH-BOOTSTRAP.md`: explicitly supersede former frozen Option B wrapper chain with approved single repository boundary.

## New call graph
```text
SplashStore → BootstrapRepository (Domain) → BootstrapRepositoryImpl (Data)
    → BootstrapRemoteDataSource → RequestHandler / Ktor
    → Bootstrap DTO → Mapper → BootstrapOutput / DynamicDestination
```

## Compatibility and cleanup
- No backend/API JSON, bootstrap endpoint, feature flags, typed failure, screen destination or Token/Bearer behavior altered.
- No Koin duplicate implementation, no new class, dependency, logger, or HTTP client.
- Approval resolves conflict documented in `40-W2-APPLICATION-BOOTSTRAP-BOUNDARY-RECONCILIATION.md`; 20 frozen V2 conditional simplicity rule honored by explicit owner approval.
- `BootstrapRemoteDataSource` retained since removing that is a separate audit decision; Android/Web smoke, trusted storeUrl and W2 remaining tests still open.
- Not claiming JVM or platform validation until user runs commands.

## Validate locally
```powershell
cd D:\CarBroz\CB-Partner
git status --short
git fetch origin --prune
git merge --ff-only origin/architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :network:jvmTest :data:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest
git diff --check
```
