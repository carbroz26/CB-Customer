# 11 — Pass 3 Detailed Field Mapping and Pass 1–2 Recheck

**Status:** DETAILED CONTRACT RECONCILIATION; PLAN DRAFT (not frozen). **No Kotlin source edits, compilation or test execution.**
**Priority:** V2 target architecture is authoritative for planning. Legacy code is evidence of current wire semantics, fields and behavior; it never dictates V2 structure. All paths below are source-relative. Required/optional classifications distinguish DTO serialization requirements from semantic/Domain requirements.

## A. DTO field matrix — full concrete declarations

| Existing DTO type | Exact wire fields and Kotlin defaults | V2 DTO / Domain mapping | Validation/edge cases |
|---|---|---|---|
| `DynamicResponseDto` | `status:Int` required; `code:String` required; `message:String` required; `data:JsonElement?=null`; `traceId:String?=null` | `ApiEnvelopeDto` in data. Operation selects ScreenDocumentDto vs ActionResponseDto, *not* field guessing. | 2xx HTTP with non-SUCCESS application code fails; missing screen/action data per operation fails; traceId never logged unredacted |
| `DynamicScreenDto` | `screenId:String`, `schemaVersion:String`, `targetApp:String`, `template:JsonObject` required; `theme:DynamicThemeDto?=null` | `ScreenDocument` with typed `TemplateNode`, ScreenTheme | Validate expected screenId/templateId/type, supported schema and app target before render |
| `DynamicDestinationDto` | `screenId:String`, `templateId:String`, `templateType:String`, `endpoint:String` all required | `ScreenDestination`, V2 also models method/auth **if backend supplies them** | Common DestinationMapper shared bootstrap/action. Current wire DTO does not decode method/auth. Add contract and explicit agreed defaults, not silent discard |
| `FormTemplateDto` | `id:String`, `type:String` required; `properties:JsonObject={}`; `components:List<JsonObject>=[]` | `FormTemplateNode` + template shell/body + optional proposed header/footer slots | Template type form_template; body-only padding migration. **Backend discussion says exactly one component per template today:** enforce validated cardinality in V2 contract if confirmed, despite list-shaped wire. |
| `StackComponentDto` | `id:String`, `type:String`; `properties={}`; `elements=[]`, `sections=[]` | `StackComponentNode`, direct elements + nested sections | Order and interleaving semantics must be explicit; two separate arrays may not encode cross-category order |
| `StackSectionDto` | `id:String`, `type:String`; `properties={}`; `elements=[]`, `groups=[]` | `StackSectionNode`, elements/groups | Parent constraints and ordering |
| `StackGroupDto` | `id:String`, `type:String`; `properties={}`; `elements=[]` | `StackGroupNode` | No child groups or sections in currently supported wire |
| `TextElementDto` | `id:String`, `type:String`; `properties={}`; `validation:DynamicValidationDto?=null`, `binding:DynamicBindingDto?=null`, `actions:DynamicActionsDto?=null` | `TextElementNode`; text, spans, typography, nested Content and actions; do **not** silently generalize unused validation/binding | Mapper currently ignores validation/binding for Text. Determine whether supported or merely broad DTO schema |
| `InputElementDto` | same common Element fields as Text | `InputElementNode`; binding/validation/presentation and visual/content/action | Required field behavior, max length, keyboard, segmented input |
| `ButtonElementDto` | same common Element fields as Text | `ButtonElementNode` | Mapper currently ignores validation/binding for Button |
| `ImageElementDto` | same common Element fields as Text | `ImageElementNode` | Mapper ignores validation/binding; `url` semantically required though mapper currently `orEmpty()` |
| `IconElementDto` | same common Element fields as Text | `IconElementNode` **preserved** | Icon is real Element *and* icon Content is separate category. URL semantically required, fallback URL optional. |
| `DynamicActionsDto` | `onClick`, `onLongClick`, `onValueChange`, `onFocus`, `onSubmit`: each `DynamicActionDto?=null` | Typed event/action associations, preserving all five hooks | Define lifecycle/event dispatch precisely; don't drop focus/change/submit |
| `DynamicActionDto` | `type:String` required; `payload:JsonObject={}`; `destination:DynamicDestinationDto?=null` | Typed Request/Navigate/ExternalUri/Present/Dismiss/State/Sequence actions as supported | Every variant gets a typed decoder/handler or explicit composition contract; existing sequence is executor-driven, not yet DTO-typed |
| `DynamicBindingDto` | `key:String` required | `InputElementNode.bindingKey` or supported bindable interface | Nonblank and unique-per-scope expectations need contract |
| `DynamicValidationDto` | `required:Boolean?=null`, `pattern:String?=null`, `message:String?=null` | `ValidationRule` | Null means unspecified, NOT false unless backend contract says so. Invalid pattern handling; error text sourcing |
| `DynamicThemeDto` | `theme:String?=null`, `statusBar:String?=null`, `properties:JsonObject={}` | ScreenTheme and/or typed screen-level background/status-bar policy | Distinguish legacy theme from template shell background; avoid uncontrolled draw defaults |

**Important:** The five Element DTO classes share the same raw field shape, but concrete Domain models MUST retain independent, supported properties. Similar JSON skeleton does not imply identical behavior.

## B. Complete concrete Domain/mapper property matrix

All names below come from inspected Kotlin declarations and corresponding mapper expressions. `required` means semantically required in V2 only when explicitly stated. Nullable Domain fields are optional; decoder uses current fallback values, but V2 defaults require contract validation.

| V2 concrete model / current type | Current declared Domain properties and current mapper mapping | V2 property destination and policy |
|---|---|---|
| `FormTemplateNode` / FormTemplate | `id,type,orientation,verticalArrangement,horizontalArrangement,horizontalAlignment,verticalAlignment,padding,fillMaxSize,semanticRole,components`. Mapper: orientation defaults vertical, arrangements/alignments through DynamicValueMapper, padding nullable, fillMaxSize defaults false, semanticRole optional, maps components. | `id` identity, `type` concrete type, body layout holds orientation/arrangements/alignments/bodyPadding/fillMaxSize; shell holds background and proposed header/footer; preserve semanticRole only if renderer/use case uses it. Template component cardinality: discussion guarantees one current template component; model contract to be frozen explicitly. |
| `StackComponentNode` / StackComponent | `id,orientation,verticalArrangement,horizontalArrangement,horizontalAlignment,verticalAlignment,padding,fillMaxWidth,elements,sections`; mapper same reusable visual decoders. | Concrete StackLayout properties + direct elements/sections, no composition duplication. |
| `StackSectionNode` / StackSection | `id,orientation,verticalArrangement,horizontalArrangement,horizontalAlignment,verticalAlignment,padding,fillMaxWidth,elements,groups`. | Concrete StackLayout + direct elements/groups. |
| `StackGroupNode` / StackGroup | `id,orientation,verticalArrangement,horizontalArrangement,horizontalAlignment,verticalAlignment,padding,height,fillMaxWidth,background,border,shape,elements`. | Group layout + decoration + children. No section/group traversal in Group. |
| `TextElementNode` / TextElement | `id,semanticRole,text,spans,fontSize,fontWeight,letterSpacing,lineHeight,color,textAlign,width,maxWidth,fillMaxWidth,padding,leadingContent,trailingContent,actions`. Text mapper: literal property `text` `orEmpty()`; if text is JSON object, adds a DynamicTextSpan from converted value then mapped spans. | Text content modeled by typed literal/reference expressions and spans; preserve all text/style/layout/content/event properties. Clarify whether JSON object text is expression or rich span, avoid arbitrary leading empty span. |
| `InputElementNode` / InputElement | `id,semanticRole,placeholder,keyboardType,fontSize,fontWeight,textColor,textAlign,maxLength,weight,width,maxWidth,fillMaxWidth,padding,validation,binding,presentation,leadingContent,trailingContent,actions`. Defaults: keyboardType text; validation/binding optional; segmented presentation optional. | Bindings and validation own typed field semantics, presentation/keyboard/layout typed, no hardcoded phone/OTP behavior. Missing binding behavior by supported input mode; value type generalization beyond String requires additional concrete Element capabilities. |
| `ButtonElementNode` / ButtonElement | `id,semanticRole,text,width,maxWidth,height,fillMaxWidth,fontSize,fontWeight,textColor,background,shape,padding,leadingContent,trailingContent,actions`; text currently defaults empty. | Appearance/content/events only, no direct ReferenceResolver network calls or binding reads. Text expression is supported where backend declares it. |
| `ImageElementNode` / ImageElement | `id,semanticRole,url,fallbackUrl,width,height,maxWidth,maxHeight,fillMaxWidth,fillMaxHeight,contentScale,padding,leadingContent,trailingContent,actions`; url currently `orEmpty()`. | Required backend asset `url` + optional `fallbackUrl`, sizing/scale/layout/content/actions. No native glyph or static URL fallback. |
| `IconElementNode` / IconElement | `id,semanticRole,url,fallbackUrl,size,color,padding,leadingContent,trailingContent,actions`; url currently `orEmpty()`. | Standalone IconElement retained, URL required; content-oriented IconContent remains separate. |
| `IconContentNode` / IconContent | `id?,semanticRole?,url,fallbackUrl,size,color,spacing?,actions`; mapper parses content `id,properties,actions`, url defaults empty. | Required URL, optional fallback, content-owned spacing, optional content action; do not mislabel spacing as whole Element padding. |
| `DividerContentNode` / DividerContent | `id?,semanticRole?,orientation,width,height,thickness,color,spacing?,actions`; defaults horizontal, thickness 1.0. | Visual line parameters + content-owned spacing; no first-class DividerElement without backend contract. |
| `ScreenDocument` / DynamicScreen | `screenId,schemaVersion,targetApp,template,theme?`. | UI definition only. |
| `ScreenTheme` / DynamicTheme | `theme?,statusBar?,properties:Map<String,DynamicValue>` mapped as recursive concrete values. | Type approved presentation properties and status-bar policy; no new unrelated theme system. |
| `RequestOutcome` / DynamicResponse | Current `status,code,message,data:DynamicScreen?,traceId?,dataPayload:DynamicValue?,destination?`. | Split: API envelope stays data; ScreenDocument and RequestOutcome distinct Domain meanings. RequestOutcome data is action response data, destination extracted consistently. |
| `ActionRequest` / DynamicRequest | `endpoint:String,method:String=GET,authentication:String=NONE,validate:Boolean=false,body:DynamicValue?=null,responseMode:String=destination`. | Concrete request transport parameters separate from RequestAction declaration; typed method/auth/mode, validate in Store action flow rather than network. |
| `DynamicAction` | Current `type:String,payload:Map<String,DynamicValue>,destination?`. | Sealed/typed variants, distinct handlers preserving all capability contracts. |
| `DynamicValue` | `StringValue(String),NumberValue(Double),BooleanValue(Boolean),ObjectValue(Map),ArrayValue(List),NullValue`. | Resolved immutable values. Distinguish ValueExpression (references/literals/nested). Consider decimal fidelity; do not silently truncate huge numbers. |
| `DynamicBinding` | `key:String` | Typed binding address or String key validated by explicit syntax; avoid redundant wrapper unless semantics require it. |
| `DynamicValidation` | `required?,pattern?,message?` | Generic typed backend validation rules. |
| `DynamicInputPresentation` | `type,count?,spacing?,segmentWidth?,segmentHeight?,background?,border?,shape?,focusBorder?` | Input-specific presentation type, backend controls all dimensions/visuals. |
| `DynamicTextSpan` | `text:DynamicValue,color?,fontWeight?,underline:Boolean=false,action?` | Display text expression and optional clickable action; same ActionMapper as Element and Content. |
| `DynamicPadding` | `start,top,end,bottom:Double=0.0` | Reuse focused layout value object; each node/slot owns appropriate padding. |
| `DynamicSpacing` | `start,top,end,bottom:Double=0.0` | Keep semantically distinct content spacing; may share internal conversion code without merging meaning. |
| `DynamicArrangement` | Start,Center,End,Top,Bottom,SpaceBetween,SpacedBy(spacing) | Preserve orientation-valid arrangement semantics. |
| `DynamicBackground` | Solid(color), LinearGradient(angle=0.0, colors:List<DynamicGradientStop>=[]) | Backend-provided solid/gradient; retain gradient angle/stops; do not invent theme fallback. |
| `DynamicBorder` | width, color | Typed border; require valid width/color |
| `DynamicShape` | RoundedCorner(cornerRadius) | Typed shape; renderer translates to Compose, no custom draw unless necessary |

## C. Exact property-decoding ownership (new V2)

| JSON properties currently handled in DynamicValueMapper | V2 owner | Semantics |
|---|---|---|
| Any JSON primitive / array / object / null | `ValueMapper` | Resolved concrete value tree, no implicit reference evaluation |
| `$binding/$context/$state/$literal` typed nested JSON | `ValueExpressionMapper` | Parse reference syntax for display/action values |
| `padding.start/top/end/bottom/horizontal/vertical/all` | `VisualPropertyMapper` (layout function) | Precedence explicit per side > axis > all > 0 only when padding object present |
| `spacing.start/top/end/bottom` | `VisualPropertyMapper` content spacing function | Missing sides default 0 only for present spacing object |
| `verticalArrangement/horizontalArrangement` `type=spacedBy/center/end/bottom/spaceBetween` | visual/layout mapping | Validate orientation-compatible enum; no silent unknown type fallback |
| `horizontalAlignment/verticalAlignment` | visual/layout mapping | Typed orientation-appropriate alignment |
| `background.type=linearGradient`, `angle`, `colors[].color`, or solid `color` | visual mapping | Preserve color stops, validate malformed/no colors |
| `border.width/color` | visual mapping | Reject partial required config |
| `shape.type=roundedCorner,cornerRadius` | visual mapping | Unsupported shape explicit |
| `spans[].text,color,fontWeight,underline,action/onClick` | `TextSpanMapper` *internal helper or dedicated class only if complexity warrants* + single ActionMapper | No private second action mapper |
| `presentation.type,count,spacing,segmentWidth,segmentHeight,background,border,shape,focusBorder` | `InputPresentationMapper` helper owned by Element mapping | Validate count/dimensions, preserve backend visuals |
| `semanticRole` | local concrete Element/Content mapping helper | Preserve if contract used for semantics/accessibility |

## D. Action and event field parity

Existing `DynamicActionsDto` has five optional hooks: onClick, onLongClick, onValueChange, onFocus, onSubmit. `DynamicActionDto` has required type and optional payload/destination. `ElementMapper` maps every Element's actions; `ElementContentMapper` maps nested actions; text-span decoding currently uses a second private conversion. **One ActionMapper must decode every action location.** No other category mapper interprets action-specific payload fields.

Supported runtime action inventory from Pass 1: request, navigate, external_uri, present, dismiss, state; sequence is currently executor-supported and must be preserved. This pass did not re-audit concrete handler wire key semantics; Pass 4 must freeze them per variant. Request response mode/retain and recursive sequence behavior must have separate success/error/effect tests.

## E. Structural nesting / requiredness review

Current DTOs serialize `components/elements/sections/groups` with empty-list defaults. These wire-level defaults do not prove all empty collections are semantically valid. V2 model validators should reject impossible required hierarchy while permitting genuinely optional categories. **Explicit user business rule: one component in each current template**, independent of future component *types*. Accordingly, V2 Domain `FormTemplateNode` should favor `component: ComponentNode` (singular) if backend formally confirms exactly-one, even if the wire uses an array of length one. If future templates can own several components, model that when supported rather than assume it now. This is a design-correction candidate because the earlier V2 draft used `List<ComponentNode>` despite the user's cardinality statement; do not let legacy DTO list dictate Domain cardinality.

Direct Component elements plus sections, Section elements plus groups, Group elements, nested leading/trailing Element Content are supported. Wire stores direct/nested child categories in separate arrays; define visual ordering in Template/Component/Section renderers rather than assume arbitrary JSON map order.

Malformed Content arrays: current `mapNotNull { it as? JsonObject }` drops invalid items; V2 must reject instead, with path and type in *safe* error metadata. Unsupported `type` cannot disappear silently.

## F. Pass 1 and Pass 2 recheck / omissions

| ID | Earlier pass omission or refinement | Required plan adjustment |
|---|---|---|
| R01 | Pass 1 loosely described initial actions Request/Navigate with preservation note; current action capabilities include external_uri, present, dismiss, state, sequence. | Freeze full capability parity as mandatory implementation scope. Never compile a V2 runtime that permanently omits currently used actions. |
| R02 | Pass 1 spoke of each Entry's Store but did not specify same templateId repeated visits and selection ambiguity. | Separate Entry ID from templateId; popTo match policy and refresh/preserve-bindings contract. |
| R03 | Pass 1 should not imply existing six-category renderer registry needs rewrite. | Reuse proven KClass registration principles without forcing legacy naming. |
| R04 | Pass 2 correctly identified network Json duplication but final ownership must not introduce a network -> domain dependency. | Keep transport-neutral request method/auth DTO at network boundary or tiny shared-neutral types; Domain-facing action enums convert in data adapter. |
| R05 | Pass 2 planned common headers; current `RequestHandler` can accept arbitrary headers and optional bearer lambda. | Explicit header precedence/allowlist; do not permit backend JSON to supply `Authorization`, `Host`, or unsafe overrides. |
| R06 | Pass 2 documented maintenance/update states but did not fix startup decision priority or app exit handling. | Freeze maintenance vs required-update precedence and safe external app-store navigation before source wave. |
| R07 | Pass 2 proposed one Json; generic transport should not be hardwired to decode app envelope, while Data shares the same strict serializer. | Separate transport status/body vs operation-specific envelope mapping; exact instance reuse is optional if config fidelity guaranteed. |
| R08 | Pass 2 scope includes `SplashScreen` styling: this is a static native Splash, *not* a backend dynamic template; do not automatically remove its legitimate decorative Compose layout under the 'no custom draw in dynamic' rule. | Keep static splash distinct from server-driven visual restrictions. |
| R09 | Pass 2 lacked explicit test for HEAD/GET without JSON Content-Type and for metadata-only optional headers. | Add header/method-specific request configuration tests. |
| R10 | Existing bootstrap mapper has its **own** `DynamicDestinationDto` with nullable fields, while Dynamic data has another all-required destination DTO. | Unify wire destination mapping; do not duplicate null/default rules or lose optional method/auth. |
| R11 | Bootstrap DTO has flags/version and endpoint shape not detailed earlier. | Exact fields: config.version, maintenance.enabled/title/message; update.required/optional/minimumVersion/latestVersion/storeUrl; features.registrationEnabled/individualPartnerEnabled/organizationPartnerEnabled; startup.authenticated/destination, all parsed with current explicit invalid-response checks. |
| R12 | Source `BootstrapMapper` already validates application response `status` and `code` while DynamicResponseMapper does not itself validate either. | One intentional operation-appropriate app-envelope success policy; avoid duplicate HTTP+application response checking. |
| R13 | The earlier target tree proposed header/body/footer `components:[]`; user's current exactly-one template component rule needs explicit slot vs main template-cardinality distinction. | Single body Component by contract if formally confirmed; optional header/footer may independently define content structures as proposed schema, not copy empty-array conventions. |
| R14 | Pass 1 module proposal included `:core` but runtime metadata is already small and useful. | Keep core narrow, don't expand with generic utility files; don't move Dynamic-specific visual types into core. |
| R15 | Generic validation tests need negative/edge cases from *all supported* properties, including malformed regex and null semantics. | Domain-backed test fixtures span text, number-like input, segmented input, focus/submit events; no screen-specific validator. |
| R16 | Current data DTO Element skeleton permits validation and binding for non-input types, but ElementMapper ignores these for Text/Image/Button/Icon. | Decide per concrete backend contract; avoid automatic conversion into universal BindableElement just because DTO fields exist. |

## G. Proposed direct new-class contracts and connections

```kotlin
// domain/dynamic/model — no JsonElement/Compose/Ktor types
data class ScreenDocument(
    val screenId: String,
    val schemaVersion: String,
    val targetApp: String,
    val template: TemplateNode,
    val theme: ScreenTheme?,
)
data class ScreenDestination(
    val screenId: String,
    val templateId: String,
    val templateType: String,
    val endpoint: String,
    val method: HttpVerb,
    val authentication: AuthenticationMode,
)
sealed interface ElementNode { val id: String; val type: String }
sealed interface ContentNode { val type: String }

// data/dynamic/mapper — caller-known response interpretation
class ScreenDocumentMapper(
    private val templates: TemplateMapper,
    private val themes: ScreenThemeMapper,
) {
    fun map(dto: ScreenDocumentDto): ScreenDocument = ScreenDocument(
        screenId = dto.screenId,
        schemaVersion = dto.schemaVersion,
        targetApp = dto.targetApp,
        template = templates.map(dto.template),
        theme = dto.theme?.let(themes::map),
    )
}
```

Note: `sealed interface` is only valid if all direct concrete implementations remain in the same package/module according to Kotlin rules. With concrete models intentionally in subpackages, prefer ordinary `interface` unless this is explicitly redesigned. This is a **real compile-time defect in the above illustrative snippet**, not a reason to change the V2 category design. Also `HttpVerb/AuthenticationMode` must be located without causing network -> domain coupling (Pass 2 rule).

```text
ScreenDto -> ScreenDocumentMapper -> TemplateMapper
 -> ComponentMapper -> SectionMapper -> GroupMapper -> ElementMapper -> ContentMapper
ElementMapper and ContentMapper -> ActionMapper
ActionMapper -> ValueExpressionMapper + DestinationMapper
ActionResponseDto -> ActionResponseMapper -> RequestOutcome
BootstrapDestinationDto -> DestinationMapper -> ScreenDestination
```

For Kotlin construction, wiring is bottom-up, avoiding cyclic dependencies. An internal `VisualPropertyMapper` groups only actual reusable visual property decoding; unrelated reference/text/action conversions must not be bundled into it.

## H. Required tests generated from this audit

- DTO required/default behavior for every row in Section A.
- Every concrete node maps all fields in Section B including positive, null, malformed and boundary values.
- Gradient angle/color list and background type; padding side/axis/all priority; content spacing; orientation-valid alignment/arrangement.
- Required Image/Icon/IconContent URL; optional fallback chain primary→fallback→no asset; no hardcoded glyph fallback.
- Text span action uses same mapper and typed model as Element event and Content click.
- Input presentation count/dimensions, keyboard, binding, required regex/message; retain non-phone fixtures.
- Nested direct Elements, sections/groups, leading/trailing content and ordering.
- Template with exactly one component accepted, zero or two rejected **if backend cardinality contract confirmed**; header/footer slot migration tested separately.
- Action response vs Screen response decode; malformed/missing nested destination and application failure.
- Bootstrap's complete maintenance/update/features and destination fields map through shared DestinationMapper.
- Unsupported node type and unknown action produce typed errors with safe diagnostic path, no silent empty output.
- Domain models contain no Compose/Ktor/Json-specific types. Mapper/registry object creation has no circular dependency.
- Tests for existing event hooks and all action capability variants after Pass 4 contract audit.

## I. Plan status, defects and follow-up

**V2 structure remains unchanged**, except one substantive design correction candidate: Template's exactly-one component Domain cardinality, based on the owner's explicit statement; the agreed six-category model and renderer architecture do not change. Another source-independent compiler rule: ordinary interfaces, not sealed cross-package interfaces, for structurally split node model hierarchy.

No class or source was changed, no tests run. Pass 3 field inventories for DTOs, Domain declarations and production mappers are now complete for the listed files. Pass 3's final fixture-by-fixture semantic verification and exact caller migration graph must be completed with Pass 4/5 audits before PLAN FROZEN.
