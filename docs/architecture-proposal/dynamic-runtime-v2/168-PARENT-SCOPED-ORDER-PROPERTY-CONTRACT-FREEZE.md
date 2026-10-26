# 168 — Dynamic Runtime V2 — Parent-Scoped Order Property Contract FREEZE

> **SUPERSEDED — 2026-10-27 (owner decision):** The `properties.order` mixed-sibling rendering extension, including its proposed mapper/renderer changes and tests, has been **withdrawn before implementation**. Do NOT implement `order`, `childOrder`, or `children[]` for the current Partner Type Selection. See [Document 169](169-PARTNER-TYPE-ORIGINAL-ARRAYS-V3-REVIEW.md) and its new [V3 review JSON](partner-type-selection-v3-original-arrays-REVIEW-CANDIDATE.json), which use only the existing `elements[]` / `sections[]` / `groups[]` arrays and their existing source-defined render order. This file is retained as historical documentation, **not an active freeze or implementation plan**. V3 JSON is a review candidate requiring owner approval.

**Decision date:** 2026-10-27
**Status:** SUPERSEDED / HISTORICAL; NEVER IMPLEMENTED.
**Frontend branch:** `architecture/dynamic-runtime-v2-discussion-freeze`
**Source checked:** remote branch HEAD `1e76cc24eaf836d39f3596e350b424e95bb4d7d0`.
**Scope:** generic mixed sibling rendering order only. No Partner Type JSON conversion, Kotlin implementation, Backend migration, assets, tests or device verification in this decision.

## 1. Supersession and authority

This decision **supersedes the `children[]` wire encoding and related typed mapping plan in document 163**. Retain document 163 only as historical reasoning about the six-category hierarchy and the need for arbitrary mixed order. Its `children[]` prescription is **NOT** the current contract. Document 164 and `partner-type-selection-ordered-v1-CANDIDATE.json` remain historical **review candidates** and are not suitable for rendering until converted separately. Do not treat conflicting older text as implementation authorization.

## 2. Unchanged hierarchy and JSON containers

- Screen -> exactly one Template -> Component instances; template component ordering remains the order of `components[]`.
- `StackComponent` retains exactly the existing direct `elements[]` and `sections[]` collections. Their items may interleave visually through `properties.order`.
- `StackSection` retains exactly the existing direct `elements[]` and `groups[]` collections. Their items may interleave visually through `properties.order`.
- `StackGroup` retains only `elements[]` in existing array order. Do not add `groups[]`, `sections[]` or `children[]` to Group.
- `FormFieldSection` and `ListSection` retain their internal specialized child protocols; as direct children of a StackComponent, each may carry `properties.order`.
- `ElementContent` leading/trailing ordering remains unchanged. No new seventh product category or `children[]` field.
- JSON objects must not repeat a key (`elements` cannot appear twice); multiple Elements belong in one `elements[]` array.

## 3. Exact wire rule: optional `properties.order`

The `order` attribute is carried on **each direct child node**, not on the parent and not by renaming keys:

```json
{
  "id": "example_component",
  "type": "stack_component",
  "properties": { "orientation": "vertical" },
  "elements": [
    { "id": "title", "type": "text", "properties": { "order": 1, "text": "Title" } },
    { "id": "subtitle", "type": "text", "properties": { "order": 3, "text": "Subtitle" } }
  ],
  "sections": [
    { "id": "category", "type": "stack_section",
      "properties": { "order": 2, "orientation": "horizontal" },
      "elements": [], "groups": [] }
  ]
}
```

Expected render order: `title` (Element) -> `category` (Section) -> `subtitle` (Element).

Inside a StackSection, the same rule applies across its **direct** `elements[]` and `groups[]`, e.g. avatar Element order 1, text StackGroup order 2, arrow Element order 3. Each parent has its own independent order scope; nested numbering does not interact with ancestor or sibling parents.

## 4. Determinism and validation

1. If **none** of a parent’s eligible direct children carries `properties.order`, **preserve existing legacy render order exactly**: Component renders Elements then Sections; StackSection renders Elements then Groups. Preserve legacy array order within each collection.
2. If **any** eligible direct child has `properties.order`, **all** eligible direct children of that same parent must have an explicit order. Mixed ordered/unordered siblings fail mapping; do not silently append missing items.
3. `order` must be a literal JSON **positive integer** (`1`, `2`, ...); not a string, fractional, zero, negative, boolean or null. Integers must fit the supported implementation integer range. No automatic numbering.
4. Order numbers must be **unique within the same parent**, including across its two permitted child collections. Duplicate order values fail mapping; no tie-breaker.
5. Numbering **need not be contiguous**: `1, 3, 10` is valid and sorts ascending. The recommended authoring style is `1, 2, 3` for readability.
6. In the ordered mode, direct sibling IDs must be nonblank and unique across both permitted collections. Do not make two copies of a child to emulate interleaving.
7. A `children[]` key is not part of the contract and must be explicitly rejected at the StackComponent/StackSection mapping boundary, rather than silently ignored.
8. An ordering attribute does not change child ownership, selection, state, actions, weight, visibility, validation, accessibility or presentation semantics. The same typed domain instances are used for rendering and traversal.

## 5. Minimal source implementation boundary (future authorization)

- `StackComponentDto` / `StackSectionDto`: keep current `elements[]` / `sections[]` / `groups[]` JSON DTO structure. Child `properties` are already `JsonObject`; add no general-purpose DTO bag or child type.
- `ComponentMapper`: read `properties.order` from raw direct Element/Section nodes, validate, map through existing `ElementMapper` / `SectionMapper`, and produce an ordered **typed** view without losing original collections.
- `SectionMapper`: same for direct Element/Group, using existing mappers.
- Domain `StackComponent` / `StackSection`: optional typed ordered view/wrappers of the **same** child instances, preserving existing `elements` / `sections` / `groups` projections and constructor/backward compatibility. No `Any`, reflection, new Dynamic category, or duplicate divergent tree.
- Existing `StackComponent` / `StackSection` Compose renderers iterate typed ordered children when present, otherwise the old loops. Existing registry and weight behavior must be preserved.
- `ListSection.emptySectionId` referenced StackSection must remain a **fallback only**, never an ordinary visible sibling even if it has an order number. Keep validation and button/action lookup on preserved collections; no new Store, navigation or networking layer.
- Extend Backend strict schema/authoring interfaces only when Backend implementation is separately authorized. Current Frontend JSON preview and eventual Backend output must share one canonical contract.

## 6. Required tests when implementation is separately approved

- Component: Element(1), Section(2), Element(3); Section: Element(1), Group(2), Element(3).
- Order independence from physical array order; independent scopes in nested parents; non-contiguous numbers accepted.
- Reject duplicate/missing/invalid order, duplicate sibling IDs in ordered mode, unsupported nesting, and unexpected `children[]`.
- Legacy Login v19, OTP v3, Country Picker, six dialogs, Toast and existing fixtures unchanged in source and display behavior.
- `ListSection.emptySectionId` fallback is not double-rendered; form validation, button lookup, generic actions and traversals remain correct.
- Unit tests, builds and Samsung A35 / Web preview only in subsequent implementation/verification phases.

## 7. Out of scope / next checkpoint

- **Next documentation-only step:** convert the old Partner Type `children[]` candidate to the existing `elements[]` / `sections[]` / `groups[]` arrays with explicit `properties.order`. Preserve its visual properties, category-specific `navigate` destinations, Help `present`, and X/Got it `dismiss`. Proposed destinations/endpoints stay provisional until relevant Backend screens exist.
- Do not implement Kotlin yet. Section actions/pressed style/minHeight, Group weight, image assets, visual parity and Backend screens are **separate implementation/approval gates**, not automatically frozen by this document.
- Shimmer is **deferred** by the owner. Do not introduce any skeleton contract or red error banner. Continue using the existing generic error dialog design.
- Preserve approved Splash, Login v19, OTP v3, Country Picker, Dialogs and Toast without unrelated changes.

**Decision principle:** Preserve existing JSON hierarchy and collections; `properties.order` controls mixed sibling render order only.
