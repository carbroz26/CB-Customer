# 163 — Dynamic Runtime V2 — Ordered Mixed Children Contract FREEZE

**Decision date:** 2026-10-26. **Scope:** source-reviewed JSON → DTO → Domain → Mapper → renderer traversal contract, independent of any one screen. **Baseline source:** `0b372e53deef8aa8f4e61c338cfad77e90cb0a0e` on `architecture/dynamic-runtime-v2-discussion-freeze`. **Status:** normative architecture/contract FREEZE; no Kotlin implementation or Gradle verification is claimed by this document. No Backend changes.

## 1. Owner decision — arbitrary authored child order

The previously implied rule that Component elements must precede Sections, or Section elements must precede Groups, is **not** a product hierarchy rule. The user expressly requires **any interleaving** of permitted direct children. Freeze:
- **Screen → exactly one Template → Components (1..n)**; Component instances follow authored order.
- **StackComponent → Element and Section**, ordered arbitrarily: Element→Section→Element; Section→Element→Section; Element→Section, Section→Element, etc.
- **StackSection → Element and Group**, ordered arbitrarily: Element→Group→Element (Partner Type Card), Group→Element→Group, etc.
- **StackGroup → Elements only**, original `elements[]` ordering, never nested Groups or Sections.
- Distinct `ListSection` and `FormFieldSection` remain concrete specialized Section types; this decision does not redefine their existing protocols.
- ElementContent leading/trailing stays inside the owning Element; do not create a new presentation/element/group category.

## 2. Exact proposed JSON wire extension (this ordered-children format is frozen)

For `stack_component` new authoring:
```json
{
  "id": "content", "type": "stack_component",
  "properties": {"orientation": "vertical"},
  "children": [
    {"id": "intro", "type": "text", "properties": {"text": "Intro"}},
    {"id": "cards", "type": "stack_section", "properties": {"orientation": "horizontal"}},
    {"id": "outro", "type": "text", "properties": {"text": "Outro"}}
  ]
}
```
For `stack_section` new authoring:
```json
{
  "id": "category_card", "type": "stack_section",
  "properties": {"orientation": "horizontal"},
  "children": [
    {"id": "avatar", "type": "icon_badge", "properties": {"size": 64, "imageSize": 64,
      "source": {"type": "resource", "value": "images/partner_type_individual_01"}}},
    {"id": "copy", "type": "stack_group", "properties": {"orientation": "vertical"},
      "elements": [
        {"id": "title", "type": "text", "properties": {"text": "Individual Partner"}},
        {"id": "description", "type": "text", "properties": {"text": "Work independently"}}
      ]},
    {"id": "arrow", "type": "icon_badge", "properties": {"size": 40, "imageSize": 24,
      "source": {"type": "resource", "value": "icons/partner_type_arrow_right_01"}}}
  ]
}
```
The example resource IDs are **proposed new bundled assets**, not claimed to be in the current asset registry. Only child shape/order and categories are frozen by this document.

**Strict mode**: either use new `children[]` OR legacy `elements[]` + `sections[]` (Component) / legacy `elements[]` + `groups[]` (Section). Never accept both, **even if one legacy array is empty**. DTO mapping must fail explicitly for invalid child type, duplicate ID in one parent, unsupported nesting, or competing encodings. `children:[]` is legal where empty legacy children were legal. Group stays `elements[]`; do not add `Group.children`.

**Backward compatibility**: old Login v19, OTP v3, six Dialog fixtures, Country Picker, Resend Toast, and all legacy JSON are left unchanged. When decoding a legacy node, preserve the existing renderer's Elements-then-Sections or Elements-then-Groups behavior; true interleaving can only be expressed with `children[]`. Avoid claiming arbitrary legacy ordering that the old JSON cannot encode.

## 3. Typed DTO/Domain/Mapper and renderer responsibilities

| Existing layer | Exact planned change | Boundary |
|---|---|---|
| `StackComponentDto` | Optional `children: List<JsonObject>?`; keep legacy `elements`/`sections` | Raw wire structure only, no layout behavior |
| `StackSectionDto` | Optional `children: List<JsonObject>?`; keep legacy `elements`/`groups` | Raw wire structure only |
| Domain `StackComponent` | Optional `orderedChildren: List<DynamicComponentChild>?` | Typed child order, no generic `Any` |
| Domain `StackSection` | Optional `orderedChildren: List<DynamicSectionChild>?` | Typed child order, no UI-specific naming |
| Domain child wrappers | `DynamicComponentChild.Element(DynamicElement)`, `.Section(DynamicSection)`; `DynamicSectionChild.Element(DynamicElement)`, `.Group(DynamicGroup)` | Kotlin ordering wrappers **only**, not new Dynamic product types or registries |
| `ComponentMapper` / `SectionMapper` | Explicit `type` dispatch per permitted child; preserve authored position; reject mixed wire schemes | Reuse existing `ElementMapper` / `SectionMapper` / `GroupMapper` |
| `StackComponent` / `StackSection` renderers | Iterate typed ordered children, render by existing registry, including correct `weight` / fallback handling | No screen-specific rendering, no changed styling defaults |
| Traversal consumers | Derived `elements` / `sections` / `groups` **projections** from ordered children where present, so validation, button lookup, ListSection fallback, action dispatch remain correct | One authoritative source list, not two diverging model trees |

Exact domain constructor/backward-compatibility approach: keep existing constructor properties `elements`, `sections`, `groups` for source callers; when `orderedChildren != null`, its projections must match these compatibility views (or compute views from it). Prohibit divergent duplicates. Legacy source callers still work. **No reflection, untyped children, generic property bag, fallback hardcoded text/size/color, new Store, or duplicated renderer registry.**

Recommended pure tests: map Component `Element,Section,Element`; Section `Element,Group,Element`; reject Section directly inside Section, Group directly inside Component, or Group inside Group; reject mixed `children` and legacy arrays; accept legacy Login/OTP/Dialog fixtures byte-for-byte; ensure render traversal sees exact sequence; validate/fallback does not lose ordered-child entries.

## 4. Partner Type Selection structural decision

Reuse `FormTemplate` background image and two-component shell from Login/OTP:
- Branding `StackComponent` (hero): back/brand, title, subtitle.
- Bottom white `StackComponent(orientation=vertical)`: **exactly four `StackSection`** siblings, in order: Individual, Organization, Partner Under Organization, Help.
- Each category `StackSection(orientation=horizontal)` owns its **whole-card surface**, `children = [IconBadgeElement avatar, StackGroup(orientation=vertical, two TextElements), IconBadgeElement arrow]`.
- Help `StackSection` owns only the text/help icon; its TextElement uses existing `present` action to open one `BottomSheetPresentation`.
- Help Bottom Sheet reuses the same card-row structure without arrow; `Got it` uses existing `dismiss`. No extra Help screen.
- **Card selection action/pressed semantics, StackSection minHeight, StackGroup weight, new packaged imagery, endpoint and partner type IDs are separate product/source amendments, not silently implemented by ordered-children freeze.**

## 5. Loading and failures — user correction

**Only generic shimmer/skeleton loading** will be researched for the reference's three placeholder cards. Do **NOT** implement the reference's red error banner. Existing app-wide generic `DialogPresentation` error flow from Splash/Login/OTP is the only approved error UI for this screen.

Generic shimmer must have an explicit state/trigger and authored geometry/color/timing; `ButtonElement.loading` is not the same as three content-card placeholders. On a first uncached screen request, the Backend screen JSON does not yet exist: do not pretend a Backend-authored loading skeleton can render before its own response. Choose a legitimate lifecycle source (known/retained screen, independently loaded shell or approved generic local fallback) before freezing shimmer transport. This decision is **OPEN**, not an excuse to create a custom PartnerTypeSkeleton composable.

## 6. Stage gates

**FREEZE NOW**: child hierarchy and mixed ordered `children[]` wire/typed boundary above; keep existing error Dialogs.
**REVIEW CANDIDATE, NOT IMPLEMENTATION FREEZE**: whole clickable `StackSection`, pressed visuals, group remaining-width, card min height, shimmer lifecycle, full Partner Type JSON. Do not implement until those and Backend identity/endpoint/destination are reconciled.
**Backend truth (read-only audit)**: current `CB-Backend/development/domains/partner/domain/PartnerType.ts` exposes only `INDIVIDUAL` and `ORGANIZATION`, and existing `partner.partner.routes.ts` exposes authenticated registration routes requiring businessName, **not** a verified post-OTP three-option selection endpoint. Third `Partner Under Organization` is therefore a pending business/schema decision. Do not misrepresent guessed endpoint/enum as live. Preserve Backend untouched.

Owner process: Discuss → Research → Decide → Plan → Freeze → Implement → Test → Review → Freeze. Current commit is documentation-only.
