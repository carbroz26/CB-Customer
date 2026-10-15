# 16 — Pass 5 Final Verification + Passes 1–5 Cross-Audit

**Status: SOURCE RECONCILIATION COMPLETE FOR REVIEWED FILES; OVERALL PLAN NOT FROZEN.** This is documentation/static source review only; no source code edited and no test/build/platform execution. Do not report 100% coverage or V2 implementation readiness on this basis.

## Checked
- Planning docs 01–07 and source reconciliation/audit docs 08–15, including canonical owner Login/OTP fixtures described in doc 12.
- Rendering sources: FormTemplate, StackComponent/Section/Group, Text/Input/Button/Image/Icon renderers, Icon/Divider content, image loader, layout/color helpers, registry contracts/registration, DynamicRenderer and RenderContext.
- Platform files: shared App, Android MainActivity, Web Main, iOS Main, DynamicNavigationHost, feature Dynamic Gradle configuration.
- `DynamicRegistryTest.kt` assertions: six category registration, independent category maps, same class reused across instances, duplicate registration rejection.
- Pass 4 inspected actions/Store tests and caller graph, Pass 3 DTO/domain matrices, Pass 2 network/bootstrap/splash and Pass 1 module/DI/nav findings.

## Pass 5 concrete verification findings

| ID | Status | Finding / V2 resolution |
|---|---|---|
| F501 | CONFIRMED | Registry is correctly category-separated and has duplicate tests. **Reuse small category registry, do not create six independent manager subsystems.** |
| F502 | CONFIRMED | No renderer-specific `commonTest` source was returned in tree listing. Existing registry tests do NOT prove UI pixel/semantics correctness. Plan Compose UI, screenshots and fixture tests explicitly. |
| F503 | CONFIRMED | Source Stack renderers render direct `elements` before `sections` or `groups` where both exist. Owner JSON has separate arrays. Preserve this deterministic ordering unless product approves explicit new order metadata; do not build speculative mixed-node ordering schema. |
| F504 | CONFIRMED | Current asset primitive uses Coil + primary URL + optional fallbackURL then locally bundled painter; owner approves an **optional generic local fallback** after network/cache failure. It is a reusable rendering helper, not UI node category nor second network stack. Target rename `renderer/asset/RemoteAssetImage.kt`. |
| F505 | CONFIRMED | All Icons must have backend-provided `url` (optional `fallbackUrl`); legacy name-only icon JSON must be migrated in backend. No name→glyph mapping. Local generic icon placeholder must not imply incorrect action semantics. |
| F506 | CONFIRMED | `DynamicLayout.kt` owns modifiers for 9 concrete types plus visuals. Split into focused `LayoutModifiers` and `VisualConverters` only if simplifying existing overlap; do not generate excessive classes. |
| F507 | CONFIRMED | Backend gradient `colors[].stop` is present in fixtures but DTO Domain GradientStop currently only stores color; `angle` is not respected in button gradient. Add typed position and render proper angle and stops. |
| F508 | CONFIRMED | OTP text resend `enabled:false` and `disabledColor` are present in fixture but ignored in old Domain mapper/renderer. Preserve and block disabled events; no login/OTP-specific code. |
| F509 | CONFIRMED | Input SegmentedInput defaults to number keyboard regardless of declared `keyboardType` and does not dispatch all declared focus/submit events. V2 typed input interactions need testable generic routing. |
| F510 | CONFIRMED | Image/Icon/Divider/required binding can silently not render in some current cases; mapping must identify malformed required fields; UI must never silently blank out whole screen. |
| F511 | CONFIRMED | Native iOS reads Info.plist API origin; Web uses DOM meta or derived :3000; Android uses BuildConfig. Cross-platform env/HTTPS/browser CORS and app DI startup are verification gates. |
| F512 | OPEN CONTRACT | Header/Footer are optional *additive fields* at Template level; no relocating `template.components[]` into `body`. Current template padding applies only to Body once slots exist. Exact slot DTO shape, scroll/fixed/collapsible config and slot cardinality are **not frozen**. |
| F513 | OPEN DESIGN | A separate class each for HeaderSlotRenderer, BodySlotRenderer and FooterSlotRenderer may be unnecessary overengineering if wrappers only call existing ComponentRegistry. Keep `renderer/shell/TemplateShell.kt` and create focused functions; split files/classes only where different behavior/scroll/insets warrants it. |
| F514 | OPEN CONTRACT | Define generic placeholder per asset type and absence/permission policy, relative URL resolution and Coil cache identity. Asset URL cannot silently inherit token to external host. |
| F515 | OPEN PLATFORM | iOS/macOS, Web/Wasm browser, Android device or screenshot execution not done; source review cannot prove visual parity. |

## Recheck of Passes 1–4

| Pass | Confirmed correct target | Missed issue / correction |
|---|---|---|
| 1: modules/DI/navigation/registry | Keep existing Gradle module topology, Koin at app lifetime, single NavStack and separate presentation, six renderer categories; per-entry Store. | Only one `DynamicContainer` pipeline; avoid speculative RegistryManager/DI manager; define effect delivery after originating entry disposal; separate nav `entryId` from templateId. App Kotlin Koin lifecycle remains unresolved until platform verification. |
| 2: transport/bootstrap | One shared Ktor client, typed response classification by requested operation, centralized headers/auth and strict HTTP vs app-code failure. | Canonical Login/OTP destinations contain four fields, not six. Method/auth may be additive, never pretend fixtures include them. Shared JSON config must not introduce a second transport, yet one Json *instance* is not mandatory if strict configuration equality suffices. No generic Bootstrap use-case that just forwards repository. |
| 3: models/mappers | Exactly one screen Template; Template contains one or multiple Components; six categories and supported nesting; concrete typed DTO/model. | Old exactly-one-Component statement rejected. URL-only Icon migration required, while local fallback is allowed. OTP `enabled/disabledColor`, gradient stops and content `name` conversion are explicit fixture issues. Avoid `sealed` across child subpackages; split value/visual/action parsing but avoid one mapper class per primitive. |
| 4: actions/runtime/Store | All seven existing capabilities, single resolver, store intent/state/effect, request validation in nested sequence, ordered effects, retained typed values and refresh. | Initial plan incorrectly called only two actions required; docs corrected. Missing-vs-null, partial sequence failure, host effect delivery despite navigation-disposal, action failure UI and refresh binding semantics need exact contract decisions. |
| 5: render/platform | Reuse existing Compose+Coil, structure and registry, additive Shell, platform-owned insets and source-driven visuals. | Renderer UI tests absent from reviewed test tree; gradients incorrect, missing fields, legacy PNG behavior now owner-approved optional generic fallback. Do not add many trivial SlotRenderer wrapper classes. |

## Corrections already applied to authoritative proposal drafts (this audit turn)
- `02` now distinguishes actual four-field destinations from **proposed** method/auth extensions.
- `03` keeps canonical `template.components[]` and shows additive header/footer rather than `body.components[]`. Treats IconElement as supported, and records URL-only backend icon migration plus local generic image fallback.
- `04` corrects initial action set to all seven and makes nested Request validation mandatory.
- `07` explicitly preserves all seven actions, without deferring preservation.
- `README` reflects existing plan draft and no implementation freeze.
Older passages elsewhere in docs may remain historically inconsistent; before PLAN FROZEN, run exhaustive string/search reconciliation across all 01–16 docs and authoritative repository docs. This document takes precedence as source of the latest owner decisions where a stale draft contradicts it.

## Lean final ownership proposal (no extra modules)

```text
:app                   application composition, session lifecycle, platform effect handoff
:core                  approved runtime metadata/log contracts only
:network               one Ktor client / transport / headers / auth policy
:domain                typed ScreenDocument, 6 categories, actions, values, repository contracts
:data                  wire DTOs, 6 category mappers, screen/action/bootstrap mapping, repositories
:navigation            one stack owner; entry identity, pop/replace/present/refresh policy
:feature:splash         bootstrap Store and static Splash composable
:feature:dynamic        one entry Store, resolver, validator, actions, renderer registry, renderers, asset helper
:androidApp/:webApp/iosApp platform boot/configuration and integrations
```

```text
feature/dynamic/renderer/
  ScreenRenderer.kt
  template/FormTemplateRenderer.kt
  shell/TemplateShell.kt                  # slot placement, body-only padding, insets/scroll
  component/StackComponentRenderer.kt
  section/StackSectionRenderer.kt
  group/StackGroupRenderer.kt
  element/{Text,Input,Button,Image,Icon}ElementRenderer.kt
  content/{Icon,Divider}ContentRenderer.kt
  asset/RemoteAssetImage.kt               # shared Coil/cache/remote URLs/optional local generic fallback
  layout/LayoutModifiers.kt
  style/VisualConverters.kt
```
No mandatory standalone `HeaderSlotRenderer.kt`, `BodySlotRenderer.kt`, `FooterSlotRenderer.kt` until their distinct logic justifies classes; they may be private `@Composable` functions inside TemplateShell. Concrete future Template types use `template/` and may reuse same Shell infrastructure. No additional seventh category.

## Outstanding gates — BLOCK PLAN FROZEN
1. **Exact Header/Footer wire shape**: slot properties (padding/fixed/collapsible), supported components, app/system safe-area and keyboard/scroll interactions; owner/backend approval and fixture.
2. **Backend icon migration**: URL/fallbackUrl for every formerly name-only icon, and visual fixture acceptance (not just code-level acceptance).
3. **Navigation**: popTo+refresh wire semantics, missing/duplicate target policy, binding retention/recreation and multi-effect session handoff.
4. **Sequence failure**: decide partial success/failure publication and state patch atomicity; request network side effects cannot be rolled back.
5. **Pass 6 file-by-file compile-ready plan** with exact create/modify/delete, function signatures/callers, new vs old DTO mapping, dependency cycles and tests.
6. **Operational source baseline**: verify HEAD and project docs prior to actual implementation; local Windows/KMP builds and platform tests must pass for eventual *implementation freeze*. Do not require an implementation build merely to approve a future plan, but distinguish these gates clearly.

### Freeze classification
- **Discussion/architecture principles:** stable for modules, six UI categories, typed values/actions, SRP and canonical JSON, subject to recorded owner clarifications.
- **Passes 1–5 source audit findings:** reviewed and recorded, with unresolved contract gates.
- **PLAN FROZEN:** **NO**. It would be misleading to mark it frozen before open wire/behavioral decisions and Pass 6's exact migration manifest.
- **IMPLEMENTATION FROZEN:** **NO**. No source implementation performed.

## Next action
Resolve five explicit plan-blocking contracts and create Pass 6 exact implementation blueprint. Then circulate a single reconciled freeze candidate for owner approval, update master project docs after authoritative decision, and only afterward authorize Kotlin edits.
