# 68 — Navigation / Presentation Contract Freeze Candidate

**State: OWNER DECISION PENDING — NOT PLAN_FROZEN — NO KOTLIN IMPLEMENTATION AUTHORIZED.**

Frontend: `CB-Partner` branch `architecture/dynamic-runtime-v2-discussion-freeze`. Backend: `CB-Backend` branch `development`, read-only. Preceded by source/contract audit 67. Preserve inert `sequence`, Request Action and single navigation-stack authority.

## Candidate decisions for owner acceptance

**N1 — Dismiss presentation only (recommended APPROVE).**
- Explicit `dismiss` closes an active presentation. It MUST NOT pop the normal navigation stack when no presentation exists.
- With no active presentation, return controlled failure/no-op per existing effect-consumer error convention, not silent navigation.
- Distinguish from user/platform Back: Nav3 Back or `navigate(mode:pop/back)` may dismiss active presentation before stack pop, retaining existing `DynamicNavigationState.pop()` behavior.
- Scope if approved: small edit in `app/ApplicationContent.kt`, targeted tests of effect consumer/presentation boundary if practical. `DismissActionHandler` should remain typed-result only.

**N2 — Present wire mismatch: defer implementation until precise target semantics (recommended DEFER).**
- Backend canonical `{type:'present',targetId,payload:{presentation:'dialog'|'bottom_sheet'|'popup'}}` has no destination.
- Frontend `DynamicActionDto`/`DynamicAction` have no targetId and `PresentActionHandler` requires destination. This cannot be honestly declared supported.
- No inferred `targetId -> DynamicDestination` mapping. Retain current code for compatibility until separately contracted front-end-only resolution or future jointly approved schema migration. Do not edit Backend.

**N3 — Top-level back alias: retain pending compatibility evidence (recommended KEEP FOR NOW).**
- `DynamicActionExecutor` maps action.type `back` to `dismiss` today. Backend canonical actionSchema does not define standalone `back`.
- Preserve alias for now rather than delete based only on an absence in current Backend schema. Explicit navigate mode back remains a distinct supported frontend capability. Revisit in Shared Infrastructure audit with usage inventory.
- No new alias and no new navigation handler.

**N4 — Presentation styles: defer unsupported style-specific UI (recommended DEFER).**
- `DynamicNavigationHost` currently renders presentation as Compose `Dialog` only.
- Backend schema enumerates dialog, bottom_sheet, popup. Do not pretend other variants are rendered, silently treat them all as Dialog, or implement new host/renderers before destination/targetId contract is approved.

**N5 — Action event keys: keep observed five frontend hooks (recommended KEEP).**
- Backend `actions` schema is an open keyed map; frontend currently maps onClick/onLongClick/onValueChange/onFocus/onSubmit.
- No speculative renderer/DTO generic-action rewrites without actual unsupported event use case and UI event wiring contract. Audit separately.

**N6 — Effect batches/list simplification: defer to Shared Infrastructure audit (recommended DEFER).**
- Existing `List<DynamicActionEffect>`, channel batching, single session consumer and state-before-effects ordering remain unchanged until all producer/consumer and disposal paths are reconciled.
- `sequence` remains inert, independent registered action. No recursive execution.

## Scope and implementation gates if candidate approved
- The only immediate production edit eligible under these decisions is the explicitly approved N1 minimal `ApplicationContent` Dismiss fallback removal. Keep `DynamicNavigationState`, `DynamicNavigationHost`, Navigate/Present/Dismiss handlers, DTOs, Domain, Backend and all other handlers unchanged.
- First write focused tests covering `dismiss` with modal, without modal at root and nonroot, `navigate(pop/back)` independence, and presentation stack identity. Verify RED for presently incorrect Dismiss fallback; then minimal GREEN change.
- Tests for the actual `ApplicationContent` effect consumer should not be claimed without executable coverage. If effect application isn't testable without broad refactor, report that blocker; do not add new EffectManager/router solely for test convenience.
- Run `:navigation:jvmTest :feature:dynamic:jvmTest :data:jvmTest :domain:jvmTest :webApp:wasmJsBrowserDevelopmentWebpack` and `git diff --check`; Android/Web smoke remains separate.
- **Freeze requires explicit owner approval of N1–N6.** Following freeze, Kotlin edits require a separate explicit implementation instruction if project operating gates demand it.
