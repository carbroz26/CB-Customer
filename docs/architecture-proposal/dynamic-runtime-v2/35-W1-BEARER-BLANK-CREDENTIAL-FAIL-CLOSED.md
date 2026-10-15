# 35 — W1 Bearer Credential Readiness: Blank Token Fail-Closed

**State:** implementation + test committed; user local JVM validation pending. **Not production auth wiring**. Branch: `architecture/dynamic-runtime-v2-discussion-freeze`.

## Frozen alignment and current migration contract
- 17 W1, 20 §1/§5/§6: one Ktor client, secure auth policy, no shadow infrastructure; 23: KDoc/log/redaction/hygiene; 34 M07–M09: token issuance, storage and protected-screen policy still require explicit backend/app contract.
- Evidence: `network/http/RequestHandler.kt` owned authorization injection through existing `bearerTokenProvider`; `network/NetworkModule.kt` leaves provider unbound in production. Existing tests already show `NONE` attaches no header, valid `BEARER` attaches token, missing provider stops HTTP. An empty/whitespace-only non-null provider result previously passed through and created an invalid Authorization header.

## Exact changed source
1. `network/src/commonMain/kotlin/com/carbroz/cbpartner/network/http/RequestHandler.kt`, `performRequest`:
   - **Before:** `val token = bearerTokenProvider() ?: error("Bearer authentication requested but no credential is available")`
   - **After:** `val token = bearerTokenProvider()?.takeIf { it.isNotBlank() } ?: error("Bearer authentication requested but no credential is available")` (formatted across two lines).
   - Fail closed before sending any request with absent, empty or whitespace-only credential; valid provider value is forwarded unchanged. Does **not** create token, infer session or change public NONE behavior. Existing class KDoc already covers authorization responsibility; no new redundant comment.
2. `network/src/commonTest/kotlin/com/carbroz/cbpartner/network/http/RequestHandlerTest.kt`: `blankBearerCredentialFailsBeforeNetworkRequest` covers empty and whitespace-only provider returns, asserts error + zero MockEngine sends, closes clients.
3. This reconciliation record. No other source changed.

## Hygiene and compliance
- No new imports, classes, variables beyond local test values, dependency, Koin module, logger or HTTP client. No unused registration introduced. Test-owned client closed. No secrets logged or leaked to error messages.
- Does not claim completed W1: actual issued-token envelope, secure native/web credential store, session commit and refresh/logout, and screen-load auth policy remain **unimplemented**. Must not pretend backend migration alone enables BEARER.
- This change can be used safely before backend starts issuing credentials.

## Verification pending
On local Windows:
```powershell
git status --short
git fetch origin --prune
git merge --ff-only origin/architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :network:jvmTest :data:jvmTest :feature:dynamic:jvmTest
git diff --check
```
Windows iOS disabled warning remains expected; do not suppress or label iOS verified.
