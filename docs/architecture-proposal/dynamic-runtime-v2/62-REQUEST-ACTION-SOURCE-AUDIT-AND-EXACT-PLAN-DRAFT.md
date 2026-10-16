# 62 — Request Action source audit and exact cleanup plan (DRAFT)

**Status:** SOURCE AUDIT / PLAN DRAFT — NOT FROZEN, NO KOTLIN EDITS. **Frontend** CB-Partner branch `architecture/dynamic-runtime-v2-discussion-freeze`; **backend** CB-Backend `development` READ-ONLY. Sequence remains an inert independently registered placeholder per doc61. Backend migration deferred.

## 1. Inspected sources
Frontend: `feature/dynamic/action/RequestActionHandler.kt`, `DynamicActionResult.kt`, `DynamicActionExecutor.kt`, `feature/dynamic/store/DynamicStore.kt`, `feature/dynamic/action/RequestActionHandlerTest.kt`, `domain/model/dynamic/DynamicRequest.kt`, `DynamicResponse.kt`, `data/dynamic/DynamicRepositoryImpl.kt`, frozen doc60. Earlier source audit includes `DynamicActionValueResolver`, `DynamicReferenceResolver`, `DynamicFormValidator`, mapper/DTO and `app/ApplicationContent`.
Backend read-only: `sdui/engine/src/core/Action.ts`, `PartnerLoginScreen.ts`, `PartnerOtpScreen.ts`, `domains/identity/PHASE-6-SEND-OTP-DESTINATION-CONTRACT.md`. Backend's request authoring defaults `responseMode` to `none`, whereas frontend RequestActionHandler defaults to `destination`.

## 2. Source-derived gaps
- **Default mismatch:** Backend `requestAction` emits explicit responseMode with default `none`; frontend missing responseMode defaults `destination`. Backend production screen flows use explicit mode; frontend default divergence should be reconciled without changing backend.
- **Validation split:** Store `requiresValidation` only recognizes literal `BooleanValue(true)` in `action.payload["validate"]`, but handler resolves `validate` via bindings/context/state, and forwards it in `DynamicRequest`. Form validation in Store is skipped for dynamically resolved true. Action Handler itself has no screen input so cannot validate form directly without passing UI state through handler boundary. Define one pre-request validation authority in Store, using shared reference resolution and strictly typed flag semantics. `DynamicRequest.validate` field is presently not used by `DynamicRepositoryImpl.fetch` for validation; its transport purpose requires an explicit decision before removal.
- **Mode coercion:** Request handler maps `replace`, `pop/back`, and all other explicit modes (including unknown) to Push. Navigate handler was made strict in W4-A; Request-specific mode needs contract review. The backend RequestActionOptions does not author a `mode` field; don't invent new Backend changes.
- **Retain silent failures:** `extractRetainedState` ignores nonobject mappings and skips nonstring mapping values; `DynamicReferenceResolver.resolvePath` returns `NullValue` for missing as well as explicit null; business/session state can be corrupted by a typo. Define strict mapping and missing-vs-null semantics before editing.
- **Unsafe UX error:** Store converts failed action's `exception.message` to full-screen DynamicState.Failure despite preserving its screen object. In-place recoverable UI needs scope-aware failure representation, not raw exception text.
- **Response semantics:** `responseMode: none` can return Completed or StateChanged; `destination` demands `response.destination` and yields Navigate (possibly plus retained state). `RequestActionHandler` doesn't call `NavigateActionHandler`, only returns a typed result. Preserve this SRP boundary and state-before-effect order.
- **Domain model discrepancy:** `DynamicRequest.responseMode` defaults to destination, `method` defaults GET, while frontend handler defaults method POST and backend action authoring requires method. Defaults may be legitimate at separate layers but should be explicit/consistent based on actual wire.
- **Destination discrepancy for cross-check:** Backend frozen auth-flow destination contract includes `method` and `authentication` in its domain value; frontend DynamicDestination currently contains four identity fields. This may be intentional layer difference, but verify DTO/mapper and actual response schema before any Request refactor.

## 3. Ownership/architecture to preserve
UI event -> entry Store (form validation/intent/cancellation) -> Executor dispatch (one handler) -> Request handler (payload resolution, repository call, response interpretation) -> Result (stateChanges + optional navigation effect) -> Store state commit -> one app-session effect consumer (actual NavigationState). No handler invokes another handler. No new manager/ViewModel/router; no backend edits. Sequence remains inert and unrelated.

## 4. Exact file-by-file plan, subject to freeze
| File | Change | Reason |
|---|---|---|
| `feature/dynamic/action/RequestActionHandler.kt` | EDIT: explicit response mode validation, consistent missing defaults, strict mode typing if mode supported; validate endpoint/method/auth shape before fetch; retain presence semantics; preserve response->destination typed result | Prevent malformed action side effects and silent Push/Null |
| `feature/dynamic/store/DynamicStore.kt` | EDIT: resolve request validation flag from one shared resolver before dispatch, in-place typed/generic safe request error while retaining loaded screen; no handler calls another | Single form-validation owner |
| `feature/dynamic/action/DynamicActionValueResolver.kt` | REVIEW, no immediate removal; keep until action-wide cleanup can update all call sites safely | Avoid unrelated rewrite |
| `feature/dynamic/runtime/DynamicReferenceResolver.kt` | MINIMAL EDIT only if approved retain missing-vs-null requires presence API; avoid duplicate parser | Typed references and retention |
| `feature/dynamic/action/DynamicActionResult.kt` | KEEP state+optional navigation result; no Request-specific result hierarchy without evidence | SRP, no overengineering |
| `domain/model/dynamic/DynamicRequest.kt` | REVIEW defaults and `validate` transport field usage, modify only with agreement | Avoid unexplained breaking changes |
| `domain/model/dynamic/DynamicResponse.kt` | KEEP fields unless verified mismatch | Preserve response |
| `data/dynamic/DynamicRepositoryImpl.kt` | KEEP repository boundary, add tests if required, no Request-internal navigation | Repository abstraction |
| `data/dynamic/mapper/DynamicResponseMapper.kt` + DTOs | AUDIT target destination field shape and status/response envelope before changing | Backend compatibility |
| `feature/dynamic/action/RequestActionHandlerTest.kt` | ADD missing/default/invalid responseMode; request mode; missing retain; explicit null; Request with successful backend destination and retention; failure and cancellation | Focused handler coverage |
| `feature/dynamic/store/DynamicStoreTest.kt` | ADD literal/resolved validate flag; bad form prevents fetch; failed request keeps form usable and avoids state/effects; state-before-nav-effect | Entry orchestration tests |
| `data/.../DynamicBackendFlowContractTest.kt` | ADD cross-repo fixture behavior for Login send OTP / resend OTP / Verify OTP only if source-backed | Wire compatibility |
| `app/ApplicationContent.kt` | No Request-specific change planned, only test existing navigation effect application | Host owns navigation |
| `feature/dynamic/DynamicModule.kt` | KEEP current DI, no separate request manager | Registration unchanged |

## 5. Decisions to freeze BEFORE editing
R1. Missing `responseMode`: align with backend author's `none` vs require explicit wire value (backend currently emits explicit `none`/`destination`). No silent change without owner approval.
R2. `validate` flag: literal/resolved Boolean, explicit null/wrong type, and missing default; Store only validates screen on true and passes flag to existing DynamicRequest pending field review.
R3. Request `mode`: backend author doesn't expose; decide whether to reject supplied unknown/invalid values and keep valid `push/replace/pop/back`, or disallow custom Request mode entirely. Do not infer.
R4. `retain` missing paths: fail required mapping vs preserving explicit null, whether optional mappings exist; never silently drop malformed keys.
R5. Recoverable action error: keep form visible and safe sanitized message, no raw network text. Confirm retry/dismiss clearing semantics.
R6. Domain DynamicRequest fields/defaults and backend destination schema method/auth parity: keep until explicit contract-level reconciliation.

## 6. Small gated implementation waves after plan freeze/authorization
1. Add red tests for precise approved Request semantics; don't delete valid tests.
2. Narrow Request handler normalization/strict interpretation; first run `:feature:dynamic:jvmTest`.
3. Narrow Store request validation ownership/error recovery; `:feature:dynamic:jvmTest`.
4. DTO/Repository edits only when source-backed necessary; `:data:jvmTest`.
5. Full `:domain:jvmTest :data:jvmTest :navigation:jvmTest :feature:dynamic:jvmTest :webApp:wasmJsBrowserDevelopmentWebpack`, `git diff --check`. Android/Web smoke later. No iOS simulator on Windows.
6. Explicit source reconciliation/doc freeze after validated builds.

## 7. Stop rules
No Kotlin/backend edits while plan DRAFT. Do not change Sequence placeholder or unrelated state/present/dismiss implementations. No new modules, managers, factories, validators, duplicate resolver, HTTP client or ViewModel. If actual backend response conflicts with frozen frontend destination, stop and report rather than invent adaptation.
