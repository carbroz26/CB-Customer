# CB-Partner — Roadmap

**Status:** ACTIVE ROADMAP
**Scope:** Frontend only
**Last Updated:** 2026-10-04

## Purpose

The roadmap defines major outcomes. The Project Tracker and Trello own detailed execution state. Feature documents own technical contracts.

## Phase 0 — Constitution

**State: COMPLETE**

Outcome: governance, AI workflow, documentation, Git discipline and recovery process established.

## Phase 1 — Technical Foundation

**State: COMPLETE / FROZEN**

Outcome: reproducible KMP/Compose foundation, module graph, convention plugins, Pure Store/MVI/UDF direction, DI/network foundations and runtime infrastructure.

BASE-ARCH-001 → BASE-ARCH-017 are frozen; BASE-ARCH-014 and BASE-ARCH-017 are code-frozen after verification/review.

## Phase 2 — First Dynamic Vertical Slice

**State: IN PROGRESS**

Outcome:

```text
Bootstrap
 ↓
DynamicDestination
 ↓
Dynamic navigation/container
 ↓
Dynamic screen API
 ↓
Dynamic JSON
 ↓
Registry/Renderer
 ↓
Actions
 ↓
Next DynamicDestination
 ↓
Login → OTP verification
```

Current JVM compile/test baseline is green. Full backend/platform verification and final feature freeze remain.

### Current checkpoint

- Bootstrap implemented/tested.
- Dynamic architecture DYNAMIC-01 → DYNAMIC-07 frozen.
- Dynamic registry/renderer/store/use cases/network boundary implemented.
- Login/OTP contracts recorded.
- Login → OTP end-to-end verification pending.

## Phase 3 — Partner Product Expansion

**State: PENDING**

After Dynamic is frozen, implement partner workflows incrementally from actual product/backend contracts. Do not create business-specific architecture speculatively.

Expected future examples may include Dashboard, Booking, Service execution, Earnings, Profile and other partner workflows, but no feature is authorized until its real contract is available.

## Phase 4 — Hardening

**State: PENDING**

Reliability, performance, accessibility, observability, deeper test coverage, release automation and platform quality are hardened progressively as real features expose requirements.

DYNAMIC-07 observability is already architecturally frozen and is part of the current Dynamic implementation completion work.

## Phase 5 — Evolution

**State: FUTURE**

Add capabilities only when justified by real requirements.

## Roadmap Rule

Do not use the roadmap to invent implementation tasks. Use:

```text
Roadmap → Project Tracker → Feature Discussion → Implementation Plan → Source → Tests → Review
```

Major roadmap changes require explicit discussion and documentation.
