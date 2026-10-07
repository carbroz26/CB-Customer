# CB-Partner — Project Operating System

**Status:** AUTHORITATIVE
**Scope:** Frontend repository only
**Last Updated:** 2026-10-04

## Purpose

This document is the project operating constitution. It controls how the project is discussed, planned, implemented, tested, reviewed, documented and recovered.

The objective is **controlled speed without architecture drift or repeated restarts**.

## 1. Project Boundary

CB-Partner is the frontend/partner application for the doorstep car-washing platform. Backend is a separate repository and project-management system.

## 2. Core Technical Direction

- Kotlin Multiplatform
- Compose Multiplatform
- Clean Architecture
- Pure Store-based MVI
- UDF
- No ViewModel
- Multi-module architecture
- Dependency Injection
- Gradle Convention Plugins

Detailed technology/architecture decisions are binding only when accepted/frozen in the relevant durable record.

## 3. Source-of-Truth Rule

```text
Current explicit user decision
        ↓
AI_START_HERE
        ↓
Accepted/Frozen decisions
        ↓
Current architecture/feature contracts
        ↓
Current status/tracker
        ↓
Source/tests
        ↓
Historical chat
```

Historical conversation never overrides a current frozen repository decision.

## 4. EXACT FEATURE LIFECYCLE — THREE PHASES

Every significant feature/module follows exactly three phases:

### Phase 1 — FEATURE DISCUSSION

```text
DISCUSS → RESEARCH → DECIDE → DISCUSSION FROZEN
```

Resolve requirements, boundaries, alternatives, important edge cases and architectural decisions.

Once the feature discussion is frozen, immediately move to the complete implementation plan.

### Phase 2 — IMPLEMENTATION PLAN

Create one complete plan containing:

- objective;
- scope / non-goals;
- modules/files;
- dependencies;
- architecture impact;
- complete implementation sequence;
- tests;
- acceptance criteria;
- risks;
- documentation updates.

Then:

```text
PLAN → PLAN FROZEN
```

Plan freeze is the implementation authorization boundary.

### Phase 3 — IMPLEMENTATION

```text
IMPLEMENT COMPLETELY
        ↓
TEST
        ↓
REVIEW
        ↓
FEATURE FROZEN
```

Do not create separate approval/freeze gates for every implementation unit.

Implementation units are sequencing labels only.

## 5. No Extra Gates

Do not repeatedly stop after every class/unit for another discussion, plan or authorization.

The intended workflow is:

```text
DISCUSS ONCE
   ↓
FREEZE DISCUSSION
   ↓
PLAN ONCE
   ↓
FREEZE PLAN
   ↓
IMPLEMENT COMPLETELY
   ↓
TEST
   ↓
REVIEW
   ↓
FREEZE FEATURE
```

This replaces the older unit-level gate-heavy workflow.

## 6. AI Permission Rule

DISCUSS/RESEARCH/DECIDE do not modify source.

PLAN does not modify source.

After the complete plan is frozen and implementation is authorized, AI may implement the complete feature within that plan.

## 7. No Silent Architecture Change

If implementation reveals a material conflict:

```text
STOP
 ↓
REPORT
 ↓
DISCUSS / RESEARCH
 ↓
DECIDE
 ↓
FREEZE
 ↓
UPDATE PLAN
 ↓
IMPLEMENT
```

Do not silently redesign a frozen architecture boundary.

## 8. No Restart Rule

A bug or design problem is not a reason to restart the project.

Classify it as:

1. bug;
2. local design problem;
3. refactoring opportunity;
4. feature boundary problem;
5. architecture problem;
6. invalid fundamental assumption.

Prefer fix/refactor/migrate/recover to restarting from scratch.

## 9. Documentation Rule

Every significant feature has:

```text
docs/features/<feature>/
├── 00-FEATURE-DISCUSSION.md
├── 01-IMPLEMENTATION-PLAN.md
└── 02-IMPLEMENTATION-STATUS.md
```

Project-wide recovery is maintained in:

`docs/project-management/04-PROJECT-MASTER-STATE.md`

## 10. Store / MVI / UDF Rule

```text
UI Event
   ↓
Store
   ↓
State / Effect
   ↓
UI
```

No ViewModel layer.

## 11. Module Rule

Create a module only for a real boundary such as dependency isolation, ownership, platform separation, build concerns or feature isolation. Do not create modules speculatively.

## 12. API Boundary Rule

```text
UI → Store → Use Case → Repository → Data Source → Network
```

UI/Store code does not own raw HTTP. DTOs are transport models; explicit mapping protects domain/application code from transport changes.

## 13. Git Rule

Before source modification:

```powershell
git status
git branch --show-current
git diff --stat
```

Preserve user changes. Keep work scoped. Inspect the final diff. Never use destructive Git commands to resolve uncertainty.

## 14. Testing Rule

A successful compile is not feature completion.

Run the tests and platform verification required by the frozen feature plan. Do not weaken assertions or remove tests merely to obtain a green build.

## 15. Review Rule

After testing, compare the complete feature against:

- user requirement;
- frozen discussion;
- frozen plan;
- architecture;
- tests;
- scope;
- final Git diff.

Resolve required findings before feature freeze.

## 16. Trello Rule

Trello is the execution tracker:

```text
BACKLOG → TO DO → READY → IN PROGRESS → DONE
```

Trello does not replace architecture/decision documents.

## 17. Recovery Rule

New sessions read:

1. `AI_START_HERE.md`
2. `14-PROJECT-MASTER-STATE.md`
3. `04-CURRENT-STATUS.md`
4. `12-PROJECT-TRACKER.md`
5. relevant frozen architecture/feature documents
6. Git state as required

## 18. Definition of Done

A feature is DONE only when:

- required behavior is implemented;
- frozen scope is satisfied;
- required tests pass;
- required platform verification passes;
- review is accepted;
- documentation is current;
- Git state is understood;
- feature is explicitly frozen.

## Golden Rule

**Move the project from one known, documented, verified state to the next. Do not add process overhead after a decision is already frozen.**
