# 19 — Backend-Migration-Independent V2 Contract (Owner Decision)

**State: OWNER DECISIONS CONFIRMED; IMPLEMENTATION BLUEPRINT FREEZE CANDIDATE (not code authorization).**
**Supersedes conflicting "blocked until backend migration" statements in 17/18.** No source edits or tests in this commit.

## Governing principle
Implement the **final typed frontend contract once** and permit old/current and upgraded backend documents to coexist by means of ordinary optional fields, not temporary adapters, fake response data, environment flags, duplicate models, or an icon-name compatibility registry. Screens must keep functioning before backend migration; sending valid upgraded JSON must exercise existing code immediately. The frontend cannot guarantee zero changes if backend later chooses an unrelated, unapproved JSON shape. Therefore **freeze the additive schema now**, even when backend does not yet emit it.

## Fixed shape + optional slots (proposed exact additive schema for coordinated backend adoption)
Preserve `data.template = {id,type,properties,components:[...]}` exactly. Define optional siblings inside template:

```json
{
  "template": {
    "id": "tpl_example",
    "type": "form_template",
    "properties": {
      "padding": {"start":24,"top":20,"end":24,"bottom":20}
    },
    "components": [{"id":"body","type":"stack_component","properties":{},"elements":[]}],
    "header": {
      "properties": {"padding":{"start":0,"top":0,"end":0,"bottom":0}},
      "components": [{"id":"header_content","type":"stack_component","properties":{},"elements":[]}]
    },
    "footer": {
      "properties": {"padding":{"start":0,"top":0,"end":0,"bottom":0}},
      "components": [{"id":"footer_content","type":"stack_component","properties":{},"elements":[]}]
    }
  }
}
```

**Interpretation:** `header` and `footer` absent means do not show those slots; body still renders from existing `template.components[]`. Slot structure is a suggested schema to coordinate with backend; once accepted, additional backend properties may only be used if already part of agreed typed definitions. No `body` wrapper, no mandatory slot on every screen, and no new Dynamic category. Header/footer renderer uses the same Component registry. Template properties.padding applies only to body; slot-specific padding separately. Initial shell can support simple static slots without speculative collapse scripting; future collapse requires a separately defined schema, not guessed controls.

### Data/Domain structural contract
```kotlin
data class TemplateSlots(
    val header: Slot? = null,
    val footer: Slot? = null,
)
data class Slot(
    val components: List<ComponentNode>,
    val padding: DynamicPadding? = null,
)
```
A concrete TemplateNode retains mandatory `components: List<ComponentNode>` (1+). Slot can be absent or contain empty children if contract allows; no artificial placeholder screen. If header/footer absent render exactly current layout/body.

## Asset policy with old and new backend
- Backend migration will replace name-only icons with mandatory URL-based icon data and optional fallbackUrl.
- Production **renderer** already uses final URL contract and one `RemoteAssetImage` loader. It never converts icon `name` to a local glyph and never hardcodes special names.
- During migration, old name-only Content can be *read as an unavailable remote asset*, so generic **locally bundled icon placeholder** is displayed. Any `name` field is ignored by rendering; it is **not** used for choosing assets. For icons with a valid `url`, remote/cache primary → fallbackUrl → generic local placeholder on failure. This is the one normal image failure-state path, not a temporary branch.
- Important distinction: `url` is required by the **new backend icon contract**, but the frontend wire decoder allows its absence for current legacy documents, mapping to an explicit UnavailableAsset/nullable URL state; do **not** populate a fake URL (`""`) as if it were valid, crash the entire screen, or silently discard whole Elements. Domain renderer's asset data is either Available(url,fallbackUrl) or Unavailable (generic local fallback).
- Never implement `name`→native icon lookup, a compatibility migration mapper, or mutable feature toggle. Do not pretend a placeholder represents a semantic glyph. Controls retain their text/action semantics and accessibility; missing critical icon may need a non-misleading generic graphic or no image.
- ImageElement missing required URL remains a typed document failure unless product explicitly marks it optional; migration exception is narrowly limited to existing name-only icon Content/Element inputs. Treat this as deliberate tolerant-reader schema compatibility built into final DTO boundary, not as a runtime workaround. Regression test old fixture and URL-migrated fixture against same binary.

## Gradient: implement NOW from current schema
Example backend theme declares `gradient: {type:"linear",angle:135,colors:[{color:"#DDF8F6",stop:0},{color:"#F7FEFD",stop:0.28},{color:"#FFFFFF",stop:0.55},{color:"#D9F7F4",stop:1}]}`.
Mapper must preserve **angle** and all (color, stop) pairs. Renderer must use stop-aware `Brush.linearGradient(colorStops=...,start=...,end=...)` (or Compose equivalent) rather than `colors.map { color }`, computing endpoints from size for declared angle. Validate >=2 stops, each stop in [0,1], ascending order, valid hex. No hardcoded color or gradient stops. The angle orientation must be fixed by contract with a golden visual fixture; unit is degrees based on supplied `135` and earlier source, but orientation is not verified by the fixture text alone. Support the already-present fields without waiting for backend migration.

## Navigation: implement current contract, keep internal extensibility
- Existing `push`, `replace`, `pop`, `present`, `dismiss`, `back` and related action semantics remain functional.
- `popTo + refresh` is **not needed now and the backend does not emit it**. Do not add undocumented payload keys, UI controls, or special handling for invented JSON. Retain distinct `entryId`, clean NavigationState ownership and an explicit Store refresh mechanism if the existing internal lifecycle needs it; do not promise support for an unknown future wire shape without approval.
- When backend needs `popTo`, define/approve a versioned action payload and extend typed ActionMapper then; that later separate feature is not a current implementation freeze blocker. No temporary branch.
- Any failure of loaded screen refresh must not destroy otherwise usable UI.

## Sequence: preserve current tested successful semantics
- All children execute in sequence; a successful Navigate/Present/ExternalUri effect **does not stop later actions**; ordered effects and state patch retained.
- For an unsuccessful child, adopt deterministic fail-fast semantics: stop later children and **do not publish accumulated new effects/state from this sequence**; completed HTTP server side effects cannot be rolled back. Cancellation throws and publishes no later effects. Documented as chosen V2 default subject to owner overriding it before plan signature, not a retroactive claim they signed off this failure detail.
- Session effect queue/order survives navigation entry disposal, and all requests requiring validation validate even when nested.

## Acceptance matrix (migration-independent)
1. Old Login/OTP fixture: screens render and interactions function; name-only content shows generic placeholder where needed; existing image URL loads or falls back normally.
2. Migrated fixture with same screen tree but icon `url`/optional `fallbackUrl`: same frontend build uses real icons automatically, including cache/network failures.
3. Template without header/footer: body unchanged. Template with agreed optional siblings: slot components rendered with independent padding/insets, no modification to existing components array.
4. Screens with 1 or many components; direct Element and Section/Group nesting; original actions, references, validation retained.
5. Theme gradient maintains stops 0/0.28/0.55/1 and 135-degree direction, verified visually.
6. Existing navigation continues without `popTo`; no unknown navigation defaults silently change stack.
7. No duplicate mapper variants, no icon registries, no giant base class, no new module.

## Decision classification
- **Owner-approved:** optional header/footer per screen, no migration dependency for existing flow, URL-only actual icon assets with generic bundled fallback meanwhile, backend migration without frontend code change for AGREED URL contract, gradients from current wire, no present need for popTo.
- **Concrete proposal to synchronize:** exact `header/footer` DTO schema shown above. Backend must match this shape to guarantee no frontend change; neither provided fixture currently demonstrates it.
- **Implementation-review defaults:** sequence failure rollback-publication semantics and exact 135° angle orientation. Owner has confirmed sequence continuation after successful effects, not separately approved every failure detail.
- **Freeze:** Core architecture and old-current flow contract can freeze; entire final PLAN FROZEN requires one coherent blueprint/signoff. Backend deployment does not block coding/planning once additive schema is accepted.
