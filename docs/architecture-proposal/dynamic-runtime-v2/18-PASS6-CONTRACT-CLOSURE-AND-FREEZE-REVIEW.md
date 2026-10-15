# 18 — Pass 6 Contract Closure & Freeze Review

> **Owner clarification — superseding migration gating:** [19-BACKEND-MIGRATION-INDEPENDENT-CONTRACT.md](19-BACKEND-MIGRATION-INDEPENDENT-CONTRACT.md). Backend deployment of URL-icons, optional slots and popTo is **not** a blocker to implementing current frontend behavior. Old name-only icons yield a generic local placeholder without lookup; migrated icon URLs load automatically. Gradient stops and angle are already present and required now. popTo is deferred, not a current requirement. The exact optional Header/Footer field contract must be agreed so future JSON works unchanged. Legacy recommendations in this draft marked 'blocked' solely due to backend readiness are superseded by doc 19.

**Status: CONTRACT CLOSURE PROPOSAL / FREEZE CANDIDATE — NOT PLAN FROZEN.**
Source: owner-provided Login and OTP response fixtures, owner clarifications, Passes 1–5, and draft 17. No Kotlin code was edited or compiled. This is an explicit decision matrix: **Confirmed** = supported by owner's decision; **Recommended** = coherent choice to approve at freeze; **Blocked** = requires new backend fixture or separate approval. Old code never dictates the target design.

## 1. Confirmed invariants, no reinterpretation
- Exactly one `data.template` per Screen response; **one or many `template.components[]`**. Component → Elements and/or Sections; Section → Elements and/or Groups; Group → Elements only; Elements → optional leading/trailing Content (inside properties). Six categories only.
- Existing API envelope, screen/template/component/section/group/element field placement remains fixed. Header/Footer must be **additive**, not `template.body.components`.
- Typed DTO/domain/mappers; Pure Store MVI, entry-local bindings, session-retained values, one Navigation authority, no ViewModel, no duplicate engines/managers/modules.
- Current seven action capabilities `request,navigate,external_uri,present,dismiss,state,sequence`; five event hooks. `$binding,$context,$state,$literal` only, no `$response`.
- Each icon/image receives backend `url` and optionally `fallbackUrl`; the backend will migrate existing `name` icons. No frontend icon name map. Shared asset-loading helper `renderer/asset/RemoteAssetImage.kt`, used by ImageElement, IconElement, IconContent. Coil cache where available; after primary+fallback failures a type-appropriate generic local placeholder **may** display. Preserve no-asset policy for optional/decorative images if placeholder would mislead.
- `renderer/template/` houses concrete Template renderers; `renderer/shell/` owns optional slots. Only `TemplateShell.kt` is mandatory. Header/Body/Footer dedicated renderer classes are conditional on real distinct code, not presumed.
- Template `properties.padding` applies to Body alone when header/footer exist, while shell background is separate; insets and keyboard areas are platform-owned.

## 2. Final decisions: exact behavior recommendations

| ID | Area | Recommended exact semantics for owner acceptance | State |
|---|---|---|---|
| C01 | Header/Footer structural wire | Add optional `template.header` and `template.footer` sibling objects. Proposed `{properties: {...}, components: [...]}` is **NOT existing wire**; zero/one/many slot components and accepted property names require backend/schema agreement. Do not introduce `body` wrapper. | **BLOCKED: exact backend wire fixture** |
| C02 | Header/Footer initial behavior | Release V2 with static optional Header + Body (scrollable where declared) + static optional Footer. Collapsing/fading/swap and sticky policy only when backend supplies explicit typed config and fixture; don't invent defaults or extra animation orchestration. | **Recommended scope narrowing; needs approval** |
| C03 | Child order | Render direct `elements[]` first then `sections[]` within each Component; direct `elements[]` first then `groups[]` within each Section, retaining array order. This matches current traversal and doesn't add arbitrary mixed-node schema. | **Recommended** |
| C04 | popTo target | Match `templateId` in current stack, select **nearest matching entry from top** (if repeated visits), remove above it; if absent return typed failure and do not mutate stack. `entryId` unique per visit, never equal to templateId. | **Recommended** |
| C05 | popTo/refresh wire | Propose `navigate.payload.mode=\"popTo\", target templateId as existing destination.templateId, refresh:boolean`; this is additive action payload only and **not part of canonical existing JSON**. Without a backend-approved example, do not implement assumed keys. | **BLOCKED: backend acceptance** |
| C06 | refresh/bindings | A refresh re-fetches the target entry's own destination. Retain its entry-local bindings unless the navigation request explicitly asks for reset through an approved typed field; clear field errors; session retained state stays. `Retry` on failed fetch should not be blocked by an older successful document. | **Recommended**, reset wire **blocked** |
| C07 | effects lifecycle | A session-owned sequenced effect dispatcher accepts committed ordered effects **before** the entry is disposed; the host applies each once in order. Never create a second navigation stack. Process-death persistence not guaranteed; cancellation on logout/session stop. Avoid storing lambdas or host platform references in Domain. | **Recommended; integration implementation test mandatory** |
| C08 | sequence execution | Execute all successful children sequentially, even after a Navigate/Present/ExternalUri effect; child state patches visible to following child. On first child failure/cancellation, no newly accumulated effects/state patch published from that Sequence; previously completed server requests cannot be rolled back. | **Recommended default; needs owner signoff** |
| C09 | nested validation | Each RequestAction with validate=true checks the *current screen and bindings* directly before that request, including nested Sequence. Reject invalid with field errors and skip HTTP. | **Required** |
| C10 | retain | `retain` mapping paths are required by default: missing path is error, explicit null is distinct and may be retained as null. No implicit `$response`. If optional retains are ever desired, introduce explicit optional syntax after backend approval. | **Recommended** |
| C11 | action response | Parse by caller operation; `responseMode=none` requires successful envelope but no destination. `responseMode=destination` requires valid nested destination. Non-success app code always failure. | **Required** |
| C12 | transport security | Relative endpoint constrained to configured trusted API origin; untrusted header overrides rejected. Bearer only from secure configured credential provider. No automatic token/body injection into asset URLs. | **Required** |
| C13 | destination method/auth | Canonical fixtures carry only screenId/templateId/templateType/endpoint. Keep these four wire fields for initial compatibility. Screen fetch GET / NONE defaults based on currently proven source behavior; add method/auth only with explicit backend contract. Request actions already specify method/auth independently. | **Recommended** |
| C14 | image local fallback | Backend primary URL → fallback URL; use normal Coil memory/disk caching when available, then bundled generic placeholder on failure where semantically safe. Failures should not crash screen. Generic icon placeholder must not impersonate a specific action glyph. | **Confirmed direction; fallback selection test required** |
| C15 | gradients | Preserve color and stop position (0..1) and angle from JSON. Validate stops ordering/count. Establish angle clockwise/origin by screenshots against backend design before freeze of exact draw algorithm. | **BLOCKED: visual convention parity fixture** |
| C16 | controlled UI events | Disabled elements don't emit actions; `onValueChange/onFocus/onSubmit` routed according to declared event hooks, segmented input uses supplied keyboard and dimensions; no OTP hardcoding. | **Required** |
| C17 | Store errors | Keep current document rendered for action/effect failure and show recoverable error, full-screen error reserved for missing document on load failure; cancellation propagates without error UI. | **Required** |
| C18 | DI and scope | Koin initialized once per app lifecycle, Store entry-scoped, RetainedState and effect dispatcher session-scoped. App entry owns session close/reset; no global shared Store. | **Required** |

## 3. Contradiction and overengineering review

| Earlier proposal | Closure judgment |
|---|---|
| Exactly one Component per Template | **Incorrect; removed.** One Template; 1+ Components. |
| Wrap old `template.components` in `template.body.components` | **Rejected.** Add optional header/footer siblings only. |
| Only Request/Navigate in V2 | **Rejected.** Preserve all seven action types. |
| All icons must omit native local fallback | **Revised by owner.** URL-only actual assets plus optional local generic placeholder. |
| Icons by `properties.name` | **Backend migration only.** Don't create client icon registry. |
| Mandatory four slot files | **Overengineering.** One TemplateShell, optional focused Composables. |
| Six renderer registries as six services | **Overengineering.** One category-aware registry, independent maps. |
| Shared `Json` instance mandatory | Not necessary; consistent JSON policy and no duplicate transport more important. |
| `DynamicResponse` screen detection by field heuristics | **Rejected.** Operation-specific mapping. |
| `LoadDynamicScreenUseCase` as pure pass-through | Remove if no actual policy; Store may call Domain repository boundary. |
| Extra Bootstrap forwarding interface and RemoteDataSource layers | Remove only where no unique policy/caller requires them; no blanket deletion. |
| Multiple “manager” classes for shell/patch/effects | Choose minimal ownership: one session dispatcher (needed for disposal safety); focused pure functions for patch operations; avoid action/renderer facade forests. |
| Render-time missing node/asset/invalid binding silently return | **Rejected.** Validate required contract in Data, expose typed screen error. |
| Extra Domain HTTP method fields not present in canonical destination | Mark additive only; do not claim current wire. |
| Sealed category interfaces implemented across packages | Compile restriction; use ordinary interfaces. |

## 4. Kotlin module dependency sanity check
- Domain models and repository interfaces have no Ktor/Compose/Koin/Json types.
- `:network` may depend on `:core` but not `:domain`; `:data` depends on Domain and Network, not Feature.
- `:feature:dynamic` consumes Domain and Navigation contracts; don't accidentally make Navigation depend on Feature. Navigation host presentation is Compose, never calls repositories.
- `:app` constructs/owns session and platform effect adapter; one renderer registry and Koin root.
- Typed Sequence handler must not constructor-inject the executor that constructor-injects Sequence handler. Prefer narrow callback in executor execution context or executor-owned recursive composition with isolated testable helper.
- Avoid generic handler unchecked casts across Kotlin Native/Wasm; type-safe registration adapter or shared non-generic `ActionHandler` + typed `when` at one boundary.
- Pure Store does not own platform URI opener nor direct backstack mutation.

## 5. Ready-for-freeze gate

**Before signing PLAN FROZEN:**
1. Supply backend-approved sample JSON with optional header/footer and URL icons, plus original Login/OTP fixture compatibility.
2. Accept proposed navigation popTo/refresh payload and reset behavior or explicitly defer popTo to a versioned extension.
3. Approve sequence failure publication semantics and effect persistence boundary.
4. Validate visual gradient angle/stops convention and shell safe area/keyboard/scroll policy with product fixture.
5. Revise `17-PASS6-EXACT-IMPLEMENTATION-BLUEPRINT-DRAFT.md` to replace all `open` fields with approved decisions or explicitly postpone capabilities; re-evaluate wave tests and per-source moves.
6. Run final documentation-wide contradiction scan, including `AI_START_HERE.md`, authoritative dynamic docs and decision log; record any owner-authorized superseding decision. Do not update Master State to implementation complete.
7. Owner approves exact plan. **Implementation requires a separate authorization.**

**Current classification: architecture target stable; Passes 1–5 static audit complete for inspected files; Pass 6 is a strong review draft, not a frozen implementation contract.** No green CI, device, iOS or Web claims.
