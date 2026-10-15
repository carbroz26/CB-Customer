# 20 — Dynamic Runtime V2 — PLAN FROZEN

**State: PLAN FROZEN — owner-authorized plan sign-off; IMPLEMENTATION NOT YET AUTHORIZED.**
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`.
**Reference implementation plan:** `17-PASS6-EXACT-IMPLEMENTATION-BLUEPRINT-DRAFT.md` (file/module operations and test waves), as **amended and superseded** by this freeze contract; `19-BACKEND-MIGRATION-INDEPENDENT-CONTRACT.md` fixes migration-independent compatibility; `16-PASS5-FINAL-AND-PASS1-5-CROSS-AUDIT.md` records source review.
**Approval basis:** Owner explicitly requested consolidation of outstanding decisions into a freeze candidate and exact implementation-plan sign-off, then a separate start of Kotlin work. This file defines those choices; no claim that backend has deployed pending changes or that source/build tests have passed.

## 1. Immutable decisions

- Preserve existing Gradle modules and KMP Compose targets; pure MVI/UDF Store, no ViewModel, strict SRP, no global managers or duplicate transport/router/state/mappers; Domain is framework-free.
- Screen DTO preserves `{status,code,message,data:{screenId,schemaVersion,targetApp,template:{id,type,properties,components},theme?},traceId?}` and one Template with **one or more Components**, Components with direct Elements/Sections, Sections with direct Elements/Groups, Groups with Elements, and nested Element Content.
- Six separate logical categories: Template / Component / Section / Group / Element / Content; typed concrete DTO→Domain→Renderer mappings. Current FormTemplate/StackComponent/StackSection/StackGroup; Text/Input/Button/Image/Icon Element; Icon/Divider Content. Existing `template.components[]` is the Body; never create a `body` wire wrapper. No DividerElement.
- Preserve seven existing action types and five declared event hooks: request, navigate, external_uri, present, dismiss, state, sequence; onClick/onLongClick/onValueChange/onFocus/onSubmit. Reference namespaces only `$binding`, `$context`, `$state`, `$literal`; no `$response`.
- Backend owns image/icon URL and optional fallbackUrl, no icon-name glyph mapping. Single shared `RemoteAssetImage` uses Coil caches, primary/fallback and then semantically safe bundled generic placeholder. Absent URL on currently name-only Icon Content/Element maps to unavailable asset without fatal screen failure; migrated URLs use same frontend binary automatically. ImageElement missing required URL still error unless explicitly optional.
- Gradient `type=linear`, `angle` measured in **degrees**, colors with explicit stops are typed and drawn using validated stop-aware Compose linear gradient. Screen and element gradients share conversion. Rendering tests pin the exact visual convention against fixture; current on-device direction not claimed verified.
- Distinct entryId for each visit, single NavigationState and independent presentation, session-retained state vs entry bindings, validation before every Request including nested sequence, state commit before effects, session-safe ordered effect delivery, cancellation propagation and recoverable in-place action errors. Native and web hosts stay in same navigation authority.

## 2. Header/Footer schema — frozen frontend/backend target

This **specific additive schema is the coordination contract** for future backend migration, even though backend currently omits both fields. No guarantees for an unrelated future backend schema.

```json
{
  "data": {
    "template": {
      "id": "tpl_example",
      "type": "form_template",
      "properties": {
        "padding": {"start":24,"top":20,"end":24,"bottom":20}
      },
      "components": [{"id":"body_1","type":"stack_component","properties":{},"elements":[]}],
      "header": {
        "properties": {"padding":{"start":0,"top":0,"end":0,"bottom":0}},
        "components": [{"id":"header_1","type":"stack_component","properties":{},"elements":[]}]
      },
      "footer": {
        "properties": {"padding":{"start":0,"top":0,"end":0,"bottom":0}},
        "components": [{"id":"footer_1","type":"stack_component","properties":{},"elements":[]}]
      }
    }
  }
}
```

Example shows **only the relevant template portion** of a larger valid response; do not treat it as an entire screen document. Fields `header` and `footer` are optional and independent. `SlotDto(properties?,components[])` maps to typed slot with padding and its permitted Component list; **empty slot is valid and renders no content**. Template Body requires existing components list (at least one in supported production screen documents). Existing `template.properties.padding` applies only to Body when a shell exists; Header/Footer padding never inherits Body padding. No header/footer implies zero slots and existing Body rendering.

**V2 initial supported shell:** static optional Header, Body with scroll only when declared, static optional Footer. Slot order Header→Body→Footer; rendering reuses Component registry, no duplicate renderer trees. Slot background/layout is backend-defined, safe area and IME insets are platform-owned. Collapsible/fading/swapping header, bottom tab navigation and persistent cross-screen shell remain future separately specified capabilities; do not create dormant schema or extra renderers for them. Use `renderer/template/FormTemplateRenderer.kt` for concrete Template and `renderer/shell/TemplateShell.kt` for shared placement. Header/Body/Footer helper functions remain local until complexity warrants separate files.

## 3. Navigation, refresh and Sequence — frozen scope

- For V2 initial release support current wire-mode `push`, `replace`, `pop/back`, modal `present/dismiss` and external URI. Unknown operation is a typed validation error; never silently coerce to Push.
- `popTo + refresh` **is explicitly OUT OF SCOPE** for current code freeze, because backend does not emit it and owner says it is not needed now. No speculative wire keys, aliases or temporary compatibility branches. When required, separately freeze an additive typed action payload and implement on the existing single Navigation authority. Entry IDs and Store load/retry lifecycle must not preclude this later extension. Refresh can be a focused internal operation for reload/retry only when actual current requirement exists, not an unused generic action infrastructure.
- Sequence child actions run in order; state patches are visible to subsequent children. Navigate/Present/ExternalUri **do not stop later successful children**. Commit final state changes once, then deliver ordered effects from the session-safe handoff that survives originating entry disposal.
- On child failure: **fail fast**; do not publish that Sequence's newly accumulated state patch/effects; no later children run. Already completed network side effects are explicitly not rollbackable. On coroutine cancellation propagate and publish no new effects. No fake transaction/rollback manager.
- Validation precondition attaches to each RequestAction even inside Sequence, not only top-level Store event. Missing required retain paths fail; explicit null stays distinguishable.
- Entry-local input bindings survive normal push/pop identity restoration while entry exists; a new pushed entry has independent bindings. Session-retained values persist until session end/reset. Action errors preserve currently loaded screen; load failures without usable document show explicit failure UI.

## 4. Image contract, including pending migration

- Icon and Image renderers know **actual asset URL**, style/layout/semantics remain in concrete renderer. `RemoteAssetImage` alone manages cache, URL fallback and bundled placeholder.
- Absent icon URL in pre-migration, name-only fixture is tolerated as UnavailableAsset (generic placeholder, or no decorative image where misleading), *not* mapped from `name` or treated as malformed whole screen. This is permanent nullable/UnavailableAsset semantics, not a temporary adapter. After URL migration, URL wins automatically.
- Valid asset URL can be absolute or approved relative as determined by trusted asset-origin policy; do not leak auth bearer headers to arbitrary remote image hosts.
- Visual-critical controls retain text/accessibility cues; generic icon placeholder must not falsely look like a specific operation.

## 5. Exact implementation work scope (binding manifest)

The exact existing-file inventory and create/replace/delete table in document **17 Section 2** forms this plan's file-level implementation manifest, with these precedence corrections:
1. Use **all seven** typed action forms from first V2 implementation; `SequenceActionHandler` or small executor-owned pure sequence helper only, not both.
2. Keep six-category registry as one category-aware registry with one set of type-safe adapters. No registry manager per category.
3. Keep `template.components[]` and introduce optional header/footer fields in the same FormTemplate DTO/mapper/domain pipeline; do not relocate to body. Slot renderer(s) are functions inside `TemplateShell.kt` until justified.
4. Keep name-only icon fixtures readable without icon lookup, by explicit unavailable asset representation; URL and remote fallback semantics are final. Asset loader logical file is `renderer/asset/RemoteAssetImage.kt`.
5. **Do not implement popTo or an invented refresh wire decoder** in initial waves. W4 navigation tests cover current push/replace/pop/present/dismiss, entry restoration and session-safe effects; future popTo tests excluded.
6. Keep `LoadDynamicScreenUseCase` only if real policy warrants it; otherwise remove trivial delegation and update all constructor/test callers. The same evidence rule applies to trivial Bootstrap remote data source, separate slot classes, extra converters and distinct action adapter wrappers.
7. Apply consistent typed rendering and gradient stop/angle conversion; support backend text enabled/disabledColor and input event/segmented keyboard behaviors; the old fixtures must render without backend migration.
8. All source moves require updating complete imports/callers/tests in same vertical wave; no shadow old-and-new runtime engines at wave exit.

The target logical packages and per-file operations in doc 17 are **implementation instructions**, not a mandate to generate one class per named responsibility. Class declarations may share files if clear and tested. Changes to fixed schema or deleting a supported capability require new owner decision, not developer discretion.

## 6. Implementation waves and acceptance

W0: lock actual source commit/branch; read `AI_START_HERE.md`, project-management documents, authoritative dynamic docs, docs 12/16/17/19/20; verify exact Kotlin source and Gradle tasks; reconcile obsolete drafts. W1: network; W2: bootstrap/splash; W3: domain/data/DTO/mappers; W4: navigation/session current operations and effect handoff; W5: rendering/TemplateShell/gradients/image local fallback; W6: typed actions/sequence/runtime/Store; W7: app/Android/Web/iOS integration + remove obsolete code; W8: reconcile docs and tests.

Mandatory test categories:
- Current owner Login/OTP JSON before backend migration; fixture with URL-migrated icons; screen without and with optional Header/Footer; one/many Component trees.
- Screen identity/schema validation, all node types, Content spacing, gradients exactly with `135` and four stops `0,0.28,0.55,1`; backend controls color and text enabled states.
- All seven actions; nested request validation, typed reference/missing-null paths, retention; Sequence continue-after-effects, fail-fast on child error and cancellation, session-safe ordered effects after nav disposes entry.
- No parallel HTTP/router/Store/renderer stack. Android and Web build/run/screenshots; macOS/Xcode iOS compile/simulator (Windows cannot assert this gate). Current JVM task list and Web webpack command remain as in doc 17, revalidate W0.

An implementation wave **may not be declared complete** without its applicable tests. This plan sign-off is not a claim that those tests have executed.

## 7. No-go changes and stop rules

Stop implementation and return to decision review for any newly required wire-field rename/move, backend format differing from the approved header/footer or icon URL shape, removing action/event types, new HTTP client or stack, new universal manager, generic native icon registry, silent response failures, platform credential leak, lifecycle navigation loss, or unverified data migration affecting canonical fixtures. Do not build workarounds when a backend feature is pending.

## 8. Sign-off

**PLAN FROZEN (authorized for planning): YES.**
- Owner requested implementation-plan consolidation and sign-off; this frozen contract records concrete defaults and deferrals.
- Backend URL icon or optional header/footer deployment **is not** a precondition for frontend code work. Migration acceptance depends on future backend JSON matching the frozen schema.
- `popTo + refresh` deferred by owner and excluded from initial implementation.
- This sign-off freezes the described implementation *plan*, not actual Kotlin behavior/test results.

**KOTLIN IMPLEMENTATION AUTHORIZED: NO.**
Implementation can start only on the owner's separate, explicit request. Remain on approved branch/workflow and perform W0 source verification before writing Kotlin.
