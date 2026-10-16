# 80 — C1 EffectFailed Dead-Path Cleanup — PLAN_FROZEN

**Status: PLAN_FROZEN — KOTLIN IMPLEMENTATION NOT AUTHORIZED YET.**
**Owner decision:** 2026-10-16 — C1 minimal cleanup explicitly approved.
**Frontend:** `carbroz26/CB-Partner`, existing branch `architecture/dynamic-runtime-v2-discussion-freeze` only.
**Evidence:** read-only audit 77, repository-wide caller inventory 78, owner-confirmed scan, cleanup decision 79.
**Backend:** `CB-Backend/development` READ ONLY.

## Frozen decision
Remove the Store-only `DynamicIntent.EffectFailed` path, which has no production sender in the owner-confirmed Kotlin reference scan. Retain the actual Store action/validation failure handling, app-owned effect failure reporting, and all effect batching and lifecycle behavior. Do not alter State Action (requirement deferred) or inert Sequence Action.

## Exact two-file Kotlin implementation scope
1. `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/store/DynamicStore.kt` — remove only:
   - `data class EffectFailed(val message: String) : DynamicIntent`
   - `is DynamicIntent.EffectFailed -> reportEffectFailure(intent.message)`
   - unused `private fun reportEffectFailure(message: String)` method.
   Keep `handleAction` failure recovery, `validationMessage` updates, `applyActionResult`, `deliverEffects`, Store lifecycle and cancellation unchanged.
2. `feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/store/DynamicStoreTest.kt` — remove the two tests that send `DynamicIntent.EffectFailed` only **after** reconciling their safety assertions against production-reachable action-failure tests. Move any genuinely unique screen/binding preservation and sanitized error assertion to existing action-failure test(s) within the same file before deleting those test cases.

## Must remain unchanged
`ApplicationContent.kt`, `DynamicContainer.kt`, `DynamicActionResult.kt`, `DynamicActionExecutor.kt`, `DynamicActionEffect` contract, `Channel<List<DynamicActionEffect>>`, all other handlers including inert Sequence, all navigation, DTO/domain/reference-resolver code, Backend. No new manager, error mediator, feature, or abstraction.

## Verification and execution gates
1. **Await separate explicit owner instruction to start implementation.** Approval of C1 and this plan freeze are not Kotlin-edit authorization.
2. Test/coverage reconciliation first, then precise removal within two-file scope. Preserve all active action errors, state-before-effects, cancellation and existing session effect error behavior.
3. Confirm `EffectFailed` and `reportEffectFailure` have zero Kotlin source references after cleanup.
4. Owner-run `./gradlew.bat :feature:dynamic:jvmTest :domain:jvmTest :data:jvmTest :navigation:jvmTest :webApp:wasmJsBrowserDevelopmentWebpack` and `git diff --check`.
5. Record source reconciliation and honest test status; Android/Web interactive smoke remains separately pending. No unrelated warning or dependency cleanup.
