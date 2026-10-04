# CB-Partner — AI START HERE

**Document type:** Global AI Control / Router
**Status:** AUTHORITATIVE
**Scope:** Entire CB-Partner frontend repository
**Last Updated:** 2026-10-04

## 1. NON-NEGOTIABLE RULE

Read this document before taking project action. Determine the documented project state before coding. Never guess, silently redesign, or reconstruct the project from historical chat when a durable record exists.

## 2. PROJECT IDENTITY

- Project: `CB-Partner`
- Repository: `carbroz26/CB-Partner`
- Frontend/partner application only.
- Backend is a separate repository and documentation system.

## 3. AUTHORITATIVE ORDER

When information conflicts:

1. Current explicit user instruction
2. `AI_START_HERE.md`
3. Explicitly accepted/frozen decisions
4. Current architecture reconciliation / technical contracts
5. Current project status
6. Approved feature/module implementation plan
7. Feature implementation status
8. Trello execution state
9. Current source/tests
10. Historical conversation

If a frozen decision conflicts materially with a current implementation, **STOP → REPORT → DISCUSS → DECIDE → FREEZE → UPDATE PLAN → IMPLEMENT**. Never silently rewrite a frozen decision.

## 4. REQUIRED NEW-CHAT RECOVERY

For a new/recovered session, read in this order:

1. `AI_START_HERE.md`
2. `docs/project-management/14-PROJECT-MASTER-STATE.md`
3. `docs/project-management/04-CURRENT-STATUS.md`
4. `docs/project-management/12-PROJECT-TRACKER.md`
5. `docs/project-management/03-DECISION-LOG.md`
6. `docs/project-management/01-AI-CONTEXT.md`
7. `docs/architecture/ARCHITECTURE-CURRENT-STATE.md`
8. `docs/architecture/BASE-ARCHITECTURE.md` for frozen base decisions
9. relevant feature discussion/plan/status documents
10. source/tests only after the state is understood

Then produce a short SESSION RECOVERY SUMMARY:

- Current phase
- Current feature/work item
- Frozen decisions
- Completed implementation
- Latest verification
- Pending work
- Blockers/conflicts
- Next valid action

Do not start coding before this state is clear.

## 5. CURRENT PROJECT SNAPSHOT

As of 2026-10-04:

- BASE-ARCH-001 → BASE-ARCH-017: **DECISION_FROZEN**
- BASE-ARCH-014: **CODE_FROZEN**
- BASE-ARCH-017: **VERIFIED / REVIEW ACCEPTED / CODE_FROZEN**
- DYNAMIC-01 → DYNAMIC-07: **DISCUSSION FROZEN**
- Dynamic implementation plan: **PLAN_FROZEN / IMPLEMENTATION AUTHORIZED**
- Dynamic implementation: **IN PROGRESS**
- Bootstrap Option B: **SELECTED / REQUIRED IMPLEMENTATION**
- Latest owner-provided focused compile/JVM test gate: **BUILD SUCCESSFUL**
- Current branch: `feature/dynamic-ui`

The compact recovery source is:

`docs/project-management/14-PROJECT-MASTER-STATE.md`

## 6. CURRENT GRADLE AUTHORITY

For Gradle installation, Wrapper, version policy and build/test commands use:

`docs/build/GRADLE-SETUP-AND-WORKFLOW.md`

Current project baseline: **Gradle 9.7.1**.

## 7. CURRENT MODULE TOPOLOGY

```text
:core
:domain
:network
:data
:navigation
:app
:feature:splash
:feature:dynamic
:androidApp
:webApp
build-logic (included build)
iosApp/ (native Xcode boundary, not Gradle)
```

Do not reintroduce `:desktopApp` based only on older Base Architecture wording. The current implementation topology is recorded in `ARCHITECTURE-CURRENT-STATE.md`. Any material platform change requires an explicit architecture decision.

## 8. FROZEN TECHNICAL DIRECTION

- Kotlin Multiplatform
- Compose Multiplatform
- Clean Architecture
- Pure Store-based MVI
- UDF
- No ViewModel
- Multi-module architecture
- Koin DI
- Ktor Client
- kotlinx.serialization
- Kermit
- Navigation 3
- Gradle Convention Plugins

Do not add a library or architecture pattern just because it is convenient. Check the frozen decision/feature contract first.

## 9. FEATURE LIFECYCLE — EXACTLY THREE PHASES

Every significant feature/module follows:

```text
1. FEATURE DISCUSSION
   Discuss + Research + Decide → Discussion Frozen

2. IMPLEMENTATION PLAN
   Complete feature plan → Plan Frozen

3. IMPLEMENTATION
   Implement completely → Test → Review → Feature Frozen
```

Implementation units are sequencing aids inside a feature. They are **not** separate approval/freeze gates.

Once a phase is frozen, immediately move to the next phase. Do not create unnecessary unit-level gates.

## 10. MODE ROUTER

### DISCUSS / RESEARCH / DECIDE

No source modification. Resolve requirements, boundaries, alternatives and decisions.

### FEATURE DISCUSSION FREEZE

Record exact frozen requirements, scope, non-goals and constraints, then immediately create/reconcile the complete implementation plan.

### PLAN

One complete implementation contract: objective, scope, files/modules, dependencies, sequence, tests, acceptance, risks and documentation.

### PLAN FREEZE

Explicit approval authorizes implementation. Do not create another planning gate for each unit.

### IMPLEMENT

Verify frozen discussion + frozen plan + correct branch + safe Git state. Implement the complete feature, not speculative adjacent work.

### TEST

Run the required compile/test/platform verification. Do not weaken tests to make them pass.

### REVIEW

Compare implementation with requirements, frozen decisions, plan, architecture, tests and scope. Classify findings; do not silently fix review findings without authority.

### DOCUMENT

Update only authoritative durable records.

### STATUS / WHAT'S NEXT

Recover state and return exactly one next valid action.

## 11. DOCUMENT ROUTING

| Purpose | Authoritative document |
|---|---|
| AI entry/routing | `AI_START_HERE.md` |
| Compact cross-session state | `docs/project-management/14-PROJECT-MASTER-STATE.md` |
| AI rules | `docs/AI_GUIDELINES.md`, `docs/project-management/02-AI-RULES.md` |
| Stable context | `docs/project-management/01-AI-CONTEXT.md` |
| Decisions | `docs/project-management/03-DECISION-LOG.md` |
| Current state | `docs/project-management/04-CURRENT-STATUS.md` |
| Roadmap | `docs/project-management/05-ROADMAP.md` |
| Trello | `docs/project-management/06-TRELLO-WORKFLOW.md` |
| Development lifecycle | `docs/project-management/07-DEVELOPMENT-WORKFLOW.md` |
| Implementation protocol | `docs/project-management/08-IMPLEMENTATION-PROTOCOL.md` |
| Review protocol | `docs/project-management/09-REVIEW-PROTOCOL.md` |
| Recovery/checkpoints | `docs/project-management/10-CHECKPOINTS.md` |
| Git/terminal | `docs/project-management/11-TERMINAL-GIT-COMMANDS.md` |
| Execution ledger | `docs/project-management/12-PROJECT-TRACKER.md` |
| Branching | `docs/project-management/13-GIT-BRANCHING-WORKFLOW.md` |
| Current architecture | `docs/architecture/ARCHITECTURE-CURRENT-STATE.md` |
| Frozen base architecture | `docs/architecture/BASE-ARCHITECTURE.md` |
| Architecture index | `docs/architecture/ARCHITECTURE.md` |
| Frontend engineering | `docs/frontend/FRONTEND.md` |
| API integration | `docs/api/API_CONTRACT.md` |
| Dynamic discussion | `docs/features/dynamic/00-FEATURE-DISCUSSION.md` |
| Dynamic implementation plan | `docs/features/dynamic/01-IMPLEMENTATION-PLAN.md` |
| Dynamic implementation status | `docs/features/dynamic/02-IMPLEMENTATION-STATUS.md` |
| Gradle | `docs/build/GRADLE-SETUP-AND-WORKFLOW.md` |

## 12. FEATURE DOCUMENT STRUCTURE

Every significant feature uses:

```text
docs/features/<feature>/
├── 00-FEATURE-DISCUSSION.md
├── 01-IMPLEMENTATION-PLAN.md
└── 02-IMPLEMENTATION-STATUS.md
```

These are the durable feature records. Do not create separate unit-level freeze/authorization documents unless explicitly required by an accepted decision.

## 13. DYNAMIC DOCUMENT AUTHORITY

For Dynamic work, follow this order:

1. `docs/features/dynamic/00-FEATURE-DISCUSSION.md` — frozen DYNAMIC-01 → DYNAMIC-07 contract
2. `docs/features/dynamic/01-IMPLEMENTATION-PLAN.md` — complete implementation contract
3. `docs/features/dynamic/02-IMPLEMENTATION-STATUS.md` — actual implementation/verification state
4. `docs/project-management/14-PROJECT-MASTER-STATE.md` — cross-session map
5. current source/tests

Do not use old pasted/review-draft SDUI documents as implementation authority. They may contain useful historical reasoning, but the frozen Dynamic discussion is authoritative.

## 14. STARTUP / BOOTSTRAP NON-NEGOTIABLES

- Splash and Bootstrap are one startup flow, not two separate product features.
- Splash is the visible startup UI; Bootstrap is the config/startup execution behind it.
- `ApplicationBootstrap` is the startup-facing domain contract.
- Bootstrap uses the selected Option B layering:

```text
ApplicationBootstrap
  ↓
BootstrapRepository
  ↓
BootstrapRepositoryImpl
  ↓
BootstrapRemoteDataSource
  ↓
RequestHandler
```

- Bootstrap endpoint: `GET /api/v1/partner/config/bootstrap`.
- Bootstrap request headers are documented in the Dynamic implementation plan/current state.
- `startup.nextScreen` becomes the canonical `DynamicDestination`.
- Do not retain a second direct Bootstrap HTTP path after implementation.
- Do not invent a separate Bootstrap UI architecture.

## 15. DYNAMIC NON-NEGOTIABLES

- Dynamic is backend-driven.
- No native Login/OTP/Dashboard screen architecture.
- `templateType` selects rendering; it never selects business navigation.
- Full `DynamicDestination` is the navigation/back-stack entry.
- One permanent Dynamic Container.
- Registry is explicit; no reflection/package scanning.
- Initial registered vocabulary: `stack_template`, `stack_component`, `stack_section`, `stack_group`, `text`, `image`.
- Definition rendering and action execution are separate.
- Dynamic state is destination-scoped and separate from business/domain state.
- Frozen actions: `request`, `navigate`, `present`, `dismiss`, `state`, `external_uri`, `sequence`.
- Frozen references: `$literal`, `$binding`, `$context`, `$response`.
- No duplicate SDUI structural validator.
- No silent type substitution/property downgrade.
- Runtime failures are contained and observable through the existing logger.
- Sensitive data, OTPs, credentials and tokens are never logged.

## 16. DYNAMIC USE-CASE OWNERSHIP

The existence of a UseCase class does not mean it belongs in the top-level `:domain` module.

- `LoadDynamicScreenUseCase` is feature orchestration over the Dynamic repository contract.
- `ExecuteDynamicActionUseCase` coordinates feature-owned `DynamicActionHandler` capabilities.
- Keep these use cases in `:feature:dynamic`.
- Current `feature/dynamic/domain/usecase` naming is a cleanup target; target package is `feature/dynamic/usecase`.
- Do not move feature-owned capabilities into `:domain` merely for naming symmetry.

## 17. LOGIN → OTP CHECKPOINT

Known Dynamic destinations:

```text
partner_login
  tpl_7K2M9Q
  form_template
  GET /api/v1/partner/screen/auth_login

partner_otp
  tpl_P6X8N3
  form_template
  GET /api/v1/partner/screen/auth_otp
```

Known OTP actions:

```text
POST /api/v1/partner/auth/send_otp
  phoneNumber = $context(authFlow.phoneNumber)
  deviceId    = $context(deviceId)
  responseMode = none

POST /api/v1/partner/auth/verify_otp
  challengeId = $response(data.challengeId)
  phoneNumber = $context(authFlow.phoneNumber)
  otp         = $binding(otp)
  deviceId    = $context(deviceId)
  responseMode = destination
```

These are backend-driven Dynamic contracts, not permission to create OTP-specific frontend architecture.

## 18. GIT ROUTING

Before source changes:

```powershell
git status
git branch --show-current
git diff --stat
```

Preserve user changes. Keep changes scoped. Inspect the final diff. Never use destructive Git commands to resolve uncertainty.

Normal feature flow:

```text
development
   ↓
new feature branch
   ↓
implementation
   ↓
test/review
   ↓
merge through approved workflow
```

Do not continue from stale branches without checking current project state.

## 19. NO SILENT ARCHITECTURE CHANGES

If implementation reveals a material architecture problem:

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

A better idea is not permission to redesign a frozen boundary.

## 20. ANTI-RESTART

Classify problems before restarting anything:

1. Bug
2. Local design problem
3. Refactoring opportunity
4. Feature boundary problem
5. Architecture problem
6. Invalid fundamental assumption

Prefer fix/refactor/migrate/recover. Do not restart from scratch merely because implementation is imperfect.

## 21. STOP CONDITIONS

STOP when:

- required context is missing;
- documents materially conflict;
- a frozen decision must change;
- the implementation plan is missing;
- implementation authority is missing;
- unexpected user changes exist;
- a destructive operation is proposed;
- a material dependency is unexpectedly required;
- scope expands materially;
- source contradicts documented architecture;
- a significant assumption is required.

## 22. SESSION HANDOFF

At meaningful completion, update the relevant status/decision/feature records and report:

```text
COMPLETED
FROZEN
IMPLEMENTED
VERIFIED
OPEN
BLOCKED
NEXT VALID ACTION
```

## 23. CURRENT NEXT ACTION

**Implement Option B Bootstrap repository/remote-source boundary first. Then continue every remaining documented Dynamic gap, followed by Login → OTP integration verification, platform verification, literal plan-to-source review, final Dynamic review and feature freeze.**

Do not reopen DYNAMIC-01 → DYNAMIC-07. Do not mark the feature complete because the JVM baseline is green.

## 24. ONE-LINE EXTERNAL AI INSTRUCTION

> Read `AI_START_HERE.md` first. Recover state from the Master State, Current Status and Project Tracker. Follow frozen decisions and the current feature plan. Never guess, silently redesign, create extra gates, suppress documented gaps, or restart the project.
