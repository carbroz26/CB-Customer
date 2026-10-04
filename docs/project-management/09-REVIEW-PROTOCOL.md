# CB-Partner — Review Protocol

**Status:** FROZEN / CURRENT
**Last Updated:** 2026-10-04

## Purpose

Review verifies that the complete feature satisfies the approved requirement, frozen discussion and implementation plan without turning review into uncontrolled redesign.

## Review Timing

Review occurs after complete feature implementation and required testing. Do not create a separate review/freeze gate for every implementation unit.

## Review Sequence

1. Scope
2. Functional correctness
3. Frozen architecture
4. Store/MVI/UDF
5. KMP/platform compatibility
6. Lifecycle/concurrency/errors
7. Tests
8. Maintainability
9. Security/privacy where relevant
10. Documentation
11. Git diff

## Store/MVI/UDF Checks

- Store owns agreed state flow.
- UI renders state and emits events.
- Business logic is outside composables.
- Events flow inward and state flows outward.
- Side effects are controlled/testable.
- No ViewModel is introduced.

## Dynamic-Specific Checks

- No native Login/OTP/Dashboard screen branching.
- `templateType` is never used for business navigation decisions.
- Full `DynamicDestination` is used for navigation entries.
- Registry remains explicit.
- Frozen Dynamic vocabulary is respected.
- Renderer/action responsibilities remain separate.
- No duplicate SDUI structural validator was introduced.
- Unknown/unsupported definitions are not silently substituted.
- Dynamic failures remain contained.
- Sensitive diagnostics do not expose OTPs/tokens/credentials.

## Findings

- **BLOCKER** — cannot accept.
- **REQUIRED** — must fix before feature freeze.
- **OPTIONAL** — useful but not required.
- **FUTURE** — separate backlog/work item.

## Review Rules

Review the actual Git diff and test results, not only the final UI/result. Compare against the frozen plan. Do not request unrelated cleanup. Do not silently redesign architecture during review.

Review findings do not automatically authorize source changes. Required fixes remain within the feature implementation authority when they are already covered by the frozen plan; material scope/architecture changes require the reopening process.

## Acceptance

Acceptance occurs after blockers/required findings are resolved and required verification passes.

Then update:

- feature implementation status;
- Project Tracker;
- Current Status;
- Master State;
- Trello execution state;
- Git/PR state as applicable.
