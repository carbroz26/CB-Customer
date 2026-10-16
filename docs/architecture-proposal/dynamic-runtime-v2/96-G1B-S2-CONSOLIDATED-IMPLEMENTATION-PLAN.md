# 96 — G1-B + S2 Consolidated Implementation Plan

**Status: SOURCE-RECONCILED IMPLEMENTATION PLAN FOR OWNER REVIEW — NO PRODUCTION IMPLEMENTATION AUTHORIZATION.**
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`
**Source baseline:** `5e1528de9448edcd7125e7ce96515cd48e28745e`
**Authority:** frozen 51, 85, 89 (D1–D4), 91 exact API, 95 S1 freeze/productivity rule.

## Objective and no-scope-creep rules
Implement successful Bootstrap -> retained logical session -> fresh active-graph entry Store -> atomic action-result acceptance -> one consumer claim/dispatch/ack. One cohesive, authorized implementation pass followed by focused tests, not an artificial series of tiny plan/approval/verification loops. Reuse existing classes/registries. No ViewModel, new module, manager, global DI, second navigation authority, backend/wire change, or speculative refactor. Preserve Android/Wasm/iOS commonMain contract.

## Source-truth inventory
- `App.kt` has a composition-scoped `koinApplication` in `remember(runtimeMetadataProvider,apiBaseUrl)`, closed in `DisposableEffect(koin)`. No retained session slot.
- `ApplicationContent.kt` always creates a remembered `SplashStore`, even if a prior successful session should survive host reattach; on Success closes SplashStore and builds `DynamicContent`. DynamicContent separately `remember`s navigation and runtime, has composition-owned `Channel.UNLIMITED` and `LaunchedEffect` effect consumer. This is the core integration location.
- `SplashStore.kt` already owns failure/retry/maintenance/update and unique diagnostic ID. Existing source stays unchanged.
- `DynamicSession.kt` S1 frozen at baseline: owns `DynamicNavigationState`, `DynamicRuntimeState`, ordered effects, revision, one lease, claim/start/ack/detach/close. It neither executes actions nor retains Koin/URI.
- `DynamicStore.kt` currently directly mutates `runtimeState.setAll` and calls suspending `deliverEffects`/`deliverCorrelatedEffects`; state+queue commit is split (D1 gap).
- `DynamicContainer.kt`: `remember(entry) { storeFactory() }`, `LaunchedEffect(store)` load, `DisposableEffect(store)` close. If same entry survives *within the same composition* but Koin changes, Store could be reused; ensure the active graph identity keys the subtree or store recreation, with smallest change backed by test.
- `DynamicNavigationEntry.kt` retains bindings only; not response snapshot. `DynamicStore.load()` starts GET when its screen state has no response. Therefore host reattach may still reload an entry after G1-B/S2 work unless G3 snapshot is addressed. **Never claim complete no-refetch behavior merely from successful Bootstrap retention.**
- `RetainCompatibilityProbe.kt` validates isolated `retain` and `RetainedEffect` on Android; no Koin or full host proof yet.

## Proposed minimal source modifications (file-by-file)
1. **`app/src/commonMain/.../App.kt`**: own one retained nullable session handoff slot at stable host composition boundary *before* creating per-attachment splash content; attach `RetainedEffect` to its stable retained owner to perform final `session.close()`, and do not run close on ordinary attachment `DisposableEffect`. Keep Koin composition-scoped with its existing close callback. Do not retain Koin or provider in logical session. Ensure recomposition and delayed bootstrap success do not duplicate session construction.
2. **`app/src/commonMain/.../ApplicationContent.kt`**: receive retained slot/session, show Splash only while session absent; create `SplashStore` scoped to current Koin only while needed. On successful state, install `DynamicSession(destination, diagnosticSessionId)` once, hand off, then close/dispose SplashStore. On active session, skip Bootstrap. Preserve maintenance, update and retry. Keep exact active Koin for new entry Store; key composition/store factory with graph identity when necessary. Reuse existing DynamicNavigationHost, logger, URI handling and safe error UI.
3. **`feature/dynamic/src/commonMain/.../store/DynamicStore.kt`**: inject a single non-suspending, non-app-specific callback, conceptually `commitActionResult: (Map<String, DynamicValue>, List<DynamicActionEffect>, TraceContext?) -> Boolean`. On successful executor result while coroutine active, invoke once, including state-only changes and empty effects. Remove split direct state patch and suspending effects callbacks within this integration. Preserve intent/reducer/validation semantics, cancellation before commit, trace and entry-scoped close. Returning false (retired session) must not report an action delivered; generic UI-safe handling only. No feature -> app import or extra UseCase.
4. **`app/src/commonMain/.../ApplicationContent.kt` consumer**: replace composition-owned channel with S1 `workRevision` observation. Exactly one lease for the active consumer. Drain `claimNext` in effect order, mark dispatch started immediately before applying existing navigation/URI, call `acknowledge(Applied|Rejected)`, and keep safe failure display. On cancellation/Dispose, detach lease; no replay of started ambiguous effect. Revoked/stale lease must not dispatch next item. Do not hold session lock around navigation/platform invocation. Do not add a second channel or manager.
5. **`feature/dynamic/src/commonMain/.../DynamicContainer.kt`**: preferably unchanged; only adjust remember keys or parent composition identity if test confirms Store graph stale-reuse. Do not retain Store itself.
6. **`app/src/commonMain/.../DynamicSession.kt`**: S1 FROZEN; unchanged unless integration exposes a reproducible behavioral conflict with document 91. Any necessary change must be minimal and reported.
7. **`feature/splash/.../SplashStore.kt`, `navigation/.../DynamicNavigationState.kt`**: unchanged by default.
8. **`navigation/.../DynamicNavigationEntry.kt` / domain snapshot**: **G3 dependency**, not silently included in G1-B/S2 scope. Existing entry bindings survive; loaded response does not. If no-refetch on reattach is mandatory for acceptance, propose one explicit G3 snapshot extension within the cohesive implementation before claiming the full lifecycle gate; navigation must not import feature/dynamic. Do not add duplicate screen DTO.

## One targeted verification pass
- App test G1-B: first Bootstrap creates session with same destination/diagnostic ID; host reattach reuses session without calling Bootstrap again; graph A can close and a new entry Store must resolve graph B; retirement closes session once, temporary detach does not; unrelated new session has no shared state.
- S2 Store/action integration: successful state-only result, successful navigation/external effect, cancellation before successful result, old Store close after accepted commit, ordered effects across detach, failed navigation followed by later effects, generic safe failure and no sensitive URI logs.
- Reuse S1 17 common + 1 JVM concurrent tests and existing G2-A tests. Do not duplicate existing navigation tests.
- Run relevant JVM suites once after code is ready; Android debug build + Wasm compile once because host/code cross platform. Real Android reattach/rotation and Web host behavior need manual smoke; iOS compile/runtime on macOS later.
- Distinguish Android probe retention evidence from actual Koin/bootstrap integration. If Web viewport teardown cannot retain state with current host API, document real platform limitation, not fictitious support.

## Focused pre-implementation decisions (single authorization, no extra ceremonial gate)
- Keep S1 API frozen; derive callback signature directly from its `commit(changes,effects,trace)` shape.
- Retain a **nullable session slot**, not SplashStore; final RetainedEffect cleanup must read current session value rather than capture initial null.
- Ensure cancellation-before-commit check uses existing action coroutine Job; no suspension inside commit callback. Clearly separate commit ownership and post-commit store cancellation.
- Use active graph identity to avoid stale entry Store only if host reattach composition structure would otherwise reuse it.
- **G3 dependency is real:** decide whether this authorized pass includes the minimum entry-owned response snapshot for no-repeat GET, or explicitly defer G3 and limit the G1-B acceptance claim to Bootstrap reuse and retained session identity.
- Do not alter frozen S1 for hypothetical issues.

## Stop conditions and completion
One cohesive implementation commit (or one clear reviewed diff) and one bounded verification pass, not repeated mini-approvals. If source reveals a real architecture conflict (G3 type boundary, retained host capability), request only that decision. No production changes by this planning document alone. Keep backend untouched and branch unchanged.
