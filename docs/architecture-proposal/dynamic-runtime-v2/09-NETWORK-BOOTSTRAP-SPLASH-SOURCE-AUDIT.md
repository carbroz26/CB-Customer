# 09 — Network, Bootstrap and Splash: Source Audit / Plan Pass 2

**State:** SOURCE AUDIT COMPLETE FOR LISTED FILES; PLAN DRAFT, NOT FROZEN. No Kotlin source edited or tests executed.
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`
**Objective:** Replace these boundaries with target-first V2 design without losing current boot/network behavior.

## Evidence inspected
- Network: `ApiBaseUrl.kt`, `NetworkModule.kt`, `http/HttpClientBuilder.kt`, `http/RequestHandler.kt`, `NetworkResult.kt`, `NetworkException.kt`.
- Data: `bootstrap/BootstrapRepositoryImpl.kt`, `bootstrap/remote/BootstrapRemoteDataSource.kt`, `di/BootstrapModule.kt`, `di/SerializationModule.kt`, `dynamic/remote/DynamicRemoteDataSource.kt`.
- Domain/Core: `domain/bootstrap/ApplicationBootstrap.kt`, `domain/model/bootstrap/BootstrapFailure.kt`, `core/runtime/RuntimeMetadata.kt`.
- Presentation/App: `feature/splash/SplashStore.kt`, `SplashScreen.kt`, `SplashModule.kt`, `app/App.kt`, `app/ApplicationContent.kt`, Android MainActivity and Web Main.kt.
- Tests: `network/.../RequestHandlerTest.kt`, `ApiBaseUrlTest.kt`; `data/.../BootstrapRepositoryImplTest.kt`, `NetworkModuleTest.kt`; `feature/splash/.../SplashStoreTest.kt`.
Remaining bootstrap DTO/mapper tests, engine/platform code and Gradle scripts will be incorporated in the final cross-file manifest; no claims about unread files.

## Confirmed source findings

| ID | Source fact | Architectural assessment | Target resolution |
|---|---|---|---|
| N01 | `ApiBaseUrl` validates HTTP(S) origin without path/query/fragment. | Good existing invariant. | Preserve in `ApiConfiguration` or reuse focused origin type; add URL policy tests. |
| N02 | `HttpClientBuilder` configures default origin, Content-Type JSON on all requests, ContentNegotiation with a separate Json configuration (`prettyPrint`, `isLenient`, `ignoreUnknownKeys`). Data DI also creates a Json with `ignoreUnknownKeys`. | Duplicate serialization configuration; Content-Type on GET not ideal; no explicit Accept. | One shared Json configuration source and consistent transport ownership; body-type-aware Content-Type and JSON Accept, with tests. |
| N03 | `RequestHandler` injects platform/appVersion/buildNumber headers; bearer token via optional lambda, default null. No deviceId/deviceName HTTP header despite metadata supporting deviceId. | Correct central location, incomplete/implicit token provider and unconfirmed extra metadata. | Explicit common-header and credential providers; add only backend-approved headers, no blanket raw metadata. |
| N04 | `RequestHandler.performRequest` accepts arbitrary request headers and path segments; does not implement a centralized audited endpoint-host policy for backend action endpoint strings. | Sensitive auth/domain control and endpoint validation need a defined boundary. | Resolve only validated backend-relative API paths against approved base origin; never send bearer token to user-supplied arbitrary host. |
| N05 | `RequestHandler.executeRequest` returns typed `NetworkResult<R>`; converts ResponseException and serialization errors; rethrows CancellationException then catches all Throwable into Unknown. | Boundary mixes response decoding with transport, and catch-all may hide programmer errors. Cancellation fix is present but needs tests. | One transport execution contract and explicit transport failure classification; typed application-envelope parsing in data. |
| N06 | `RequestHandler.toNetworkException` reads optional `DefaultError` message from failed response. | Valid functionality but error parsing must not leak untrusted text and must not swallow cancellation in suspend error parsing. | Reuse safe HTTP error decoding and propagate cancellation. |
| N07 | `BootstrapRemoteDataSource.fetch` delegates to RequestHandler GET with split segments. `DynamicRemoteDataSource.fetch` does same for method/endpoint/body. | Two thin feature remote wrappers; no independent transports. | Consolidate common HTTP request mechanics; keep feature-specific repository operations, avoid pointless pass-through classes. |
| N08 | `BootstrapRepositoryImpl.fetch` catches SerializationException, CancellationException, then Throwable, while `mapResponse` also uses `runCatching` and classifies errors. | Duplicate failure translation and broad handling. | One decode/map point, narrow typed failure handling, preserve cancellation. |
| N09 | `BootstrapModule` registers `ApplicationBootstrap` as anonymous object with `invoke()` returning `repository.fetch()`. | Pass-through abstraction with no independent behavior. | Remove redundant forwarding if no distinct use-case policy; inject BootstrapRepository into SplashStore. |
| N10 | BootstrapMapper validates response and reads maintenance/update/features/initial destination. SplashStore has only Initial/Loading/Success/Failure. | Data is mapped, but maintenance/update conditions are not represented as dedicated startup states; current App moves to Dynamic on any Success. | Explicit maintenance/required-update startup gate; optional update per approved UX. |
| N11 | SplashStore owns a default independently-created `CoroutineScope(Dispatchers.Default)`, loadJob and cancel-on-close. App creates SplashStore through Koin factory inside remembered composition but does not visibly call `close()` in ApplicationContent. | Lifecycle ownership/cleanup gap; risk of independent scope surviving when switched out. | Explicit startup-scoped Store owner; child scope; cancel safely at app transition/disposal. |
| N12 | `SplashScreen` is a sizable single file with UI layout and state-specific status branches; `LaunchedEffect(Unit)` calls load. | All belong to splash presentation, but separate pure state rendering/helpers only when meaningful; avoid multiplying files for single-purpose Compose functions. | Keep SplashScreen UI-only, signal Start once per lifecycle, test each state. |
| N13 | `App.kt` performs `startKoin` inside a Composable `remember`. Android and Web construct ApiBaseUrl at platform entry. | Base URL platform injection is correct; app DI lifecycle and re-entry need explicit ownership. | Move DI start/stop to stable platform/app bootstrap boundary, compose shared App with ready dependencies. |
| N14 | `RuntimeMetadata` contains platform/version/build/deviceId and `asContext()`; shared request sends only first three. | Useful minimal core contract; context vs HTTP header metadata intentionally distinct. | Preserve metadata provider in small core; one approved context projection; avoid leaking IDs in logs or headers without contract. |
| N15 | `data/di/NetworkModuleTest.kt` only checks a standalone MockEngine GET; does not instantiate actual NetworkModule. | Test naming/coverage mismatch. | Replace with true DI/configuration integration tests verifying constructed client, headers and engine policy. |
| N16 | Existing tests cover success, malformed bootstrap, null data, 503, Splash retry/loading-success, base URL host validation, common metadata header, cancellation and no body transport logging. | Valuable regression baseline but far from full boundary coverage. | Keep intent of tests and expand matrix; do not claim they were run in this pass. |

## Target class signatures / ownership (conceptual Kotlin)
```kotlin
// core/runtime
data class RuntimeInfo(val platform: String, val appVersion: String, val buildNumber: String, val deviceId: String)
fun interface RuntimeInfoProvider { fun current(): RuntimeInfo }

// network/config
class ApiConfiguration(baseUrl: String) { val origin: ApiOrigin /* validated */ }

// network/http
data class TransportRequest(
    val method: HttpVerb,
    val endpoint: String,
    val authentication: AuthenticationMode,
    val body: String? = null,
    val headers: Map<String, String> = emptyMap(),
)
data class TransportResponse(val statusCode: Int, val body: String)
interface HttpTransport { suspend fun execute(request: TransportRequest): TransportResponse }

// domain/bootstrap
interface BootstrapRepository { suspend fun fetch(): BootstrapResult }
data class BootstrapResult(
    val configuration: BootstrapConfiguration,
    val authenticated: Boolean,
    val initialDestination: ScreenDestination,
)

// feature/splash
sealed interface SplashUiState {
    data object Loading : SplashUiState
    data class Maintenance(val title: String?, val message: String?) : SplashUiState
    data class RequiredUpdate(val storeUrl: String?) : SplashUiState
    data class Ready(val bootstrap: BootstrapResult) : SplashUiState
    data class Failure(val message: String) : SplashUiState
}
```
`HttpVerb`, `AuthenticationMode` and exact failure result syntax need a common-module placement decision before implementation. Avoid accidentally importing a Domain type into Network unless the network/domain dependency graph intentionally permits it. Prefer a transport-local verb/auth enum or a neutral permitted contract over circular dependencies.

## Target invocation sequence
1. Android/iOS/Web entry supplies environment-specific ApiConfiguration, RuntimeInfoProvider and credential source.
2. App composition initializes Koin once with explicit teardown/lifetime and shared Json + HttpClient + HttpTransport.
3. SplashStore (startup-owned child scope) invokes BootstrapRepository; repository uses one HttpTransport and decodes Bootstrap envelope via Json/BootstrapMapper.
4. Bootstrap validates HTTP/application status and required config/startup fields. SplashStore derives maintenance/update/ready state according to explicitly documented precedence.
5. If Ready, App constructs one DynamicSession with initial ScreenDestination; SplashStore startup work is disposed/cancelled. Dynamic host starts entry-scoped store; Screen loader fetches via the **same** HttpTransport.
6. Structured, redacted events report lifecycle outcomes without raw bodies, OTPs, tokens or device IDs.

## Exact file actions proposed (subject to caller graph)
| Current file | Planned change | Why |
|---|---|---|
| `network/ApiBaseUrl.kt` | Retain validation, relocate/rename to focused network config as appropriate | One origin validation rule |
| `network/NetworkModule.kt` | Move registrations to `network/di/NetworkModule.kt`, no parallel client | Ownership |
| `network/http/HttpClientBuilder.kt` | Refactor/replace with single client factory + Json injection | Avoid independent Json and default GET Content-Type |
| `network/http/RequestHandler.kt` | Replace with `HttpTransport` + `KtorHttpTransport`, host and credential policy | One explicit transport boundary |
| `network/NetworkResult.kt`, `NetworkException.kt` | Retire or unify as typed transport failure only after all callers migrate | Avoid two error hierarchies |
| `data/bootstrap/remote/BootstrapRemoteDataSource.kt` | Remove pass-through when Repository calls HttpTransport directly | Reduce redundant layer |
| `data/dynamic/remote/DynamicRemoteDataSource.kt` | Remove pass-through when Repository calls HttpTransport directly | Same central request mechanics |
| `data/bootstrap/BootstrapRepositoryImpl.kt` | Replace with `RemoteBootstrapRepository.kt` single decode/map/failure path | SRP |
| `data/bootstrap/mapper/BootstrapMapper.kt` | Keep dedicated mapping, reuse DestinationMapper | Accurate startup Domain |
| `data/di/BootstrapModule.kt` | Consolidate data registrations under owning Data DI | Remove anonymous forwarding use case |
| `domain/bootstrap/ApplicationBootstrap.kt` | Remove only after verifying all real callers/tests | No value beyond forwarding |
| `feature/splash/SplashStore.kt` | Rewrite startup policy and scope ownership | Explicit maintenance/update/ready |
| `feature/splash/SplashScreen.kt` | Keep visual UI; support explicit states; ensure single trigger and teardown | Behavior parity |
| `feature/splash/SplashModule.kt` | Move to `feature/splash/di/SplashModule.kt` | DI ownership |
| `app/App.kt` | Move Koin init from composition to explicit app/platform lifetime | Avoid global double startup |
| `app/ApplicationContent.kt` | Refactor to explicit splash-to-Dynamic session composition | Single startup gate |
| `core/runtime/RuntimeMetadata.kt` | Retain/refine minimal shared metadata contract | Core justified |
| `data/di/SerializationModule.kt` | Reuse single strict Json configuration instance | Eliminate config drift |

## Network test matrix
- Base origin: HTTPS/HTTP permitted per environment, missing host, path/query/fragment rejection, ports; same-origin relative endpoint validation, encoded traversal, scheme-relative URL and absolute external host rejection.
- Headers: common platform/app/build; optional device ID/name only if approved; JSON Accept; Content-Type only when body present; auth NONE excludes Authorization and BEARER requires credential; case/precedence for request-specific headers.
- Transport: methods GET/POST, empty body, JSON body and encoding; HTTP 400/401/403/404/409/422/429/500+, timeout, connection failure, JSON validation in data; error redaction and cancellation propagation.
- DI: actual assembled NetworkModule and shared serializer, same transport object used by Bootstrap and Dynamic; correct platform API base URL.
- Logging: exact safe events; assert no OTP/token/phone/challenge/deviceID/request/response bodies in debug/staging output.

## Bootstrap & Splash tests
- Bootstrap 200+SUCCESS including feature flags, maintenance enabled, required/optional update and combinations; precedence documented and tested.
- Missing/invalid config/startup, missing/invalid destination fields, HTTP vs API application failure, serialization failure, body shape mismatch.
- Splash MVI initial->loading->ready; maintenance, update required, failure, retry; no repeated request on recomposition; duplicate rapid start; cancellation; store disposal.
- API destination includes method and authentication when backend supplies them, and missing metadata defaults only under approved wire contract.
- Compose splash UI validates actual rendering, retry interaction and accessibility on supported test targets.
- End-to-end smoke: app startup -> bootstrap -> initial Dynamic entry -> screen load, using one network transport.

## Open design decisions for detailed plan
- Exact failure API (typed exception vs Result) and whether status/message should cross Domain.
- Environment config for iOS production and Web production; auth provider storage/refresh behavior.
- App/Splash lifecycle and Koin startup/close strategy across three platforms.
- Maintenance vs required-update precedence; optional update UX.
- Backend-approved common header list and device privacy policy.
- Endpoint host/path allowed policy, HTTP cleartext for dev only.
- Whether headers are injected via Ktor defaultRequest/interceptor vs transport adapter, without duplicate injection.

## Verification
**Read-only planning, not implementation; no builds or tests run.** Before PLAN FROZEN finish all other passes, verify Gradle target APIs, the complete class-level dependency/caller graph and backend contracts; then write exact per-file compile order, migration removal order and tests.
