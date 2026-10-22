# 119 — CB-Partner Login L-01–L-15: Verified Status, Pending Dependencies & New-Chat Handoff

**Prepared:** 2026-10-22 (Asia/Kolkata)
**Purpose:** Single source-of-status handoff for the *current Login screen + its Country Picker*, preserving the frozen consolidated implementation plan while avoiding repeated mini-phases.
**Status:** HANDOFF / STATUS SNAPSHOT — NOT a second implementation plan, not a change to a frozen product decision, and NOT production implementation authorization.
**Primary implementation plan:** [Document 118 — L-01–L-15 CONSOLIDATED ONE-WAVE PLAN FROZEN](118-L01-L15-CONSOLIDATED-ONE-WAVE-IMPLEMENTATION-PLAN-FROZEN.md).
**Current frontend GitHub HEAD before this handoff:** `dabe46a22eef2949eafd08112e549975e05eafcc`; ONLY branch `architecture/dynamic-runtime-v2-discussion-freeze`.
**Backend HEAD inspected:** `5ec0f18cd0e7103a5bb2c8b19126d0b447369c07` on `feature/backend-migration`, READ ONLY absent explicit cross-repo approval.
**Last verified automated production baseline:** `c28e4732fea50daf5bf036eaa84d3c055a1594e6` (B5); subsequent commits 116–118 are documentation-only. Re-check remote HEAD at new-chat start.

## 1. Exact goal and scope

Finish the approved **Partner Login screen and its current-screen Country Picker Presentation**, using the actual latest owner-supplied Login JSON and UI screenshot(s). Implement only missing generic capabilities in the existing Kotlin Multiplatform + Compose Multiplatform Dynamic Runtime, and verify Login → Send OTP **request/response/navigation handoff**. This is **NOT** authorization to build a new OTP screen UI, dashboard, KYC, service selection, training or other onboarding screens.

Work productively: **one external implementation wave**, internally grouped into independently testable checkpoints. No repeated separate B6/Phase-4 approvals or arbitrary small plan documents after owner authorizes Document 118. Source/contract conflicts and genuinely missing external data remain explicit stop gates for affected behavior; no guessed implementations.

The owner will attach a **new Login JSON** and **new Login UI image** in the next conversation. Treat them as supplied design/contract candidates; inspect them in full, compare them to the B5 canonical target and Document 118, identify exact differences and possible later owner supersessions. They are NOT automatically proof that unsupported runtime fields or backend endpoints already work. A new explicit owner design decision can update the integrated target without silently modifying older frozen audit snapshots.

## 2. Repositories, branches and environment

**Frontend:** https://github.com/carbroz26/CB-Partner — ONLY `architecture/dynamic-runtime-v2-discussion-freeze`. Do not create/switch to another feature branch, do not delete unrelated branches or do broad Git cleanup. Local Windows path `D:\CarBroz\CB-Partner`. Primary modules: `app` (DI composition/effect consumer); `androidApp`, `webApp` (hosts); `core` (shared utilities/diagnostics/runtime); `domain` (typed models/interfaces); `data` (DTO/mapper/repository/catalog adapter); `network` (shared HTTP/errors); `navigation` (single navigation authority); `feature/splash` (bootstrap); `feature/dynamic` (Pure Store, actions, typed registry/renderers).
**Backend:** https://github.com/carbroz26/carwash-backend — `feature/backend-migration`. Inspect source for routes/contracts; backend modifications are forbidden unless an implementation authorization expressly covers that repository and its specific subtrack. Source of backend facts requires reinspection of actual HEAD.
**User environment:** Windows, Kotlin Multiplatform/Compose; Android physical Samsung Galaxy A35 used previously with Wi-Fi ADB; Web WASM. Use fresh `adb devices -l` because serial/port can change. iOS simulator/device acceptance cannot be executed on Windows.
**Minimal sync/verification:**
```powershell
git pull --ff-only origin architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :domain:jvmTest :data:jvmTest :feature:dynamic:jvmTest :app:jvmTest :feature:dynamic:compileAndroidMain :webApp:wasmJsBrowserDevelopmentWebpack
git diff <PRE-WAVE-SHA> HEAD --check
```
Do not claim Gradle/Android/Web testing ran without actual outputs. Use short productive commands; keep source diffs scoped.

## 3. Mandatory documentation reading and reconciliation

Read **EVERY document**, not only the newest ones, in `docs/architecture-proposal/dynamic-runtime-v2/`, in numeric/chronological order, including README and all frozen/draft/implementation/verification records. **Reading alone is insufficient**:
- Build a decision and supersession timeline; identify when a historical requirement was revised, superseded, implemented, verified, reopened, or deferred.
- An early "PENDING"/"missing" finding may have been solved by a later commit/document (e.g. DividerElement, ImageContent, gesture placement, B4 loading, B5 reference). Never re-implement because an old checklist says pending.
- An old "FROZEN" may later have an explicit owner correction (e.g. B3b **Template** owns background URL-image; old Theme resource-image Appendix is obsolete; B5 `emptyGroup` superseded by `emptySectionId` + sibling StackSection).
- Doc 112 Appendix A and pre-B5 JSON are historical snapshots. The B5 canonical file `partner-login-country-picker-current-target.json` is an L-10-only audit baseline, **not** an executable, current integrated target; do not overwrite it.
- Documents **113** B4, **114–115** B5, **116–117** B6 review and **118** consolidated wave matter specifically. Document 118 supersedes older approval-cycle separation **only as process and consolidated scope**, preserving earlier frozen product/architecture contracts.
- Document **119** is this handoff/status snapshot, not an additional authorizing plan. The canonical complete file-by-file implementation ceiling, test matrix, conditional backend manifest, staged integration and input stop gates are in Document 118. Do not invent another competing master plan.
- Cross-check *every* important documentation assertion with latest actual source, mapping/registration, DI, callbacks, tests and backend schema. A typed DTO does NOT prove registration or UI. A successful build does NOT prove real UI or backend behavior.

When reading `118`, give special attention to the **bounded LazyColumn viewport measurement gate**, immutable B5 original JSON, source-vs-actual-backend OTP body incompatibility and G1–G4 external inputs.

## 4. Confirmed completed work and proof (do not redo)

Baseline implementation for Phase 1–3: Login/Presentation typed contracts, generic Present/Dismiss, validation/focus/binding machinery and layout mapping. Subsequent targeted correction batches:
| ID / Batch | Completed source behavior | What was actually verified |
|---|---|---|
| B1 / L-15 | Shared `sdui/DynamicGestureModifier.kt` and existing gesture logging/callers | Earlier JVM/Android/Web automated baseline; package already correct |
| B2 / L-01 | Concrete DividerElement renderer + registry | Automated PASS; actual Login visual smoke remains independent |
| B3 / L-02/L-04 | ImageContent renderer, typed asset source and trusted URL asset boundary | Automated PASS; genuine asset rendering not proven |
| B3b / L-06 runtime | Template-owned URL background image with bounds/contentScale/alignment | Automated PASS; current B5 target JSON still contains obsolete Theme image |
| B4 / L-07 | Transient scoped Button pending, validated Request, content-to-spinner, disabling, cleanup and Presentation generation safety | Owner supplied JVM/Android/Web BUILD SUCCESSFUL 2026-10-22, 6m39s |
| B5 / L-10 mapping | Typed `properties.emptySectionId`, strict sibling StackSection validation, obsolete embedded `emptyGroup` rejection, structurally revised canonical B5 target | Owner supplied JVM/Android/Web BUILD SUCCESSFUL 2026-10-22, 3m22s; diff --check clean |
| Doc 116 | Source audit, remaining behavior vs Phase 4 separation | Documentation-only |
| Doc 117 | B6 scope/ownership/verification gates frozen | Documentation-only |
| Doc 118 | **Unified L-01–L-15 ONE-WAVE implementation framework/scope FROZEN** | Documentation-only, no source implementation |
No separate B6/Phase-4 implementation was authorized or performed merely by creating Documents 116–118. Existing tests/compilation evidence belongs to B1–B5 only, not the full Login / Country Picker UI acceptance.

## 5. Current missing work / completion register

| L-ID | Current disposition at this handoff |
|---|---|
| L-01 | DONE source: DividerElement; regression/physical Web visual pending |
| L-02 | DONE source: ImageContent; real logo/flag visual pending |
| L-03 | MISSING: concrete product-owned `phone_number` validator DI/provider + vetted region rules/catalog/backend capabilities |
| L-04 | DONE typed URL/resource source runtime; source/path/security and actual image smoke pending |
| L-05 | MISSING: approved bundled Login logo/flag/icons (current packaged registry returns null) or explicitly approved trusted URL migration with reachable real assets |
| L-06 | DONE B3b renderer; current JSON obsolete Theme resource image must be reconciled to Template URL image in a **versioned integrated target** with approved real URL |
| L-07 | DONE automated Button pending; Android/Web/a11y real spinner/error/cancel checks pending |
| L-08 | MISSING: concrete `sdui/section/ListSection.kt`, type registration and fully keyed lazy rendering/viewport |
| L-09 | MISSING: typed catalog source, pure evaluator, scoped `$item`, filtering/search, selectedStyle, selected icon, separator, stable item keys, atomic row selection |
| L-10 | DONE B5 DTO/domain/reference validation; MISSING conditional fallback renderer and suppress ordinary rendering of referenced sibling |
| L-11 | MISSING: trusted `$config` provider/source and safe legal URLs/hosts; preserve forbidden `$response` |
| L-12 | Generic Request/retain/nav code tested; MISSING real Send OTP region/body/backend contract alignment and end-to-end Login → OTP navigation verification |
| L-13 | MISSING approved generic typed/sanitized network-server-rate-limit red alert and inline-vs-alert presentation contract/runtime |
| L-14 | MISSING full Android physical-device, Web and scenario/screenshot acceptance; iOS pending macOS |
| L-15 | DONE: gesture modifier already in shared sdui package; regression only |

**Critical runtime trap:** Current B5 `ListSection` is mapped but **not registered/rendered**. Adding a list renderer alone is NOT sufficient: its sibling fallback is currently rendered in ordinary traversal; it must be excluded there and displayed once only on zero **filtered** rows. Compose LazyColumn needs finite viewport; no hardcoded sheet height or unbounded nested scroll.

**Current target is NOT complete:** It has `$item` row references and `$config` links that runtime does not yet support, Resource asset IDs not registered to genuine drawable bytes, and historical Theme-background placement. Do not claim it is fully usable.

## 6. Document 118 — single-wave workflow and exact architecture boundaries

**One implementation authorization from owner, once:** authorize the full consolidated L-01–L-15 wave according to Document 118. Default: frontend only; backend remains read-only. If owner expressly authorizes backend too, allow only Document 118 bounded backend subtrack on `feature/backend-migration`, not open-ended migration/refactor. No new branches.

**Internal stages (not further approvals):** A: contract/source/input verification; B: catalog/typed resolver/validator/errors foundations; C: ListSection renderer+registration+row/selection+fallback; D: real assets/backend-authored errors/versioned integrated JSON and backend adjustments *only if authorized*; E: combined regression and Android/Web/E2E acceptance. Stop only affected line for absent real data/product/security contract, proceed with independent approved work. Root-cause fixes/test are within the wave, but never invent unapproved product semantics.

**Architecture:** Keep existing six category hierarchy (Template, Component, Section, Group, Element, ElementContent), Clean Architecture, strict SRP, one class-one reason, Pure Store/MVI/UDF, explicit registries, Koin DI; no ViewModel; keep one `DynamicReferenceResolver`, one `DynamicActionExecutor`, one navigation authority, one HTTP stack, one retaining Dynamic session. No duplicate Store or controller/manager/facade, no generic property bag, no `ListElement`, no CountryPicker-specific renderer or hardcoded visual defaults. Backend owns authored UI styling and requests; product/provider modules own business data and phone rules.

**Exact source change manifest:** Use Document **118 §4** FRONTEND per-file table, test paths and KEEP list; **118 §5** BACKEND conditional manifest; **118 §§6–9** internal gate/test/security/no-change rules. Before changing any class: read full file, callers, tests, constructor/DI, registry and retained state semantics. If source reveals the stated responsibility is already implemented, KEEP it. If a new class is necessary, require proven SRP and listed approved path; if a true contract conflict arises, reconcile rather than improvise.

**Mandatory external inputs in Doc 118 §8:**
- G1 approved country catalog, real supported region/phone rules and backend country matrix;
- G2 genuine logo/flag/icon assets, approved Template hero URL, visual design tokens;
- G3 trusted legal/config URLs and authorized domains + structured backend-authored error presentation;
- G4 accepted backend Send OTP endpoint/body/response/country support and backend edit authority where needed.
No invented data, legal URLs, resources, OTP response or fake build/visual acceptance.

**Backend discrepancy already audited in 118:** frontend-approved `POST /api/v1/auth/send_otp` request body `phoneNumber + countryCode + deviceId` vs inspected backend `auth.schema.ts` currently `phoneNumber` only and phone validator India-specific. Verify current routes/schema/live envelope before changing anything; frontend cannot silently omit approved fields and backend code stays read-only without authorization.

## 7. Next conversation exact steps (productive, no repeated small phases)

1. Confirm frontend branch/HEAD and backend branch/HEAD and compare to these snapshots; `git pull --ff-only` only on the correct branch and only if needed.
2. Read **all** files in `docs/architecture-proposal/dynamic-runtime-v2/` chronologically, not skim. Create an internal supersession map for each important decision. Check Document 118/this 119 status first to orient, but do not skip earlier files or override later decisions with old material.
3. Read actual source, modules, packages and relevant classes **line by line**, including DTO/Domain/Mapper/Section/Group/Element/Content/Action/Store/Navigation/DI/Assets/Backend, plus callers/tests. Correlate all 15 rows with implementation and regression tests. Don't treat older pending flags as current truth.
4. Examine owner-provided **new Login JSON and UI screenshot** in detail. Match each element/size/color/spacing/state/layout and action/binding/reference, compare against B5 canonical + 118 owner decisions. Produce a **compact exact-diff/gap register**, distinguishing true new product decisions from already approved corrections; never invent backend values.
5. Reconcile only *real* contract blockers (G1–G4), viewport sizing and backend request mismatch; avoid a new sprawling planning cycle or separate tiny approvals. Keep one consolidated plan (118) with a scoped amendment only if justified by latest owner-approved JSON or a proven source/contract conflict.
6. Once the owner gives the **one authorization** to implement the consolidated wave, execute all authorized unblocked internal stages with focused commits and tests; do NOT ask again to approve normal small changes. On unresolved external inputs, mark affected behavior **BLOCKED** instead of building a fake feature.
7. Run actual JVM/Android compile/Web checks and true physical-device/Web scenarios, audit changed-file scope/whitespace and retain source-truth proof. Report separate **implemented, automated pass, real visual pass, backend E2E pass, blocked** states.
8. Do not broaden to OTP screen UI, KYC, dashboard or other workflows beyond login contract/next-screen handoff.

**Language:** Chat explanations and discussions in **Hindi** with English technical terms; code, filenames, comments, KDoc, test names and all repository documents in **English**. Respect user's preference for concise productive implementation steps, deep discussion only for material decisions.

**Next gate:** Plan Document 118 is already frozen as consolidated framework. Owner's **one separate implementation authorization** has NOT been given in this handoff request. The incoming Login JSON/UI image and any genuinely missing real-world inputs must first be reconciled to the frozen plan before modifications. This handoff is not an authorization or a promise of an asynchronous implementation.
