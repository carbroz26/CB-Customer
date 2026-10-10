# Dynamic UI Implementation Plan

Status: FROZEN — implementation contract
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
- DYNAMIC-ARCHITECTURE.md
- DYNAMIC-ARCHITECTURE.md
- DYNAMIC-IMPLEMENTATION-PLAN.md
- obsolete `docs/dynamic/*` documents listed in the architecture audit
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


## 21. Source-by-source reconciliation contract — 2026-10-07

This section is mandatory. It maps the inspected source to the frozen target. Implementation is incomplete until every item is satisfied.

### 21.1 Store
Current violations:
- constructs DynamicFormValidator internally;
- decides validation from raw action payload;
- combines loading, action jobs, screen reduction, runtime-state application and effect conversion;
- exposes RuntimeState directly;
- stores Context as a snapshot property;
- branches on DynamicActionResult variants.

Target:
- DynamicStore remains the Pure MVI orchestration boundary; do not create managers for trivial responsibilities.
- Inject DynamicFormValidator as a pure capability.
- Store may coordinate generic validation requested by an action, but validation rules remain only in DynamicFormValidator.
- Store may reduce action results into canonical state/effects; it must not implement request/navigation/URI capability details.
- RuntimeState mutation occurs only through DynamicRuntimeState.
- Store exposes canonical DynamicState and one-shot DynamicEffect.
- No navigation object, repository, HTTP client, renderer or platform launcher is owned by Store.

### 21.2 Screen state
DynamicScreenState is immutable and belongs to exactly one navigation entry. It contains the screen document, typed bindings and validation information. Loading/failure remain part of the surrounding MVI state.
It must not contain RuntimeState, navigation stack, executor, repository or mutable Context.
There is no second BindingState source of truth.

### 21.3 RuntimeState
DynamicRuntimeState is the sole retained Dynamic flow-state capability:
- read current typed tree;
- resolve nested paths;
- apply typed changes.
Change sets should be applied atomically rather than emitting one intermediate state per retained key.
No frontend-defined business namespace or business-specific API is allowed.

### 21.4 Context
Context is immutable/read-only typed application composition data. Dynamic never mutates or persists it. Only platform, appVersion, buildNumber and deviceId are supplied by the current frozen runtime metadata contract.

### 21.5 Reference resolver
DynamicReferenceResolver is the only path traversal implementation. Exactly these namespaces are supported:
- $binding
- $context
- $state
- $literal
$response is not a namespace and must not appear in the supported namespace set.
The resolver preserves DynamicValue types and recursively resolves ObjectValue/ArrayValue children.

### 21.6 Action executor and handlers
DynamicActionExecutor only normalizes, validates duplicate registration, dispatches and sequences actions with a finite depth boundary. It never mutates navigation, performs HTTP or retains response data.
DynamicActionHandler implements one capability and returns DynamicActionResult. It never mutates navigation or platform state.
RequestActionHandler resolves generic request metadata/body, calls DynamicRepository, extracts only explicitly configured retain mappings and returns transient state changes/navigation instructions.
StateActionHandler writes arbitrary server-defined RuntimeState keys using typed DynamicValue.
Navigate/Present/Dismiss return navigation instructions only.
ExternalUri returns an external URI effect only.

### 21.7 Sequence semantics
Each state-producing action in a sequence must feed its state changes into the state snapshot used by subsequent actions. Typed values, navigation result and explicit state changes must be preserved. Sequence depth protection remains deterministic.

### 21.8 Navigation
NavigationState is the only stack owner. DynamicNavigationEntry remains identity-only and contains only DynamicDestination.
DynamicContainer is the effect/application boundary. No action handler mutates DynamicNavigationState.
Normal back is pop. Returning to a prior entry must reveal its existing screen state without a destination-driven reload.

### 21.9 Entry-scoped Store lifecycle
Navigation entry identity, not destination equality, is the Store lifecycle key. No global Store map, DynamicScreenCache, DynamicNavigationStack or destination-keyed cache is allowed.
If Navigation 3 requires an existing entry-scoped state-retention decorator to preserve entry composition/state while the entry remains on the back stack, use that existing navigation mechanism rather than creating Dynamic infrastructure.

### 21.10 Rendering
DynamicRenderer validates screen/destination identity, resolves template.type through DynamicRegistry, constructs DynamicRenderContext and delegates. No concrete template branch exists in the root renderer.
Rendering follows Template → Component → Section → Group → Element → Content where applicable.
Renderers do not execute actions, call repositories, mutate RuntimeState or mutate navigation.

### 21.11 Registry
Separate template/component/section/group/element/content registries remain behind DynamicRegistry. Duplicate normalized keys fail deterministically. No silent replacement.

### 21.12 Domain/data/network
Domain remains generic. DynamicDestination is identity only.
Data owns Dynamic repository and mapping; mapper preserves String/Number/Boolean/Object/Array/Null and uses injected Json.
Network owns HTTP infrastructure and canonical authentication/metadata/logging behavior. Dynamic creates no HttpClient.

### 21.13 Generic fixture
The authoritative fixture must demonstrate bootstrap destination, screen document, binding, context, request, transient response, explicit retention into RuntimeState, next destination, $state lookup, normal back restoration, generic navigation, external URI, validation and typed values.
No $response or business-specific namespace is permitted.

### 21.14 Final source audit
Before final build/test inspect every Dynamic commonMain file, every Dynamic test, navigation integration, application composition/DI, Dynamic domain/data contracts and mapper, then search for all forbidden patterns and review the complete diff against this plan. A passing build does not override an SRP/boundary violation.

### 21.15 Implementation order
Implement sequentially: runtime/reference/value contracts → screen state/Store → action executor/handlers → navigation integration → renderer/registry → domain/data → composition/DI → tests/fixtures → docs/status → source audit → final Gradle build/test.
No unrelated architecture may be added.

### 21.16 One-shot effects
DynamicEffect is a one-shot event stream, not persistent UI state. DynamicStore must use a buffered Channel/ReceiveChannel flow (or an equivalent one-shot event mechanism already frozen by the project) so consecutive navigation/external-URI effects are not collapsed by StateFlow semantics.
DynamicState remains the only persistent screen state.

### 21.17 Generic navigation defaults
Generic navigation that creates a destination entry uses Push by default. Replace is explicit (mode = replace), and Pop is explicit (mode = pop/back). Request actions that receive a destination follow the same rule. This is required so normal back reveals the previous Navigation 3 entry with its existing screen state.

### 21.18 $response rejection tests
Tests must not use $response as a supported reference or fixture value. A focused test that supplies $response and asserts deterministic rejection is required and is allowed.

### 21.19 Navigation-version boundary
The Dynamic implementation must not silently upgrade Navigation 3 or introduce a Dynamic-specific retention/cache mechanism. Entry-scoped state retention is delegated to the existing Navigation 3 entry lifecycle. A Navigation 3 dependency/version change is a separate base-architecture decision and must not be bundled into Dynamic implementation.

## 22. UI contract reconciliation amendment — 2026-10-08

This section is mandatory for the Dynamic rendering/UI implementation. It resolves the remaining UI-model, property, responsive-layout, element, and renderer-boundary ambiguity. It does not reopen the frozen runtime architecture.

### 22.1 Frozen hierarchy
The Dynamic UI hierarchy is fixed and must not be changed:
Template → Component → Section → Group → Element
Element content remains a separate capability: Element → Content.
Do not flatten these levels, replace them with a universal node model, or introduce an alternative generic hierarchy.

### 22.2 Template contract
A Template owns identity/type, applicable semantic layout properties, and ordered Components. Properties must retain stable semantic meaning. If a type supports both orientations, it must expose meaningful arrangement/alignment properties for those orientations rather than silently reusing an unrelated property. A type with fixed semantic direction may derive direction from its type semantics.

### 22.3 Component contract
A Component owns identity/type, applicable semantic layout properties, and ordered Sections and/or Elements according to the frozen JSON contract. StackComponent may define orientation, arrangement/alignment, padding and applicable sizing/fill properties. Do not force every visual property onto every component type.

### 22.4 Section contract
A Section owns identity/type, applicable semantic layout properties, and ordered Groups and/or Elements. StackSection may define orientation, arrangement/alignment, padding and applicable sizing/fill properties. Horizontal arrangement must not be represented by silently reusing a vertical-arrangement property.

### 22.5 Group contract
A Group is the primary styled/container node. StackGroup may own orientation and applicable arrangement/alignment, padding, width/height constraints, fill behavior, background, border, shape, and ordered Elements. Group-owned properties are applied to the Group container before its children are rendered.

### 22.6 Concrete Element contract
There is no generic first-class DynamicElement abstraction in the target model. Each element has its own concrete domain model, mapper and renderer: TextElement, ImageElement, InputElement, ButtonElement and IconElement. Runtime casts from a generic DynamicElement into concrete element types are forbidden.

### 22.7 TextElement contract
TextElement may own text, spans, fontSize, fontWeight, letterSpacing, lineHeight, color, textAlign, applicable width/max-width/fill behavior, applicable padding, semantic role, supported content and actions. Typography remains design values; do not introduce screen-width-based font scaling. Text wraps naturally unless the contract explicitly requires otherwise.

### 22.8 ImageElement contract
ImageElement may own source, width/height, maxWidth/maxHeight, fill behavior, contentScale, applicable padding, semantic role, supported content and actions. The existing maxWidth plus fillMaxWidth pattern is an approved responsive pattern.

### 22.9 InputElement contract
InputElement may own placeholder, keyboardType, maxLength, weight, validation, binding, presentation, applicable sizing/fill properties, applicable padding, semantic role and supported content/actions. If JSON supplies weight, the renderer must honor that model value; it must not replace it with an unrelated hard-coded weight.

### 22.10 ButtonElement contract
ButtonElement may own text, height, width/fill behavior, fontSize, fontWeight, textColor, background, shape, applicable padding, semantic role, supported content and actions. Button visual styling belongs to ButtonElement and must not be duplicated or silently overridden by common layout code. Fixed design heights such as 56dp remain valid.

### 22.11 IconElement contract
IconElement may own name, size, color, applicable padding, semantic role and supported content/actions. Do not invent stretch, percentage or screen-relative sizing without a demonstrated requirement. IconContent remains separate.

### 22.12 Common property rules
Common properties are not a universal property object. A property is shared only when its semantic meaning is valid for that node. Applicable common patterns include padding, width, height, maxWidth, maxHeight, fillMaxWidth, fillMaxHeight, alignment, arrangement, background, border and shape. Not every node exposes every property. Missing JSON properties use normal Compose/default behavior.

### 22.13 Property ownership and application order
Properties belong to the node whose JSON properties object contains them. Rendering order is: map JSON into the concrete model → create the node's Compose container/content → apply that node's owned layout/visual properties → render children inside it. Parent properties must not leak into child models. Shared modifier helpers are allowed only as narrow property-application utilities.

### 22.14 Width, height and fill semantics
Width is generally more likely to be adaptive than height. Approved patterns are fixed design dimensions where semantically required, fillMaxWidth/fillMaxHeight where applicable, maxWidth/maxHeight, weight where the parent supports it, natural Compose measurement and normal Row/Column/Box behavior. Do not introduce generic screen-width or viewport percentages merely for consistency.

### 22.15 Responsive UI philosophy
Responsive behavior is provided primarily by standard Compose layout behavior: backend meaningful constraints → concrete domain model → node-owned property application → standard Compose layout → natural responsive behavior. Do not introduce ConstraintLayout, a custom responsive engine, screen-width typography scaling, or scattered width calculations. BoxWithConstraints/available-size inspection is allowed only when a real layout requires composition to change based on available size.

### 22.16 OTP sizing decision
The current OTP contract remains six segments, 44dp segment width, 8dp spacing and 52dp segment height. Do not change the JSON contract during this refactor. Test actual narrow devices first. If adaptation is proven necessary, prefer internal OTP renderer/layout adaptation over global screen-percentage properties.

### 22.17 Element mapper boundary
Target flow: JSON element type → concrete element mapper → concrete element domain model. Do not retain a centralized mapper whose primary result is a generic DynamicElement followed by renderer-side casts. Shared parsing helpers are allowed when they reduce duplication without hiding concrete mapping responsibility.

### 22.18 Element renderer boundary
Target flow: ElementRegistry → concrete element renderer → concrete element model. Renderers own only concrete-element rendering and may use narrow common-property helpers. They must not perform network calls, execute actions directly, mutate RuntimeState/navigation, implement reference traversal, or contain business-specific conditions.

### 22.19 Registry/mapper/renderer relationship
Registry normalizes keys, registers concrete renderers, rejects duplicates and performs lookup. Mapper converts transport JSON into the correct concrete model and preserves DynamicValue/reference semantics. Renderer renders the mapped model, applies node-owned properties, delegates children/content through the appropriate registry and emits generic UI events through DynamicRenderContext. No registry maps, no mapper renders, and no renderer maps raw transport JSON.

### 22.20 Required UI contract tests
Tests must cover concrete element mapping without generic DynamicElement casts, element padding, InputElement weight honoring the mapped value, width/maxWidth/fill behavior, node-owned property application, type-specific property ownership, horizontal/vertical arrangement semantics without property-name reuse, maxWidth plus fillMaxWidth behavior, fixed design dimensions, current OTP dimensions, and registry dispatch to each concrete element renderer.

### 22.21 UI implementation scope
This rendering refactor is limited to the UI contract above plus the already frozen runtime/store/action/navigation work in this plan. It must not introduce a new Dynamic architecture, second navigation system, Dynamic network/repository layer, ViewModel, universal property model, custom responsive engine, or unrelated UI cleanup.

### 22.22 UI source completion rule
Before implementation is complete, every Template, Component, Section, Group, Element, Content, mapper, registry and renderer source file must be checked against this section. A compiling implementation that still contains generic DynamicElement casts, ignored mapped properties, semantic property reuse, or renderer-level business logic is incomplete.

### 22.23 Contract authority
This amendment is part of the frozen implementation plan. Where older discussion, source comments, implementation shortcuts or stale fixtures conflict with this section, this section and the authoritative Dynamic architecture document govern. No code change is authorized merely by this amendment; implementation begins only after the user explicitly freezes this reconciled plan and authorizes implementation.


### 22.24 Local asset fallback amendment — 2026-10-10

Explicitly authorized implementation amendment to the backend-owned asset contract. The user explicitly authorized implementation on 2026-10-10:
- Keep backend `url` required by contract and `fallbackUrl` optional; do not change the backend.
- The existing shared `DynamicAssetImage` tries a non-blank primary URL, then a non-blank backend fallback URL, then receives a caller-selected local fallback painter.
- Bundle separate generic fallback resources for image elements and icon elements/content in `:feature:dynamic` common resources.
- Missing/blank URL values are tolerated by the data mappers so the renderer can show the local fallback instead of throwing.
- Do not add a service, repository, cache, new registry, or other asset infrastructure.
- Add mapper regression coverage for missing URLs. Verify with the existing Gradle test matrix before marking this amendment verified.
