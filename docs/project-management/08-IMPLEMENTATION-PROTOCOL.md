# CB-Partner — Implementation Protocol

**Status:** AUTHORITATIVE
**Last Updated:** 2026-10-04

## Preconditions

Implementation requires:

- understood requirement;
- accepted/frozen required decisions;
- complete approved implementation plan;
- READY execution state where Trello is required;
- correct branch;
- known repository state.

## AI Preflight

Before changing source:

1. Read `AI_START_HERE.md`.
2. Read the Master State, Current Status and Tracker.
3. Read the relevant frozen discussion and complete implementation plan.
4. Inspect source/tests/build configuration.
5. Inspect Git status/branch/diff.
6. Confirm the requested change belongs to the frozen feature plan.

## Complete-Feature Implementation Rule

Once the complete feature plan is frozen and implementation is authorized, implement the complete feature through its required implementation sequence.

Implementation units are sequencing labels only. They are **not** separate authorization or freeze gates.

Do not stop after each class/unit to request another approval when the work remains inside the frozen plan.

## Implementation Loop

1. Establish baseline.
2. Implement the next coherent part of the frozen feature.
3. Compile/check frequently.
4. Add/update focused tests.
5. Inspect the diff.
6. Continue through the remaining feature scope.
7. Run complete feature verification at the end.

## Forbidden Silent Changes

Do not silently:

- add material dependencies;
- change module boundaries;
- redesign architecture;
- refactor unrelated areas;
- rename broad APIs without need;
- weaken tests;
- invent missing backend contracts.

## Deviation Protocol

If a material deviation is required:

```text
STOP → identify conflict → explain impact → discuss/research → decide → freeze → update plan → continue
```

## Failure Handling

Classify failures as:

- current-change;
- pre-existing;
- environment/platform;
- unrelated.

Do not hide failures by deleting tests, weakening assertions or suppressing meaningful errors.

## Completion Report

Report:

- changed files;
- delivered behavior;
- tests/checks and results;
- platform verification;
- deviations;
- known limitations;
- documentation updated;
- Git state.

## Hard Stop

Stop when the frozen contract cannot be safely implemented without a material change.

## Antigravity Handoff

After a remote Git change, provide exact local fetch/pull/switch/verification commands for the user's Antigravity environment. Never assume local synchronization or local verification.

## No Silent Git Actions

Do not silently merge, tag, force-push, reset, overwrite user work or clean unrelated changes.
