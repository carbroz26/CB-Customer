# 02 — Bootstrap, Network, Shared Transport, Response Contracts

**State:** Discussion frozen; HTTP contract details subject to backend reconciliation.

## Startup flow
Platform entry -> shared app composition -> Splash Store -> BootstrapRepository -> common HttpTransport -> Bootstrap DTO/mapper -> startup configuration and destination -> app-created Dynamic session/navigation -> one Dynamic container.

Splash is a dedicated application startup state machine. It owns bootstrap loading/retry and maintenance/required-update/error rendering. It does not parse screen documents or mutate Dynamic stack. `:app` transitions to Dynamic mode only when startup is permitted.

```kotlin
interface BootstrapRepository {
    suspend fun fetch(): BootstrapResult
}
data class BootstrapResult(
    val configuration: BootstrapConfiguration,
    val authenticated: Boolean,
    val initialDestination: ScreenDestination,
)
```
These sketches show responsibilities, not final compile-ready signatures/error type.

## Endpoint-specific interpretation
Three responses share an envelope but not a Domain response:
- Bootstrap `data.config`, `data.startup.destination`.
- Screen document `data.screenId`, `data.schemaVersion`, `data.targetApp`, `data.template`, optional `data.theme`.
- Request-action response arbitrary `data` including optional nested `data.destination`.
The caller knows which response is expected. **Do not infer a screen from presence of field names** or flatten all meanings into `DynamicResponse(data: DynamicScreen?, dataPayload, destination)`.

One data-layer envelope validator checks HTTP/application status, `code`, required data shape and safe error classification. A shared DestinationMapper decodes the exact same destination contract for Bootstrap and Dynamic action response.

## ScreenDestination
```kotlin
data class ScreenDestination(
    val screenId: String,
    val templateId: String,
    val templateType: String,
    val endpoint: String,
    val method: HttpVerb,
    val authentication: AuthenticationMode,
)
```
The owner-supplied canonical Login/OTP destination JSON has only `screenId`, `templateId`, `templateType`, `endpoint`. `method` and `authentication` are proposed additive fields, not present in those fixtures. GET/NONE defaults require explicit backend agreement. Expected destination identity is verified against fetched ScreenDocument.

## Shared network infrastructure
One Ktor-based `HttpTransport` for bootstrap, screen loading and action requests. One configured API base URL per environment/platform injection (dev, staging, prod), never a per-Element base URL constant. Validate allowed host/relative endpoint resolution before attaching credentials.

```kotlin
interface HttpTransport {
    suspend fun execute(request: TransportRequest): TransportResponse
}
data class ApiConfiguration(val baseUrl: String)
```

Network owns common request metadata, authorization behavior, content type, request timeout/transport failures, without knowing Screen or OTP.

## Header policy
- Typical headers subject to backend confirmation: `Accept`, `Content-Type` on JSON-body requests, `X-CarBroz-Platform`, `X-CarBroz-App-Version`, `X-CarBroz-Build-Number`, approved Device ID, optional Locale/Request ID.
- Device Name and IDs are only provided if needed, privacy-reviewed, and supported by platform permissions; do not log them.
- `Authorization` is computed only for requests explicitly requiring bearer auth; token comes from a secure provider, never UI JSON or a hardcoded value.
- Bootstrap and Dynamic reuse this infrastructure; no duplicated headers per template or action.
- No unconditional body mutation: context values are exposed through explicit `$context` expressions. Do not silently attach deviceId or metadata to arbitrary POST bodies.

## Error and cancellation policy
- Non-success API `code` is a failure even with HTTP 200.
- Invalid/missing destination, template mismatch, malformed response, missing mandatory value are explicit contract failures; no silent rendering.
- CancellationException propagates across suspend functions; never converted to ordinary Result/error UI.
- No raw body/debug metadata leaks. Timeouts, authorized hosts and iOS/Web engine/CORS behavior must be verified in implementation planning and platform tests.

## Exact ownership
- `HttpTransport`: transport mechanics and approved request setup only.
- `BootstrapMapper`: bootstrap DTO -> Domain only.
- `ScreenDocumentMapper`: screen DTO -> concrete node Domain only.
- `ActionResponseMapper`: action response data and optional destination only.
- `DestinationMapper`: shared destination conversion.
- `RemoteDynamicRepository.loadScreen`: call endpoint, decode expected ScreenDocument, validate identity.
- `RemoteDynamicRepository.executeRequest`: send resolved request, decode expected action response.
No fetching by renderers, navigation layer or action mapper.
