# 165 — 2026-10-26 Complete Handoff: Accepted Splash/Login/OTP, Generic Dialogs/Toast, Ordered Children and Partner Type Selection

**Document type:** authoritative current-session status/handoff, prepared 2026-10-26. **Scope:** retrospective evidence + exact next-work gates, not a change request. **Frontend source baseline:** 169c2aa2ef2ec8ac71351be259f25aed3db363ca on architecture/dynamic-runtime-v2-discussion-freeze. **Backend read-only baseline:** b67b36141aa6ae9d0488c2582b0114d2abb09c86 on development. **No Kotlin, backend, JSON preview or UI resource implementation was changed during this documentation handoff.** Use source and frozen decisions over contradictory historical summaries.

## 1. Repositories, authority, mandatory reading

- Frontend: https://github.com/carbroz26/CB-Partner ; local Windows PowerShell D:\CarBroz\CB-Partner ; active branch architecture/dynamic-runtime-v2-discussion-freeze. Work on that existing branch; do not open speculative parallel branches.
- Backend: https://github.com/carbroz26/CB-Backend ; active development (main retained). **Do not implement Backend changes until the owner explicitly initiates Backend contract migration**. Read Backend code for reconciliation only.
- The sole authoritative project planning/document corpus is docs/architecture-proposal/dynamic-runtime-v2/ (this folder). It currently contains 201 files including 163 Markdown documents at this source baseline. Read **every** V2 document in the next chat, not just the newest summary. Follow actual version/status chronology: a later approved freeze/addendum supersedes the earlier draft on its exact subject, while older audit evidence remains historical.
- First read README.md; then 165 (this handoff), 166 (full source/docs inventory), 167 (new-chat starter); 164 (Partner Type JSON review) + complete partner-type-selection-ordered-v1-CANDIDATE.json; 163 (ordered-children freeze); 162 (navigating Toast V1.1 acceptance); 161 (base Toast); 160 (generic Dialog plan and P5 device acceptance); 157 (Login v19/OTP v3 Backend reconciliation); 158/159 (previous handoff/prompt as historical), 155/156 (countdown/Resend). Then read all remaining V2 documents 01...164 fully. Do not take old 158/159 references to historical Login ID, endpoint, or pending Backend integration as an instruction to revert newer approved work.
- Read **all actual Kotlin source files line by line**, every module/package/class (especially full ~983-line DynamicStore, ~348-line ApplicationContent, ~182-line DynamicSession, full DTOs/mappers/registry/renderers). Review Gradle configs, JSON fixtures, resources, navigation and tests. Do not merely inspect class names or trust this handoff. Inventory 166 is a file locator, not a claim that every file has been reviewed line by line in preparing this handoff.
- Definitive runtime behavior is determined by current source + most recent approved frozen contract; reconcile documentation contradictions with source truth and record changes. Never mark pending steps complete solely because a document contains an illustrative JSON, a proposed API endpoint, or a previous assistant claim.

## 2. Proven timeline and acceptance evidence

1. Original source and policy: Clean Architecture + typed Compose Multiplatform SDUI runtime (six categories), Koin DI, MVI/UDF Pure Store without ViewModel. Login v19 (canonical template tpl_P101L), Country Picker and OTP v3 (tpl_P102O) approved visual/behavior contracts.
2. OTP timer/Resend: generic data.countdowns, entry-local monotonic clock, computed countdowns.resend.ready/remainingText, original challenge preservation, new challenge only on accepted success; 10-second development preview only. Owner earlier confirmed successful Resend re-start and failure handling.
3. Generic six dialogs: accepted on Samsung A35 using app-wide/local packaged JSONs, including startup and screen-scope safety variants. Splash rotation state retention and Dynamic entry Store retention fixed. Owner stated Dialogs/look/Retry/Cancel worked and rotation no longer dismisses them.
4. Generic Toast V1 + V1.1: new typed ToastPresentation, existing DynamicPresentationHost/Component registry, retained app root ActiveToast with monotonic 0%-to-100% left-to-right progress and white bottom track; no close icon, white card with small green circular tick. Existing present action and request.successPresentationId reusable; Login initial successful Send OTP → navigate OTP then authored 'An OTP has been sent to…'; OTP successful Resend → same-screen 'A new OTP has been sent to…'. Request failures do **not** present success Toast. Owner explicitly tested both and confirmed progress continues across rotation.
5. Owner supplied earlier consolidated :domain:jvmTest :data:jvmTest :feature:dynamic:jvmTest :feature:splash:jvmTest :app:jvmTest :androidApp:assembleDevelopmentDebug :webApp:wasmJsBrowserDevelopmentWebpack BUILD SUCCESSFUL at commit 64b22c29 for Dialogs. Later a compiler dependency issue in Android preview was fixed by moving OTP next-screen fixture mapping behind app/LocalDynamicPreview.withNextScreenFixture(). Owner then supplied :androidApp:installDevelopmentDebug BUILD SUCCESSFUL (2m 6s, 154 tasks) and Samsung A35 installation, followed by explicit Login/Resend Toast device acceptance. **Do not claim a new all-target consolidated run on exact current HEAD if owner has not provided it.**
6. Live Backend integration, real certificate/TLS categorization, full OTP verification/token security, real region/phone support, persisted resume, maintenance ETA, small-screen/landscape pixel-perfect checks are NOT certified by dev previews. The 2026-10-26 status is owner-accepted current preview/UI baseline, not production readiness.
7. Discussion then shifted to Partner Type Selection reference images. The owner corrected category-card hierarchy, arbitrary mixed child ordering, reusable error Dialog rule and shimmer-only remaining feedback. Documents 163 and 164 record this. **Partner Type screen has not been implemented**. Do not continue directly to KYC/training before its JSON contract, implementation plan, tests and owner acceptance.

## 3. Architecture: modules, ownership and lifecycle

**Module map** (actual directory names: androidApp, webApp, app, core, network, domain, data, navigation, feature/dynamic, feature/splash, build-logic):
- core: diagnostic IDs, TraceContext, platform runtime metadata types.
- network: ApiBaseUrl, AppEnvironment, shared Ktor client / request handler / typed transport errors and headers. No new Dynamic-only HTTP client.
- domain: BootstrapOutput, DynamicDestination, DynamicRequest/Response/Screen, typed Theme, Component/Section/Group/Element/Content, actions, presentations, dynamic values/validation, repository contracts.
- data: Bootstrap/Dynamic repositories + remote sources, exact DTOs/typed mapper chain, country catalog, error mapper, DI modules. DTOs own wire decoding; Domain models own typed values. Never fetch in renderer.
- navigation: DynamicNavigationState / Host / Entry. One active destination identity; one typed four-field destination. No parallel navigation stack.
- feature/splash: native splash visuals, Bootstrap Store and startup policy; it initiates bootstrap then hands off destination to the app.
- feature/dynamic: DynamicContainer, DynamicStore, action executor+handlers, runtime value/reference/validation/countdown, six explicit registries, Compose template/component/section/group/element/content renderer and presentation host.
- app: DI composition (App.kt), ApplicationContent, app-scoped DynamicSession, accepted ordered effects/ack, local preview substitution, startup Dialog fixture loading, transient Toast lifetime. Retain across Activity recreation; teardown only on actual owner retirement.
- androidApp: Samsung MainActivity, Intent preview flags and packaged JSON assets, Android metadata. webApp: WASM fixture loading and web preview entry. iOS is a source boundary but cannot run iOS simulator tests on Windows.
- build-logic: Gradle convention config. Avoid adding layers/wrappers with no work.

**End-to-end production/runtime flow:**

    Android/Web App -> existing Koin modules -> SplashStore
      -> GET /api/v1/partner/config/bootstrap -> BootstrapMapper / BootstrapRepository
      -> maintenance/update/error policy or initial four-field DynamicDestination
      -> retained DynamicSession -> DynamicNavigationState/Host + DynamicNavigationEntry
      -> DynamicContainer -> one entry-scoped DynamicStore -> LoadDynamicScreenUseCase
      -> DynamicRepository -> shared Network -> DynamicResponseDto/ScreenDto
      -> TemplateMapper/ComponentMapper/SectionMapper/GroupMapper/ElementMapper/
         ElementContentMapper/PresentationMapper -> typed Domain
      -> explicit DynamicRendererRegistration registry -> FormTemplate/TemplateShell
      -> typed Compose renderers -> UI
      -> UI callback -> DynamicIntent.Action/ValueChanged -> DynamicStore
      -> generic validation/eligibility/origin guard -> DynamicActionExecutor handler
      -> accepted stateChanges + ordered DynamicActionEffects -> DynamicSession.commit
      -> ApplicationContent claim/dispatch/ack: navigation, external URL, toast;
         modal presentations are owned within DynamicStore state via host.

Pure Store + MVI/UDF; NO ViewModel; no network/cancellation/navigation business in individual UI renderer. Session is the single shared retained state/effects boundary, Store only orchestration, handlers only one action capability each. Trace/log IDs sanitized; do not print OTP, phone, bearer token, device ID or raw error response.

## 4. Six Dynamic UI categories / exact hierarchy and current source truth

Exactly six PRODUCT categories:
1. Template: one per screen, concrete FormTemplate currently. It may have optional Header/Footer slots with Components; body is template.components, not a made-up body object. Multiple Component instances permitted.
2. Component: concrete StackComponent currently. Holds direct Elements and Sections.
3. Section: concrete StackSection, FormFieldSection and ListSection currently. StackSection holds direct Elements and Groups; specialized sections have separate rules.
4. Group: StackGroup currently. Holds **Elements only** and never nested Group/Section.
5. Element: TextElement, ImageElement, InputElement, ButtonElement, DividerElement, IconElement, IconBadgeElement **are currently registered in source**.
6. ElementContent: DividerContent, IconContent, ImageContent (the accessory slots within Elements). DividerElement and DividerContent are **both registered** today; outdated notes claiming DividerElement was removed are historical.

Static:
    Screen
      └─ exactly 1 FormTemplate
          ├─ optional header slot -> Components
          ├─ body Components (1..n)
          │   ├─ Elements (0..n)
          │   └─ Sections (0..n)
          │       ├─ Elements (0..n)
          │       └─ Groups (0..n)
          │           └─ Elements only
          │               └─ leading/trailing ElementContent (0..n)
          └─ optional footer slot -> Components
      + screen.data.presentations[] (dialog, bottom_sheet, toast),
        initialBindings, countdowns, theme.

ElementContent is NOT a seventh category. BottomSheet/Dialog/Toast are typed Presentation family, NOT Templates/Elements. The exact registry is feature/dynamic/.../registry/DynamicRendererRegistration.kt; no reflection and duplicates rejected. UI visual props backend-authored, no hidden default spacing/colors/typography/image fallback; use typed background/border/shape, spacing, padding and known packaged asset or validated URL. Header/footer padding independent of body, safe-area reviewed. Elements can have typed leading/trailing Icon/Divider/Image content with accessory spacing (NOT accessory padding). Resource key mapped through DynamicPackagedAssets; network asset primary->fallback->nothing, never guessed icon glyph.

**CRITICAL newer ordering freeze (Doc 163):** Product requires ANY mixed authored order:
- Component: Element -> Section -> Element; Section -> Element -> Section, etc.
- Section: Element -> Group -> Element; Group -> Element -> Group, etc.
- Group: Elements only, preserve array order.
Current StackComponentDto/StackSectionDto have legacy separate arrays, current mappers/renderers render all Elements first, then Sections/Groups. THIS IS A **SOURCE DEFECT RELATIVE TO THE APPROVED NEW CONTRACT**, not a user rule. Doc 163 freezes new mutually-exclusive ordered children[] authoring with typed Domain wrappers (DynamicComponentChild.Element/Section and DynamicSectionChild.Element/Group) while preserving old Login/OTP/Dialog legacy arrays; old format cannot encode arbitrary interleaving. New format is **NOT IMPLEMENTED** yet. Reconcile DTO/Domain/mapper/renderer AND traversal (validation, button IDs, list fallback, action origin) before using it. No Any/blob/reflection or screen-specific special case. Do not silently migrate old approved JSON or modify visual parity.

## 5. JSON envelopes, bindings and action semantics

**Dynamic screen envelope**: {status,code,message,data:{screenId,schemaVersion,targetApp,initialBindings,template,presentations,theme,countdowns},traceId?}. action response data contains retained business fields and optional data.destination. **Four-field DynamicDestination**: {screenId,templateId,templateType,endpoint}, NOT old six-field versions (method/authentication belong to requests). Renderer checks destination identity against returned screen data/template before painting.

**Source-defined DynamicValue**: StringValue, NumberValue, BooleanValue, ObjectValue, ArrayValue, NullValue, nested object paths and arrays. Read DynamicValueMapper and DynamicReferenceResolver first. Current resolver namespaces:
- $binding = active screen/entry form binding (e.g. mobileNumber, countryCode, selected country);
- $context = approved runtime metadata (e.g. deviceId) without copying secret data into UI;
- $state = retained accepted response values (e.g. challengeId, phoneNumber, resendAfterSeconds) + computed local state such as countdowns.resend.ready/remainingText;
- $literal = explicit authored value;
- $item = **only** within current ListSection row; outside a row it fails;
- $config = allowlisted trusted app config (currently legal.termsUrl and legal.privacyUrl only).
- **$response is forbidden**; retain maps approved response.data paths into state. Do not revive deprecated $response, and do not expose access/refresh tokens in generic state.

Current handlers: request, navigate, present, dismiss, state, external_uri, sequence. The sequence handler is a registered **inert placeholder** (does not execute nested actions). Avoid automatic chained/nested action execution. Present requires only payload.presentationId; Dismiss has no destination/payload (local Dialog fallback uses its own explicitly checked retry/cancel intent at container boundary); Navigate supports push/replace/pop by existing handler rules. Request: endpoint, method, authentication, validate, optional body, responseMode none/destination, optional navigation mode, retain, enabledWhen guard, optional successPresentationId. A request only changes challenge/timer or emits success Toast AFTER real success and Store acceptance. Retry source and dialog generations guard staleness; failed requests preserve prior success state. No speculative new actions. Typed state action remains source-defined, not carte blanche arbitrary writes.

**OTP timer**: server-duration-driven data.countdowns id resend and durationSeconds:$state.resendAfterSeconds; entry monotonic clock computes countdowns.resend.ready and remainingText. Frontend dev preview 10 s, Backend policy currently 60 s, OTP expiry 300 s. Success reissues challenge and countdown; failure/rejection must NOT. 6 input slots preserve earlier approved edit/backspace/paste focus rules (do not shift digits). Confirm final details from OTP docs and source.

## 6. Accepted Login/OTP source-of-truth and actual JSON files

**Login v19**: docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-parity-v19.json, mirrored Web preview at webApp/src/wasmJsMain/resources/preview/partner-login.json and Android assets. Screen partner_login, exactly one form_template instance tpl_P101L. Two vertically arranged major StackComponents: hero/branding over background image images/partner_login_hero_07; white bottom login_form with top-rounded 24dp corners, FormFieldSection phone control, Continue Button request and legal text/links. Login's Country Picker is an actual bottom_sheet in data.presentations[], not a second screen; ListSection catalog/filter/selection and empty fallback, country search, row images, X/outside/Back cancel, selected dial code/flag. Login Continue success POST /api/v1/partner/auth/send_otp with phoneNumber $binding.mobileNumber and deviceId $context.deviceId, validate=true, responseMode=destination, mode=push, retains challengeId/phoneNumber/resendAfterSeconds, successPresentationId=otp_sent_success. Confirm current exact JSON values, layout and classes line-by-line before any change.

**OTP v3**: docs/architecture-proposal/dynamic-runtime-v2/partner-otp-ui-contract-v3.json, mirrored webApp/src/wasmJsMain/resources/preview/partner-otp.json and Android assets. Screen partner_otp, form_template instance tpl_P102O, same approved hero background and native white form; segmented six-digit input, title/phone label, Change phone number pop, countdown and guarded Resend text, Verify & Continue Button, support text. Successful verify POST /api/v1/partner/auth/verify_otp with challengeId/phoneNumber/otp/deviceId, responseMode destination. Resend POST /api/v1/partner/auth/send_otp with responseMode none, enabledWhen countdowns.resend.ready, retain challengeId/resendAfterSeconds, successPresentationId otp_resend_success. Previous OTP preview failure mode simulates network error and Retry; real Backend has different rate-limit/delivery outcomes. Literal +91 display currently remains preview-oriented; real country and phone normalization not solved.

**Presentation Toast V1/V1.1**: docs 161 + 162; full JSON samples toast-v1-login-otp-sent-success.json and toast-v1-otp-resend-success.json. Existing typed ToastPresentation with white card, small green tick (new IconBadgeElement), NO X, white bottom progress track and JSON fillColor green animating 0->100 across durationMillis; app-scoped retained ActiveToast with monotonic expiry and rotated continued progress. Login successful destination action queues Navigate then Toast from origin Login binding snapshot; Resend success queues Toast on current screen. Absent successPresentationId means no Toast; failure means no Toast. One active Toast, no duplicate after rotation. User physically verified both success paths on Samsung A35; preserve frozen UI. No backend production proof.

**Splash**: GET /api/v1/partner/config/bootstrap, starts native Splash; statuses Maintenance > Required Update > Optional Update > Normal Login handoff, error Dialogs on startup. Original spinner with 'Connecting…' / 'Setting up your experience'. Optional 'Not Now' uses retained Bootstrap destination; Update Now uses validated store URL; blocked states do not pretend screen exists. SplashStore and Koin retained to survive rotation; bootstrap Load idempotence prevents duplicate request. Don't force splash itself through JSON Dynamic Template.

**Six generic dialogs** (owner accepted; Doc 160): App Under Maintenance; Update Required; Update Available; Connection Problem; Something Went Wrong; Secure Connection Error. Local packaged files feature/dynamic/src/commonMain/composeResources/files/dialog-v1-{maintenance,required_update,optional_update,connection,server_error,secure_connection}.json plus blocked-startup-safe server_error_startup and secure_connection_startup variants = eight fixture files. Documents have matching dialog-v1-*.json. AppDialogEntry maps through SAME DynamicResponseMapper and DynamicPresentationHost. IconBadgeElement is new circular image badge (source/size/imageSize/background/border) first introduced for Dialog icons and reusable for Partner Type. StackSection background/shape/border added for generic dialog surfaces. Runtime supports Retry vs Cancel/Close with distinct intent; no automatic replay on dismiss; startup blocked versions cannot Cancel. No maintenance ETA without Backend ETA; no Go to Offline Mode absent approved route. Existing NetworkFailureMapper maps Connection/Timeout to CONNECTION, Server to SERVER, preserves ACCESS/VALIDATION/RATE_LIMIT; **confirmed TLS/certificate category mapping from real exceptions is not proven** (simulation only), do not claim production Secure Connection Error detection. No new red error banner or dismissible Error Toast for Partner Type.

## 7. Development previews and reproducible Samsung A35 testing

Development-only override is in app/App.kt with LocalDynamicPreview and LocalBootstrapPreview; the normal shared Store/mapper/action handlers/renderers run unchanged. Only repository/catalog/bootstrap source is replaced for explicit debug/development. Android: androidApp/.../MainActivity.kt reads packaged partner-login.json and partner-otp.json from androidApp/src/main/assets/; mapping through app/LocalDynamicPreview.withNextScreenFixture(raw) avoids Android direct Domain dependency. Web: WebDynamicPreview.kt loads /preview/partner-login.json and /preview/partner-otp.json; same parseLocalDynamicPreview and next fixture. Countries preview is eight-country demo catalog, NOT approved production region support.

Actual Android Intent flags from source:
- --ez cbpartner.login_preview true : open Login v19; successful Continue routes to exact mapped OTP fixture and shows first OTP success Toast.
- --ez cbpartner.otp_preview true : enter OTP v3 with dev state phoneNumber=9876543210, fake challengeId and 10 s countdown; Resend success Toast.
- --ez cbpartner.otp_resend_failure true (with otp_preview) : mocked Resend failure; existing error handling; no new challenge.
- --es cbpartner.preview_network_failure CONNECTION|SERVER|SECURE_CONNECTION|UNKNOWN (with Login/OTP preview) : simulate corresponding action failures.
- --es cbpartner.bootstrap_preview normal|maintenance|required_update|optional_update : local Bootstrap policy fixtures, read by actual SplashStore.
- --es cbpartner.bootstrap_error CONNECTION|SERVER|SECURE_CONNECTION|UNKNOWN : simulate Bootstrap failure.
- --ez cbpartner.retention_probe true : source retention test mode, if required for focused lifecycle debugging.

Android package/component: com.carbroz.cbpartner.android.dev/com.carbroz.cbpartner.android.MainActivity. Windows instructions (pick ONE exact serial from adb devices -l; A35 may show duplicate transports):
    cd D:\CarBroz\CB-Partner
    git pull --ff-only origin architecture/dynamic-runtime-v2-discussion-freeze
    .\gradlew.bat :domain:jvmTest :data:jvmTest :feature:dynamic:jvmTest :feature:splash:jvmTest :app:jvmTest :androidApp:assembleDevelopmentDebug :webApp:wasmJsBrowserDevelopmentWebpack
    .\gradlew.bat :androidApp:installDevelopmentDebug
    adb devices -l
    adb -s <CURRENT_SERIAL> shell am start -S -W -n "com.carbroz.cbpartner.android.dev/com.carbroz.cbpartner.android.MainActivity" --ez cbpartner.login_preview true

For OTP change last flag to otp_preview; for startup errors use bootstrap_error; do NOT hardcode stale ADB Wi-Fi port. Build warnings on Windows iosSimulatorArm64Test disabled and AGP host test config/npm ws conflict are historical nonfatal warnings when build succeeds. Do not claim Web or all JVM tests have passed on latest HEAD without fresh evidence.

## 8. Partner Type Selection — current EXACT point in discussion (NOT implemented)

User attached a tall Hero/Category screen and a six-state Samsung A35 spec image (default, pressed card, help bottom sheet, skeleton loading, error reference, retry). Owner correction supersedes previous assistant suggestions:
- Reuse approved Login/OTP FormTemplate background, brand header, 'Choose your partner category' title/subtitle, and **one bottom WHITE StackComponent (orientation vertical)**. No new category-specific UI composable/component/template.
- Bottom StackComponent **exactly four StackSection siblings in order**: (1) Individual Partner card; (2) Organization card; (3) Partner Under Organization card; (4) small Help text row.
- Each card is **a horizontal StackSection** that OWNS surface background, border, rounded corners, padding, whole-card onClick and pressed style. Direct ordered children MUST be: (a) IconBadgeElement circular avatar; (b) StackGroup vertical (exactly two independent TextElements for title/description); (c) IconBadgeElement circular arrow. This was why mixed ordering matters; putting Elements in old elements[] and Group in groups[] puts arrow before text due to legacy renderer.
- Fourth Help Section: no extra rounded card; ordinary white background, TextElement with leading Help icon. Text onClick uses existing present action to open a BottomSheetPresentation. Sheet also reuses same horizontal avatar + vertical text group row layout (no arrows); has header/title/X and footer Got it Button via existing dismiss. No second screen, custom BottomSheet or unrelated rich layout.
- Generic selected/pressed card only, JSON-authorable; no special partner-specific conditional branch. Three static authored cards for this scope, not speculative ListSection rewrite. 96dp is **minHeight** to avoid clipping text when scaled. Sheet rows are informational. Per six-state image base 411x891 dp, horizontal margins 24dp, title ~32sp, subtitle 16sp, card 96dp design base, avatar 64dp, arrow badge 40dp, card border/radius and help sheet parameters documented in reference; treat actual screenshot vs two different image layouts carefully, do not mix contradictory measurements silently.
- **Error UI:** explicitly reuse the already-implemented generic Dialogs. The reference's dismissible red error banner is NOT in scope. Only Loading skeleton/shimmer needs a new generic contract discussion.
- Owner-approved **Doc 163 ordered children wire**: one optional ordered children[] array on StackComponent (permitted Element or Section) and StackSection (permitted Element or Group), with mutually exclusive legacy elements/sections or elements/groups. A typed Kotlin projection preserves legacy Login/OTP without rewriting. Group always elements only. **163 is document-only; DTO/domain/mapper/renderer implementation and traversal still pending.**
- **Doc 164** is a complete ~34KB JSON **REVIEW CANDIDATE**: docs/architecture-proposal/dynamic-runtime-v2/partner-type-selection-ordered-v1-CANDIDATE.json, accompanying docs/164-PARTNER-TYPE-SELECTION-ORDERED-JSON-REVIEW.md. Proposed Screen partner_type_selection, proposed unique form template instance tpl_P103T, same hero image. It includes all 4 card Sections, help sheet, illustrative click/request and new image IDs. **It is NOT parseable by current Frontend DTO because ordered children and section actions/group weight not yet implemented. Do not copy this JSON into Android/Web runtime previews and call it finished.**
- Real generic gaps: StackSection.actions and pressedStyle/minHeight; StackGroup.weight in horizontal parent; authored rendering of Element/Group/Element; category image asset IDs not yet bundled/registered; skeleton shimmer/trigger; Backend third category identity and selection endpoint/destination.
- Candidate endpoint /__contract_pending__/partner-type-selection is deliberately INVALID for production; proposed UNDER_ORGANIZATION ID does not exist in current Backend. Current Backend development/domains/partner/domain/PartnerType.ts enum only INDIVIDUAL and ORGANIZATION. Existing authenticated /register/individual and /register/organization routes require businessName; they are NOT a verified post-OTP category selection API. Do not invent backend success response/destination, identity nor organization membership state.
- Reference Help Bottom Sheet should follow existing bottom_sheet, no rewrite of Dialogs or new custom Avatar Element; IconBadgeElement is already registered.
- Product follow-up after Partner Type acceptance: freeze concrete KYC and Training UI/JSON in the same generic architecture. Do not prematurely build them.

## 9. Pending work — safe precise order without overengineering

**Now (discussion/JSON gates only; NO automatic Kotlin implementation):**
A. Read Docs 163/164, verify full candidate against actual image and Login/OTP JSON. Confirm ordered hierarchy, names/copy, layout, avatar asset contract. Do not change accepted Login/OTP visuals.
B. Source-level exact implementation manifest for ordered children; DTO presence/no ambiguity, typed Domain wrappers, mapper switch, renderer mixed iteration and weight, validation/findButton/ID traversal, ListSection fallback. Tightly scoped reusable extension only; never introduce another Screen Store or generic opaque UI.
C. Discuss reusable entire-StackSection clickable surface, pressed state, minHeight; generic horizontal StackGroup weight; request pending/disabled guard, no duplicate send, robust content wrap and Compose ripple semantics. Fix only real proven gaps.
D. Discuss generic shimmer: skeleton geometry/colors/clip/animation from JSON vs current renderer, what **lifecycle event** starts it (initial first-load screen JSON not yet delivered vs request initiated from currently displayed screen), what stops it on success/error/cancel/rotation, and whether a shell/placeholder must be available. Do not build a fake screen-specific skeleton or add a loading action type speculatively. Error failure uses existing generic Dialog.
E. Read-only Backend reconciliation for three partner IDs, endpoint/method/auth/body, success destination and KYC fork. User may approve temporary preview-only fake response like Login/OTP after JSON is frozen; production Backend changes require separate authorization.
F. Freeze exact Partner Type JSON as new reviewed version **only after** above decisions; compare images and typography, asset IDs, fallback and screen ID/template ID; ensure actual mapper can consume contract after implementation. Make an exact targeted P0-Pn file manifest and test list; get separate authorization before code.
G. Implement on one existing branch, one minimal batch, focused JVM/web compile, Android A35 preview and owner screenshot, reject regressions. After acceptance, progress to KYC then Training.

**Later separately authorized production integration**: Docs 157/160 P6; strict Backend SduiModel/Action/SduiBuilder update to emitted v19/v3/new Partner JSON; authenticated Verify/JWT/session storage; proper region catalog/phone formatting, 429 response semantics, real TLS category, process-death countdown/deadline, persisted bootstrap policy, real Backend response-level presentations. No Backend changes until requested. Do not prematurely claim live functionality.

## 10. Fast, careful implementation principles (owner nonnegotiable)

- Operating process: DISCUSS → SOURCE RESEARCH → DECIDE → PLAN only when necessary → FREEZE → IMPLEMENT → TEST → REVIEW → FREEZE. For tiny fixes, no lengthy debate or oversized plans: verify actual line, smallest localized correction, focused tests, commit and ask owner for result. Avoid deferring straightforward fixes.
- Strict SRP and Clean Architecture, no duplication/network/nav/cache/store wrappers; existing shared Koin/Repository/Store/RenderContext/Registry ownership. Prefer existing concrete types to speculative new generic components, but add small reusable typed features where source evidence requires them.
- Typed DTO/Domain/Mapper/Renderer for each genuine UI category; explicit registries; no reflection; no unbounded generic JSON property bag. Don't add field to Domain unless authored/present in JSON.
- Respect existing frozen visual sizes/colors/copy/back/keyboard/errors, accessibility/rotation, test screens; every new authored size/color stays in JSON, not Compose hardcoded fallback.
- Secrets protected; no OTP/challenge/token/deviceId/phone/headers/body in unsafe log output. Cancellation and stale generation checked, retry only when owner permitted. Preserve exact navigation destination identity.
- Docs/KDoc/code in English, conversation in concise Hindi/Hinglish. README only actual verifiable steps. Source after frozen docs is truth; reconcile mismatches explicitly and keep changelog/status current.
- Command preference: minimal git pull/fetch, direct narrow Gradle tests/build, no broad Git reset/branch churn/architecture redesign.
- When a question is already decided by owner, don't ask again. When user gives screenshot/UI, compare exact view before assumptions. Never call a draft frozen/implemented/verified or a dev mock live Backend.

## 11. Next-chat entry instructions

1. Read **all files** under V2 and source/test code across every module line by line; create compact Source-vs-Docs checkpoint, not a long conversational plan. Start from Docs 165–167 then chronological/reconcile older docs and source.
2. State active heads/branches, approved working UI (Splash/Login/OTP/Dialog/Toast with device acceptance), exact Partner Type 4-Section structure, frozen ordered children rule vs source gap, shimmer-only/error existing Dialog, Backend third-type/route blocker.
3. Current priority = **Partner Type Selection discussion / exact JSON contract**, not live Backend integration, KYC, Training implementation or redoing Toast/Dialog.
4. Report only decisions genuinely open and the smallest correct solution; once approved, commit docs, implement code in existing branch, run targeted build/tests and verify on Samsung A35.
5. Read full 166 inventory for audit completeness; do not infer the inventory itself proves reading code. Preserve freeze chronology and correct old 158/159 claims with newer 157+160–165 facts.
