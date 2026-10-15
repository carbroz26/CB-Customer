# 10 — Pass 3: Domain Models, DTOs and Six-Category Mappers

**Status:** Source audit and new-class contract planning; **PLAN DRAFT**, not frozen. No Kotlin files modified.
**Guiding authority:** Discussion-frozen V2 target in documents 01–06. The current implementation is evidence for behavior preservation, not the architecture to imitate.

## 1. What was inspected

Direct source reads: Domain `DynamicScreen`, `DynamicTemplate`, `FormTemplate`, `DynamicComponent`, `StackComponent`, `DynamicSection`, `DynamicGroup`, `DynamicElement`, `InputElement`, `TextElement`, `ElementContent`, `DynamicAction`, `DynamicValue`. Data `TemplateMapper`, `ComponentMapper`, `SectionMapper`, `GroupMapper`, `ElementMapper`, `ElementContentMapper`, `DynamicResponseMapper`, `DynamicActionMapper`, `DynamicValueMapper`. Repository tree inspected for remaining DTO/model names.

**Coverage boundary:** The remaining concrete Domain files (all Button/Image/Icon/StackSection/StackGroup/Content), DTO file bodies, mapper tests and fixture bodies have not all been read at this pass; final per-file signatures/deletions need additional reads. Conclusions below distinguish direct source evidence from proposal.

## 2. Observed behavior and problems

| ID | Verified observation | V2 direction |
|---|---|---|
| D01 | Current `DynamicScreen` has screenId/schemaVersion/targetApp/template/theme, no transport metadata. | Represent as `ScreenDocument`; preserve semantics, not old name. |
| D02 | `DynamicTemplate` exposes id/type/components; `FormTemplate` includes padding and layout, components array. | V2 `TemplateNode`, `FormTemplateNode`, shell/body split; preserve direct JSON fixture compatibility only with deliberate migration. |
| D03 | `DynamicComponent`, `DynamicSection`, `DynamicGroup` expose allowed direct Elements/nested categories. | Keep six independent category interfaces and correct allowable child graph. |
| D04 | `DynamicElement` is marker-only. `InputElement`, `TextElement` hold id, visual fields, actions/content; Input has binding and validation. | V2 `ElementNode` category contract with only genuinely shared identity; unique concrete fields remain concrete. |
| D05 | `ElementContent` is sealed, contains id/semanticRole/spacing/actions; icon and divider are distinct content types. | Keep `ContentNode` independent from `ElementNode`; preserve actions, content-owned spacing and concrete types. |
| D06 | `DynamicAction` is string `type` + untyped payload map + optional destination. | Typed action models, registered typed handlers for **all** currently supported capabilities; no business screen assumptions. |
| D07 | `DynamicValue` is immutable tree with string/double/bool/object/array/null. | Keep resolved `DynamicValue` separate from typed `ValueExpression` (reference vs literal), with migration tested. |
| D08 | Six category mappers already exist and dispatch explicit supported `type` strings. | **Do not change six-category target design due to legacy.** Rebuild as purpose-named `*NodeMapper` or cleanly reuse only equivalent logic. |
| D09 | Template/Component/Section/Group mappers repeat arrangement, alignment, padding logic via shared `DynamicValueMapper`. | Isolate visual/layout decoding into focused property mapper, not one everything-value utility. |
| D10 | `ElementMapper` instantiates action/content mappers internally; handles five concrete elements plus styling, spans, actions and nested content in one class. | Constructor-inject category helpers; keep dispatch as sole category responsibility; extract concrete conversion when substantial, without one-file-per-property overengineering. |
| D11 | `ElementContentMapper` has default-created ActionMapper and silently ignores non-object items in content arrays via `mapNotNull`. | Strict category validation for malformed content, one configured action mapper, explicit unsupported type error. |
| D12 | `DynamicValueMapper` handles generic value conversion, padding/spacing, arrangements, gradients/border/shape, spans, text actions and input presentation. | Split actual responsibilities into `ValueMapper`, `ValueExpressionMapper` and focused `VisualPropertyMapper` / subtype mappers if justified. |
| D13 | `DynamicValueMapper.spans` privately parses actions with different action mapping path from `DynamicActionMapper`. | Exactly one ActionMapper for element events, content and text-span actions. |
| D14 | `DynamicResponseMapper` identifies screen response by presence of four keys and otherwise labels payload as action result; it also extracts destination. | Decode by requested operation, not heuristic JSON shape. Distinct `ScreenDocumentMapper` and `ActionResponseMapper`; same `DestinationMapper`. |
| D15 | `DynamicActionMapper` supports onClick/onLongClick/onValueChange/onFocus/onSubmit but uses untyped action type/payload; direct destination conversion duplicates other destination mapping. | Preserve supported event vocabulary, typed action decoding, centralized destination mapping. |
| D16 | `ElementMapper` uses `.orEmpty()` for missing required text/asset URL and number fallbacks in several places. | Distinguish required vs optional per concrete backend contract; never fake a missing required icon URL. |
| D17 | `FormTemplate` currently includes template padding, whereas V2 requires Body-only padding when slots exist. | Explicit wire-version/compatibility decision; migrate body padding without touching Header/Footer, test fixtures. |
| D18 | `TemplateMapper` uses `components: List` and component mapper maps both direct elements and sections. | V2 preserves permitted children and multiple usages of same concrete template/element across screens. |

## 3. Proposed final Domain classes by responsibility

Each package below is a **target V2 decision** independent of old source paths.

| New path / model | Fields / role | Does NOT own |
|---|---|---|
| `domain/dynamic/model/ScreenDocument.kt` | screenId, schemaVersion, targetApp, template: TemplateNode, optional ScreenTheme | HTTP envelope, Store bindings |
| `domain/dynamic/model/ScreenDestination.kt` | screenId, templateId, templateType, endpoint, method, auth | Back-stack entry identity |
| `domain/dynamic/model/DynamicValue.kt` | immutable resolved scalar/object/array/null tree | reference parsing or mutable state |
| `domain/dynamic/model/ValueExpression.kt` | typed Literal/Reference/ObjectExpression/ArrayExpression, source binding/context/state | resolution side effects |
| `domain/dynamic/model/LayoutProperties.kt` | reusable, small value types for genuinely common padding/size/arrangement | concrete rendering or universal style bag |
| `domain/dynamic/model/VisualProperties.kt` | typed supported background (solid/gradient), border, shape, text presentation | Compose Brush/Modifier |
| `domain/dynamic/model/ValidationRule.kt` | backend validation declarations for bindable controls | hardcoded phone/OTP rules |
| `domain/dynamic/node/TemplateNode.kt` | id, type, structurally permitted components | Stack-only properties |
| `domain/dynamic/node/ComponentNode.kt` | shared id/type and permitted direct elements/sections | Grid/Stack-specific layout |
| `domain/dynamic/node/SectionNode.kt` | shared id/type and permitted direct elements/groups | concrete layout |
| `domain/dynamic/node/GroupNode.kt` | shared id/type and permitted elements | concrete layout |
| `domain/dynamic/node/ElementNode.kt` | common id/type only if real consumers require | universal binding/actions |
| `domain/dynamic/node/ContentNode.kt` | own category, id/role/spacing/actions only if shared | false Element inheritance |
| `domain/dynamic/node/template/FormTemplateNode.kt` | shell/background, body components/layout; optional header/footer typed slots after contract agreement | Screen identity, navigation stack |
| `domain/dynamic/node/component/StackComponentNode.kt` | orientation/arrangement/alignment/padding, direct element/section children | Section layout policy |
| `domain/dynamic/node/section/StackSectionNode.kt` | stack layout plus elements/groups | Group-specific visual |
| `domain/dynamic/node/group/StackGroupNode.kt` | stack layout plus child elements and group decoration | action execution |
| `domain/dynamic/node/element/TextElementNode.kt` | text/spans/typography/layout/content/event actions | reference resolution or click execution |
| `domain/dynamic/node/element/InputElementNode.kt` | binding key, validation, keyboard, maxLength/presentation, content/actions | mutable value or validator |
| `domain/dynamic/node/element/ButtonElementNode.kt` | label/appearance/content/actions | HTTP, binding lookup |
| `domain/dynamic/node/element/ImageElementNode.kt` | URLs/asset fallback, sizing/content/actions | image loading engine |
| `domain/dynamic/node/element/IconElementNode.kt` | **preserve if backend element form remains supported**; URL/fallback and visual/actions | name→native-glyph mapping |
| `domain/dynamic/node/content/IconContentNode.kt` | distinct nested icon content, spacing and actions | full standalone Element behavior |
| `domain/dynamic/node/content/DividerContentNode.kt` | thickness/orientation/color/spacing/actions | standalone DividerElement |
| `domain/dynamic/action/*` | typed Request/Navigate/ExternalUri/Present/Dismiss/State and sequence semantics | executable code in Domain classes |

`BindableElement` should only be introduced if FormValidator/other consumers actually benefit from shared typed binding semantics. Same for a common action-bearing interface: no purely decorative interfaces.

## 4. Concrete V2 DTO + Mapper pipeline and dependency graph

### DTO families
```text
data/dynamic/dto/
  ApiEnvelopeDto.kt
  ScreenDocumentDto.kt
  ScreenDestinationDto.kt
  ActionResponseDto.kt
  node/
    TemplateDtos.kt        # FormTemplateDto, next future types
    ComponentDtos.kt       # StackComponentDto
    SectionDtos.kt         # StackSectionDto
    GroupDtos.kt           # StackGroupDto
    ElementDtos.kt         # Text/Input/Button/Image/Icon DTO
    ContentDtos.kt         # IconContent/DividerContent DTO
  action/
    ActionDtos.kt          # all supported action types/event slots
```
Response DTOs preserve **backend JSON placement**. Root API envelope, `data.template` and `data.destination` are decoded in their own operation-specific context. DTOs alone may use `JsonObject` at polymorphic wire boundaries; Domain is typed.

### Mapper ownership
| Mapper | Public conceptual API | Dependencies |
|---|---|---|
| `ValueMapper` | `map(JsonElement): DynamicValue` for concrete response/body values | none |
| `ValueExpressionMapper` | `map(JsonElement): ValueExpression` for `$literal/$binding/$context/$state` | ValueMapper |
| `VisualPropertyMapper` | `padding/layout/background/border/shape/typography` typed conversions | ValueMapper's primitive extraction only, or internal focused parsing |
| `DestinationMapper` | `map(ScreenDestinationDto): ScreenDestination` | none |
| `ActionMapper` | `map(ActionDto): DynamicAction` and event slots, text-span actions | DestinationMapper, ValueExpressionMapper |
| `ContentMapper` | `map(JsonObject): ContentNode`; strict list decoding | ActionMapper, VisualPropertyMapper |
| `ElementMapper` | `map(JsonObject): ElementNode`; dispatch Text/Input/Button/Image/Icon | ActionMapper, ContentMapper, VisualPropertyMapper, ValueExpressionMapper |
| `GroupMapper` | `map(JsonObject): GroupNode`; construct concrete group + elements | ElementMapper, VisualPropertyMapper |
| `SectionMapper` | `map(JsonObject): SectionNode`; elements/groups | ElementMapper, GroupMapper, VisualPropertyMapper |
| `ComponentMapper` | `map(JsonObject): ComponentNode`; elements/sections | ElementMapper, SectionMapper, VisualPropertyMapper |
| `TemplateMapper` | `map(JsonObject): TemplateNode`; components/slots | ComponentMapper, VisualPropertyMapper |
| `ScreenDocumentMapper` | `map(ScreenDocumentDto): ScreenDocument` | TemplateMapper, ScreenTheme mapping |
| `ActionResponseMapper` | `map(ActionResponseDto): RequestOutcome` | DestinationMapper, ValueMapper |

Constructor graph is acyclic:
```text
ValueMapper -> ValueExpressionMapper
             -> ActionMapper -> ContentMapper -> ElementMapper
                                                -> GroupMapper -> SectionMapper
                                                -> ComponentMapper -> TemplateMapper
                                                -> ScreenDocumentMapper
DestinationMapper -> ActionMapper and ActionResponseMapper
VisualPropertyMapper -> Content/Element/Group/Section/Component/Template mappers
```
Arrows represent consumed dependencies; instantiate leaf mappers first. Any selected model/mapper registry must not generate circular parent-child initialization.

### Representative contracts (signatures are planning sketches, not compile assertions)
```kotlin
interface TemplateNode { val id: String; val type: String; val components: List<ComponentNode> }
interface ComponentNode { val id: String; val type: String; val elements: List<ElementNode>; val sections: List<SectionNode> }
interface SectionNode { val id: String; val type: String; val elements: List<ElementNode>; val groups: List<GroupNode> }
interface GroupNode { val id: String; val type: String; val elements: List<ElementNode> }
interface ElementNode { val id: String; val type: String }
interface ContentNode { val type: String }

class ScreenDocumentMapper(private val templates: TemplateMapper) {
    fun map(dto: ScreenDocumentDto): ScreenDocument = ScreenDocument(
        screenId = dto.screenId,
        schemaVersion = dto.schemaVersion,
        targetApp = dto.targetApp,
        template = templates.map(dto.template),
        theme = /* type-safe theme conversion */,
    )
}
```
Do not implement placeholder code literally. Interfaces are minimal, and actual DTO default/non-null policies are finalized after complete DTO reads.

## 5. Suggested per-file migration list (first pass)
| Old production source | New target |
|---|---|
| `domain/model/dynamic/DynamicScreen.kt` | `domain/dynamic/model/ScreenDocument.kt` |
| `domain/model/dynamic/DynamicResponse.kt` | `RequestOutcome.kt` + data-layer envelopes |
| `domain/model/dynamic/template/DynamicTemplate.kt`, `FormTemplate.kt` | `domain/dynamic/node/TemplateNode.kt`, `template/FormTemplateNode.kt` |
| `domain/model/dynamic/{component,section,group}/...` | corresponding `domain/dynamic/node/*` category and concrete node |
| `domain/model/dynamic/element/*` | corresponding `domain/dynamic/node/element/*Node.kt` preserving all supported elements |
| `domain/model/dynamic/content/*` | corresponding `domain/dynamic/node/content/*Node.kt` |
| `domain/model/dynamic/value/*` | small typed `model` value/expression/visual/layout contracts |
| `data/dynamic/model/*` | `data/dynamic/dto/*` with type-specific model DTO grouping |
| `data/dynamic/mapper/DynamicResponseMapper.kt` | separate `ScreenDocumentMapper.kt`, `ActionResponseMapper.kt` |
| `data/dynamic/mapper/DynamicValueMapper.kt` | `ValueMapper.kt`, `ValueExpressionMapper.kt`, `VisualPropertyMapper.kt` |
| `data/dynamic/mapper/DynamicActionMapper.kt` + private span conversion | one typed `ActionMapper.kt`, wired into text/content mapping |
| `data/dynamic/mapper/ElementContentMapper.kt` | `ContentMapper.kt`, strict content decoding |
| `data/dynamic/mapper/{Element,Group,Section,Component,Template}Mapper.kt` | corresponding V2 category mappers, typed models |
| Multiple destination mapping bodies | one shared `DestinationMapper.kt` |

**Do not rename the project package root by default.** `com.carbroz.cbpartner` can remain, with V2 package responsibility layout underneath. Whole-repository namespace replacement is unrelated risk unless justified.

## 6. Coverage matrix required before freezing plan

Each existing concrete model and its DTO must have:
1. Every declared JSON field accounted for (required, optional, default, unsupported).
2. Valid concrete type decoding; unsupported type error.
3. Structural children preserved, including direct elements and nested sections/groups and ordering.
4. Backend styling semantic fidelity (padding, content-owned spacing, alignment, background/gradient, text spans, assets).
5. Event action mapping consistent across Element/Content/Span, including external_uri and all existing handlers.
6. Typed expression resolution test for literal, binding, context, retained state, object/array and missing required paths.
7. Missing/invalid required fields rejected rather than silently turned into empty string, unless contract explicitly makes them optional.
8. Generic validation across input types, not phone/OTP-specific UI implementation.
9. Screen vs action response envelope tests; HTTP 200 with application error; destination nested in action data, not root.
10. Renderer compatibility and Compose interaction tests for each supported node.
11. Multi-platform compilation/runtime acceptance after implementation; nothing has been compiled in this audit.

## 7. Strong guardrail: V2 is target, not legacy
A source mismatch is a migration work item. Only a demonstrable V2 design defect, backend impossibility, Kotlin/platform constraint or owner-approved contract improvement can change V2. Never switch back to raw DynamicAction payload or old `DynamicResponse` merely because adapting callers is faster. Fast means fewer unnecessary intermediate layers, coherent vertical changes, exhaustive tests—not quiet architectural compromise.

## 8. Next required work
- Read remaining concrete Domain files and all concrete DTO definitions/tests/fixture cases line-by-line; issue field-by-field tables, not assumptions.
- Audit existing caller imports of removed model names and plan compile-order replacements.
- Confirm proposed Template Shell/Header/Body/Footer wire migration with backend contract: body padding must never affect slots.
- Confirm which action events (onClick/onLongClick/onValueChange/onFocus/onSubmit) and Action types are truly supported by production server.
- Complete Pass 4 runtime/action/store and Pass 5 rendering/platform; merge into exact per-file manifest for PLAN FROZEN review.

**Verification:** source/doc read only; no Gradle/test/device run and no Kotlin implementation changes.
