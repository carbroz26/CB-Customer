# 167 — Paste-Ready New Chat Prompt: Complete CB-Partner V2 Context and Partner Type Selection Continuation

**Paste the following into a NEW CB-Partner project chat. This is an operative instruction to the next assistant.**

---

I am continuing my CB-Partner Kotlin Multiplatform + Compose Multiplatform SDUI project. The previous chat ended after (1) all current Splash, Login v19, Country Picker, OTP v3, six generic dialogs and reusable success Toast V1.1 were approved and physically verified on my Samsung A35 development APK, and (2) we deeply discussed the next Partner Type Selection screen, froze generic ordered mixed children at the JSON/Domain design level, and created a complete Partner Type JSON REVIEW CANDIDATE. **This is NOT a request to redesign completed UI, implement Backend immediately, or assume the Partner Type JSON is already executable.**

## A. Repos, branches and strict authoritative documentation

Frontend repository: https://github.com/carbroz26/CB-Partner
Local Windows repo: D:\CarBroz\CB-Partner
Active Frontend branch: architecture/dynamic-runtime-v2-discussion-freeze

Backend repository: https://github.com/carbroz26/CB-Backend
Active Backend branch: development (main retained). Backend is READ-ONLY for current work until I separately authorize live integration/migration.

The ONLY authoritative project planning and freeze directory is docs/architecture-proposal/dynamic-runtime-v2/. Do **NOT** base decisions on other unapproved project docs or summaries when this V2 folder + source is the authority. **READ EVERY file in that V2 folder thoroughly, not just this prompt, the README, or the newest docs.** Read the existing ~163 Markdown docs plus JSON fixtures in the folder (confirm actual count on fresh HEAD). Resolve historical documents and newer amended/frozen decisions chronologically. Never promote an old draft over an approved newer version. This must be an actual reading/reconciliation exercise, not an assertion that the files were read.

Mandatory first reading order:
1. V2 README.md and 165-2026-10-26-COMPLETE-HANDOFF-FRONTEND-DIALOG-TOAST-PARTNER-TYPE.md — full current checkpoint and what was actually accepted/pending.
2. 166-COMPLETE-V2-DOCS-AND-SOURCE-FILE-AUDIT-INVENTORY.md — all source paths + every V2 Markdown file; checklist for full audit.
3. This 167 prompt.
4. 164-PARTNER-TYPE-SELECTION-ORDERED-JSON-REVIEW.md and its complete partner-type-selection-ordered-v1-CANDIDATE.json.
5. 163-ORDERED-MIXED-CHILDREN-JSON-DOMAIN-MAPPER-CONTRACT-FREEZE.md.
6. 162-GENERIC-TOAST-V1-1-NAVIGATING-SUCCESS-AMENDMENT.md, 161-GENERIC-TOAST-PRESENTATION-V1-FREEZE.md, 160-APPLICATION-WIDE-GENERIC-DIALOG-V1-IMPLEMENTATION-PLAN.md.
7. 157-LOGIN-OTP-BACKEND-FRONTEND-EXACT-CONTRACT-RECONCILIATION-FREEZE-2026-10-25.md, 158 full old handoff, 159 old starter prompt, and 155/156 OTP countdown and resend evidence.
8. All remaining V2 documents fully; categorize old vs amended vs accepted vs open from actual dates/status. Read the COMPLETE current canonical Login v19 and OTP v3 JSON, not merely names.

The last chat's documentation commits created 165 and 166 and then 167. GitHub HEAD may advance, so verify latest actual branch HEAD before any work.

## B. MANDATORY deep line-by-line source-code audit before future implementation

**Read every module, every package, every Kotlin class and its complete source LINE BY LINE, including tests, Gradle config, JSON resources and platform entry points.** The 166 inventory enumerates 224 Kotlin files as of its snapshot. Do not read only filenames, rely on cached memories, only inspect 10-line snippets, or claim line-by-line verification you have not performed. If there are updates, include new files. Understand every class's purpose, caller/callee, dependency and single responsibility; reconcile each frozen document with what code ACTUALLY implements.

Modules to audit:
- app, androidApp, webApp, core, network, domain, data, navigation, feature/dynamic, feature/splash, build-logic.
- App.kt, ApplicationContent.kt, AppDialogEntry.kt, DynamicSession.kt, LocalDynamicPreview.kt, ActiveToast.kt, Android MainActivity.kt, WebDynamicPreview.kt, all DI modules.
- SplashStore/SplashScreen, BootstrapRepository/DTO/Mapper, NetworkException/NetworkFailureMapper/shared RequestHandler.
- DynamicContainer, DynamicStore (~983 lines), DynamicScreenState, LoadDynamicScreenUseCase, DynamicRuntimeState, DynamicReferenceResolver, DynamicFormValidator, DynamicCountdownClock, DynamicListItems.
- DynamicResponseMapper, DynamicValueMapper, TemplateMapper, ComponentMapper, SectionMapper, GroupMapper, ElementMapper, ElementContentMapper, PresentationMapper, all DTOs & Domain models.
- DynamicActionExecutor, Request/Navigate/Present/Dismiss/ExternalUri/State/Sequence handlers and DynamicActionResult, all tests.
- DynamicRendererRegistration, DynamicRegistry, FormTemplate/TemplateShell, StackComponent/StackSection/FormFieldSection/ListSection/StackGroup renderers, ALL Element and Content renderers, DynamicPresentationHost and asset registry/fallback.
- NavigationState/Host/Entry, all field validation/list selection/semantic-layout/focus tests.
- Read actual Backend PartnerType.ts, partner routes, controllers/DTO, SDUI schema/builder/actions, Bootstrap and OTP contracts **read-only** for current compatibility decisions; a later Backend-migration wave requires its own deep source audit.

Check differences between source truth, frozen docs, pending plans and old drafts; record only verified issues. Source + approved latest freeze is authority; if they conflict, flag exact file/line and reconcile before implementation.

## C. Strict architecture / cardinality / rendering

Kotlin Multiplatform + Compose Multiplatform, Clean Architecture, MVI/UDF Pure Store; **NO ViewModel**, no duplicate Store, no screen-specific HTTP client, no copy of navigation/cache/asset systems, no generic magic renderer/property bag, no reflective dispatch. Strict SRP: one class one reason to exist. Explicit six-category typed registries and matching DTO/Domain/Mapper/Renderer. Source currently registers:
- Template: FormTemplate, exactly one Template per Screen.
- Components: StackComponent; one concrete type can have multiple instances per Template.
- Sections: StackSection, FormFieldSection, ListSection.
- Groups: StackGroup, only Elements as children.
- Elements: TextElement, ImageElement, InputElement, ButtonElement, DividerElement, IconElement, IconBadgeElement.
- ElementContent: DividerContent, IconContent, ImageContent via leading/trailing accessory lists; spacing belongs to accessory, do not invent accessory padding. **DividerElement AND DividerContent are both currently registered; older removal plans are historical.**
Header/Body/Footer are Template slots, not new categories. Body padding not duplicated on Header/Footer. Theme contract unchanged. Assets explicitly mapped, backend-authored URL/resource/fallback, no glyph-name default mapping. No visual default padding/radius/color/size/typography/selection style in Compose; author values in JSON. Future Component/Section/Group types may be added ONLY for real new backend concepts, not speculative generic types.

**Newly FROZEN ordered child rule, not yet implemented:** StackComponent may interleave Element/Section in ANY authored sequence; StackSection may interleave Element/Group in ANY authored sequence; StackGroup continues Elements-only. Old elements+sections / elements+groups arrays cannot express this (actual renderer currently renders all Elements first). Doc 163 freezes mutually exclusive JSON children[] format and typed Kotlin child wrappers + legacy projections; preserve existing Login v19/OTP v3/Dialog JSON unchanged. Do NOT assume it works today. Its DTO/Domain/Mapper/rendering/validation/button lookup/ListSection fallback traversal still needs an authorized implementation.

## D. Dynamic runtime / bindings / actions / networking ownership

Actual flow: Bootstrap SplashStore -> four-field DynamicDestination {screenId,templateId,templateType,endpoint} -> retained DynamicSession/navigation entry -> DynamicStore load/validate/execute -> shared Repository/Network -> typed DTO -> Domain model -> registry/Compose UI -> DynamicIntent.Action -> generic executor -> accepted retained state changes and ordered effects -> app consumer claim/ack -> navigation/presentation. Do not introduce duplicate navigation stack or reimplement HTTP in elements.

DynamicValue String/Number/Boolean/Object/Array/Null; $binding (entry form), $context (runtime metadata), $state (retained session + computed countdown), $literal, $item (list rows), $config (allowlisted trusted legal URIs). Nested resolver paths supported; $response is FORBIDDEN. Backend result retain maps data.* paths to approved state; never publish tokens/OTP secrets as generic state/logs.

Registered handlers: request, navigate, present, dismiss, external_uri, state, sequence. Sequence is INERT; never execute nested actions. Request is generic, validation + enabledWhen + retain + responseMode:none/destination and optional successPresentationId; no per-screen auth branching. Navigating success with Toast emits ordered Navigate then Toast from original Login screen snapshot; OTP Resend success emits Toast same screen. Failure never resets timer/challenge or shows success Toast. Duplicate action/entry generation/cancellation guards remain. Review state and action tests before edits.

## E. ALREADY ACCEPTED — preserve, do NOT reopen

Splash: native Connecting / Setting up visuals, bootstrap normal, maintenance, required/optional update and error scenarios. Generic six app-wide dialogs: App Under Maintenance, Update Required, Update Available, Connection Problem, Something Went Wrong, Secure Connection Error. Eight packaged JSONs including server_error_startup and secure_connection_startup safety variants; shared Dialog renderer/action and Retry/Cancel; owner saw correct UI and rotation retention. No invented offline mode or maintenance ETA. Real TLS-specific exception classification still a future live integration gap, not a claimed completed feature.

Login v19: canonical JSON docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-parity-v19.json ; partner_login / tpl_P101L; matching Web and Android fixture. White bottom card, existing hero branding, Country Picker actual bottom_sheet with list/filter/selection, form field/input and Continue. OTP v3: docs/architecture-proposal/dynamic-runtime-v2/partner-otp-ui-contract-v3.json ; partner_otp / tpl_P102O, six-digit focus/paste/delete rules, timer and Resend, Verify. Reference screenshots/design decisions already approved; preserve.

Generic Toast V1/V1.1: white rounded card, small green circular tick, dark-green message, NO close X, white bottom track and green 0->100% animation, auto-dismiss; app-root retained active Toast survives rotation with progress continuing. Initial Login Send OTP success navigates to OTP with 'An OTP has been sent to…'; OTP Resend success shows 'A new OTP has been sent to…'. Both were manually verified on Samsung A35 by owner including rotation. Backend live edge cases are deferred; do not claim production parity.

Current actual preview workflow: opt-in DEBUG+Development in Android MainActivity and WebDynamicPreview; existing mapper/Store/renderers with development repository/Bootstrap/catalog overridden, **not a hardcoded screen UI**. Android flags:
  --ez cbpartner.login_preview true
  --ez cbpartner.otp_preview true
  --ez cbpartner.otp_resend_failure true (with otp_preview)
  --es cbpartner.preview_network_failure CONNECTION|SERVER|SECURE_CONNECTION|UNKNOWN
  --es cbpartner.bootstrap_preview normal|maintenance|required_update|optional_update
  --es cbpartner.bootstrap_error CONNECTION|SERVER|SECURE_CONNECTION|UNKNOWN
  --ez cbpartner.retention_probe true (diagnostic)
Local 10-second timer is preview-only; Backend currently 60s. Repo compiled/installed successfully on Samsung A35; more recent full consolidated JVM/Web build on exact present HEAD is not certified by owner messages. Use adb devices -l to choose exact serial if the phone has two ADB transports. Android component com.carbroz.cbpartner.android.dev/com.carbroz.cbpartner.android.MainActivity. Existing login preview successful Send OTP navigates to mapped OTP fixture; Web has matching preview. No real/backend-authenticated e2e claim from preview.

## F. **CURRENT ACTIVE WORK:** Partner Type Selection — discuss/freeze exact JSON, then plan, then implement

The user supplied TWO images: large Partner Type screen and Samsung A35 six-state design/spec sheet. We have agreed:
- Reuse the Login/OTP hero background and branding; title 'Choose your partner category', subtitle as authored; white bottom rounded StackComponent orientation vertical.
- Bottom panel EXACTLY FOUR StackSections, in order: Individual Partner, Organization, Partner Under Organization, and Help text link.
- Each of the first three is a clickable HORIZONTAL StackSection owning full card background, border, corners, min height, pressed/selected state and action.
- Each card's direct **ordered children** are [IconBadgeElement avatar 64dp, StackGroup vertical {TextElement title + TextElement description}, IconBadgeElement arrow badge 40dp]. NO PartnerTypeCardElement, no separate Card Component, no outer StackGroup for card. The Section is the card.
- Help is fourth StackSection with white unrounded background and a TextElement with leading help icon. Click uses existing present({presentationId:'partner_type_help'}) and opens existing BottomSheetPresentation. Sheet uses same simple image/avatar + vertical text Group rows (no arrows), header/X and 'Got it' Button with existing dismiss action. No new bottom sheet renderer or new help screen.
- Ref spec recommends 24dp screen margin, 96dp card BASE height (use minimum for text accessibility), 16dp card gap, 64dp image, 40dp arrow badge, card 16dp radius and 1dp border; exact authored properties must be compared to screenshots. Don't mix conflicting long/compact image variants without reviewing.
- Error: **REUSE already approved generic error Dialogs**. NO red error banner, no Error Toast. We ONLY need to decide a GENERIC Loading Shimmer/Skeleton that can render three placeholder cards while request pending. Initial first-load has no Backend screen JSON yet; do not invent a JSON-triggered loader before screen is received. Resolve loading source/lifecycle + JSON styling + cancellation/rotation/disabled state first. Shimmer is not a unique Partner Type composable.
- Doc 163 ordered children rule is frozen but implementation pending. Doc 164 and complete partner-type-selection-ordered-v1-CANDIDATE.json contain an exact **REVIEW CANDIDATE**, not a runnable/final JSON. Candidate includes proposed tpl_P103T screen identity, THREE card Section actions and help sheet; unsupported source fields are deliberate.
- Real generic gaps to reconcile: StackSection.actions + pressedStyle + minHeight, StackGroup.weight for text remaining width, DTO/Domain/Mapper/renderer interleaving, asset registry + three avatar images and arrow/help/back resources, skeleton state/behavior. Don't add new classes before source evidence.
- Backend development currently exposes PartnerType only INDIVIDUAL and ORGANIZATION; third UNDER_ORGANIZATION is a PROPOSAL only. Existing /register/individual and /register/organization routes require businessName, not a verified post-OTP selection endpoint. Candidate endpoint /__contract_pending__/partner-type-selection intentionally nonproduction. Do not invent actual API, success destination or KYC branch. Audit Backend read-only, then discuss/approve the exact business identifiers/endpoint/auth/body/response, and only later implement Backend if separately authorized.
- KYC and Training are the NEXT screens AFTER Partner Type acceptance; do not start their UI or implementation now.

## G. Productive next steps — don't turn small work into lengthy plans

1. First perform mandatory all-V2-doc and source reading, then give me a COMPACT checkpoint: latest branch heads; accepted versus pending status; source-exact hierarchy/renderer limitations; Partner Type JSON review candidate differences; true open decisions. No vague assurances and no repetition of already answered questions.
2. Continue the discussion starting from 163 and 164, not from scratch. Decide only minimal generic changes needed for ordered child rendering, whole-card click/pressed interaction, group weight/minHeight, and shimmer loader. REUSE all existing networking, Dialogs, actions, presentations and source classes.
3. Freeze a precise, valid final Partner Type JSON contract only after actual remaining business/API and asset/loader decisions. If necessary make a narrow V2 doc amendment; don't label the proposed JSON functional while unsupported.
4. Once I explicitly approve implementation, create a short exact file-by-file manifest, implement the smallest batch on the existing branch, run focused tests + Android/Web Gradle build and Samsung A35 debug preview/visual verification. Preserve Login, OTP, Splash, Dialog and Toast regressions.
5. Update V2 docs with completed tests and pending gates (do not silently mark tests passed). If implementation error arises, source-find it and fix the smallest concrete defect immediately, not a broad refactor.

**Strict developer/workflow principles:** DISCUSS → SOURCE AUDIT → DECIDE → short PLAN only for real multistep work → FREEZE → IMPLEMENT → TEST → REVIEW → FREEZE. Fast productivity WITHOUT guessing, blind edits, duplicate classes or broken contracts. Chat in concise Hindi/Hinglish; English-only project docs, KDoc/code. Follow SRP, clean architecture, pure Store and Compose Multiplatform. No unnecessary new branches, Git reset, giant plans for minor fixes or repeated approvals. Prefer only git pull/fetch and necessary targeted Gradle commands. Never re-ask facts already provided. Remember user specifically requested understanding the ENTIRE project before implementation; but keep the visible audit conclusion compact so progress is fast.

**Start by reading 165/166/167, then all other V2 documents and all current source line by line. Next give a brief, evidence-backed Partner Type Selection contract checkpoint. Do not change production or Backend code without authorization.**

---

**End of paste-ready prompt.**
