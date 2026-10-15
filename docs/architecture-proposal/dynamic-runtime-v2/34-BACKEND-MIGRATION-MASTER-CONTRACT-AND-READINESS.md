# 34 — CB-Partner Backend Migration Master Contract & Readiness Plan

**Status:** CONSOLIDATED MIGRATION REGISTER + IMPLEMENTATION PROPOSALS. **Not a new frozen wire contract and not backend-deployed.**  
**Repository:** `carbroz26/CB-Partner`; **branch:** `architecture/dynamic-runtime-v2-discussion-freeze`.  
**Backend repository:** `carbroz26/CB-Backend` (separate; no changes authorized here).  
**Scope:** frontend/backend cooperation for existing Login → OTP → mandatory Aadhaar verification → service selection → video training → optional practical training booking → dashboard, with online/offline enabled *only after onboarding approval*.

## 0. Authority, status vocabulary, and precedence

Read `AI_START_HERE.md`, project-management docs, `12-CANONICAL-JSON-FIXED-CONTRACT.md`, `17-PASS6-EXACT-IMPLEMENTATION-BLUEPRINT-DRAFT.md`, `19-BACKEND-MIGRATION-INDEPENDENT-CONTRACT.md`, `20-PLAN-FROZEN-V2-IMPLEMENTATION-CONTRACT.md`, `23-W0-ADDENDA-FROZEN-KDOC-LOGGING-ENVIRONMENTS.md`, `26-W1-FROZEN-PLAN-TO-SOURCE-TRACEABILITY.md`, and W1 audits 27–33 before implementing.

- **FROZEN:** explicitly agreed in V2 document 20/19; preserve exactly unless new owner approval.
- **EXISTING:** seen in owner Login/OTP fixtures or current source; not necessarily an endorsed permanent design.
- **PROPOSED:** design recommendation requiring owner/backend/frontend contract freeze before deployment.
- **FUTURE/DEFERRED:** intentionally not part of initial migration.
- **REMOVE CANDIDATE:** evidence needed; do not delete from live API on speculation.

**Migration principle:** produce the final typed frontend implementation once, tolerate absent additive optional fields naturally, and let backend adopt the **same approved shape**. No parallel legacy/new DTO trees, temporary switches, feature flags, icon-name registry, second Ktor client, auth manager proliferation or silent fallback on protected endpoints. The frozen plan governs frontend architecture; this document does not override it.

## 1. Consolidated migration register

| ID | Change | Why / consumers | Status | Backend action | Frontend action |
|---|---|---|---|---|---|
| M01 | Optional `template.header` and `template.footer` | Static contextual header/footer; reused component tree; template renderer/shell | **FROZEN target** | Emit exact optional slots on selected screens | W3 DTO/mapper + W5 TemplateShell; old body still works |
| M02 | URL-driven `icon_content` / `icon_element` (and required image URL) | Backend controls visual assets, avoids frontend glyph registry | **FROZEN target** | Send `properties.url`, optional `fallbackUrl`; retire name-only assets after rollout | W3 parse; W5 single remote asset path and unavailable fallback |
| M03 | 4-sided `spacing` on ElementContent | Consistent leading/trailing icons and dividers, without per-widget padding | **Previous project decision; exact wire verification gate** | Align final property name `spacing` and object shape before new payload | Map existing DynamicSpacing; tests on icon/divider |
| M04 | Gradient stops, degrees, text enabled/disabledColor and segmented input | Preserve actual Login/OTP visual behavior | **EXISTING fixture + FROZEN preserve** | Keep existing names; do not drop values | Map/render exactly; golden visual regression |
| M05 | Seven actions, five hooks, four value namespaces | Behavior extensibility without generic infrastructure | **FROZEN** | Keep supported types and syntax | Typed DTO→domain→handlers |
| M06 | Navigation identity/destination | Load screen using stable four-field destination | **FROZEN** | Keep `screenId/templateId/templateType/endpoint` | Verify loaded identity; one navigation authority |
| M07 | OTP success issues authenticated session/token pair | Protected KYC/training/dashboard | **PROPOSED, requires auth response freeze** | Add typed success credential envelope and session server-side | Secure session-owned commit prior to navigation; existing RequestHandler injection seam |
| M08 | Refresh rotation, logout/revoke and restore | Sessions across app restart without repeated OTP | **PROPOSED** | Endpoint/session record, expiration/rotation/revocation | Platform secure refresh storage and refresh coordinator (minimal, single client) |
| M09 | Protected screen load authentication policy | Post-OTP screen fetch must attach credential | **OPEN contract decision** | Explicit policy source without breaking four-field destination | Do not infer `BEARER` from arbitrary URL or silently downgrade |
| M10 | Partner onboarding/eligibility state | KYC and training access, dashboard statuses, online/jobs gating | **PROPOSED business API contract** | Authoritative backend flags/status + authorization | UI renders status; backend enforces operations |
| M11 | Response cleanup (`authentication:"NONE"`, redundant layout props etc.) | Smaller, less ambiguous payloads | **REVIEW CANDIDATES, NOT approved removal** | Audit real screen/action payloads before dropping | Preserve defaults; schema change only after tests |
| M12 | Current remaining network classifications, safe logs, environment config | Robust KMP transport and secure rollout | **W1/W7 outstanding** | Standardized error codes and safe server logs | Typed Ktor errors + safe Kermit logs; development/staging/production |
| M13 | `popTo + refresh`, animated headers, tab shells, new onboarding docs/PAN | Not required in current app flow | **DEFERRED** | Do not invent fields | Do not implement until separately frozen |

## 2. Frozen screen wire contract: preserve, then add slots

Existing envelope (current source/fixtures): `{status,code,message,data:{screenId,schemaVersion,targetApp,template:{id,type,properties,components},theme?},traceId?}`. The `template.components[]` **is the body**; **never** rename to `template.body.components`. **One template** with **one or more components**. Components may directly contain Elements and/or Sections; Sections may directly contain Elements and/or Groups; Groups contain Elements; Element properties may contain nested leading/trailing Content. Six logical categories: Template, Component, Section, Group, Element, Content.

**M01 — exact frozen additive template portion** (embedded under existing `data.template`; not an entire response):

```json
{
  "id": "tpl_kyc",
  "type": "form_template",
  "properties": {"padding": {"start": 24, "top": 20, "end": 24, "bottom": 20}},
  "components": [
    {"id": "kyc_body", "type": "stack_component", "properties": {}, "elements": []}
  ],
  "header": {
    "properties": {"padding": {"start": 0, "top": 0, "end": 0, "bottom": 0}},
    "components": [
      {"id": "kyc_header", "type": "stack_component", "properties": {}, "elements": []}
    ]
  },
  "footer": {
    "properties": {"padding": {"start": 0, "top": 0, "end": 0, "bottom": 0}},
    "components": [
      {"id": "kyc_footer", "type": "stack_component", "properties": {}, "elements": []}
    ]
  }
}
```

**Rules:** `header/footer` are independent optional siblings; if absent, render only original body. Empty slot `components:[]` is valid. Header→Body→Footer order. Template properties.padding is **body-only** when shell exists; each slot uses its own padding. Safe areas/IME are platform-owned; no speculative tabs or sticky/collapsible behavior. Header/footer use the *same* Component renderer registry, not parallel rendering infrastructure.

## 3. M02: assets / icons / images

**New backend target, icon content example** (shown inside existing element `properties.trailing[]`):

```json
{
  "type": "icon_content",
  "properties": {
    "url": "https://assets.carbroz.example/icons/arrow-forward.svg",
    "fallbackUrl": "https://assets.carbroz.example/icons/arrow-forward.png",
    "size": 20,
    "color": "#FFFFFF"
  }
}
```

The URLs are **illustrative placeholders**, not deployed hosts. Reuse current actual icon content/element `type` spellings from fixture/DTO when implementing; `icon_content` here is only an example and must be verified. ImageElement retains required `properties.url`; `fallbackUrl` is optional where approved. Old icon `properties.name` remains *parseable* during migration but **must not be rendered via hardcoded native glyph lookup**. Missing legacy icon URL maps to explicit unavailable asset: optional generic nonmisleading placeholder or no decorative graphic. New `url` primary → `fallbackUrl` → generic fallback. No fake empty URL; do not send Authorization to arbitrary image origins. Verify allowed asset host/protocol/relative-url rules with backend and platform (Android SVG/PNG/Web support), accessibility semantics and caches before release.

**Backend migration strategy:** roll out URL assets for Login and OTP first; same frontend build should accept both old and new fixture. No schema rewrite required for other element categories.

## 4. M03/M04: spacing, gradients and current visual/validation fields

Content spacing **proposed shape to verify against existing decision/source**:

```json
{"type":"divider_content","properties":{"spacing":{"start":8,"top":0,"end":8,"bottom":0},"thickness":1,"color":"#D7DFE3"}}
```

Do **not** silently substitute `padding`, `leadingContentPadding`, `trailingContentPadding` or invent separate Icon/Divider padding contracts. `DynamicSpacing` four-side semantics must be verified against frozen/current model before sending.

**Existing actual gradient field shape (retain):**
```json
{
  "gradient": {
    "type": "linear",
    "angle": 135,
    "colors": [
      {"color": "#DDF8F6", "stop": 0},
      {"color": "#F7FEFD", "stop": 0.28},
      {"color": "#FFFFFF", "stop": 0.55},
      {"color": "#D9F7F4", "stop": 1}
    ]
  }
}
```
Preserve precise stops and degrees; test gradient direction against a golden image. Note owner button example also uses `type: "linearGradient"`; normalize only after checking per-consumer existing semantics. Text `enabled:false`, `disabledColor`, spans/actions; segmented OTP input properties and backend-driven typography/layout are real fields and must not be deleted.

## 5. M05/M06: request actions, authentication flag and destination

**Current request action example, intentionally unchanged until simplification decision:**
```json
{
  "type": "request",
  "payload": {
    "method": "POST",
    "endpoint": "/api/v1/partner/auth/otp/verify",
    "authentication": "NONE",
    "validate": true,
    "body": {"challengeId": {"$state": "challengeId"}, "otp": {"$binding": "otp"}},
    "responseMode": "destination",
    "retain": {"challengeId": "data.challengeId"}
  }
}
```
The example path and retain are illustrative; a successful OTP response token must **not** go through `retain` into DynamicRuntimeState. Preserve request `authentication` for now. Current `RequestActionHandler` defaults missing auth to `NONE`, `DynamicRequest` defaults `NONE`, and `RequestHandler` recognizes only NONE/BEARER. Backend omission of `authentication:"NONE"` on **known explicitly public** actions could be safe under current behavior but this is a security-sensitive backward-compatibility choice; **do not globally change default to BEARER without reviewing all consumers**. When protected APIs arrive, require an explicit, validated auth policy; missing flag on protected action must never silently downgrade.

**Fixed destination shape:**
```json
{"screenId":"partner_kyc","templateId":"tpl_kyc","templateType":"form_template","endpoint":"/api/v1/partner/kyc"}
```
Do not silently add `method`, `authentication`, `template`, `refresh` to destination. **Open M09**: current `LoadDynamicScreenUseCase` screen loads lack destination authentication metadata; explicit approved policy must be resolved before protected KYC navigation, perhaps a trusted endpoint policy based on typed app-owned session, but do not infer from arbitrary backend `endpoint` path strings. If adding fields to destination, requires deliberate separate frozen additive contract.

Seven supported actions: `request`, `navigate`, `external_uri`, `present`, `dismiss`, `state`, `sequence`. Five hooks: `onClick`, `onLongClick`, `onValueChange`, `onFocus`, `onSubmit`. Namespaces: `$binding`, `$context`, `$state`, `$literal`; no `$response`. No `popTo/refresh` or unapproved aliases in this migration.

## 6. M07/M08: proposed authentication protocol — NOT yet supported by backend

**Product rule:** OTP success establishes identity/session. KYC and Training determine **eligibility**, not whether partner receives a token. Dashboard may be visible while training pending; ONLINE/OFFLINE and job acceptance only after approved onboarding.

**Proposed endpoints (not existing/backend-confirmed):**
- `POST /api/v1/partner/auth/otp/request` public: phone number → challengeId/expiry, per-number/device/IP rate limits.
- `POST /api/v1/partner/auth/otp/verify` public: challengeId+OTP → credential session + partner identity + next destination.
- `POST /api/v1/partner/auth/refresh` refresh credential → rotating credential pair.
- `POST /api/v1/partner/auth/logout` authenticated → revoke session.
- `GET /api/v1/partner/auth/session` authenticated → minimal current session/partner status for restore.
These endpoints are an API **proposal**, not proof the current backend has them or that their paths should replace deployed ones.

**Proposed successful verify response** (maintains existing `status/code/message/data` envelope, preserves four-field destination):
```json
{
  "status": 200,
  "code": "OTP_VERIFIED",
  "message": "Verification successful",
  "data": {
    "session": {
      "accessToken": "<opaque-access-token>",
      "refreshToken": "<opaque-refresh-token>",
      "tokenType": "Bearer",
      "expiresIn": 900
    },
    "partner": {
      "id": "partner_123",
      "onboardingStatus": "KYC_PENDING"
    },
    "destination": {
      "screenId": "partner_kyc",
      "templateId": "tpl_kyc",
      "templateType": "form_template",
      "endpoint": "/api/v1/partner/kyc"
    }
  }
}
```

- `session` is a **typed security envelope proposal**, not SDUI screen properties or arbitrary retained state; it must be consumed and committed by the app/session security boundary **before navigation**. Do not put access/refresh tokens into `$state`, `$context`, `RuntimeMetadata`, UI logs, action error strings, or `retain`. Current `DynamicResponseMapper` generically converts non-screen `data` to `DynamicValue`; implementing this shape safely will require a reviewed, narrowly scoped auth-result extraction and redaction path to avoid accidental sensitive propagation.
- Token format recommendation: cryptographically random opaque access + rotating opaque refresh with server session lookup; compare JWT only if justified by deployment architecture. Proposed 15-minute access lifetime, 30-day refresh inactivity and bounded absolute lifetime are **product configuration proposals**, not standard requirements. Database stores hashes/verifiers, not raw refresh tokens. TLS always.
- On invalid/cancelled OTP, no credential commit; on success, atomically replace session; on refresh, single-flight coordination and replacement only after server success; refresh reuse handling, device revocation, logout, expiry, 401 recovery and safe retries require tests. Never retry unsafe POST on a 401 without verifying idempotency; define explicit policy.
- Native: access token in process memory, refresh credential in appropriately protected Android Keystore-backed private storage and iOS Keychain. Wasm/web: **separate browser session transport decision**, ideally HttpOnly Secure SameSite cookie/BFF-compatible strategy; never blindly expose long-lived refresh token to JS/localStorage. Do not promise identical storage API on web.
- Auth is not authorized just by a client's status enum; backend must enforce per-partner, per-resource permissions.
- Koin/network seam already exists: `RequestHandler(httpClient,bearerTokenProvider = { ... },runtimeMetadataProvider = ...)`. **Currently provider defaults null in production**; BEARER fails closed until real credentials. No new token manager needed to prepare the seam, but a real session credential owner is required when backend arrives.

**Ready now vs cannot fake:** Now: preserve/test BEARER vs NONE header, missing-credential failure, redaction, candidate typed sealed auth-result contract in design/test fixtures, confirm Koin seam, no backend token assumption. **Do not** create a placeholder token, silently attach empty Bearer, store `session` in SDUI state, auto-refresh mock session in production, or assert final migration will work without client implementation. Only after approved token envelope/platform storage/session lifecycle and protected screen policy can end-to-end auth be implemented and verified.

## 7. M10: product-specific eligibility and status (PROPOSED)

Suggested backend-owned dimensions:
- **Auth:** `UNAUTHENTICATED` / `AUTHENTICATED` / `SESSION_EXPIRED`.
- **Onboarding:** `KYC_PENDING` → `TRAINING_PENDING` → `UNDER_REVIEW` → `ONBOARDED`; `SUSPENDED` as restrictive state. Exact states/transitions/terms to freeze with business team.
- **Availability:** `ONLINE` / `OFFLINE` only if onboarding and job eligibility rules pass.

Proposed minimal session/dashboard response portion:
```json
{
  "partner": {
    "id": "partner_123",
    "onboardingStatus": "TRAINING_PENDING",
    "canGoOnline": false,
    "canAcceptJobs": false
  }
}
```

KYC must include Aadhaar verification and service selection, then training video and practical slot booking (booking can be later; dashboard reachable). Backend must recheck eligibility on `goOnline`, job acceptance, restricted operations and suspend/revoke paths. Do not encode durable permissions in stale access-token claims or assume UI hiding constitutes authorization. Sensitive Aadhaar data handling/retention requires independent privacy/compliance approval.

## 8. M11: remove/minimize candidates — separate safe / conditional / no-go

| Field/pattern | Recommendation | Reason / guard |
|---|---|---|
| `payload.authentication:"NONE"` on verified **public** endpoints | **OPTIONAL OMIT CANDIDATE** after security contract approval | Current handler defaults NONE; saving bytes is marginal. Must not use missing auth as an implicit protected-route decision. Consider making explicitly public/required auth policy typed instead of simplifying prematurely. |
| `payload.authentication:"BEARER"` | **KEEP** until new typed endpoint policy approved | Explicit intent for protected requests; removal can silently downgrade to NONE. |
| `payload.method:"POST"` | **KEEP**; consider omit only when invariant and typed per action | Current RequestActionHandler defaults POST while DynamicRequest defaults GET: **inconsistent defaults**; do not rely on omission before reconciliation. |
| `payload.responseMode:"destination"` | **CONDITIONAL OMIT**, only if this default is frozen and action intends navigation | Current handler defaults destination; `none` must stay explicit for non-navigation. |
| `payload.validate:false` | **CONDITIONAL OMIT**, if absent means false across nested actions | Preserve `true` where required; W6 validation is not fully implemented. |
| Empty `actions` event maps, empty optional `leading/trailing`, `header/footer` | **OMIT optional-empty where decoder permits**, after serialization regression | Minimize wire noise; don't remove required empty `components` on valid slots or mandatory shape fields. |
| `template.components`, unique element IDs, `type`, binding `key` | **KEEP** | Structural identity/registry, input and typed mapper contracts. |
| `screenId/schemaVersion/targetApp`, `templateId/templateType`, destination `endpoint` | **KEEP** | Screen identity, version/target validation, navigation. |
| Root `status/code/message` | **KEEP until envelope error contract review** | Mappers/models currently consume; do not remove solely because HTTP status exists. Ensure cross-level status consistency. |
| `traceId` | **KEEP as optional**, privacy-safe correlation | Useful on support/error paths; do not log tokens. |
| Icon `properties.name` | **RETIRE only after URL migration and regression verification** | Name-based lookup forbidden; preserve tolerant decoding during rollout. |
| Repeated frontend-owned absolute icon paths/base URLs | **AVOID** | Backend owns URLs with trusted origin rules, per frozen contract. |
| `padding` vs `spacing` on content | **DO NOT SUBSTITUTE** | Content spacing semantics agreed separately; inspect canonical implementation. |
| Duplicated `authentication` at action/destination/global request levels | **DO NOT introduce** | Define one trusted policy source before protected screen loads. |
| Hardcoded `online` based on OTP or KYC alone | **FORBID** | Business approval/training gating remains authoritative on backend. |

**Not authorized:** dropping schema fields, replacing `components`, changing action type, replacing current endpoints, deducing all requests are authenticated just because a token is present, or removing `authentication` universally.

## 9. Backend implementation handoff / verification gates

**Gate A — Contract decisions:** owner and backend approve header/footer exact schema (already frozen target), icon URL/fallback origin policy, content `spacing` exact shape, auth success and refresh response, whether token survives restart, protected screen load policy, status vocabulary and permission matrix. Explicitly record which were frozen vs newly approved.

**Gate B — Backend:** modify *backend repo independently* on its authorized branch; use existing controllers/services/storage rather than duplicating stacks. Endpoint validation, OTP rate limit/TTL/consumption, session hashes/rotation/revocation, partner eligibility checks, safe logs, DB migrations and rollback plan. Do not claim backend ready before tests.

**Gate C — Frontend prep (now):** complete frozen W1 transport, reject invalid/missing bearer before HTTP, keep public Login/OTP functioning, add mock fixtures/contract tests for old/new slots/icons and candidate token envelope *without shipping raw session into DynamicValue*, design typed app session owner plus existing RequestHandler DI injection; identify protected screen fetch policy before implementing. No fake credential storage/production empty token.

**Gate D — Vertical integration:** Backend issue token → app session securely commits before navigation → protected KYC fetch attaches access credential → refresh rotation → session restore → logout/revoke → training pending dashboard → backend only allows online/jobs when approved. Verify three environments and Android/Web, iOS on macOS.

**Gate E — Regression/security:** (1) old Login+OTP JSON; (2) new optional slots independent padding; (3) old name-only icons and new URL icons/fallback; (4) gradients 135° stops 0/.28/.55/1; (5) all seven actions/five hooks; (6) valid/missing/expired bearer + NONE no Authorization; (7) failed/cancelled OTP no commit; (8) 401 refresh/concurrent requests/token rotation/reuse; (9) logout/app restart/suspension; (10) no token in logs/DynamicValue/`$context`/cache or third-party image request; (11) status/eligibility enforced server-side; (12) platform smoke/build verification. No unrelated architectural redesign.

## 10. Code-level trace and current implementation facts (as audited)

- `network/NetworkModule.kt`: RequestHandler bound without production `bearerTokenProvider`.
- `network/http/RequestHandler.kt`: supports NONE/BEARER, transport-owned auth headers, typed timeout/unresolved DNS, cancellation; missing bearer rejects before HTTP. Existing HTTP auth exception behavior is **not** complete session lifecycle.
- `feature/dynamic/action/RequestActionHandler.kt`: action authentication default NONE, method default POST, responseMode default destination, `retain` selects data into generic state changes.
- `data/dynamic/DynamicRepositoryImpl.kt` → `DynamicRemoteDataSource.kt` → network `RequestHandler`: request forwarding; dynamic response has generic data payload.
- `feature/dynamic/store/DynamicStore.kt`: applies state changes before navigation/effects but general state is **not** credential owner.
- `feature/dynamic/runtime/DynamicRuntimeState.kt`: retained DynamicValue map, not secure secrets.
- `app/App.kt` / `ApplicationContent.kt`: Koin + app composition, navigation/runtime state; secure provider and logout/reset not wired.
- `domain/model/dynamic/DynamicRequest.kt`: defaults method GET vs request handler POST; audit/normalize deliberately, do not change as a cleanup without callers.
- `data/dynamic/model/action/DynamicActionDto.kt`: seven conceptual action families plus five supported hooks share current polymorphic action payload.
- No code/test/dependency changed by this document. Latest owner-reported JVM test success applies to commit `899243b4` prior to document-only audits.

## 11. Rollout and open decision checklist

- [ ] Backend and owner explicitly approve **auth response fields** (success/error/refresh/expiry), endpoint paths and web strategy.
- [ ] Decide credential expiry, refresh reuse policy, persistence and logout/reset semantics.
- [ ] Freeze protected screen-load auth source while **preserving four-field destination** or separately approve additive schema.
- [ ] Freeze four-sided Content `spacing` shape against current backend/frontend contract.
- [ ] Verify exact icon content type spellings and asset allowlist/mime behavior.
- [ ] Approve onboarding/training/availability business-state model and endpoint authorization gates.
- [ ] Review `authentication:NONE`, redundant defaults and root envelope with actual fixture-by-fixture tests; defer removals until safe.
- [ ] Implement one backend+frontend vertical slice, test, stage, then progressively enable migrated payloads.
- [ ] Reconcile doc 20 and implementation docs only on explicitly approved change; no silent frozen-contract override.

**Owners should treat this file as a migration handoff and proposal register, not a claim that backend emits header/footer/icons/tokens or that frontend can authenticate without subsequent production code.**
