# 73 — Resolver Usage Inventory and Test Parity Review

**Status: READ-ONLY REVIEW; PLAN NOT FROZEN; NO KOTLIN EDITS AUTHORIZED.** Branch `architecture/dynamic-runtime-v2-discussion-freeze`. State Action deferred by owner; inert Sequence unchanged; Backend unchanged. Follows audit 72.

## Source inventory — confirmed Kotlin direct callers

`DynamicActionValueResolver` **production callers**:
- `action/RequestActionHandler.kt`: resolve body, resolve request payload scalar reference via local helper, string/boolean checks, retain path string. Also uses `DynamicReferenceResolver.resolveExistingPath` directly.
- `action/NavigateActionHandler.kt`: resolve dynamic navigation mode + string extraction.
- `action/ExternalUriActionHandler.kt`: resolve typed URI expression.
- `action/StateActionHandler.kt`: read legacy payload.key and resolve payload.value. State semantics deferred, but caller import replacement can be behavior-preserving.
- `PresentActionHandler.kt`, `DismissActionHandler.kt`, `SequenceActionHandler.kt`: no resolver calls.
- `DynamicActionValueResolverTest.kt`: action-wrapper behavior tests.

`DynamicReferenceResolver` **direct production callers**:
- `action/DynamicActionValueResolver.kt`: forwards resolve, string, boolean, resolvePath; asString wraps string.orEmpty.
- `action/RequestActionHandler.kt`: resolveExistingPath distinguishes absent source from explicit NullValue.
- `runtime/DynamicRenderContext.kt`: resolve dynamic render properties, with wrappers string/number/boolean.
- `runtime/DynamicRuntimeState.kt`: resolvePath nested reads.
- `store/DynamicStore.kt`: resolve Request validate flag, before form-validation/network.
- `runtime/DynamicReferenceResolverTest.kt`: direct tests.

No use of wrapper `asString` or wrapper `resolvePath` was found among the examined action handlers; **repository-wide negative claim remains unverified**, because GitHub code search returned incomplete zero matches and exhaustive fetch of all 154 Kotlin files exceeded connector tool-call limits. Do not delete those methods based solely on this negative evidence. A local whole-repository reference scan is the remaining completeness gate.

## Exact method parity
| Forwarder | Canonical delegate | Semantics |
| --- | --- | --- |
| `DynamicActionValueResolver.resolve` | `DynamicReferenceResolver.resolve` | Same reference recursion and namespace checking |
| `...string` | `...string` | Typed `StringValue` only; null otherwise |
| `...boolean` | `...boolean` | Typed `BooleanValue` only; null otherwise |
| `...resolvePath` | `...resolvePath` | Nested path, missing indistinguishable from present NullValue |
| `...asString` | `string(value).orEmpty()` | Converts missing/wrong-type to empty string; no direct canonical equivalent needed unless real caller identified |

`DynamicReferenceResolver.resolveExistingPath` is **not** identical to resolvePath: missing -> Kotlin null, explicit `DynamicValue.NullValue` preserved. Request `retain` uses it for correctness, so leave untouched. Similarly `$literal` in canonical resolver currently recursively evaluates nested reference objects; preserving this behavior is mandatory, not a proposal to redefine literals.

## Test parity review
`DynamicActionValueResolverTest.kt`:
1. `resolvesTypedBindingsWithoutStringCoercion`: typed string/number/boolean in nested object;
2. `resolvesNestedContextAndState`: nested context/state paths;
3. `responseReferenceIsRejected`: unsupported `$response` fails;
4. `resolvesLiteralRecursively`: literal wrapping object containing a binding resolves recursively.

`DynamicReferenceResolverTest.kt`:
1. `resolvesNestedTypedStateWithoutStringification`: numeric and boolean state;
2. `rejectsResponseNamespace`: no `$response`;
3. `rejectsUnknownReferenceNamespace`;
4. `preservesObjectReferencesAsObjectValues`: object-valued references remain objects.

Overlap exists for typed-state and response rejection, but wrapper-only tests add distinct typed bindings, nested context and literal-recursion coverage. **Never delete test coverage without migrating the non-overlapping assertions.** Add/retain path-missing vs explicit-null tests (particularly `resolveExistingPath` and Request retention) before cleanup. Existing `RequestActionHandlerTest` verifies actual Request retain; Store tests verify validation, state-before-effect; Executor tests verify cancellation, duplicate registration, inert Sequence.

## Design assessment and proposed narrow plan for future freeze
1. Canonical resolver `runtime/DynamicReferenceResolver.kt`: **KEEP unchanged**.
2. `action/DynamicActionValueResolver.kt`: **REMOVE candidate**, contingent on repository-wide scan showing no other callers and source visibility/integration compatibility.
3. `action/{RequestActionHandler,NavigateActionHandler,ExternalUriActionHandler,StateActionHandler}.kt`: change calls/imports only to canonical resolver; do not alter action validation, Backend State contract, or Request behavior.
4. `action/DynamicActionValueResolverTest.kt`: migrate its four unique test behaviors to `runtime/DynamicReferenceResolverTest.kt`, then remove wrapper test file. Keep all assertions, improve path/null tests if needed.
5. `DynamicStore.kt`, `DynamicActionExecutor.kt`, `DynamicActionResult.kt`, `DynamicRuntimeState.kt`, `DynamicRenderContext.kt`, `DynamicModule.kt`, `ApplicationContent.kt`, `SequenceActionHandler.kt`: **KEEP**.
6. Effect batching and dual action-error handling: **out of scope** until separate caller/lifecycle audit. No second manager or new abstractions.

## Required final completeness gate before owner PLAN_FROZEN
On the local checkout run a project-root PowerShell scan:
```powershell
Get-ChildItem -Recurse -Filter *.kt -File | Where-Object { $_.FullName -notmatch '[\\/](build|\\.gradle)[\\/]' } | Select-String 'DynamicActionValueResolver|DynamicReferenceResolver|asString\(' | Select-Object Path,LineNumber,Line
```
Review ALL hits, including tests and other modules. Exclude generated build files only. Confirm no special tests/tools refer to the forwarding object by class name. This is read-only. Return the hit list for exact closure.

**No decisions frozen here.** Separate owner decision on consolidation, frozen file list, explicit authorization for Kotlin edits and local full JVM/Web gates remain required.
