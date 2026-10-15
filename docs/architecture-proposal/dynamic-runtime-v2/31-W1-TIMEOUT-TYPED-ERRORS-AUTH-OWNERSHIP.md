# 31 — W1 Ktor Timeout Mapping and Authentication Ownership

**Status:** Kotlin source, focused MockEngine test, and KDoc committed. **Post-change JVM tests NOT executed remotely. W1 is OPEN.**

## Frozen plan alignment
- 17 W1, 20 §1/§5/§6: one shared Ktor transport; typed HTTP/transport failures; preserve cancellation; no duplicate auth stack.
- 23: KDoc and privacy-safe logging in implementation wave; no raw URL/body/token/exception log.
- 26–30: source-to-plan audit and prior JVM results. This implementation follows source/caller tracing.

## Exactly changed
1. `network/src/commonMain/kotlin/com/carbroz/cbpartner/network/http/RequestHandler.kt`: add imports `io.ktor.client.plugins.HttpRequestTimeoutException`, `io.ktor.client.network.sockets.ConnectTimeoutException`, `SocketTimeoutException`; catch these **after CancellationException** and before catch-all Throwable, map to existing `NetworkException.Timeout`. Use stable messages that never include raw exception text or URL. Existing ResponseException/SerializationException handling and request behavior remain unchanged. Class KDoc updated with typed timeouts/privacy.
2. `network/src/commonTest/kotlin/com/carbroz/cbpartner/network/http/RequestHandlerTest.kt`: add one table-driven MockEngine test for request/connect/socket timeout and safe exception message with no private endpoint leakage. No new test dependency or helper class.
3. This audit record; no extra infrastructure.

## Why no Connection mapping yet
The presence of `NetworkException.Connection` is not itself proof of a precise cross-platform Ktor connection exception for DNS/refused/reset on Android/Web/iOS. Catching generic `IOException` would mask other errors. Investigate actual engine exceptions/shape before classifying; keep the existing `Unknown` behavior until tested.

## Authentication wiring source evidence
- `feature/dynamic/action/RequestActionHandler.handle` resolves auth mode from dynamic payload, defaults NONE.
- `DynamicRepositoryImpl.fetch` forwards method/endpoint/auth; `DynamicRemoteDataSource.fetch` forwards to RequestHandler.
- `networkModule` makes singleton RequestHandler but does not provide bearerTokenProvider; default callback gives null.
- `App.kt` Koin composition includes RuntimeMetadataProvider/ApiBaseUrl, not credential service.
- Repository tree contains no Kotlin files with token/auth/session/credential/secure in their paths; this name-based scan **does not prove there is no credential logic elsewhere**. Do not invent a global token manager or credentials in RuntimeMetadata. Real credentials must be sourced from a frozen secure session contract with platform lifecycle, separately reconciled with W4/W7.

## Hygiene and safe diagnostics
- All added imports used; no variables/classes/managers introduced.
- No Gradle, Koin or app integration modifications; no deletion of potentially referenced legacy types.
- KDoc updated. No additional Kermit log was introduced: existing logging policy forbids raw URLs and cause messages, and logger injection/integration is not yet verified; typed status/category in NetworkResult is sufficient for this minimal unit. Must revisit safe operational logs with approved composition policy.
- No source code execution available remotely; last owner-reported successful JVM matrix preceded this change. Rerun `:network:jvmTest :data:jvmTest :feature:dynamic:jvmTest`, inspect actual failures before any correction.

## Next gate
If tests pass, verify connection-related exceptions on supported engines via platform-appropriate tests before implementing Connection mapping. Auth remains incomplete until secure owner/lifetime and token injection are explicitly defined; do not claim W1 100% complete.
