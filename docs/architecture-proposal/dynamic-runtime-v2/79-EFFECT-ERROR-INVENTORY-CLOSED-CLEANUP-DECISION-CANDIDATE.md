# 79 — Effect/Error Inventory Closed and Minimal Cleanup Decision Candidate

**Status:** OWNER DECISION PENDING — NOT PLAN_FROZEN — NO KOTLIN IMPLEMENTATION AUTHORIZED.
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`. Based on audit 77, reference inventory 78, and owner's repository-wide PowerShell source scan. Backend development read-only; State Action deferred and Sequence inert.

## Repository-wide reference reconciliation
Owner scanned all non-build Kotlin source files for `EffectFailed|reportEffectFailure|deliverEffects|DynamicActionEffect|effectError|effectBatches|Channel<List<DynamicActionEffect>>`.

Confirmed production:
- `app/src/commonMain/kotlin/com/carbroz/cbpartner/app/ApplicationContent.kt`: lines 23,74,76,78–104,133,139–142. Session channel and effectError used by app effect consumer and UI.
- `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/action/DynamicActionResult.kt`: lines 8–71 define and expose Result.effects and typed effects.
- `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/store/DynamicStore.kt`: line 27 declares `DynamicIntent.EffectFailed(message)`, line 59 handles it by `reportEffectFailure(intent.message)`, line 159 defines reducer; lines 47,155 use effect delivery callback. **No other production senders of `DynamicIntent.EffectFailed` found**.
Confirmed tests:
- `feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/store/DynamicStoreTest.kt`: line 142 and 318 are the only two `EffectFailed` intent invocations. Lines 330,345,360 cover result effect delivery / retained state-before-effect.
No other Kotlin reference matches reported in user's scan.

## Correctness judgment
**KEEP:** `DynamicActionEffect`, result/effect separation, state-before-effects ordering, App session channel, Store action failures, App effect failures and their independent UI surfaces. Current singleton/empty result effects are not sufficient reason to change list/batch contract or lifecycles.

**Cleanup candidate C1:** `DynamicIntent.EffectFailed` currently has no production sender; the App effect consumer handles its own error and does not route effect failures to a Store. Consequently the Store's `reportEffectFailure` is an unreachable production path under present wiring, though deliberately exercised by tests. Removing C1 would simplify unused Store entry point without moving responsibilities or changing active feature behavior.

## Exact proposed C1 file scope — ONLY if owner approves
1. **MODIFY** `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/store/DynamicStore.kt`: remove `DynamicIntent.EffectFailed`, its `accept` branch, and `reportEffectFailure(message)` method. Leave all action failure/validation, effects delivery, lifecycle and state code untouched.
2. **MODIFY** `feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/store/DynamicStoreTest.kt`: remove exactly the two tests that exclusively call `DynamicIntent.EffectFailed`, after checking the remaining real action-failure tests adequately cover preservation of screen/bindings and sanitized error. Do not erase unique safety assertions without transferring them into a production-reachable action failure test.
3. **NO CHANGE** to `ApplicationContent.kt`, `DynamicContainer.kt`, `DynamicActionResult.kt`, `DynamicActionExecutor.kt`, `SequenceActionHandler.kt`, navigation, State handler/contract, Backend, effect batch / channel type.

## Gates
- Confirm owner wants C1 cleanup, or explicitly KEEP intentional future hook.
- If approved, publish exact PLAN_FROZEN and **await separate Kotlin implementation authorization**.
- Tests first if coverage migration is required; minimal diffs in two files; run `:feature:dynamic:jvmTest :domain:jvmTest :data:jvmTest :navigation:jvmTest :webApp:wasmJsBrowserDevelopmentWebpack`, `git diff --check`, and run source reference scan.
- Local Android/Web interactive smoke still separate and pending. No Gradle test execution in this document preparation.

## Recommendation
Approve C1 minimal removal of the unwired `EffectFailed` intent and reducer, while leaving effective Store/App error handling and all batching behavior untouched. If retaining the placeholder is desired, record that decision and close the audit without implementation.
