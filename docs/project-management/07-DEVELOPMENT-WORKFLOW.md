# CB-Partner — Development Workflow

**Status:** AUTHORITATIVE / CURRENT
**Scope:** Development lifecycle, Git, implementation, testing and review

## 1. Feature lifecycle

```text
FEATURE DISCUSSION
  Discuss → Research → Decide → Discussion Freeze
        ↓
IMPLEMENTATION PLAN
  Complete Plan → Plan Freeze
        ↓
IMPLEMENTATION
  Implement Completely → Test → Review → Feature Freeze
```

Implementation units are sequencing aids, not extra approval gates.

## 2. Implementation

Before implementation:
- requirement is understood;
- required decisions are frozen;
- complete implementation plan is approved;
- correct branch is checked;
- Git state is safe;
- actual source/build structure is inspected.

During implementation:
1. establish baseline;
2. implement the frozen scope;
3. compile/test frequently;
4. add focused regression coverage;
5. inspect the diff;
6. complete the whole approved scope.

Do not silently add dependencies, modules, architecture changes, unrelated refactors, invented backend behavior, or weakened tests.

## 3. Testing

Testing must verify behavior, not only compilation.

Use the authoritative Gradle document for setup and build commands:

`docs/build/GRADLE-SETUP-AND-WORKFLOW.md`

For Dynamic, use:

`docs/dynamic/DYNAMIC-IMPLEMENTATION-PLAN.md`

and

`docs/dynamic/DYNAMIC-IMPLEMENTATION-STATUS.md`

Do not claim Android/iOS/Web verification unless that platform was actually exercised.

## 4. Review

Review the complete implementation against:
1. scope;
2. frozen decisions;
3. implementation plan;
4. architecture;
5. functional behavior;
6. Store/MVI/UDF;
7. KMP/platform boundaries;
8. lifecycle/concurrency/error handling;
9. tests;
10. security/privacy;
11. documentation;
12. Git diff.

Classify findings as BLOCKER, REQUIRED, OPTIONAL or FUTURE.

Review does not silently authorize redesign.

## 5. Git branches

Permanent:

- `main` — production-ready code.
- `development` — integration branch.

Working:

- `architecture/<name>`
- `feature/<name>`
- `fix/<name>`
- `hotfix/<name>`

Typical lifecycle:

```text
development
  ↓
working branch
  ↓
DISCUSS → PLAN → IMPLEMENT → TEST → REVIEW → FREEZE
  ↓
commit / push / PR
  ↓
development
  ↓
main when production-ready
```

Current working branch:

`feature/dynamic-ui`

State meanings are distinct:

```text
IMPLEMENTED ≠ VERIFIED ≠ REVIEWED ≠ FROZEN ≠ MERGED ≠ SYNCED
```

## 6. Clone and synchronize

First clone:

```powershell
git clone https://github.com/carbroz26/CB-Partner.git
cd CB-Partner
git status
git remote -v
```

Before work:

```powershell
git fetch origin --prune
git status
git branch --show-current
git diff --stat
```

Continue a branch:

```powershell
git switch <branch>
git pull --ff-only origin <branch>
git status
```

Never discard unknown local changes.

## 7. Inspect and commit

```powershell
git status
git diff --stat
git diff
git diff --check
git log -10 --oneline --decorate
```

Commit only focused, reviewed changes:

```powershell
git add <files>
git diff --cached
git commit -m "type(scope): description"
git status
```

Push:

```powershell
git push -u origin <branch>
```

Never force-push without explicit authorization.

## 8. Pull request and integration

```text
working branch
  → push
  → PR/review
  → merge to development
  → integration verification
  → release review
  → main when production-ready
```

Do not locally merge merely for convenience when the project workflow requires a PR.

After integration:

```powershell
git fetch origin --prune
git switch development
git pull --ff-only origin development
git status
```

## 9. Safe recovery

Use:

```powershell
git fetch origin --prune
git status
git log --oneline --decorate -10
```

Safe file undo:

```powershell
git restore <file>
git restore --staged <file>
```

Use `git revert <commit>` for an already-published commit when appropriate.

Do not use `git reset --hard`, force-push, broad restore, or destructive branch deletion to resolve uncertainty.

## 10. Antigravity handoff

After any material remote repository change, give the local environment exact synchronization commands:

```powershell
git fetch origin --prune
git switch <branch>
git pull --ff-only origin <branch>
git status
```

Then run the applicable build/test commands from the authoritative build or feature document.

## 11. Documentation completion

After a meaningful feature change update only the relevant authorities:

- decisions → Decision Log;
- current project state → Project Master State;
- architecture → architecture authority;
- feature plan/status → feature authority;
- build setup/version → Gradle authority.

Do not maintain parallel current-state or tracker documents.
