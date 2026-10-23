# 132 — P0 FormFieldSection exact contract, direct-dependency SRP audit, implementation plan freeze

**Date:** 2026-10-23  
**Repo / branch:** `carbroz26/CB-Partner` / `architecture/dynamic-runtime-v2-discussion-freeze`  
**Source HEAD audited:** `29f936cb3dac9c3d6ab816dca891fa06dc5756b6`  
**Decision:** **P0 IMPLEMENTATION PLAN FROZEN** by owner agreement to FormFieldSection and the staged source-audit/contract-freeze workflow. **This commit changes documents only.**  
**Candidate document:** `docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-parity-v8-P0-PLAN-ONLY.json` (**DO NOT serve as the active preview fixture until production implementation and tests pass**).  
**Current executable preview:** `webApp/src/wasmJsMain/resources/preview/partner-login.json`, still **v7**.

## 0. Precedence and protected constraints

The later owner decision supersedes the **field-specific** focus/validation/message/reservation responsibilities historically attributed to `StackGroup` in docs 112/117/118 and introduced in v7/doc131. It does **not** revoke StackGroup's generic layout/styling API or Country Picker selected-row behavior; list-selection cleanup is **P1**. Do not silently edit historical documents or claim their obsolete Group ownership remains frozen. Existing fixed visual reference `webApp/src/wasmJsMain/kotlin/com/carbroz/cbpartner/web/VisualLoginPreview.kt` is read-only. Single Store/registry/render context; no login-specific classes, ViewModels, back-end modifications, native glyph fallbacks, or new UI categories (FormFieldSection is a concrete **Section**).

The frozen overall nesting still applies: Template→Component→Section→Group→Element→Content. A FormFieldSection owns directly authored Elements and optionally a Group; no Group contains a nested Group.

## 1. Exact responsibility ownership

| Responsibility | Owner / invariant |
| --- | --- |
| Template shell/hero, Component spacing | existing FormTemplate and StackComponent, unchanged |
| Label → control → message slot order | **FormFieldSection renderer**, not StackGroup |
| Single field identity | exactly **one** nested bound InputElement; derive binding from it |
| Required/length/named rules | InputElement's existing `DynamicValidation`; validated by existing DynamicFormValidator |
| Retained errors, edit-time revalidation | existing DynamicStore; never a second attempted flag |
| Normal/focus/invalid **whole control border** | FormFieldSection `controlSurface` plus its `focusPresentation`/`validationPresentation`, in order invalid > focused > normal |
| Number-only light-red background | InputElement `validationStyle.background`, not flag/code/divider |
| Flag, dial code, divider, input horizontal arrangement | plain StackGroup; no input error or focus awareness |
| Error text style and fixed empty/message space | FormFieldSection's authored validationPresentation.messageStyle |
| Country selected-state behavior | existing ListSection/StackGroup/IconElement, untouched in P0, **P1 audit** |

Do not infer field kind from `id`, screenId, binding names, 'phone', 'firstName' etc.

## 2. Frozen JSON schema: `form_field_section`

A field has `id`, `type:"form_field_section"`, `properties`, `elements[]`, and optional `groups[]`. Exactly **two** valid shapes:

**Direct:** `elements = [TextElement(label), InputElement(control)]` and `groups = []`.

**Composite:** `elements = [TextElement(label)]` and `groups = [StackGroup(control)]`; that Group contains exactly one bound InputElement and any authored sibling Elements (e.g. country selector and divider). Reject missing/duplicate label, multiple InputElements, extra top-level elements/groups, absent/blank binding, or direct+Group controls together. Accessory icons/images belong to the relevant Element's leading/trailing Content as today.

Required `properties.horizontalAlignment` chooses label/field alignment with existing arrangement mapping (Login `start`); `fillMaxWidth` is explicitly authored. Optional `padding` follows existing DynamicPadding. `label.properties.spacing.bottom` provides the label→control gap and must be explicitly authored if desired; do not hardcode 8dp.

`properties.controlSurface` (required typed object):
```json
{
  "height": 56,
  "fillMaxWidth": true,
  "background": {"color":"#FFFFFF"},
  "border": {"width":1,"color":"#CBD5E1"},
  "shape": {"type":"roundedCorner","cornerRadius":12}
}
```
`height` must be positive/finite; `fillMaxWidth` required boolean. Other authored fields use existing typed DynamicBackground/DynamicBorder/DynamicShape. No implicit colors/radii/gaps. The FormFieldSection wraps the control in a surface with backend-authored clipping/background/border; neither the contained Group nor the Input owns a duplicate **outer** border.

Optional `properties.focusPresentation` reuses **DynamicFocusPresentation** `{binding,border}`. Optional `properties.validationPresentation` reuses **DynamicValidationPresentation** `{binding,errorBorder,messageStyle}`; its `messageStyle.reserveHeight` is required positive/finite whenever validationPresentation is present. Existing fontSize/fontWeight/color/spacing are authored and reused, no invented defaults. For an input with `validation`, inline validationPresentation is required for this concrete Section. Both presentation bindings, when declared, **must equal** the unique bound input's binding; fail mapping if mismatched. Error priority is invalid > focused > normal; falling back to backend's normal border if a particular presentation was not authored is allowed only by the existing contract.

Field controls can opt out of focus/validation presentation when no validation rules are defined. For a no-error field there is no forced reserved slot. The renderer reads `context.validationErrors[inputBinding]` and `context.focusedBinding` only; it does not calculate validity, clear errors or dispatch new actions.

A field-owned control surface forbids duplicate container styling on its child StackGroup: `background`, `border`, `shape`, `height`, `focusPresentation`, and `validationPresentation` must be absent. An InputElement nested in a FormFieldSection may still have its own inner padding/background/`validationStyle` and input behavior, but not a competing authored **outer** `border`/`focusBorder`. These are **field-specific composition constraints**, not global prohibitions for standalone StackGroup or InputElement (Country Picker Search remains unchanged).

A reserved slot must always be laid out at the same height, before/after field validation; text appears inside with authored `spacing.top`. No extra height when invalid. The backend must author enough height for its message length/font/device width; minimum message-fit tests are required.

## 3. Frozen Login v8 **PLAN-ONLY** migration

The attached whole-screen candidate JSON is generated from v7 with changes **only** to `data.template.components[login_form].sections[0..1]`:
- Remove sibling StackSections `phone_label_section` and `phone_field_section`.
- Add one `FormFieldSection(id="mobile_field")`: label TextElement retained verbatim except `spacing.bottom:8`; the Group retains exactly the same three Elements and their actions/bindings/validation.
- Migrate normal phone row height/background/border/shape to `controlSurface` and focus/validation presentation (including reservedHeight 24, top text spacing 6) to the FormFieldSection.
- Strip the migrated field-only presentation/visual properties from the child StackGroup so it owns only horizontal child arrangement.
- **Reuse** the previous phone-field `padding.bottom:24` as the reserved 24dp validation slot instead of adding 24dp on top. No extra field bottom padding; Continue button position should not jump when the message appears or clears.
- Preserve all original country/flag/chevron/divider/input child JSON, including editable-only pink tint `#FFF1F2`, country selector Present, request action, and authored spacing.
- Preserve Section siblings `continue_action_section` and `legal_section` byte-for-byte in normalized JSON; preserve ALL other screen/Theme/hero/Picker/Actions/Bindings data.
- Historical v7 remains immutable. On successful implementation, copy the **exact candidate** into the Web preview fixture and designate the implemented target; until then v7 is the only runtime-compatible fixture.

## 4. Concrete DTO, Domain and renderer API

```kotlin
@Serializable
data class FormFieldSectionDto(
    val id: String,
    val type: String,
    val properties: JsonObject = JsonObject(emptyMap()),
    val elements: List<JsonObject> = emptyList(),
    val groups: List<JsonObject> = emptyList(),
)

data class FormFieldControlSurface(
    val height: Double,
    val fillMaxWidth: Boolean,
    val background: DynamicBackground? = null,
    val border: DynamicBorder? = null,
    val shape: DynamicShape? = null,
)

data class FormFieldSection(
    val id: String,
    val label: TextElement,
    val controlInput: InputElement? = null,
    val controlGroup: DynamicGroup? = null,
    val inputBinding: String,
    val controlSurface: FormFieldControlSurface,
    val horizontalAlignment: DynamicArrangement,
    val fillMaxWidth: Boolean,
    val padding: DynamicPadding? = null,
    val focusPresentation: DynamicFocusPresentation? = null,
    val validationPresentation: DynamicValidationPresentation? = null,
) : DynamicSection {
    // Implement the existing DynamicSection.elements/groups contract using
    // the above exact typed children, and validate XOR control shape.
}
```

Names and declared fields above are frozen. The model has one reference-only `inputBinding` resolved/validated from the bound InputElement during mapping, **not** a duplicate value store. `controlGroup:DynamicGroup` is already within the six-category interface; current implementation accepts StackGroup only. Renderer invokes existing `context.renderElement(label)`, `renderElement(controlInput)` or `renderGroup(controlGroup)`, never bypasses registries. Add exactly one typed Section registration and one dispatch clause.

The model must expose `DynamicSection.elements` and `groups` so existing typed validator traversal, button-ID validation and direct registry traversal keep seeing the same descendants (label+direct input OR label+group). Renderer method should only compose; parsed JSON validation, structural invariants and binding consistency belong in the SectionMapper/domain constructors.

## 5. Direct source dependency audit & exact file plan

| Classification | File / exact modification |
| --- | --- |
| NEW | `data/.../model/section/FormFieldSectionDto.kt` — typed DTO, no generic Any |
| NEW | `domain/.../model/dynamic/section/FormFieldSection.kt` — concrete Section + typed FormFieldControlSurface; direct/composite XOR |
| NEW | `feature/dynamic/.../sdui/section/FormFieldSection.kt` — label/control/reserved-slot composition; style priority |
| CHANGE | `data/.../mapper/SectionMapper.kt` — explicit dispatch, structural checks, binding equivalence, field-owned surface parsing |
| CHANGE | `feature/dynamic/.../registry/DynamicRendererRegistration.kt` — register concrete Section |
| CHANGE | `domain/.../model/dynamic/group/StackGroup.kt` — remove field-only focusPresentation/validationPresentation |
| CHANGE | `data/.../mapper/GroupMapper.kt` — remove field presentation parsers, reject obsolete field-only keys on StackGroup rather than silently ignore |
| CHANGE | `feature/dynamic/.../sdui/group/StackGroup.kt` — remove validation/focus/message/reserve branches; retain only generic layout and currently frozen selectedStyle |
| CHANGE | `feature/dynamic/.../runtime/DynamicLayout.kt` — remove group borderOverride; keep normal authored Group border and shape |
| CHANGE | `feature/dynamic/.../runtime/DynamicFormValidator.kt` — find presented binding in FormFieldSection; keep existing required→pattern→named logic |
| NO CHANGE | `DynamicStore.kt`, `DynamicRenderContext.kt`, `InputElement.kt`, `ButtonElement.kt`, all action handlers and Theme |
| NO CHANGE | `ListSection.kt`, `StackComponent.kt`, `IconElement` (P1 listed below), Country Picker JSON and VisualLoginPreview |
| TEST CHANGE | `data/.../mapper/DynamicValidationMessageContractTest.kt` — move field slot and reserved height assertions from StackGroup to FormFieldSection |
| TEST CHANGE | `feature/dynamic/.../runtime/DynamicFormValidatorTest.kt` — change presented-binding setup; keep standalone fallback message behavior |
| TEST CHANGE | `feature/dynamic/.../store/DynamicStoreTest.kt` — update validationResponse fixture; preserve submit+edit+entry-retention regression |
| TEST CHANGE | `feature/dynamic/.../registry/DynamicRegistryTest.kt` — verify new section registration and duplicate rejection |
| TEST ADD | Mapper tests for direct Name/LastName, composite phone, wrong/missing control, multiple input, binding mismatch, forbidden nested borders and invalid reserveHeight |
| TEST ADD | Rendering/measurement regression for stable height at empty/error/edited-valid, error border applies to composite surface while phone code/divider white, no white strip, focus priority |

**Additional dependency checks before implementation:**
- `ComponentMapper`'s `ListSection.emptySectionId` current sibling validation has an `else` for new Section types; confirm no unintended change and keep fallback target **StackSection only**.
- `StackComponent` dispatches sections generically via registry; keep it unchanged.
- `DynamicResponseMapper.validateButtonIds` and `DynamicFormValidator.validateComponents` iterate Section.elements and Section.groups; preserve both contracts and exact one-input identity.
- `DynamicStore` revalidates on edit only after an error exists, as required. Preserve error persistence/clear behavior; presentation focus and error maps remain scoped.
- Existing `data/.../InputSurfaceLayoutContractTest.kt` covers padding; check whether any fixture assumes group-owned validation. Historic tests/source samples using the old Group presentation must be migrated or intentionally rejected; do not silently add a compatibility rendering pathway.
- **Backend compatibility gate:** backend emission is not modified in P0. If backend still sends field-aware `stack_group` JSON, it cannot be claimed compatible with the new strict Group contract until a separately authorized backend migration. Do not claim end-to-end real-backend parity from Web fixture only.

## 6. P0 tests/acceptance gates

1. Parse v8 candidate into exact new typed Section, verified binding identity, authored assets/actions unchanged; malformed combinations fail fast.
2. Generic first/last name direct fields use identical Section renderer without flag/divider; composite phone also renders through same Section renderer.
3. Empty submit => "Mobile number is required."; invalid 6–9/11–15 => length error; invalid 10 => named validator message; valid India 10 => errors removed. 12→11 retains error border+tint; valid 10 clears both.
4. Normal, focused, invalid outer border priority; flag/code/divider white, number portion pink only when invalid, readonly style unaffected.
5. **Whole field measured height identical in normal/error/cleared states**; reserved 24dp replaces previous spacing and does not extend card when message changes; a one-line message fits target supported viewport.
6. Country Picker selected rows, empty fallback, search, flag assets, bottom sheet, hero and legal links unchanged and rendering visually equivalent.
7. Run `:data:jvmTest :feature:dynamic:jvmTest :feature:dynamic:compileAndroidMain :webApp:wasmJsBrowserDevelopmentWebpack`; then Web screenshot compare to protected `?cbpartnerVisualPreview=1` at equal viewport. Test result must not be asserted before execution.
8. New v8 JSON becomes active fixture only in the implementation wave **after** mapper+renderer/test readiness; no preview break during planning.

## 7. P1 independent SRP findings — RECORD ONLY; NO SOURCE EDIT IN P0

| Class | Specific observed coupling | P1 decision gate |
| --- | --- | --- |
| `StackGroup.selectedStyle` | List row-selected flag applies group background | Relocate list-row visual policy without losing authored row surface semantics |
| `IconElement.visibleWhenSelected` | Reads rowSelected and reserves hidden icon layout | Evaluate reusable conditional visibility vs ListSection-specific policy; do not invent generic visibility engine without approval |
| `StackComponent` | Detects referenced ListSection.emptySectionId and skips sibling StackSection | Move list fallback coordination if a cleaner owner exists; preserve cross-sibling identity and single fallback render |
| `ComponentMapper` | List-specific sibling-reference validation | Revisit reference validation ownership alongside StackComponent, avoid duplicate passes |
| `DynamicRenderContext` | Has fallback lookup and row-selected context alongside render dispatch | Audit responsibilities; avoid second per-screen Store/context |
| `InputElement` | Standard and segmented presentations, focus and actions in one renderer | Input-related behavior may be legitimate; check line-by-line for actual unrelated reasons to change, avoid speculative new InputElement types |
| `ButtonElement` | Request-specific pending origin and loading within general Button renderer | Distinguish Button visual loading from request orchestration; no P0 change |
| `TextElement` | Spannable links and text gestures | Currently a legitimate text capability, not a proven violation |
| `ListSection` | List rows/filter/separators/empty state | List-specific responsibilities look coherent; validate view/evaluator boundary in P1 |
| `FormTemplate`, `StackSection`, Image/Icon/Divider Element/Content renderers | No clear out-of-category responsibility in this reviewed source | Keep unchanged pending deeper evidence; do not invent cleanup |

Note: P1 is an audit backlog, not permission to refactor. In particular, leaving `StackGroup.selectedStyle` during P0 is an **explicit temporary exception** to the ultimate goal of layout-only StackGroup, justified solely to preserve Country Picker behavior. The final P1 disposition needs owner review.

## 8. Stop conditions

Stop implementation (do not ship silent fallbacks) if the new Section cannot keep container clipping and original phone surface shape at the same viewport, the 24dp slot clips approved messages, any old Group presentation fixture is still incorrectly accepted, or Country Picker selection/fallback regresses. Fix only confirmed dependencies, do not change backend/read-only VisualPreview or introduce hidden screen-specific defaults. Workflow: discussion→source audit→contract freeze [this document]→implementation→targeted tests→visual review→acceptance freeze.
