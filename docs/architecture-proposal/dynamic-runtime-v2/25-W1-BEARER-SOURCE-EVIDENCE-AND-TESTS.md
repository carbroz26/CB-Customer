# 25 — W1 Bearer Wiring Source Audit and Tests

**Status:** Source and caller audit completed for bearer decision; regression tests committed but **post-change build not run**; do not declare W1 complete.

## Actual caller chain inspected
- `NetworkModule.kt` creates one Ktor HttpClient and RequestHandler with `runtimeMetadataProvider=get()` but no `bearerTokenProvider`. Its default is `{ null }`.
- `DynamicRemoteDataSource.fetch` forwards backend `authentication` string to `RequestHandler.executeRequest`; `BootstrapRemoteDataSource.fetch` uses default NONE.
- `RequestHandler.executeRequest` allows NONE or BEARER; `performRequest` adds Authorization only for BEARER, reads provider and throws when no token; outer catch(Throwable) maps it to NetworkResult.Error (Unknown). It does not send a request if credential acquisition fails.
- `data/di/NetworkModuleTest.kt` instantiates its own HttpClient(MockEngine) without invoking production `networkModule`. Thus the test name gives no evidence of production Koin bearer wiring.
- `NetworkException` declares Connection and Timeout types, but `RequestHandler` has no specialized mapping of transport failures to them; those types are **potentially unused**, but do not delete without full caller examination.
- The actual non-BEARER login/bootstrap flow still has a shared client, and no owner-approved secure token provider lifecycle is present in inspected NetworkModule. Introducing a fake bearer token store or switching auth off would be an incorrect workaround.

## Exact code change
Only `network/src/commonTest/kotlin/com/carbroz/cbpartner/network/http/RequestHandlerTest.kt`: two new MockEngine characterization tests:
1. With a supplied token provider, NONE sends no Authorization, BEARER sends `Bearer test-token`.
2. Without a token provider, BEARER returns NetworkResult.Error without sending HTTP request.
No production Kotlin, dependency, DI registration or build configuration was modified; no new logger or KDoc added because only test behavior was characterized. Existing no-raw-body logging remains intact.

## Verification and cleanup checklist
- Existing imports already cover HttpClient/MockEngine/HttpMethod/NetworkResult/assertions; no new imports or dependencies added.
- No production class, variable, object, unused registration or dependency removed on unproven inference.
- **Post-change JVM compilation/test status: UNVERIFIED** until the owner runs `./gradlew :network:jvmTest :data:jvmTest :feature:dynamic:jvmTest`.
- Future source decisions require real bearer credential provider contract, endpoint validation and connection/timeout behavior tests. Do not claim missing token provider is a fully-fixed feature.
