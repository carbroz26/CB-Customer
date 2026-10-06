# CB-Partner Dynamic UI — Implementation Status

**Status:** ACTIVE STATUS ONLY  
**Updated:** 2026-10-06  
**Branch:** `feature/dynamic-ui`

This document records current implementation reality only. It is not a historical journal, architecture proposal, or decision log.

## 1. Current state

- Dynamic architecture: frozen.
- Dynamic implementation plan: frozen and authorized.
- Typed Dynamic UI contract migration: implemented on the current branch.
- Dynamic action boundary: reconciled to `DynamicActionExecutor`.
- `DynamicDestination`: four-field contract.
- `LoadDynamicScreenUseCase`: retained as the screen-loading use case.
- `ExecuteDynamicActionUseCase`: absent/prohibited.
- Stale backend-shaped JSON fixtures have been reconciled to the current DTO/source contract.
- Windows Gradle verification: pending in the developer checkout.

## 2. Current source reality

Current typed data hierarchy:

```
DynamicResponseDto
  ↓
DynamicScreenDto
  ↓
FormTemplateDto
  ↓
StackComponentDto
  ↓
StackSectionDto
  ↓
StackGroupDto
  ↓
typed element DTO
  ↓
typed domain model
```

Current response mapping separates:

```
screen data → DynamicScreen
data.destination → DynamicDestination
non-screen data → DynamicValue payload
```

The current source does not require an empty template instance before API loading.

## 3. Documentation baseline

The only active Dynamic documentation is:

1. `DYNAMIC-ARCHITECTURE-FROZEN.md`
2. `DYNAMIC-DISCUSSION-AND-DECISIONS.md`
3. `DYNAMIC-IMPLEMENTATION-PLAN-FROZEN.md`
4. `DYNAMIC-IMPLEMENTATION-STATUS.md`

Older `docs/features/dynamic/*` records and dated reconciliation/freeze files are superseded and must not be used as active implementation authority.

## 4. Current test work

### Completed

- Typed DTO/domain mapper reconciliation.
- Bootstrap network failure assertion reconciliation.
- Typed DynamicContractTest imports/assertions.

### Current test work

- Reusable backend-shaped Login, Send OTP and OTP fixtures are now aligned to the current DTO contract.
- Dynamic response mapper contract tests now use the shared fixtures.
- Action reference resolver coverage now covers `$binding`, `$context`, `$response` and `$literal`.
- RequestActionHandler coverage now verifies the OTP verify request body from binding/context/previous-response references.
- Existing Store coverage continues to verify binding/context request resolution and destination convergence.
- `DynamicBackendFlowContractTest` now covers Bootstrap → Login → Send OTP → OTP as one backend contract chain.

### Next

1. Run `:data:jvmTest`.
2. Run `:feature:dynamic:jvmTest`.
3. Run broader compile/test gates.
4. If the full flow exposes a cross-destination `$response` lifetime problem, stop and reconcile that contract before changing production behavior.
5. Review and freeze Dynamic implementation status.

## 5. Known fixture corrections

The current stale tests have these known structural problems:

- Login/OTP fixtures place `theme` under `template`; the current DTO expects screen-level `data.theme`.
- The mapper destination fixture places `destination` under `template`; the current response contract expects `data.destination`.
- Existing minimal fixtures omit backend fields needed to test the intended Login/OTP contract.

Production mapper changes are not justified by these fixture mismatches.

## 6. Verification status

Do not mark tests green without an actual Gradle run.

Required developer-side commands remain:

```
./gradlew.bat :data:jvmTest
./gradlew.bat :feature:dynamic:jvmTest
./gradlew.bat :data:compileKotlinJvm
./gradlew.bat :feature:dynamic:compileKotlinJvm
```

## 7. Next valid action

Run the data and Dynamic JVM test suites from the Windows developer checkout. The fixture-level JSON decoding failures have been addressed without changing production DTO/mapper architecture.
