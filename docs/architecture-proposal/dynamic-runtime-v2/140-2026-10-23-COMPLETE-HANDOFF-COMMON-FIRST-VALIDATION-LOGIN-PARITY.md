# 140 — CB-Partner Dynamic Runtime V2: 2026-10-23 complete conversation handoff, source reconciliation and open gates

**Recorded date:** 2026-10-23  
**Frontend:** https://github.com/carbroz26/CB-Partner  
**Active branch (do not change without instruction):** architecture/dynamic-runtime-v2-discussion-freeze  
**Audited remote HEAD before this handoff:** 21229a450e4c001554c70ffbdeef982d1bf415ab  
**Local Windows folder:** D:\CarBroz\CB-Partner  
**Authority:** This V2 document folder + CURRENT source/test truth. This document is the latest *status, owner decisions and next-work handoff*; it does not silently replace frozen product contracts or authorize unapproved implementation. Read the supersession chain, not just the oldest freeze.

## 0. Entry instructions for next chat (mandatory, time-efficient)

1. Read every file under docs/architecture-proposal/dynamic-runtime-v2, **in full**, grouping by historical phases and later supersession, including README; especially docs 01–08, 12, 16–20, 23, 44–55, 60–81, 98–122, 124–140 and the actual JSON fixtures. Do not infer implementation status from a file's title or an old plan.
2. Read actual code for **every module, package, class and interface before claiming you understand the project**. For the imminent changes, inspect every affected Kotlin source **line by line** with callers, DI wiring, tests, Gradle source sets, platform entries, DTO/domain/mapper chain and latest GitHub HEAD. Produce a compact relevant-class map rather than a huge unfiltered file dump. If the full-repo inspection is not finished, say exactly which areas are not yet audited.
3. Reconcile each claim against latest source, tests and later documents. Distinguish PROPOSED, FROZEN, IMPLEMENTED, LOCAL BUILD VERIFIED, DEVICE VISUALLY ACCEPTED, DEFERRED, and BLOCKED. A previous PENDING may now be done; an old FROZEN may be superseded. Don't use other doc trees for architecture decisions: V2 + source are authority.
4. Continue current goal: finish **Login v11 + Country Picker visual/dynamic parity**, generic safe-area + validation ownership audit; not OTP implementation, unrelated P1 cleanup, backend migration or new architecture.
5. Work fast but correctly: combine related edits into a **single bounded wave**, with an exact short touched-file/test list. No repeated mini-plans for trivial fixes, no speculative abstractions, no premature implement-before-contract. Source conflicts and wire/backend dependence require targeted user decisions, not guessing.
6. Follow DISCUSS → RESEARCH/SOURCE AUDIT → DECIDE → SHORT PLAN/FREEZE → IMPLEMENT → TEST → DEVICE REVIEW → FREEZE. For a minor owner-authorized compiler/test fix, act narrowly without restarting multi-stage planning.

## 1. Immutable engineering rules from owner

- **Kotlin Multiplatform + Compose Multiplatform across Android, iOS, Web; COMMON-FIRST always.** Platform entry points exist for actual platform APIs only. The user's mention of commonMain/androidApp/iosApp/webApp was illustrative, not a rigid instruction to place every concern in a particular folder. Determine real ownership from source, Clean Architecture, lifecycle and SRP.
- **No duplicated UI, DynamicStore, renderer, navigation stack, HTTP client, cache, DTO mapper, validator state, action executor or platform-specific Login screens.** No ViewModel; Pure Store + MVI/UDF, entry-scoped Store, retained session/navigation owner.
- Strict SRP, separation of Domain vs data DTO/mappers vs runtime/UI vs composition vs platform integrations; no needless service interfaces, managers, adapters, generic property bags, speculative framework, unused extension or extra "helper" class. Neither underengineering nor overengineering.
- One generic Dynamic runtime, no Kotlin branch on screen ID, phone/OTP/name UI style, theme-specific visual defaults, platform-authored design dimensions, native icon glyph maps or arbitrary local fallback icons. Backend JSON authors design, bindings, control validation declarations, actions and destination. Generic OS safe areas are NOT backend UI padding.
- Type-safe concrete models/mappers/registrations per truly supported concept. No reflection-based registry, fail on duplicate and unsupported type; rendering delegates descendants via registry.
- Reference namespaces: $binding, $context, $state, $literal; **never $response**. DynamicValue supports String, Number, Boolean, Object, Array, Null; nested paths and preserved types. Sensitive request/response/OTP/user data must not be blindly logged.
- Use minimal Git operations; owner prefers only git pull + focused Gradle tasks. Avoid changing/switching branches, broad resets/reverts, backend or unrelated code. GitHub remote changes should be verified; local Gradle/device output only the user can execute on their Windows phone.
- Conversations may explain in Hindi/Hinglish; all project documents/KDoc/notes are English. Document class purpose, caller, ownership and why it exists (not narrating every obvious expression).
- Protected Web visual baseline VisualLoginPreview.kt remains read-only; do not adjust reference to conceal differences. Real JSON preview routes through normal DTO mapper, single Store and real dynamic renderers.

## 2. Modules and current source ownership

| Module/source root | Actual responsibility and important files |
| --- | --- |
| :core | Cross-cutting runtime metadata, safe logging/diagnostics; not a dumping ground |
| :domain | Typed DynamicResponse/Screen/Template/Component/Section/Group/Element/Content, DynamicValue/Validation/Actions/Destination and repository contracts; no Compose/HTTP |
| :data | DynamicResponseDto and six category DTOs, ElementMapper/TemplateMapper/ComponentMapper/SectionMapper/GroupMapper/ElementContentMapper, response/theme/action mappers, repository implementation, catalog, bootstrap mapping |
| :network | Shared Ktor/HTTP transport, errors/config/auth/log policy; not created by Dynamic screen |
| :navigation | DynamicNavigationEntry/State/Host, one back stack, screen push/replace/pop |
| :feature:splash | SplashStore, SplashScreen, startup/bootstrap handoff; no Dynamic element rendering |
| :feature:dynamic | DynamicContainer, entry DynamicStore/ScreenState, DynamicRuntimeState, DynamicReferenceResolver, DynamicFormValidator + named registry, DynamicActionExecutor/handlers/results, six-category DynamicRegistry/renderers, FormTemplate/TemplateShell, modal presentation |
| :app commonMain | App/Koin composition, retained DynamicSession, ApplicationContent, Splash-to-Dynamic handoff and effect consumer; current product-specific PartnerPhoneNumberValidator is under app/validation and under audit |
| :androidApp | Android ComponentActivity/metadata, development-only local preview Intent and APK asset packaging; currently also navigationBarsPadding host wrapper, **not accepted final cross-platform ownership** |
| app/src/iosMain + iosApp | ComposeUIViewController entry / Swift UIKit launch; no separate Dynamic renderer |
| :webApp | ComposeViewport + development-only local JSON visual routes, browser platform metadata and read-only visual reference |

Current Gradle dependencies: androidApp applies cbpartner.android.application + cbpartner.compose, implementation(compose.foundation), app/core/network/activity-compose. The Android-only Compose Foundation dependency was a narrow compiler fix at commit 21229a450e4c001554c70ffbdeef982d1bf415ab; do NOT claim build or visual success of that fix without owner logs. Distinguish platform Android source from source commonMain content.

## 3. EXACT Dynamic hierarchy and wire flow

API response carries status/code/message and data{screenId,schemaVersion,targetApp,template,theme?,presentations?,bindings?/other supported response fields}; verify any additional field against current DTO, not assumptions. Screen has **exactly ONE Template**, with **one or more Component instances**. Exact structural tree:

~~~text
DynamicScreen
  └── DynamicTemplate: exactly 1 (currently FormTemplate)
        ├── optional Header slot → Components (same six-category renderers)
        ├── Body: template.components[] → 1..n Components
        │     └── Component → 0..n direct Elements AND/OR 0..n Sections
        │           └── Section → 0..n direct Elements AND/OR 0..n Groups
        │                 └── Group → Elements only (1..n in field/list contracts)
        │                       └── Element → optional leading/trailing Content[]
        └── optional Footer slot → Components
Separate presentations (dialog/bottom-sheet) render a declared overlay; not a seventh structural category.
~~~

Exactly six logical categories **Template / Component / Section / Group / Element / Content**. Current SOURCE registration (overrides old inventories):
- Template: FormTemplate.
- Component: StackComponent.
- Section: StackSection, **FormFieldSection**, **ListSection**.
- Group: StackGroup.
- Element: TextElement, ImageElement, InputElement, ButtonElement, **DividerElement**, IconElement.
- ElementContent: DividerContent, IconContent, **ImageContent**.

Important historical correction: old doc 20 states "No DividerElement"; newer Login work and registry **do include DividerElement**, alongside DividerContent. Do NOT remove/duplicate based on old docs. FormFieldSection is a concrete reusable Section; do not move focus/validation/whole-control border to StackGroup. ListSection supports a source/items/filter/selection and emptySectionId referencing a sibling StackSection shown only as empty fallback. Country Picker behavior is working and must not regress. Future Grid/List/Carousel variants require actual backed contract, not speculative types.

Data path: backend/canonical local JSON → DTO decoding → DynamicResponseMapper → TemplateMapper → ComponentMapper → SectionMapper → GroupMapper → ElementMapper → ElementContentMapper (and action/validation/value mappers as necessary) → typed Domain DynamicScreen → DynamicRepository/LoadDynamicScreenUseCase → Entry DynamicStore state → DynamicContainer → DynamicRenderer and registered Template/Component/Section/Group/Element/Content renderers → Compose UI. Renderers must not parse JSON or call HTTP.

Template body padding must never secretly apply to header/footer; theme gradient is theme-owned; **full-size hero background is template-owned**. Current Login backgroundImage is resource images/partner_login_hero_02, contentScale=crop, alignment=topCenter. Template body is scrollable vertical spaceBetween.

## 4. Runtime references, actions, navigation, validation

- Bindings: screen/entry-scoped controlled inputs (e.g. mobileNumber, countryId, countryCode, countryFlagAsset), and independent presentation-local values; context contains allowlisted runtime metadata; session retained state holds values successfully committed by action; literal expressions are read-only.
- Input renderer emits onValueChange/focus/submit via existing callbacks → DynamicIntent → DynamicStore updates entry values, performs generic clearOnEdit/revalidation when authored → state flow → rerender. FormFieldSection reads validationErrors[its input binding] only to render outline/message; doesn't calculate validation.
- One event hook chooses one action (onClick,onLongClick,onValueChange,onFocus,onSubmit as supported); action dispatch → Store → DynamicActionExecutor → registered typed handler → ActionResult; Store/session commit state first and deliver navigation/external effects via one retained session effect consumer. NavigationState push/replace/pop is single authority; actions do not directly manipulate Compose/Nav stack.
- Current executor registered action types: request, navigate, present, dismiss, external_uri, state, sequence. **Sequence is an inert registered placeholder** (returns Completed; never executes child payload.actions), per doc61 superseding old doc20. State action handler currently uses payload key/value; backend state wire mismatch remains a separate gate. Present in current Login Country Picker uses presentationId and actual presentation host; old docs69 say backend targetId/presentation wire cannot be assumed compatible. Dismiss is modal-only; navigate pop/back differs. No speculative timers or OTP resend semantics.
- Request handler resolves endpoint/method/authentication, typed body with reference resolver, validation flag; sends via one DynamicRepository; can return retained state and destination navigation (push/replace) after success. Validate before request when declared; invalid prevents API. Error/cancellation/obsolete entry scope must not leak or commit effects.
- Current DynamicValidation DTO/Domain/Mapper supports required, pattern, message, requiredMessage, lengthMessages, clearOnEdit, optional validator{key,arguments}; DynamicFormValidator is already generic and traverses elements. **lengthMessages only chooses message AFTER a validation fails; it is NOT an independent length check**. Default messages "This field is required"/"Invalid value" currently live in runtime; do not silently rewrite without policy.
- DynamicValidator interface and DynamicValidatorRegistry are extension points, registered through Koin getAll in feature/dynamic/DynamicModule.kt. Unknown named validator key fails; duplicate registration fails.
- **OPEN OWNER CONCERN:** app/src/commonMain/kotlin/com/carbroz/cbpartner/app/validation/PartnerPhoneNumberValidator.kt implements validator key phone_number with India-only region IN, ASCII 10 digits starting 6–9; app/App.kt registers it in composition. Login JSON v11 explicitly requests validation.validator.key="phone_number", arguments.region={$binding:"countryId"}. This is actual screen/business-specific logic outside generic validator, and the owner questions its module ownership/existence. **Do not blindly delete**: existing screen can break and selectable countries beyond IN need contract compatibility. Audit named validator contract, backend actual supported-country/phone validation, request error behavior, test callers and DI. Decide whether fully backend-authored regex/typed generic rules suffice or some externally configured business validator remains necessary; do not move the same hardcoded phone logic to feature/dynamic or domain. Server must remain final authority.
- Login v11 clearOnEdit=true is authored, retains one generic Store; error state clears immediately on next edit but returns on submit while invalid. **Error message space is NOT reserved**: FormFieldSection shows actual message only when error; normal phone input→Continue gap is authored 16dp; error adds top6dp + text height and may alter card/top due spaceBetween. Historic docs131–135 fixed-slot paragraphs are superseded by doc138 for Login, not proof that pending OTP v2 reservation target is implemented. Reconcile OTP contract separately.

## 5. Current Login & Country Picker, owner screenshot parity

**Versioned active:** docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-parity-v11.json. Same file bytes as webApp/src/wasmJsMain/resources/preview/partner-login.json. Native Android *developmentDebug* bundles that Web JSON through generated Gradle asset task; Android special Intent reads it and passes it to shared parseLocalLoginPreview(raw)/App, reusing Mapper/Store/renderer. Historical v10/v9 snapshots not overwritten. Web URL query cbpartnerLoginPreview=1 uses the same live fixture; cbpartnerVisualPreview=1 is a separate **protected** hardcoded visual baseline, NOT dynamic.

- Login screen partner_login, template tpl_7K2M9Q, 2 components login_branding and login_form.
- Brand group: horizontal center, start/end24dp, top48dp, bottom32dp; leading logo32x32dp + gap9dp; brand text20sp/700/28sp #0A3A8D; brand→Welcome12dp.
- Welcome: 32sp/700/40sp #0A3A8D, center currently. Subtitle: 16sp/400/24sp #6B7280, center, heading→subtitle8dp. **Owner original full-size reference has Welcome/subtitle LEFT alignment**, brand remains centered. Do not silently alter board-vs-image conflict without freeze.
- Hero02: full-template background crop topCenter; on Samsung screenshot too much sky, skyline too low, worker head/back/arm/spray/waist and car visible too little; trees/palms and left/right clipping wrong. Need check source aspect ratio, contentScale, safe screen region, phone density/insets BEFORE regenerating/positioning asset. Original shows skyline immediately below subtitle, worker back down to waist, visible arm and washing car; do not focus only on vertical axis.
- Login form: white card top radius24dp, padding left/right/top16dp and bottom24dp; phone label14sp/600 #18263B with bottom8dp; entire controlSurface56dp, radius16dp, normal border #CBD5E1, focus #0A3A8D, invalid #EF4444. Composite row flag24x18, dial15sp/600, divider28dp, input placeholder16sp/400/24sp #9CA3AF; red tint #FEE2E2 ONLY on editable number input, normal flag/code/divider.
- Continue 56dp/radius16dp; font16sp/600/24sp white; button currently #007AFF. Legal description12sp/400 #6B7280; links14sp/500/20sp #007AFF, underline.
- Explicit original-vs-screenshot mismatches still OPEN: brand/logo size, top/status gap, logo/brand relative positions, Welcome/subtitle left alignment/typeface, font visual weight/size, label, legal readability, original button darker blue, original subtitle grayer/bluer, hero light pastel sky vs saturated current, worker/car/trees framing and card geometry. Design board #007AFF conflicts with original darker-looking button: establish reference priority and measured evidence rather than guessing hex/font. Exact original font family is NOT known from source/design board.
- **User confirmed Samsung native app launched and normal-state 32dp fixed error reservation removed**; do not reintroduce. Error, focus, keyboard, picker scenarios still require device regression screenshots.
- Country Picker is a same-screen presentation with list source/catalog, selected row styles, flags, search and emptySectionId fallback. Preserve current picker behavior and JSON. Not all country flag resources are verified; no invented assets.
- Native preview command:
~~~powershell
adb shell am start -S -W -n "com.carbroz.cbpartner.android.dev/com.carbroz.cbpartner.android.MainActivity" --ez cbpartner.login_preview true
~~~
Development APK installs as com.carbroz.cbpartner.android.dev; MainActivity actual namespace com.carbroz.cbpartner.android. The above **fully qualified** component avoids earlier Error type 3. Real device Samsung SM_A356E via wireless ADB, scrcpy screen mirror; screenshots can be captured using adb shell screencap -p and adb pull, and user shares PNG.

## 6. Exact history and evidence ledger in this conversation

| Area | Remote/source checkpoint | Evidence/status |
| --- | --- | --- |
| Login v10 design board typography/color | 4b8333c6 | Source authored, device acceptance not final |
| Hero_02 PNG user upload | c058f355 | User Git commit confirmed |
| Hero_02 explicit registration/JSON | 33111135 | GitHub source verified |
| Native development preview entry | 4813015d | Initial APK missing JSON; fixed below |
| Dev APK asset packaging attempts | 72213c6e → 2a27f01d | First static dir failed; generated assets task subsequently allowed user's native preview; avoid reliving fixed issue |
| v11 drop reserveHeight and 16dp gap | d6157e91; follow-up test fix f51062ad | Owner's Samsung screenshot confirms removal; earlier test fixture reserveHeight caused compiler error then fixed |
| Doc138 chronology correction | 1b06cd0d | Clarified spaceBetween/card movement |
| Android host navigationBarsPadding | 445cfa7a | Source changed; user encountered missing Compose Foundation imports/dependency |
| Android compile dependency fix | **21229a450e4c001554c70ffbdeef982d1bf415ab** | androidApp build.gradle adds cbpartner.compose + compose.foundation; **NEW BUILD RESULT NOT YET PROVIDED** |
| Latest user architecture decision | This chat handoff | Common-first, minimal platform code, SRP/clean architecture/no duplication; Android-only inset considered provisional and to be audited for commonMain move |
| Latest user validation ownership question | This chat handoff | Phone-specific validator under app/validation questioned; **NO APPROVAL to delete/move without backend/fixture test reconciliation** |

Device proof: screenshot shows reserve gone, but screenshot before common safe-area migration is not acceptance for new inset. DO NOT state safe-area fix works on device before new build/screenshot. Native old login came from backend until explicit intent preview and packaging corrected; never conflate normal bootstrap app with local JSON preview.

## 7. Open tasks — correct order, smallest safe waves

**Gate A — finish compilation/visual verification of current code (NO edits):**
1. Pull latest commit, run :androidApp:installDevelopmentDebug (or assemble before install); send build log. Earlier compiler failure was corrected in build.gradle but current success is unproven.
2. Start preview with explicit Intent, compare legal Terms row vs Samsung 3-button nav, keyboard open, picker overlay, dynamic scrolling and hero/card movement. If known Android fix works, it is a temporary accepted device correction, not final cross-platform placement.
3. If compile fails, fix only proven issue; do not change Login styling to conceal it.

**Gate B — focused cross-platform safe-area ownership audit/decision before migration:**
1. Read App.kt, ApplicationContent.kt, Android MainActivity, app iosMain MainViewController, iosApp Swift entry, web ComposeViewport, DynamicContainer/Renderer/FormTemplate/TemplateShell/PresentationHost/NavDisplay, Gradle dependencies, relevant tests, Compose version.
2. Check how navigationBarsPadding/safeDrawing/statusBars/IME insets behave across Android, iOS and Wasm/desktop/browser, whether root already consumes insets, and whether overlay/background should draw full-bleed while INTERACTIVE content stays above bar. Avoid double application of insets, unintended Web padding, and clipping photo/card. Top/status and bottom/navigation should be independently owned.
3. **Candidate not frozen final implementation:** move reusable inset policy to one common application boundary if compatible; strip Android-specific Compose Box/padding from MainActivity. Platform adaptation remains where actually required; do not unconditionally add navigationBarsPadding in all hosts without evidence.
4. One small approved patch, targeted Android/Web/possible iOS compile gates; Samsung device validation, then owner acceptance.

**Gate C — generic validation ownership architecture discussion/audit (no code yet):**
1. Enumerate every Input validation DTO/domain/mapper/source validator, Koin registrations and test/fixture call sites. Distinguish generic required/pattern/length/error messaging/edit policy from product semantics. Confirm phone_number named validator and region field in v11 JSON, choice of country list, real backend accepted regions and server-side validation responses.
2. Decide precise contract: (a) all supported rules backend-authored typed declarative generics (regex, min/max if actually needed), or (b) external product policy under proper owner with explicit named registry; do not fake comprehensive country-specific validator in runtime or app composition. Evaluate if "no phone-specific validator at all" is implementable without reducing correctness/security.
3. If approved, small tracked refactor, migrate JSON/backend contract first if required, update tests including missing/unsupported validator, non-IN regions, clearOnEdit, request validate flag. Preserve no hardcoded phone/OTP/name logic in Dynamic runtime.
4. Separate action from safe-area wave unless exact change is trivially coupled (currently NOT coupled).

**Gate D — Login UI parity once layout settles:**
1. At same Samsung A35 viewport and provided original screenshot, inspect each item: logo box/baseline and top offset, CB-Partner brand and Welcome/subtitle text alignment, font sizes/weights/line heights/family, label/country placeholder/button/legal metrics and colors, side margins, image crop top/left/right and card/footer spacing.
2. Reconcile original reference against design board and present JSON tokens; present only genuine conflicts needing owner choice. No speculative pixel perfect dp claims from scaled mockup. Version new JSON (v12 if approved) instead of rewriting v11 history.
3. Keep hero artifact changes isolated from renderer unless actual generic crop bug proved; reframe asset for worker upper body/arm/waist, left car, trees on both sides.
4. Device screenshots for normal, empty/error, editing, keyboard, loading, Country Picker/search/empty result; owner freezes P0 only after parity. No unrelated P1 refactor or OTP development before P0 acceptance.

**Gate E — separately deferred:** OTP JSON v2 is an unimplemented visual contract (timer/resend/segmented/error rules not proven); previous OTP reserved-slot targets conflict with new generic field model and need separate decision. Backend migration requires explicit separate authorization. P1 StackGroup selected-style/list empty reference/Icon selected-visibility SRP cleanup stays deferred. Deferred State/Present backend wire, old inert Sequence semantics, and speculative popTo/refresh remain unchanged.

## 8. Fast commands and verification

~~~powershell
cd D:\CarBroz\CB-Partner
git pull --ff-only origin architecture/dynamic-runtime-v2-discussion-freeze
adb devices -l
.\gradlew.bat :data:jvmTest :feature:dynamic:jvmTest :app:jvmTest :feature:dynamic:compileAndroidMain :webApp:wasmJsBrowserDevelopmentWebpack :androidApp:installDevelopmentDebug '-Pcbpartner.api.baseUrl=http://127.0.0.1:3000'
adb shell am start -S -W -n "com.carbroz.cbpartner.android.dev/com.carbroz.cbpartner.android.MainActivity" --ez cbpartner.login_preview true
adb shell screencap -p /sdcard/cbpartner-login.png
adb pull /sdcard/cbpartner-login.png .\cbpartner-login.png
~~~

The full command is the comprehensive wave; for tiny scope use only relevant Gradle targets. These commands were documented, **not executed from this GitHub connection**. Native offline preview requires neither running backend nor Web dev-server nor adb reverse. Normal bootstrap needs reachable backend and may use adb reverse tcp:3000 tcp:3000 if URL points to localhost. Do not run extra Git operations or claim Gradle was executed remotely.

## 9. Output/decision protocol for next chat

Start by saying current HEAD, authoritative docs read, classes inspected, confirmed source-vs-doc changes, exact unresolved blockers. Provide ONE compact dependency/owner matrix and ONE prioritized execution order. Never spend 20 messages asking the owner to reconfirm already settled principles. Discuss **actual** contract questions only (e.g., should backend own regex versus named plugin; safe-area background/content semantics; board vs original color authority). Otherwise deliver a small targeted patch and user Windows commands. Reconcile after owner build/screenshot. All docs English, chat explanation Hindi/Hinglish. Do not invent test results, latest backend HEAD, font family, unsupported JSON schema, screenshot dp or platform guarantees.

**Separate next-chat copy/paste prompt:** 141-NEXT-CHAT-STARTER-PROMPT-COMMON-FIRST-SAFE-AREA-VALIDATION-LOGIN-PARITY-2026-10-23.md.
