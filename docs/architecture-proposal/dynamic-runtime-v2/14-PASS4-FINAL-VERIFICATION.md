# 14 — Pass 4 Final Source-and-Test Verification

**Status:** Source/test reconciliation performed; implementation plan DRAFT, NOT FROZEN. Static verification only: no Kotlin source modifications, Gradle runs, device tests, or demonstrated green test results.

## Evidence reviewed
Production: all nine action package files (executor, result, six concrete handlers, value resolver); reference resolver, runtime state, validator, screen state and Store; DynamicContainer. Cross-boundary: LoadDynamicScreenUseCase, navigation host, navigation entry/state, DynamicRenderer, DynamicRenderContext and app composition. Existing tests: DynamicActionExecutorTest, DynamicActionValueResolverTest, RequestActionHandlerTest, DynamicReferenceResolverTest, DynamicRuntimeStateTest, DynamicStoreTest, DynamicNavigationStateTest. Grounded owner fixtures: Login/OTP and canonical JSON doc 12.

## Verified tests and gaps

| Subject | Existing tests demonstrate/assert | Missing V2 acceptance tests |
|---|---|---|
| Executor | duplicate handlers rejected; nested sequence; cancellation prevents later execution; overlapping dotted patches; retaining state with completed/external effect; nested navigate/present/dismiss/external effects in order | Typed handler adapter, invalid nested action, depth 31/32/33, partial failure, multiple navigation effects at host, action-result errors |
| Resolver | binding numeric/bool retained without stringification, nested context/state, literal recursion, reject $response and unknown namespace | Present vs missing null, empty/ambiguous path, arrays, multiple namespaces, exact-key vs dotted precedence |
| Request handler | response-mode none completion; typed binding/context/state in body; data.retain and navigation | Missing retain path must fail, no silent null; HTTP/application error, auth, duplicate tap, non-POST, CancellationException |
| Runtime State | nested dotted get/set, sibling preservation, object/array/null fidelity | parent/child write conflicts, deletes/replacements, invalid path, session reset and concurrent updates |
| Store | typed binding submitted; retained response state; error state preserves screen payload; ordered sequence effects; independent stores for entries | Actually visible controls on action error, refresh, terminal-effect execution cancellation, disposal, validation inside sequence, real loaded screen identity |
| Navigation State | push same destination yields two distinct object entries, pop restores original, replace, independent modal present/dismiss and back priority | stable explicit entryId, popTo, refresh, return C→A, host delivery after stack mutation, app/browser back |
| Screen renderer | checks IDs/types but returns blank on mismatch; uses drawWithCache gradient | Fail-before-render and safe error UI, gradient stop fidelity and no unnecessary Canvas; see Pass 5 |

**Important distinction:** These are observations about test assertions, not claims they passed in this session.

## End-to-end caller graph grounded in source

```text
ApplicationContent [creates navigation + runtimeState + renderer registry + DynamicStore]
 -> DynamicNavigationHost [NavDisplay + separate Dialog presentation]
 -> DynamicContainer [remember(entry), Load/DisposableEffect, effect collector]
 -> DynamicStore [entry-scoped binding, job control, validator, executor]
   -> LoadDynamicScreenUseCase [DynamicRepository.fetch(DynamicRequest(endpoint))]
   -> DynamicFormValidator [walk template/components/elements/sections/groups]
   -> DynamicActionExecutor [handler selection OR raw sequence recursion]
      -> RequestActionHandler -> DynamicRepository.fetch
      -> NavigateActionHandler
      -> ExternalUriActionHandler
      -> PresentActionHandler
      -> DismissActionHandler
      -> StateActionHandler
   -> DynamicRuntimeState [shared session state patches]
 -> DynamicContainer effect collector [navigation push/replace/pop; modal; URI]
 -> DynamicRenderer [data/template/destination identity check; render via Registry]
 -> DynamicRenderContext [reference resolution and delegated child render calls]
```

Consequences: Store only knows entry.destination for loading; action handlers never mutate navigation directly. Navigation host correctly has a separate modal Dialog. But Container directly interprets effects, and its failure screen hides underlying controls even when DynamicScreenState is retained. Distinguish the new host effect adapter from action dispatch; no extra independent navigation stack.

## Severity and final planning decisions

**P0 — must fix in V2:**
- DynamicRenderer silently returns blank for mismatched/missing response.data; move identity/schema validation into screen-loading boundary and render typed error.
- RequestActionHandler retains missing paths as NullValue or skips malformed mappings; define strict required/optional mapping.
- Store decides request validation using `action.payload.validate` only at top level. A nested request in sequence may bypass validation; enforce validation at action-execution boundary before HTTP for every RequestAction.
- Action failures turn Store into Failure despite retained screen, while Container renders only error text; preserve ready screen and show actionable errors.
- Store no forced refresh after first response; provide distinct Refresh intent and clear preserve/reset binding policy.
- Typed action decoding must preserve existing `external_uri`, `present`, `dismiss`, `state`, `sequence`, and current five event keys; no JSON redesign.

**P1 — required correctness:**
- Keep single resolver, remove DynamicActionValueResolver delegating wrapper; separate missing vs explicit null.
- Keep exact ordering and continue-after-navigation effect during sequences; scope child action results into one ActionOutcome. Original executor computes final state delta and effect list and does not break on terminal effects.
- Explicit sequence failure policy: in current implementation child failure throws before final ActionOutcome return, so accumulated effects/state are *not published by Store*. Network effects already executed cannot be rolled back. Proposal is keep fail-fast publication semantics, but owner choice remains needed if partial publication intended. This is not a silent change to the agreed continue-after-successful-effects rule.
- Multiple host effects (e.g. Navigate, Present, Dismiss, Navigate) may mutate/replace composing entry and cancel its collector partway through list, meaning ordered effects can be lost at host even if executor preserved all. **Design delivery coordinator at navigation/session scope or an explicitly acknowledged queue**, and test against navigation-induced disposal; do not create duplicate navigation authority.
- Store channel is entry-scoped; any effects pending when the entry is disposed can be dropped. Distinguish deliberate cancellation from guaranteed delivery and document chosen semantics.
- Current SequenceActionExecutor directly accesses DynamicRuntimeState.applyChanges and performs its own destination decode; typed dispatch/one patch implementation should remove these cross-owner responsibilities.
- RendererContext currently both resolves values and traverses category renderers. Assign a renderer orchestration helper separate from pure resolve context if actual SRP demands; avoid unnecessary speculative split.

## Target execution + lifecycle, clarified

```text
Typed ActionMapper (from fixed JSON)
  -> DynamicStore receives event
  -> ExecutionContext(binding snapshot + approved context + retained snapshot + validation service)
  -> ActionExecutor (dispatch typed handler)
     -> RequestActionHandler (validation precondition, resolve, repository, retention)
     -> StateActionHandler (emit patch)
     -> NavigateActionHandler / Present / Dismiss / ExternalUri (emit effects)
     -> SequenceActionHandler (nested typed dispatch; local state updates; collect ordered effects)
  -> ActionOutcome(statePatch, ordered effects)
  -> Store commits session state patch
  -> session-safe effect delivery boundary
  -> navigation/URI host consumes outcomes in guaranteed defined order
```

**Validation rule:** A request with `validate=true` must validate the current typed screen regardless of whether it is clicked directly or executed inside any number of nested sequences. This requires a dedicated action execution precondition or Store/Executor collaboration, *not* hardcoded parsing of payload in Store.

**No dependency cycle:** SequenceActionHandler may accept a narrow `executeChild` callback/service bound when executor is assembled; do not inject the concrete executor into a handler while executor constructor injects the handler in a cycle. Prefer an executor-owned compositional function if it actually makes the smallest single-owner implementation; a separate class should not become a wrapper.

**State scoping:** current Store bindings entry-local; DynamicRuntimeState application-shared via ApplicationContent. V2 RetainedState is explicitly session-scoped; same templateId in different navigation entries still yields distinct bindings. Full session reset clears retained values. Refresh re-fetches same destination with agreed binding preservation policy.

**Action result commit semantics:** before host navigation/effects, Store publishes retained changes. On failure, no new effects are published. In-flight network side effects may already have happened and should never be falsely represented as atomic rollback.

## Exact remaining verification gates before PLAN FROZEN

1. Complete exact typed action wire matrix from existing handler payload fields, names/aliases, including RequestAction, StateAction and SequenceAction nested destination parsing; document all defaults.
2. Define multi-effect delivery semantics on host/session scope and write reliable integration tests for Navigate→Present→Dismiss→Navigate with entry disposal; no double execution.
3. Resolve sequence partial failure and action validation policy definitively in target V2 plan; decide missing retain behavior and refresh preserve/reset semantics.
4. Pass 5 renderer/UI/platform source audit and compile-safe signature review; inspect remaining callback paths and KMP test task availability.
5. Write whole-repository exact create/modify/delete/caller migration manifest and staged compilation/CI/device validation. No source edit until plan frozen.

## Required new tests (not yet executed)
`SequenceActionHandlerTest` for nested types, failure/partial effects, depth/ordering, validation and state patch; `ActionExecutorTest` for typed dispatch/registration; `ReferenceResolverTest` for all four namespaces and missing-null; `RetainedStateTest` for immutable patch/reset; `FormValidatorTest` for all hierarchy branches and field errors; `DynamicStoreTest` for refresh/retained screen failures/disposal; `SessionEffectDeliveryTest` for effect persistence after navigation; `DynamicNavigationStateTest` for popTo+refresh; fixed Login/OTP integration fixture tests.

**Outcome:** Pass 4 source-and-test reconciliation is recorded. It is NOT green runtime verification and is NOT PLAN FROZEN.
