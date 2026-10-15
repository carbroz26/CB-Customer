# 13 — Pass 4: Actions, Runtime State and Pure MVI Store — Source Audit & Target Contracts

**Status:** Pass 4 production-source audit and target contract draft. NOT PLAN FROZEN; no Kotlin code edited. Target-first V2 architecture is authoritative; source is parity evidence. The fixed wire JSON in document 12 is not changed. User-approved existing capabilities must survive.

## 1. Inspected production source
- `feature/dynamic/action/`: DynamicActionExecutor, DynamicActionHandler, DynamicActionResult, DynamicActionValueResolver, RequestActionHandler, NavigateActionHandler, ExternalUriActionHandler, PresentActionHandler, DismissActionHandler, StateActionHandler.
- `feature/dynamic/runtime/`: DynamicReferenceResolver, DynamicRuntimeState, DynamicFormValidator.
- `feature/dynamic/store/`: DynamicScreenState, DynamicStore.
- `feature/dynamic/DynamicContainer.kt`.
- Context from prior passes: DynamicModule registrations, app composition, navigation stack and fixed owner-supplied screen JSON.

**Coverage boundary:** Pass 4 targeted test source, LoadDynamicScreenUseCase, remaining navigation host and renderer callsites require final caller-graph inspection before exact class-by-class migration freeze. No tests or builds run in this audit.

## 2. Source-level findings

| ID | Observed fact | V2 decision |
|---|---|---|
| A01 | `DynamicActionExecutor` normalizes strings/registers handlers and rejects duplicate keys. | Preserve a single registered dispatcher and duplicate rejection; use typed actions/checked adapters. |
| A02 | Executor also decodes `sequence` nested actions from `DynamicValue.ObjectValue`, parses destination, applies state changes and computes recursive dotted-path deltas. | Separate ActionExecutor (dispatch), SequenceActionHandler (ordered orchestration) and focused StatePatch utility; one ActionMapper decodes nested actions at data boundary, not runtime JSON-like decoding. |
| A03 | Executor special-cases `back` alias to `dismiss`. | Preserve wire behavior if confirmed, but decode aliases once at ActionMapper; avoid hidden executor aliases. |
| A04 | Executor depth cap `MAX_SEQUENCE_DEPTH=32`, merges state changes before effects and continues after an effect. | Preserve bounded sequence execution, all successful effects in order and continue-after-effect; specify failure/atomicity policy before freeze. |
| A05 | `DynamicActionResult` has Completed, Navigate, Present, Dismiss, ExternalUri, StateChanged, Sequence. `effects` is a list. | V2 result is uniform `ActionOutcome(statePatch, effects)` with typed effects and ordered delivery. Avoid seven variant-specific state/effect boilerplate if not required by consumers. |
| A06 | Request handler resolves endpoint/method/auth/validate/responseMode/body, executes repository fetch, extracts retained response fields and returns navigation effect. | Decode typed RequestAction in Data; Store performs validation gate; handler invokes repository and returns data/outcome; RetainedValueExtractor owns response path selection; navigation mode typed once. |
| A07 | `RequestActionHandler.extractRetainedState` uses `mapNotNull` for invalid retain path mappings and resolvePath can return NullValue for missing path. | No silent skip of mandatory retained fields. Explicit missing path/optional path policy and tests. |
| A08 | `NavigateActionHandler` and Request handler each parse `mode`, default unknown to Push. | Single typed NavigationModeMapper; unsupported mode must fail rather than silently push. |
| A09 | ExternalUri handler resolves URI and emits effect. | Retain independent ExternalUriActionHandler; strict allowed scheme/host navigation policy, platform handling at host. |
| A10 | Present and Dismiss handlers return distinct modal effects; navigation state keeps presentation separate. | Preserve modal presentation distinct from stack push; dismiss policy explicit for modal vs back. |
| A11 | State handler uses raw key, resolved value, returns path change. | Typed StateAction; one state patch semantics/validation utility. |
| A12 | `DynamicActionValueResolver` forwards every operation to `DynamicReferenceResolver`. | Delete forwarding wrapper after moving all callers to one ReferenceResolver. |
| A13 | `DynamicReferenceResolver` supports $binding/$context/$state/$literal; recursively resolves objects/arrays; rejects unknown/multiple namespaces. | Preserve four references; parse to typed ValueExpression in Data and resolve once in runtime. No $response. |
| A14 | `DynamicReferenceResolver.resolvePath` returns NullValue both for missing path and actual null, and first attempts flat `root[path]`. | Distinguish missing from real null for request/retention; maintain intentional exact-key-vs-dotted semantics with tests. |
| A15 | `DynamicRuntimeState` owns StateFlow and companion setPath/applyChanges; writes dotted paths with implicit intermediate objects. | Session-owned RetainedState store, one StatePatchApplier for immutable deep updates. Reject invalid paths according to typed policy. |
| A16 | `DynamicFormValidator` walks Template→Component→Section/Group→Input, returns FIRST String error; validation uses StringValue + Regex. | Generic traversal for fixed hierarchy, field-keyed ValidationErrors; backend-driven rules; safe regex handling; typed values/empty/null semantics. |
| A17 | `DynamicStore` creates default Dispatchers.Default CoroutineScope, holds load/action Job, MutableStateFlow and buffered Channel. | Explicit entry-owned scope and teardown; distinguish screen state, action status, and durable UI effects. No implicit singleton scope. |
| A18 | Store's `load()` exits when a response is already present, so there is no actual re-fetch path for refresh-on-popTo. | Distinct `Load`, `Retry`, `Refresh`; refresh policy controls binding retention and endpoint. |
| A19 | Store gate `requiresValidation` checks raw action.payload.validate as BooleanValue; dynamic expressions or sequence-contained request validation are not gated. | Typed validation requirement per RequestAction, executor-side precondition through action context or explicit validator port; no bypass through sequences. |
| A20 | Store mutates shared runtime state before emitting effects in order; uses Channel.BUFFERED and swallows ClosedSendChannelException on disposal. | Preserve commit-before-effects, ordered event delivery, cancellation-safe effect boundary; errors after partial effect and host acknowledgement explicitly tested. |
| A21 | Store Failure retains prior screen data but container currently displays only Text(error), hiding existing controls. | Non-destructive Ready state with actionError/fieldErrors for action failure; full-screen failure only for load without content. |
| A22 | Container uses remember(entry) Store, LaunchedEffect load/effects, DisposableEffect close; directly interprets ExternalUri/Navigate/Present/Dismiss and LocalUriHandler. | Keep single DynamicContainer as UI host; move navigation/effect application to a focused host adapter only if it reduces SRP violations and duplication, not speculative managers. |
| A23 | Store allows one active actionJob and rejects repeated taps; captures bindings once before coroutine. | Explicit concurrency policy: duplicate trigger suppression, no stale screen effect after entry disposal, snapshot consistency and cancellation. |
| A24 | Present handler exists, Dismiss handler exists, ExternalUri handler exists, but there is no independent SequenceActionHandler in source. | Preserve all current capabilities and add one focused sequence orchestrator in V2; no removal of external links or modal handling. |

## 3. V2 class responsibility and method contracts

| V2 class / target package | Sole responsibility | Inputs/outputs and forbidden responsibilities |
|---|---|---|
| `domain/dynamic/action/DynamicAction.kt` + typed variants | Immutable action declaration, no behavior | Request, Navigate, ExternalUri, Present, Dismiss, State, Sequence; no Ktor, no Compose |
| `domain/dynamic/action/ActionEvent.kt` (only if needed) | Typed onClick/onLongClick/onValueChange/onFocus/onSubmit event keys | No UI callback execution |
| `feature/dynamic/action/DynamicActionHandler.kt` | Strategy contract, own action type | `suspend handle(typedAction, ActionExecutionContext): ActionOutcome`; no global DI lookup |
| `feature/dynamic/action/DynamicActionExecutor.kt` | Registered dispatch to correct handler, duplicate check | `execute(action,context): ActionOutcome`; no endpoint/body/sequence JSON parsing |
| `feature/dynamic/action/SequenceActionHandler.kt` | Execute nested typed actions in order, threading state snapshots and accumulating effects | Explicit recursion guard; no HTTP implementation or direct navigation mutation; avoid executor↔handler constructor cycle through narrow child executor callback/port |
| `feature/dynamic/action/RequestActionHandler.kt` | Execute one typed RequestAction using repository and return outcome | No raw JSON decode, no navigation stack modification, no repeated mode parsing |
| `feature/dynamic/action/NavigateActionHandler.kt` | Emit a typed navigation effect | Does not mutate NavigationState |
| `feature/dynamic/action/ExternalUriActionHandler.kt` | Emit validated external URI effect | No platform UriHandler in handler |
| `feature/dynamic/action/PresentActionHandler.kt` | Emit typed modal-present effect | No stack push |
| `feature/dynamic/action/DismissActionHandler.kt` | Emit typed dismiss/back effect | No direct stack mutation |
| `feature/dynamic/action/StateActionHandler.kt` | Resolve state operation into state patch | No StateFlow mutation |
| `feature/dynamic/action/ActionOutcome.kt` | State patch + ordered effects/result of one action | No side effects, immutable |
| `feature/dynamic/runtime/RuntimeValues.kt` | Immutable action/read snapshot: bindings, context, session state | No mutable host references |
| `feature/dynamic/runtime/ReferenceResolver.kt` | Resolve parsed typed expressions and paths | No repository or click behavior |
| `feature/dynamic/runtime/RetainedState.kt` | Session-retained state authority with read/commit/reset | No parsing backend retain declarations |
| `feature/dynamic/runtime/StatePatchApplier.kt` | Apply immutable nested updates consistently | No coroutine or effects |
| `feature/dynamic/runtime/RetainedValueExtractor.kt` | Extract declared response paths with required/optional semantics | No HTTP, no navigation |
| `feature/dynamic/runtime/FormValidator.kt` | Traverse typed screen and validate backend-defined rules, return per-binding failures | No hardcoded phone/OTP; no Compose |
| `feature/dynamic/store/DynamicContract.kt` | Typed intent/state/effect definitions | No execution logic |
| `feature/dynamic/store/DynamicStore.kt` | Pure MVI entry orchestration; load/retry/refresh/bindings/validation/execute/commit effects | No Ktor/JSON, no navigation mutation, no Compose |
| `feature/dynamic/DynamicContainer.kt` | Render current Dynamic entry using registry and Store | Does not create standalone transport or independent stack |
| `app/session/DynamicSession.kt` | Own NavigationState, RetainedState and session lifecycle | No concrete screen business logic |

**Avoid overengineering:** StatePatchApplier and RetainedValueExtractor are justified by observed complex path update/extraction responsibilities. If callsites can use a single small pure helper in the owning file without losing clear tests, use a focused function instead of placeholder class. Never add BaseAction/BaseManager/GenericEngine.

## 4. Intended APIs (planning signatures, not compilable source yet)

```kotlin
data class RuntimeValues(
    val bindings: Map<String, DynamicValue>,
    val context: Map<String, DynamicValue>,
    val state: Map<String, DynamicValue>,
)

data class ActionOutcome(
    val stateChanges: Map<String, DynamicValue> = emptyMap(),
    val effects: List<DynamicEffect> = emptyList(),
)

interface DynamicActionHandler<A : DynamicAction> {
    val type: ActionType
    suspend fun execute(action: A, context: ActionExecutionContext): ActionOutcome
}

sealed interface DynamicIntent {
    data object Load : DynamicIntent
    data object Retry : DynamicIntent
    data class Refresh(val bindingPolicy: BindingPolicy) : DynamicIntent
    data class ValueChanged(val key: String, val value: DynamicValue) : DynamicIntent
    data class ActionTriggered(val action: DynamicAction) : DynamicIntent
    data class EffectFailed(val reason: String) : DynamicIntent
}

sealed interface DynamicUiState {
    data object Initial : DynamicUiState
    data object Loading : DynamicUiState
    data class Ready(
        val document: ScreenDocument,
        val bindings: Map<String, DynamicValue>,
        val fieldErrors: Map<String, String>,
        val submitting: Boolean,
        val actionError: String?,
    ) : DynamicUiState
    data class Failure(val reason: String) : DynamicUiState
}
```
Generic handler collections need a **type-safe erased adapter** or validated KClass mapping for Kotlin/JVM/Native/Wasm; `DynamicActionHandler<out DynamicAction>` cannot directly invoke `execute(A,...)` without an adapter. Do not sprinkle unchecked casts.

## 5. Execution and ordering contracts

### Request action
```text
Element event
 -> DynamicStore intent
 -> typed ActionDecoder already created RequestAction
 -> validate (when action's declared policy requires)
 -> immutable RuntimeValues snapshot
 -> DynamicActionExecutor -> RequestActionHandler
 -> ReferenceResolver resolves endpoint/body against snapshot
 -> DynamicRepository.executeRequest (network in data only)
 -> ActionResponseMapper decodes action outcome
 -> RetainedValueExtractor obtains committed path values
 -> ActionOutcome(stateChanges, orderedEffects)
 -> Store commits RetainedState
 -> Store emits effects in original order
 -> Host applies navigation/modal/external URI
```

### Sequence action
- Parse child ActionDto into typed actions in **data**, including nested sequences.
- SequenceActionHandler asks dispatcher to execute each child with locally updated state snapshot, preserving original bindings/context unless semantics require explicit updates.
- Child effects are accumulated in strict execution order; a navigation effect is not an automatic break.
- Depth limit is retained as explicit bounded-recursion safety rule (existing source: 32; target number can remain 32 unless tested requirement says otherwise).
- Return a single ActionOutcome with canonical statePatch plus all effects. Store first commits changes, then publishes effects.
- **Failure policy to freeze:** choose fail-fast/no new effects publication on child failure vs preserving effects successfully generated by earlier children. Current executor fails before returning accumulated effects; retain this unless product explicitly chooses partial success. All network side effects already completed cannot be rolled back; document this caveat.
- Destination/URI effects must not be silently overwritten by later child results; cancellation stops execution and publishes no late navigation.

### Navigation modes
One ActionMapper maps approved literals into typed navigation operation. Current push/replace/pop supported; future popTo(templateId) + refresh requires backend additive policy, distinct destination-vs-entry identity, missing/duplicate target tests. Present remains modal, not push.

### Reference and retention
- `$binding` entry-scoped, `$context` approved app metadata, `$state` session-retained, `$literal` exact backend literal.
- Typed expression objects separate from resolved DynamicValue. Reject multiple/unknown reference namespaces.
- Missing required path is **not** the same as explicit null; formal `Resolved/Missing` outcome or strict exception, depending caller.
- Retain `data.challengeId` and `data.phoneNumber` in fixed fixture style, no implicit previous-response cache.
- Deep patch keys and object replacement/deletion rules must have one shared definition.

## 6. Fixed-JSON compatibility and non-removal

The owner-provided Login/OTP JSON includes request payload with method/endpoint/authentication/validate/body/responseMode/retain; navigate destination; text-span external_uri; $binding/$context/$state/$literal. Preserve wire shape while decoding into typed action models. No OTP-specific handler or screen.

In fixed fixtures:
- Login Input binding is mobileNumber with required regex and maxLength; request retains challengeId/phoneNumber and emits destination.
- OTP screen reads phoneNumber from state text span, segmented Input binding otp, resend request responseMode none, verify request uses retained and binding values; text links emit external_uri.
- Action set also includes present/dismiss/state/sequence in existing source; write separate synthetic fixtures for each without modifying these original user fixtures.

## 7. Migration file matrix (first pass)
| Current file | V2 action |
|---|---|
| `action/DynamicActionExecutor.kt` | Rewrite to typed registered dispatch only |
| `action/DynamicActionHandler.kt` | Rewrite typed strategy contract and adapter |
| `action/DynamicActionResult.kt` | Replace with uniform `ActionOutcome` and typed effects |
| `action/DynamicActionValueResolver.kt` | Delete duplicate wrapper after callers moved |
| `action/RequestActionHandler.kt` | Rewrite one typed request operation |
| `action/NavigateActionHandler.kt` | Rewrite to emit typed navigation effect |
| `action/ExternalUriActionHandler.kt` | Preserve independent external URI capability |
| `action/PresentActionHandler.kt` | Preserve distinct modal presentation |
| `action/DismissActionHandler.kt` | Preserve dismiss/back semantics |
| `action/StateActionHandler.kt` | Rewrite isolated patch creation |
| new `action/SequenceActionHandler.kt` | Move sequence orchestration out of executor |
| `runtime/DynamicReferenceResolver.kt` | Replace with typed `ReferenceResolver.kt` |
| `runtime/DynamicRuntimeState.kt` | Replace with `RetainedState.kt` + focused patch semantics |
| `runtime/DynamicFormValidator.kt` | Rewrite generic field-errors validator |
| `store/DynamicScreenState.kt` | Replace with typed `DynamicContract.kt` |
| `store/DynamicStore.kt` | Rewrite entry Store, refresh, lifecycle and effect semantics |
| `DynamicContainer.kt` | Update Render + effect boundary, no duplicate state authority |
| `data/dynamic/mapper/DynamicActionMapper.kt` | Single typed ActionMapper for all action sources and nested sequences |
| `data/dynamic/mapper/DynamicValueMapper.kt` | ValueExpressionMapper + concrete ValueMapper as in Pass 3 |
| `domain/model/dynamic/action/DynamicAction.kt` | Typed concrete actions in domain/dynamic/action |

## 8. Detailed test matrix

- ActionMapper: full typed variants, case/unknown type, all events, nested sequence, malformed destination and unsupported mode; round-trip fixture integrity where relevant.
- Registry: no duplicate type, unsupported action typed error, deterministic handler selection and no unsafe unchecked dispatch across KMP targets.
- Resolver: four namespaces; exact literal, nested object/array, dotted path, exact flat key, missing vs null, unknown/multiple refs, invalid path/empty ref, no $response.
- State patches: simple/deep paths, overwrites, conflicting parent/child order, object key deletion/replacement, immutable snapshots, concurrent commits/reset, no silent malformed path.
- Validator: template with 1+ components, direct elements, nested Section/Group, multiple inputs, required/regex/malformed pattern, optional blank, segmented, typed values, per-field errors, event-specific validation, property bounds.
- Request: POST correct resolved body, no hidden context fields, auth NONE/BEARER, app failure, missing required retain, responseMode none/destination, state-before-effect, missing destination, cancellation and duplicate taps.
- External URI: $literal/$context, invalid scheme, validation errors, click from text span and Element.
- Modal: present while already presenting, dismiss presentation vs pop, back alias behavior; no accidental push.
- Sequence: empty/missing actions, nested depth boundary, continue after effects, ordered effects, state threading, overlapping patches, early-child failure, late-child failure after effects, cancellation, state commit and no partial unintended navigation.
- Store: initial/load/retry/refresh, return C→A with forced screen reload, preserve/reset bindings policy, validation errors, actionError keeps visible screen, stale completion ignored, scope/disposal safety, buffered effects and host acknowledgement/failure.
- Integration: generic Login fixture -> request+retain -> OTP fixture -> verification, legal link; no production Login/Otp specializations.

## 9. Pending before PLAN FROZEN
1. Inspect every test in action/runtime/store, LoadDynamicScreenUseCase, DynamicNavigationHost and renderer callback callsites; produce exact caller migration map.
2. Reconcile Sequence failure/partial-effect contract and StatePatch semantics with desired product behavior; do not silently replace owner decision to continue after terminal effects.
3. Decide typed handler dispatch implementation avoiding cycles between ActionExecutor and SequenceActionHandler.
4. Specify effect publication reliability/acknowledgement and multiple navigation effects behavior in a single sequence.
5. Confirm action destination popTo/refresh additive wire details, retained lifetime and back/presentation semantics.
6. Finish Pass 5 and produce complete exact file-by-file implementation plan.
