# CB-Partner — Frontend API Integration Contract

**Status:** ACTIVE — backend specification remains authoritative
**Scope:** Frontend repository only
**Last Updated:** 2026-10-04

## 1. Authority

The backend repository owns the authoritative API specification. This document records frontend integration rules and the backend contracts currently known to the frontend project.

Do not invent missing backend fields, endpoints or error semantics.

## 2. Boundary

UI and Store code do not perform raw HTTP operations. Network access belongs behind the approved `:data` / `:network` boundary.

```text
UI → Store → Use Case → Repository → Remote Data Source → RequestHandler → Ktor
```

## 3. Current Network Boundary

`:network` owns:

- Ktor `HttpClient` construction;
- `RequestHandler`;
- generic network result/error contracts;
- network DI.

`:data` owns feature-specific remote sources, DTOs, mapping and repository implementations.

## 4. Bootstrap Endpoint Currently Implemented

```text
GET /api/v1/partner/config/bootstrap
```

Known request headers:

```text
X-CarBroz-Platform: ANDROID
X-CarBroz-App-Version: 1.0.0
X-CarBroz-Build-Number: 1
```

The success response contains `data.config` and `data.startup.nextScreen`.

The frontend maps `nextScreen` directly to the canonical `DynamicDestination`:

```text
screenId
templateId
templateType
endpoint
method
authentication
```

## 5. Login Screen Contract

```text
GET /api/v1/partner/screen/auth_login
```

Destination:

```text
screenId      = partner_login
templateId    = tpl_7K2M9Q
templateType  = form_template
method        = GET
authentication = NONE
```

The Login screen is Dynamic UI. No native Login screen architecture is created.

## 6. OTP Screen Contract

```text
GET /api/v1/partner/screen/auth_otp
```

Destination:

```text
screenId      = partner_otp
templateId    = tpl_P6X8N3
templateType  = form_template
method        = GET
authentication = NONE
```

## 7. Send OTP Action Contract

```text
POST /api/v1/partner/auth/send_otp
```

Authentication:

```text
NONE
```

Request body values:

```text
phoneNumber = $context(authFlow.phoneNumber)
deviceId    = $context(deviceId)
```

Response mode:

```text
none
```

## 8. Verify OTP Action Contract

```text
POST /api/v1/partner/auth/verify_otp
```

Authentication:

```text
NONE
```

Request body values:

```text
challengeId = $response(data.challengeId)
phoneNumber = $context(authFlow.phoneNumber)
otp         = $binding(otp)
deviceId    = $context(deviceId)
```

Response mode:

```text
destination
```

A successful response therefore produces a `DynamicDestination`. The frontend must not infer the destination from `screenId` or `templateType`.

## 9. Dynamic Action Contract

Frozen action vocabulary:

```text
request
navigate
present
dismiss
state
external_uri
sequence
```

Frozen reference vocabulary:

```text
$literal
$binding
$context
$response
```

References are resolved before request validation/execution where applicable.

## 10. Request Errors

Dynamic request failures use a structured error model. HTTP status is retained as metadata; it is not the complete Dynamic error taxonomy.

Errors do not automatically create navigation destinations.

## 11. DTO Rule

Transport DTOs represent backend data. They do not automatically become domain models.

Explicit mapping remains the default:

```text
DTO → Mapper → Domain
```

Do not add a generic mapper framework.

## 12. Authentication / Security

Authentication follows the backend contract. Credentials, tokens and OTPs must never be logged. Sensitive values should not be copied into UI state unless the Dynamic contract requires them for the current operation.

## 13. Retries

No blanket automatic retry is permitted. Retry behavior must be defined by the relevant action/request contract.

## 14. Current API Scope

Known frontend-integrated flow:

```text
Bootstrap
  ↓
Login destination
  ↓
Login screen GET
  ↓
Send OTP POST
  ↓
OTP destination
  ↓
OTP screen GET
  ↓
Verify OTP POST
  ↓
Destination response
```

The full real-backend flow is not yet verified end-to-end.

## 15. Testing

Current JVM verification covers network, bootstrap/data, domain, splash and Dynamic tests. Backend-shaped Login/OTP integration tests remain pending.

## 16. Related Authority

- Backend API specification: backend repository.
- Dynamic contract: `docs/features/dynamic/00-FEATURE-DISCUSSION.md`.
- Current source/implementation: `docs/features/dynamic/02-IMPLEMENTATION-STATUS.md`.
- Current architecture: `docs/architecture/ARCHITECTURE-CURRENT-STATE.md`.
