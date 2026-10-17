# W6-B — Frontend Action Contract vs Backend Migration Gap Matrix

**Status: SOURCE AUDIT / DRAFT ONLY — NOT PLAN FROZEN; NO KOTLIN OR BACKEND SOURCE AUTHORIZED.**
**Frontend:** `carbroz26/CB-Partner` `architecture/dynamic-runtime-v2-discussion-freeze` at `956e3b29cfbcfb751dd1281a479388e74cd618ab`.
**Backend (read-only):** `carbroz26/carwash-backend` `development` at `f51f8918c834b13731acde2982c6c20647675ec4`, fork of `clearvisionfx-dev/carwash-backend`. Backend implementation/PR prohibited for now; migration changes recorded here only.
**Authority:** later owner revisions in docs 60/61: Sequence remains a registered inert action, regardless of earlier conflicting removal text. W6-A verified implementation preserved; outside-tap dismissal explicitly deferred.

## Sources read
Frontend: `data/.../DynamicActionDto.kt`, `DynamicActionMapper.kt`, `domain/.../DynamicAction.kt`; all handlers and `DynamicActionExecutor.kt`, `DynamicActionResult.kt`, `DynamicStore.kt`, `DynamicSession.kt`, `ApplicationContent.kt`, `DynamicNavigationState.kt`; `DynamicActionExecutorTest.kt`, `NavigateActionHandlerTest.kt`, `RequestActionHandlerTest.kt`. Backend: `src/modules/sdui/engine/src/core/{Action,SduiModel}.ts`, `src/modules/sdui/engine/tests/Action.spec.ts`, and `PartnerLoginScreen.ts` / `PartnerOtpScreen.ts`.

## Exact observed compatibility matrix

| Area | Current backend source | Current frontend source | Finding / scope |
|---|---|---|---|
| Hooks | Elements include action map; production login/OTP use click and span click | DTO has onClick, onLongClick, onValueChange, onFocus, onSubmit | Five-hook DTO maintained; do not add hooks |
| Request basics | requestAction constructs method/endpoint/authentication/validate/body/responseMode | RequestActionHandler resolves typed values, validates response mode, executes repository | Corresponding request shape largely aligned |
| Request retain | Action.ts accepts/emits `retain` | RequestActionHandler consumes `retain` and commits retained response state | **Schema contradiction**: strict `requestActionSchema` in SduiModel.ts does not declare retain; requires migration/actual serialization-path checks |
| Request navigation mode | Request handler frontend supports `payload.mode=push/replace` | Backend request builder/schema do not declare mode | Frontend extension not cross-repo contracted; do not emit without joint decision |
| Navigate | Action.ts returns `{type:'navigate', destination}`; SduiModel.ts accepts destination | DTO maps destination; handler uses action.destination | Aligned in production path. Backend Action.spec.ts older assertions expect `payload.screenId`: source-test mismatch to document, not a proved test failure |
| Present | `{type:'present',targetId,payload:{presentation}}` | action DTO has no top-level targetId; handler requires destination | **Incompatible**; frontend has distinct presentation entry, cannot silently map `targetId` to destination |
| Dismiss | optional top-level targetId | handler emits Dismiss; navigation consumer dismisses presentation only | Targeted-vs-active semantics unresolved; safe rejection if none active |
| State | top-level targetId and payload.operation/property/value; set/toggle constraints | handler expects payload.key/value; writes session runtime state path | **Incompatible**; cross-element state contract deferred; no speculative adapter |
| External URI | `payload.uri` typed reference | resolves URI to effect; application consumer opens URI | Corresponding mechanism exists; runtime context values still require separate evidence |
| Sequence | Backend builder/schema allows nested `payload.actions` | Registered Sequence handler ignores all payload and returns Completed | Deliberate divergence: docs60 later revision / doc61 freeze; don't execute or remove |
| $response | Backend ref.response() and Zod referenceSchema allow it | Frontend frozen resolution excludes $response | Backend migration candidate, but audit persisted payload compatibility before removal |
| back alias | No canonical backend action type `back` in SduiModel.ts | executor rewrites `back` to dismiss; navigate mode back maps Pop | Semantic ambiguity; needs owner decision before code change |
| Effects | Frontend concrete results each 0 or 1 effect | Session holds ordered batches/leases and commits state before effect | No evidence yet authorizing cardinality refactor; W4 safety preserved |

## Tests and evidence limitations
Frontend executor covers dispatch, duplicate registration, unknown, inert Sequence, trace and cancellation. Navigate tests cover default/explicit/invalid modes and missing destination. Request tests cover typed refs, retain, response defaults, invalid mode, missing paths and trace. The W6-A owner-reported JVM regression confirms ordered `ValueChanged` then Request reads latest binding snapshot; it does not cover every coroutine race. There is no verified execution of backend Vitest in this audit. Backend Action.spec.ts `navigate` expectations visibly differ from Action.ts returned shape. Further test coverage for Present/Dismiss/State contract must await frozen semantics.

## Backend migration document only (NO backend edits now)
1. Reconcile `Action.ts` builder and strict `SduiModel.ts` schema for Request `retain`; first inspect actual validation/emission and persisted screen documents. Align backend Action.spec.ts navigation expectations to `destination` if confirmed as canonical.
2. For Present, decide whether `targetId` names an existing element/overlay or a new screen destination. Do not translate it to frontend `DynamicDestination` without documented semantics, lifecycle and dismissal contract.
3. For Dismiss, decide how optional `targetId` interacts with current presentation; document target-match vs active-only behavior.
4. For State, jointly define target, supported properties, operations and ownership/lifetime; frontend currently handles session key/value state, not element state. This is a future migration decision.
5. Remove/replace `$response` backend authoring only after tracing active usages/legacy documents and designing supported response retention, without resurrecting frontend `$response`.
6. Sequence nested actions remain inert on frontend by owner decision. No backend removal or execution migration during W6-B.
7. Align backend sample screens/test fixtures and actual emitted JSON with contract after separate approval; avoid changing original repo directly, use approved fork feature branch → upstream PR.

## Proposed frontend next steps (PLAN DRAFT — NO SOURCE EDITS)
A. Preserve Request/Navigate/ExternalUri/Sequence and session/nav architecture. Do not combine Present and Navigate or Dismiss and Pop.
B. Make narrow tests-first plan for accepted, current local behavior where missing: present/dismiss rejection lifecycle and action snapshot; do not assert unsupported backend payload compatibility.
C. Resolve owner's decisions on Present, State and back alias before authorizing source changes. Add test cases only against frozen chosen wire semantics.
D. Post-approval implementation on frontend same branch only; targeted Gradle JVM suite + Web/Android gates and source review.
E. Keep W6-A outside-click keyboard dismissal deferred and separate.

**Decision gate:** OWNER REVIEW REQUIRED. This document is a source-informed gap audit and candidate migration plan, not frozen implementation authority.
