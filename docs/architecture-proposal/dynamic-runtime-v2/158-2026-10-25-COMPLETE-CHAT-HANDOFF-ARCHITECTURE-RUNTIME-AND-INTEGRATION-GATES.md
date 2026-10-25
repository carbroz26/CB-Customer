# 158 — Complete 2026-10-25 Chat Handoff: V2 Architecture, Login/OTP Parity, Resend Verification and Backend Gates

**Date:** 2026-10-25  
**Purpose:** Consolidate decisions and current verified state for the next ChatGPT conversation. This is an **additive handoff**, not a redesign or retroactive declaration of implementation.  
**Frontend repository:** https://github.com/carbroz26/CB-Partner  
**Active frontend branch:** `architecture/dynamic-runtime-v2-discussion-freeze`  
**Source checkpoint before this handoff:** `585ecd070e6981dc610b99fdbe1167e8de8ae2b4`  
**Backend repository:** https://github.com/carbroz26/CB-Backend  
**Backend branch/source checkpoint verified by Document 157:** `development` / `b67b36141aa6ae9d0488c2582b0114d2abb09c86`  
**Scope:** Frontend plus backend SDUI contract reconciliation for Login v19 → OTP v3 → Resend → Verify; no backend implementation in this handoff.

> **Mandatory first actions in a new chat:** Read *every document and JSON fixture* under `docs/architecture-proposal/dynamic-runtime-v2/`, beginning with `README.md`, `157`, `158` and `159`, then reconcile **all 179 current files** at this checkpoint in chronological/supersession order; read the actual modules/packages/classes **line by line** before planning or editing. The file count is a checkpoint, not a permanent invariant. Read the frontend and backend source of every relevant dependency, not only class names. Do not assume past statements are current merely because they say "FROZEN", "COMPLETE" or "PENDING". Recheck branch HEAD and tests.

## 1. Documentary authority and critical read order

1. Start with `README.md` and the latest `157` (Backend↔Frontend exact target-contract freeze), this `158` (complete handoff), and `159` (next-chat starter prompt).
2. Read the **entire folder**, including docs `01–20`; implementation/audit waves `21–111`; Login/Country Picker `112–120` and `124–150`; OTP `121–122` and `151–156`; all corresponding JSON versions. No skipping because filenames appear historical.
3. In particular read `01` (rules/modules), `02` (bootstrap/network), `03` (hierarchy/renderers), `04` (bindings/actions), `05` (navigation/lifecycle), `06` (tests), `08–16` (source reconciliation), `19–20` (backend-independent and original V2 freezes), `44/47` (six categories), `48–55` (navigation), `56–81` (action evolution), `82–111` (lifecycle/diagnostics), `112/119/126/140/141` (handoffs), `132–150` (Login validation, visual parity and generic changes), `151–156` (OTP current development), `157` (newest exact backend target contract).
4. **Supersession rule:** Current checked-in code and latest explicit approved decision win over an old source snapshot or old status. Older doc status is historical where later decisions superseded it. However, an implemented discrepancy is not automatically a new product decision: document the conflict and identify the proper owner instead of silently redefining the frozen contract.
5. **Document 157 is already an approved target-contract freeze** as of this handoff, but states the backend implementation is NOT complete. Treat its open blockers as open, not as implemented merely because their desired shape is frozen. Neither this `158` nor the next-chat prompt replaces `157`.

**Current exact editable visual JSON authority**:
- `docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-parity-v19.json`, mirrored byte-for-byte by `webApp/src/wasmJsMain/resources/preview/partner-login.json`.
- `docs/architecture-proposal/dynamic-runtime-v2/partner-otp-ui-contract-v3.json`, mirrored byte-for-byte by `webApp/src/wasmJsMain/resources/preview/partner-otp.json`.
- Both file pairs were rechecked identical at source checkpoint above. Earlier Login v3–v18 and OTP v1/v2 JSONs are important historical reference **only**; never substitute them for the current approved layouts. User-supplied old OTP API sample is a **reference for challenges, actions and bindings**, not visual authority.
- Backend `sdui/engine/src/screens/partner/PartnerLoginScreen.ts` and `PartnerOtpScreen.ts` currently implement the old design and cannot replace these JSON fixtures.

## 2. What actually happened and what was verified in this conversation

1. Owner shared a historic OTP screen JSON as reference for `challengeId`, `phoneNumber`, `deviceId`, `otp` and generic request/navigate/external-uri actions. Assistant separated old screen configuration from actual Send OTP action-result payload; old UI was NOT approved.
2. We chose development **10-second mock** countdown while keeping production duration returned by backend (currently `resendAfterSeconds=60`). No hardcoded countdown behavior in production renderer.
3. `155-GENERIC-COUNTDOWN-RESEND-OTP-MINIMAL-CONTRACT-FROZEN-2026-10-24.md` froze minimal `data.countdowns`, entry-local monotonic countdown calculation, computed `$state.countdowns.resend.remainingText` and `ready`, and generic guarded RequestAction for Resend. No `TimerElement`/new Store/ViewModel.
4. Frontend implementation commits (source historical progression): `504fa9b2` contract doc; `fee763ae` countdown/preview/test implementation; `cac950c0` mapper nullability fix; `a12fce76` platform-preview dependency boundary and injectable test clock. `146c208e` added failure-mode preview + failure/duplicate-request tests. The later source checkpoint `585ecd07` adds documentation-only `157`; recheck exact current source before edits.
5. Android compilation initially failed because `androidApp/MainActivity.kt` directly imported Domain `DynamicValue` without that module on its direct classpath. Fixed by performing typed-value transformation inside `app/LocalDynamicPreview.kt` rather than introducing an unwanted Android→Domain dependency.
6. Owner's later Gradle command `:data:jvmTest :app:jvmTest :feature:dynamic:jvmTest :androidApp:assembleDevelopmentDebug` returned **BUILD SUCCESSFUL** (while Git was temporarily at old `a12fce76`). A later owner report confirmed the failure-preview behavior after switching to the failure-mode source; do not invent a final post-`146c208e` full-build log beyond what was actually provided. Existing tests were added, but an explicit latest-HEAD green output should still be captured in the next integration gate.
7. Owner-device verification: Samsung SM-A356E / Galaxy A35, Android 16. Countdown `00:10 -> 00:00` and disabled→enabled behavior observed. In normal preview, clicking Resend restarted at `00:10`. In failure preview, clicking Resend displayed `Unable to complete action` and **did not restart**. These are **real device UI observations against a development fake repository** — **not** a proof of live Backend HTTP/429 or actual SMS success.
8. Automated test source includes challenge replacement/guard/duplicate in-flight and failure-retry assertions; these are separate from owner-observed UI. Treat regression tests as only as verified as the last applicable green run; do not overstate proof of secure backend challenge or session behavior.
9. Git local hiccup: corrupted remote-tracking reference `refs/remotes/origin/architecture/dynamic-runtime-v2-discussion-freeze` caused fetch/merge failures and a stale APK; protect the local untracked `CB-Partner-Live-Trace-V1.13.ps1`. Git maintenance must be minimal and nondestructive; do not assume broken-ref status remains unresolved or solved without fresh local `git log`/status. Multiple ADB devices require an explicit `-s SERIAL`. Wireless ADB serial used in latest device run: `192.168.0.102:37353` (dynamic, do not hardcode as persistent); scrcpy was confirmed working when device selected. PSReadLine UpArrow rendering crash is a terminal UI issue, not an app issue.
10. Contract reconciliation then produced **Document 157**: exact target endpoints, four-field destinations, backend schema/authoring migration and explicit blockers; **its integration is not implemented**.

**Verified vs not verified**: Android preview Success and Failure = user-confirmed; visually unchanged Login/OTP JSON mirrored = source-verified; backend/auth/OTP HTTP live success, country catalog, JWT persistence, next protected screen, backend UI parity, typed 429 UX, production resend deadline across re-entry = NOT VERIFIED / NOT IMPLEMENTED.

## 3. Platform/module/package architecture — understand source, don't manufacture layers

`Kotlin Multiplatform + Compose Multiplatform`, commonMain-first and Clean Architecture, Pure Store + MVI + unidirectional data flow, **no ViewModel**.

| Module/package | Actual source responsibility | Key read targets |
| --- | --- | --- |
| `androidApp` / `iosApp` / `webApp` | Thin platform startup/metadata, preview launch/packaged assets, real OS integration only | `MainActivity`, Android metadata, Web `Main` / `WebDynamicPreview`, iOS entry |
| `app` | Composition root, DI wiring, session/effect consumption; dev-only preview repository | `App.kt`, `ApplicationContent.kt`, `DynamicSession.kt`, `LocalDynamicPreview.kt` |
| `core` | Shared runtime metadata/diagnostics and common platform-neutral primitives | source package inventory, diagnostic privacy |
| `network` | Shared HTTP transport, error/auth/headers/base URL setup | network builder/request handler/module |
| `domain` | UI-independent typed models, destinations, values, repository contracts | `model/dynamic/**`, `DynamicDestination.kt`, `BootstrapRepository`, `DynamicRepository` |
| `data/bootstrap` | Bootstrap network DTO→mapper→repository and DI | `BootstrapDtos`, `BootstrapMapper`, `BootstrapRepositoryImpl` |
| `data/dynamic` | Screen/action DTO, exact mapper chain, remote repository, country catalog, diagnostics | `DynamicResponseMapper`, `TemplateMapper`, `ComponentMapper`, `SectionMapper`, `GroupMapper`, `ElementMapper`, `ElementContentMapper`, `DynamicActionMapper`, `DynamicValueMapper`, `PartnerCountryCatalog` |
| `navigation` | Single navigation state, entry identity/bindings and navigation host | `DynamicNavigationEntry`, `DynamicNavigationState`, `DynamicNavigationHost` |
| `feature/splash` | Bootstrap entry Store + splash renderer/logic | `SplashStore`, `SplashScreen`, `SplashModule` |
| `feature/dynamic/store` | Entry-scoped UI state, intents, value changes, action orchestration/validation and cooldown ownership | `DynamicStore.kt`, `DynamicScreenState.kt` |
| `feature/dynamic/runtime` | Reference resolver, validation, retained state, countdown pure helper, layout/render context/list items | `DynamicReferenceResolver`, `DynamicFormValidator`, `DynamicRuntimeState`, `DynamicCountdownClock`, `DynamicRenderContext` |
| `feature/dynamic/action` | One action executor and registered typed command handlers; request retains success data | `DynamicActionExecutor`, `RequestActionHandler`, `NavigateActionHandler`, `PresentActionHandler`, `DismissActionHandler`, `ExternalUriActionHandler`, `StateActionHandler`, `SequenceActionHandler` |
| `feature/dynamic/renderer + registry + sdui` | Category-specific typed renderer registration, Compose-only layout/interaction; no network from renderer | `DynamicRegistry`, `DynamicRendererRegistration`, `DynamicRenderer`, `sdui/template`, `component`, `section`, `group`, `element`, `content` |
| `feature/dynamic/presentation` | Generic modal/picker host reusing common renderer hierarchy | `DynamicPresentationHost` |

Read **all packages and classes** under each module line by line for actual source dependencies; table is the onboarding route, **not** substitute for code review. Check tests in each module and actual `build.gradle.kts` module directions.

## 4. Six-category Dynamic rendering hierarchy and cardinality

```text
HTTP response {status,code,message,data,traceId}
 └─ data = DynamicScreen {screenId,schemaVersion,targetApp,template,theme,...}
    ├─ exactly one Template (example FormTemplate)
    │  ├─ optional Header slot: Components
    │  ├─ Body: template.components [1..n]  (NOT template.body.components)
    │  │   ├─ Component (example StackComponent) [repeated instances]
    │  │   │  ├─ direct Elements [0..n]
    │  │   │  └─ Sections [0..n]
    │  │   │     ├─ direct Elements [0..n]
    │  │   │     └─ Groups [0..n]
    │  │   │        └─ Elements [1..n]
    │  │   │           └─ optional leading/trailing Content [0..n]
    │  └─ optional Footer slot: Components
    ├─ optional presentations (e.g. bottom sheet, with own components)
    ├─ optional countdowns (screen behavior, not element type)
    ├─ optional initialBindings
    └─ optional theme
```

Exactly **six logical rendering categories**: Template, Component, Section, Group, Element, ElementContent. `ElementContent` is an accessory/decoration under an Element, **not another Element or Group**. `Header/Body/Footer` are Template slots, **not** extra categories. Template main padding applies **only** to Body; header/footer independently own padding, host handles safe areas.

**Current source-registered concrete types** (inspect `DynamicRendererRegistration.kt`): FormTemplate; StackComponent; StackSection, ListSection, FormFieldSection; StackGroup; TextElement, ImageElement, InputElement, ButtonElement, DividerElement, IconElement; DividerContent, IconContent, ImageContent. Earlier conceptual text claiming `DividerElement` was removed is not current source truth: its model/renderer are registered now. Do not delete or change type vocabulary based on old memory alone. Future Grid/List/Carousel or other concrete categories are extensibility intentions, **not automatic implementation obligations**.

One screen may use the same Template **type** as another with a different exact unique `template.id`. Login = `partner_login` / `tpl_7K2M9Q` / `form_template`; OTP = `partner_otp` / `tpl_P102O` / `form_template`. Avoid confusing Template *instance ID* and type. `DynamicRenderer` checks screenId + templateId + templateType against active destination before render.

**Data movement (not generic opaque node bags):**
`HTTP JSON -> DynamicResponseDto/ScreenDto -> exact Template/Component/Section/Group/Element/Content DTOs -> respective typed Mappers -> typed Domain models -> explicit six-category Registry -> Compose renderers`.
No reflection-based dynamic discovery, do not introduce a broad nullable "all UI properties" superclass, no duplicate mapper chain; checked dispatch per concrete type.

**Nested content:** `properties.leading[]`/`trailing[]` use Icon/Divider/Image Content with source/size/color/`spacing`. Resource or URL asset keys and fallback behavior are contract-driven; do not restore historical icon-name-to-glyph mapping or invisible default paddings. Relative spacing, error presentation, typography and visuals must follow exact approved current JSON. Respect registered DividerElement as actual source type without conflating it with DividerContent.

## 5. Generic runtime, reference and action flow (actual responsibility)

```text
SplashStore
 -> GET /api/v1/partner/config/bootstrap
 -> BootstrapMapper -> initial DynamicDestination
 -> DynamicSession + DynamicNavigationState create screen entry
 -> DynamicNavigationHost -> DynamicContainer
 -> one DynamicStore loads GET screen via LoadDynamicScreenUseCase
 -> DynamicRepository / Network
 -> DynamicResponseMapper -> typed screen + registry/renderer
 -> Input -> ValueChanged(key,value) -> Store entry bindings
 -> Element onClick -> DynamicIntent.Action -> DynamicStore
 -> declared generic validate + enabledWhen eligibility
 -> DynamicActionExecutor -> registered action handler
 -> RequestActionHandler -> DynamicRepository HTTP
 -> Request result (successful retain paths only, plus optional destination)
 -> Store / DynamicSession committed stateChanges and effects
 -> Host consumes queued effect -> navigation state updates
 -> new screen entry loads; renderer checks four-field destination identity
```

Read `DynamicSession.kt` + `ApplicationContent.kt` for effect commit/claim/ack ownership; renderer may dispatch callback, **never directly navigate or network**. `DynamicStore` per entry owns orchestration, validation and cancellation; `DynamicRuntimeState` holds successfully retained session values. Values are strongly typed `StringValue, NumberValue, BooleanValue, ObjectValue, ArrayValue, NullValue`. `DynamicReferenceResolver` supports `$binding` (entry form), `$context` (approved device/app metadata), `$state` (retained state and active screen computed overlay), `$literal`. **`$response` is forbidden in Frontend JSON**. The helper resolves nested paths and object/array values; Request action retain uses selected `data.path` values on successful responses, not a global response namespace.

**Actions currently registered in source**: `request`, `navigate`, `external_uri`, `dismiss`, `present`, `state`, `sequence`. **Registered ≠ semantically approved for arbitrary use.** `SequenceActionHandler` is an **inert Completed placeholder** (never executes nested actions), per later decisions. `StateActionHandler` uses a simple `payload.key/value` but differs from the older Backend state wire proposal and must not be generalized without separate audit. `Present` expects `payload.presentationId`; `Dismiss` rejects destination/payload and is modal semantics; `navigate` handles push/replace/pop where supported. Avoid reviving older Docs 04/20 aspirations for executing nested Sequence or old present shape.

**Action guards and timer**:
- Screen-authorable `data.countdowns=[{id:"resend",durationSeconds:{"$state":"resendAfterSeconds"}}]`.
- On screen entry, numeric backend duration is captured by entry-owned monotonic clock; computed `countdowns.resend.remainingText` and `countdowns.resend.ready` published into read-only transient state. No per-TextElement coroutine.
- Current OTP `otp_timer_text` TextElement has clock leading accessory `icons/clock` and spans reference the computed state; `otp_resend_control` is TextElement with `payload.enabledWhen={"$state":"countdowns.resend.ready"}`; Store action guard prevents premature request, renderer disables gestures/color.
- Request `responseMode:none` + `retain{challengeId:"data.challengeId",resendAfterSeconds:"data.resendAfterSeconds"}`; success updates retained state and restarts timer, failure does **neither**.
- Backend enforces cooldown, rate limit and 6-digit OTP; 10 seconds belongs **only** to development preview. Time re-entry/process death and server-aligned deadline remain an explicit open gate.

## 6. Login/OTP JSON and backend gap matrix — use Document 157 as frozen target

| Boundary | Current Frontend fixture | Current Backend source / target |
| --- | --- | --- |
| Bootstrap | Approved Login `partner_login` / `tpl_7K2M9Q` | Backend default guest Login template id `tpl_P101L`; persisted config may override; migrate both to approved identity |
| GET Login | v19 source of visual truth | Backend `PartnerLoginScreen` still renders historical screen; migrate builder/schema/fixtures without changing v19 visuals |
| Login send action | `/api/v1/auth/send_otp`; body includes `countryCode`, `phoneNumber`, `deviceId` | Canonical `POST /api/v1/partner/auth/send_otp` requires `phoneNumber,deviceId`. Login retain challengeId/phoneNumber/resendAfterSeconds already exists |
| Initial send response | `responseMode:destination` and `mode:push`; retain values | Backend returns `data.challengeId`, `phoneNumber`, `resendAfterSeconds`, `expiresInSeconds`, `isNewUser`, `destination` |
| OTP navigation/document | `partner_otp` / `tpl_P102O` with v3 UI | Actual SendOtp destination matches this identity, but GET `PartnerOtpScreen` still emits historical UI and Backend strict schema lacks countdowns |
| Resend request | `POST /api/v1/partner/auth/send_otp` with phoneNumber/deviceId, guard and retain | Contract compatible; Backend enforces 60-second cooldown; failure 429 or delivery 503 |
| Verify request | `/api/v1/auth/verify`, payload `code` | Canonical `POST /api/v1/partner/auth/verify_otp` requires `otp` instead of `code`; challengeId/phoneNumber/deviceId required |
| Verify success | Frontend `responseMode:destination` | Backend returns JWT access token + refresh token + `partner_dashboard` destination; authenticated secure storage/authorization and renderer availability NOT solved |
| Country picker | UI exists; eight preview countries supplied only for dev | Real `PartnerCountryCatalog` has no approved dataset by default; region/phone normalization/country code handling open |
| Backend SDUI schema | v19/v3 rely on present/list/field/guard/countdown and four-field destination | Backend authoring/strict SduiModel differs; update *existing typed Backend schema/builder/composers* minimally before expecting live parity |

**Four-field destination freeze**: `{screenId,templateId,templateType,endpoint}`. Historical six-field destination claims including `method/authentication` are superseded here. Method/authentication live in Request payload; no client-side duplicated destination classes. A Backend screen response contains exactly one screen data object; a Send/Verify response carries the result data, not an embedded screen.

**Actual Backend contract**: Send OTP `{phoneNumber,deviceId}`; Verify `{challengeId,phoneNumber,otp,deviceId}` (optional deviceModel/osVersion/fcmToken deferred); backend `AuthSecurityPolicy` cooldown 60 s, OTP TTL 300 s, 6-digit length, max 5 attempts, 15-min / 5-challenge window. No temporary security downgrade to match local 10-second timer. Rate-limited `OTP_RESEND_COOLDOWN` or `OTP_RATE_LIMITED` are 429; delivery `OTP_DELIVERY_FAILED` is 503.

**Backend builder migration**: `sdui/engine/src/core/SduiModel.ts` is strict and lacks newer exact fields (countdowns, presentations, initialBindings, `enabledWhen` and other v19/v3 constructs). Verify `SduiBuilder`, `Action`, Backend screen contracts and tests before mapping. Preserve official controllers and productionSduiService; never introduce a second JSON-returning controller, blanket `unknown` schema acceptance, or screen-specific Frontend bypass. Migrate Backend output to approved JSON and strict per-type vocabulary. No backend production edits were performed in this conversation.

## 7. UI and behavior invariants to protect

**Login v19**: brand and hero imagery, top/white card, title/subtitle, phone country code/flag/separator, Country Picker bottom sheet/list filtering, error focus and red border ONLY on editable number region (flag/code/separator normal), validation and button loading/disabled, text dimensions/spacing/colors; preview and contract JSON remain identical. Any actual request endpoint/body update must be isolated to action payload, not visual properties. Country picker has nuanced conditional emptySectionId behavior (referenced fallback is not rendered as a normal sibling). Avoid re-introducing old regression.

**OTP v3**: Login design tokens/parity, title one line on Samsung A35, 6 painted boxes backed by **one BasicTextField and one `otp` binding**, sequential replacement/no unexpected digit shift, paste/backspace/delete focus behavior, Done/outside tap hides IME without unwanted re-open, conditional inline errors WITHOUT reserved blank space, error icon/color, white card/button/hero, clock directly adjacent to centered timer text. Direct Resend after enabled; no confirmation dialog. Keep existing JSON sizes/colors/alignment/layout untouched while correcting transport.

**Source nuance**: The old `DividerContent` vs `DividerElement` debate is not settled by historical prose: both concrete types currently appear registered. General SRP audits must compare actual mapper and registry rather than deleting types or creating aliases based on assumptions.

## 8. Pending next work, exact safe implementation order

**Current priority: implement frozen Document 157 target only after line-by-line source review.** Short, production-oriented waves; no unrelated cleanup or new architecture design.

**Wave A — Backend strict schema/typed builder parity**: audit Backend SduiModel, Action, SduiBuilder, screen composers and tests. Prove support for v19/v3 exact fields/sections/groups/presentation/slots/content/countdown/guard/validations. Add only needed typed cases and tests, never universal permissive properties. No Backend code edits until source ownership and targets clear.

**Wave B — Backend live screen documents and bootstrap destination**: update existing Login/OTP screen composers and/or approved typed source to emit *identical intended UI semantics* as authoritative v19/v3; update backend screen schema; align `partner_login/tpl_7K2M9Q` bootstrap default and persisted override policy. Validate exact screen/destination identity and source parity. Preserve Login/OTP assets and appearance.

**Wave C — Wire-only action reconciliation**: Backend-authored initial Send endpoint/body and Verify endpoint/`otp` field to Document 157. Update approved Frontend JSON and mirrored Web preview *only where needed for wire parity*, no UI styling change; ensure retain mapping unchanged. Resend already partner URL. Keep request and navigation authority where they are.

**Wave D — Live transport and security**: verify Bootstrap GET, Login GET, initial Send response, OTP GET, Resend success/429/failure (server-authoritative wait), Verify invalid/valid with safe test credentials; confirm new challenge replaces old, no duplicate requests, correct timer/error and no OTP/hash/token leakage. Treat actual success beyond Verify as blocked until secure session auth.

**Separate blocking gates (do not smuggle into waves)**: approved country catalog/multi-region policy & normalized numbers; JWT and refresh-token secure storage/bearer injection; authenticated next screen `default_template` not currently supported by existing Frontend form-only TemplateMapper; specific 429/error UX; process death/restored countdown deadline semantics; live supported regions. Freeze each only after source/business decision and tests.

**Not urgent and do not mix**: unrelated generic action refactor, State/Sequence semantic expansion, extra template types, logging redesign, visual restyling, all-day master plans, broad branch cleanup. Source truth wins over pressure to prematurely mark everything "complete".

## 9. Correctness, SRP, security and fast-productivity rules

- `DISCUSS -> RESEARCH -> DECIDE -> PLAN -> FREEZE -> IMPLEMENT -> TEST -> REVIEW -> FREEZE` for *new architectural choices*. Already-approved exact patches may skip repetitive planning and proceed in a small, test-backed wave; never bypass safety/correctness.
- **One job per class**; no duplicate Store/handler/mapper/network/API client/navigation stack/auth state owner/indirection without logic. Do not inject new abstraction merely "for future" or implement only for one screen. Future categories require typed concrete DTO+Domain+Mapper+Renderer+Registry+tests.
- CommonMain-first for Android/iOS/Web, platform-specific files only for OS APIs; respect Clean Architecture dependency directions. Android→Domain direct import caused a real compilation failure in this conversation; keep typed preview conversion at existing `app` boundary.
- UI visuals and errors are authored by approved Backend JSON; no frontend default padding, colors, styling, screen-based type switch, hidden placeholder, icon fallback glyph. Preserve accepted defensive mapping/validation but reject unknown concrete types rather than silently swallowing.
- No ViewModel, reflection registration, generic `Any` UI properties, new `$response`, new `TimerElement`, per-element coroutine, insecure OTP/token logs, or policy-changing demo cooldown.
- Check source and tests, then change the **smallest set** of files; one concise decision/implementation note only for material gaps. Small fixes need no gigantic debate, repeated consent requests or multi-page plans. Provide exact `KEEP/MODIFY/CREATE` only when helpful and clearly distinguish current source facts from proposals.
- Local working tree: never destructive `reset --hard`, `clean`, branch deletion, staging untracked `CB-Partner-Live-Trace-V1.13.ps1` or broad Git operations. Active branch only unless specifically authorized. Pre-commit latest HEAD / ref verification, commit by fast-forward update; validate changed files.
- Safe diagnostic policy: no plaintext OTP, OTP hash, auth/refresh tokens, arbitrary PII, unfiltered raw request/response body. Use existing approved trace projection/logger only.
- Tests: Backend strict schemas, screen exact fixtures and routes, app/data/dynamic JVM tests, Android build, Samsung A35 preview success/failure, Web preview, then live backend. **Never assert a green build unless tool output/user log proves it for that commit.**
- User interaction: Hindi/Hinglish for explanations; English only in repository docs/source/KDoc/JSON; short productive steps and exact commands, no unnecessary narrative or forms.

## 10. Next-chat first response and acceptance checkpoint

1. Confirm actual HEAD of both repositories, list all V2 files, **read all**, build a chronological authoritative/superseded/open-decision register. Document 157 is already frozen, do not re-freeze it.
2. Line-by-line inspect entire frontend modules/packages/classes and relevant Backend identity/SDUI/bootstrap/controllers/tests, with special emphasis on the mapper→renderer and action→retain→effect flow. Cross-check all six categories, source-boundary dependencies, runtime/Store ownership and Login v19/OTP v3 visual JSON **exact**.
3. Output a **compact** gap/ownership matrix: what already works vs missing, exact Backend files to modify, precise tests, regressions and unresolved genuinely blocking decisions. Do not invent missing features or change code in an audit-only step.
4. **Then implement frozen 157 in small safe waves once authorized in new chat**, without revisiting approved visual design or re-planning routine changes. Review tests and update status in this V2 folder after each verified wave.

Current end state: **Development preview OTP Resend success/failure on Samsung = observed working. Frozen backend target in 157 = documented but NOT integrated.** This 158 is the full status/architecture handoff; 159 contains the paste-ready next-chat prompt.
