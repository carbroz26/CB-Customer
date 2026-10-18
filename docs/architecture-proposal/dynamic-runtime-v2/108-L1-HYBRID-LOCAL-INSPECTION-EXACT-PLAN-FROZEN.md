# 108 — L1 Hybrid Local Inspection: Exact Implementation Plan FROZEN

**State:** EXACT L1 PLAN FROZEN by owner's hybrid-approach approval (2026-10-18). **NO IMPLEMENTATION AUTHORIZATION**; no runtime/source changes permitted by this document alone.
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze` only. Backend READ ONLY. Governing decisions: Doc23 operational safe logs + Doc107 guarded local inspector. No new Gradle modules, HTTP transport, logging facade, navigation owner, or ViewModel.

## User-visible contract and placement

- **Terminal:** existing Kermit `CBPartnerFlow`/`CBPartnerNetwork` operational events, retained within Doc23 privacy constraints; PowerShell text colors, no background. L1 does not change the viewer.
- **Android in-app developer inspector:** a read-only, dismissible panel ABOVE the normal dynamic screen (owned at app composition, not a backend-driven template/component/element). It is unavailable outside eligible local Android development/debug sessions. Only `SANITIZED SDUI INSPECTION` content; NEVER imply raw server JSON or display plaintext OTP/tokens/private fields.
- The panel shows an explicit status (`Inspection unavailable`, `Inspection off`, `Inspector ready / awaiting sanitized snapshot`); L1 **does not yet collect or render actual SDUI JSON**. JSON extraction and actual HTTP/DTO snapshots belong to approved later L2/L3. No fake fixture/demo content in a live panel.

## L1-A: Pure eligibility contract

**NEW** `core/src/commonMain/kotlin/com/carbroz/cbpartner/core/diagnostics/LocalInspectionEligibility.kt`
- One pure function (or minimal immutable value type), no side effects or manager. Inputs: `environment: AppEnvironment` (network type is not available in :core: avoid reverse dependency by taking validated Boolean `isDevelopment` supplied by composition), `debuggable: Boolean`, `verifiedLocalBackend: Boolean`, `optedIn: Boolean`. Result `Boolean` only. All inputs must be true. No defaults enabling inspection. No URL string, auth, session data or payload passed to helper.
- `verifiedLocalBackend` is a **proven assertion supplied by a platform-specific trusted development configuration**, not a URL-pattern guess, DNS name, app-client IP, ADB reverse appearance or header sent by the backend. If proof cannot be established, pass `false`, keeping the inspector inaccessible.
**NEW** `core/src/commonTest/kotlin/com/carbroz/cbpartner/core/diagnostics/LocalInspectionEligibilityTest.kt` — exhaustive 16 Boolean combinations plus negative transitions, all disabled unless all conditions true. No network tests in core.

## L1-B: Android-only read-only UI shell and ownership

**EDIT** `androidApp/src/main/kotlin/com/carbroz/cbpartner/android/MainActivity.kt`
- Supply `isDevelopment` from `AppEnvironment.Development`; `debuggable` from `BuildConfig.DEBUG`. Default `optedIn=false`; explicit opt-in is a local, session-only developer gesture within eligible UI (not an exported Intent extra, deep link, persistent preference or remote configuration). No cleartext secrets or value logging.
- Backend verification gate remains `false` unless an auditable local-run trust proof is supplied and separately approved. The current `127.0.0.1:3000` and `adb reverse` arrangement proves routing but does not authenticate server identity. Do not ship a bypass claiming localhost is verification.
- Construct eligible local-inspection capability at Android startup; do not alter retention-probe behavior, logging severity, ApiBaseUrl parsing or dynamic navigation.

**EDIT** `app/src/commonMain/kotlin/com/carbroz/cbpartner/app/App.kt`
- Add a single explicit immutable local-inspection eligibility input (default OFF for non-Android callers); do not register a universal manager in Koin or inject it into Domain/Data/Network at L1. Scope in-memory enabled state to the existing Compose App lifecycle; clear on retirement/disposal. No exported or persisted state.

**EDIT** `app/src/commonMain/kotlin/com/carbroz/cbpartner/app/ApplicationContent.kt`
- Host a read-only inspector panel overlay at app composition (separate from `DynamicNavigationHost` and `DynamicRenderer`). Visibility depends on current eligibility and active session opt-in; never intercept or rewrite SDUI action handling while closed. Normal bootstrap, navigation, loading and render execution remains identical when inspection OFF.

**NEW** `app/src/commonMain/kotlin/com/carbroz/cbpartner/app/LocalInspectionPanel.kt`
- Minimal dismissible Compose UI with status only, an explicit opt-in control for eligible Android development sessions and a read-only placeholder for future **sanitized** snapshots. No persistence, capture API, clipboard/export/file/save/share control, HTTP call, logger, source JSON parsing, or new runtime-state manager. Must not display values merely because they exist in `DynamicRuntimeState`. No default visual styling changes to backend-driven screen; overlay styling belongs only to local developer tooling.
- L1 panel is not a delivery sink for original JSON. L2/L3 must explicitly define capped, allowlisted inspection DTOs and memory lifetime before the panel can consume snapshots.

**NEW/EDIT** focused existing app tests (exact preference after test inventory): `app/src/commonTest/kotlin/com/carbroz/cbpartner/app/LocalInspectionAccessTest.kt` (pure panel eligibility boundary/default OFF and disposal semantics if testable without Compose UI test infrastructure). Platform Android manual test for debug overlay and absence in stage/prod/release; no speculative new test dependency.

## Hard boundaries

- **NOT L1:** `RequestHandler.kt`, `DynamicRepositoryImpl.kt`, mappers, `DynamicStore.kt`, `DynamicRenderer.kt`, `DynamicContainer.kt`, action callbacks, `DynamicRegistry.kt`, Web/iOS implementation, PowerShell parsing, raw/sanitized payload generation. Do not modify these to make L1 look functional.
- Existing `app/LoggingConfiguration.kt`, `network/ApiBaseUrl.kt`, `network/NetworkModule.kt`, Android manifests unchanged.
- Web/iOS follow-up gets separate platform-specific eligibility and interaction scope after Android proof; do not assume `BuildConfig.DEBUG` exists on Web/iOS.
- No source changes to Doc23 constraints: **NO raw HTTP request/response bodies, OTP, phone, Aadhaar, credentials, bearer tokens, device IDs or arbitrary URLs in Kermit/Logcat**. Future sanitized inspectable JSON is allowed ONLY in the guarded ephemeral in-app surface with separate per-field approval.
- Failure to prove server identity means `verifiedLocalBackend=false`; no local-only inspection activation even when build is debuggable and developer opted in. This is a deliberate unresolved security gate, not a claim that this phase already delivers complete JSON.

## Source hygiene and acceptance

1. Pull same branch; verify HEAD and clean worktree. No new branches/PRs.
2. Implement **L1-A** only, inspect full diff and direct callers, run `:core:jvmTest`; ensure all 16 input combinations and never-enabled default; no :core→:network dependency.
3. Implement **L1-B** only after source review and proof/config approach owner approval if any non-off inspector access is expected. Inspect Compose retention/state lifetime, old retention probe and Android/flavor settings. Run `:app:jvmTest :androidApp:assembleDevelopmentDebug :webApp:wasmJsBrowserDevelopmentWebpack`; existing app tests and `git diff --check`.
4. Android A35 runtime: baseline bootstrap/login/OTP unaffected, overlay absent until all gates pass; no clipboard/file/Logcat payload. Verify staging, production, release variants cannot activate. Fail-closed when backend moves remote. iOS runtime remains unverified on Windows.
5. Do not mark the functionality completed with tests alone: owner reviews actual source and Android evidence, confirms local trust proof and later L2/L3 sanitized payload contract.

## Freeze and next gate

**Frozen:** hybrid presentation split, L1-A exact eligibility helper/tests, L1-B Android/app overlay shell only, zero new logging transport or raw data. **Open:** evidence-backed local backend identity verification and secure sanitized snapshot acquisition/size budget (L2/L3). **Next action:** separate explicit owner implementation authorization for L1-A; L1-B implementation cannot bypass the open trust gate.
