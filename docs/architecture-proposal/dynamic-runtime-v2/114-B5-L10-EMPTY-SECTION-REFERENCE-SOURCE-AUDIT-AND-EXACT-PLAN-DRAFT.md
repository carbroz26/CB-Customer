# B5 / L-10 — ListSection emptySectionId Source Audit & Exact Implementation Plan

**Date:** 2026-10-22  
**State:** SOURCE-RECONCILED PLAN DRAFT — OWNER REVIEW PENDING — NOT FROZEN — NO IMPLEMENTATION AUTHORIZATION  
**Frontend:** carbroz26/CB-Partner, branch \`architecture/dynamic-runtime-v2-discussion-freeze\`  
**Reviewed HEAD:** \`edcfbfe821aa164c3fbf4936f6f42e914c9b90af\` (B4 compiled/JVM/Web owner verification; no B5 code changes)  
**Backend:** carbroz26/carwash-backend, \`feature/backend-migration\` — read-only; no backend change in B5  
**Authority:** V2 Document 112 §16.1, the owner-reviewed \`LOGIN-JSON-L01-L15-REVIEWED-FROZEN-IMPLEMENTATION-PLAN.md\` B5 table (attached planning reference), and the 21-Oct \`partner_login_country_picker_PRESENT_FINAL_FROZEN(1).json\`. Appendix A's \`emptyGroup\` is a historical superseded artifact.  
**Scope:** **B5 = L-10 contract, DTO/domain/mapping, target-JSON, fixtures and data mapping tests ONLY.** Phase-4 ListSection rendering, fallback display, filtering/catalog/\`$item\`/selection remain deferred. B4 and later batches are untouched.

## 1. Source-level reconciliation (observed, not presumed)

| Layer / file | Current source | Gap / B5 decision |
|---|---|---|
| \`data/.../model/section/ListSectionDto.kt\` | \`id\`, \`type\`, \`properties\`, \`groups\`, \`emptyGroup: JsonObject?\` | Remove obsolete \`emptyGroup\` member. Retain reference in \`properties\` only. |
| \`domain/.../section/ListSection.kt\` | \`emptyGroup: DynamicGroup?\`; items/source/filter/selection/itemSeparator/row groups already typed | Replace only with optional \`emptySectionId: String?\`. No additional Element or category. |
| \`data/.../mapper/SectionMapper.kt\` | Raw \`JsonObject\` dispatch, decode to \`ListSectionDto\`, \`dto.emptyGroup?.let(groups::map)\` | Explicitly reject old top-level \`emptyGroup\` **before** DTO decoding; map strict optional \`properties.emptySectionId\`. |
| \`data/.../mapper/ComponentMapper.kt\` | Maps sections independently with \`dto.sections.map(sections::map)\` | After mapping all siblings, validate each referenced ID within that same Component only. |
| \`data/.../mapper/DynamicResponseMapper.kt\` and \`PresentationMapper.kt\` | Both reach \`ComponentMapper\`, including Screen Template slots and active Presentation definitions | **KEEP**. Component-local validation naturally covers both contexts; no whole-screen registry needed. |
| \`feature/dynamic/.../sdui/component/StackComponent.kt\` | Renders all sibling sections in order | **KEEP**. No fallback-visibility behavior in B5. |
| \`feature/dynamic/.../registry/DynamicRendererRegistration.kt\` | Has StackSection renderer, no ListSection renderer | **KEEP**. Phase 4 owns ListSection rendering. |
| \`data/.../DynamicContractFixtures.kt\` | \`GENERIC_PHASE1_SCREEN_JSON\` includes embedded \`emptyGroup\` | Move existing empty Group into a newly declared sibling StackSection; point ListSection at its ID. |
| \`data/.../mapper/DynamicResponseMapperTest.kt\` | Current Phase-1 assertion still reads \`list.emptyGroup\`; test \`Json { ignoreUnknownKeys = true }\` | Replace assertion and add reference/invalid-contract tests. |
| Frozen 21-Oct Country Picker JSON | \`presentations[].components[id=picker_content].sections\` has \`picker_header\`, \`picker_search\`, \`country_list\`; \`country_list.emptyGroup.id = empty_content\` | Replace embedded group with referenced sibling StackSection inside the same \`picker_content.sections\`; preserve nested group/elements/styles unchanged. |

**Verified limitation:** Phase-1/3 list mapping does not equal ListSection UI support. Current \`DynamicRenderContext.renderSection\` requires a registered concrete renderer; ListSection has none. Rendering a target containing a ListSection still cannot be claimed working in B5. The referenced fallback must NOT also be rendered normally **when Phase-4 fallback rendering is implemented**. No premature "visibility" flags or special-case filtering in B5.

**Important decode risk:** Current mapping tests configure \`Json { ignoreUnknownKeys = true }\`. Merely deleting \`ListSectionDto.emptyGroup\` would silently discard legacy fallback data. The raw-node rejection is mandatory, independent of serializer configuration.

## 2. Frozen target shape, generic example (not runnable ListSection UI)

\`\`\`json
{
  "id": "collection",
  "type": "stack_component",
  "sections": [
    {
      "id": "results",
      "type": "list_section",
      "properties": {
        "source": {"type": "catalog", "key": "example"},
        "emptySectionId": "results_empty"
      },
      "groups": [
        {"id": "row", "type": "stack_group", "elements": []}
      ]
    },
    {
      "id": "results_empty",
      "type": "stack_section",
      "groups": [
        {"id": "empty_content", "type": "stack_group", "elements": []}
      ]
    }
  ]
}
\`\`\`

The example is a **mapping contract illustration** only. The backend owns identifiers and all visual tokens. No \`ListElement\`, \`emptyGroup\`, duplicate \`visibleWhen\`, \`emptyState\` field, or generic screen-specific logic.

**Reference invariants:** \`emptySectionId\` is optional, but when present it MUST be a JSON string, nonblank (prefer \`isNotBlank()\`, preserving the declared literal ID), and MUST match **exactly one** sibling \`StackSection.id\` inside the **same** \`StackComponent.sections\` list. It cannot refer to itself, a ListSection, other Component, another Presentation, Header/Footer Component, or a missing/ambiguous sibling. Do not silently choose the first duplicate or resolve by whole-screen ID lookup. Multiple ListSections sharing one target are not declared forbidden by this narrowly frozen plan; do not introduce exclusivity without an additional explicit decision.

**Target JSON conversion proposal:** Use a unique sibling Section ID such as \`country_list_empty_state\`; set \`country_list.properties.emptySectionId\` to this ID; remove the \`country_list.emptyGroup\` node; insert the sibling \`{"id":"country_list_empty_state","type":"stack_section","groups":[<the existing empty_content group unchanged>]}\` next to \`country_list\` inside \`picker_content.sections\`. Preserve the original \`empty_content\` group's own orientation/padding/spacing, all three child Elements, and all other payload properties exactly. This proposed reference ID is a local planning choice, not a runtime constant.

## 3. Exact file-by-file implementation manifest (ONLY after distinct approval)

1. **MODIFY** \`data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/model/section/ListSectionDto.kt\` — delete \`emptyGroup: JsonObject? = null\`; retain \`properties: JsonObject\`. No \`emptySectionId\` DTO top-level member.
2. **MODIFY** \`domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/section/ListSection.kt\` — replace \`emptyGroup\` with \`emptySectionId: String? = null\`; keep \`groups\`, \`source\`, \`items\`, \`filter\`, \`selection\`, \`separator\`, \`weight\` and Element/Section interfaces as-is.
3. **MODIFY** \`data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/mapper/SectionMapper.kt\` — for \`list_section\`, first reject presence of legacy top-level \`emptyGroup\` (including \`null\` or malformed object) before decoding. Also reject misplaced top-level \`emptySectionId\` rather than silently ignoring it; \`emptySectionId\` lives in \`properties\`. Parse optional \`properties.emptySectionId\` with \`JsonPrimitive.isString\` and \`isNotBlank()\`, failing if present with \`null\`, number, boolean, object, array or whitespace-only string. Map to Domain \`ListSection.emptySectionId\`. Retain all current ListSection source/row/filter/selection/separator validation.
4. **MODIFY** \`data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/mapper/ComponentMapper.kt\` — map \`dto.sections\` **once**, then for each mapped ListSection with a nonnull reference, find sibling entries with exactly the referenced ID; require exactly one match **and** that it is a \`StackSection\`, and reject self-reference. Return the existing \`StackComponent\` with that validated list. No screen-global resolver/helper class; ComponentMapper already owns siblings.
5. **MODIFY** \`data/src/commonTest/kotlin/com/carbroz/cbpartner/data/dynamic/DynamicContractFixtures.kt\` — migrate only \`GENERIC_PHASE1_SCREEN_JSON\` obsolete embedded fallback into a sibling \`stack_section\`. Preserve the existing \`empty\` StackGroup and its content; use a new unique sibling Section ID and matching \`properties.emptySectionId\`. Leave unrelated fixtures untouched.
6. **MODIFY** \`data/src/commonTest/kotlin/com/carbroz/cbpartner/data/dynamic/mapper/DynamicResponseMapperTest.kt\` — replace \`list.emptyGroup\` assertion with ID + sibling \`StackSection\` + nested original Group assertions, adjust expected section count, and add the strict cases below.
7. **MODIFY documentation, ONLY WITH B5 IMPLEMENT APPROVAL** \`docs/architecture-proposal/dynamic-runtime-v2/112-LOGIN-COUNTRY-PICKER-PHASE1-3-CONSOLIDATED-FREEZE-AND-AUDIT-HANDOFF.md\` — preserve historic Appendix A unchanged, add clear target-JSON revision reference consistent with §16.1.
8. **ADD canonical target contract JSON, ONLY WITH B5 IMPLEMENT APPROVAL** \`docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-current-target.json\` — migrate the exact provided 21-Oct artifact with only L-10 edits described above. Label **target contract, Phase-4 ListSection UI not executable yet**. Do not opportunistically change unrelated assets, Theme, CTA, screen IDs, requests, or country-list fields.

**KEEP / explicitly forbidden in B5:** \`feature/dynamic/.../sdui/component/StackComponent.kt\`, \`.../sdui/section/StackSection.kt\`, \`.../runtime/DynamicRenderContext.kt\`, \`.../registry/DynamicRendererRegistration.kt\`, Store, ActionExecutor, Validators, backend, navigation, networking, Template Shell, asset renderers, B4 Button loading and all Phase-4 ListSection logic. No \`ListElement\` or new business category.

## 4. Required mapping-only regression matrix

- **Valid:** absent \`emptySectionId\`; correct nonblank value; referenced sibling StackSection before/after ListSection; preserve existing Group/Element contents; independent references in multiple Components; Screen body and Presentation Component paths.
- **Invalid reference value:** explicit JSON null, "", whitespace-only, number, boolean, object, array; misplaced top-level \`emptySectionId\`; stale top-level \`emptyGroup\` including \`null\`; simultaneous \`emptyGroup\`+valid \`emptySectionId\`.
- **Invalid sibling graph:** missing target, target living only in a second Component, target living only in a different Presentation/Header/Footer, target is another ListSection, self-reference, ambiguous duplicate target StackSection IDs.
- **Legacy/compatibility:** \`Json { ignoreUnknownKeys = true }\` MUST still reject embedded \`emptyGroup\`; existing \`items xor source\`, row Group and catalog/filter/selection/separator mapping remain unchanged.
- **Current fixture:** \`GENERIC_PHASE1_SCREEN_JSON\` maps new ListSection ID and \`StackSection\` group content without requiring ListSection rendering.
- **Canonical target JSON:** parse/map contract, assert exact sibling graph, and explicitly note that production UI rendering of its ListSection is not supported until Phase 4. Do not use a screenshot or successful decode as proof of list/empty-state runtime behavior.

## 5. Verification sequence / acceptance gates

1. **Before modification:** check branch exactly \`architecture/dynamic-runtime-v2-discussion-freeze\`, re-fetch HEAD, read involved files, compare the frozen 21-Oct JSON against the proposed one-property + sibling transformation. Stop if source or contract moved unexpectedly.
2. **After approved B5 changes:** run \`.\gradlew.bat :data:jvmTest\`. Because other common modules depend on the Domain shape, also run \`:domain:jvmTest :feature:dynamic:jvmTest :app:jvmTest :feature:dynamic:compileAndroidMain :webApp:wasmJsBrowserDevelopmentWebpack\` as regression/compilation gates, without requiring ListSection UI to render.
3. Git diff against pre-B5 commit: exact reviewed files/JSON/doc only; \`git diff <pre-B5-HEAD> HEAD --check\`; no backend changes, new branches, Gradle cleanup or unrelated code refactor.
4. Owner reviews tests/output. Declare **B5 contract implementation VERIFIED** only after successful mapping and compilation tests; label actual empty fallback visual acceptance **PHASE 4 PENDING**, not B5 PASS.

## 6. Review risks / owner decision needed only for change of scope

- The source still lacks ListSection renderer, catalog/$item/filter/selection and conditional sibling consumption. **This is an intentional Phase-4 boundary**, not a reason to add UI code under B5.
- After B5 mapping, a raw fallback StackSection is still in \`StackComponent.sections\`; its Phase-4 renderer must reserve/exclude that referenced Section from ordinary sibling rendering, then render it **only when** its owning ListSection has no visible items. This statement is future acceptance, not an implemented B5 behavior.
- Any request to render the new target now, remove normal sibling from runtime, introduce \`visibleWhen\` on the fallback, permit cross-component references, or add full ListSection support would require a *new, separately frozen plan and authorization*.
- No code changes, fixture migrations, target JSON edits or Phase-4 behavior are authorized by this draft.

**Decision status:** B5 audit complete, exact file operations proposed against \`edcfbfe8\`; owner may review/freeze. B5 Kotlin/JSON implementation remains NOT STARTED.
