# 74 — Resolver Consolidation: Repository Inventory Closed / Exact Plan Freeze Candidate

**Status:** REVIEW CANDIDATE — NOT PLAN_FROZEN — NO KOTLIN IMPLEMENTATION AUTHORIZED.
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`.
**Inputs:** Source audit 72, inventory/test-parity review 73, owner's local full-Kotlin-file PowerShell match output (2026-10-16). State Action behavior deferred. Sequence inert. Backend unchanged.

## Closed reference inventory

Local repository-wide scan reported all matching Kotlin files under `:feature:dynamic`, with these exact wrapper production consumers:

1. `action/RequestActionHandler.kt`: `resolve`, `string`, `boolean` (body, scalar parameters and retain path).
2. `action/NavigateActionHandler.kt`: `resolve`, `string` (mode).
3. `action/ExternalUriActionHandler.kt`: `resolve` (URI).
4. `action/StateActionHandler.kt`: `resolve`, `string` (legacy key/value; do not change functionality).

Canonical resolver direct consumers: `action/RequestActionHandler.kt` presence-aware retained path; `runtime/DynamicRenderContext.kt`; `runtime/DynamicRuntimeState.kt`; `store/DynamicStore.kt`. Tests: `action/DynamicActionValueResolverTest.kt` and `runtime/DynamicReferenceResolverTest.kt`.

Search included `DynamicActionValueResolver|DynamicReferenceResolver|asString\(` and excluded build/.gradle folders. Wrapper's `asString` and `resolvePath` had declaration matches only, no reported external usages. Result supports removing the whole forwarding object; the wrapper is `internal`, canonical object also `internal`, with direct callers in same module. This scan does not claim external reflective/non-Kotlin consumers, for which there is no known evidence.

## Exact implementation file list after separate freeze and authorization

| File | Action |
| --- | --- |
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/action/DynamicActionValueResolver.kt` | REMOVE forwarding-only object |
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/action/RequestActionHandler.kt` | REPLACE wrapper references with direct canonical resolver references; retain current `resolveExistingPath` and request validation/default semantics |
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/action/NavigateActionHandler.kt` | Direct canonical resolver calls, identical behavior |
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/action/ExternalUriActionHandler.kt` | Direct canonical resolver call, identical behavior |
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/action/StateActionHandler.kt` | Direct canonical resolver calls **only**, no state wire/behavior implementation |
| `feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/action/DynamicActionValueResolverTest.kt` | TRANSFER four existing assertions/coverage into canonical resolver tests, then REMOVE wrapper test |
| `feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicReferenceResolverTest.kt` | EXTEND: typed bindings, nested context/state, literal recursive resolution, rejection of response namespace, and missing-vs-explicit-null path semantics; do not delete unique existing tests |

**KEEP untouched:** canonical `DynamicReferenceResolver.kt`; `DynamicStore.kt`, `DynamicRuntimeState.kt`, `DynamicRenderContext.kt`, `DynamicActionExecutor.kt`, `DynamicActionResult.kt`, `DynamicModule.kt`, app consumer, all unrelated handlers, Backend and inert `SequenceActionHandler.kt`.

## Preservation gates
- No replacement that turns a missing path into real NullValue or vice versa.
- No changed `$literal` nested resolution behavior, no `$response` support, no stringification or type coercion.
- No changes to Request malformed-mode/retain-before-HTTP handling; state-before-navigation effect; cancellation.
- No refactor of effect lists/batch channel, top-level back alias, or deferred State/Present contracts.
- **Test-first**: transfer unique resolver assertions before removing wrapper and calls. Review diff for semantic equivalence.
- Owner-run `./gradlew.bat :feature:dynamic:jvmTest :domain:jvmTest :data:jvmTest :navigation:jvmTest :webApp:wasmJsBrowserDevelopmentWebpack` and `git diff --check`; Android/Web smoke if relevant.
- Freeze only with explicit owner approval; Kotlin edits only with separate implementation authorization according to project gates.
