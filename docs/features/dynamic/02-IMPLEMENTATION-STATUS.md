# CB-Partner Dynamic UI — Implementation Status

**Status:** IMPLEMENTATION IN PROGRESS  
**Updated:** 2026-10-04  
**Branch:** `feature/dynamic-ui`

DYNAMIC-01 → DYNAMIC-07 remain frozen. DYNAMIC-08 is frozen as the unified destination contract and its source/test alignment is JVM verified.

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
data.startup.destination
        ↓
DynamicDestination
```

### Dynamic API response

```text
data.destination
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
- Bootstrap startup destination mapping uses the canonical `DynamicDestination`.
- Dynamic mapper, Bootstrap mapper, Dynamic action and Dynamic store tests were aligned with the frozen destination contract.
- Affected DYNAMIC-08 compile/test failures were resolved and the owner-provided JVM verification passed.

## 4. Verification baseline — 2026-10-04

The verification below is the pre-Bootstrap-Option-B baseline. Bootstrap reconciliation changes made after this baseline require a fresh verification run.

Compile set:

```text
:domain:compileKotlinJvm
:data:compileKotlinJvm
:feature:splash:compileKotlinJvm
:feature:dynamic:compileKotlinJvm
:navigation:compileKotlinJvm
:app:compileKotlinJvm
```

Result: **BUILD SUCCESSFUL — 43 actionable tasks: 43 executed**.

JVM verification set:

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

## 6. Bootstrap Option B implementation status

Phase A implementation has now been applied to the branch according to the frozen implementation plan and final reconciliation:

- [x] `BootstrapRepository` domain contract exists.
- [x] `BootstrapRepositoryImpl` owns Bootstrap data orchestration.
- [x] `BootstrapRemoteDataSource` owns the Bootstrap endpoint, method and required headers.
- [x] Bootstrap transport DTOs moved under `data/bootstrap/model`.
- [x] `BootstrapMapper` moved under `data/bootstrap/mapper` and remains explicit DTO → domain mapping.
- [x] Bootstrap startup destination maps directly to canonical `DynamicDestination`.
- [x] `BootstrapOutput` no longer depends on legacy `BootstrapConfig` or `StartupConfig` domain contracts.
- [x] Legacy `BootstrapConfig` domain contract removed.
- [x] Legacy `StartupConfig` domain contract removed.
- [x] Transitional `DefaultApplicationBootstrap` removed.
- [x] `ApplicationBootstrap` DI delegates to the single `BootstrapRepository` path.
- [x] Splash/application composition consumes `BootstrapOutput.destination`.
- [x] Bootstrap mapper/repository/Splash tests were reconciled with the new output contract.
- [ ] Fresh compile/test verification after these changes.
- [ ] Runtime proof that exactly one production Bootstrap HTTP path is exercised.

## 7. Remaining implementation / verification

| Area | Status |
|---|---|
| DYNAMIC-08 discussion | FROZEN |
| Destination domain model | IMPLEMENTED / VERIFIED |
| Dynamic response destination mapping | IMPLEMENTED / VERIFIED |
| Dynamic action destination mapping | IMPLEMENTED / VERIFIED |
| Bootstrap startup destination mapping | IMPLEMENTED |
| DYNAMIC-08 affected tests | PASSING on baseline |
| Pre-Phase-A JVM compile set | PASSING |
| Pre-Phase-A selected JVM test set | PASSING |
| Bootstrap Option B source reconciliation | IMPLEMENTED |
| Bootstrap legacy-domain cleanup | IMPLEMENTED |
| Bootstrap DI/test reconciliation | IMPLEMENTED / VERIFICATION PENDING |
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

## 8. Rules for continuation

- Do not reopen DYNAMIC-01 → DYNAMIC-08.
- Do not add `DynamicNextScreen` back.
- Do not add `nextScreen` aliases to the canonical domain contract.
- Do not parse template IDs in the frontend.
- Do not create Login/OTP/Dashboard-specific screen classes.
- Do not put direct navigation destinations inside action payloads.
- Do not add method/authentication back into `DynamicDestination`.
- Keep request metadata outside destination identity.
- Do not restore `DefaultApplicationBootstrap` as a second production HTTP path.
- Do not restore `StartupConfig`, `NextScreenConfig`, or standalone `BootstrapConfig` as domain contracts.
- Start the next authorized implementation item rather than reopening completed discussion.

## 9. Next action

**Run the fresh Phase A compile/test verification. If green, perform the runtime single-path/canonical-destination hand-off verification and then continue with the next ordered item: Dynamic feature use-case package ownership cleanup.**
