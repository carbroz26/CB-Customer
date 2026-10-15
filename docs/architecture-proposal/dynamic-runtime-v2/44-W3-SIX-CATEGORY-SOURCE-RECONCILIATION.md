# 44 — W3 Six-Category DTO/Domain/Mapper Source Reconciliation

**Status:** INITIAL SOURCE AUDIT; NO Kotlin implementation authorized by this record. Branch `architecture/dynamic-runtime-v2-discussion-freeze`. Grounded in actual source read 2026-10-16 and frozen documents 17/19/20/23, backend migration 34, canonical 12. Build evidence: owner reported W2 JVM + Web build success at `8b8e31e0`; NO W3 build claimed.

## Six categories — actual chain
1. **Template:** `FormTemplateDto` (`id,type,properties,components:List<JsonObject>`) → `TemplateMapper` type-dispatch `form_template` → `FormTemplate` implementing `DynamicTemplate` with `components: List<DynamicComponent>` → registry/template renderer.
2. **Component:** `StackComponentDto` (`elements[],sections[]`) → `ComponentMapper` type-dispatch `stack_component` → `StackComponent` → component registry.
3. **Section:** `StackSectionDto` (`elements[],groups[]`) → `SectionMapper` `stack_section` → `StackSection` → section registry.
4. **Group:** `StackGroupDto` (`elements[]`) → `GroupMapper` `stack_group` → `StackGroup` → group registry.
5. **Element:** five concrete DTOs (`TextElementDto`, `ImageElementDto`, `InputElementDto`, `ButtonElementDto`, `IconElementDto`) → one `ElementMapper` five-branch dispatch → five concrete Domain elements → element registry.
6. **Content:** nested leading/trailing `JsonObject` (no current dedicated DTO per type) → `ElementContentMapper` two-branch dispatch (`icon`, `divider`) → `IconContent` and `DividerContent` implementing `ElementContent` with `DynamicSpacing` → content registry.

**Composition:** `DynamicResponseMapper` owns mapper assembly exactly once: ElementMapper → GroupMapper → SectionMapper → ComponentMapper → TemplateMapper. No duplicate hierarchical pipeline detected in reviewed sources. The six logical categories are distinct without gratuitous mapper interface/factory per concrete type.

## Confirmed contract behavior
- Screen `data.template.components[]` remains the Body; existing tests cover multiple components, plus nested elements/sections/groups, actions, bindings, validation and leading/trailing icon/divider.
- Explicit unsupported concrete type failure; unknown Element throws `IllegalStateException`, other categories `IllegalArgumentException`. This is inconsistency, not evidence requiring a new error framework.
- Current icons parse URL/fallbackUrl and tolerate pre-migration absent icon URL as `""`; no glyph registry in mapper. Contrast ImageElement's `url.orEmpty()`, which may silently allow missing required image URL; frozen 20 says missing required image URL is an error unless optional.
- Content `spacing` is mapped, independent of node padding.
- `DynamicResponseMapper` distinguishes screen payload via four screen fields, preserves non-screen payload and optional destination.
- The source tests assert examples of nested hierarchy and typed properties. Do not pretend tests cover unimplemented future Header/Footer.

## Gaps against frozen V2 / migration (ranked)
**P0 — optional Template Header/Footer absent.** Frozen 20 §2 defines optional `template.header` and `template.footer` slot with `properties.padding` and `components[]`, empty slot valid and Body remains `components[]`. Current `FormTemplateDto`, `FormTemplate`, `TemplateMapper` have *no* header/footer fields. On JSON with new optional fields, unknown keys might be discarded (`ignoreUnknownKeys`) and runtime silently loses UI content. W3 must add the exact slot DTO/Domain mapping, without a `body` wrapper or extra category, and add pre-/post-migration mapping tests.

**P1 — missing required Image URL semantics.** `ElementMapper.mapImage` maps missing `url` to empty string, same as intentionally tolerated legacy Icon URL. Frozen 20 distinguishes these cases. Verify real fixture/optional semantics; make absent required Image URL explicit map failure while preserving old icon behavior.

**P1 — Content DTO validation and unsupported/malformed nodes.** `ElementContentMapper.mapList` silently drops non-JsonObject entries with `mapNotNull`, which can conceal malformed payload. Content `id` optional, type required, props default empty, actions decoded separately; this is a genuine validation policy question. Preserve valid empty content lists, reject malformed entries rather than silently remove after tests, unless wire requires tolerance.

**P1 — complete caller/renderer acceptance:** existing `DynamicRenderer` uses registry template render; `DynamicRenderContext` routes all five child categories by registry. W5 header/body/footer TemplateShell and actual renderer placement aren't implemented in reviewed code. W3 must add data/domain contract without prematurely rewriting W5 rendering; temporary acceptance tests must expose mapped slots.

**P2 — optional presentational parity:** frontend `DynamicRenderer.screenTheme` currently reads color stops but creates Brush.linearGradient(colors) ignoring explicit stop positions; this is a W5 render issue, not W3 mapper rewriting. `TextElement` spans and segmented OTP mappings are already tested; preserve.

**P2 — uniform diagnostics:** unsupported template/component/section/group vs element/content exceptions differ. Source-level error classification and message sanitization can be normalized narrowly after verifying repository caller behavior; no ErrorManager.

**Do not mistake** repeated `orientation`, arrangement and padding reads across four distinct concrete categories for evidence that a generic layout hierarchy is necessary; the categories have separate allowed fields (e.g., Group background/shape). Prefer clear concrete mappers rather than a shared mutable universal-properties bag.

## Exact first vertical slice proposal (W3-A, not yet implemented)
1. Add a **single** small `TemplateSlotDto` (or `SlotDto`) with optional properties and `components: List<JsonObject>` that mirrors frozen JSON. Keep Body existing `FormTemplateDto.components`. Add optional `header` and `footer` fields.
2. Add lightweight Domain `TemplateSlot` type with `padding: DynamicPadding?` and `components: List<DynamicComponent>`; `FormTemplate.header/footer: TemplateSlot?` only. Empty slot maps to empty component list; no renderer/duplicate registry yet.
3. Reuse existing ComponentMapper and DynamicValueMapper within TemplateMapper for slot mapping; do not instantiate a second mapper tree or new slot factory.
4. Add focused Data tests: no slots (canonical Login/OTP), header only, footer only, both independent paddings, empty slot, multiple typed components, unsupported concrete child and missing required field.
5. Update doc34 migration register on actual implementation; later W5 uses TemplateShell to render slots. No backend deployment gating for additive optional fields.
6. Run `:domain:jvmTest :data:jvmTest :feature:dynamic:jvmTest :webApp:wasmJsBrowserDevelopmentWebpack` + diff check; Android/iOS runtime deferred.

## Follow-on W3-B
After W3-A tests pass: tighten Image URL required semantics, malformed content entries and content type dispatch with fixture-specific evidence; retain legacy name-only Icon unavailable state and `spacing`; no speculative extras.

## Scope gate
This audit does not authorize redesign of renderer registry, dynamic actions, navigation, gradient/UI layout, HTTP clients, repository envelope, or adding a second contract tree. Confirm alignment before implementing any revised wire fields not frozen in 20.
