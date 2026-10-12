# Dynamic UI Implementation Status

**Status:** CONTENT-OWNED SPACING IMPLEMENTED — JVM/ANDROID/WEB BUILD AND TEST GATES PASSED; FINAL SOURCE/DIFF AND VISUAL REVIEW OPEN
**Date:** 2026-10-11
**Authority:** `docs/dynamic/DYNAMIC-ARCHITECTURE.md`
**Plan:** `docs/dynamic/DYNAMIC-IMPLEMENTATION-PLAN.md`

## 1. Current truth

The content-owned spacing revision is implemented on `feature/dynamic-content-spacing-contract`: `ElementContent.spacing: DynamicSpacing?` is shared by IconContent and DividerContent, the mapper reads backend `properties.spacing`, and existing renderers apply the four-sided external spacing. The owner ran the seven-module JVM matrix (`:core:jvmTest`, `:domain:jvmTest`, `:data:jvmTest`, `:network:jvmTest`, `:navigation:jvmTest`, `:feature:splash:jvmTest`, `:feature:dynamic:jvmTest`), Android unit tests, Android Kotlin compilation/debug assembly, Web/Wasm development webpack, and Web/Wasm browser tests successfully on PR head `c08a6729186590eb094e79176f34daa119aa8a4a`. The full line-by-line source/diff audit and actual Web visual inspection remain open; do not treat this branch as finally frozen.

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
- Concrete element models no longer inherit from a generic `DynamicElement` abstraction.
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

## 8. Current state

The previous baseline freeze does not cover the accessory-owned padding amendment. The amendment is implemented and owner-environment JVM tests plus targeted compilation have passed. The complete final diff/obsolete-property repository audit and Web visual verification remain pending; do not mark the branch FINAL FROZEN.

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

The older parent-level text-spacing approach is superseded by the accessory-owned padding contract in section 13. Do not reintroduce parent-owned spacing fields or backend dependencies on them.


## 12. Superseded trailing-spacing discussion

The older trailing-only text-spacing approach is superseded by the accessory-owned padding contract in section 13. Backend accessory padding is the current contract.


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

Implemented: the shared nullable `DynamicPadding` property is part of `ElementContent`, both `IconContent` and `DividerContent` carry it, the mapper reads each accessory's own `properties.padding`, and the existing accessory renderer applies it through `applyDynamicPadding`. The obsolete parent-level spacing fields were removed from the domain model, mapper and renderer; generic mapper regression coverage checks four-sided values and absent-padding behavior. On 2026-10-11, the owner ran the seven-module JVM test matrix (`:core:jvmTest`, `:domain:jvmTest`, `:data:jvmTest`, `:network:jvmTest`, `:navigation:jvmTest`, `:feature:splash:jvmTest`, `:feature:dynamic:jvmTest`) successfully, then targeted `:domain:compileKotlinJvm`, `:data:compileKotlinJvm` and `:feature:dynamic:compileKotlinJvm` successfully. Source snapshots for the domain contract, mapper, accessory renderer, text renderer, fixtures and tests were reviewed; the branch comparison from `3d1ee3d` to `aec7d37` shows the approved 16-file scope. Still open: a complete line-by-line diff/obsolete-property repository sweep and Web visual verification. Do not claim final freeze or verified platform behavior before those remaining gates pass.



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

Implementation and focused tests are committed. Local verification completed successfully on PR head `c08a6729186590eb094e79176f34daa119aa8a4a`: seven-module JVM test matrix; `:androidApp:testDebugUnitTest`; `:androidApp:compileDebugKotlin`; `:androidApp:assembleDebug`; `:webApp:wasmJsBrowserDevelopmentWebpack`; and `:webApp:wasmJsBrowserTest`. GitHub returned no PR-triggered workflow runs and zero commit check-runs for this head, so no CI pass is claimed. The remaining gates are the complete line-by-line diff/obsolete-property audit and actual Web visual inspection; do not declare final freeze until those are completed.
