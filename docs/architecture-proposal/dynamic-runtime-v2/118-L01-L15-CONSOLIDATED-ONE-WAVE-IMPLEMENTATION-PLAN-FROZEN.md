# 118 — L-01–L-15 Consolidated Completion: ONE-WAVE Implementation Plan

**Decision date:** 2026-10-22
**Owner direction:** Replace many small implementation authorizations with **one complete, source-reconciled L-01–L-15 delivery wave**, internally batched, tested and reviewed.
**Status:** **CONSOLIDATED IMPLEMENTATION FRAMEWORK / SCOPE FROZEN**. **Production implementation NOT STARTED; ONE SEPARATE OWNER AUTHORIZATION still required.** An input or backend contract explicitly marked as unresolved below is **not falsely asserted to be product-frozen**.
**Frontend:** `carbroz26/CB-Partner` / **only** `architecture/dynamic-runtime-v2-discussion-freeze`; source HEAD at preparation `624828a1bf099ee560fcce4d51d07954d7adbdff`.
**Backend read-only inspection:** `carbroz26/carwash-backend` / `feature/backend-migration`, HEAD `5ec0f18cd0e7103a5bb2c8b19126d0b447369c07`. **No backend edits authorized by this document.**
**Delivery architecture:** Kotlin Multiplatform + Compose Multiplatform; typed Screen → Template → Component → Section → Group → Element → ElementContent; Clean Architecture, Pure Store/MVI/UDF, Koin explicit registration; backend-authored UI tokens; no ViewModel; no second network/navigation/action/state source of truth.
**Authorities:** Document 112 §§7–23 as corrected by 113 (B4) and 114/115 (B5), source-based B6 Documents 116/117, canonical `partner-login-country-picker-current-target.json`, 21-Oct approved design and evidence from actual frontend/backend source. Historical Appendix A and older L01–L15 checklist are evidence, not superseding contracts.

## 0. Explicit supersession of PROCESS, not product decisions

The owner replaces Document 117's instruction to obtain separate approvals for each B6 item and a separate Phase-4 planning cycle. **One consolidated plan freeze + one explicit implementation authorization** shall cover the entire approved wave. Internal batches are implementation checkpoints, **not** additional planning/approval gates. All already-frozen semantics (B3b Template image; B4 origin and pending; B5 same-Component emptySectionId; Present/Dismiss; navigation/effect atomics; inert Sequence; safe logging) remain unchanged. Documents 112–117 remain historical and detailed contract authorities when not inconsistent with this execution/process supersession.

A real frozen-contract/source disagreement, missing licensed asset, unverified product data, backend response mismatch, security concern or change requiring a *new* product decision is a **stop condition for the affected path**, not permission to improvise. Independent authorized work may continue. No automatic expansion into other screens or functionality. **Do not declare all L-01–L-15 production-complete if external inputs are still blocked**; report implemented, tested and blocked separately.

## 1. Source-truth L-01–L-15 disposition

| ID | Existing truth / exact completion target | Workstream |
|---|---|---|
| L-01 | Typed DividerElement renderer and registration already exist; preserve behavior. | Regression + Android/Web visual |
| L-02 | ImageContent mapping, renderer and registration exist. | Regression + real asset visual |
| L-03 | DynamicValidator registry exists but no concrete `phone_number` provider registered by `App.kt`. | Product-owned validator + region-policy data; fail closed if unknown |
| L-04 | Typed `source: resource/url` and trusted URL loader exist. | Security/format regression |
| L-05 | `DynamicPackagedAssets.kt` currently maps **no** Login asset IDs to actual Compose resources. | Integrate genuine approved resource files/keys OR approved trusted-URL migration; no dummy glyphs/assets |
| L-06 | B3b Template-owned URL image works; B5 target still has obsolete Theme resource image. | Separate **approved in-wave contract revision** of canonical target and server composer; preserve Theme/Template ownership |
| L-07 | B4 pending Request spinner/disabling/cleanup implemented. | Positive, error, cancellation and a11y/visual regression |
| L-08 | ListSection DTO/domain/mapper exist; **no concrete UI renderer or registration**. | Full generic ListSection renderer + registration |
| L-09 | List source catalog, filter, `$item` row scope, item keys, atomic selection, selected visuals and separators incomplete. | Full list behavior, generic catalog interface with product data provider |
| L-10 | B5 typed `emptySectionId` sibling StackSection reference is mapped/validated. | Reserve referenced sibling from normal rendering; conditionally render **only** when filtered visible count = 0 |
| L-11 | `$config` legal URI ref appears twice in canonical JSON; resolver currently allows only binding/context/state/literal. | Trusted typed configuration snapshot + allowlisted URI handling |
| L-12 | Generic Request/retain/navigation-effect atomic handoff tested; live Send OTP contract differs from current backend. | Frontend/backend agreement + end-to-end valid/invalid/cancel verification |
| L-13 | Network categories exist; Store/Container display generic safe text, not backend-authored inline/red alert. | Safe structured error classification + authored presentation contract; never render raw exception message |
| L-14 | JVM/Android compile/Webpack B1–B5 PASS; full Login/Picker states not witnessed. | Physical-device and Web acceptance, iOS pending macOS |
| L-15 | Shared gesture modifier already lives in `sdui/DynamicGestureModifier.kt`. | KEEP; regression only, no package move |

No artificial recreation of B1–B5 capabilities. The *same* implementation wave includes historically named B6 and Phase 4.

## 2. Cross-repository contract and mandatory evidence

**Consolidated target source of truth:** Existing `docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-current-target.json` is the **B5 L-10-only migration**. It preserves unrelated obsolete values deliberately. Do not rewrite it silently; create a **versioned integration revision** from this exact source using reviewed field-level changes, with a machine-readable change/unchanged-fields test.

**Verified backend incompatibility at inspected backend HEAD (NOT theoretical):**
- Canonical Continue `POST /api/v1/auth/send_otp` body currently has `phoneNumber`, `countryCode`, `deviceId` references. Backend `src/modules/identity/validators/auth.schema.ts` accepts **only** `phoneNumber` (`additionalProperties:false`). Sending canonical body as-is can fail before Handler; never claim integration PASS.
- Backend `PhoneNumber.ts` validates **Indian 10-digit mobile** only (and strips +91/91/0). Canonical UI has generic country selection and allows up to 15 digits; do not claim global validation/backend support.
- Backend `RequestOtpHandler.ts` returns challengeId/phoneNumber/destination through `ResponseHelper.success`. Frontend `DynamicResponseMapper` reads `data.destination` and `retain` paths `data.challengeId` / `data.phoneNumber`, so *shape looks compatible*, but backend endpoint/routing and exact template IDs still require live contract tests.
- Backend `PartnerLoginScreen.ts` is an older separate composition (fixed +91, no ListSection) with request body `phoneNumber` + `deviceId`; backend SDUI registry currently has no ListSection definition. Backend contract document `PARTNER-AUTH-SDUI-CONTRACT.md` uses routes/template IDs which can differ from current frontend target and auth routes. **Actual registered route + current live envelope win; do not combine mutually inconsistent historical endpoints.**
- Backend `IConfigProvider` is backend-side async configuration, **not proof of a frontend `$config` delivery path**. Current frontend `App.kt` exposes no such legal configuration to the renderer.

**Single-wave integration choice:** Backend changes are a *conditional subtrack* of this plan, required if the approved new Country Picker/OTP flow is to be delivered end-to-end using this backend. Future one-time authorization must explicitly cover **both** `CB-Partner` and `carwash-backend` branches if backend edits are desired; otherwise backend stays read-only and L-12/full E2E is explicitly BLOCKED, not faked by frontend transformations. Do not weaken the frontend's approved body or extra fields to placate an older schema without that product contract decision.

## 3. Frozen architecture and immutable behavior contracts

### 3.1 Sources and ownership
1. **Catalog data:** one **typed read-only Domain catalog port** keyed by catalog ID; data module supplies immutable snapshot of known records; data source does not own UI filtering/selection and is not a new network stack. `ListSection.items` uses authored inline typed records; `ListSection.source = Catalog(key)` resolves the registered catalog; exactly one source remains required (existing mapper). Missing catalog/key/item fields produce controlled failure, no empty-list camouflage.
2. **List evaluation:** one feature/dynamic **pure reusable evaluator** receives mapped ListSection, current binding snapshot, catalog snapshot and returns validated rows with stable item IDs, filtered in declared original order, plus selection comparison; same evaluation logic must be used by renderer and Store (no duplicated logic). Filter uses declared fields, contains matching, optional caseInsensitive; blank search restores source order; missing values fail closed as defined in tests. No implicit sort.
3. **Rendering:** concrete `ListSection` registered under existing Section map, not a ListElement/seventh category. Use Compose lazy list with stable authored `itemKey` identity and keyed row scopes; apply backend-authored dimensions, spacing, selected background, divider thickness/color/spacing; no hardcoded row styling or loading item count. **Critical layout gate:** the current bottom-sheet Dialog wraps a Column without explicit bounded list height, while ListSection declares `weight: 1`. Audit the exact Compose measurement chain before adding `LazyColumn`; guarantee finite list viewport constraints from existing backend-authored Presentation/Section sizing, and do not nest an unbounded vertical LazyColumn in a vertical scroll. If no valid bounded-height contract exists, stop the affected layout implementation for a typed sizing decision; never hardcode a device-specific sheet height.
4. **Fallback:** `ComponentMapper` B5 ensures target is exactly one same-Component sibling **StackSection**. `StackComponent` excludes all referenced fallback Sections from ordinary sibling traversal. Scope a typed sibling-Section lookup to that Component only. ListSection renderer receives only its validated sibling fallback from the current Component and renders it exactly once when *filtered visible rows are empty*. No `emptyGroup`, `emptyState`, duplicate `visibleWhen`, cross-component lookup or ordinary-sibling duplicate.
5. **Row `$item`:** read-only typed row context (an immutable map of item fields), valid **only while rendering/action-dispatching that row**; preserve nested typed values, never mutate screen bindings to model the row. Existing resolver remains the **one** reference implementation. `$response` continues forbidden; `$item` outside row context must reject. Separate row `$item` from trusted session `$config`.
6. **Selection:** row tap dispatches an intent containing navigation-entry scope, Presentation ID + open generation if applicable, `ListSection.id`, and stable item key. Store re-finds list in accepted scope and re-resolves item against *current* catalog/bindings, rejects missing/duplicate/stale/unfiltered selections. Apply **all** declared `selection.result` mappings to the existing screen bindings in **one** reducer/entry update (e.g., countryId/id, countryCode/dialCode, countryFlagAsset/flagAsset), then optionally dismiss only the matching Presentation. No duplicate selected-state owner; compare selectedBinding with item[itemKey] to derive selectedStyle/visibleWhenSelected. No partial writes on invalid fields. Do not convert selection into generic `state` Action, change existing Request/Present/Dismiss contracts or invent a second Store.
7. **Repeated-row actions:** rendering callbacks capture row instance identity without leaking another row's item on recomposition; `$item` resolves for authored row actions with correct scope. B4 loading is **not implicitly extended** to repeated row Button instances: implement explicit row-origin identity and tests before enabling row Request spinners; if unsupported at this freeze, fail that combination at mapping, not silently share one Button ID.
8. **Selected visuals:** reuse `StackGroup.selectedStyle.background` and `IconElement.visibleWhen.selected` + `reserveSpaceWhenHidden`. Only selected row gets backend-authored style; hidden icons retain backend-declared spacing where requested; no native glyph fallback.
9. **Scope/lifecycle:** navigation entries isolate screen data; Presentation binds overlay local-first and uses open generation, dismiss clears transient list search, stale row taps and async work cannot update reopened Presentation; cancellation stops pending work. Preserve existing B4 single-flight + navigation commit invariants.

### 3.2 Phone validator
- Keep `DynamicValidator` interface and `DynamicValidatorRegistry`. The new **product-owned** `phone_number` implementation is registered in app composition (not feature/dynamic's generic code). `required` → `pattern` → `named validator` ordering and already-approved inline Group error style remain.
- Use an approved, tested region-specific metadata/policy set shared with the actual backend acceptance rules. Do not invent phone prefixes, assume all countries accept Indian rules or use a permissive length-only fallback. Invalid, blank, unknown region and missing provider inputs fail safely with backend-authored validation message and **no Request**.
- **Input gate:** an approved region rule/catalog dataset + backend-supported region matrix must exist. If backend remains India-only, full multi-country OTP acceptance must stay BLOCKED or the UI must carry an **explicitly approved** limited-region capability; never silently disable/remap countries.

### 3.3 `$config` and legal URLs
- Add one typed **trusted app-config snapshot** owned at app/session/bootstrap boundary; never take legal URLs from arbitrary row data or untrusted server error text. Pass it through existing Store/RenderContext/Action resolution, without an additional global object or second resolver.
- Approved references are `{"$config":"legal.termsUrl"}` and `{"$config":"legal.privacyUrl"}` from the target. All missing/invalid config paths reject cleanly without opening a URL; do not supply guessed legal sites. Only explicitly approved HTTP(S) trusted legal destinations may open. No arbitrary URI scheme, javascript, file, intent, or host redirection; preserve the single existing `ExternalUri` effect/handler.
- **Input gate:** actual owner, values, allowed domains, bootstrap exposure and refresh/snapshot lifecycle must be verified. Until then, links cannot be marked integrated; keep fail-closed.

### 3.4 Typed error feedback
- Network categories already exist: validation, rate-limit, server, connection, timeout, bad request, other. Preserve typed exception origin and sanitize **before** Store/UI; no direct exception message, user data, token, OTP, API URL or raw server response in visuals/logs.
- Distinguish **inline input validation** (existing Group.validationPresentation, normal/focus/error priority) from **request/network/rate-limit/server** feedback (approved red alert/card with backend-authored styling). Do not conflate network 400/429 with locally invalid input, invent fixed copy/timeout or hardcode red tokens in Compose.
- A **typed, backend-authored error-presentation contract** must carry message/title/icon style, background, border, padding, radius and layout as already approved by product UI. Existing `DynamicContainer` safe generic message is only legacy fallback for transport/system errors; it is not evidence of the completed red alert design.
- **Input gate:** reconcile the exact authored location/response schema and allowlisted safe error code/message vocabulary. No generic dynamic `Any` property bag, special login renderer, duplicate network-error classification layer or new action manager. Unknown error category displays a safe generic failure, with pending cleared and no stale state mutation.

### 3.5 Genuine assets, template image and server JSON
- `DynamicPackagedAssets` presently has **zero approved Login keys**; integration requires actual licensed, supplied drawable resources and explicit `Res.drawable` lookup **or** an owner-approved migration of each key to origin-trusted URL with verified reachable bytes. No custom icon-name->native-glyph dictionary. Existing temporary fallback PNGs must not be represented as approved design assets.
- **B3b** image remains `template.properties.backgroundImage` with trusted `url` source, optional fallbackUrl, backend-owned contentScale/alignment and proper bounds; `theme.properties.backgroundImage` is obsolete. Theme gradient remains independent. Use a **separate versioned integration target JSON** (not overwrite B5 canonical without diff) and backend emitter alignment only with a verified real trusted hero URL and author-approved design values.
- Preserve the original 21-Oct screen and B5 reference structure; any integration JSON transformation must assert permitted paths and byte-equivalent normalized content for untouched objects.

## 4. Exact proposed FRONTEND file-by-file implementation manifest

All paths relative to `CB-Partner`. **M = MODIFY; N = NEW; T = TEST; K = KEEP.** The manifest is the **ceiling of change scope**, not an order to modify every file. Avoid an abstraction if source review shows existing one already owns the behavior.

| Operation | File | Single intended responsibility |
|---|---|---|
| N | `domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/section/DynamicListCatalog.kt` | Small typed catalog port, read-only item snapshot; no UI logic or network. |
| N | `data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/catalog/PartnerCountryCatalog.kt` | Product data adapter for approved country catalog (content blocked until authoritative dataset supplied); no sorting/selection. |
| M | `data/src/commonMain/kotlin/com/carbroz/cbpartner/data/di/DynamicDataModule.kt` | Explicitly register one catalog port. |
| N | `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicListItems.kt` | Pure source/typed-item resolution, stable-key validation and filter function, shared by Store + renderer. |
| N | `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/section/ListSection.kt` | Concrete lazy Section renderer: rows, separators, selected row feedback and empty fallback; no catalog storage or business rules. |
| M | `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/registry/DynamicRendererRegistration.kt` | Register exactly one ListSection model/renderer pair. |
| M | `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/component/StackComponent.kt` | Skip referenced fallback siblings; establish current Component's sibling reference scope. |
| M | `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicRenderContext.kt` | Typed child context for current Component fallback and list row; immutable `item`, selected flag, callback; no arbitrary properties. |
| M | `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicReferenceResolver.kt` | Extend **same** typed resolver with row-only `$item` and trusted-session-only `$config`, fail closed outside proper scope. |
| M | `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/group/StackGroup.kt` | Apply selectedStyle only within row context; retain focus/validation border ownership. |
| M | `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/element/DecorationElements.kt` | Honor selected conditional icon and reserveSpaceWhenHidden, without touching base asset loader. |
| M | `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/DynamicContainer.kt` | Forward exact List selection origin, app-config snapshot and safe typed feedback to existing Store/renderer. |
| M | `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/presentation/DynamicPresentationHost.kt` | Forward scoped List callback/config and provide bounded list viewport only from approved backend-owned sizing; preserve modal dismiss semantics, no hardcoded screen fraction. |
| M | `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/renderer/DynamicRenderer.kt` | Propagate typed list/config context only. |
| M | `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/store/DynamicStore.kt` | Validate list-item intent at latest state/scope and atomically apply declared result; preserve B4 pending, cancellation and effect batching. |
| M | `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/store/DynamicScreenState.kt` | Add only necessary transient typed error/selection state; no persistent duplicate catalog or selected-state cache. |
| M | `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/action/ExternalUriActionHandler.kt` | Resolve trusted `$config` through existing Action path + allowed URI policy; no new link handler. |
| N | `app/src/commonMain/kotlin/com/carbroz/cbpartner/app/validation/PartnerPhoneNumberValidator.kt` | App/product-owned named phone validation after vetted regional policy; not generic Dynamic business logic. |
| N | `app/src/commonMain/kotlin/com/carbroz/cbpartner/app/DynamicAppConfig.kt` | Typed read-only approved legal URI configuration snapshot; no duplicate resolver. |
| M | `app/src/commonMain/kotlin/com/carbroz/cbpartner/app/App.kt` | Register validator/config and pass real providers through current Koin composition. |
| M | `app/src/commonMain/kotlin/com/carbroz/cbpartner/app/ApplicationContent.kt` | Pass trusted config/list dependencies through existing session and effect consumer; preserve single navigation authority. |
| M (conditional genuine input only) | `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/asset/DynamicPackagedAssets.kt` | Map only actual packaged resource keys when verified supplied files are present. |
| N (conditional genuine input only) | `feature/dynamic/src/commonMain/composeResources/drawable/<approved-asset-filenames>` | Real logo, flags, icons. Filenames/bytes **cannot be fabricated** before input; scope is asset data only. |
| M (after error-schema input gate) | `data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/model/screen/DynamicScreenDto.kt` | Add only the exact approved backend-authored error-presentation field(s). |
| M (after error-schema input gate) | `data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/mapper/DynamicResponseMapper.kt` | Map typed authored error presentation; do not infer colors from HTTP status. |
| M (after error-schema input gate) | `domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/DynamicScreen.kt` | Own immutable typed authored feedback configuration if required. |
| M (after error-schema input gate) | `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/DynamicContainer.kt` | Place generic feedback using specified authored layout; avoid second ViewModel/Store. |
| K | `docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-current-target.json` | **KEEP unchanged** as B5 audit baseline; create versioned revision below instead. |
| N (approved integration migration) | `docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-integrated-target.json` | Full approved L01–L15 target compiled from original with exact tracked allowed differences. |
| M (documentation) | `docs/architecture-proposal/dynamic-runtime-v2/112-LOGIN-COUNTRY-PICKER-PHASE1-3-CONSOLIDATED-FREEZE-AND-AUDIT-HANDOFF.md` | Add immutable integration revision link/status only; preserve historical Appendix A. |

**Test owners / permitted new test paths (create narrowly):**
- `feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicListItemsTest.kt` (N): catalog and inline source, case sensitivity, query, order, stable key, duplicates/invalids, no sort.
- `feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicReferenceResolverTest.kt` (M): typed `$item`, no row `$item` rejection, immutable nested path, `$config` allowlist/missing, forbid `$response`.
- `feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/store/DynamicStoreTest.kt` (M): exact entry+Presentation generation, cancel/stale selection, immutable result atomicity, B4 regression, error lifecycle.
- `feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/registry/DynamicRegistryTest.kt` (M): ListSection registered exactly once; duplicate registration fails.
- `feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicFormValidatorTest.kt` (M): required/pattern/named provider/region fail closed.
- `feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/asset/DynamicAssetImageTest.kt` (M): trusted origin, resource validity, missing keys.
- `feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/action/RequestActionHandlerTest.kt` (M): exact Send OTP body/retain/destination contract tests, current B4/request semantics.
- `data/src/commonTest/kotlin/com/carbroz/cbpartner/data/dynamic/mapper/DynamicResponseMapperTest.kt` (M): typed source and feedback mappings, no silent unknown keys, B5 reference regression.
- `app/src/commonTest/kotlin/com/carbroz/cbpartner/app/ConsolidatedLoginIntegrationTest.kt` (N): Koin validator/catalog/config injection + backend contract fixture through existing app composition; no real SMS.
- `feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/section/ListSectionTest.kt` (N): only if host-compatible Compose test harness exists; otherwise use pure evaluator/registry tests and physical/Web visual matrix, **never pretend JVM screenshot assertions ran**.

**Read/KEEP by default:** network `NetworkException.kt` / `RequestHandler.kt`; Request/Navigate/Present/Dismiss/Sequence handlers; NavigationState/Entry; DynamicSession; DynamicRegistry; FormTemplate/TemplateShell; current B1 GestureModifier, B2 DividerElement, B3 ImageContent/Asset Image, B4 Button and B5 reference validation; existing DTO hierarchy except explicitly listed typed feedback adaptation. A new file requires proven single responsibility and inclusion in this manifest or owner-approved reconciliation.

## 5. Exact proposed BACKEND integration subtrack (conditional on one cross-repo authorization)

Existing backend stays **READ ONLY now**. The list below is a **proposed bounded backend manifest** grounded in inspected source. Backend change is necessary for complete live delivery but **not automatically authorized** by a frontend-only phrase.

| Operation | Backend file | Reason; MUST preserve domain boundary |
|---|---|---|
| M | `src/modules/identity/validators/auth.schema.ts` | Align Send OTP request's declared field set with actual approved contract; never silently drop `countryCode`/`deviceId`. |
| M | `src/modules/identity/dto/auth.dto.ts` | Typed request fields consistent with schema. |
| M | `src/modules/identity/controller/auth.controller.ts` | Pass vetted fields to Identity handler; no SDUI semantics inside controller. |
| M | `src/modules/identity/application/handlers/RequestOtpHandler.ts` | Reconcile validated phone/region/device with current SMS + Redis challenge lifecycle; preserve safe rollback/attempt rules. |
| M | `src/modules/identity/domain/value-objects/PhoneNumber.ts` | Replace India-only assumption only when real regional support approved; preserve canonical E.164 conversion and strict fail-closed validation. |
| M | `src/modules/sdui/engine/src/screens/partner/PartnerLoginScreen.ts` | Compose exact approved typed Login/Picker JSON; no hand-built non-generic UI in frontend. |
| M | `src/modules/sdui/engine/src/nodes/definitions.ts` | Register backend authoring definitions for any newly supported concrete Section/Element types. |
| M | `src/modules/sdui/engine/src/core/Action.ts` | Add only approved `$item`/`$config` reference authoring, preserving forbidden frontend `$response` contract. |
| M | `src/modules/sdui/engine/src/core/SduiBuilder.ts` | Typed creation methods needed for ListSection, Divider/Icon, Content without raw ad hoc JSON; retain builder hierarchy and explicit registry. |
| M | `src/modules/sdui/engine/src/core/SduiValidator.ts` | Validate same approved action/reference/structural contracts and B5 sibling scope at server authoring boundary. |
| N (if not already exists) | `src/modules/sdui/engine/src/nodes/section/ListSection.ts` | Typed authoring Section definition; **only** if existing registry has no equivalent. |
| N (if not already exists) | `src/modules/sdui/engine/src/nodes/element/Divider.ts` and `Icon.ts` | Typed authoring concrete Element definitions; only if source audit confirms missing. |
| M | `src/modules/partner/controller/partnerSdui.controller.ts` | Return aligned production composer output as current API envelope. |
| M (configuration after approved values) | `src/modules/configuration/application/use-cases/GetPartnerBootstrapUseCase.ts` | Expose only vetted legal-link config through current bootstrap if the authoritative contract selects bootstrap; no extra fetch/manager. |
| T | `src/modules/sdui/engine/tests/PartnerLoginScreen.spec.ts` | Exact frozen component/section/action/references/emptySectionId, no legacy `emptyGroup`. |
| T | `src/modules/sdui/engine/tests/NodeDefinitions.spec.ts` | Explicit category registration/serialization. |
| T | `src/modules/sdui/engine/tests/SduiValidator.spec.ts` | Invalid ref, missing sibling, wrong type and unknown action tests. |
| T (existing identity tests to locate) | `src/modules/identity/` existing request/controller tests | Allowed additional targeted tests covering schema/body/region/response/rollback; determine exact test path from source tree prior to writing. |

**Backend implementation gate:** Before writing any backend code, compare actual endpoint routing, SDUI engine DSL capabilities and tests. If this manifest proves technically incompatible with frozen backend governance or requires more classes, **stop affected backend subtrack and reconcile within this same consolidated wave**; no speculative direct `JsonObject` substitute, no silent deletion of user-approved body fields. Avoid Prisma migrations unless genuinely required and separately evidenced.

## 6. INTERNAL checkpoints, one EXTERNAL authorization

| Internal stage | Focus | Gate before next internal stage |
|---|---|---|
| A — Input/contract lock | Recheck HEAD, approved catalog/regions/assets/legal URLs; backend route/body/response; pin exact JSON field diff; classify blocked inputs | No undocumented product assumption; mapping fixture demonstrates both success and rejection |
| B — Core typing/resolution | Catalog port and pure evaluator, `$item`/`$config` provenance, phone validator DI, typed safe feedback mapping | Focused domain/data/feature JVM tests; Koin and renderer-registration tests |
| C — List rendering | Section renderer registration, component fallback-sibling suppression, row selection, selected visuals, lazy/separator | Empty/nonempty/filter selection tests, repeated row identity, stale Presentation and accessibility review |
| D — Login/error/assets/backend | Authored inline/red alert, real assets/templates, versioned JSON, backend route/body/OTP response where jointly authorized | Contract/golden JSON diff, backend tests where allowed; B4/B5 regression |
| E — Integration/acceptance | Clean combined automated suite, Android physical device, Web, Login → Picker → OTP, fail/cancel/429, return/reopen | Owner reviews actual logs/screens; only passing, real scenarios marked DONE |

After ONE owner authorization, work continuously through A–E. Multiple **focused commits** permitted on **existing branches only**; do not create temporary branches or repeatedly ask for routine approval. Failure is fixed in its owning stage with root-cause tests. A genuine external data/security/new-contract conflict pauses only affected files; log blockers, not guessed values.

## 7. Mandatory test and acceptance matrix

| Area | Positive | Negative, boundary and lifecycle |
|---|---|---|
| DTO/Domain/registry | Every backend-authored Template/Component/Section/Group/Element/Content mapped and registered | Unknown/duplicate registrations, invalid sibling and unsupported category fail clearly |
| Catalog/inline list | Declared order, string fields, item snapshot, stable itemKey and empty source | Missing key/catalog, malformed item, duplicate/non-string IDs, no silent sort |
| Filter | Search by name/dialCode, case-insensitive contains if true, blank resets, independent Screen/Presentation binding | No matches triggers only fallback, rapid query changes, missing filter field, stale search callbacks |
| Row render | Correct flag/text/dial code, selectedStyle and selected icon; between-item divider; lazy keyed rows | No divider after last item, no double fallback, no leaked row `$item` outside scope; unknown asset no fake image; **bounded/unbounded sheet viewport and nested-scroll Compose measure test** |
| Selection | Tap resolves latest item then atomically writes countryId/countryCode/countryFlagAsset; optional dismiss | Duplicate taps, absent mapping field, inactive/stale modal open-generation, no partial binding mutation, preserve screen on reopen |
| Validation | Blank/invalid/valid phone against authoritative region rules, required → pattern → named validation | Unknown/unavailable country rule blocks Request; no false global acceptance, correctly retained inline message |
| Config/legal | Resolved approved legal URLs open through one effect consumer | Missing config, invalid type, forbidden schemes, unapproved hosts, `$response` forbidden |
| Error UI | Safe network/rate-limit/server variants use backend-authorized tokens; pending spinner clears | No raw exception body, 429 retry/cooldown assumptions, double-tap, cancellation and failed handoff |
| Request/OTP | Exact actual POST body/schema, 200 data destination, declared challenge retention, one navigation effect | Invalid body rejected before send; missing destination/retain path fails closed; 400/429/timeout/500 no stale effect |
| Visual | Android A35 and Web: branding, row, focus, keyboard, picker, selected row, empty search, icon/asset/hero and accessibility | Rotation/resize/recomposition, modal outside/back, stale callback/reopen, loading/error, no duplicate input or fallback |
| Platforms | JVM tests + Android compilation + Web webpack | iOS simulator tests remain pending macOS; no unsupported parity claim |

**Automated owner commands after each relevant stage and final combined gate:**

```powershell
git pull --ff-only origin architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :domain:jvmTest :data:jvmTest :feature:dynamic:jvmTest :app:jvmTest :feature:dynamic:compileAndroidMain :webApp:wasmJsBrowserDevelopmentWebpack
git diff <pre-wave-SHA> HEAD --check
```

Backend, **only when explicitly co-authorized**: run its own existing package-manager/typecheck/unit tests and contract fixtures; do not claim backend PASS without execution proof. Android physical device uses current `adb devices -l` and approved ADB-reverse/local API base URL workflow; don't reuse stale Wi-Fi serial. No network body/OTP/token logging. Recheck actual changed file list against this manifest and source whitespace; any unauthorized difference fails review.

## 8. Four external input/contract gates — honest single-wave completion rule

These are **required input evidence**, not invitations to more miniature architecture phases:

| Gate | Required artifact / owner fact | Without it |
|---|---|---|
| G1 — Real catalog + phone coverage | Approved country records (ID, name, dialCode, flag asset/URL), authoritative phone region validation matrix, vetted backend supported countries | Implement evaluator with synthetic fixtures, but **block production country picker/Send OTP acceptance** |
| G2 — Genuine UI assets + hero | Actual authorized logo/flags/icons and reachable trusted hero URL + approved Theme gradient/design | Renderer may be coded/tested; **block target visual acceptance** |
| G3 — Trusted legal config + safe errors | Real legal URLs + host policy + typed app-config delivery; backend-authored red alert/inline error schema, allowlisted safe error fields | Keep links/error-specific rendering **fail closed / unverified**; do not invent strings |
| G4 — Backend Send OTP compatible contract | Current route, accepted JSON body, country support, 200 envelope destination template/endpoint, OTP auth lifecycle and server error response contract; explicit scope authorization for backend edits if needed | **Block end-to-end Login → OTP acceptance**; don't silently strip fields |

The scope and stop/go decisions are frozen, but actual G1–G4 *values, files and live backend support* are **not supplied/verified and cannot truthfully be declared frozen**. Unblocked code may be built and tested under the ONE future wave authorization; before marking complete, each gate must be evidenced. A real change in product meaning needs an explicit owner reconciliation, but no extra approvals for ordinary implementation tasks.

## 9. Non-change boundaries / fail-fast rules

- No new `ListElement`, second list Component, new Template, seventh UI category or product-specific renderer; no reflection or service-locator usage in renderers.
- No generic property bag, speculative Manager/Facade, duplicate Repository/HttpClient/NavHost/ActionExecutor, extra ViewModel or state owner.
- Do not replace existing `DynamicReferenceResolver` or resurrect `$response` / executable Sequence.
- Do not persist row context, catalog snapshot, selected-state mirror, transient errors or B4 loading into navigation state without explicit evidence; scope mutable state to existing Store.
- Do not change B1–B5 behavior or original canonical B5 JSON without a versioned, documented approved integration diff.
- Do not normalize all error kinds into inline validation or hardcode red alert colors in frontend.
- No backend modification on `feature/backend-migration` without explicit future authorization naming that repository; no schema/security regression or invented international telephone rules.
- No changes to unrelated modules, Git branches, Redis/Prisma/network logging, OTP business logic other than explicitly necessary backend-integration manifest.
- No assertion of product-ready or platform parity based only on successful JSON decode or Gradle compilation.

## 10. ONE future authorization and completion definition

**Plan authority:** This document supersedes Documents 116–117 **only for implementation-wave batching/approval process and combined scope**. Earlier frozen product semantics and backend READ-ONLY policy remain unless the future authorization explicitly grants the listed cross-repo subtrack.

**Required next user authorization once:** “Authorize implementation of the consolidated L-01–L-15 wave according to Document 118 [frontend only / frontend plus specified backend subtrack].” If the user simply says “authorize consolidated L-01–L-15 implementation,” the default is **frontend only**, backend remains read-only. The approval covers all internal stages and scoped tests, with no repeated permissions for normal changes.

**Final state reporting:** Separate (a) contract/model implemented, (b) automated tests passed, (c) Android/Web UI observed, (d) backend E2E passed, (e) genuine data/assets/config available, and (f) blocked/unverified items. The wave is **COMPLETE** only when all required real-world gates are satisfied, not when an artificial preview or mapping tests pass.

**Current state (this freeze): PLAN ONLY; zero implementation source files changed; no build/test commands run in this documentation-only turn.**
