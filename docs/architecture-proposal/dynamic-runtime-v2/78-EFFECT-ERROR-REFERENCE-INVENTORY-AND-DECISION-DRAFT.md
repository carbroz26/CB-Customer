# 78 — Effect and Error Reference Inventory / Decision Draft

**Status: READ-ONLY INVENTORY DRAFT — NOT COMPLETE OR PLAN_FROZEN.** Source branch `architecture/dynamic-runtime-v2-discussion-freeze`; Backend development read-only. Inert Sequence unchanged. Follows audit 77.

## Directly verified current production references
- `app/src/commonMain/kotlin/com/carbroz/cbpartner/app/ApplicationContent.kt` lines 74–104, 133, 139–142: `Channel<List<DynamicActionEffect>>(Channel.UNLIMITED)` remembered by navigationState, session-level `effectError`, channel close in `DisposableEffect`, batch loop and try/catch in `LaunchedEffect`, effect application for URI/navigate/present/dismiss, `deliverEffects = { effects -> effectBatches.send(effects) }`, screen-top dismissible error.
- `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/store/DynamicStore.kt` lines 27,47,59,155,159: `DynamicIntent.EffectFailed(message)` declaration and `reportEffectFailure` reducer, `deliverEffects: suspend (List<DynamicActionEffect>) -> Unit`, state-before-effects dispatch.
- `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/action/DynamicActionResult.kt` lines 8–71: result variants expose zero or singleton effects, four effect variants.
- `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/DynamicContainer.kt`: no hits for the searched effect/error symbols; owns Store screen-level message rendering via `validationMessage`.
- `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/action/RequestActionHandler.kt`: no direct references to searched effect/error symbols; it creates typed results.

## Directly verified test references
- `feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/store/DynamicStoreTest.kt` lines 142 and 318 explicitly send `DynamicIntent.EffectFailed`; lines 330, 345, 360 test `deliverEffects` and effect type, including state retention ordering.
- Read-only inspection of `DynamicActionExecutorTest.kt` in audit 77 verified inert Sequence and cancellation but does not give direct app-channel consumer coverage.

## Confirmed behavioral distinctions
- Executor/Store action failures -> screen-owned `validationMessage`; effect application exceptions -> app-owned `effectError`.
- App consumer **does not call** `DynamicIntent.EffectFailed`; Store's intent path is directly tested but not wired from this production app consumer.
- Store state changes are applied **before** effects are sent.
- Each currently defined result emits 0 or 1 effect; effect-list and channel-batch contracts permit multiples. App catches errors per effect and continues later effects within the same batch.
- Closing an entry Store and closing the session channel have different lifetimes; no proposed change until specific behavior and tests are grounded.

## Why repository-wide completeness is not claimed
GitHub recursive tree returned 152 Kotlin files. An attempted complete content scan exceeded connector tool-call limits. Search of six critical files confirmed above hits, but **other files have not been exhaustively checked in this turn**; negative whole-project results cannot be asserted.

## Read-only final verification command (owner PowerShell)

```powershell
Get-ChildItem -Recurse -Filter *.kt -File |
    Where-Object { $_.FullName -notmatch '[\\/](build|\.gradle)[\\/]' } |
    Select-String 'EffectFailed|reportEffectFailure|deliverEffects|DynamicActionEffect|effectError|effectBatches|Channel<List<DynamicActionEffect>>' |
    ForEach-Object {
        "{0}:{1}: {2}" -f $_.Path.Replace((Get-Location).Path + '\', ''), $_.LineNumber, $_.Line.Trim()
    }
```
Scan only source, exclude generated build trees. Confirm all production and test callers, including platforms/modules. This is necessary before claiming `EffectFailed` dead or freezing removal.

## Recommendation pending inventory completion
**KEEP effect batching and dual error displays for now.** Current 0/1 effects alone do not justify changing an established effect delivery contract. **Flag `DynamicIntent.EffectFailed` as a potential redundant intent**, not a confirmed dead public API, until the complete caller scan shows no other production dispatch. If no callers beyond Store declaration/tests, propose a *separate minimal* cleanup plan limited to that intent, associated reducer, and its dedicated tests. First review whether the test-only path documents deliberately reserved behavior before removing it. No new EffectManager or ErrorManager, no rearchitecture, no State/Present/Sequence change.
