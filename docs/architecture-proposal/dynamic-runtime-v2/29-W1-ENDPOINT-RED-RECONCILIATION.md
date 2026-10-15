# 29 — W1 Endpoint Security: RED Failure Reconciliation

**State:** MINIMAL SOURCE CORRECTION AND REGRESSION TEST COMMITTED; POST-CHANGE GRADLE VERIFICATION NOT YET RUN. Remains W1 OPEN. Owner posted real failing `:network:jvmTest` on head `1e9746e9`: 15 network tests, 1 failure; targeted run 12 RequestHandler tests, 1 failure in `endpointSegmentsMustNotCarryUrlAuthorityOrTraversal`, MockEngine requestsSent was 1 rather than 0. Other tests in those runs passed. The failure output does **not identify which invalid endpoint** triggered first; the assertion tests a policy rather than demonstrating URL redirection.

## Root cause from source + frozen contract
- `DynamicRemoteDataSource` receives the original endpoint String and split('/') discards blanks. A string like `//evil.example/path` loses its absolute/authority-style prefix after splitting.
- `RequestHandler` receives only List<Any> segments, so it cannot reconstruct the original raw syntax. The earlier `RequestHandlerTest` repeated Data's split and incorrectly made the Network boundary responsible for reconstructing destroyed input evidence.
- Frozen 17/20 require trusted-origin relative endpoints, no duplicate managers or validators. The minimal place to reject the original invalid endpoint is **before splitting it in DynamicRemoteDataSource**.

## Exactly what changed, why and effect
1. `data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/remote/DynamicRemoteDataSource.kt`:
   - Replaced expression-body fetch with a block allowing validation of the original endpoint while it is still intact. It removes at most one normal leading slash, splits the rest, rejects blank segments, dot / dot-dot traversal, colon (scheme/authority), backslash, query/fragment markers, and percentage-encoded text (strict initial relative-path contract).
   - Throws `IllegalArgumentException` before any HTTP call. Normal `/api/v1/partner/screen` and `api/v1/partner/screen` still yield the same segment list.
   - Added concise KDoc for caller boundary, ownership and intentionally unsupported endpoint query/fragment. No new class or dependency.
2. `network/src/commonTest/kotlin/com/carbroz/cbpartner/network/http/RequestHandlerTest.kt`: removed **only** the misplaced failing raw-endpoint assertion; retained valid-origin test, HTTP error status tests, bearer/header/cancellation tests. No production Network source changed.
3. `data/src/commonTest/kotlin/com/carbroz/cbpartner/data/dynamic/remote/DynamicRemoteDataSourceTest.kt`: new actual-caller tests that feed original unsafe raw strings and assert rejection before outgoing HTTP, plus valid path/body result test. Reuses existing Ktor MockEngine dependency from Data tests.
4. This file is a documentation-only traceability record. No endpoint redirect adapter, URL parser manager, new transport or authentication store introduced.

## Known limits and mandatory follow-ups
- Strict rejection of percent escapes is intentional conservative policy but **not confirmed by all potential future backend endpoint fixtures**. Before final freeze, verify real backend endpoints contain no legitimate pre-encoded path values. If they do, adjust one parser contract with focused tests; do not silently work around it.
- Valid raw API endpoint shapes with query/fragment remain out of scope; existing RequestHandler supports typed `queryParams` independently.
- Bootstrap caller uses fixed path and is untouched. Other RequestHandler callers that bypass DynamicRemoteDataSource must be checked before claiming whole-transport endpoint security. A separate network-wide segment guard would need proof and must not duplicate validation unnecessarily.
- No safe logging added for endpoint failures: logging untrusted endpoints risks exposing tokens/PII, and a new logger pipeline is not justified by this focused input guard. KDoc was added with change. Future safe event logging belongs to agreed structured diagnostics integration.
- Hygiene: no new imports in production Data source beyond existing; Network test existing imports remain needed; new Data test imports used. No dependencies/Koin registrations removed. Full static Gradle dependency audit and diff check still pending.
- No local Gradle compile or runtime execution is available through remote GitHub content tools. **Owner must run post-change tests.** KMP compile and proper exception types remain subject to execution.

## Verification command
```powershell
cd D:\CarBroz\CB-Partner
git status --short
git fetch origin --prune
git pull --ff-only origin architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :network:jvmTest :data:jvmTest :feature:dynamic:jvmTest
git diff --check
```
Do not mark W1 complete until test results are confirmed.
