# 75 — Resolver Consolidation: Owner-Approved Plan Frozen

**Status: PLAN_FROZEN — Kotlin implementation NOT YET AUTHORIZED.**
**Owner approval:** 2026-10-16 — explicit approval of exact freeze candidate 74.
**Frontend:** `carbroz26/CB-Partner`, branch `architecture/dynamic-runtime-v2-discussion-freeze`.
**Authority:** Audits 72 and 73, owner local repository-wide PowerShell reference inventory, candidate 74. Backend `CB-Backend/development` read-only.

## Frozen choice

Remove only the redundant `DynamicActionValueResolver` forwarding object and migrate its four existing action-handler callers directly to the already-present `DynamicReferenceResolver`. This is structural consolidation, **not** new behavior. Preserve all typed resolution semantics including missing-vs-explicit-null, nested `$literal` behavior, and rejection of `$response`. Do not add or change State Action's deferred targetId/operation/property contract. Leave `SequenceActionHandler` registered and inert.

## Exact seven-file scope

1. **REMOVE:** `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/action/DynamicActionValueResolver.kt` after confirming zero remaining callers.
2. **MODIFY:** `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/action/RequestActionHandler.kt` — swap forwarding calls for canonical resolver calls only; preserve all Request R1–R6 contracts and retention handling.
3. **MODIFY:** `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/action/NavigateActionHandler.kt` — swap resolver calls only.
4. **MODIFY:** `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/action/ExternalUriActionHandler.kt` — swap resolver calls only.
5. **MODIFY:** `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/action/StateActionHandler.kt` — swap resolver calls only; State behavior remains deferred.
6. **REMOVE:** `feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/action/DynamicActionValueResolverTest.kt` only after migrating its unique assertions.
7. **EXTEND:** `feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicReferenceResolverTest.kt` — preserve typed binding values, nested context and state, recursively evaluated literal references, unsupported `$response`, object types and missing-vs-explicit-null presence-aware paths.

## KEEP / defer

Keep `DynamicReferenceResolver.kt` implementation unchanged, along with `DynamicStore.kt`, `DynamicRuntimeState.kt`, `DynamicRenderContext.kt`, `DynamicActionExecutor.kt`, `DynamicActionResult.kt`, `DynamicModule.kt`, `ApplicationContent.kt`, navigation, all remaining handlers, and Backend. Do not simplify batch/effect architecture or remove top-level back alias as part of this slice.

## Implementation gates

1. **Separate explicit owner instruction is required to start Kotlin edits.** This frozen document approves the plan only.
2. Test-first: transfer unique behavior assertions to canonical test class; add null/missing presence test; verify tests before deleting wrapper.
3. Replace exactly four handler call sites, confirm project-wide reference inventory zero for old wrapper, then remove old wrapper and wrapper-only test. Minimal coherent commits, same branch.
4. Local owner verification: `./gradlew.bat :feature:dynamic:jvmTest :domain:jvmTest :data:jvmTest :navigation:jvmTest :webApp:wasmJsBrowserDevelopmentWebpack` and `git diff --check`. No assertions of tests being GREEN until logs.
5. Source reconciliation and owner review before status/acceptance freeze. Android/Web runtime smoke independently tracked. Sequence inert and Backend unchanged.
