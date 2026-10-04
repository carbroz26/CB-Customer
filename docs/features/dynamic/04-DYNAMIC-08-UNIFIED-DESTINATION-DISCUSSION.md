# DYNAMIC-08 — Unified Destination Contract & Backend Response Alignment

## Status

**FROZEN — IMPLEMENTATION IN PROGRESS**

This is a new post-DYNAMIC-07 contract alignment. DYNAMIC-01 → DYNAMIC-07 remain frozen and are not reopened.

The goal is to make navigation destination handling common and simple across Bootstrap, API responses, and direct Dynamic actions, while removing the old `nextScreen` and unnecessary destination metadata.

## 08.1 — One Destination Concept

The frontend has exactly one canonical destination model:

```text
DynamicDestination
├── screenId
├── templateId
├── templateType
└── endpoint
```

These four fields are required because:

- `screenId` identifies the backend screen.
- `templateId` identifies the concrete screen/template instance and is used as the navigation/back-stack identity.
- `templateType` identifies which registered template renderer must be used.
- `endpoint` identifies the API used to load the Dynamic screen.

`method` is not part of the destination identity; Dynamic screen destinations are loaded with GET. Authentication metadata is not part of the destination contract.

## 08.2 — Bootstrap

The startup configuration uses `destination`, not `nextScreen`:

```json
{
  "startup": {
    "authenticated": false,
    "destination": {
      "screenId": "partner_login",
      "templateId": "tpl_P101L",
      "templateType": "form_template",
      "endpoint": "/api/v1/partner/screen/auth_login"
    }
  }
}
```

Bootstrap maps this object directly to the same `DynamicDestination` used by Dynamic navigation.

## 08.3 — Dynamic API Responses

The response envelope remains:

```text
status
code
message
data
traceId
```

When an API response produces a navigation destination, the destination is inside `data`:

```json
{
  "data": {
    "message": "OTP sent successfully",
    "challengeId": "...",
    "destination": {
      "screenId": "partner_otp",
      "templateId": "tpl_P101O",
      "templateType": "form_template",
      "endpoint": "/api/v1/partner/screen/auth_otp"
    }
  }
}
```

There is no root-level `nextScreen` or `destination` field.

For a screen-fetch response, `data` remains the Screen object containing `screenId`, `schemaVersion`, `targetApp`, `template`, and optional `theme`. The screen document itself does not need a `nextScreen` field.

## 08.4 — Actions

Actions use `type` as the discriminator. `payload` is reserved for action-specific request data.

Request:

```json
{
  "type": "request",
  "payload": {
    "method": "POST",
    "endpoint": "/api/v1/partner/auth/send_otp",
    "body": {},
    "validate": true,
    "responseMode": "destination"
  }
}
```

Direct navigation:

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

A direct `navigate` action does not wrap the destination inside `payload`. This avoids a meaningless second container and makes the contract self-describing.

`request` with `responseMode = destination` means: execute the request, read `data.destination` from the response, and navigate to it. There is no separate request-then-navigate action sequence for this common case.

## 08.5 — Removed Complexity

The following are removed from the canonical frontend destination contract:

- `nextScreen` as a destination property/name.
- `DynamicNextScreen` model.
- destination `method`.
- destination `authentication`.
- destination-specific business screen classes.
- `payload.destination` for direct `navigate` actions.

The frontend must not create compatibility aliases unless required by an actually supported legacy backend response.

## 08.6 — Template ID Convention

Template IDs remain unique and human-readable enough to create manually:

```text
tpl_P101L
```

Convention:

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

The sequence is the uniqueness component; the final screen code makes the ID recognizable. Version changes should increment the sequence when the template becomes a new destination identity rather than silently reusing an unrelated ID.

The frontend treats the value as opaque and does not parse the ID.

## 08.7 — Frozen Principles

1. `DynamicDestination` is the only destination domain model.
2. Destination fields are `screenId`, `templateId`, `templateType`, and `endpoint`.
3. `templateId` is the navigation/back-stack identity.
4. `templateType` selects the registered template renderer.
5. Bootstrap uses `startup.destination`.
6. Dynamic API responses use `data.destination` when an API response produces navigation.
7. Screen-fetch responses remain screen documents and do not carry `nextScreen`.
8. Request actions keep request data inside `payload`.
9. Direct navigate actions put `destination` beside `type`, not inside `payload`.
10. `responseMode = destination` resolves the response's `data.destination` and performs navigation.
11. No separate `DynamicNextScreen` model exists.
12. No destination `method` or `authentication` fields exist.
13. The frontend does not parse or infer meaning from template IDs.
14. The backend owns destination selection; the frontend resolves and renders the supplied destination.
15. No Login/OTP/Dashboard-specific navigation architecture is introduced.
