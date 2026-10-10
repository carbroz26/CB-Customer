# CB-Partner — AI START HERE

**Status:** AUTHORITATIVE GLOBAL ROUTER
**Last Updated:** 2026-10-10

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

As of 2026-10-10:

- BASE-ARCH-001 → BASE-ARCH-017: frozen.
- BASE-ARCH-014: code-frozen.
- BASE-ARCH-017: verified/review-accepted/code-frozen.
- Dynamic architecture and implementation plan: frozen.
- Original Dynamic runtime/UI source audit and JVM matrix: previously passed.
- Local Dynamic asset fallback amendment: implemented on `feature/dynamic-ui`; current verification is pending.
- Current branch: `feature/dynamic-ui`.
- Latest remote Dynamic commit: `b7aab20`.
- Local Dynamic asset fallback verification: current task; do not claim final freeze until tests/build and source audit pass.
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
