# 85 — DYNAMIC-SESSION-LIFECYCLE-001: Cross-Platform Session Ownership

**Status: PLAN_FROZEN — OWNER APPROVED 2026-10-17. PRODUCTION KOTLIN IMPLEMENTATION NOT AUTHORIZED BY THIS DOCUMENT.**
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`.
**Source baseline:** `895fcdcf199a91a9cb265b697a9f9ed8fc80eaae`.
**Related authority:** W4 ownership and ordered-effect contracts (docs 50, 51, 53, 55); host proof doc 84. This document extends only the logical-session lifecycle scope. Do not change existing navigation/action wire semantics.

## Owner-approved frozen decisions

1. **Generic dynamic contract only.** No login, OTP, dashboard, business screen IDs, template IDs or element-specific restoration branches. Backend decides screens and actions; frontend owns only runtime lifecycle and navigation state.
2. **One minimal commonMain logical-session state holder** (working name `DynamicSession`) is justified strictly for *coordinated state lifetime*. No ViewModel, global singleton, second navigation controller, network manager, or generic application state framework. Holder must not execute actions, render UI, decode backend payloads, apply navigation operations, or hold platform URI handler.
3. **Navigation remains single-authority** in existing `DynamicNavigationState`. Entry identity and membership semantics remain W4-defined. `DynamicRuntimeState` remains the session-level typed-value owner.
4. **Entry-owned in-memory screen snapshot**, in addition to existing entry bindings. Closed `DynamicStore` instances are never retained/reused after disposal. Store remains pure, entry-composition scoped and owns intents, reduction, validation and in-flight operations. New Store restores an existing valid entry snapshot rather than unnecessary GET.
5. **Bootstrap handoff is retained, not a live SplashStore.** Successful bootstrap destination and diagnostic session ID pass into the logical session. Recomposition/configuration changes must not re-bootstrap a handed-off session.
6. **Effect batches retain W4 semantics:** ordered immutable whole-batch handoff, state-before-effects, a single application consumer, no dropping accepted batches due to source entry disposal, and safe error reporting while continuing later effects. Exactly-once claims for partially applied side effects across consumer detachment are **not yet authorized**; resolve the concrete continuation contract before edits.
7. **Retention scope:** in-memory composition/host configuration-change continuity when supported by the host. Do not serialize arbitrary dynamic values or credentials into SavedState, disk, web storage or preferences. Process death, hard browser reload and application termination recovery are excluded.
8. **Cross-platform architecture:** shared lifecycle contract in commonMain, minimal Android/Web/iOS host integration strictly according to verified lifecycle capability. No Android-only business logic. iOS code should be reviewed but iOS runtime/compiler verification must not be claimed on Windows.
9. **Clean Architecture/SRP:** no new Gradle module, no duplicate Store, no duplicate navigation, no feature-to-navigation reversed dependency. Scope all file changes to evidence-driven needs.

## Candidate fields and public contract — shape frozen, signatures subject to exact dependency reconciliation

- Logical identity: `diagnosticSessionId` plus initial `DynamicDestination` from completed bootstrap.
- Owned existing instances: `DynamicNavigationState`, `DynamicRuntimeState`.
- Ordered session-owned effect batch sink and lifecycle-safe delivery state; only one UI/platform consumer applies effects.
- Generic, UI-safe effect failure state as required.
- Minimal API: initial construction after bootstrap handoff, `deliverEffects(trace?, immutableEffects)`, idempotent `close()`, immutable/exposed access to navigation/runtime state.
- **Excluded fields:** `Koin`, `SplashStore`, `DynamicStore`, `CoroutineScope` from entry Stores, Compose UI state handles, UI `UriHandler`, decoded business semantics. Implementation can use private lifecycle primitives *only after the cleanup proof*.

## Exact source targets (change only if backed by tests)

| Target | Scope |
|---|---|
| `app/src/commonMain/kotlin/com/carbroz/cbpartner/app/DynamicSession.kt` (proposed new file) | Minimal coordinated logical session state/effects/teardown |
| `app/src/commonMain/kotlin/com/carbroz/cbpartner/app/App.kt` | Retention boundary and Koin lifetime (avoid closed-graph references) |
| `app/src/commonMain/kotlin/com/carbroz/cbpartner/app/ApplicationContent.kt` | Bootstrap handoff, one effect consumer and UI state |
| `navigation/src/commonMain/kotlin/com/carbroz/cbpartner/navigation/DynamicNavigationEntry.kt` | Minimal entry-owned screen snapshot with no dependency on feature/dynamic |
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/store/DynamicStore.kt` | Restore/synchronize snapshot; preserve cancellation and reduction |
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/DynamicContainer.kt` | Recreated Store renders retained entry; closes on composition disposal |
| `navigation/.../DynamicNavigationState.kt`, `feature/dynamic/.../DynamicRuntimeState.kt`, `feature/splash/.../SplashStore.kt` | Prefer unchanged; touch only with demonstrated failing test |
| Android `MainActivity.kt`, Web `Main.kt`, iOS `Main.kt` | Host adapters only if platform retention gaps demand |
| Existing navigation, dynamic Store/runtime tests and focused app/host tests | Generic regression suite |
| `RetainCompatibilityProbe.kt` and temporary opt-in host hooks | Remove after proof, before production closure |

**Module-boundary gate:** `DynamicNavigationEntry` cannot import `DynamicScreenState` from `feature/dynamic`. Choose an existing domain-owned snapshot representation or an exact minimal neutral representation; do not add a duplicate model without owner review. **Do not treat the candidate file list as authorization for all files to change.**

## Required regression gates

1. Recomposition and proven host configuration change preserve the same logical session (Android observation positive; Web recomposition/resize positive, true ComposeViewport teardown/recreation unverified; iOS unverified).
2. Stack entries, unique entryIds, current destination, modal presentation and session diagnostic ID preserved.
3. Per-entry bindings and typed nested `DynamicRuntimeState` preserved; removing entry releases its snapshot; new session isolated.
4. Loaded valid dynamic response and UI-safe validation state restored without redundant load; entry-scoped Store is newly created, not reused after close.
5. Bootstrap handoff survives host recreation; no repeat Bootstrap for active session.
6. Cancellation before commit produces no new committed effects; in-flight action not automatically replayed, and old Store's jobs cancel.
7. Accepted whole-batch ordered effects survive temporary consumer detach/reattach without duplicate completed effects; partially applied batch must have explicit tested policy.
8. Session closure idempotent, final cleanup not triggered by temporary host detachment, no Koin closed-graph usage; no resource leak.
9. No persistence of arbitrary user data to device/browser storage; diagnostics do not log raw values.

## Blocking implementation-design gates (NOT SILENTLY FROZEN AS SOLVED)

- **G1 — DI and retain final cleanup:** prove exact Compose `retain` final removal/disposal semantics in the project version; prove retained session never accesses disposed Koin and bootstrap handoff survives host reattachment. Owner review of any necessary lifetime restructuring.
- **G2 — Effect continuation:** define/test accepted batch lifecycle including consumer detached during a partially applied batch, especially external URI effects. Do not invent exactly-once guarantees.
- **G3 — Snapshot dependency boundary:** select dependency-safe entry snapshot type without importing feature/dynamic from navigation; respect current domain/mapper model authority.
- **G4 — Cross-platform host parity:** Android behavior was observed in manual experiment; Web resize/recompose/refresh observed; Web genuine viewport recreation and iOS controller recreation remain unproven. Document platform-accurate guarantees before acceptance.
- **G5 — Test & build:** first write focused red regression tests, then implement only signed-off design; owner runs JVM tests + Android/Wasm builds + Android device test; iOS source reviewed and compiler/runtime verified on macOS later.

## Strict change control

This approval **freezes goals, responsibility allocation, non-goals, and regression acceptance**. It does **not** authorize implementation of unresolved G1–G4 mechanisms or production Kotlin edits. Resolve each gate with source-level evidence; if solving one would change a frozen rule, request an explicit decision before editing. Discussion -> Research -> Decide -> Plan -> Freeze -> Implement -> Test -> Review -> Freeze. Backend repository is out of scope/read-only. Existing local untracked trace-preview/patch files must be preserved.

**Next:** complete G1–G4 technical reconciliation and present narrow Kotlin implementation authorization scope, then implement in isolated slice only after explicit approval.
