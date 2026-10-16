# 95 — S1 Final Freeze and Productivity-First Working Rule

**Status: S1 ISOLATED SESSION BEHAVIOR FROZEN — OWNER APPROVED 2026-10-17.**
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`
**Verified source baseline:** `5b7ef47ab3c3b30db31e01a56eb4f6ad89e61b3c`
**Authority:** previously frozen D1–D4 (89), exact API (91), scoped S1 plan (92), audits (93–94).

## S1 freeze: exact scope
- `app/src/commonMain/.../DynamicSession.kt`: only isolated in-memory state/effect acceptance, FIFO batches, single consumer lease, claim/dispatch-start/ack, safe detach continuation, final retirement and pending work signal.
- Existing `DynamicNavigationState` remains the sole navigation authority; session never executes navigation, opens external URIs, executes actions, or owns Koin/Store.
- Minimal corrections landed before approval: prepare/enqueue effects before state patch, rollback unannounced batch for ordinary synchronous `setAll` failure, publication after successful patch, reference-identity verification of issued claim tokens.
- `DynamicSessionEffectsTest.kt`: 17 common regression tests; `DynamicSessionConcurrencyTest.kt`: 1 JVM four-thread concurrent-commit test.
- Owner execution on Windows at baseline: `:app:jvmTest :feature:dynamic:jvmTest` BUILD SUCCESSFUL (22s); `:app:compileKotlinWasmJs :androidApp:assembleDebug` BUILD SUCCESSFUL (20s).
- Limitations: passing JVM concurrency test and Android/Wasm compilation do not prove main-thread contention, Web runtime host recreation or iOS behavior. Current `DynamicStore` still uses split state mutation + suspending effect delivery; end-to-end D1 remains a separately approved S2 integration gate. G1-B, G3 and G4 likewise not complete.
- **No more S1 work unless a concrete future integration failure or contract regression requires it.** Never reopen S1 for speculative polish.

## Strict owner-approved productivity rule
**Goal: achieve the frozen implementation plan cleanly, completely and quickly, with evidence proportional to actual risk.**

1. **No unnecessary incremental gates.** Group closely related, already-approved changes in one cohesive slice. Do not split one routine fix into repeated research, plan, freeze, approval, test and review loops without a genuine architectural decision.
2. **Risk-based process.** Architecture, module boundaries, ownership, lifecycle, cancellation, concurrency, security/sensitive data and backend wire-contract changes deserve focused discussion/planning/targeted verification. Mechanical edits, documentation-only updates, formatting, ordinary tests and low-risk local fixes should be handled directly with minimal coordination.
3. **Single source of truth.** Read authoritative source and frozen contract; do not regenerate overlapping review documents, repeat broad audits or reopen settled choices without a demonstrable source conflict.
4. **Keep SRP and Clean Architecture strict.** No duplicate manager/wrapper, unused abstraction, extra module/branch, generic framework or scope drift. Preserve existing responsibilities and boundaries.
5. **Test the change, not the entire world.** Run the smallest relevant suite first; run wider JVM/Android/Wasm gates when impact crosses modules/platforms or before a meaningful release/final gate. Never repeatedly rerun unchanged expensive builds for documentation-only or already verified changes.
6. **Prefer one productive pass.** Inspect necessary code, state a concise file list/risk, implement authorized work, run relevant tests, review the actual diff/results, and close. Do not invent future hypotheticals that do not affect a present decision.
7. **Preserve explicit authorization on material changes.** A true unresolved architecture/contract decision, production implementation beyond the agreed scope, or destructive change still needs owner approval. Do not interpret productivity as permission to alter frozen decisions.
8. **Concise user communication.** Provide direct decisions, meaningful blockers, exact commands and outcomes; avoid hourly-long conversations about simple changes. Documentation should be updated only when it carries a durable decision or necessary handoff.
9. **No avoidable verification claims.** Passing compilation is not runtime proof, but missing non-blocking proof should be documented briefly and deferred to the appropriate integration gate—not converted into endless pre-implementation steps.
10. **Outcome:** deliver the full frozen design without mistakes, duplicated effort or ritual overhead. The default for noncontroversial work inside an authorized scope is to act, not ask for another unnecessary approval.

## Next work
Proceed to the G1-B/S2 integration plan with one consolidated, source-grounded pass. Preserve D1–D4 and document 91 contracts. Do not assume G1-B/S2 production changes are authorized by this S1 freeze. Backend unchanged.
