# CB-Partner — Roadmap

**Status:** ACTIVE ROADMAP
**Scope:** Frontend only
**Last Updated:** 2026-10-07

## Purpose

The roadmap defines major product/engineering outcomes. It does not duplicate task trackers, implementation plans or current-state records.

## Phase 0 — Constitution

**State: COMPLETE**

Governance, AI workflow, documentation, Git discipline and recovery process are established.

## Phase 1 — Technical Foundation

**State: COMPLETE / FROZEN**

Outcome:

- KMP/Compose foundation;
- module graph;
- convention plugins;
- Pure Store/MVI/UDF;
- Koin/runtime infrastructure;
- Ktor/network foundation;
- Navigation 3 foundation.

BASE-ARCH-001 → BASE-ARCH-017 are frozen. BASE-ARCH-014 and BASE-ARCH-017 are code-frozen.

## Phase 2 — First Dynamic Vertical Slice

**State: IMPLEMENTED / POST-UI-REFACTOR JVM VERIFICATION PENDING / FINAL PLATFORM VERIFICATION PENDING**

Target flow:

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
Registry / Renderer
 ↓
Actions
 ↓
Next DynamicDestination
 ↓
Login → Send OTP → OTP → Verify
```

Current state:

- Dynamic architecture and implementation plan are frozen.
- The frozen UI contract reconciliation is implemented.
- Generic JSON type preservation is verified from the pre-refactor matrix.
- The post-UI-refactor Dynamic/data/network/navigation JVM matrix is pending on the owner Windows environment.
- Login → OTP contract is reconciled with current backend source.
- Web, Android and iOS runtime verification remain platform gates.
- Backend local OTP delivery remains a separate backend task.

## Phase 3 — Partner Product Expansion

**State: PENDING**

After the Dynamic vertical slice is formally frozen, add real partner workflows incrementally from accepted backend/product contracts.

No business architecture is created speculatively.

## Phase 4 — Hardening

**State: PENDING**

Progressively address reliability, performance, accessibility, observability, deeper platform coverage, release automation and production quality as real requirements expose them.

## Phase 5 — Evolution

**State: FUTURE**

Add new capabilities only when justified by real requirements and a separately accepted work item.

## Roadmap rule

Do not use the roadmap to invent tasks.

```text
Roadmap
  ↓
Approved work
  ↓
Feature discussion
  ↓
Implementation plan
  ↓
Source
  ↓
Tests
  ↓
Review / Freeze
```

Current execution truth belongs in `docs/project-management/04-PROJECT-MASTER-STATE.md`.
