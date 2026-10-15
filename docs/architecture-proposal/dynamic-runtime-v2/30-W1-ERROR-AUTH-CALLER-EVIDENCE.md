# 30 — W1 Error Classification and Production Authentication Wiring

**State: SOURCE/PLAN AUDIT + TWO CHARACTERIZATION TESTS COMMITTED; POST-CHANGE JVM EXECUTION PENDING. W1 NOT COMPLETE.**
Owner's prior `:network:jvmTest :data:jvmTest :feature:dynamic:jvmTest` success applied to earlier endpoint fix, not these new tests.

## Frozen plan trace
- Plan 17 W1 and 20 §1, §5, §6 require a single client/transport, typed errors, cancellation and secure auth.
- Addendum 23 requires KDoc and safe diagnostics alongside actual source changes, source-hygiene checks, no duplicate framework.
- No framework or storage may be fabricated merely to force plan-complete status.

## Concrete source findings
1. `network/http/RequestHandler.kt`: `expectSuccess` non-2xx `ResponseException` mapped by status; `SerializationException` mapped to Serialization; `CancellationException` rethrown; all other thrown errors -> Unknown. Existing `NetworkException.Connection` and `Timeout` are defined but are not emitted by these catch clauses.
2. `response.body<DefaultError>()` extracts untrusted server-provided message with status fallback. Examine UI propagation before using this value in diagnostics.
3. `RequestActionHandler.handle` resolves `authentication` in payload with default NONE and constructs `DynamicRequest` (no token). `DynamicRepositoryImpl.fetch` forwards it to `DynamicRemoteDataSource.fetch`, then RequestHandler. `NetworkModule` passes runtime metadata but not bearerTokenProvider. `App.kt` Koin composition takes only runtime metadata and API base URL, not a credential/session provider.
4. This is a **real production BEARER wiring gap** for a backend action requiring it, but the inspected composition lacks an authorized secure credential lifecycle. Do **not** invent global token memory, read Dynamic RuntimeState as credentials, or map missing token to NONE. The frozen plan requires secure session ownership to be reconciled with W4/W7 before wiring.
5. Native/network operation errors and cancellation must not be inferred from an artificial IllegalStateException. The next actual connection/timeout mapping requires target-specific Ktor exception types/callers and tests.

## Exact code diff
- `network/src/commonTest/kotlin/com/carbroz/cbpartner/network/http/RequestHandlerTest.kt`:
  - `unexpectedTransportFailureCurrentlyUsesUnknownError` documents present catch-all behavior for an artificial MockEngine `IllegalStateException`. This is a baseline characterization test, **not proof of real connection timeout categorization**.
  - `malformedHttpErrorBodyFallsBackToStatusDescription` checks 403 retains typed Forbidden/status with malformed response body. Depending on mock client serialization defaults, body may be ignored; the primary assertion is type/status.
- No production file changed; no new import, dependency, Koin registration or logging facility. No KDoc edited because only tests added.
- No obsolete types removed: Connection/Timeout need a caller usage and platform check first. No raw response body logged.

## Next gate
Run `.\gradlew.bat :network:jvmTest :data:jvmTest :feature:dynamic:jvmTest`. If passed, trace real Ktor timeout/connection exceptions and show typed mapping expectation before implementing. Trace authenticated session lifecycle separately to a trusted provider owner; authorization request must never silently send a blank credential.

**Branch only:** `architecture/dynamic-runtime-v2-discussion-freeze`.
