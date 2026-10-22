# 126 — CB-Partner Login + Country Picker Dynamic Parity: Detailed Recovery & Next-Chat Handoff
**Prepared:** 2026-10-23 (India); prior work mainly 2026-10-21/22
**Frontend:** https://github.com/carbroz26/CB-Partner
**Active branch ONLY:** `architecture/dynamic-runtime-v2-discussion-freeze`
**Source HEAD inspected BEFORE this documentation commit:** `791ebe6261151c6113c387814ffd0ca337db9abf`
**Backend READ-ONLY:** https://github.com/carbroz26/carwash-backend, `feature/backend-migration`, last observed `5ec0f18cd0e7103a5bb2c8b19126d0b447369c07`
**Current goal:** Make frozen JSON and the *actual generic Dynamic Runtime* render Login + Country Picker like the owner-approved visual prototype. **OTP work is paused** except preserving its UI contract.

## 1. Critical distinction — DO NOT CONFUSE TWO PREVIEWS

- `?cbpartnerVisualPreview=1`: `webApp/src/wasmJsMain/kotlin/com/carbroz/cbpartner/web/VisualLoginPreview.kt`. Owner explicitly confirmed it now looks proper. This is a **hardcoded, mock, standalone Compose UI**, with an 8-country list, temporary skyline/shield/flag drawings, its own Compose state, and hand-built Country Picker. **It is not production JSON Dynamic rendering. NEVER break or overwrite it.**
- `?cbpartnerLoginPreview=1`: `WebLoginPreview.kt` loads `webApp/src/wasmJsMain/resources/preview/partner-login.json`, decodes via real `DynamicResponseMapper`, injects an offline `DynamicRepository`, and uses existing `App`, `DynamicStore`, `DynamicRenderer`, `DynamicPresentationHost`, registries, domain models and actions. This is **the true Dynamic visual-parity target**, still unverified.
- Normal app `App(...)`: splash/bootstrap then production Dynamic Repository/Network; backend must eventually emit exactly approved JSON and approved catalog/assets/config.
- Success criterion: live backend JSON → the existing typed Dynamic pipeline → owner-approved visuals and interactions, **without** hardcoded Login/Country Picker production renderers. A successful dummy screenshot is NOT acceptance evidence for the Dynamic pipeline.

## 2. Complete chronology / freeze reconciliation

1. Login original design and 12-state Samsung Galaxy A35 UI references reviewed/approved, including Country Picker, keyboard, loading, validation, rate limits, server/network error alert styles, white edge-to-edge lower card, original photo/branding; see owner attachments and Docs 112+. References may contain historical variations: reconcile against *later* freeze and source, not blindly merge.
2. Docs 112–118 established typed Login/Country Picker, B3b image owner, B4 pending/action semantics, B5 `ListSection.emptySectionId` sibling fallback, B6 and L01–L15 roadmap. **Doc118 has a frozen consolidated plan, not proof every task was delivered.**
3. `partner-login-country-picker-current-target.json` is historical B5 audit baseline, **still contains obsolete Theme-owned image**. DO NOT silently copy/overwrite it.
4. `partner-login-country-picker-integration-v2.json` moved hero to `template.properties.backgroundImage`, restored exact `theme.properties.gradient` and is preserved historical integrated target (Doc120).
5. Approved Web visual preview had Phone input/country picker regressions after an unverified OTP implementation wave. Recovery commit `f8b76120234a8d5a1ea1486c8f7674ddd9839d58` restored the known-good working visual and production source baseline non-destructively, preserving only **whole-row phone error border and numeric-field-only tint** (Doc124). All OTP P0–P5 speculative source changes from that wave were rolled back; their old commits remain Git history only.
6. Owner confirmed recovered Visual Login/Country Picker now proper. It was clarified explicitly that **hardcoded visual != actual Dynamic**. Owner prioritized Login + Country Picker actual Dynamic rendering parity, pausing OTP implementation.
7. New parity increments after recovery: `47725b3f` aligned offline Dynamic sample catalog from 4 → 8 visual-only countries; `2575c74d` typed generic sheet `heightFraction`, `background`, `shape` and StackComponent `fillMaxHeight`; `f18da833` versioned JSON `partner-login-country-picker-parity-v3.json` and Web fixture; `e208635f` added `BottomSheetLayoutContractTest`; `9fa5e7ff` generic Group rounded child clipping; `2ea5a325` generic FormTemplate full-height scroll/spaceBetween bottom anchoring; `791ebe626115` documented those parity changes in Doc125.
8. **Current state is NOT complete**: these are source changes, **NOT independently demonstrated passing JVM, Android, Wasm build or A35/visual acceptance**. No backend modifications. Never label them done because a commit exists.

## 3. Authoritative documentation and source reconciliation

The exclusive planning/implementation documentation folder is `docs/architecture-proposal/dynamic-runtime-v2/` (at least 125 markdown documents BEFORE this handoff). **Next chat must read ALL V2 documents fully** (not just latest or named snippets) and ALL frontend packages/modules/classes line-by-line including tests and configuration. Source code determines what's implemented; later chronologically frozen documents override earlier historical candidates **only for explicit decisions they supersede**. Check later reversals and rollbacks before claiming status. Treat source/document mismatch as a reported gap, not permission to invent features. Do not rely on other project docs as architectural authority.

Particularly important: Docs 112, 113, 114, 115, 116, 117, 118, 120, 121, 122, 124, 125, THIS Doc126 and corresponding prompt Doc127. Doc123 was introduced in the rejected P0–P5 wave and is **NOT present in recovered current tree**; it is evidence in prior Git history only, not active source-truth.

JSON files:
- Historical B5: `docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-current-target.json` (obsolete Theme-background candidate; preserve).
- Historical integration: `docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-integration-v2.json` (correct Theme vs Template ownership).
- **CURRENT Login Dynamic parity target:** `docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-parity-v3.json` (one JSON screen, full country picker, 0.70 viewport sheet height).
- **Current live offline preview fixture:** `webApp/src/wasmJsMain/resources/preview/partner-login.json`; exact JSON semantic copy of parity-v3 at preparation.
- OTP original UI v1: `docs/architecture-proposal/dynamic-runtime-v2/partner-otp-ui-contract-v1.json`.
- **CURRENT OTP UI-only frozen target:** `docs/architecture-proposal/dynamic-runtime-v2/partner-otp-ui-contract-v2.json`; no active OTP runtime work, no `?cbpartnerOtpPreview=1` in recovered source.

Read `VisualLoginPreview.kt` and `Main.kt` as the visual reference/routing truth. Source coverage must include **each class** in `core`, `domain`, `data`, `network`, `navigation`, `feature/splash`, `feature/dynamic`, `app`, `webApp`, `androidApp`, common tests, Gradle settings and actual DI registrations; specifically DTOs, mappers, models, six typed render categories, list evaluator, validation, Store/effects, Koin, asset security, Web routes and Theme/Template. Do not assume remembered paths/behavior without finding exact current source.

## 4. Strict frozen architecture and non-negotiable coding rules

- Kotlin Multiplatform + Compose Multiplatform; Clean Architecture, MVI + UDF, single **Pure Store**, no ViewModel.
- Typed hierarchy: Screen → exactly ONE Template (unique templateId per screen instance) → Components (same concrete component category, 1..n instances) → Sections and/or direct Elements → Groups and/or direct Elements → Elements with separate leading/trailing ElementContent. Existing concrete FormTemplate, StackComponent, StackSection/ListSection, StackGroup, Text/Image/Input/Button/Icon/Divider Elements, typed Icon/Divider/Image Content; inspect current source before adding category.
- Six typed registries (Template/Component/Section/Group/Element/ElementContent); explicit registration, duplicate registration fails; no reflection or generic JSON-property bag to bypass typed DTO/domain/mapper/renderer contracts.
- Existing typed values; permitted reference namespaces `$binding`, `$context`, `$state`, `$literal`; list-row `$item` and reviewed allowlisted legal `$config` in their authorized scopes; **never `$response`**. Confirm resolver and reserved-name validation in source.
- Frozen action vocabulary and separate handlers: `request`, `navigate`, `present`, `dismiss`, `external_uri`, `state`, `sequence`. No new executor, duplicate navigation/network/cache stack or fake screen-specific Store. Sequence terminal/effect rules stay frozen; do not reinterpret.
- SRP one class = one reason to change; no duplicate wrappers/no-op abstractions/overengineering; before adding any class or property, prove current type/mapper/renderer cannot satisfy the requirement.
- UI styling MUST be backend-authored and typed: padding, spacing, corners, dimensions, borders, colors, typography, align, scaling, icons and fallbacks. Do not introduce hardcoded production defaults, inferred business semantics, or new visual design/pattern. Preview-only assets/data may remain hardcoded **only** inside preview boundary, never masquerade as backend authorization.
- Theme keeps **only** its separate 135° linear gradient and statusBar; hero image belongs to Template `backgroundImage` and source must be approved trusted URL; no `theme.properties.backgroundImage`. Do not replace with resource or invent reachable image.
- Country Picker uses `ListSection` with `emptySectionId` sibling StackSection conditional fallback (never render that fallback as a regular sibling and again on empty). Typed source/filter/list key/selection/selected styling; no hardcoded list filtering in renderer if shared evaluator exists. Identity and atomic update should follow existing Store and navigation-entry ownership.
- Sensitive logs: never expose OTP, token, mobile number, full payload/PII. Keep safe request error sanitization.
- Preserve branch history; work **only** on `architecture/dynamic-runtime-v2-discussion-freeze`; no temporary branches, no destructive reset, no unrelated commits. Backend `feature/backend-migration` is **read-only** until separately co-authorized.
- Method: DISCUSS → RESEARCH → DECIDE → PLAN → FREEZE → IMPLEMENT → TEST → REVIEW → FREEZE. Already confirmed behavior may be worked within authorized Login parity scope, but new product semantics require review/freeze first. Do not change frozen contracts to make existing code fit without explicit review.

## 5. Approved Login visual and validation semantics

- White full-viewport-width lower Login card, upper 24dp rounded corners only, internal 24dp side padding; Theme cyan/light 135° gradient; Template-only hero. Approved styling/screenshots remain attached to owner conversation; **re-attach on next chat** and use visually.
- Country selector, flag, code, thin grey divider, phone number row, continuation button and legal links.
- **Error border is #DC2626 on the entire rounded 56dp phone row** (including selector, flag, country code, divider and editable number).
- **Error fill #FFF1F2 is ONLY behind editable phone-number area**; selector/code/divider remain white. **No second inner red border** on just the number.
- Inline phone validation message is under the row. Owner wants reserved space when hidden to prevent white card height fluctuations; **not yet implemented in recovered Dynamic renderer**, no silent key acceptance.
- Login has separately approved **network/server/rate-limit alert card distinct from inline input validation**. DO NOT put transport errors as validation text **under input**. For OTP, even an alert card inside its white form is expressly rejected.
- Country Picker sheet edge-to-edge, white, top radius 24dp, 70% viewport-height target, drag handle 36x4, header/search/rows/filter/selected marker/empty fallback. The current JSON v3 declares sheet shape/background/height; list remains dependent on real catalog + assets. Current 8 preview countries are synthetic UI-only samples, **not authorization to support 8 countries in production**.
- Design assets are external blockers: approved actual CB-Partner logo, hero image, icons, flags not packaged in DynamicPackagedAssets. Preview-only Canvas graphics must not be ported as production glyph fallback.
- Existing `/images/partner_login_hero.jpg` is a **reference URL, not a verified hosted asset**. Legal `$config` URLs and backend-supported country matrix also require real approved configuration.

## 6. OTP frozen UI-only contract — NO OTP implementation in current task

Current authoritative JSON `docs/architecture-proposal/dynamic-runtime-v2/partner-otp-ui-contract-v2.json`, superseding v1 only per Doc122:
- Same Theme gradient/Template hero / white full-width bottom form and reused upper branding; extra title `Verify your number`, instruction `We've sent a 6-digit OTP to`, retained phone text and `Change phone number` existing `navigate(mode:pop)`.
- ONE segmented InputElement, binding `otp`, six 56x56dp slots, 5dp gap, radius 16, centered 24sp digits, empty dash `—`.
- Normal border #D1D5DB and focus border #3B82F6. On invalid OTP **ALL SIX** slot borders #DC2626 AND ALL SIX backgrounds #FFF1F2, including focused slot, **NO outer Group error border**.
- One reserved inline code validation message below the six boxes; no generic "Unable to verify", transport/server/network error **under the input** or inside OTP white bottom sheet. Non-code failures must not be misclassified as code-invalid.
- Copy/paste/backspace: sequential filling; selected cell replacement only; delete occupied cell without shifting later values; backspace on empty cell focuses previous without deleting; paste from selected position overwrites forward ignoring overflow. One binding/source of truth, no six independent Stores.
- Timer is **STATIC TextElement** "Resend OTP in 00:30" right now; `OR` TextElement with leading/trailing DividerContent; Resend is **STATIC non-clickable/disabled TextElement**. **NO real timer, resend request, confirmation dialog or restart** in frozen v2.
- In future after separate freeze, Resend tap → ONE direct existing `request` action; never prompt "Request New OTP?" dialog. Backend currently enforces 60 sec cooldown while reference has 00:30; must reconcile backend-authoritative deadline before enabling.
- `Verify & Continue` authored button with illustrative `request`, loading and proposed `enabledWhen` target; code/role/challenge response/backend contract **not production verified**. Actual recovered renderer does not implement many new v2 properties (nullable Group errorBorder, all-six invalid border, dash placeholder, reserved messages, enabledWhen); don't claim source implemented because JSON declares them.
- Network/server feedback outside OTP form remains **undecided**; no silent success and no invented toast/dialog. The 12/14-state boards are visual references, not proof every recovery transition is product-frozen.
- OTP implementation paused until Login + Country Picker actual Dynamic parity is accepted.

## 7. Current Login actual Dynamic parity increments — SOURCE IMPLEMENTED, NOT VERIFIED

Commits in order after recovery:
- `47725b3f`: 8 sample offline countries aligned with approved visual prototype in Web-only `WebLoginPreview.kt`.
- `2575c74d`: typed `BottomSheetPresentation.heightFraction/background/shape`, `PresentationMapper` validation, generic `DynamicPresentationHost` bounded rounded surface and `StackComponent.fillMaxHeight` mapped/laid out.
- `f18da833`: `docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-parity-v3.json` and Web fixture add 0.70 fraction, white background, top corner shape, `picker_content.fillMaxHeight=true`.
- `e208635f`: `BottomSheetLayoutContractTest` added (unexecuted in this chat).
- `9fa5e7ff`: generic `DynamicLayout.applyLayout(StackGroup)` clips row child backgrounds to authored rounded shape, validation message stays outside.
- `2ea5a325`: `FormTemplate` scroll + full-size mode gets minimum viewport height, so authored `spaceBetween` anchors Login card bottom when content fits.
- `791ebe62`: updates Doc125 with last two implementation descriptions. **Starting source HEAD inspected**.

The source was inspected for these commits; no successful Gradle/Android/Web verification or exact screenshot parity has been observed for this latest batch. Do **not** add yet another broad wave before compile and preview acceptance.

## 8. Gap ledger / current priorities

**P0 – Do NOW:** Pin live HEAD/branch; read all V2 docs and actual Kotlin current source as above; run narrow compile/tests for the six touched areas (data/domain/feature:dynamic/app/web), record real failures. Review `BottomSheetLayoutContractTest` and `FormTemplate` viewport code for compatibility/correctness before claiming tests pass.

**P1 – Dynamic side-by-side:** Run `?cbpartnerVisualPreview=1` vs `?cbpartnerLoginPreview=1` at A35 411x891 + wider screen. Compare: hero/gradient/header/branding, 56dp phone row and rounded red error, input caret alignment, bottom card edge-to-edge/anchoring, validation message expansion, Country Picker 70% height/top radius/mask, header/search, row selection, filtered/empty result, keyboard, legal links. Do not touch approved dummy. If build fails, fix one proven generic cause, verify, then continue.

**P2 – Actual unresolved source vs JSON:** Reserved inline validation-space generic contract (currently absent); error types and server cards per approved Login; real logo/flags/icons/hero, trusted URLs, legal config, production country support, backend Send OTP payload/destination agreement. Do not invent data/assets or product decisions.

**P3 – Compatibility and functional:** Ensure generic typed JSON properties are parsed and rendered (no silently ignored values), Country Picker can open/search/select/dismiss atomically with correct retained bindings, no stale callback, proper inaccessible/empty states, no false multi-country OTP support.

**P4 – Production integration after owner acceptance:** Have backend emit exact agreed versioned JSON and required real data/assets; backend changes only after explicit permission. Verify splash → Login → Country Picker → OTP with real network, request/response, errors/cancellation and secure logs.

**Paused:** OTP runtime, OTP timing/resend actions, OTP API production integration; preserve JSON v2 and Doc122.

## 9. Practical commands and working style

Minimal local Windows commands:
```powershell
git pull --ff-only origin architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :domain:jvmTest :data:jvmTest :feature:dynamic:jvmTest :app:jvmTest :feature:dynamic:compileAndroidMain :webApp:wasmJsBrowserDevelopmentWebpack
.\gradlew.bat :webApp:wasmJsBrowserDevelopmentRun
```
Use the **actual Gradle-displayed Web port**, not guessed 8080. Check `?cbpartnerVisualPreview=1` vs `?cbpartnerLoginPreview=1`. For Android, use existing Windows Wi-Fi ADB instructions and current device serial/IP; don't assume last session's old Wi-Fi port. Keep tests **minimal, relevant and evidence-based**, not repeated full suites without reason.

Next-chat tone: **Hindi explanation in chat, English-only repository docs/code/comments**. Productive, brief updates, precise file-level audit, concise decisions; don't start a lengthy new architecture brainstorm; don't directly generate new classes/actions/patterns/designs. Follow exactly source/frozen contracts, SRP, no duplication, smallest authorized code diff. Never claim source unverified features are green.

## 10. Preexisting source-truth warning

Doc118 calls L01–L15 a planned one-wave, Doc123 existed only in pre-recovery history, Doc124 rollback restored earlier architecture, Doc125 describes current parity increments. Do not naively treat the latest historical status written in any one doc as actual source. **Chronology + current branch tree + test evidence decide completion**.

This handoff is the authoritative **checkpoint summary**, not a new product contract. For the exact full JSON payloads see `docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-parity-v3.json` and `docs/architecture-proposal/dynamic-runtime-v2/partner-otp-ui-contract-v2.json`, or Doc127 which embeds both unchanged.
