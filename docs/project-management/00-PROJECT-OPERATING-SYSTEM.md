# CB-Partner — Project Operating System

**Status:** AUTHORITATIVE
**Scope:** Frontend repository only
**Last Updated:** 2026-10-07

## Purpose

This document defines how CB-Partner is discussed, planned, implemented, tested, reviewed, documented and recovered without architecture drift or repeated restarts.

## 1. Source of truth

```text
Current user instruction
      ↓
AI_START_HERE.md
      ↓
Accepted / frozen decisions
      ↓
Project Master State
      ↓
Relevant architecture / feature / API / build contract
      ↓
Source + tests
      ↓
Historical chat
```

One authority exists for each topic. Historical chat never overrides a frozen repository decision.

## 2. Feature lifecycle

Exactly three phases:

```text
FEATURE DISCUSSION
Discuss → Research → Decide → Discussion Freeze
        ↓
IMPLEMENTATION PLAN
Complete Plan → Plan Freeze
        ↓
IMPLEMENTATION
Implement → Test → Review → Feature Freeze
```

Implementation units are sequencing aids, not additional approval gates.

## 3. Permission

- DISCUSS / RESEARCH / DECIDE: no source modification.
- PLAN: plan/documentation only unless explicitly authorized.
- IMPLEMENT: source changes against the frozen plan.
- TEST: required verification and genuine in-scope fixes.
- REVIEW: inspect and classify findings.
- DOCUMENT: synchronize durable records.
- STATUS: recover and report.

## 4. Architecture protection

- No ViewModel.
- Pure Store/MVI/UDF.
- No speculative modules.
- No duplicate infrastructure abstractions.
- No business logic in composables.
- No feature-specific raw HTTP.
- No silent frozen-decision changes.
- No invented backend contracts.
- No unrelated refactoring during feature work.

## 5. Documentation structure

Project management contains only project-wide process/state:

```text
00 Project Operating System
01 AI Context
02 AI Rules
03 Decision Log
04 Project Master State
05 Roadmap
06 Trello Workflow
07 Development Workflow
```

Feature/technical details belong in their specific directories.

The Dynamic feature intentionally uses three documents:

```text
DYNAMIC-ARCHITECTURE.md
DYNAMIC-IMPLEMENTATION-PLAN.md
DYNAMIC-IMPLEMENTATION-STATUS.md
```

Completed/small feature areas use a single focused document when a separate discussion/plan/status history would only duplicate the same authority.

## 6. API boundary

```text
UI → Store → Use Case / orchestration → Repository → Data Source → Network
```

UI and Store code do not own raw HTTP.

## 7. Git safety

Before source changes:

```powershell
git status
git branch --show-current
git diff --stat
```

Preserve user work. Never use destructive Git operations to resolve uncertainty.

## 8. Testing and review

Compilation alone is not feature completion.

Required testing comes from the relevant implementation plan. Review compares the complete implementation against requirements, frozen decisions, plan, architecture, tests, scope and final diff.

## 9. Trello

Trello tracks real work:

```text
BACKLOG → TO DO → READY → IN PROGRESS → DONE
```

Architecture, decisions and detailed technical state remain in documentation.

## 10. Recovery

New sessions read:

1. `AI_START_HERE.md`
2. `docs/project-management/04-PROJECT-MASTER-STATE.md`
3. `docs/project-management/03-DECISION-LOG.md`
4. only the relevant technical/feature/build/API document
5. source/tests after state is understood

## Golden rule

**Move the project from one known, documented, verified state to the next.**
