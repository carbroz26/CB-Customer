# CB-Partner Dynamic UI — Implementation Status

## Status

**IMPLEMENTATION IN PROGRESS**

Implementation is being performed strictly against:

`docs/features/dynamic/01-IMPLEMENTATION-PLAN.md`

No silent deviation is permitted.

## Current State

### Discussion

- DYNAMIC-01 — FROZEN
- DYNAMIC-02 — FROZEN
- DYNAMIC-03 — FROZEN
- DYNAMIC-04 — FROZEN
- DYNAMIC-05 — FROZEN
- DYNAMIC-06 — FROZEN
- DYNAMIC-07 — FROZEN

### Plan

- `01-IMPLEMENTATION-PLAN.md` — PLAN FROZEN / IMPLEMENTATION AUTHORIZED

## Completed Implementation Work

1. Added the complete Dynamic implementation plan.
2. Restricted `DynamicRegistry` to the initial vocabulary:
   - `stack_template`
   - `stack_component`
   - `stack_section`
   - `stack_group`
   - `text`
   - `image`
3. Added duplicate-registration protection.
4. Removed `FormTemplate`, `InputElement`, and `ButtonElement` from the Dynamic implementation.
5. Simplified `DynamicRenderer` to the initial definitions and destination identity checks.
6. Added `LoadDynamicScreenUseCase`.
7. Added `ExecuteDynamicActionUseCase`.
8. Reduced `DynamicStore` to MVI/UDF orchestration and removed its direct request-building/network logic.
9. Moved generic binding/context request-value resolution into the action use-case boundary.
10. Removed the hardcoded test device ID from `DynamicStore`.
11. Kept navigation and external URI as separate Dynamic effects.
12. Removed transport headers from the Dynamic repository contract so common transport metadata remains below the Dynamic/domain boundary.
13. Moved Dynamic request-body JSON serialization from the use-case into the data repository implementation.
14. Kept request-body resolution as domain values until the data boundary.
15. Added focused action-use-case coverage for binding/context resolution and destination response handling.
16. Updated Store tests to verify that the Store/use-case boundary passes resolved domain values rather than serialized JSON.
17. Added destination screen/template identity validation before Dynamic rendering.
18. Updated `DynamicScreen` to reset/load the Store for each destination and pass the complete destination into rendering.
19. Changed Bootstrap `StartupConfig.nextScreen` to use the canonical `DynamicDestination` domain model instead of the duplicate `NextScreenConfig` model.
20. Updated `BootstrapMapper` to construct `DynamicDestination` directly from `startup.nextScreen`.
21. Added a focused Bootstrap mapper test proving all destination fields are preserved.

## Bootstrap Integration Checkpoint

Completed for the domain/data boundary:

```text
Bootstrap DTO
    ↓
BootstrapMapper
    ↓
BootstrapOutput.startup.nextScreen
    ↓
DynamicDestination
```

The canonical destination model now owns the destination contract. No second bootstrap destination model remains in `StartupConfig`.

The actual application-level navigation/back-stack handoff is intentionally still pending; it must be implemented against the existing navigation/application composition boundary rather than invented inside Bootstrap or Dynamic data.

## Remaining Implementation Work

- verify DI/wiring for Store and use cases;
- complete common request metadata integration using the existing Runtime Infrastructure boundary, without modifying the frozen BASE-ARCH-017 contract;
- complete application composition/navigation handoff from Bootstrap `DynamicDestination`;
- verify destination/back-stack integration;
- complete focused Dynamic tests;
- run Gradle build/test verification;
- perform literal plan-to-source compliance audit;
- complete review and feature freeze.

## Verification

Not yet complete. No final feature-freeze claim is valid until the complete implementation has passed the required tests/build and the literal plan-to-source audit.

## Deviations

None intentionally accepted.

## Next Action

Continue implementation against `01-IMPLEMENTATION-PLAN.md` without introducing an additional planning or approval gate.
