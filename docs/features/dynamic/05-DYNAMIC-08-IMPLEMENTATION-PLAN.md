# DYNAMIC-08 — Unified Destination Implementation Plan

## Status

**IMPLEMENTATION IN PROGRESS**

Discussion is frozen in `04-DYNAMIC-08-UNIFIED-DESTINATION-DISCUSSION.md`.

## Scope

Align the frontend runtime with the new backend JSON contract without reopening DYNAMIC-01 → DYNAMIC-07.

### Contract changes

```text
Bootstrap:
startup.nextScreen → startup.destination

API navigation response:
data.nextScreen → data.destination

Direct navigate action:
type + payload.destination → type + destination

Destination:
screenId + templateId + templateType + endpoint
```

## Implementation order

1. Update the canonical `DynamicDestination` domain model.
2. Update Dynamic response DTO/domain models so destination is a response-level convenience mapped from `data.destination`, while screen data remains a pure screen document.
3. Update Dynamic action DTO/domain models so direct navigation owns `destination` beside `type`; request-specific fields remain in `payload`.
4. Update `DynamicResponseMapper` to map `data.destination` and remove `DynamicScreen.nextScreen`.
5. Update `DynamicActionHandler` and `ExecuteDynamicActionUseCase` to resolve direct destinations and request-response destinations from the new contract.
6. Update Bootstrap DTO/domain mapping from `startup.nextScreen` to `startup.destination`.
7. Update affected unit tests with backend-shaped JSON.
8. Remove stale `DynamicNextScreen` references/imports.
9. Verify no production source still reads `nextScreen` or expects destination `method`/`authentication`.
10. Run compile and JVM tests for affected modules.

## Non-goals

- No renderer redesign.
- No navigation architecture redesign.
- No new screen-specific classes.
- No new action framework.
- No compatibility alias unless a real supported legacy backend response requires it.
- No changes to Bootstrap business behavior, Dynamic rendering, registry behavior, or observability behavior.

## Verification

Required:

```text
:domain:compileKotlinJvm
:data:compileKotlinJvm
:feature:splash:compileKotlinJvm
:feature:dynamic:compileKotlinJvm
:navigation:compileKotlinJvm
:app:compileKotlinJvm

:data:jvmTest
:domain:jvmTest
:feature:splash:jvmTest
:feature:dynamic:jvmTest
:navigation:jvmTest
```

Focused assertions:

- Bootstrap maps `startup.destination` to `DynamicDestination`.
- Send OTP / verify OTP style responses map `data.destination`.
- `$response` still resolves response data such as `data.challengeId`.
- Direct navigate action resolves `action.destination`.
- Request `responseMode = destination` navigates using `response.destination`.
- Destination contains exactly the four canonical fields.
- No `DynamicNextScreen` production/test references remain.
