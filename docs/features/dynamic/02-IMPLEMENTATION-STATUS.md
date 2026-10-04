# CB-Partner Dynamic UI — Implementation Status

**Status:** IMPLEMENTATION IN PROGRESS  
**Updated:** 2026-10-04  
**Branch:** `feature/dynamic-ui`

DYNAMIC-01 → DYNAMIC-07 remain frozen. DYNAMIC-08 is frozen as the unified destination contract and its source/test alignment is now JVM verified.

## 1. Frozen architecture status

- DYNAMIC-01 — FROZEN
- DYNAMIC-02 — FROZEN
- DYNAMIC-03 — FROZEN
- DYNAMIC-04 — FROZEN
- DYNAMIC-05 — FROZEN
- DYNAMIC-06 — FROZEN
- DYNAMIC-07 — FROZEN
- DYNAMIC-08 — FROZEN / SOURCE ALIGNED / JVM VERIFIED

## 2. DYNAMIC-08 canonical contract

The canonical destination contract is:

```text
DynamicDestination
├── screenId
├── templateId
├── templateType
└── endpoint
```

Removed from destination identity:

```text
method
authentication
```

HTTP method and authentication are request metadata and are handled outside destination identity.

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
data.nextScreen / data.destination
        ↓
DynamicResponse.destination
```

The screen-fetch response is a screen document and does not own `DynamicScreen.nextScreen`.

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

`responseMode = destination` resolves `DynamicResponse.destination` and navigates using the same `DynamicDestination` model.

## 3. Source changes completed and verified

- `domain.model.DynamicDestination` uses the four canonical fields.
- `DynamicResponse` exposes optional `destination`.
- `DynamicScreen.nextScreen` removed.
- `DynamicAction` has optional `destination` beside `type` and `payload`.
- `DynamicDestinationDto` aligned with the four-field backend contract.
- `DynamicActionDto` aligned with `destination` beside `payload`.
- `DynamicResponseMapper` maps destination and preserves arbitrary response data in `dataPayload`.
- `DynamicActionHandler` resolves direct navigation from `action.destination`.
- `ExecuteDynamicActionUseCase` resolves request navigation from `response.destination`.
- Dynamic repository request method is separated from destination identity.
- Bootstrap DTO/mapper/domain startup model use `destination` instead of `nextScreen` at the domain boundary.
- Application startup consumes `startup.destination`.
- Dynamic mapper, Bootstrap mapper, Dynamic action and Dynamic store tests were aligned with the new contract.
- Affected compile/test failures were resolved and the owner-provided JVM verification passed.

## 4. Verification — 2026-10-04

Compile verification:

```text
:domain:compileKotlinJvm
:data:compileKotlinJvm
:feature:splash:compileKotlinJvm
:feature:dynamic:compileKotlinJvm
:navigation:compileKotlinJvm
:app:compileKotlinJvm
```

Result: **BUILD SUCCESSFUL — 43 actionable tasks: 43 executed**.

JVM verification:

```text
:core:jvmTest
:domain:jvmTest
:data:jvmTest
:navigation:jvmTest
:feature:splash:jvmTest
:feature:dynamic:jvmTest
:app:compileKotlinJvm
```

Result: **BUILD SUCCESSFUL — 62 actionable tasks: 62 executed**.

Known Windows warnings about disabled iOS simulator tests and Android host-test configuration are non-fatal.

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

## 6. Remaining implementation / verification

| Area | Status |
|---|---|
| DYNAMIC-08 discussion | FROZEN |
| Destination domain model | IMPLEMENTED / VERIFIED |
| Dynamic response destination mapping | IMPLEMENTED / VERIFIED |
| Dynamic action destination mapping | IMPLEMENTED / VERIFIED |
| Bootstrap startup destination mapping | IMPLEMENTED / VERIFIED |
| Affected unit tests | PASSING |
| Stale destination-model references | VERIFIED FOR CURRENT JVM BUILD |
| Full JVM compile set | PASSING |
| Selected JVM test set | PASSING |
| Bootstrap Option B | NEXT IMPLEMENTATION |
| Bootstrap legacy-domain cleanup | PENDING |
| Bootstrap DI/test reconciliation | PENDING |
| Canonical destination runtime hand-off | PENDING |
| Dynamic use-case package cleanup | PENDING |
| Request metadata/authentication | PENDING |
| Network exception taxonomy implementation | PENDING |
| DYNAMIC-07 production implementation | PENDING |
| Backend integration Login → Send OTP → OTP → Verify OTP | PENDING |
| Push/replace/pop/back verification | PENDING |
| Android verification | PENDING |
| iOS/macOS-hosted verification | PENDING |
| Web/Wasm verification | PENDING |
| Literal plan-to-source compliance review | PENDING |
| Final Dynamic review/freeze | PENDING |

## 7. Rules for continuation

- Do not reopen DYNAMIC-01 → DYNAMIC-08.
- Do not add `DynamicNextScreen` back.
- Do not add `nextScreen` aliases to the canonical domain contract.
- Do not parse template IDs in the frontend.
- Do not create Login/OTP/Dashboard-specific screen classes.
- Do not put direct navigation destinations inside action payloads.
- Do not add method/authentication back into `DynamicDestination`.
- Keep request metadata outside destination identity.
- Start the next authorized implementation item rather than reopening completed discussion.

## 8. Next action

**Start Bootstrap Option B implementation/reconciliation. After it is complete, verify the runtime hand-off into the same four-field `DynamicDestination`, then continue the remaining ordered Dynamic gaps.**
