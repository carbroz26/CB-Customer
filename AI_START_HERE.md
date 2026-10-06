# CB-Partner — AI START HERE

**Document type:** Global AI Control / Router  
**Status:** AUTHORITATIVE  
**Scope:** Entire CB-Partner frontend repository  
**Last Updated:** 2026-10-06

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

Read in this order:

1. `AI_START_HERE.md`
2. `docs/project-management/14-PROJECT-MASTER-STATE.md`
3. `docs/project-management/04-CURRENT-STATUS.md`
4. `docs/project-management/12-PROJECT-TRACKER.md`
5. `docs/project-management/03-DECISION-LOG.md`
6. `docs/project-management/01-AI-CONTEXT.md`
7. `docs/architecture/ARCHITECTURE-CURRENT-STATE.md`
8. `docs/architecture/BASE-ARCHITECTURE.md`
9. `docs/dynamic/DYNAMIC-ARCHITECTURE-FROZEN.md`
10. `docs/dynamic/DYNAMIC-DISCUSSION-AND-DECISIONS.md`
11. `docs/dynamic/DYNAMIC-IMPLEMENTATION-PLAN-FROZEN.md`
12. `docs/dynamic/DYNAMIC-IMPLEMENTATION-STATUS.md`
13. source/tests only after the state is understood

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
- DYNAMIC-08 Unified Destination Contract: **FROZEN / VERIFIED**
- Dynamic implementation plan: **PLAN_FROZEN / IMPLEMENTATION AUTHORIZED**
- Dynamic implementation: **VERIFIED / READY FOR DOCUMENTATION FREEZE**
- Bootstrap Option B: **FROZEN / REQUIRED IMPLEMENTATION**
- Canonical shared `DynamicDestination`: **FROZEN**
- Network exception taxonomy: **FROZEN / REQUIRED IMPLEMENTATION**
- Latest verified JVM gate: **BUILD SUCCESSFUL — 2026-10-06**
- Current branch: `feature/dynamic-ui`

## 6. CURRENT GRADLE AUTHORITY

Use `docs/build/GRADLE-SETUP-AND-WORKFLOW.md` for Gradle installation, Wrapper, version policy and build/test commands.

Current baseline: **Gradle 9.7.1**.

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

## 9. FEATURE LIFECYCLE — EXACTLY THREE PHASES

```text
1. FEATURE DISCUSSION
   Discuss + Research + Decide → Discussion Frozen

2. IMPLEMENTATION PLAN
   Complete feature plan → Plan Frozen

3. IMPLEMENTATION
   Implement completely → Test → Review → Feature Frozen
```

Implementation units are sequencing aids, not separate approval/freeze gates. Once a phase is frozen, move immediately to the next phase.

## 10. MODE ROUTER

### DISCUSS / RESEARCH / DECIDE

No source modification. Resolve requirements, boundaries, alternatives and decisions.

### FEATURE DISCUSSION FREEZE

Record exact frozen requirements, scope, non-goals and constraints, then reconcile the complete implementation plan.

### PLAN

One complete implementation contract: objective, scope, files/modules, dependencies, sequence, tests, acceptance, risks and documentation.

### PLAN FREEZE

Explicit approval authorizes implementation. Do not create unnecessary planning gates.

### IMPLEMENT

Verify frozen discussion + frozen plan + correct branch + safe Git state. Implement the complete feature, not speculative adjacent work.

### TEST

Run required compile/test/platform verification. Do not weaken tests to make them pass.

### REVIEW

Compare implementation with requirements, frozen decisions, plan, architecture, tests and scope. Classify findings; do not silently fix review findings without authority.

### DOCUMENT

Update authoritative durable records.

### STATUS / WHAT'S NEXT

Recover state and return exactly one next valid action.

## 11. DOCUMENT ROUTING

| Purpose | Authoritative document |
|---|---|
| AI entry/routing | `AI_START_HERE.md` |
| Compact cross-session state | `docs/project-management/14-PROJECT-MASTER-STATE.md` |
| Decisions | `docs/project-management/03-DECISION-LOG.md` |
| Current state | `docs/project-management/04-CURRENT-STATUS.md` |
| Tracker | `docs/project-management/12-PROJECT-TRACKER.md` |
| Current architecture | `docs/architecture/ARCHITECTURE-CURRENT-STATE.md` |
| Frozen base architecture | `docs/architecture/BASE-ARCHITECTURE.md` |
| Dynamic frozen architecture | `docs/dynamic/DYNAMIC-ARCHITECTURE-FROZEN.md` |
| Dynamic discussion/decisions | `docs/dynamic/DYNAMIC-DISCUSSION-AND-DECISIONS.md` |
| Dynamic frozen implementation plan | `docs/dynamic/DYNAMIC-IMPLEMENTATION-PLAN-FROZEN.md` |
| Dynamic current status | `docs/dynamic/DYNAMIC-IMPLEMENTATION-STATUS.md` |
| Gradle | `docs/build/GRADLE-SETUP-AND-WORKFLOW.md` |

There is no second active Dynamic documentation tree. Historical Dynamic records are superseded and are not implementation authority.

## 12. STARTUP / BOOTSTRAP NON-NEGOTIABLES

Splash and Bootstrap are one startup flow.

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
  ↓
BootstrapOutput
  ↓
DynamicDestination
```

- Bootstrap Option B is the required implementation boundary.
- Endpoint: `GET /api/v1/partner/config/bootstrap`.
- Headers are documented in the current Dynamic plan.
- `startup.destination` maps directly to `DynamicDestination`.
- `DefaultApplicationBootstrap` must not remain as a second direct HTTP path.
- `StartupConfig`, `NextScreenConfig` and standalone `BootstrapConfig` are legacy domain artifacts and must not remain as parallel contracts.

## 13. CANONICAL DESTINATION NON-NEGOTIABLE

There is one canonical `DynamicDestination` used by Bootstrap, Dynamic API response navigation, and direct Dynamic navigation actions.

```text
Bootstrap data.startup.destination ─────┐
Dynamic API data.destination ────────────┼──→ DynamicDestination → DynamicNavigationState
Dynamic action destination ──────────────┘
```

Canonical destination fields:

```text
screenId
templateId
templateType
endpoint
```

`templateId` identifies the concrete destination/back-stack entry. `templateType` selects the registered template renderer. The frontend treats the template ID as opaque and never parses it.

There is no `DynamicNextScreen` model, no canonical `nextScreen` field, and no destination `method` or `authentication` field.

For request actions, HTTP method and other request metadata remain inside action `payload` / repository request parameters.

Direct navigation is:

```json
{
  "type": "navigate",
  "destination": {
    "screenId": "partner_login",
    "templateId": "tpl_P101L",
    "templateType": "form_template",
    "endpoint": "/api/v1/partner/screen/auth_login"
  }
}
```

`destination` is beside `type`; it is not nested inside `payload`.

A request using `responseMode = destination` reads `data.destination` from the API response and navigates using the same destination model.

## 14. DYNAMIC NON-NEGOTIABLES

- Dynamic is backend-driven.
- No native Login/OTP/Dashboard screen architecture.
- One permanent Dynamic Container.
- Registry is explicit; no reflection/package scanning.
- Initial vocabulary: `stack_template`, `stack_component`, `stack_section`, `stack_group`, `text`, `image`.
- Frozen actions: `request`, `navigate`, `present`, `dismiss`, `state`, `external_uri`, `sequence`.
- Frozen references: `$literal`, `$binding`, `$context`, `$response`.
- No duplicate SDUI structural validator.
- No silent type substitution/property downgrade.
- Sensitive data, OTPs, credentials and tokens are never logged.

## 15. DYNAMIC USE-CASE OWNERSHIP

- `LoadDynamicScreenUseCase` is the Dynamic screen-loading use case.
- `ExecuteDynamicActionUseCase` does not exist and must not be reintroduced.
- Dynamic action execution belongs to `DynamicActionExecutor` and action-specific handlers/capabilities.
- Do not create a second feature-local domain architecture or replacement manager/coordinator/command layer.

## 16. NETWORK EXCEPTION NON-NEGOTIABLES

Frozen production classification:

```text
BadRequestException
UnauthorizedException
ForbiddenException
NotFoundException
ConflictException
ValidationException
RateLimitException
ServerException
NetworkConnectionException
TimeoutException
SerializationException
ProtocolException
UnknownNetworkException
```

Classification rules:

- HTTP status errors are distinct from connection errors.
- Timeout is distinct from connection failure.
- Serialization is distinct from HTTP failure.
- Protocol/malformed response is distinct from HTTP status.
- Unknown is the final fallback only.
- Business/API outcomes are not automatically transport exceptions.
- Inspect existing network error types before adding classes; do not duplicate equivalent models.

## 17. LOGIN → OTP CHECKPOINT

Known destinations and actions remain backend-driven. Do not create OTP-specific Store/render/navigation classes.

## 18. GIT ROUTING

Before source changes:

```powershell
git status
git branch --show-current
git diff --stat
```

Preserve user changes. Keep changes scoped. Inspect final diff. Never use destructive Git commands to resolve uncertainty.

## 19. NO SILENT ARCHITECTURE CHANGES

If implementation reveals a material architecture problem:

```text
STOP → REPORT → DISCUSS / RESEARCH → DECIDE → FREEZE → UPDATE PLAN → IMPLEMENT
```

## 20. STOP CONDITIONS

STOP when required context is missing, documents materially conflict, a frozen decision must change, implementation authority is missing, unexpected user changes exist, scope expands materially, or source contradicts documented architecture.

## 21. SESSION HANDOFF

At meaningful completion, update durable records and report:

```text
COMPLETED
FROZEN
IMPLEMENTED
VERIFIED
OPEN
BLOCKED
NEXT VALID ACTION
```

## 22. CURRENT NEXT ACTION

**Complete the final Dynamic documentation freeze/reconciliation. The implementation and current JVM verification are complete for the tested Bootstrap → Login → Send OTP → OTP → Verify flow.**

Do not reopen DYNAMIC-01 → DYNAMIC-08. Do not change production architecture to satisfy stale fixtures. The verified baseline is now documentation-freeze work only.

## 23. ONE-LINE EXTERNAL AI INSTRUCTION

> Read `AI_START_HERE.md` first. Recover state from the Master State, Current Status and Tracker. Follow frozen decisions and the current feature plan. Never guess, silently redesign, create extra gates, suppress documented gaps, or restart the project.
