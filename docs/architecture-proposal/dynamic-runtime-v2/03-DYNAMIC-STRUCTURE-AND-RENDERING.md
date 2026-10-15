# 03 — Six-Category Dynamic Structure, Rendering, Slots and Theme

**State:** Discussion frozen. Header/footer JSON shown below is a proposed extension, NOT an existing backend contract.

## Model hierarchy
```text
ScreenDocument
 └─ TemplateNode
     ├─ ComponentNode (body)
     │   ├─ SectionNode -> GroupNode -> ElementNode
     │   └─ ElementNode (direct)
     ├─ optional header components
     └─ optional footer components
ElementNode -> optional leading / trailing ContentNode
```
Exactly six logical categories: Template, Component, Section, Group, Element, Content. **Confirmed cardinality:** exactly one Template per screen response; that Template owns one or more Components. Each Component may have direct Elements and/or Sections; each Section may have direct Elements and/or Groups; each Group has only Elements; each Element may have zero or more nested Content nodes. They are different structural responsibilities. A Component may directly own Elements and/or Sections; a Section may own Groups and/or Elements. Do not fabricate intermediate layers.

Concrete initially supported examples: FormTemplateNode, StackComponentNode, StackSectionNode, StackGroupNode, TextElementNode, InputElementNode, ButtonElementNode, ImageElementNode, IconContentNode, DividerContentNode. IconElementNode is an existing supported concrete Element and is retained. Backend migration will provide a required URL and optional fallbackUrl for all Icon Elements and Icon Content; icon-name-only assets are not a V2 contract. Future Grid/List/Carousel/Checkbox/Picker/Upload are examples, not present implementation requirements.

## Common vs unique
- Shared category interfaces contain only truly shared properties (identity and permitted children).
- Concrete models contain relevant category-specific alignment, arrangement, sizing, padding, appearance, bindings, validation and action declarations.
- No hundred-field nullable universal DynamicNode and no unnecessary per-property interfaces.
- A BindableElement contract is justified only if reused by real consumers (e.g. generic validator), not as decoration.
- Models describe domain/renderer-relevant meaning, not arbitrary `Map<String, Any?>` or framework-specific Compose objects.
- Node naming proposal `*Node` distinguishes Domain from `*Dto` transport and `*Renderer` Compose; final naming requires source reconciliation.

## DTO and mapping
```text
ScreenDocumentDto
 -> ScreenDocumentMapper
 -> TemplateMapper
 -> ComponentMapper
 -> SectionMapper
 -> GroupMapper
 -> ElementMapper
 -> ContentMapper
```
Each polymorphic category dispatches backend `type` explicitly to supported concrete DTO/model, with focused errors for unknown type and invalid structure. Preserve typed values/expressions including nested objects and arrays. Mapper dependencies run bottom-up; avoid cycles, reflection-based discovery and duplicated decoding.

## Renderer architecture
Six logical renderer registries keyed by supported concrete model class, possibly held by one small registry owner. Register explicitly, reject duplicate types, and centralize checked adapter/cast once. Each renderer renders only its concrete node and delegates permitted children back to the correct registry:
- FormTemplateRenderer: template layout and child components.
- StackComponent/Section/GroupRenderers: their own arrangement and direct/nested children.
- TextRenderer: literal/dynamic spans, typography, alignment and supported click callback.
- InputRenderer: controlled value, backend keyboard/presentation/max length, dispatched ValueChanged, validation display.
- ButtonRenderer: backend visuals/content, dispatch declared action only.
- Image/Icon/Divider renderers: backend-approved asset/decoration data only.

The renderer never performs HTTP, mutates navigation or reads Koin. A small RenderContext provides immutable values, error display and callbacks.

## Template shell with slots
Header, Body and Footer are **layout slots**, not new global node categories. A future template can support optional Header/Footer and required Body, with components in each slot. Each slot has independent padding/placement and can reuse existing renderers.

**Critical padding rule:** template's body padding applies only to Body, never automatically to Header/Footer. The background belongs to Template Shell; each slot owns its own backend padding. System safe area/window insets are a separate platform-owned concern, not secretly conflated with backend padding.

### Proposed additive future schema (not backend-approved)
Keep existing `template.components[]` and `template.properties.padding` in their fixed locations. Add only optional `header` and `footer` sibling fields. No `template.body` migration.
```json
{"template":{"id":"tpl_example","type":"form_template","properties":{"padding":{"start":24,"top":20,"end":24,"bottom":20}},"components":[{"id":"body_1","type":"stack_component","properties":{},"elements":[]}],"header":{"properties":{"padding":{"all":0}},"components":[]},"footer":{"properties":{"padding":{"all":0}},"components":[]}}}
```
Header/Footer slot structure/cardinality/scroll behavior remain an explicit contract-freeze gate. Body is the existing components array.

## Collapsible header and footer
- Template-specific supported header behavior may be fixed, collapsible, faded or swapped to category UI on scroll; body supplies scroll input; slot renderer owns presentation-only scroll/animation progress.
- Backend owns supported modes, expanded/collapsed content, heights, colors/gradient, transitions. Do not design arbitrary animation scripting.
- Pure presentation animation frames should not flood the MVI Store. Business visibility state belongs to Store/runtime state.
- Footer may contain future bottom-navigation component; its selected destination is derived from the single navigation authority, never an independent duplicated stack.
- Persistent app shell across multiple screens requires later explicit ownership decision; not automatically session-global.

## Theme/background
Apply backend-declared solid/gradient using existing Compose background/Brush modifiers where sufficient; remove unjustified custom draw code, but do not ban all drawing APIs. Exact gradient angles/stops require validated conversion. No invented colors/padding/typography/assets/OTP sizes. Asset contract: backend supplies primary URL and optional fallbackUrl; Coil caches eligible remote images and a generic bundled placeholder may be shown after remote/cache failure. No icon-name lookup. Generic placeholders must never masquerade as semantic action icons.

## Rendering failure policy
Validate screenId/templateId/templateType before rendering; mismatches/unsupported types are explicit errors rather than blank return. Unknown fields follow documented compatibility policy; unknown concrete node type must not silently disappear.
