# Dynamic UI Implementation Status

> **Current V2 addendum (2026-10-17):** See the final section, "V2 G1-B/S2 Android evidence and next gate", for the active `architecture/dynamic-runtime-v2-discussion-freeze` branch. The historical status sections below describe earlier workstreams and must not be interpreted as the current V2 branch status.

**Status:** CONTENT-OWNED SPACING REVIEW OPEN; SIX-REGISTRY TYPED DISPATCH PLAN FROZEN — SOURCE IMPLEMENTATION NOT STARTED
**Date:** 2026-10-12
**Authority:** `docs/dynamic/DYNAMIC-ARCHITECTURE.md`
**Plan:** `docs/dynamic/DYNAMIC-IMPLEMENTATION-PLAN.md`

## 1. Current truth

The owner-approved revision now applies typed, concrete-model-class registration and lookup to all six independent renderer registries: Template, Component, Section, Group, Element, and Content. All six renderer contracts are generic and typed; each registry remains category-specific, with type erasure/casts confined to a narrow checked adapter where necessary. The same plan retains ordered action-effect aggregation, reliable Store effect delivery, and distinct modal presentation semantics. The authoritative contract is `DYNAMIC-ARCHITECTURE.md` section 24 and the file-by-file plan is `DYNAMIC-IMPLEMENTATION-PLAN.md` section 22.30. Documentation reconciliation is complete; the revised plan is frozen. Source implementation and verification have not started. Exact source paths and current declarations for concrete renderers must be verified before edits.

The content-owned spacing revision is implemented on `feature/dynamic-content-spacing-contract`: `ElementContent.spacing: DynamicSpacing?` is shared by IconContent and DividerContent, the mapper reads backend `properties.spacing`, and existing renderers apply the four-sided external spacing. The owner ran the six-module JVM matrix (`:core:jvmTest`, `:domain:jvmTest`, `:data:jvmTest`, `:navigation:jvmTest`, `:feature:splash:jvmTest`, `:feature:dynamic:jvmTest`), Android unit tests, Android Kotlin compilation/debug assembly, Web/Wasm development webpack, and Web/Wasm browser tests successfully on PR head `c08a6729186590eb094e79176f34daa119aa8a4a`. The full line-by-line source/diff audit and actual Web visual inspection remain open; do not treat this branch as finally frozen.

The original runtime architecture remains unchanged. The asset amendment adds only shared local resources, renderer fallback handling, tolerant mapping for missing URLs, and focused mapper regression coverage.

Do not mark the current branch FINAL FROZEN again until the owner-environment Gradle tests/build and the final source/diff audit pass.

## 2. Implemented runtime contract

### Runtime and references

- `DynamicValue` preserves String, Number, Boolean, Object, Array and Null.
- Numeric-looking JSON strings remain `StringValue`.
- `DynamicRuntimeState` is generic retained flow state.
- Nested dotted state paths are supported.
- `DynamicReferenceResolver` is the single path-traversal boundary.
- Supported references are only `$binding`, `$context`, `$state`, and `$literal`.
- `$response` and unknown namespaces are rejected.
- Object and array references preserve their DynamicValue types.

### Screen state and Store

- `DynamicScreenState` is entry-scoped.
- Bindings are typed.
- RuntimeState is not copied into screen state.
- `DynamicStore` uses the injected form validator.
- One-shot effects use the buffered effect mechanism.
- Store does not own navigation, HTTP, rendering, URI launching or business logic.
- No persistent previous-response state remains.

### Actions

- `DynamicActionExecutor` is the single action dispatch/sequence boundary.
- Duplicate handler registration fails deterministically.
- Sequence depth is bounded.
- Sequence state changes propagate to subsequent actions.
- Request actions use the existing repository boundary.
- Response retention is explicit and generic.
- `responseMode = none` can complete without a destination.
- Navigate, Present and Dismiss return navigation instructions.
- External URI returns an effect; the application performs the actual launch.
- No `ExecuteDynamicActionUseCase` exists.

### Navigation

- `DynamicNavigationEntry` owns only destination identity.
- `DynamicNavigationState` owns the stack.
- Push is the default operation.
- Replace and Pop are explicit.
- Equal destinations can be pushed as independent entries.
- Back pops the current entry and reveals the previous entry.
- Dynamic does not create a second navigation stack or cache.

### Rendering and registries

- Root rendering dispatches through `DynamicRegistry`.
- No hardcoded form-template business branch exists in the generic root renderer.
- Root identity validates screen ID, template ID and template type.
- The frozen hierarchy remains Template → Component → Section → Group → Element.
- Source baseline before the frozen registry refactor: concrete element models do not yet implement the planned `DynamicElement` contract. The approved refactor will add that marker solely to type the element collections and generic renderer contract; no source changes have been made for this work.
- Concrete element mapper/renderer boundaries are separated for Text, Image, Input, Button and Icon.
- Duplicate normalized registrations fail deterministically.
- Rendering does not perform network, navigation, action execution or RuntimeState mutation.
- Content remains a separate capability.
- Template and Section horizontal/vertical arrangement contracts are explicit and no vertical property is reused for horizontal semantics.
- Node-owned padding/size/fill properties are mapped and applied at the owning node.
- Input weight is mapped and honored inside its Row scope.
- Button styling remains inside the Button renderer rather than the common layout helper.
- Responsive behavior uses normal Compose constraints such as fill/maxWidth/weight; no ConstraintLayout or custom responsive engine was introduced.

### Data and mapping

- `DynamicResponseMapper` uses the injected kotlinx.serialization Json.
- JSON values preserve semantic types.
- Object references are not stringified.
- Screen payload and transient response payload are kept separate.
- Explicit response retention maps response data into generic RuntimeState.
- No business-specific Dynamic repository/network/domain layer was introduced.

### Composition and DI

- One RuntimeState is created per active Dynamic flow.
- Navigation entries receive entry-specific Stores.
- There is no global DynamicStore map or destination-keyed cache.
- Dynamic dependencies are composed through Koin.
- Action handlers are collected through the existing DI boundary.
- No ViewModel was introduced.

## 3. Source-level audit findings resolved

The final audit corrected and verified:

1. Mapper tests use the injected Json constructor.
2. Generic contract tests use frozen `$state` references.
3. Reference-rejection tests use literal dollar-prefixed namespaces.
4. Store retention tests load the entry before dispatch.
5. RuntimeState coverage includes Object and Null.
6. Sequence-depth guard coverage exists.
7. Navigation Replace coverage exists.
8. Renderer template-type identity validation exists.
9. Generic fixture assertions were aligned with the authoritative fixture; the fixture itself was not weakened.

## 4. Forbidden-pattern audit

The Dynamic source/test sweep found no remaining implementation uses of:

- `previousResponse`;
- `previousResponseData`;
- persistent `ResponseState`;
- business Dynamic namespaces;
- `DynamicScreenCache`;
- `DynamicNavigationStack`;
- `DynamicBackStack`;
- Dynamic-specific `HttpClient`;
- String-only Dynamic runtime contracts;
- generic root hardcoding of the form template type.

The remaining `$response` occurrences are intentional tests asserting deterministic rejection of the forbidden namespace.

## 5. Backend JSON reconciliation

The attached Config/Login/Send-OTP/OTP JSON is reference input only.

The current frontend contract requires:

- explicit response retention into generic RuntimeState;
- `$state` for retained challenge/phone values;
- `$binding` for current screen input;
- `$context` only for generic environment metadata;
- `$literal` for fixed backend-provided literals.

The frontend must not add `$response` or business-specific `authFlow.*` context to accommodate stale JSON.

The generic JSON type policy also preserves phone numbers and OTP values as strings when the backend supplies them as JSON strings.

## 6. Baseline verification before the current amendment

The results below record the prior baseline only; they do not verify the accessory-owned padding changes.

Required post-refactor matrix:

```powershell
.\gradlew.bat :data:jvmTest :network:jvmTest :navigation:jvmTest :feature:dynamic:jvmTest
```

Result:

```text
BUILD SUCCESSFUL
42 actionable tasks: 18 executed, 24 up-to-date
```

The only reported platform warning was the expected Windows limitation that `iosSimulatorArm64Test` is disabled because iOS simulator tests require macOS. It is non-blocking and no workaround was introduced.

Final source/diff audit was completed after the green matrix. No implementation violation of the frozen UI contract was found.

Post-refactor required matrix:

```powershell
.\\gradlew.bat :data:jvmTest :network:jvmTest :navigation:jvmTest :feature:dynamic:jvmTest
```

Run this after pulling the latest `feature/dynamic-ui` head. If the matrix fails, fix only genuine failures within the frozen plan, then repeat the source audit.

Historical focused regression set:

```text
BUILD SUCCESSFUL
18 actionable tasks: 6 executed, 12 up-to-date
```

Broader Dynamic JVM matrix:

```powershell
.\gradlew.bat :data:jvmTest :network:jvmTest :navigation:jvmTest :feature:dynamic:jvmTest
```

Result:

```text
BUILD SUCCESSFUL
42 actionable tasks: 11 executed, 31 up-to-date
```

The Windows warning that `iosSimulatorArm64Test` is disabled because iOS simulator tests require macOS is expected and non-blocking.

## 7. Remaining platform verification

These are separate platform gates and do not reopen the frozen Dynamic architecture:

- Web/Wasm runtime verification;
- Android runtime verification;
- iOS/Xcode runtime verification on macOS.

Backend local OTP delivery/provider work is also separate from the frontend Dynamic contract.

## 8. Historical state before content-owned spacing revision

This section records the previous accessory-padding implementation state and is superseded by section 14 for the current contract. The previous baseline freeze does not cover the accessory-owned padding amendment. The amendment is implemented and owner-environment JVM tests plus targeted compilation have passed. The complete final diff/obsolete-property repository audit and Web visual verification remain pending; do not mark the branch FINAL FROZEN.

Final audit coverage included:

1. frozen implementation plan comparison;
2. all 10 previously identified UI-contract points;
3. repository/source sweep confirming no `DynamicElement` abstraction remains;
4. concrete element mapper and renderer dispatch;
5. node-owned layout/property ownership and application order;
6. responsive-layout contract;
7. Login/OTP JSON compatibility and generic type preservation;
8. final Git history/diff review from the frozen-plan baseline;
9. authoritative status-document reconciliation;
10. final freeze state.

The audit found no genuine contract violation requiring additional implementation changes.

## 9. Authoritative Dynamic documents

1. `DYNAMIC-ARCHITECTURE.md`
2. `DYNAMIC-IMPLEMENTATION-PLAN.md`
3. this status document

Superseded Dynamic discussion/frozen-plan/reconciliation documents have been removed so they cannot compete with the current authority.


## 10. Contract revision: backend-owned assets

Work item: `DYNAMIC-CONTRACT-REVISION-ICON-001`

Implementation authorization was granted after the revision plan was frozen.

Implemented on `feature/dynamic-ui`:
- `IconElement` uses backend `url` + optional `fallbackUrl`; icon-name/glyph semantics were removed.
- `IconContent` uses backend `url` + optional `fallbackUrl`.
- `ImageElement` uses required backend `url` + optional `fallbackUrl`.
- Icon and image rendering use a small shared asset-loading composable that tries the backend primary URL, optional backend fallback URL, then a caller-selected bundled local fallback asset.
- Dynamic test fixtures were converted from icon names to backend-owned dummy URLs.
- Mapper tests assert URL/fallback propagation and now cover missing asset URLs.
- Backend SDUI contract tests verify that icon/image URL and fallback properties are accepted and preserved by the existing open `properties` contract.

No Dynamic Store, navigation, action, hierarchy, or networking architecture was changed.

Local fallback amendment added on 2026-10-10:
- `feature/dynamic/src/commonMain/composeResources/drawable/dynamic_image_fallback.svg`
- `feature/dynamic/src/commonMain/composeResources/drawable/dynamic_icon_fallback.svg`
- Missing/blank asset URLs are passed through to the renderer as empty values so the local fallback can render.
- Backend remains unchanged.

The current owner-environment JVM test matrix and targeted compilation passed on 2026-10-11. The final repository-wide obsolete-property audit, complete final diff review and Web visual verification remain open. Do not mark FINAL FROZEN until the remaining review and visual gates are addressed.


## 11. Superseded text-spacing discussion

The older parent-level text-spacing approach is superseded by the content-owned spacing contract in section 14. Do not reintroduce parent-owned spacing fields or backend dependencies on them.


## 12. Superseded trailing-spacing discussion

The older trailing-only text-spacing approach is superseded by the content-owned spacing contract in section 14. Backend accessory padding is the current contract.


## 13. Historical contract: accessory-owned padding (superseded) — 2026-10-11

**Historical note:** This section records the prior `DynamicPadding`/`properties.padding` approach. It is superseded for accessory content by section 14 below. `DynamicPadding` remains valid for ordinary node/container padding only.

**State:** Plan frozen and implementation authorized on 2026-10-11. Implementation is complete for the approved source scope. Owner-environment JVM tests and targeted compilation passed on 2026-10-11; final diff/obsolete-property audit and Web visual verification remain open.

The backend CB-Backend branch development now emits four-sided properties.padding on divider accessories. The owner reports that backend engine tests/build passed and the live Login and OTP responses now contain the intended trailing/leading divider padding. Backend commits for this contract include 73aec3a, 9e0994a, f086da2, 05250b3, 9894f7c, f67e906, 2231fb8, and 82026d8.

Accepted frontend contract:
- Reuse the existing DynamicPadding type.
- Add one nullable shared padding property to the existing ElementContent domain contract, available to both IconContent and DividerContent.
- Map each accessory's own properties.padding in the existing data mapper.
- Apply the padding in the existing accessory content renderers using their existing root layout modifier and the existing padding helper where suitable.
- Remove the obsolete parent-level text-spacing fields from the frontend model, mapper, renderer, fixtures and tests.
- Do not add abstractions, registries, screen-specific conditions, architecture layers, or unrelated cleanup.

Implemented: the shared nullable `DynamicPadding` property is part of `ElementContent`, both `IconContent` and `DividerContent` carry it, the mapper reads each accessory's own `properties.padding`, and the existing accessory renderer applies it through `applyDynamicPadding`. The obsolete parent-level spacing fields were removed from the domain model, mapper and renderer; generic mapper regression coverage checks four-sided values and absent-padding behavior. On 2026-10-11, the owner ran the historical seven-module JVM test matrix (`:core:jvmTest`, `:domain:jvmTest`, `:data:jvmTest`, `:network:jvmTest`, `:navigation:jvmTest`, `:feature:splash:jvmTest`, `:feature:dynamic:jvmTest`) successfully, then targeted `:domain:compileKotlinJvm`, `:data:compileKotlinJvm` and `:feature:dynamic:compileKotlinJvm` successfully. Source snapshots for the domain contract, mapper, accessory renderer, text renderer, fixtures and tests were reviewed; the branch comparison from `3d1ee3d` to `aec7d37` shows the approved 16-file scope. Still open: a complete line-by-line diff/obsolete-property repository sweep and Web visual verification. Do not claim final freeze or verified platform behavior before those remaining gates pass.



## 14. Content-owned spacing contract revision — 2026-10-11

**Current state:** IMPLEMENTED; owner-environment JVM, Android unit/compile/assemble, and Web/Wasm build/browser-test gates PASSED on PR head `c08a6729186590eb094e79176f34daa119aa8a4a`; final source/diff audit and visual inspection remain OPEN.  
**Revision:** `DYNAMIC-CONTRACT-REVISION-CONTENT-SPACING-001`.  
**Documentation branch:** `feature/dynamic-content-spacing-contract`.

The implementation is committed on `feature/dynamic-content-spacing-contract`. Frontend domain models, JSON mapping and accessory rendering use `DynamicSpacing`; backend contract details are documented here only as cross-repository context and were not changed in this frontend PR. The verification results below are from the current PR head, not from the previous padding revision.

Frozen target:
- `ElementContent.spacing: DynamicSpacing?` replaces `ElementContent.padding: DynamicPadding?` for all content implementations.
- `DynamicSpacing` has `start`, `top`, `end`, and `bottom` external layout-space values.
- Backend content JSON uses `properties.spacing`, not `properties.padding`.
- Remove text-parent `contentSpacing` and `trailingContentSpacing` as competing mechanisms.
- Preserve ordinary node/container `DynamicPadding`, text `letterSpacing`, and the generic renderer/registry architecture.

Implementation and focused tests are committed. Local verification completed successfully on PR head `c08a6729186590eb094e79176f34daa119aa8a4a`: six-module JVM test matrix; `:androidApp:testDebugUnitTest`; `:androidApp:compileDebugKotlin`; `:androidApp:assembleDebug`; `:webApp:wasmJsBrowserDevelopmentWebpack`; and `:webApp:wasmJsBrowserTest`. GitHub returned no PR-triggered workflow runs and zero commit check-runs for this head, so no CI pass is claimed. The remaining gates are the complete line-by-line diff/obsolete-property audit and actual Web visual inspection; do not declare final freeze until those are completed.


## 15. Generic element renderer registry consolidation — plan frozen (2026-10-13)

**Decision:** `DYNAMIC-REGISTRY-ELEMENT-GENERIC-001`.  
**Phase:** Architecture amendment and implementation plan frozen; implementation not started.  
**Source changes for this work:** None.  
**Verification:** No source tests/builds have been run for this work.

The owner approved the model-class-based dispatch amendment. The renderer registry uses one element map keyed by `KClass<out DynamicElement>`; `.element(type, modelClass, renderer)` keeps backend keys explicit for normalized blank/duplicate-key validation, but the key does not select the renderer. `DynamicRenderContext` dispatches by the concrete element model's class identity, without a growing class-to-key `when`. A single internal adapter checks `KClass.isInstance` and contains the only unchecked cast.

The five concrete element models will implement the shared domain `DynamicElement` contract. The component, section, and group element collections will become `List<DynamicElement>`. `:data` keeps its concrete backend-type mapping responsibility; no backend type property is added to domain models solely for renderer lookup. Template/component/section/group/content registry boundaries and content rendering remain unchanged.

**Compatibility preflight:** Kotlin common API exposes `KClass.isInstance`. The configured project's actual Gradle/Compose compilation has not yet been run. Before source edits, compile the adapter in common source and audit affected model collections, mapper usages, registry construction paths, fixtures, and tests. If the frozen design does not compile or violates an existing source constraint, stop and report rather than silently redesigning.

**Next valid action:** open the separate implementation gate, then implement only the frozen file-by-file scope. Until then, do not change source. After implementation, run the frozen JVM test/compile matrix, inspect the complete diff, and update implementation status only from actual results.


## 16. Historical Element registry simplification — superseded for current work (2026-10-13)

**Decision:** `DYNAMIC-REGISTRY-ELEMENT-SIMPLIFICATION-001`  
**State:** PLAN FROZEN; SIMPLIFICATION SOURCE CHANGES NOT STARTED; VERIFICATION NOT RUN

The owner approved Option A: Element must use the same straightforward string-keyed registry pattern as Template, Component, Section, Group, and Content wherever responsibilities align. The current generic `ElementRenderer<T>` / `CheckedElementRenderer` / `KClass` registry is a candidate implementation to simplify, not the accepted target design.

Frozen intended changes:
- Replace the generic renderer and checked adapter with five concrete typed renderer contracts.
- Use normalized string-keyed maps and typed lookup/registration paths for `text`, `image`, `input`, `button`, and `icon`, reusing the existing shared blank/duplicate registration helper.
- Remove model-class lookup, `KClass` registration, the unchecked cast, and the separate normalized-key set.
- Dispatch the heterogeneous `DynamicElement` collection with a local exhaustive `when` that routes each concrete model to its matching typed renderer.
- Keep the five concrete data mappers and `ElementMapperSupport` unchanged in this registry work item; mapper helper design is separate from renderer registry design.
- Keep content registry/rendering, backend mapping semantics, and all unrelated Dynamic architecture unchanged.

Historical next step when this record was written: implement section 22.28. That step is superseded for the current task by section 17 and implementation plan section 22.30; do not execute section 22.28 as the current target. Do not mark implementation complete or frozen before actual verification.


## 17. Dynamic registry, action-effect, and presentation amendment — revised plan frozen (2026-10-13)

**Decision:** `DYNAMIC-REGISTRY-ACTION-EFFECT-AMENDMENT-001`  
**Branch:** `feature/dynamic-ui`  
**State:** COMPLETE PLAN FROZEN; DOCUMENTATION RECONCILIATION COMPLETE; SOURCE-PATH VERIFICATION REQUIRED; SOURCE IMPLEMENTATION NOT STARTED; TESTS NOT RUN

### Owner-approved registry contract

- All six registries—Template, Component, Section, Group, Element, and Content—use concrete model-class identity for registration and lookup.
- All six renderer contracts are generic and typed: `TemplateRenderer<T>`, `ComponentRenderer<T>`, `SectionRenderer<T>`, `GroupRenderer<T>`, `ElementRenderer<T : DynamicElement>`, and `ElementContentRenderer<T : ElementContent>`.
- The six category registries remain separate. No universal registry or new Template/Component/Section/Group domain hierarchy is introduced solely for generic rendering.
- Typed adapters confine unavoidable type erasure/casts at the registry boundary. Concrete renderers receive their concrete model type.
- Existing supported models are explicitly registered; backend string type mapping remains in `:data`. No central concrete-type dispatch is added to `DynamicRenderContext`.

### Action and presentation contract

- Action sequences aggregate ordered effects and state changes. State changes flow step-to-step; a later Completed/state-only result does not erase earlier effects; real failures stop the sequence.
- Store effect delivery and UI consumption preserve order without lossy send paths or cancellation of earlier effects.
- `present` is a modal distinct from navigation push. One active presentation is allowed; the underlying entry/Store remains intact; the presentation has its own entry/Store; a second active presentation is rejected deterministically; dismiss/back prioritizes the active presentation.
- Existing navigation Push/Replace/Pop, request repository boundary, external URI launcher, DynamicValue/reference semantics, backend-owned visual properties, content-owned spacing, no-ViewModel rule, and no-duplicate-infrastructure rule remain unchanged.

### Documentation authority and supersession

- Architecture: `docs/dynamic/DYNAMIC-ARCHITECTURE.md` section 24.
- Exact file-by-file implementation plan, tests, and stop conditions: `docs/dynamic/DYNAMIC-IMPLEMENTATION-PLAN.md` section 22.30.
- This revised decision supersedes conflicting registry-key requirements in earlier records. Historical decisions remain for traceability.
- Source implementation and tests have not started. Verify actual renderer paths and current declarations before source edits; do not guess filenames or create duplicate renderer files.
- Work only on `feature/dynamic-ui`; no new branches, no backend edits, and no work on the separate Element mapper consolidation.
- Do not claim implementation, tests, builds, visual review, or final freeze until they are actually completed and audited.

## 18. DECISION-001 — Six-category mapping final source freeze (2026-10-15)

**State:** IMPLEMENTED → JVM VERIFIED → ANDROID/WEB BUILD VERIFIED → DIFF RECONCILED → SCOPE FROZEN.

- Branch: `feature/dynamic-ui`; final tested source head `5ca1e14e8b8461dcdfeb62c638922ddd31b8c118`; baseline `6630f29b`.
- Source diff: 13 files; 85 insertions, 20 deletions. `git diff --check` reported no whitespace errors.
- Owner ran `./gradlew.bat :domain:jvmTest :data:jvmTest :feature:dynamic:jvmTest :navigation:jvmTest`: BUILD SUCCESSFUL, 46 actionable tasks.
- Owner ran `./gradlew.bat :androidApp:assembleDebug :webApp:wasmJsBrowserDevelopmentWebpack`: BUILD SUCCESSFUL, 232 actionable tasks.
- Supports polymorphic concrete-type dispatch at Template, Component, Section and Group data-mapping boundaries; preserves multiple Component instances, existing Form/Stack models, six independent model-class renderer registries, and existing Element/Content mapping.
- Frozen scope is **Decision 001 only**. No runtime visual QA, iOS build, future concrete Grid/List implementation or whole-feature final freeze is claimed. Older documents' implementation-not-started statements are historical to their named registry/action/effect workstreams and must not override this verified decision.
- Next: Decision 002 (sensitive network logging) remains an independent read-only investigation until separately approved. Other X-Ray findings remain pending.

## 19. DECISION-002 — Shared HTTP raw-body logging removal (2026-10-15)

**State:** IMPLEMENTED → OWNER-TESTED → ANDROID/WEB BUILD VERIFIED → SOURCE DIFF RECONCILED → SOURCE-SCOPE FROZEN.

- Branch: `feature/dynamic-ui`; baseline `ff58dfdca73f901b037d5d34ea0ebcc31576d0d8`; source head `c77afe232447277b67f6b28638f61b608195ba8c`.
- The shared network `RequestHandler` no longer prints raw request body, successful String response body or console-formatting decorations. Transport functionality and headers were preserved. Added one focused test protecting returned String response values.
- Owner environment: `./gradlew.bat :network:jvmTest :data:jvmTest :feature:dynamic:jvmTest` — BUILD SUCCESSFUL, 43 actionable tasks.
- Owner environment: `./gradlew.bat :androidApp:assembleDebug :webApp:wasmJsBrowserDevelopmentWebpack` — BUILD SUCCESSFUL, 232 actionable tasks.
- Git: clean working tree; `feature/dynamic-ui` fast-forwarded to the same source head as origin; `git diff --check ff58dfd..HEAD` passed; two-file diff totals 14 insertions and 57 deletions.
- **Boundary:** Decision 002 source scope only is frozen. No whole Dynamic feature freeze, iOS build confirmation, comprehensive repository logging inventory, or runtime console-capture assertion is claimed. Network cancellation and other X-Ray items remain separate.


## V2 G1-B/S2 Android evidence and next gate — 2026-10-17

**Active branch:** `architecture/dynamic-runtime-v2-discussion-freeze`. **State:** G1-B/S2 integration implemented; owner-reported JVM/Wasm/Android build verification and focused physical Android smoke **PASS within tested scope**. **Not a whole-feature freeze.** This addendum supersedes historical branch-status language above for this V2 slice; older sections remain historical evidence.

**Source and verification:** G1-B/S2 source commits `079107b8`, `a2c5939`, `29f13e08`; visibility correction `211a3bad`. The owner reported `BUILD SUCCESSFUL` for `:app:jvmTest :feature:dynamic:jvmTest :app:compileKotlinWasmJs :androidApp:assembleDebug` (203 tasks). A later networking compatibility fix `edf1c92c` uppercases the HTTP platform header. The exact Android development connection confirmed working uses wireless `adb reverse tcp:3000 tcp:3000` and APK base URL `http://127.0.0.1:3000`; see root README (updated at `c6968573`). A PC-side `curl` with uppercase `ANDROID` returned HTTP 200, while lowercase `android` returned HTTP 500; direct Samsung-to-PC LAN access timed out. Do not conflate that transport issue with G1-B/S2 lifecycle behavior.

**Physical Samsung SM_A356E evidence supplied by owner:** Bootstrap and initial screen GETs returned 200; request POST returned 200, `navigation.applied operation=push`, next screen GET returned 200, and `render.committed outcome=success`. A later POST/navigation/GET path also returned 200. During controlled landscape → portrait rotation on OTP screen, app PID stayed `27585` before/after both orientations; owner confirmed OTP screen remained visible. The active session remained `f1b8d1e1-b880-4438-9256-00bd74be1e0c` in follow-up network and render logs, with successful subsequent POST → navigation push → GET/render after rotation. No `FATAL EXCEPTION` appears in the supplied filtered sample. This supports navigation, process stability, session continuity and post-rotation action operation on the tested device; do not infer process-death restoration from this.

**Boundaries/open verification:** Filtered client logs contain methods/status but not endpoint paths, so *no duplicate Bootstrap on host recreation* is **not yet independently confirmed at endpoint level**; correlate with backend request logs if making that specific claim. Repeated GET + render after rotation are observed, consistent with entry screen reloads, but endpoint identity is not proven by the client logs alone. Android activity recreation itself, retained entry response/input restoration, process death, Web runtime host reattach, and iOS behavior remain separate gates. An earlier change of PID from `26986` to `27585` occurred outside the controlled rotation interval and does not establish a rotation-caused process restart.

**Source-reconciled G3 gate:** `navigation/src/commonMain/kotlin/com/carbroz/cbpartner/navigation/DynamicNavigationEntry.kt` holds entry identity and `bindings` but no loaded screen response snapshot. `feature/dynamic` entry Store can therefore reload the screen on reattach. G3 requires an explicit minimal entry-owned snapshot ownership/type and lifecycle decision respecting module dependencies (navigation must not import feature/dynamic), plus targeted restore/reload/cross-entry-isolation tests. **G3 production implementation is not authorized by this evidence-only update.** Consult V2 contract 20, frozen decisions 89/91/95 and consolidated plan 96, then source-reconcile the exact model/mapper/store boundaries before requesting one material G3 authorization. Do not reopen S1, add managers/caches, touch backend, create branches, or run new builds for this documentation-only update.


## V2 G3 snapshot retention — owner verification, 2026-10-17

**Current branch:** `architecture/dynamic-runtime-v2-discussion-freeze`. **Implementation commit:** `33bb59de08e3acb5af26ac5cb68858b638292f35`. **State:** IMPLEMENTED; focused JVM tests, Wasm compilation, Android assembly/install and Samsung rotation smoke passed. This entry supersedes the preceding G3-not-authorized/pending language. This is a scoped verification record, not a whole-feature or cross-platform lifecycle freeze.

**Approved source design:** `DynamicNavigationEntry` holds the in-memory domain `DynamicResponse` and nullable UI-safe validation message alongside existing entry bindings. `DynamicStore` restores an existing entry snapshot without another screen GET, saves a successful load, and synchronizes safe validation messages. Entry Store is still disposed/recreated normally; `DynamicNavigationState` remains the only stack authority. No extra snapshot model, manager, cache, DI service, backend modification or persistence was introduced. New regressions extend existing `DynamicStoreTest` and `DynamicNavigationStateTest`.

**Owner-run automated/build evidence:** `:navigation:jvmTest :feature:dynamic:jvmTest` — BUILD SUCCESSFUL in 33 s (38 actionable tasks); `:app:jvmTest :app:compileKotlinWasmJs :androidApp:assembleDebug` — BUILD SUCCESSFUL in 50 s (198 actionable tasks). `:androidApp:installDebug '-Pcbpartner.api.baseUrl=http://127.0.0.1:3000'` — BUILD SUCCESSFUL in 41 s, installed on one Samsung SM-A356E device using the existing wireless ADB-reverse setup. Expected Windows iOS simulator and Android host-test warnings were nonblocking; no iOS runtime success is claimed.

**Physical Android rotation evidence:** After opening the relevant flow and clearing Logcat, owner rotated landscape and portrait. Process PID was `30290` before, during and after; `render.committed` succeeded twice at 04:38:28 and 04:38:31 with the same session ID `61ce4f9f-587c-47af-8329-1a79bcd2f180`. No `http.request`, `http.response` or `FATAL EXCEPTION` appeared in the submitted filtered rotation-window output. This is consistent with zero redundant network GET in that observation window and visible re-render; **it does not independently prove that Activity was recreated or a new Store actually restored the snapshot**. Store snapshot restoration itself has focused JVM regression coverage. Treat that distinction as explicit, not as a failed smoke test.

**Outstanding limits:** Direct lifecycle recreate instrumentation and endpoint-level Bootstrap count on recreate remain unverified; process death/hard reload deliberately excluded. Actual Web ComposeViewport teardown/recreate, iOS host retention, and native iOS build/runtime verification remain open. Do not infer these from Wasm compilation or Android success.

**Next gate — G4 cross-platform host parity:** Frozen authority `docs/architecture-proposal/dynamic-runtime-v2/85-DYNAMIC-SESSION-LIFECYCLE-001-PLAN-FROZEN.md`. First perform a focused read-only reconciliation of Android host, Web viewport owner and iOS controller/composition lifecycle with currently implemented `App.kt` retention boundary. Establish accurate guarantees for configuration change vs full host disposal, refresh and process death; identify only reproducible host-specific gaps. No new generic lifecycle manager, Kotlin production edit or expanded persistence contract is authorized by this status update. G5 final verification/review follows when its specific platform evidence is available; the scope of iOS verification is macOS-only.


## V2 G4 host parity and G5 acceptance reconciliation — 2026-10-17

**Authority:** frozen lifecycle plan `docs/architecture-proposal/dynamic-runtime-v2/85-DYNAMIC-SESSION-LIFECYCLE-001-PLAN-FROZEN.md`, decisions 89/91, and G1-B/S2 plan 96. **Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`. **Scope:** evidence and read-only G5 reconciliation; no new Kotlin/build/backend changes. Earlier G4-open statements remain history and are superseded by the bounded observations below. **G4 Android/Web smoke passed within tested scope; G4 cross-platform host parity remains partially unverified.**

### G4 actual evidence and limits

- **Android Samsung A35:** ADB orientation landscape → portrait with PID `30290`; client `render.committed` at 04:46:17.858 and 04:46:21.046 both used the same retained diagnostic session `61ce4f9f-587c-47af-8329-1a79bcd2f180`. No app network request or fatal exception appeared in the supplied filtered interval. Subsequent expanded ActivityThread logs show Vulkan render-engine setup at 04:46:17.719 and 04:46:20.903, but *not definitive onDestroy/onCreate evidence*. Separate `ActivityRecord` entries are associated with different task IDs, not conclusive evidence of this rotation's recreation. Android runtime rotation/screen continuity smoke PASS; actual Activity recreate/store reattachment and no-repeat-Bootstrap endpoint count **unverified**. No lifecycle production logging change authorized.
- **Web Wasm manual smoke (owner):** Bootstrap → Login succeeded, submitting phone navigated Login → OTP, browser window resize preserved OTP screen and typed digits, and F5 restarted Bootstrap → Login. Resize-visible state/input continuity PASS. F5 behavior is expected because hard browser reload is outside in-memory retention scope. No captured browser network trace: do **not** assert zero GET/Bootstrap calls based on UI observations. Genuine `ComposeViewport` destruction/recreation **unverified**.
- **iOS read-only source:** `iosApp/CBPartnerApp.swift` installs `MainKt.MainViewController()`; `app/src/iosMain/.../Main.kt` creates `ComposeUIViewController { App(...) }`. Same shared `App` composition contract; controller destruction/recreation retention and runtime behavior are unverified. Native iOS build/runtime tests require macOS/Xcode; no Windows-native proof is claimed.
- **Shared host boundaries:** `App.kt` owns `retain { mutableStateOf<DynamicSession?>(null) }`, retirement through `RetainedEffect`, and composition-scoped Koin closing via `DisposableEffect`. `ApplicationContent.kt` reuses handed-off session, creates only entry-scoped `DynamicStore` from current Koin graph, and applies effects using one session lease/claim/ack consumer. `DynamicContainer.kt` disposes the Store normally. No actual G4 source defect demonstrated by current evidence; avoid speculative manager, persistence, or refactor.

### G5 acceptance reconciliation (read-only; frozen plan 85 required gates 1–9)

| Gate | Actual evidence | Current verdict |
|---|---|---|
| 1. Host continuity | Android rotation/same session and Web resize/input persistence observed; actual Activity recreation, Web viewport recreate and iOS host recreate not demonstrated | Scoped smoke PASS; host recreation/platform parity OPEN |
| 2. Stack/entry identity | Existing `DynamicNavigationStateTest` cases for push/pop/replace/presentation and entry identity; physical Login → OTP path succeeded | JVM tested / device navigation smoke PASS |
| 3. Bindings and runtime isolation | `DynamicStoreTest`, `DynamicNavigationStateTest`, `DynamicSessionLifecycleTest` present in current source; JVM suites passed after G3 | Focused automated PASS; host reattach isolation not individually proven |
| 4. Entry snapshot/Store restore | G3 commit `33bb59de`, successful `:navigation:jvmTest :feature:dynamic:jvmTest`; Android rotation window observed no GET | Automated PASS; actual fresh Store due to Activity recreation not proven |
| 5. Bootstrap handoff | Android and Web initial Bootstrap/route work; shared session handoff in source | Initial path PASS; exact reattach duplicate Bootstrap endpoint count OPEN |
| 6. Cancellation | `DynamicStoreTest` covers cancelled in-flight action not committing; Store closes on dispose | Automated PASS for covered scope; host cancellation interleavings not directly measured |
| 7. Ordered effect handoff | `DynamicSessionEffectsTest`, `DynamicSessionConcurrencyTest` and application consumer claim/start/ack source; user-reported app JVM tests PASS | Automated/source PASS for existing cases; platform detach-mid-effect edge not directly smoke tested |
| 8. Retirement/Koin lifetime | `DynamicSessionLifecycleTest` and source: `RetainedEffect` final close, Koin `DisposableEffect`, no Store/Koin retained by session | Automated/source checks; actual host retirement graph recreation OPEN |
| 9. No persistence/sensitive data logs | Retained state stored in memory (no SavedState/disk introduced by G3), raw HTTP-body logging previously removed; current flow/network logs shown only summary IDs/method/status | Source-scope PASS; not a comprehensive whole-repository privacy audit |

**Already executed on G3 implementation HEAD `33bb59de`:** `:navigation:jvmTest :feature:dynamic:jvmTest` (38 tasks, BUILD SUCCESSFUL), `:app:jvmTest :app:compileKotlinWasmJs :androidApp:assembleDebug` (198 tasks, BUILD SUCCESSFUL), Android APK install (BUILD SUCCESSFUL) plus physical Android/Web smoke. **Do not rerun unchanged builds for this documentation-only evidence update.**

**G5 disposition:** Verified scoped behavior is green, but **G5 cannot be called final cross-platform PASS/frozen** without distinguishing the still-unproven actual host recreation/retirement, bootstrap endpoint reattach count, Web genuine viewport lifecycle and iOS/macOS verification. These are evidence gaps, not demonstrated defects. Next productive action is one concise final diff/ownership review (S1/G1-B/S2/G3 and host integration only) and platform-evidence closure where environments permit. Do not broaden into repository-wide architecture work, add code solely for diagnostics or declare any unverifiable result PASS. Preserve process-death/hard refresh exclusions.


## V2 G5 focused source review and error boundary correction — 2026-10-17

**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`. **Review baseline:** `510ec70f8274c94bfc89f12b21dcb24b0fd99193`. **Scope:** S1, G1-B/S2 and G3 lifecycle ownership + one narrowly confirmed user-facing load-error exposure; no broad architecture refactor.

**Ownership findings:** `App.kt` retains only the logical session and closes composition-scoped Koin on disposal; `ApplicationContent.kt` performs bootstrap handoff and one claimed-effects consumer; `DynamicSession.kt` owns in-memory runtime/navigation/effect progress, not network or Store; `DynamicStore.kt` owns load/action/reduction; `DynamicNavigationState.kt` alone owns stack/presentation; `DynamicNavigationEntry.kt` owns in-memory response, bindings and safe message; `DynamicContainer.kt` creates/closes the entry Store. G3 changes were scoped to the entry and Store plus existing tests. No additional manager or duplicated navigation/cache layer found. Existing source and tests support these ownership boundaries; cross-host lifecycle scenarios remain limited as in the previous G4 section.

**Confirmed privacy issue:** `DynamicStore.load()` passed raw `error.message` into `DynamicState.Failure`, and `DynamicContainer` renders `current.message`. `RequestHandler.ResponseException.toNetworkException()` propagates backend `DefaultError.message` into typed exceptions; generic/serialization exceptions may likewise carry internal details. Therefore a server-controlled or internal exception message could become visible in UI. This is an existing load-failure defect, not a G3 regression.

**Minimal authorized correction:** change only the Store's screen-load failure UI message to the fixed `Unable to load dynamic screen`; preserve cancellation propagation, original Result failure classification, existing action-failure handling, network transport/logging and backend behavior. Add one `DynamicStoreTest` regression with sensitive-looking upstream message asserting the fixed UI text and absence of a saved response snapshot. No new abstraction or other source changes. **Verification pending owner-run `:feature:dynamic:jvmTest`; no build/test PASS claimed for this correction.** G5 whole-cross-platform freeze remains pending genuine host recreation/iOS evidence.


### G5 error-safety correction verification — owner run 2026-10-17

The owner fast-forwarded the local branch from `510ec70f` to correction commit `dfef3ff0aace143709a27262bf53cb6c471bde08`, then ran `.\gradlew.bat :feature:dynamic:jvmTest` on Windows: **BUILD SUCCESSFUL in 1m 25s; 33 actionable tasks (11 executed, 22 up-to-date)**. This includes the targeted regression asserting that a sensitive-looking upstream load exception is not displayed as a screen-load UI failure. Expected Android host-test / Windows iOS simulator warnings and an additional Kotlin daemon session warning did not fail the build. **Verdict:** focused screen-load error-safety correction VERIFIED on JVM. No Android/Web rebuild was run for this narrow correction; no platform-runtime or whole-G5 freeze is claimed. Previously documented cross-host lifecycle evidence gaps remain open.


## V2 W5-001 renderer visual smoke and temporary-preview cleanup — 2026-10-17

**Implementation:** TemplateShell optional header/body/footer, stop-aware screen and background gradients, shared angle-aware Button/segmented Input application on V2 branch. Owner's prior `:domain:jvmTest :data:jvmTest :feature:dynamic:jvmTest :app:compileKotlinWasmJs :androidApp:assembleDebug` and focused follow-up builds passed. Isolated W5 preview at commit `493b29fc` was manually observed in Web and owner reported matching Samsung Android display. Three preview modes showed header/body/footer ordering, body-only compatibility, empty slot behavior, visible gradients, and basic Input editing. Screenshot visual observation is not pixel-accuracy proof.

**Cleanup commit:** `73e423bcdfa335b19422eed2a0738a1aead287ef` removed only `W5VisualPreview.kt` and its Android/Web opt-in hooks, retaining the unrelated G1 `RetainCompatibilityProbe` and normal app path. **Owner confirms post-cleanup focused `:app:compileKotlinWasmJs :androidApp:assembleDebug` build PASS**, without full output/task counts. **W5-001 verified scope CLOSED**, not all release-platform criteria: precise stop/angle pixels, quantitative padding, network/fallback/local assets and iOS remain separately pending. No new production change required from current evidence. See W5 doc 97 for exact evidence limitations.
