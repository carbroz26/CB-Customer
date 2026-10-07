# CB-Partner — Frontend API Integration Contract

**Status:** AUTHORITATIVE FRONTEND INTEGRATION RECORD
**Last Updated:** 2026-10-07
**Scope:** Frontend repository only

## 1. Authority

The backend repository owns the authoritative API specification.

This document records only the frontend integration boundary and the backend contract currently verified for the frontend vertical slice. Do not invent missing backend fields or semantics.

## 2. Network boundary

```text
UI
 ↓
Store
 ↓
Use Case / feature orchestration
 ↓
Repository
 ↓
Remote Data Source
 ↓
RequestHandler
 ↓
Ktor HttpClient
```

UI and Store code do not perform raw HTTP.

## 3. Bootstrap

```text
GET /api/v1/partner/config/bootstrap
```

Known headers:

```text
X-CarBroz-Platform
X-CarBroz-App-Version
X-CarBroz-Build-Number
```

The bootstrap response supplies the startup Dynamic destination. The frontend maps it to the canonical four-field `DynamicDestination`:

```text
screenId
templateId
templateType
endpoint
```

## 4. Login screen

```text
GET /api/v1/partner/screen/auth_login
```

The Login screen is Dynamic UI. The frontend does not create a native Login feature architecture.

The mobile input uses a generic binding key such as `mobileNumber`.

## 5. Send OTP

```text
POST /api/v1/partner/auth/send_otp
```

Current generic request sources:

```text
phoneNumber = $binding(mobileNumber)
deviceId    = $context(deviceId)
```

The successful response supplies challenge information and a destination. Required values are explicitly retained into generic RuntimeState rather than exposed through a persistent response namespace.

## 6. OTP screen

```text
GET /api/v1/partner/screen/auth_otp
```

The OTP screen reads retained values through generic state.

Examples:

```text
phone display = $state(phoneNumber)
otp input     = $binding(otp)
```

Resend uses:

```text
phoneNumber = $state(phoneNumber)
deviceId    = $context(deviceId)
```

Resend may use `responseMode = none` because it does not require a destination result.

## 7. Verify OTP

```text
POST /api/v1/partner/auth/verify_otp
```

Request sources:

```text
challengeId = $state(challengeId)
phoneNumber = $state(phoneNumber)
otp         = $binding(otp)
deviceId    = $context(deviceId)
```

A successful response may provide the next Dynamic destination.

## 8. Reference rules

The frontend Dynamic contract supports only:

```text
$binding
$context
$state
$literal
```

`$response` is not a supported frontend reference.

Do not introduce business-specific namespaces such as `authFlow.*` into Dynamic Context.

## 9. Type policy

The frontend preserves JSON semantic types.

```text
JSON string  → DynamicValue.StringValue
JSON number  → DynamicValue.NumberValue
JSON boolean → DynamicValue.BooleanValue
JSON object  → DynamicValue.ObjectValue
JSON array   → DynamicValue.ArrayValue
JSON null    → DynamicValue.NullValue
```

A value is not made numeric merely because it contains digits.

Therefore phone numbers, OTPs, IDs, codes and identifiers remain strings when supplied as JSON strings.

## 10. Error boundary

HTTP status, connection, timeout, serialization and protocol failures remain distinguishable at the network boundary.

Business/API outcomes are not automatically converted into transport exceptions.

## 11. Security

- OTPs, credentials and tokens must not be logged by the frontend.
- Backend responses must not be altered to expose secrets merely to make local testing easier.
- Development-only test mechanisms, if introduced, belong at the backend delivery boundary and must not weaken the production API contract.

## 12. Current verification

The Login → Send OTP → OTP → Verify contract has been reconciled against the current backend source and the attached Config/Login/OTP JSON examples.

The attached JSON is reference input only. If a stale example contains `$response` or a business-specific context namespace, the frontend must not change architecture to support it; the backend contract must be reconciled separately.

## 13. Related authority

- Architecture: `docs/architecture/ARCHITECTURE.md`
- Dynamic: `docs/dynamic/DYNAMIC-ARCHITECTURE.md`
- Dynamic implementation plan/status: `docs/dynamic/`
- Startup: `docs/features/splash-bootstrap/SPLASH-BOOTSTRAP.md`
- Current state: `docs/project-management/04-PROJECT-MASTER-STATE.md`
