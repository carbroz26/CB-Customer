# 82 — Current State, Logging Discussion and Next Chat Handoff

**Date:** 2026-10-16. **Status:** HANDOFF + DISCUSSION RECORD; **NOT A LOGGING IMPLEMENTATION PLAN OR FREEZE**.
**Frontend:** https://github.com/carbroz26/CB-Partner — branch `architecture/dynamic-runtime-v2-discussion-freeze`.
**Original backend:** https://github.com/carbroz26/CB-Backend — `development`, READ ONLY absent separate authorization.
**Locally running backend reported by owner:** `D:\CarBroz\CB-Backend-New\carwash-backend`; do not conflate its source/runtime or migration readiness with the original `CB-Backend` repo.

## Historical decisions, precedence and guardrails

Follow `AI_START_HERE.md`, project-management documents and source truth. Main V2 freeze: 19/20; canonical two owner Login/OTP JSON fixtures described in 12; subsequent specific amendments/actions 60–81 take precedence over contradictory historical prose. Do not resurrect executable Sequence from document 20: **latest owner decision keeps Sequence registered and inert**, returning Completed with no children or effects. The State action wire semantics are unresolved/deferred (71), and Present further semantics deferred (69/70). Request and N1 modal-only dismiss implemented; resolver duplication removed, obsolete EffectFailed path removed; existing batching and error boundaries preserved. No ViewModel, second HTTP client, duplicate navigation owner, generic managers, unneeded layers, extra branch, or speculative abstractions. Strict SRP; typed six-category DTO/domain/renderers, source-and-tests first. Source and docs inconsistencies must be reported, not silently corrected.

A feature changes only through DISCUSS → RESEARCH → DECIDE → EXACT FILE PLAN → FREEZE → SEPARATE IMPLEMENTATION AUTHORIZATION → TEST → REVIEW → FREEZE. This document **does not authorize Kotlin/network/backend refactoring**.

## Project and actual verified source inventory

Android entry `androidApp` supplies platform runtime metadata and BuildConfig API URL; `webApp` is Wasm/browser boundary; `iosApp/` Xcode boundary. `app` composes Splash/bootstrap, session navigation/effects and Dynamic container. `core` provides shared runtime metadata/values and existing Kermit dependency; `network` has `ApiBaseUrl`, `NetworkModule`, `http/HttpClientBuilder`, `http/RequestHandler`, `NetworkException`, `NetworkResult`; `domain` contains Bootstrap/Dynamic repositories and typed six-category models, action/destination/value contracts; `data` owns Bootstrap and Dynamic remote sources, repositories, DTOs, exact category mappers and DI; `navigation` owns single navigation state/entry/host; `feature/splash` owns `SplashStore` loading, retry, maintenance and required-update gate; `feature/dynamic` owns dynamic UI renderers, registry, screen Store, action handlers and runtime references/bindings.

Existing real class paths (inspect actual methods before changing):
- `network/http/RequestHandler.kt`: common Ktor request, reserved metadata/bearer headers, typed HTTP/timeout/connection failure, cancellations; recently added safe Kermit summary events.
- `network/http/HttpClientBuilder.kt`: HTTP client configuration; `NetworkModule.kt`: dependency wiring.
- `data/bootstrap/BootstrapRepositoryImpl.kt`, `BootstrapRemoteDataSource.kt`, `BootstrapMapper.kt`: bootstrap fetch/parse/typed mapping.
- `feature/splash/SplashStore.kt`: loading, retry, sanitization, successful handoff state.
- `navigation/DynamicNavigationState.kt`, `DynamicNavigationHost.kt`, `DynamicNavigationEntry.kt`: one session navigation authority and entry identity.
- `feature/dynamic/store/DynamicStore.kt`, `DynamicScreenState.kt`: intent/reducer/action results and loaded screen state; `runtime/DynamicRuntimeState.kt`: retained typed state; `DynamicReferenceResolver.kt`: $binding/$context/$state/$literal resolution.
- `feature/dynamic/action/DynamicActionExecutor.kt` + `DynamicActionResult.kt` + individual `RequestActionHandler`, `NavigateActionHandler`, `PresentActionHandler`, `DismissActionHandler`, `StateActionHandler`, `SequenceActionHandler`, `ExternalUriActionHandler`: registered one-action dispatch and effects; reconcile latest specific frozen decisions for each.
- `feature/dynamic/registry/DynamicRegistry.kt`, `DynamicRendererRegistration.kt`; `sdui/template/FormTemplate.kt`, `sdui/component/StackComponent.kt`, `sdui/section/StackSection.kt`, `sdui/group/StackGroup.kt`, `sdui/element/*`, `sdui/content/*`: existing rendering concrete categories.
- `data/dynamic/mapper/{Template,Component,Section,Group,Element,ElementContent,DynamicAction,DynamicResponse,DynamicValue}Mapper.kt`; typed DTO and domain peers.
These are navigation pointers, **not a complete line-by-line audit or guaranteed method-level behavior**.

## Canonical JSON / wire contract

**Do not manufacture a replacement fixture**; read the full original two Login/OTP JSON fixtures and document 12; the actual full original user JSON is not reproduced here. Existing envelope: `{status,code,message,data:{screenId,schemaVersion,targetApp,template:{id,type,properties,components},theme?},traceId?}`. Exactly one template; multiple Components allowed. Template → Components (1..n); Component → Sections (0..n) and Elements (0..n); Section → Groups (0..n) and Elements (0..n); Group → Elements (1..n); Element → leading/trailing Content (not Elements). Concrete FormTemplate, StackComponent, StackSection, StackGroup, Text/Input/Button/Image/Icon Element, Icon/Divider Content. Optional header/footer are additive template siblings; template.components remains body; independent slot padding. No DividerElement. Existing templateIds for fixtures: Login `tpl_7K2M9Q`, OTP `tpl_P102O`. Fixed destination shape `{screenId,templateId,templateType,endpoint}`. No automatic destination method/auth. Existing actions/events/reference namespace; latest Sequence inert and State deferred override old general aspiration. Asset URLs + fallback, spacing, gradient stops, input/OTP detail require exact source/doc reconciliation; no frontend hardcoded visual defaults.

Minimal illustrative **fragment only** (not substitute for owner full fixture):
```json
{"status":200,"code":"SUCCESS","data":{"screenId":"partner_login","schemaVersion":"...","targetApp":"...","template":{"id":"tpl_7K2M9Q","type":"form_template","properties":{},"components":[{"id":"brand_content","type":"stack_component","properties":{},"elements":[]}]} }}
```
Omitted values here are intentionally unspecified and are not a deployable sample.

## Backend migration constraints

Read 19, 33 and 34 in full: M01 optional template header/footer, M02 URL-driven assets, M03 content spacing, M04 gradient/text/OTP preservation, M05 actions and refs, M06 four-field destination frozen; M07 OTP session/token envelope, M08 refresh/logout, M09 protected screen authentication, M10 partner onboarding state **proposed/open**, not falsely deployed. Backend source `CB-Backend` read-only. The user's separate **CB-Backend-New/carwash-backend** currently responds to headers `X-CarBroz-Platform: ANDROID`, `X-CarBroz-App-Version`, `X-CarBroz-Build-Number`; browser without platform gives 500 `Missing update config for undefined` from `GetPartnerBootstrapUseCase.ts`, whereas curl with Android headers returned 200 with startup destination `partner_login`, `tpl_7K2M9Q`, `form_template`, `/api/v1/partner/screen/auth_login`. Never assume these two backend repositories share state or completed migrations.

Onboarding product context: OTP → mandatory Aadhaar KYC → service selection → video training → optional practical training slot → dashboard; online/offline availability only after onboarded. Business/API flags are pending contract decisions; not front-end hardcoded semantics.

## Verified runtime and git work

Owner verified JVM/Wasm suite after C1 (doc 81), and Web Bootstrap → Login. Samsung A35 Wi-Fi Android Bootstrap → Login succeeded after build with `-Pcbpartner.api.baseUrl=http://127.0.0.1:3000` and `adb reverse tcp:3000 tcp:3000`. Direct LAN Wi-Fi access was not fixed; ADB serial connection port changes. Root `README.md` now documents actual ADB-reverse, build/install/launch and Antigravity terminal Logcat commands.

A corrupt local remote ref caused failed fetch and stale APK; owner deleted **only the loose remote-tracking ref**, fetched and fast-forwarded HEAD from `1d26ab40` to `6f67e567`; `git fsck --full --no-reflogs` completed with four dangling commits, not active corruption. Owner reported two actual Android Kermit app logs: `I/CBPartnerNetwork method=GET outcome=success status=200 category=none durationMs=268` and another GET 200/58ms using new wireless port `192.168.0.102:37989`. The earlier `192.168.0.102:44905` disconnected; always `adb devices -l`, avoid stale fixed port. Source additions were network Kermit dependency, safe status/category/duration summary and README; owner verified actual Android logs, but separate broader regression evidence for this logging commit is not recorded here. SLF4J NOP warning is separate from Kermit logs.

## CURRENT WORK: developer logging usability; **NOT FROZEN OR AUTHORIZED**

Owner requests a **much clearer, concise, glance-readable** per-event debugging stream in Antigravity: correlated API request/response, method, success/failure, elapsed, relevant structured JSON, request headers/query/body, redirection from source screen to target screen, backend-returned destination (screenId, templateId, templateType, endpoint), action handler/method phases, Store state outcome, and category-based colors/format. The design should explain flow without overwhelming output. Owner asks to research whether a ready-made viewer/tool/plugin (for Android Logcat, Antigravity terminal, desktop log viewer, tracing) meets needs instead of building own complex tool. **No tool selected or tested yet.** Kermit summary logs currently work; not sufficient for desired flow tracing.

**Existing frozen safe-logging policy (doc 23):** all environments prohibit raw request/response bodies, phone, OTP, tokens, device ID, arbitrary URL/query, and untrusted server messages. Development safe operational diagnostic permitted; staging info; production warn/error. Do not silently overwrite this rule because owner says unpublished/local. Discuss a narrowly approved exception, if desired, as an explicit separate decision and document exact data lifetime/threats before any implementation. Prefer allowlisted structured projections/field names and [REDACTED] values, never plaintext credentials/identity. Multiline prettified JSON and terminal colors may be viewer-side features rather than Kermit writer; Logcat arbitrary RGB ANSI colors are not guaranteed. Avoid blanket 'all values' raw dump, adding parallel logging framework, SLF4J provider without evidence, or indiscriminate logger insertion into every method.

**Next task exclusively:** read actual code and docs; research suitable existing tools for colorized viewing/correlation; sketch **two or three short realistic sample traces** (Bootstrap→Login success, Login→OTP navigate, HTTP failure), show a clear minimal event schema and owner choices for verbosity/redaction/color. Audit current Kermit writer/config, Logcat and Web behavior, method/transition ownership, no duplicate logging by RequestHandler and handler. Return short source-backed options and an exact file-by-file **discussion draft**, NOT code changes. Freeze after explicit owner approval; implementation must be separately authorized. No background tasks.

## Mandatory reading order

1. `AI_START_HERE.md`, `README.md`, `docs/README.md`, `docs/project-management/03-DECISION-LOG.md`, `07-DEVELOPMENT-WORKFLOW.md`.
2. V2 docs `01–06`, `08–16` including 12; docs `17,19,20,22,23`; migration `33,34`.
3. Wave history `24–55` as needed for ownership; **action decisions/evidence 56–81 all**, especially 60 latest amendment, 64–70, 71, 75/76, 77–81.
4. This doc 82 and fresh source HEAD, classes and tests. Read actual complete files rather than accepting this handoff as a code audit.

## Source-change protections

Only active frontend architecture branch; no extra branch/PR, no Git destructive reset/rebase; no backend edits; no edits just to satisfy a brainstorm; check current branch/HEAD/diff and repo status before anything. For approved source tasks only: enumerate callers, exact files, expected behavior/test matrix, no source contract changes without owner freeze, minimal code, KDoc if necessary, regression tasks + Android/Web runtime gates. Do not claim success without results. Conversations in Hindi/Hinglish, docs and code comments in English.
