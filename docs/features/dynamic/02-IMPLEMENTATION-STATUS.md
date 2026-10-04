# CB-Partner Dynamic UI — Implementation Status

**Status:** IMPLEMENTATION IN PROGRESS  
**Updated:** 2026-10-04  
**Branch:** `feature/dynamic-ui`

DYNAMIC-01 → DYNAMIC-07 remain frozen. DYNAMIC-08 is frozen as a contract-alignment addition and is currently being implemented.

## 1. Frozen architecture status

- DYNAMIC-01 — FROZEN
- DYNAMIC-02 — FROZEN
- DYNAMIC-03 — FROZEN
- DYNAMIC-04 — FROZEN
- DYNAMIC-05 — FROZEN
- DYNAMIC-06 — FROZEN
- DYNAMIC-07 — FROZEN
- DYNAMIC-08 — FROZEN / IMPLEMENTATION IN PROGRESS

## 2. DYNAMIC-08 contract now implemented in source

The canonical destination contract is now:

```text
DynamicDestination
├── screenId
├── templateId
├── templateType
└── endpoint
```

Removed from the destination model:

```text
method
authentication
```

HTTP method is request metadata and is passed separately to the Dynamic repository boundary.

### Bootstrap

```text
data.startup.nextScreen
        ↓
data.startup.destination
        ↓
DynamicDestination
```

### Dynamic API response

```text
data.nextScreen
        ↓
data.destination
        ↓
DynamicResponse.destination
```

The screen-fetch response remains a screen document and no longer owns `DynamicScreen.nextScreen`.

### Direct navigation action

Canonical shape:

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

`destination` is beside `type`; it is not wrapped inside `payload`.

### Request action

Request-specific fields remain in `payload`:

```json
{
  "type": "request",
  "payload": {
    "method": "POST",
    "endpoint": "/api/v1/partner/auth/send_otp",
    "body": {},
    "responseMode": "destination"
  }
}
```

`responseMode = destination` resolves `DynamicResponse.destination` from `data.destination` and navigates using the same `DynamicDestination` model.

## 3. Source changes completed

- `domain.model.DynamicDestination` simplified to the four canonical fields.
- `DynamicResponse` now exposes optional `destination`.
- `DynamicScreen.nextScreen` removed.
- `DynamicAction` now has optional `destination` beside `type` and `payload`.
- `DynamicDestinationDto` aligned with the four-field backend contract.
- `DynamicActionDto` aligned with `destination` beside `payload`.
- `DynamicResponseMapper` maps `data.destination` and preserves arbitrary response data in `dataPayload`.
- `DynamicActionHandler` resolves direct navigation from `action.destination`.
- `ExecuteDynamicActionUseCase` resolves request navigation from `response.destination`.
- Dynamic repository request method is separated from destination identity.
- Bootstrap DTO/mapper/domain startup model now use `destination` instead of `nextScreen`.
- Application startup consumes `startup.destination`.
- Dynamic mapper, Bootstrap mapper, Dynamic action and Dynamic store tests were aligned with the new contract.

## 4. Documentation added

- `docs/features/dynamic/04-DYNAMIC-08-UNIFIED-DESTINATION-DISCUSSION.md`
- `docs/features/dynamic/05-DYNAMIC-08-IMPLEMENTATION-PLAN.md`

These documents define the new destination contract, direct navigation shape, response destination shape, and template ID convention.

## 5. Template ID convention

Recommended backend convention:

```text
tpl_<PartnerPrefix><Sequence><ScreenCode>
```

Examples:

```text
tpl_P101L  → Partner login
tpl_P102O  → Partner OTP
tpl_P103D  → Partner dashboard
tpl_P104P  → Partner profile
```

The frontend treats the ID as opaque and does not parse it.

## 6. Pending implementation / verification

| Area | Status |
|---|---|
| DYNAMIC-08 discussion | FROZEN |
| Destination domain model | IMPLEMENTED |
| Dynamic response destination mapping | IMPLEMENTED |
| Dynamic action destination mapping | IMPLEMENTED |
| Bootstrap startup destination mapping | IMPLEMENTED |
| Affected unit tests | IMPLEMENTED |
| Remove every stale `nextScreen` reference | PENDING VERIFICATION |
| Remove every stale `DynamicNextScreen` reference | PENDING VERIFICATION |
| Update all remaining `DynamicDestination(...)` call sites | PENDING VERIFICATION |
| Full JVM compile | PENDING LOCAL VERIFICATION |
| Full JVM tests | PENDING LOCAL VERIFICATION |
| Backend integration Login → Send OTP → OTP → Verify OTP | PENDING |
| Push/replace/pop/back verification | PENDING |
| DYNAMIC-07 production implementation | PENDING |
| Android verification | PENDING |
| iOS/macOS-hosted verification | PENDING |
| Web/Wasm verification | PENDING |
| Final Dynamic review/freeze | PENDING |

## 7. Important current baseline

The owner previously confirmed a successful JVM baseline before the latest destination model change. That baseline is not treated as proof for this change.

The next verification must rerun:

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

## 8. Rules for continuation

- Do not reopen DYNAMIC-01 → DYNAMIC-07.
- Do not add `DynamicNextScreen` back.
- Do not add `nextScreen` aliases to the canonical contract.
- Do not parse template IDs in the frontend.
- Do not create Login/OTP/Dashboard-specific screen classes.
- Do not put direct navigation destinations inside action payloads.
- Do not add method/authentication back into `DynamicDestination`.
- Keep request metadata in request action payload / repository request parameters.
- Update this status after the verification gate.

## 9. Next action

**Immediately complete the remaining source call-site cleanup for the four-field `DynamicDestination`, then run the full JVM compile/tests. If green, proceed directly to backend-shaped Login → Send OTP → OTP → Verify OTP integration verification.**
