# 160 — Application-wide Generic Dialog V1: Owner-approved implementation freeze

**Owner approval:** 2026-10-26 ("yes plan ko approved krta hu").
**Frontend:** `carbroz26/CB-Partner`, branch `architecture/dynamic-runtime-v2-discussion-freeze`, planning source checkpoint `077e6e8a685cfc2af8365c79fbf6b4a8462fd9ed`.
**Backend:** `carbroz26/CB-Backend`, branch `development`, **read-only for this entire Frontend implementation batch**.
**Status:** IMPLEMENTATION PLAN APPROVED. This document is a plan, **not** evidence of implemented/tested behavior.

## 1. Outcome and absolute constraints

The same reusable Dialog presentation must be available on **Splash, Login, OTP, Dashboard, KYC and future screens**; neither the renderer nor errors may belong exclusively to Splash. Support (a) backend-authored screen-scoped presentation via existing `data.presentations[]` and `present/dismiss`, (b) backend-authored immediate response-level `presentation`, and (c) local, safe network-error fallback when a response is unavailable. Six initial scenarios: App Under Maintenance; Update Required; Update Available; Connection Problem; Something Went Wrong; Secure Connection Error. Future scenarios reuse this infrastructure.

- **One new UI element only:** concrete `icon_badge` / `IconBadgeElement` (outer circle/background, centered existing image/source, optional border; no new asset loader). This is not an Avatar/profile concept.
- **One existing structural category enhanced:** `StackSection` gains exactly `background`, `shape`, `border`. Existing padding/orientation/arrangement remain; **no** Group-in-Group nesting, new Component, new Template, new Group or new ElementContent type.
- Existing `StackComponent`, `StackSection`, `StackGroup`, `TextElement`, `ImageElement`, `IconElement`, `ButtonElement`, `DynamicPresentationHost`, `DynamicAssetImage`, `DynamicRegistry`, `DynamicActionExecutor`, `PresentActionHandler`, `DismissActionHandler` must be reused.
- **All six cards vertical**, shared outer styling. Button row can be horizontal when two buttons. No separate native AlertDialog implementation per scenario.
- Optional supportive box: a **horizontal StackSection** with authored surface properties, leading IconElement, and a right **vertical StackGroup** containing independently styled 2–3 TextElements. Omit entire section when no info; no reserved blank space.
- Compose Multiplatform `commonMain` first, Clean Architecture, SRP, MVI/UDF, Pure Store, no ViewModel, reflection or duplicate networking/navigation/state framework, no hardcoded Dynamic UI styling defaults. Only explicitly authored JSON dictates visual sizes/colors/spacing.
- Do not edit approved Login v19, OTP v3 visual structures/IDs, Country Picker interaction, OTP countdown/resend/security, trusted store URL checks, Splash artwork, hero, logo or navigation identity. Source and latest approved V2 decisions win over historical documents; read/reconcile `README.md`, 157–159 and affected source first.
- **No Backend implementation in P0–P5.** Backend Bootstrap/Login/OTP migration is a **separate owner-accepted later batch** following Frontend acceptance.

## 2. Source-confirmed baseline and ownership

- `app/ApplicationContent.kt`: App root chooses Bootstrap/Splash versus DynamicContent. This is the legitimate cross-feature composition boundary; `feature/splash` must **not** depend on `feature/dynamic`. App may depend on both (already does).
- `feature/splash/SplashStore.kt`: Fetches Bootstrap, maps maintenance and required updates, currently uses generic failure text; optional updates are not shown yet. Its original successful `BootstrapOutput.destination` must be retained through Not Now.
- `domain/model/bootstrap/BootstrapOutput.kt`: maintenance enabled/title/message, update required/optional/minimumVersion/latestVersion/storeUrl all already exist. Production Bootstrap envelope is unchanged during Frontend-first testing.
- `feature/dynamic/DynamicContainer.kt` and `store/DynamicStore.kt`: screen-entry-scoped state and `activePresentationId`; existing Country Picker uses `PresentActionHandler` with `payload.presentationId`, then the existing host renders `DialogPresentation` or `BottomSheetPresentation`. Preserve origin/generation cancellation checks and `Dismiss`.
- `feature/dynamic/presentation/DynamicPresentationHost.kt`: renders `DialogPresentation` with generic Components, and separate existing BottomSheet behavior. Reuse for all six. Author dialog card surface on `StackComponent`, without adding a second renderer.
- `network/NetworkException.kt`: typed BadRequest, Unauthorized, Forbidden, NotFound, Conflict, Validation, RateLimit, Server, Connection, Timeout, Serialization, Protocol, Unknown. `RequestHandler.kt` maps HTTP and transport failures, currently drops HTTP error body into `NetworkResult.Error(null, ...)`; raw exception messages may be unsafe. Confirmed TLS/certificate classification is not yet distinguished.
- `data/dynamic/DynamicRepositoryImpl.kt`, `RequestActionHandler.kt`, `DynamicStore.kt`: action failure currently becomes “Unable to complete action”. Load failure becomes “Unable to load dynamic screen”. Do not confuse generic action outcome with authentication/field validation/rate-limit semantics.
- `app/LocalDynamicPreview.kt` + Android `androidApp/src/main/kotlin/com/carbroz/cbpartner/android/MainActivity.kt` and Web `webApp/src/wasmJsMain/kotlin/com/carbroz/cbpartner/web/WebDynamicPreview.kt`: existing opt-in development fixtures and OTP Resend success/failure preview. Extend these; do not create a parallel runtime.
- Backend `sdui/engine/src/core/SduiModel.ts` currently has strict `screenSchema` without V2 `presentations` support and legacy `present` wire shape. `ResponseHelper.ts` has no optional presentation. These are documented **P6 migration gaps**, not permission to edit Backend now.

## 3. Scoped presentation and ownership contract

Two **distinct origins, one UI renderer**:
1. **Screen-scoped** presentation is specified in an already loaded screen's `data.presentations[]` and opened/dismissed using existing `present/dismiss` and the existing entry-local Store; Country Picker behavior must remain identical. A service-specific maintenance dialog is scoped to its service/request and does not block the whole app.
2. **Response-originated** presentation is a typed outcome of the request that returned it (or validated network-error fallback); an app-wide blocking policy is elevated to App root only when the approved response code/policy explicitly makes it app-wide. The App root owns **at most one** global Dialog at a time; it must not replace every screen presentation with a singleton.

Common host:
```
ApplicationContent (Bootstrap/Splash or Dynamic entry)
  → existing operation owner (SplashStore / DynamicStore)
  → typed presentation + originating retry/dismiss callback
  → app overlay for genuine app-scope, or entry scoped presentation for screen-scope
  → existing DynamicPresentationHost
  → existing Component / Section / Group / Element / Content renderers
```

Keep action ownership with the originating Store/handler; **App never constructs arbitrary HTTP requests from dialog buttons**. Preserve entry/action generation checks and cancellation; ignore stale callbacks after navigation/dismiss. `Retry` is a **user-initiated** retry of an eligible operation with existing guards. Never auto-repeat send/resend/verify OTP, restart timers or replace challengeId after failure. App-wide blocker takes visual precedence over screen-scoped presentation and must not leave the underlying modal interactable.

## 4. Wire contract freeze

### 4.1 Existing screen-defined presentations (KEEP)

`data.presentations[]` with `{id,type:"dialog",properties,initialBindings,components}`; existing screen UI uses `{type:"present",payload:{presentationId:"..."}}`. Country Picker `bottom_sheet` unchanged.

### 4.2 Immediate API outcome (NEW **optional** response field for simulation, Backend later)

Envelope has existing `status`, `code`, `message`, `data`, `traceId` and optionally **top-level `presentation`** typed like a single existing `DialogPresentationDto`. Example:
```json
{
  "status": 503,
  "code": "SERVICE_MAINTENANCE",
  "message": "Service temporarily unavailable",
  "data": null,
  "presentation": {
    "id": "service_maintenance_dialog",
    "type": "dialog",
    "properties": { "dismissOnBack": false, "dismissOnOutsideClick": false },
    "initialBindings": {},
    "components": [
      {
        "id": "dialog_content",
        "type": "stack_component",
        "properties": {
          "orientation": "vertical", "fillMaxWidth": true,
          "background": { "color": "#FFFFFF" },
          "shape": { "type": "roundedCorner", "cornerRadius": 24 },
          "padding": { "all": 24 }
        },
        "sections": [
          { "id": "title_section", "type": "stack_section", "elements": [
            { "id": "title", "type": "text", "properties": { "text": "Service temporarily unavailable", "fontSize": 20, "color": "#12243D" } }
          ] }
        ]
      }
    ]
  }
}
```
Illustration only; six complete fixture JSONs must use the fully approved structure. Response-level `presentation` is **not** a Screen Template and is not silently inserted into the current screen's `data.presentations[]`. Absent field stays backwards-compatible. Do not accept simultaneous navigation + blocking response presentation as implicit behavior; reject/handle explicitly, with no hidden navigation.

### 4.3 Bootstrap unchanged for P0–P5

Current `data.config.maintenance.{enabled,title,message}`, `data.config.update.{required,optional,minimumVersion,latestVersion,storeUrl}` and `data.startup.destination` supply startup policy. No live Backend edit. Priority `Maintenance > Required Update > Optional Update > Normal Success`. Optional Update `Not Now` uses retained Bootstrap output/destination; no extra Bootstrap call. Required Update cannot Back/outside-dismiss; trusted store URL validation maintained. App version from runtime metadata, not invented response field. `startup.destination` remains the approved four-field Domain identity (`screenId,templateId,templateType,endpoint`); do not touch method/authentication reconciliation in this batch.

### 4.4 Network failure policy

Reuse existing `NetworkException` and transport. `Connection` / `Timeout` → Connection Problem; Server or eligible unexpected internal failures → Something Went Wrong; confirmed TLS/certificate failure only → Secure Connection Error. Do not blindly treat `Protocol` as TLS failure. `Unauthorized`, `Forbidden`, `Validation`, `RateLimit` preserve existing distinct security/validation/cooldown behavior, with no generic dialog hijacking. Backend-authored user-facing message allowed only when validated/safe; otherwise use safe local error copy. No raw server exception details, request path, token, OTP, stack trace or device identifiers in UI/log. A local fallback must work with network offline.

### 4.5 Six fixtures / actions

| Type | Origin | Info | Actions |
| --- | --- | --- | --- |
| App Under Maintenance | Bootstrap app policy or explicit app-wide backend code; service maintenance scoped separately | optional backend-authorized ETA | Try Again only when eligible; app-wide blocking |
| Update Required | Bootstrap required flag | current + minimum required version | Update Now, blocking |
| Update Available | Bootstrap optional flag | current + latest version | Update Now + Not Now |
| Connection Problem | Connection/Timeout | normally none | Retry the eligible originating request |
| Something Went Wrong | Server/unexpected authorized category | normally none | Retry; Cancel only where safe |
| Secure Connection Error | confirmed certificate/TLS failure | normally none | Retry only, never bypass trust |

## 5. Exact scoped implementation manifest

All paths below are relative to the indicated module's `src/commonMain/kotlin/com/carbroz/cbpartner/{domain|data|feature/...|app}` package root unless noted. “KEEP” means zero intentional modification.

### P0 Contract and development fixtures

**CREATE** six complete `DialogPresentation` fixtures and Bootstrap policy scenario fixtures under `docs/architecture-proposal/dynamic-runtime-v2/`, with appropriate Android/Web development resource copies only as required for preview. **KEEP** approved `partner-login-country-picker-parity-v19.json` and `partner-otp-ui-contract-v3.json` and their mirrored previews byte-for-byte. Document source-vs-fixture assertions, origin/priority rules and expected states. No Backend change.

### P1 UI foundation

| Module | File | Operation |
| --- | --- | --- |
| domain | `model/dynamic/element/IconBadgeElement.kt` | CREATE typed source, size, center image size, background, border, shape/circle and spacing |
| data | `dynamic/model/element/IconBadgeElementDto.kt` | CREATE typed DTO |
| data | `dynamic/mapper/ElementMapper.kt` | MODIFY explicit `icon_badge` branch |
| feature/dynamic | `sdui/element/IconBadgeElement.kt` | CREATE rendering via existing trusted `DynamicAssetImage` |
| feature/dynamic | `registry/DynamicRendererRegistration.kt` | MODIFY explicit registration |
| domain | `model/dynamic/section/StackSection.kt` | MODIFY typed `background`, `shape`, `border` only |
| data | `dynamic/mapper/SectionMapper.kt` | MODIFY map three properties |
| feature/dynamic | `runtime/DynamicLayout.kt` | MODIFY apply three properties via existing shared helpers |

**KEEP** `DynamicRegistry.kt`, `StackGroup.kt`, `StackComponent.kt`, `DynamicAssetImage.kt`, existing text/button content and host. Do not introduce generic style property bag or UI defaults.

### P2 Response/error boundaries

| Module | File | Operation |
| --- | --- | --- |
| domain | `model/error/AppFailure.kt` | CREATE UI-independent typed category/approved message/presentation origin |
| data | `error/NetworkFailureMapper.kt` | CREATE one centralized network→domain failure mapper |
| network | `http/RequestHandler.kt` | MODIFY bounded, non-logged server error payload preservation; do not change unrelated transport |
| network | `NetworkException.kt` | MODIFY **only** if confirmed TLS/certificate error classification genuinely required |
| data | `dynamic/DynamicRepositoryImpl.kt` | MODIFY response/error presentation propagation |
| data | `bootstrap/BootstrapRepositoryImpl.kt` | MODIFY centralized failure mapping without new network logic |
| data | `dynamic/model/response/DynamicResponseDto.kt` | MODIFY optional response-level `presentation` |
| data | `dynamic/mapper/DynamicResponseMapper.kt` | MODIFY map via existing PresentationMapper |
| domain | `model/dynamic/DynamicResponse.kt` | MODIFY optional typed presentation |

The approved plan includes the minimal Bootstrap response DTO/Mapper/Domain-output changes **only when needed to expose an optional backend-authored presentation in simulated Bootstrap response**: `data/bootstrap/model/BootstrapDtos.kt`, `data/bootstrap/mapper/BootstrapMapper.kt`, `domain/model/bootstrap/BootstrapOutput.kt`. Do not add mandatory fields to current Bootstrap contract. **KEEP** `NetworkResult.kt`, HTTP client, repository interfaces and API routes.

### P3 Presentation-state/application orchestration

| Module | File | Operation |
| --- | --- | --- |
| feature/dynamic | `store/DynamicScreenState.kt` | MODIFY typed response/error presentation state + generation |
| feature/dynamic | `store/DynamicStore.kt` | MODIFY entry-scoped failures, request outcome, retry/dismiss handling; preserve existing `present`/dismiss |
| feature/dynamic | `action/RequestActionHandler.kt` | MODIFY typed response-level presentation outcome |
| feature/dynamic | `action/DynamicActionResult.kt` | MODIFY necessary typed result/effect only; no new action type |
| feature/dynamic | `DynamicContainer.kt` | MODIFY invoke same `DynamicPresentationHost` for response/error presentation |
| feature/splash | `SplashStore.kt` | MODIFY bootstrap policy + optional update + failure states |
| feature/splash | `SplashScreen.kt` | MODIFY remove hardcoded AlertDialog/inline error/maintenance **presentation**, retain artwork and dispatch |
| app | `ApplicationContent.kt` | MODIFY compose shared app-level overlay and origin wiring |
| app | `AppDialogEntry.kt` | CREATE minimal, internal transient global presentation + origin identity carrier; **not** Store/manager |

**KEEP** `feature/dynamic/action/DynamicActionExecutor.kt`, `PresentActionHandler.kt`, `DismissActionHandler.kt`, `DynamicPresentationHost.kt`, `app/DynamicSession.kt`, existing navigation/session stacks unless a separately reviewed source-backed blocker requires an approved amendment. No new `show_dialog` action or second action dispatcher. No direct HTTP from App root.

### P4 Existing Android/Web preview expansion

| File | Operation |
| --- | --- |
| `app/src/commonMain/kotlin/com/carbroz/cbpartner/app/LocalDynamicPreview.kt` | MODIFY add opt-in supported scenario/result fixtures via real mapper path |
| `androidApp/src/main/kotlin/com/carbroz/cbpartner/android/MainActivity.kt` | MODIFY debug/development-only scenario entry |
| `webApp/src/wasmJsMain/kotlin/com/carbroz/cbpartner/web/WebDynamicPreview.kt` | MODIFY same development-only scenario selection |
| Relevant `androidApp/src/main/assets/` and `webApp/src/wasmJsMain/resources/preview/` | ADD preview fixture copies only |
| Existing Login/OTP fixtures | KEEP byte-for-byte |

Test Splash normal/maintenance/required/optional and failures; Login Send failure/retry; OTP Verify/Resend failure/cooldown/timer preservation; user-safe message, no auto-retry. Preview must exercise real DTO/Mapper/Host/Store paths, not custom static dialog rendering.

### P5 Consolidated verification **after P0–P4 coding batch**

```powershell
.\gradlew.bat :domain:jvmTest :data:jvmTest :feature:dynamic:jvmTest :feature:splash:jvmTest :app:jvmTest :androidApp:assembleDevelopmentDebug :webApp:wasmJsBrowserDevelopmentWebpack
```
Add/extend targeted domain mapper, data response/Bootstrap/transport, presentation mapping/registry/layout, Store/retry/origin/ordering and preview regression tests as required. Then Samsung A35 on-device six-state visual+interaction verification and Web verification. Verify Country Picker, Login v19 and OTP v3 screenshots/functional parity and no Gradle dependency reversal. Do not claim green unless actual output exists. After observed failures, fix only those failures, rerun affected tests, and record acceptance.

### P6 Backend migration **deferred until explicit new authorization after P5 acceptance**

`CB-Backend/development`: `sdui/engine/src/nodes/element/IconBadge.ts` (new concrete node), `sdui/engine/src/nodes/definitions.ts`, `sdui/engine/src/nodes/section/StackSection.ts`, `sdui/engine/src/core/SduiModel.ts`, `SduiBuilder.ts`, narrowly `Action.ts`, `apps/api/src/transport/response/ResponseHelper.ts`, `domains/configuration/application/contracts/partner-bootstrap.ts`, `GetPartnerBootstrapUseCase.ts`. Preserve existing PartnerBootstrapController ownership and existing bootstrap `config.maintenance/update` fields, and separately migrate Login v19/OTP v3 exact contract per doc 157. Do not weaken strict schema to `unknown` or revive `$response`.

## 6. No-scope-drift and review gate

Implement P0–P4 as **one productive Frontend coding batch** rather than individually installing/testing the APK after each phase; retain source-level checks while editing, then run **one consolidated build/test P5**. Fix observed issues only and re-verify. This does **not** remove the need for precise tests or contract review before compiling. Source may show a genuine incompatibility; in that case **stop that sub-change, report exact conflict, and obtain approval for a manifest amendment**, rather than silently adding any new class, property, framework, DI module, action, transport abstraction, server changes, or preview-only rendering hacks.

**Never claim implementation complete merely because this document or six fixtures exist.** Final acceptance requires actual passing tests/builds and owner device review.

## P5 device finding — preview template identity mismatch (2026-10-26)

After visual acceptance of Maintenance, Connection Problem, Update Required and Update Available Dialogs on Samsung A35, tapping **Not Now** on optional update transitioned to a blank/white screen. The source-level cause was a mismatched **development preview only** destination: the Bootstrap scenario fixtures used `templateId: tpl_7K2M9Q`, but the approved Login v19 JSON (`partner-login-country-picker-parity-v19.json` and Web Login preview) has `data.template.id: tpl_P102L`. `DynamicRenderer.Render` intentionally returns without drawing when destination and loaded screen identity differ. Preserve this strict protection.

**P5 targeted preview correction:** Align all four development Bootstrap scenario fixtures (`normal`, `maintenance`, `required_update`, `optional_update`) and their exact mirrors in docs, Android assets, and Web preview resources to `tpl_P102L`. Keep Login v19/OTP v3 JSON, Kotlin renderer/Store/navigation and Backend unchanged. Rebuild/reinstall Android before rechecking `Not Now`. The older authoritative doc 157 still describes `tpl_7K2M9Q` and conflicts with the current approved Login v19 fixture. This is an **open Backend migration identity-reconciliation gate**: choose and freeze one identical live Bootstrap destination and Login template ID before P6, without silently modifying either live endpoint now.


## Owner-directed reference UI parity revision — 2026-10-26

**Reference:** attached `CB-Partner App Dialog States(1).png` showing Splash Loading and six dialog states. **Status: source updated, consolidated build and physical-device visual acceptance NOT YET RUN for this revision.** Prior P5 green build and screenshots are for the older UI revision; do not claim 1:1 parity without a new install and screenshots.

**Canonical Login identity:** The owner explicitly changed approved Login v19 from `tpl_P102L` to **`tpl_P101L`**. Both canonical V2 Login JSON and Web/Android generated preview now use P101L. All 12 Bootstrap scenario copies in docs, Android and Web use the same value. Backend remains untouched; do not infer that Backend Login's layout matches Login v19.

**Reference comparison and changes:**
| Image state | Current revision | What remains open |
| --- | --- | --- |
| 1 Splash loading | Native Splash loading changed from linear bar/Loading to circular spinner + Connecting/Setting up your experience. Existing hero/branding untouched | Pixel comparison on Samsung A35 |
| 2 App Maintenance | Existing vertical card, blue gear and Retry; adjusted title/description sizes and explicit retry intent | Reference ETA box is NOT fabricated: current Bootstrap contract has no restoration estimate. Optional authoritative ETA field/conditional authored Section requires later contract decision |
| 3 Force Update | Existing red badge, info Section, one button; title typography and spacing between description and info Section adjusted | Exact font-family cannot be guaranteed from raster without identified/authorized font source |
| 4 Optional Update | Existing blue badge, info Section, two buttons; same spacing changes; preview-only latestVersion changed to reference 1.2.0, not Backend | Verify Not Now/Login and trusted store link after reinstall |
| 5 Network Error | Existing vertical card and user-initiated Retry | Reference has a Go to Offline Mode action, but the product has **no approved offline destination/behavior**. Do not show a misleading action or invent local offline mode |
| 6 Server Error | Authored compact horizontal StackSection + StackGroup, trailing clickable existing IconContent close, Cancel and Retry buttons | On blocked Splash, close/cancel are unsafe: separate horizontal `server_error_startup` fixture has Retry only; screen-scoped variant has Cancel/Close |
| 7 Secure Connection Error | Same horizontal component/section/group layout with retry and existing close IconContent | On blocked Splash, `secure_connection_startup` has Retry only; real TLS/certificate categorization remains unverified and preview-only |

**Single generic renderer retained:** No new template/component/group/content, no alternate dialog host, no ViewModel/network layer duplication. Startup-safe variants are additional JSON compositions of the same already-supported types, not new UI categories. `DynamicContainer` uses the existing `DismissResponseDialog` / `RetryResponseDialog` intents; only the local fallback Dialog's existing `dismiss` action carries an authored `dialogIntent: cancel|retry` payload, so Cancel or X cannot accidentally replay a request. Splash `Not Now` retains its own explicit business rule.

**Acceptance gate:** build/tests (`:domain:jvmTest :data:jvmTest :feature:dynamic:jvmTest :feature:splash:jvmTest :app:jvmTest :androidApp:assembleDevelopmentDebug :webApp:wasmJsBrowserDevelopmentWebpack`), then install developmentDebug, visually verify all variants including text wrapping, narrow viewport/landscape, icon padding, Back/outside behavior, Retry/Cancel and Login/OTP regressions. Never claim 1:1 screenshot parity without owner approval. Backend P6 is still deferred.

## P5 owner acceptance record — 2026-10-26

**Status: P5 FRONTEND PREVIEW + DEVICE ACCEPTED by owner, with explicit remaining integration limitations.** This status supersedes earlier "NOT YET RUN" planning notes as a chronological update, without rewriting historical findings.

- The owner supplied a successful consolidated Gradle result on branch `architecture/dynamic-runtime-v2-discussion-freeze` at commit `64b22c29`: `:domain:jvmTest :data:jvmTest :feature:dynamic:jvmTest :feature:splash:jvmTest :app:jvmTest :androidApp:assembleDevelopmentDebug :webApp:wasmJsBrowserDevelopmentWebpack`, **BUILD SUCCESSFUL (285 tasks, 0 failures)**. Previously corrected test-only `DynamicSection.id` compile error; production UI unchanged by that correction.
- Android `installDevelopmentDebug` was reported successful on Samsung A35. Device screenshots and explicit owner feedback accepted the generic startup Connection, Maintenance, Update Required, Update Available, compact Server Error, compact Secure Connection Error and optional-update `Not Now` → Login, plus normal Bootstrap → Login. The owner subsequently confirmed **all requested Login/OTP network-error, Retry/Cancel and remaining UI preview scenarios behave and look correct** ("upar ke sare scenario proper work kar rahe he. our ui bhi proper he"). Preserve accepted UI; no unsolicited redesign or further source edits.
- **Scope of acceptance:** development-only JSON fixtures, shared typed mapper/registry/Compose renderer, safe preview requests and on-device interactions. This is **not** certification of production Backend-supplied Dialog JSON, real certificate/TLS exception classification, cross-device/landscape pixel-perfect screenshots, or an approved offline mode. Font-family exactness and Maintenance ETA remain dependent on external design/data contracts. Preserve those as open, not silently PASSED.
- **Final P5 disposition:** close Preview/functional/owner UI acceptance gate for current device-tested scenarios. **P6 Backend migration remains deferred** until separately initiated/authorized. Reconcile real Bootstrap/Login/OTP contracts against owner-approved V2 JSON and `tpl_P101L`, backend strict SDUI schema, response-level presentation and security/transport codes before integration; do not infer live parity from preview results.
- **Process and change control:** No backend commit, no feature re-architecture, no renderer duplication, no new UI type beyond `IconBadgeElement`. Any new live-protocol requirement requires source evidence and explicit approved amendment.
