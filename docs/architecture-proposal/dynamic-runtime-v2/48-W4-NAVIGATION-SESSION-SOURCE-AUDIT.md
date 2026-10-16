# 48 — W4 Navigation and Session State: Initial Source-Level Audit

**Status:** AUDIT ONLY — no Kotlin edits or behavioral changes authorized by this record. Branch `architecture/dynamic-runtime-v2-discussion-freeze`. Sources inspected: all three Navigation commonMain classes, Navigation tests, DynamicContainer, DynamicStore, DynamicRuntimeState, Navigate/Present/Dismiss handlers, DynamicActionResult and DynamicActionExecutor. Contract authority: frozen doc20 §§1,3,6. W3 mapping work at `e85247c1` verified by owner; W4 has NOT been run/tested.

## Confirmed current ownership
- `DynamicNavigationState` is one Compose SnapshotStateList back stack + nullable modal presentation entry.
- `DynamicNavigationEntry(destination)` identity is the *object instance*, no separate persistent ID field. Each push, replace, setRoot and present creates a fresh instance; pop restores former object. Identity works in current in-memory state. It is not yet the frozen **explicit distinct entryId** or tested process-restoration contract.
- `DynamicNavigationHost` owns Nav3 `NavDisplay` and a Compose `Dialog` for modal, and invokes the same screen content lambda for each entry.
- `ApplicationContent` remembers one navigation state + one `DynamicRuntimeState` per initial destination; `DynamicContainer` remembers a per-entry `DynamicStore`. Thus shared state survives normal entries, while bindings belong to per-entry store while its composition is retained.
- Navigation op decoding lives in `NavigateActionHandler`; applying effects lives in `DynamicContainer.applyNavigation`. State effects and action ordering originate in `DynamicActionExecutor` and `DynamicStore`.

## Verified strengths
1. `push` creates fresh identity even for same destination; `replace` replaces only top; `pop` retains former entry instance and refuses to remove root.
2. `present` uses distinct modal entry; duplicate presentation is rejected; `dismiss` restores underlying stack; back dismisses modal first.
3. Navigation tests cover above basic state transitions.
4. No second navigation authority or speculative popTo support found.

## Contract gaps and risks — prioritize before editing
**P0 — session-safe ordered effect handoff is absent.**
`DynamicStore` buffers effects in its *entry-scoped* `Channel<DynamicActionEffect>`; `DynamicContainer` consumes through an *entry-scoped* `LaunchedEffect(store)`; `DisposableEffect(store)` closes the store and channel when entry exits composition. A `Sequence` can emit multiple effects (including navigation first), so after navigating/replacing/dismissing source entry the remaining effects are vulnerable to disposal/cancellation/loss. Channel.BUFFERED is not a session-safe delivery guarantee. This is a **source-derived risk** pending direct regression test, not a claimed reproduction. Frozen doc20 requires state commit before effects and ordered handoff surviving source entry disposal. Plan exact session-owned effect delivery by reusing one navigation authority; do not introduce global singleton manager.

**P0 — unknown navigation mode silently becomes Push.**
`NavigateActionHandler` lowercases `mode` then defaults all other strings/null to Push, and accepts `back` alias. Frozen doc20 §3 demands unknown operation be typed validation failure and explicitly excludes invented aliases. Determine whether absent `mode` means Push per current canonical fixture; validate known `push/replace/pop` explicitly; investigate semantics of mandatory destination for pop and whether alias `back` is historically used before changing.

**P1 — explicit `entryId` contract not implemented.**
`DynamicNavigationEntry` only holds destination; identity currently object identity. Frozen doc20 requests distinct entryId for each visit. Decide minimal in-memory ID strategy and test same-destination visits; do not add unrelated serialization/persistence until required.

**P1 — session retention vs entry-bound bindings not verified under Nav3 disposal.**
`DynamicRuntimeState` is shared; per-entry bindings reside in `DynamicStore`. Compose `remember(entry)` and Nav3 can dispose inactive screen composition, which would `close()` and recreate Store on re-entry, potentially losing bindings, contrary to frozen push/pop retention. Needs exact Nav3 lifecycle/test evidence and a minimal entry-state preservation plan.

**P1 — failure surfaces and rejected effects.**
`DynamicContainer` ignores Boolean outcomes of `navigation.present`, `navigation.pop`, `dismissPresentation`, and absent destination for push/replace. `DynamicStore.Failure(error.message)` or `EffectFailed(error.message)` can surface raw errors. Plan typed effect failure handling, avoid leaking raw network/server exception details, retain usable screen on action error per frozen 20.

**P2 — presentation behavior and root lifecycle.**
`DynamicNavigationHost` is distinct modal Dialog, so previous generic “present behaves as push” concern is fixed in current NavigationState. `setRoot` resets modal and stack correctly. Test actual modal disposal and back handling on supported platforms before asserting release acceptance.

**P2 — Session effect implementation ownership.**
`DynamicActionExecutor` accumulates and orders effects, `DynamicStore.applyActionResult` commits state then sends each effect; do not reimplement action semantics in Navigation. W4 should provide the destination/effect handoff owner, preserving W6 typed action sequence review.

## Suggested W4 source-first implementation order (NOT AUTHORIZED BY THIS AUDIT)
1. Write failing regression for Navigation-first Sequence (multiple effects), forced entry disposal, state committed before any effect, and ordering.
2. Define the smallest session-owned effect dispatch handoff using existing `DynamicNavigationState`/app session boundary, with one effect consumer and platform URI adapter where needed. No singleton global router or second Nav stack.
3. Test pop/replace/repeat visits and retained binding/state behavior with actual Nav3 compose lifetimes.
4. Strictly decode navigation operations and report rejected transitions. Resolve existing `back` alias against fixtures before deletion.
5. Extend Navigation + DynamicStore integration tests; JVM and Web build. Android/iOS runtime deferred and never claimed passed without execution.

## Exit gate
W4 remains **AUDIT/PLAN REVIEW**, not implemented or frozen. No Kotlin file changed; distinct entryId, effect delivery, strict operation validation and per-entry retained bindings remain pending. Final plan must be reconciled with doc20 and owner decisions before code editing.
