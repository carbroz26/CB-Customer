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
- Current known test issue before fixture reconciliation: stale backend-shaped test JSON caused `JsonDecodingException` failures.
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

### Next

1. Replace stale Login/OTP/mapper JSON with fixtures matching the current DTO contract.
2. Run `:data:jvmTest`.
3. Add/complete backend-shaped Bootstrap → Login → Send OTP → OTP → Verify coverage.
4. Run `:feature:dynamic:jvmTest`.
5. Run broader compile/test gates.
6. Review and freeze Dynamic implementation status.

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

Repair the backend-shaped test fixtures against the current DTO/source contract, then run the data and Dynamic JVM test suites. No production architecture change is required by the fixture failures identified in the source-first audit.
