# 67 — Navigation/Presentation: Frontend ↔ Backend Wire Reconciliation and Exact Cleanup Plan

**Status:** SOURCE AUDIT COMPLETE FOR LISTED FILES; PLAN DRAFT / NOT FROZEN / NO KOTLIN EDITS. Frontend: CB-Partner `architecture/dynamic-runtime-v2-discussion-freeze`; backend read-only: CB-Backend `development`. Request tests/build GREEN at owner-tested `9d76a2eb` (137 tasks, 28 executed, 109 up-to-date); Android/Web smoke still pending. Inert Sequence remains registered, with no child execution. Backend migration prohibited.

## Reviewed frontend source (read-only)
- `data/.../model/action/DynamicActionDto.kt`: five hardcoded hooks onClick, onLongClick, onValueChange, onFocus, onSubmit; action has `type`, `payload`, `destination`; no `targetId`.
- `data/.../mapper/DynamicActionMapper.kt`: maps those five hooks only, forwards typed payload and four-field destination.
- `data/.../model/response/DynamicDestinationDto.kt`, `domain/.../DynamicDestination.kt`: screenId, templateId, templateType, endpoint, same as backend `dynamicDestinationSchema` (no schema mismatch for the backend engine's canonical action destination).
- `domain/.../action/DynamicAction.kt`: type + payload + destination only; no targetId.
- `NavigateActionHandler.kt`: absent mode Push; explicit push/replace/pop/back; rejects unknown/null/nonstring; requires destination for push/replace, allows absent for pop. Handler only creates typed result, correctly leaves navigation state to app.
- `PresentActionHandler.kt`: requires `action.destination`, returns Present result. `DismissActionHandler.kt`: returns Dismiss without checking target.
- `DynamicActionExecutor.kt`: normalized type lookup, with top-level `back` aliased to `dismiss`; frozen W4 explicitly held this compatibility alias pending proof.
- `DynamicNavigationState.kt`: one navigation stack owner, unique entry IDs, independent modal presentation entry, `present` rejects concurrent modal, `dismissPresentation` returns false if absent, `pop` dismisses modal before stack pop, refuses root pop.
- `DynamicNavigationHost.kt`: Nav3 `NavDisplay` onBack calls `state.pop()`; presentation rendered with Compose `Dialog` only, dismiss request uses `dismissPresentation`.
- `app/ApplicationContent.kt`: session `Channel<List<DynamicActionEffect>>`; single consumer handles all effect kinds, with Dismiss applying `dismissPresentation() || pop()`; safe string error UI; Request-derived navigation runs through same consumer.
- `NavigateActionHandlerTest.kt`: four tests cover default/supported/rejected mode and push/replace destination.
- `DynamicNavigationStateTest.kt`: seven tests cover entries, stack and modal, root, bindings retention. No direct tests for app effect consumer's Dismiss fallback or presentation wire compatibility identified in this examined test inventory.

## Reviewed backend source (read-only)
- `sdui/engine/src/core/Action.ts`: `navigateAction(destination)` generates `{type:'navigate', destination}` (no mode); `presentAction(targetId,presentation)` generates `{type:'present',targetId,payload:{presentation}}`, not destination; `dismissAction(targetId?)` generates targetId optional; `sequenceAction(actions)` persists as backend authoring, but frontend ignores its child list.
- `sdui/engine/src/core/SduiModel.ts` actionSchema: Navigate requires four-field destination and contains NO navigate payload/mode; Present requires targetId and presentation enum dialog/bottom_sheet/popup but contains NO destination; Dismiss may include targetId and contains NO destination. Top-level back is NOT an actionSchema type.
- Same backend `SduiModel.ts`: `actions` is `z.record(nonblank key, actionSchema)` rather than five named hooks; frontend DynamicActionsDto only recognizes five keys. It is not proven that backend emits unrecognized event names today. This is a wire extensibility mismatch, not permission to invent event handlers.
- `sdui/engine/tests/Action.spec.ts`: asserts actual navigate destination, present targetId+presentation, dismiss optional targetId.
- `sdui/engine/src/screens/partner/PartnerOtpScreen.ts`: direct navigate action used; auth screen action request/externalUri flows previously observed. Neither PartnerLoginScreen nor PartnerOtpScreen directly authors Present/Dismiss from reviewed usage; a lack of use is NOT proof of general compatibility.

## Confirmed critical cross-layer gaps
1. **Present wire incompatibility:** backend valid Present uses `targetId` and `presentation`, frontend DTO/domain drops targetId, and frontend Present handler insists on `destination`; therefore this backend wire shape cannot succeed as frontend Present without a separately approved compatibility contract. Do NOT fabricate targetId->destination mapping or change backend.
2. **Dismiss semantics mismatch:** backend Dismiss optionally identifies a `targetId`; frontend loses that field and session consumer pops stack when no modal. Owner's described Dismiss intent is dialog-only; needs explicit decision whether to reject mismatched target vs ignore it and whether Dismiss only closes active modal.
3. **Navigation back conflation:** backend doesn't author top-level `back` action, while frontend maps it to Dismiss. Dedicated Navigate pop/back is already supported but backend navigate schema currently requires destination and no `mode`. Do not remove compatibility alias before owner signs off.
4. **Presentation styles:** backend advertises dialog, bottom_sheet, popup; frontend NavigationHost offers Compose Dialog only. Cannot claim style-specific presentation support.
5. **Events are not schema-parity:** generic backend action-key map vs fixed five frontend hooks; ensure screen source/mappers/tests are used before calling this an active defect.
6. **Potential dead batch machinery:** inert Sequence means source-reviewed DynamicActionResult only produces zero or one effect per ordinary action, but `List<DynamicActionEffect>` and `Channel<List<...>>` remain in use. Source-disposal safety cannot be sacrificed merely to remove lists. Requires complete consumer/runtime/test inventory in separate Shared audit.
7. **NavigationState pop policy:** `pop()` first dismisses any active modal. This is a user-back-stack policy and distinct from explicit `dismiss`. If modal-only Dismiss is approved, change effect application only; do not alter back behavior without a separate decision.

## Proposed exact file-by-file plan — REVIEW ONLY
| Path | KEEP/MODIFY/DEFER | Focus |
|---|---|---|
| `feature/dynamic/action/NavigateActionHandler.kt` | KEEP | Standalone typed navigation, modes W4 verified |
| `feature/dynamic/action/PresentActionHandler.kt` | DEFER pending backend Present targetId semantics | No speculative destination resolution |
| `feature/dynamic/action/DismissActionHandler.kt` | KEEP or narrow after decision | Only typed Dismiss result; avoid screen-pop responsibility |
| `feature/dynamic/action/DynamicActionExecutor.kt` | DECIDE alias | Top-level back->dismiss compatibility pending usage proof |
| `app/ApplicationContent.kt` | MODIFY conditionally | If owner approves modal-only explicit Dismiss, remove `|| navigationState.pop()`; preserve URI, state-before-effect, one session consumer |
| `navigation/DynamicNavigationState.kt` | KEEP | Single nav/modal authority; no second manager |
| `navigation/DynamicNavigationHost.kt` | KEEP | Actual Nav3 back policy and Compose Dialog; no invented bottom sheet |
| `data/.../DynamicActionDto.kt`, `DynamicActionMapper.kt`, `domain/.../DynamicAction.kt` | REVIEW / DEFER | No targetId transport despite backend schema; need real target semantics before additive field |
| `data/.../DynamicDestinationDto.kt`, `domain/.../DynamicDestination.kt` | KEEP | Canonical destination 4-field parity with backend SDUI engine |
| `feature/dynamic/.../NavigateActionHandlerTest.kt`, `navigation/.../DynamicNavigationStateTest.kt` | EXTEND after freeze | Modal-only explicit Dismiss, back/pop independence, root rejection, valid/invalid Present behavior |
| `app` integration tests | REVIEW feasibility | Consumer effect application and presentation style actual rendering |
| Backend `Action.ts`, `SduiModel.ts`, screen fixtures | NO EDIT | Backend migration explicitly deferred |

## Owner decisions REQUIRED before freeze
N1. Explicit `dismiss`: close modal only, never pop stack? Existing app fallback currently does.
N2. Backend `present(targetId,presentation)` is not representable in frontend `DynamicAction`: defer support until joint contract, or authorize a *frontend-only* compatible interpretation of targetId (must be source backed). No unsupported implicit destination.
N3. Is top-level `back` alias held for legacy compatibility, or should be removed given backend actionSchema excludes it? Distinguish from `navigate.payload.mode=back`.
N4. Should presentation style dialog/bottom_sheet/popup be explicitly deferred except dialog until backend/frontend contract is coordinated? No extra renderer without need.
N5. Should five frontend event hooks remain constrained to observed renderer events until separate generic events contract? No speculative schema changes.
N6. Keep session effect batch simplification for Shared Infrastructure audit rather than bundle with Navigation changes.

## Planned gates AFTER freeze/authorization
- Tests before code, minimal independently verifiable commits, full JVM navigation/dynamic/data and Web compilation tests, `git diff --check`, Android/Web UI smoke.
- Stop on schema mismatch rather than silent conversion. No Backend source edits. Keep inert Sequence and Request behavior unchanged.
