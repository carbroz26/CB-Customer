# 43 — W2 Final Source Audit and Verification Register

**Status:** SOURCE REVIEWED; focused KDoc hygiene committed; W2 **NOT FULLY VERIFIED** (Android/iOS deferred, Web URI launch pending). Branch: `architecture/dynamic-runtime-v2-discussion-freeze`. This document reconciles W2 against 17/19/20/23, migration handoff 34 and W2 records 36–42.

## Current code findings (verified by source, not inferred from names)

| Source | Actual responsibility | Audit decision |
|---|---|---|
| `feature/splash/SplashStore.kt` | Single startup Store; Load/Retry → Domain BootstrapRepository; Maintenance before RequiredUpdate; generic safe failure; scope cancel | **KEEP**. Add accurate responsibility/lifetime KDoc. Do not create second coordinator. |
| `feature/splash/SplashScreen.kt` | Compose startup display; static maintenance copy; static required-update AlertDialog; `LocalUriHandler` invocation on tap | **KEEP**. Future backend-driven UI separately specified. Dialog appearance and URI open still need device/web runtime checks. |
| `data/bootstrap/BootstrapRepositoryImpl.kt` | One remote fetch, decode, mapper invocation; catches serialization/cancellation/transport | **KEEP**. Prior duplicated nested runCatching removed. |
| `data/bootstrap/mapper/BootstrapMapper.kt` | Validate required config and destination fields; map existing output | **KEEP** explicit guards. These checks are required-wire validation, not needless duplication. Add responsibility KDoc. |
| `data/bootstrap/remote/BootstrapRemoteDataSource.kt` | Single GET bootstrap endpoint via shared RequestHandler | **KEEP** at W2 exit: endpoint-specific boundary (no alternate HTTP stack); deleting is separate owner/source decision, not justified by only short line count. |
| `data/di/BootstrapModule.kt` | BootstrapRemoteDataSource + BootstrapMapper + one Domain BootstrapRepository binding | **KEEP**. Removed anonymous ApplicationBootstrap registered in doc41. |
| `feature/splash/SplashModule.kt` | One SplashStore factory | **KEEP**. No duplicate startup registration. |
| `app/ApplicationContent.kt` | App consumes startup state; SplashStore close on Success and disposal; Dynamic navigation handoff | **KEEP** pending platform checks. |
| `SplashStoreTest.kt`, Data tests | Success, maintenance priority, update hard gate, retry, safe failure; data mapper/transport tests | **KEEP** and expand only for observed gap. |

## Source hygiene applied in this wave
- `SplashStore`: cross-module API KDoc defines lifecycle owner and safe error behavior, reordered imports.
- `BootstrapMapper`: KDoc states required validation, mapping and non-responsibilities.
- No code behavior change, dependencies, HTTP client, duplicate renderer, new classes or platform adapter introduced in this reconciliation.
- No misleading source log lines were added. KDoc addendum 23 applies to future edits too.

## Web verification supplied by owner
1. Web/Wasm development bundle **BUILD SUCCESSFUL**, webpack one warning; no build-failure claim.
2. Normal Splash → Dynamic Login **PASS**.
3. `maintenance.enabled=true`: Maintenance copy and Login blocked **PASS** using Chrome Local Overrides.
4. `update.required=true`, `storeUrl=null`: static modal, Login blocked and no Play Store action **PASS**.
5. Bootstrap **application-envelope failure over successful HTTP**, safe generic failure; toggling Local Overrides OFF, user Retry succeeded **without reload** → Login **PASS**.
6. Real transport or HTTP 500 error runtime **NOT tested** by overriding an HTTP 200 JSON envelope.
7. Valid trusted store link and actual URI opening on Android/Web **NOT tested**. No real approved backend URL/listing yet.
8. Android/iOS runtime smoke **DEFERRED** per owner. iOS disabled simulator task on Windows is expected, not an iOS pass.

## Remaining contract/risk register
- **Cross-platform store links:** shared `isTrustedUpdateUrl` currently recognizes one exact Android Play listing string. It is a *shape* derived from application ID, not a verified published listing. Web/iOS update destination policy, platform authority, redirects and fallback are unapproved. Do not mark this as cross-platform ready or silently relax URL policy. Backend migration doc 34 §12/M14 remains active.
- **Static UI:** backend supplies only maintenance/update flags/copy, not a V2 Dynamic UI document for update/maintenance. Existing static UI is intentional temporary fallback, not fabricated wire structure.
- **Startup precedence:** Maintenance outranks RequiredUpdate in current source and tests. Owner has not frozen broader product precedence; document as current behavior.
- **Lifecycle:** App's LaunchedEffect closes store on Success; no startup scope should outlive handoff. JVM coverage and Web success do not certify Android/iOS lifecycle behavior, process recreation or mid-flight cancellation.
- **Failure fallback:** generic literal in SplashStore is sanitized, but localization still belongs in presentation when owner asks for that UX slice; no new error hierarchy justified.
- **Data error categories:** thrown unknown errors become BootstrapFailure.Transport; existing NetworkResult.Error is passed unchanged. Contract evolution must be evidence-driven, preserve cancellation.
- **Potential UI cleanup:** MaterialTheme for AlertDialog is currently outside `CarBrozSplashTheme` wrapper; verify actual theme appearance before changing code. Avoid speculative visual changes.
- **KDoc/logs:** no unsafe logging introduced. Logging environment setup is W7, not W2 independent logging infrastructure.

## Frozen plan alignment / exit
- W2 source architecture: **aligned** with frozen 20 as amended by owner-approved wrapper deletion (41).
- Backend independence: **aligned** with 19 and 34 M14; no backend modified.
- No new generic infrastructure: **aligned**.
- Android/Web/iOS integration: Web subset **confirmed**; Android and iOS **deferred**, URL click **unverified**.
- **Disposition:** W2 focused Kotlin scope can be considered source-reviewed, but overall W2 acceptance remains **PARTIAL**, not fully frozen or cross-platform validated. Next step is platform smoke when accessible, plus approved final store URL contract.

## Post-KDoc-change local verification
```powershell
cd D:\CarBroz\CB-Partner
git status --short
git fetch origin --prune
git merge --ff-only origin/architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :network:jvmTest :data:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest :webApp:wasmJsBrowserDevelopmentWebpack
git diff --check
```
Do not claim these tests have run after this commit until user provides local output.
