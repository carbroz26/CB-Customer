# 42 — W2 Required Update URI / Platform Reuse Source Audit

**State:** SOURCE AUDIT + CONTRACT RECOMMENDATION ONLY; **no Kotlin implementation or tests claimed**. Branch `architecture/dynamic-runtime-v2-discussion-freeze`.

## Source and authority reviewed
- `feature/dynamic/action/ExternalUriActionHandler.kt`: existing `external_uri` resolves payload `uri` from `$binding/$context/$state/$literal`, returns `DynamicActionResult.ExternalUri`; it does **not** validate trusted hosts or open a link.
- `feature/dynamic/DynamicContainer.kt`: `LocalUriHandler.current.openUri(effect.uri)` executes existing external-uri **inside the Dynamic screen effect collector**. This is not available to Splash because Splash is **outside DynamicContent**.
- `feature/splash/SplashStore.kt`: `SplashState.RequiredUpdate(storeUrl: String?)` is emitted from existing BootstrapOutput, no URL validation or launch action.
- `feature/splash/SplashScreen.kt`: required update currently renders localized title/description and no button. `SplashStatus` handles blocked states as `Unit`, so no action. Startup state is guarded by ApplicationContent Success-only navigation.
- `data/bootstrap/model/BootstrapDtos.kt` + `BootstrapMapper.kt`: `update.storeUrl` is nullable and passed unchanged to `UpdateConfig.storeUrl`, without validation. `update.required` checked for presence, but not `storeUrl` consistency.
- `app/ApplicationContent.kt`: owns Splash composition and Dynamic transition. Existing `LaunchedEffect` closes SplashStore after Success; `DisposableEffect` handles app disposal. It currently has no shared app-level external URI action parameter.
- `androidApp/MainActivity.kt` and `webApp/Main.kt`: platform entry points supply runtime metadata/API origin. Neither contains a dedicated update link launcher.
- `docs/architecture-proposal/dynamic-runtime-v2/05-NAVIGATION-AND-PLATFORM-LIFECYCLE.md`, `20-PLAN-FROZEN-V2-IMPLEMENTATION-CONTRACT.md`, `23-W0-ADDENDA-FROZEN-KDOC-LOGGING-ENVIRONMENTS.md`, `34-BACKEND-MIGRATION-MASTER-CONTRACT-AND-READINESS.md`, W2 records 36–41 and `docs/features/splash-bootstrap/SPLASH-BOOTSTRAP.md`.

## Finding: reuse platform capability, not Dynamic action dispatch
The existing **Compose `LocalUriHandler`** is a cross-platform URI opening mechanism already used in DynamicContainer; it can also be **consumed directly from the Splash presentation**. Do **not** invoke `ExternalUriActionHandler` from Splash, synthesize Dynamic actions, use DynamicStore/Navigation/effect machinery, introduce a URLManager or add another Android Intent/Web window opener. Dynamic's action handler is for backend SDUI actions; required-update is application startup policy. Reusing the Compose **platform adapter** does not mean reusing the Dynamic **action executor**.

Suggested minimal path after contract approval:
```text
Bootstrap update.required + storeUrl
 -> BootstrapMapper / trusted update-link validation policy
 -> SplashState.RequiredUpdate(storeUrl)
 -> SplashScreen button on explicit user tap
 -> LocalUriHandler.current.openUri(validatedStoreUrl)
```
The current nullable `storeUrl` needs a **safe fallback UI**: if absent/invalid, keep the update hard gate, show a useful explanation, and do **not** open or fall back to an untrusted URL. An exception from URI open must not crash Splash or expose server URL/token; show localized safe message or action unavailable, according to approved UX. Never auto-open external store just because backend set `required:true`.

## Pending wire/security decisions before launch implementation

1. **Trusted destinations:** approve per-platform official store hosts and canonical app listing/ID. Android Play Store HTTPS listing, iOS App Store HTTPS listing, and Web app deployment/download URL (if relevant) must be verified/owned by product. Reject dangerous schemes (`javascript:`, `data:`, `file:`, `intent:`) and userinfo, non-HTTPS URLs, arbitrary redirects, mixed encodings. Redirect destination policy matters, not only initial host.
2. **Who owns validation:** API/Bootstrap DTO Mapper should validate semantic update config without importing Compose/platform code; app/splash boundary may enforce platform-specific allowlist immediately before opening. Avoid duplicate validator abstraction; a small focused policy with tests suffices if needed.
3. **Required vs optional update:** mandatory update blocks Dynamic even if link absent; optional update should not block (currently Success). No new UI mode unless product approves.
4. **Precedence:** currently `maintenance.enabled` wins over `update.required`; this was a W2 implementation default, not owner-frozen product priority.
5. **Error UX:** local safe strings for missing/invalid link/open failure; optional Retry Bootstrap only if product wants checking again after app upgrade. Do not assume URL open implies upgrade completed.
6. **Security isolation:** `LocalUriHandler` itself does not certify link safety; dynamic `external_uri` input currently has no verified allowlist and must be audited separately in W6. Do not broaden this W2 change into blanket Dynamic URI contract modifications.
7. **Backend migration doc 34:** record final approved store URL origin/shape and conditions once available. Do not insert invented hostnames into the active backend contract.

## Android/Web smoke test matrix (manual evidence pending)
- Android, backend success flags false: Splash → Login Dynamic screen; no obsolete splash job continues.
- Android, maintenance=true: only maintenance copy; no Dynamic screen; no URI open.
- Android, required=true: update copy and hard gate; **after implementation**, user-tap verified official store listing; missing/invalid URL safe; no automatic launch.
- Android, bootstrap failure: safe generic copy and retry.
- Web/Wasm: repeat Splash → Dynamic, maintenance, required and failure/retry; browser popup policy may constrain URI open, hence only user-gesture open.
- Optional update=true: normal startup remains.
- Verify Koin/Compose lifecycle and HTTP calls using platform logs without OTP, URLs, device identifiers or tokens.
- Run `:network:jvmTest :data:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest`, `:webApp:wasmJsBrowserDevelopmentWebpack`, Android build/run and iOS/macOS later. **No Android/Web/iOS smoke performed in this audit.**

## W2 final source cleanup register
- ApplicationBootstrap wrapper removed with owner approval (document 41); SplashStore uses Domain BootstrapRepository directly.
- BootstrapRepositoryImpl double decode/error layer simplified; cancellation regression tested in earlier JVM suite.
- BootstrapRemoteDataSource is thin but endpoint ownership and caller still real; removal only if separately justified, not automatically.
- SplashStore default coroutine scope and Compose handoff close implemented; actual platform lifecycle smoke still required.
- Public API KDoc: BootstrapRepository has responsibility documentation; SplashStore and BootstrapMapper lack comprehensive lifecycle/mapping KDoc; add focused KDoc with a future behavior change or W2 final sweep, not boilerplate per function.
- Existing Splash failure message generic but hardcoded in Store; may localize in UI; avoid new error hierarchy.
- No redundant HTTP client or external opener observed in inspected sources. Full repo-wide unused imports/dependencies/platform smoke cannot be certified from this focused audit.

## Next decision (implementation gate)
**Recommendation:** reuse `LocalUriHandler` in SplashScreen following explicit user tap, **but do not wire it to arbitrary backend `storeUrl`** until product confirms trusted store destination IDs/hosts and fallback/open-failure UX. A small fixed allowlist + tests is sufficient; no new abstraction beyond proven need. No Kotlin source change in this audit.
