# CB-Partner — AI START HERE

**Status:** AUTHORITATIVE GLOBAL ROUTER
**Last Updated:** 2026-10-13

## 1. First rule

Read this document before project action. Recover durable state before coding. Never guess, silently redesign, or reconstruct current truth from historical chat when a current document exists.

## 2. Repository identity

- Project: `CB-Partner`
- Repository: `carbroz26/CB-Partner`
- Frontend/partner application only.
- Backend is a separate repository.

## 3. Authority order

When information conflicts:

1. explicit current user instruction;
2. `AI_START_HERE.md`;
3. accepted/frozen decisions;
4. `docs/project-management/04-PROJECT-MASTER-STATE.md`;
5. relevant architecture/API/build/feature contract;
6. source/tests;
7. Trello execution state;
8. historical chat.

A material conflict with a frozen decision requires:

```text
STOP → REPORT → DISCUSS / RESEARCH → DECIDE → FREEZE → UPDATE PLAN → IMPLEMENT
```

## 4. New-session recovery

Read:

1. `AI_START_HERE.md`
2. `docs/project-management/04-PROJECT-MASTER-STATE.md`
3. `docs/project-management/03-DECISION-LOG.md`
4. relevant architecture/feature/API/build document
5. source/tests only after the durable state is understood

Then recover:

- current phase;
- current work;
- frozen decisions;
- completed implementation;
- latest verification;
- open work;
- blockers;
- next valid action.

There is one current-state/recovery document. Do not search for parallel Current Status, Tracker, Checkpoint or Master State files.

## 5. Documentation map

| Need | Authority |
|---|---|
| Global AI routing | `AI_START_HERE.md` |
| Project operating rules | `docs/project-management/00-PROJECT-OPERATING-SYSTEM.md` |
| Stable project context | `docs/project-management/01-AI-CONTEXT.md` |
| AI safety/rules | `docs/project-management/02-AI-RULES.md` |
| Decisions | `docs/project-management/03-DECISION-LOG.md` |
| Current state/recovery | `docs/project-management/04-PROJECT-MASTER-STATE.md` |
| Roadmap | `docs/project-management/05-ROADMAP.md` |
| Trello | `docs/project-management/06-TRELLO-WORKFLOW.md` |
| Development/Git/review | `docs/project-management/07-DEVELOPMENT-WORKFLOW.md` |
| Architecture | `docs/architecture/ARCHITECTURE.md` |
| Frozen base architecture | `docs/architecture/BASE-ARCHITECTURE.md` |
| Runtime infrastructure | `docs/architecture/BASE-ARCH-015-DECISION-RECORD.md`, `BASE-ARCH-017-DECISION-RECORD.md`, `BASE-ARCH-017-IMPLEMENTATION-PLAN.md` |
| Gradle | `docs/build/GRADLE-SETUP-AND-WORKFLOW.md` |
| API | `docs/api/API_CONTRACT.md` |
| Dynamic architecture/plan/status | `docs/dynamic/DYNAMIC-ARCHITECTURE.md`, `DYNAMIC-IMPLEMENTATION-PLAN.md`, `DYNAMIC-IMPLEMENTATION-STATUS.md` |
| Splash/Bootstrap | `docs/features/splash-bootstrap/SPLASH-BOOTSTRAP.md` |
| Web | `docs/features/web-app/WEB-APP.md` |
| Frontend engineering | `docs/frontend/FRONTEND.md` |

## 6. Frozen technical direction

- Kotlin Multiplatform
- Compose Multiplatform
- Clean Architecture
- Pure Store/MVI/UDF
- No ViewModel
- Multi-module Gradle architecture
- Gradle convention plugins
- Koin
- Ktor Client
- kotlinx.serialization
- Kermit
- Navigation 3

## 7. Feature lifecycle

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

Implementation units are sequencing aids, not extra approval gates.

## 8. Dynamic non-negotiables

- Backend-driven Dynamic UI.
- One permanent Dynamic Container.
- Entry-scoped Store.
- Generic `DynamicValue` runtime state.
- Supported references: `$binding`, `$context`, `$state`, `$literal`.
- `$response` is forbidden.
- No business-specific state namespaces.
- No Dynamic-specific HttpClient.
- No Dynamic navigation stack/cache.
- Explicit registries and renderer hierarchy.
- External URI is a first-class action capability.
- No native Login/OTP/Dashboard architecture.

## 9. Canonical destination

One `DynamicDestination` is shared by Bootstrap, Dynamic API responses and Dynamic actions:

```text
screenId
templateId
templateType
endpoint
```

`templateId` is opaque identity. `templateType` selects rendering only.

## 10. Startup

Splash and Bootstrap are one flow.

```text
Splash
  ↓
ApplicationBootstrap
  ↓
BootstrapRepository
  ↓
BootstrapRemoteDataSource
  ↓
RequestHandler / Ktor
  ↓
BootstrapOutput
  ↓
DynamicDestination
```

Bootstrap Option B is frozen.

## 11. Current project state

As of 2026-10-13:

- BASE-ARCH-001 → BASE-ARCH-017: frozen.
- BASE-ARCH-014: code-frozen.
- BASE-ARCH-017: verified/review-accepted/code-frozen.
- Original Dynamic runtime/UI source audit and JVM matrix: previously passed.
- Local Dynamic asset fallback amendment: separate verification item remains tracked in Dynamic status.
- Current branch: `feature/dynamic-ui`.
- Current Dynamic content-owned spacing amendment: implementation/build gates are recorded; final source/diff and Web visual review remain open.
- Current active task: `DYNAMIC-REGISTRY-ACTION-EFFECT-AMENDMENT-001` is plan-frozen on `feature/dynamic-ui`. It supersedes the older Option A Element-dispatch decision only where conflicting. Use typed registration-driven Element and Content dispatch by concrete model-class identity, ordered action-effect aggregation and reliable effect delivery, and distinct modal presentation semantics. Exact scope/tests/stop conditions: `docs/dynamic/DYNAMIC-IMPLEMENTATION-PLAN.md` section 22.30; architecture authority section 24. Source changes and tests have not started. Exact IconElement and IconContent/DividerContent renderer paths must be verified before source edits. Do not start the separate mapper consolidation plan in section 22.29 or touch its branch; do not create branches or edit the backend.
- The owner ran the seven-module JVM test matrix and targeted domain/data/Dynamic JVM compilation successfully on 2026-10-11.
- Final line-by-line diff/obsolete-property audit and Web visual verification remain open; do not claim final freeze yet.
- Use the latest docs/dynamic/DYNAMIC-ARCHITECTURE.md, DYNAMIC-IMPLEMENTATION-PLAN.md, DYNAMIC-IMPLEMENTATION-STATUS.md, docs/project-management/04-PROJECT-MASTER-STATE.md, and 03-DECISION-LOG.md for recovery. For the current registry/action work, authority is `DYNAMIC-REGISTRY-ACTION-EFFECT-AMENDMENT-001`, architecture section 24, and implementation plan section 22.30. Section 22.28 / `DYNAMIC-REGISTRY-ELEMENT-SIMPLIFICATION-001` is historical and superseded only where its registry dispatch conflicts with the new amendment. Do not treat historical parent-level text-spacing amendments as the current contract.
- Backend local OTP provider: inspection/decision remains separate.

## 12. Verification baseline

Latest Dynamic matrix:

```powershell
.\gradlew.bat :data:jvmTest :network:jvmTest :navigation:jvmTest :feature:dynamic:jvmTest
```

Result: **BUILD SUCCESSFUL**.

Expected Windows warning: iOS simulator tasks require macOS.

For Web, use the Web feature/build documents. For Android/iOS, claim runtime verification only after actually running the platform.

## 13. Git safety

Before changes:

```powershell
git status
git branch --show-current
git diff --stat
```

Preserve user work. Never use destructive Git operations to resolve uncertainty.

After remote changes, provide:

```powershell
git fetch origin --prune
git switch <branch>
git pull --ff-only origin <branch>
git status
```

## 14. Stop conditions

Stop when:
- required context is missing;
- documents materially conflict;
- a frozen decision must change;
- implementation authority is missing;
- unexpected user changes exist;
- scope expands;
- backend behavior must be invented;
- destructive Git action is proposed.

## 15. Completion handoff

At meaningful completion report:

```text
COMPLETED
FROZEN
IMPLEMENTED
VERIFIED
OPEN
BLOCKED
NEXT VALID ACTION
```

Never auto-start the next separately gated work item.

## 16. One-line external AI instruction

> Read `AI_START_HERE.md` first, recover state from Project Master State and the Decision Log, follow the relevant authoritative contract, never guess or silently redesign, and stop when a frozen boundary must change.



## 17. Current cross-repository contract revision — 2026-10-11

The content-owned spacing revision is plan-frozen and implementation was explicitly authorized on 2026-10-12. On frontend PR #6, the JVM test matrix, Android unit/compile/assemble tasks, and Web/Wasm build/browser-test tasks passed on source head `c08a6729186590eb094e79176f34daa119aa8a4a`. The current PR head has subsequent documentation-only reconciliation commits. Final line-by-line source/diff audit and actual Web visual inspection remain open; do not declare final freeze or merge complete until those gates are addressed. Read `docs/dynamic/DYNAMIC-ARCHITECTURE.md` section 21 and `docs/dynamic/DYNAMIC-IMPLEMENTATION-PLAN.md` section 22.26 before changing source. The backend counterpart is recorded in `sdui/SDUI-COMPOSE-CONTRACT-IMPLEMENTATION-PLAN.md` and `sdui/PARTNER-AUTH-SDUI-CONTRACT.md` on the matching backend branch. Do not broaden scope or touch the backend repository during this frontend PR.


## 18. Current Dynamic registry/action amendment — 2026-10-13

The owner explicitly froze the complete plan under `DYNAMIC-REGISTRY-ACTION-EFFECT-AMENDMENT-001`. Read `docs/dynamic/DYNAMIC-ARCHITECTURE.md` section 24 and `docs/dynamic/DYNAMIC-IMPLEMENTATION-PLAN.md` section 22.30. This decision supersedes the older Option A Element-dispatch direction only where the new typed model-class Element/Content dispatch conflicts with it. The plan also covers ordered sequence effects, reliable effect transport, and distinct modal presentation semantics.

Current phase: documentation reconciliation and exact source-path verification. No source implementation or tests have started. Do not guess the unresolved IconElement/IconContent/DividerContent renderer paths, do not implement the separate mapper-consolidation plan, do not create branches, and do not edit the backend. Source changes begin only within the frozen scope after exact paths and current source are reconciled.
