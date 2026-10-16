# 47 — W3 Final Source and Frozen Contract Reconciliation

**Status:** W3-A/W3-B JVM + Web compile verified by owner at commit `e85247c1`; source-level final review complete; W5 integration and broader V2 work remain. Branch `architecture/dynamic-runtime-v2-discussion-freeze`. Documentation only; no Kotlin changes in this reconciliation.

## Source authority and evidence
Read current GitHub files for DTO/Domain/mapper hierarchy, DynamicRepositoryImpl, DynamicResponseMapper, TemplateMapper, ElementMapper, ElementContentMapper, DynamicValueMapper, FormTemplate Compose renderer, DynamicRendererRegistration, DynamicStore. Compared frozen contract 20 and W3 records 44–46. User ran focused DynamicResponseMapperTest and full `:domain:jvmTest :data:jvmTest :feature:dynamic:jvmTest :webApp:wasmJsBrowserDevelopmentWebpack` successfully; `git diff --check` produced no errors. No Android/iOS runtime acceptance implied.

## Category-by-category contract
| Category | DTO -> mapper -> Domain | Downstream caller | Disposition |
|---|---|---|---|
| Template | FormTemplateDto + nullable TemplateSlotDto -> TemplateMapper -> FormTemplate + shared DynamicTemplate/TemplateSlot | FormTemplate Compose renderer via registry | W3 mapped; W5 shell not implemented |
| Component | StackComponentDto -> ComponentMapper -> StackComponent | DynamicRenderContext/registry | KEEP |
| Section | StackSectionDto -> SectionMapper -> StackSection | DynamicRenderContext/registry | KEEP |
| Group | StackGroupDto -> GroupMapper -> StackGroup | DynamicRenderContext/registry | KEEP |
| Element | Five concrete DTOs -> ElementMapper -> five Domain elements | element registry | KEEP; image URL now required; legacy icon remains tolerant |
| Content | leading/trailing JsonObjects -> ElementContentMapper -> IconContent/DividerContent | content registry | KEEP; strict malformed arrays/entries; spacing preserved |

## Composition, duplicate logic and SRP
- DynamicResponseMapper constructs a **single** typed mapping path: ElementMapper -> GroupMapper -> SectionMapper -> ComponentMapper -> TemplateMapper. No second DTO/domain tree or mapper factory in reviewed files.
- DynamicRepositoryImpl performs request adaptation, transport delegation and decoding through DynamicResponseMapper; it preserves cancellation if mapper/decoder throws. Domain carries no Compose imports.
- Repeated orientation/arrangement/padding expressions in Component/Section/Group are **different concrete category mappings**; not evidence for a generic universal container or redundant class. One shared DynamicValueMapper converts scalar/layout tokens for all mappers.
- Current ElementMapper owns five dispatch variants plus assembly of action/content mapping. This is category-level responsibility with type-specific fields, not by itself a reason to split into five forwarding mappers.
- TemplateSlot and TemplateSlotDto are common structural slot types, **not** a seventh dynamic category. Shared contract can be reused by future template types without adding fictional templates today.
- Domain naming vs Compose renderer naming overlaps by design but packages are distinct. Registry maps concrete Domain classes to renderers only once.

## Implemented/frozen alignment
1. Body stays `template.components[]`; optional `template.header/footer` contain own `properties.padding` and components list. Absent slot null, empty slot allowed; legacy payload stays mapped. **W3 mapping complete.**
2. Required ImageElement URL: absent/blank mapping now fails; legacy IconElement/IconContent absent URL remains supported. **W3 verified.**
3. Malformed declared leading/trailing arrays/entries: explicitly rejected rather than dropped. Valid Icon/Divider and spacing remain mapped. **W3 verified.**
4. Unknown concrete type produces explicit failure; exception class currently differs between categories (`error` vs `IllegalArgumentException`). **Existing behavior; not blocker for W3, track for error-boundary W8 review.**
5. Generic screen/data envelope: DynamicResponseMapper classifies a screen by presence of `screenId/schemaVersion/targetApp/template`. If malformed screen omits one of those fields it is treated as non-screen payload. **Potential ambiguity; review alongside W6/W8 operation-specific response validation, not silent speculative new DTOs in W3.**
6. `DynamicValueMapper` still uses tolerant defaults for some optional layout entries; current W3 validation changes are specifically Image URL and leading/trailing list shape, **not** universal strict JSON validation.
7. Former W3 audit claim `missing image URL` and malformed content were unaddressed is superseded by W3-B tests and code at `e85247c1`.

## Downstream handoff / outstanding integration
- **W5 definite:** Current `feature/dynamic/.../sdui/template/FormTemplate.kt` renders only `template.components` in Row/Column; `template.header` and `template.footer` are mapped but ignored. Implement one shared Header -> Body -> Footer `TemplateShell` per doc20 §2. Ensure Body-only legacy behavior, independent padding, empty slot, appropriate scroll and insets. Add runtime/UI tests; do not classify W3 mapping success as rendering success.
- **W5:** Gradient stop-aware brush and UI asset placeholder follow doc20; not a W3 mapper rewrite.
- **W6/W8:** Typed error sanitization/boundary when an unsupported category or malformed screen is received, and screen identity classification; a mapper throwing is correct for invalid JSON but how to show an in-place recoverable error is caller responsibility.
- **Backend migration:** Future optional header/footer additive JSON and URL-migrated icons must follow frozen shape. Frontend is not blocked on backend rollout.
- **W7:** Android/iOS runtime unverified on current Windows setup, Web compilation verified only for W3 changes.

## Exit decision
**W3 DTO/Domain/mapper implementation verified against current test suite and frozen scoped contract. No additional Kotlin refactor justified in the W3 exit wave.** Remaining issues are explicitly W5/W6/W7/W8 handoffs; do not add speculative compatibility layers, abstract factories, new modules or unused template classes. All new W3 work requires concrete regression evidence or owner-approved schema change.
