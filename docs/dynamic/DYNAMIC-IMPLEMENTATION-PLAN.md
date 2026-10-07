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
