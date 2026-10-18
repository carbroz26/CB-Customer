# 109 — T1 Terminal API Identity & Correlation — Exact Plan Frozen

**Status: T1 PLAN FROZEN, IMPLEMENTATION NOT AUTHORIZED.** Owner requested T1 field-contract and file-plan freeze. Branch: `architecture/dynamic-runtime-v2-discussion-freeze` only. Backend READ ONLY. Supersedes the cancelled in-app Inspector plan (Doc108) for terminal presentation; Doc23's safe Kermit rules remain binding. T2 JSON/mapping and T3 rendering/interaction are separate future approvals.

## Source reconciled

- `RequestHandler.executeRequest` is the shared Ktor transport and already creates unique `requestId`, logs `http.request`/`http.response` with `sessionId/actionId/parentActionId`, safe method, HTTP status/category/duration and cancellation; no raw URL, path, headers, body or error message.
- `DynamicRemoteDataSource.fetch` is a concrete backend-driven relative endpoint owner, validates endpoint segments before calling RequestHandler. Current guard rejects empty/dot segments, ':', backslash, '?' '#' '%'. **Current validation does not constitute a privacy classification**; path segments may still be personal data or credentials.
- `BootstrapRemoteDataSource.fetch` has trusted static route `/api/v1/partner/config/bootstrap`; can mark it with an explicit safe diagnostic identity.
- `TraceContext` and `safeDiagnosticId` remain in core; their source contract and generated IDs are not changed.
- Current `RequestHandlerLoggingTest` deliberately asserts arbitrary path/header/body text is absent, and request+response requestId pairing; `DynamicRemoteDataSourceTest` asserts malformed endpoint rejection and correct use of accepted relative path.
- Current viewers must not infer source fields that were never emitted.

## Frozen event field contract

Preserve existing Kermit `CBPartnerNetwork` event tags `event=http.request` and `event=http.response`, severity and status/outcome semantics. Add **one optional safe `operation` field**, value only from trusted code-owned enum-like literals:
- `bootstrap` at BootstrapRemoteDataSource.
- `dynamic_screen` only when provenance explicitly provided by the screen-load call chain (not guessed from GET).
- `dynamic_action` only when provenance explicitly provided by the action call chain (not guessed from POST).
- `dynamic_request` as fallback for existing backend-driven DynamicRemoteDataSource requests when finer provenance is not evidenced.
- `other` or absent for unclassified transports. Fixed stable vocabulary, never interpolate an arbitrary caller-supplied operation string.
Do not create another required field or break calls without a supplied operation. The request and response **must carry identical** operation on the same requestId, including HTTP error and cancellation. Existing fields remain: `requestId`, `sessionId`, `actionId`, `parentActionId`, `method` on request; response additionally `outcome`, `status`, `category`, `durationMs`. No duplicated event emission.

**API identity policy (important):**
- For trusted, code-owned bootstrap route, show `endpoint=/api/v1/partner/config/bootstrap` only in Development Debug if explicitly validated against this exact literal and current Doc23 amendment authorizes that literal. Because Doc23 otherwise bans arbitrary paths, avoid adding a generic `endpoint` field at T1.
- For backend-supplied endpoints, **do not print the original path, full URL, query, host or variable segments in Kermit**. They can carry personal data despite validation. Show the trusted operation + matching requestId. If route identity is required for future troubleshooting, freeze a **separate segment-wise allowlist or trusted route-template contract** after backend/source verification; no arbitrary path dump or unverified irreversible hash as a substitute for security.
- This means T1 improves actual API *operation* identification and pairing but does **not** pretend to expose the complete dynamic API path. That outstanding user debugging requirement is explicitly retained for a separately approved, privacy-reviewed endpoint-template contract.
- No request/response headers, bodies or JSON at T1. Do not render absent header/body fields as blank or invented. T2 will consider structurally faithful sanitized SDUI projection; it is **not** raw backend JSON.

## Exact file scope

1. **EDIT** `network/src/commonMain/kotlin/com/carbroz/cbpartner/network/http/RequestHandler.kt`: add optional code-owned diagnostic operation argument at existing `executeRequest` boundary; normalize by strict fixed-case allowlist (unknown becomes `other` or omitted), append it to both existing events, with no changes to actual request URL, headers, body, retry, status parsing, error mapping or cancellation. The public inline + `@PublishedApi` compile boundary must be checked.
2. **EDIT** `data/src/commonMain/kotlin/com/carbroz/cbpartner/data/bootstrap/remote/BootstrapRemoteDataSource.kt`: pass fixed `bootstrap` operation; no change to bootstrap path.
3. **EDIT** `data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/remote/DynamicRemoteDataSource.kt`: pass fixed `dynamic_request` operation; preserve existing relative-path validation. Do not derive an operation from untrusted endpoint text.
4. **EDIT** `network/src/commonTest/kotlin/com/carbroz/cbpartner/network/http/RequestHandlerLoggingTest.kt`: assert fixed operation emitted twice, paired with single requestId, retained on success/error/cancellation and unknown operation fail-closed; no leak of raw URL/path, header, body, OTP/token or server message.
5. **EDIT** `data/src/commonTest/kotlin/com/carbroz/cbpartner/data/dynamic/remote/DynamicRemoteDataSourceTest.kt`: verify fixed dynamic operation reaches transport, unsafe endpoint still rejected before HTTP, accepted path still sent unchanged.
6. **EDIT** existing bootstrap-focused test (use actual test location) to assert bootstrap operation if a suitable seam already exists, otherwise document coverage at a network MockEngine boundary. Do not introduce a logging-only facade.
7. **VIEWER** PowerShell script remains unchanged at T1 unless owner separately approves viewer update; no fabricated route or timing stages.

**Do not edit:** `DynamicRepositoryImpl`, `DynamicResponseMapper`, six category mappers, `DynamicRenderer`, `DynamicRenderContext`, `DynamicStore`, `ApplicationContent`, `App.kt`, `LoggingConfiguration.kt`, platform entry points, Koin modules, backend, and removed inspector code.

## Test and review gate

Run `:network:jvmTest :data:jvmTest :feature:dynamic:jvmTest :app:jvmTest`, then `:androidApp:assembleDevelopmentDebug :webApp:wasmJsBrowserDevelopmentWebpack`; `git diff --check`, exact diff and unused-code review. Android A35 development Logcat should show POST and subsequent GET with distinct actionIds, matching per-request operation, no sensitive payloads. Missing/cancelled/error outcomes remain real; no fake lifecycle completion. Staging/Production behavior must preserve existing safe operational logs and severity. iOS macOS runtime unverified here. Report local tests as pending until owner actually runs them.

## Security/ownership boundary

Doc23 still forbids arbitrary URL/path and raw request/response bodies in Kermit in all environments. T1 does not implement full SDUI JSON, all backend dynamic routes, or origin tracing. A decision to display *actual* arbitrary paths or sensitive secrets requires a separately reviewed explicit supersession of Doc23, not inferred from “local Development Debug”. No local inspector, eligibility manager, second logging framework, second Ktor client or new branch. No source implementation without separate owner authorization.
