# Dynamic UI Implementation Plan

Status: FROZEN — implementation contract (expanded contract is being applied before code implementation)
Contract source: `docs/dynamic/DYNAMIC-ARCHITECTURE.md`
Implementation rule: only the changes listed here are in scope.

## 1. Navigation entry lifecycle

### `navigation/.../DynamicNavigationEntry.kt` — CREATE
Responsibility: identity of one navigation-stack entry.
Fields:
- `destination: DynamicDestination`
No business state, network state or renderer state.
The class must use object identity so two pushes of the same destination are two independent entries.

### `navigation/.../DynamicNavigationState.kt` — MODIFY
Current: stack of DynamicDestination.
Target: stack of DynamicNavigationEntry.
Methods:
- `currentEntry`
- `currentDestination`
- `setRoot(destination)`
- `push(destination)`
- `replace(destination)`
- `pop()`
Navigation remains the only owner of the stack.
Forbidden: Dynamic screen cache, Dynamic binding/state storage, HTTP.

### `navigation/.../DynamicNavigationHost.kt` — MODIFY
NavEntry key becomes DynamicNavigationEntry. Content callback receives the entry. Back remains `state.pop()`.

## 2. DynamicStore runtime state

### `feature/dynamic/.../store/DynamicScreenState.kt` — CREATE
Responsibility: one navigation entry's mutable MVI screen state.
Contains:
- loaded `DynamicResponse?`
- typed `bindings: Map<String, DynamicValue>`
- validation state
- loading/failure state as needed by the Store
No navigation implementation.

### `feature/dynamic/.../store/DynamicStore.kt` — REWRITE
Keep Pure MVI responsibilities only.
Remove:
- `previousResponseData`
- `asResponseReference()`
- String-only binding map
- String-only context map
- destination mutation used to reuse one store for multiple entries
Target:
- current navigation entry is supplied to the Store;
- typed BindingState;
- read-only typed Context;
- shared generic RuntimeState;
- action execution;
- state/effect reduction.
On navigation to a new entry, the existing entry remains intact. The Store must not reload an entry merely because another entry was pushed.
RuntimeState is shared across the Dynamic flow and is generic `DynamicValue` data.
No business namespace is introduced.

### `feature/dynamic/.../DynamicContainer.kt` — MODIFY
Receive `DynamicNavigationEntry`.
Create/remember one DynamicStore for that entry and dispose it when the entry leaves composition.
Do not keep a global map/cache.
Pass typed bindings/context/state to DynamicRenderer.

### `app/.../ApplicationContent.kt` — MODIFY
Create navigation state only. Do not create one global DynamicStore.
Pass the navigation entry into DynamicContainer.

## 3. Reference resolution

### `feature/dynamic/.../runtime/DynamicReferenceResolver.kt` — CREATE
Single pure resolution capability for:
- `$binding`
- `$context`
- `$state`
- `$literal`
Preserve DynamicValue types and nested structures.
No action execution, network, navigation or UI.

### `feature/dynamic/.../action/DynamicActionValueResolver.kt` — REWRITE
Either delegate to the shared resolver or become the action-specific facade.
Remove `$response` and `previousResponse`.
Typed maps only.
No duplicated path traversal.

### `feature/dynamic/.../runtime/DynamicRenderContext.kt` — MODIFY
Use the shared resolver.
Expose typed bindings/context/runtime state.
Keep rendering helpers and callbacks only.
No state mutation or action execution.

## 4. Action contract

### `action/DynamicActionHandler.kt` — MODIFY
Remove previous response parameter.
Use typed `Map<String, DynamicValue>` for binding/context/state inputs as appropriate.
Handler remains one-capability only.

### `action/DynamicActionResult.kt` — MODIFY
Remove response retention as a Store-level persistent concept.
Add generic state changes using `DynamicValue`.
Request results may carry transient response data only long enough for the Store/reducer to apply explicit retention operations.

### `action/DynamicActionExecutor.kt` — MODIFY
- typed inputs;
- reject duplicate handler types deterministically;
- remove previous response;
- preserve sequence behavior;
- add finite sequence depth guard;
- no navigation implementation beyond invoking the existing navigation capability exposed by handlers.

### `action/RequestActionHandler.kt` — MODIFY
- resolve endpoint/method/body through typed references;
- remove `$response`;
- support explicit generic `retain` mapping from response paths to RuntimeState keys;
- return state changes plus navigation result;
- repository remains the only request boundary.

### `action/StateActionHandler.kt` — MODIFY
Return typed `DynamicValue`, not String.
It can write arbitrary server-defined RuntimeState keys.

### `action/NavigateActionHandler.kt`, `PresentActionHandler.kt`, `DismissActionHandler.kt`, `ExternalUriActionHandler.kt` — MODIFY
Remove previous response and use typed resolver inputs.
Keep exactly one capability each.
Navigation calls remain delegated to `:navigation`.

## 5. Rendering and registries

### `registry/DynamicRegistry.kt` — MODIFY
Keep separate typed registries behind the facade.
Normalize type keys consistently.
Duplicate registrations remain errors.

### `registry/DynamicRendererContracts.kt` — MODIFY only if required by typed context
No business logic.

### `registry/DynamicRendererRegistration.kt` — MODIFY only where current renderer contracts require it
Keep existing template/component/section/group/element/content registrations.

### `renderer/DynamicRenderer.kt` — REWRITE
Remove hardcoded `FORM_TEMPLATE_TYPE`.
Resolve `screen.template.type` through the template registry.
Destination identity checks remain.
Rendering only.

### Existing template/component/section/group/element/content renderers
Keep their current hierarchy. Change only contracts needed for typed state/resolution. No new generic UI hierarchy.

## 6. Domain/data mapping

### Dynamic domain models
Update only models whose contracts require typed state or navigation-entry integration.
Do not create Dynamic-specific repository/network modules.

### Existing DTO mapper/response mapper
Remove assumptions around persistent `$response`.
Preserve generic DynamicValue object/array/number/boolean/string types.
Unknown/malformed element JSON remains a mapper boundary concern.

## 7. Tests

Update existing:
- DynamicStoreTest
- DynamicActionValueResolver tests
- action handler tests
- renderer/registry tests
- mapper/fixture tests
- navigation tests

Remove tests asserting `$response` or `previousResponseData`.

Add tests:
1. String binding.
2. Number/boolean/object/array binding.
3. Context typed values.
4. RuntimeState typed values.
5. Nested `$state` lookup.
6. Binding → request.
7. Context → request.
8. State → request.
9. Request response explicit retention → state.
10. State survives navigation to another entry.
11. Returning by pop does not reload the previous entry.
12. Two equal destinations pushed separately keep independent bindings.
13. Template registry dispatches by template type.
14. Duplicate handler registration fails.
15. Sequence recursion/depth is bounded.
16. `$response` is rejected/unsupported.
17. Object references remain ObjectValue.
18. Generic complete screen fixture maps/renderers correctly.

## 8. Fixture contract

Replace fixture usage of:
`$response`
with explicit retention and `$state`.

Remove business-specific reference paths from generic fixtures.

## 9. Documentation cleanup
Keep:
- `docs/dynamic/DYNAMIC-ARCHITECTURE.md`
- `docs/dynamic/DYNAMIC-IMPLEMENTATION-PLAN.md`
- `docs/dynamic/DYNAMIC-IMPLEMENTATION-STATUS.md`

Delete superseded Dynamic documents after the new authority is present:
- DYNAMIC-ARCHITECTURE-FROZEN.md
- DYNAMIC-DISCUSSION-AND-DECISIONS.md
- DYNAMIC-IMPLEMENTATION-PLAN-FROZEN.md
- obsolete `docs/features/dynamic/*` documents listed in the architecture audit
- dated Dynamic reconciliation/freeze documents that duplicate authority.

## 10. Forbidden changes
- No business-specific state namespaces.
- No external screen cache.
- No DynamicNavigationStack/DynamicScreenCache.
- No duplicate repository/network/navigation/domain modules.
- No ViewModel.
- No ExecuteDynamicActionUseCase.
- No reflection.
- No universal registry.
- No unrelated cleanup.

## 11. Verification
Required before completion:
- Gradle compile/build for applicable modules.
- Dynamic unit tests.
- Navigation tests.
- Mapper/renderer/action tests.
- Final repository search for `$response`, `previousResponseData`, String-only Dynamic binding/context contracts, and hardcoded `form_template` in the generic root renderer.
- Final diff review against this plan.



# 19. Exact code contract — mandatory implementation detail

This section resolves the remaining ambiguity between architecture discussion and source code. A class is not considered implemented merely because a similarly named class exists. Its source code must obey the responsibility and field/method contract below.

## 19.1 DynamicStore exact boundary

DynamicStore may contain only:
- the injected LoadDynamicScreenUseCase;
- the injected DynamicActionExecutor;
- the current entry identity/reference;
- the injected/read-only Context provider;
- the injected/shared DynamicRuntimeState capability;
- one canonical MVI state holder;
- one canonical one-shot effect mechanism;
- coroutine lifecycle owned by this Store.

DynamicStore may expose only generic operations equivalent to:
- accept(Load);
- accept(Retry);
- accept(ValueChanged(key, DynamicValue));
- accept(Action(action));
- effect consumption/clearing;
- close/dispose.

DynamicStore must not contain:
- MutableStateFlow for bindings separate from the canonical state;
- MutableStateFlow for context that DynamicStore mutates;
- previous response fields;
- response-to-reference conversion;
- reference path traversal;
- action-type branching for request/navigate/external URI/state capabilities;
- direct calls to DynamicNavigationState.push/pop/replace;
- URI launcher calls;
- DynamicFormValidator implementation logic;
- renderer calls or renderer selection;
- HTTP calls;
- repository/data-source calls;
- business keys or business namespaces.

The Store only coordinates boundaries and reduces returned results into state/effects.

## 19.2 DynamicScreenState exact model

DynamicScreenState is the canonical state model for one navigation entry. It must be immutable/value-oriented where the existing MVI contract permits it.

It must represent:
- screen/document response;
- typed binding values;
- validation result/state;
- loading state;
- failure state/error information.

It must not represent RuntimeState or navigation stack state. RuntimeState is supplied separately and is never copied into every ScreenState.

There must not be two competing sources of truth such as mutable DynamicScreenState plus independent _bindings/_context/_state MutableStateFlows.

## 19.3 DynamicRuntimeState exact model

DynamicRuntimeState is the only long-lived Dynamic value store. Its public surface is generic:
- values/read-only snapshot;
- get(path);
- apply(changes).

Its implementation may use a typed DynamicValue tree internally. It must support nested object paths without converting values to String.

It must not expose methods named for authentication, OTP, login, booking, session, challenge, token, phone, email, user, etc.

## 19.4 Context exact model

Context is supplied as an immutable/read-only typed map/tree. Dynamic may read it through DynamicReferenceResolver.

The currently frozen runtime metadata is:
- platform
- appVersion
- buildNumber
- deviceId

Context must never become RuntimeState merely because an action executes. Context is environment/application data; RuntimeState is explicit retained flow data.

## 19.5 Reference resolution exact model

There is one path traversal implementation.

Resolution algorithm:
1. detect whether the value is a reference object;
2. accept exactly one supported reference key;
3. select the corresponding root: binding/context/state/literal;
4. traverse nested dot-separated paths for binding/context/state;
5. return the original DynamicValue without stringification;
6. reject unknown namespaces and $response deterministically.

DynamicActionValueResolver must delegate to DynamicReferenceResolver. DynamicRenderContext must use the same capability. No renderer, handler or mapper may implement its own reference traversal.

## 19.6 Action result exact model

Action results are transient. They are not stored as a response object for later reference.

A request handler may return:
- Completed;
- generic StateChanged changes;
- navigation instruction carrying DynamicDestination/back instruction;
- external URI effect.

If a response field must survive, RequestActionHandler converts only the explicitly configured retain entries into DynamicValue state changes. The Store applies those changes to DynamicRuntimeState.

No result may expose a persistent previousResponse property.

## 19.7 Request action exact model

RequestActionHandler is not a mini repository and not a mini network client.

It must:
- resolve endpoint/method/body;
- construct the existing generic request contract;
- call the existing data/repository boundary;
- process the generic response according to responseMode;
- apply explicit retain mappings as state changes;
- return the generic result.

It must not know that a key represents a phone, OTP, challenge, auth token, booking, user or any other business concept.

## 19.8 Navigation action exact model

NavigateActionHandler creates a navigation instruction.

DismissActionHandler creates a dismiss/pop instruction.

PresentActionHandler creates the frozen presentation instruction.

None of these handlers owns the stack. The existing navigation boundary executes the instruction.

ExternalUriActionHandler returns an ExternalUri effect. The platform/application effect consumer performs the actual URI launch.

## 19.9 Validation exact model

DynamicFormValidator is a pure validator. It receives the screen document and typed BindingState and returns validation information. It does not mutate Store state, navigate, execute actions or call a repository.

The Store decides when validation is requested and reduces the returned validation result into canonical MVI state.

## 19.10 Rendering exact model

DynamicRenderer must be unaware of concrete template implementations.

It must perform identity validation, obtain template.type, ask DynamicRegistry for the template renderer, create DynamicRenderContext and render.

TemplateRenderer delegates to ComponentRenderer; ComponentRenderer delegates to SectionRenderer/ElementRenderer according to the actual frozen model; SectionRenderer delegates to GroupRenderer/ElementRenderer; GroupRenderer delegates to ElementRenderer; ElementRenderer renders its concrete element and emits generic events.

No root renderer branch may compare template type to a concrete value such as form_template.

## 19.11 Registry exact model

DynamicRegistry remains one public facade, but its registration and lookup methods remain type-specific. The internal collections are separate by responsibility.

Duplicate registration is an error. A duplicate must never silently replace the first registration.

Registry lookup has no rendering side effects and no business logic.

## 19.12 Composition exact model

DynamicNavigationHost supplies a concrete DynamicNavigationEntry to DynamicContainer.

DynamicContainer owns the lifecycle of the Store associated with that entry's composition. It must not keep a global map of entry IDs to stores and must not use destination equality as the Store key.

When entry A is pushed, entry B is created independently. When B is popped, A is revealed with its existing screen document, bindings and validation state. A is not reloaded solely because B was pushed or popped.

If the same DynamicDestination is pushed twice, the two entries have independent state.

## 19.13 Data mapping exact model

The data mapper is the only place that converts transport JSON into domain Dynamic models.

It must preserve:
- StringValue;
- NumberValue;
- BooleanValue;
- ObjectValue;
- ArrayValue;
- NullValue.

It must never convert an object reference such as {\"$context\":\"profile.displayName\"} into StringValue merely because the object has one property.

The mapper must use the injected kotlinx.serialization Json instance rather than constructing another Json configuration.

## 19.14 Forbidden source patterns

The following are implementation failures and must be removed, not renamed:
- previousResponseData
- previousResponse
- ResponseState used as persistent screen state
- $response reference support
- auth.* / login.* / otp.* / booking.* Dynamic state namespaces
- Map<String, String> for BindingState/RuntimeState/Context where typed DynamicValue is required
- DynamicNavigationStack
- DynamicScreenCache
- DynamicNetworkClient/HttpClient created under Dynamic
- DynamicRepository duplicate of domain/data repository
- ExecuteDynamicActionUseCase
- ViewModel
- reflection-based Dynamic registration
- universal registry mixing all renderer categories
- hardcoded form_template dispatch in the generic root renderer
- direct navigation mutation from an action handler
- direct platform URI launch from an action handler
- sensitive request/response logging
- duplicate authentication header mechanisms

## 19.15 Source-level completion rule

Before final build/test, every class in the Dynamic package must be checked against this plan line-by-line at the source level. If a class still contains a forbidden responsibility, implementation is incomplete even when the application can compile.

The implementation status must list the actual changed files and explicitly identify any intentionally unchanged files with the reason they already satisfied the contract.

## 20. Implementation reconciliation amendments

### 20.1 RuntimeState lifetime
RuntimeState is shared for the active Dynamic flow, not application-global. The current composition root creates one `DynamicRuntimeState` with `remember` for the Dynamic flow and passes it into each entry-scoped DynamicStore. `DynamicModule` therefore does not create a global RuntimeState singleton.

This preserves:
- shared state across Dynamic navigation entries;
- isolation from unrelated application flows;
- no external cache;
- no business persistence.

### 20.2 Navigation execution
DynamicStore and DynamicActionHandler never mutate `DynamicNavigationState`. They return a `DynamicActionResult.Navigate` instruction. `DynamicContainer` is the composition boundary that consumes the one-shot navigation effect and delegates to the existing `DynamicNavigationState`.

### 20.3 Screen-state source of truth
`DynamicState.Success` and `DynamicState.Failure` carry the canonical `DynamicScreenState`. BindingState is read from that state. There is no independent `store.bindings` MutableStateFlow.

### 20.4 Context source of truth
Context is a read-only `Map<String, DynamicValue>` snapshot supplied to the entry Store. The Store does not expose a mutable context flow.

### 20.5 Request response lifecycle
`RequestActionHandler` returns explicit state changes and navigation instructions. It never returns a persistent response reference. `responseMode = none` with no retained values returns `Completed`.

### 20.6 Navigation operation contract
`DynamicActionResult.Navigate` carries a generic navigation operation:
- `Push`
- `Replace`
- `Pop`

The destination is nullable because Pop has no destination. This keeps destination identity separate from navigation behavior.

### 20.7 Nested RuntimeState
RuntimeState supports dotted paths such as `session.reference.id`. State writes are generic and create/update DynamicValue object nodes without converting values to strings.

### 20.8 Shared JSON mapper
`DynamicResponseMapper` receives the existing injected kotlinx.serialization `Json`. It must not instantiate a private Json configuration. `DynamicDataModule` remains the DI owner of the mapper.

