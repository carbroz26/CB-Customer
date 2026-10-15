# 33 — W1 Secure Bearer Ownership: Login/OTP → Retention → Session → App Trace

**State: SOURCE ↔ FROZEN PLAN RECONCILIATION; NO KOTLIN CHANGE; AUTH IMPLEMENTATION BLOCKED ON AN EXPLICIT SECURITY CONTRACT.** Branch `architecture/dynamic-runtime-v2-discussion-freeze`.

## Authorities
- 20 (frozen immutable decisions, W1 and W4/W7 boundaries), 17 (manifest as amended by 19/20), 04 (entry bindings/context/retained-state distinction), 05 (single session/entry owner and platform token service as needed), 12 (owner Login/OTP canonical screen JSON), 23 (redaction, KDoc/cleanup), 26–32 (W1 evidence).
- Do not confuse screen DTO request action auth mode with an authenticated session credential, or assume a token response schema not present in canonical screen fixtures.

## Actual caller chain read
1. Login/OTP are generic SDUI template screens, not named LoginService/OTPService in current Kotlin. Canonical doc 12 supports Request actions with authentication, body binding/state/context references and retain, but does **not** supply a post-OTP access-token response fixture or establish token field/refresh/expiry semantics.
2. `RequestActionHandler.handle` resolves `authentication` default `NONE`, request body, and `retain`. `DynamicRepositoryImpl.fetch` serializes request body and maps `DynamicResponseDto` via `DynamicResponseMapper`. Mapped `dataPayload` is passed to `extractRetainedState`.
3. `extractRetainedState` uses backend-declared `retain` mappings under `data` and returns `stateChanges` (generic values). It currently resolves missing paths via `DynamicActionValueResolver.resolvePath` without an explicit token/credential distinction.
4. `DynamicStore.applyActionResult` updates `DynamicRuntimeState.setAll(result.stateChanges)` **before** delivering effects. This follows state-before-effect order but exposes a deliberate general retained-state facility, not secure credential storage.
5. `ApplicationContent.DynamicContent` constructs `remember { DynamicRuntimeState() }` and `remember(initialDestination) { DynamicNavigationState(initialDestination) }` and passes runtimeState to entry-scoped Store factories. No secure credential provider or clearing-at-logout mechanism is present in the inspected composition.
6. `App` registers runtime metadata and API origin, `networkModule(apiBaseUrl)`, bootstrap and dynamic modules. `networkModule` registers `RequestHandler(get(),runtimeMetadataProvider=get())` with **default null bearerTokenProvider**. `RequestHandler.performRequest` requires a non-null token in BEARER mode; otherwise it returns a network error before HTTP.
7. `core.RuntimeMetadata.asContext()` exposes platform/appVersion/buildNumber/deviceId to dynamic `$context`; this metadata is not authentication state. No token should be placed here.

## Plan adherence vs implementation
| Frozen behavior | Observed | Decision |
|---|---|---|
| One generic Login/OTP SDUI flow (12,20) | Yes, generic actions, no login-specific classes | PRESERVE |
| `$binding` vs `$state` vs `$context` distinct (04,20) | Separate bindings and context plus DynamicRuntimeState | PRESERVE/RECONCILE W6 |
| Successfully retained values committed before navigation (04,20) | Store calls setAll before effect delivery | ALIGNED for existing path; session-safe effects still W4/W6 |
| Secure bearer credential origin, lifetime and logout (05,17 W1,20) | No credential provider bound in Koin | NOT IMPLEMENTED |
| Backend OTP token response field/expiry/refresh | Not defined in canonical screen fixture document | CONTRACT EVIDENCE MISSING |
| One client and transport (17/20) | Shared Ktor HttpClient and RequestHandler | PRESERVE |
| Redaction (23) | No new credential logs added | PRESERVE; proof tests needed |

## Ownership conclusion (architecture recommendation, NOT falsely attributed to frozen JSON)
- Credential lifecycle belongs to an **application/session security boundary** composed at App/platform integration, not `DynamicRuntimeState`, `RequestActionHandler`, `RuntimeMetadata`, screen renderer or a global token manager.
- Network's current `bearerTokenProvider: () -> String?` is the existing injection seam. Reuse that seam; do not create a second HttpClient/Auth plugin stack or store raw token in SDUI retained state.
- **Acquisition:** a successful OTP/auth response must have a separately approved typed credential extraction contract. Without the response example, no key such as `accessToken`, `token`, `refreshToken`, `expiresIn` can be asserted as the product's actual API field.
- **Storage:** secure at-rest/persistence policy (ephemeral vs platform keychain/keystore), refresh and expiry, lifecycle reset/logout, and interaction with session-retained values must be decided. A shared in-memory session handle may be sufficient for a nonpersistent initial flow but is not automatically secure persistent storage.
- **Cancellation and commit:** a credential update must be published only on successful, verified auth outcome, before subsequent authenticated requests/effects; failed/cancelled OTP must leave prior security session unchanged. Navigation should not race ahead of credential commit.
- **Propagation:** BEARER resolves only approved credential provider; NONE never attaches Authorization; absence is a fail-closed typed failure, not silent downgrade.
- Need prove whether screen bootstrap/destination requires BEARER as an additive typed destination contract; doc 12 explicitly does not attest method/auth on current destination.

## Mandatory next decisions/evidence before production auth wiring
1. Source of **actual successful OTP verification response** (not the OTP screen DTO): fields containing access credential, refresh, expiry and nested shape if applicable.
2. Whether auth survives process restart, and logout/session-reset behavior; platform persistence requirement.
3. Whether navigation to a protected screen loads with BEARER, and where its auth policy comes from under the approved four-field destination contract.
4. Credential owner in app/session and how secure provider is injected into existing RequestHandler without new manager/duplicate state.
5. Authentication/error path tests: NONE before OTP, OTP success acquisition, refresh if required, BEARER after success, failed/cancelled OTP, logout/reset, no token in DynamicValue/Kermit/context and no leaked authorization to remote assets.

## Scope/cleanup
No Kotlin, test, dependencies, imports, DI, Gradle, logs or KDoc changed. No source was deleted based on uncertain usage. Past owner-posted JVM suite success applies to prior connection change, not auth wiring. This is a contract audit, not a compilation claim.
