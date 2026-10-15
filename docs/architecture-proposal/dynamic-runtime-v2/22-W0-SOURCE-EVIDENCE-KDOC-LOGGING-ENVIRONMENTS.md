# 22 — W0 Verified Source Findings and Scope Amendment: KDoc, Safe Logging, Environments

**State:** SOURCE AUDIT FINDINGS VERIFIED; THREE NEW CONTRACTS PROPOSED FOR FREEZE, NOT YET FROZEN. Owner authorized V2 implementation, explicitly required line-by-line source/caller/test proof, same architecture branch only, KDoc, logging, and development/staging/production builds. Do not change Kotlin/Gradle source while these new architecture-relevant build/security decisions remain unfrozen. Existing plan 20 continues to govern otherwise.

## Verified local baseline reported by owner
- Clean `git status --short` and `git diff --stat` on original `feature/dynamic-ui`; HEAD `f59577337ac78855da3f0211ed1f30`.
- Owner switched to `architecture/dynamic-runtime-v2-discussion-freeze`, tracking remote, `git pull --ff-only` says up-to-date.
- Executed `.\gradlew.bat :network:jvmTest :data:jvmTest :feature:dynamic:jvmTest`: **BUILD SUCCESSFUL**, 43 tasks (9 executed, 34 up-to-date), 1m05s. Windows iOS simulator disabled warning is expected; don't suppress.
- This proves only those tasks at the checkout state; it does not prove Android/Web/iOS runtime or future code builds.
- **Only this architecture branch** for planning and implementation; no new branch.

## W1 line-by-line source evidence (not assumptions)
1. `network/http/RequestHandler.kt`: `executeRequest<B,R>` validates authentication NONE/BEARER before try; error classification catches Ktor ResponseException and serialization, propagates CancellationException, then catches Throwable as Unknown.
2. `performRequest` constructs URL with `appendPathSegments(urlPathSegments.map(Any::toString))`, adds runtime platform/app/build headers, then applies caller `headers`, and conditionally applies bearer Authorization. The code has **no explicit reserved header override validation** and no explicit endpoint host/authority verification at this boundary. Test its actual Ktor header merge/override semantics instead of assuming overwrite.
3. `ResponseException.toNetworkException` decodes DefaultError.message and maps 400,401,403,404,409,422,429,5xx. Decode failure uses status description. Confirm sanitized error message policy before logging/exposing to UI (server error messages may contain sensitive data).
4. `HttpClientBuilder.kt`: private protocol/host/port fluent builder, `expectSuccess=true`, `Content-Type: application/json` in defaultRequest and one installed ContentNegotiation Json with `prettyPrint=true,isLenient=true,ignoreUnknownKeys=true`. No configured timeout plugin in this source. Evaluate necessity and compatibility rather than auto-adding layers.
5. `NetworkModule.kt`: Koin singleton HttpClient and singleton RequestHandler with runtime metadata provider, currently no injected bearer token provider. A request with BEARER will fail due to null provider. This is a **verified wiring gap**; do not invent token storage or silently disable auth.
6. `ApiBaseUrl.kt`: validates HTTP(S), host, origin-only path, no query/fragment; existing test covers HTTP/HTTPS/path rejection. It does not by itself prove all endpoint override cases safe.
7. `BootstrapRemoteDataSource.kt`: hardcoded GET `/api/v1/partner/config/bootstrap` calls shared handler, segments split on slash. Independent transport abstraction needs evidence of value (not automatically delete).
8. `DynamicRemoteDataSource.kt`: parses backend `method`, splits endpoint on slash and delegates request body/auth to shared handler. Does not itself restrict an endpoint containing query/authority encodings; verify Ktor behavior and contract-specific validation.
9. `RequestHandlerTest.kt`: only cancellation propagation, successful request, runtime metadata headers, 503 mapping, sensitive response returned without transport console logging. **Missing tests** for bearer wiring, caller header conflicts, malicious/invalid endpoint, query handling, malformed JSON, other HTTP classifications and timeout.
10. `core/runtime/RuntimeMetadata.kt`: runtime metadata includes deviceId and string version/build defaults; the transport currently does not send deviceId header. Do not log deviceId.
11. `network/build.gradle.kts` uses Ktor 3 dependencies and MockEngine; `core/build.gradle.kts` already includes Kermit. Reuse existing logger when appropriate; no new logging library by default.

## Environment/build source evidence
- `build-logic/.../AndroidApplicationConventionPlugin.kt` sets default debug API URL from `cbpartner.api.baseUrl` or `http://10.0.2.2:3000`; release uses provided URL or placeholder `https://api.invalid`, through generated BuildConfig field.
- `androidApp/build.gradle.kts` has one Android applicationId `com.carbroz.cbpartner.android`, minSdk24/targetSdk36; **no development/staging/production product flavors declared**.
- `build-logic/.../WebApplicationConventionPlugin.kt` creates Wasm browser executable only; `webApp/build.gradle.kts` has no development/staging/production distribution profiles. Web runtime Main uses meta override or origin host :3000.
- iOS shared entry reads Info.plist `CBPARTNER_API_BASE_URL` or localhost:3000; environment selection belongs to Xcode schemes/configurations, not Android product flavors.
- `gradle/libs.versions.toml`: Kermit 2.1.0 already available in core; do not add another logging framework.

## Required scope addenda (proposals, must freeze exact details)
### A. KDoc documentation policy
- Document **public and non-obvious APIs** with one clear responsibility, inputs/invariants, lifecycle/ownership, cancellation/failure behavior and caller where useful. No verbose comments narrating code or comments on every obvious property. Keep Domain and shared APIs independent from implementation.
- KDoc updates occur in the same vertical implementation wave as changed code and have matching tests where contract behavior matters.
- No standalone documentation framework or duplicate source inventory; use file-level contract matrix in 17/20.

### B. Safe, environment-aware logging
- One logging policy using the existing Kermit dependency if its common target integration is verified; no raw request/response bodies, OTP, phone numbers, tokens, device identifiers, unrestricted JSON or PII in *any* environment.
- Log safe operation names, state transition type, outcome/status code, duration and sanitized error classification; avoid raw URL/query/body. Debug/dev and staging may offer extra safe diagnostics; production minimal safe operational/error logs.
- Do **not** reintroduce RequestHandler raw console logging (Decision 002). Decide logger interface/injection/levels from actual call graph. Logging failures must not affect request/business success.
- Tests assert sensitive values are not emitted.

### C. Development/staging/production build configuration
- Distinguish **deployment environment** (development, staging, production) from Android build type (debug/release). Suggested Android flavors `development`, `staging`, `production` combined with existing debug/release only if user accepts complexity and build requirements; no new Gradle modules.
- Each environment supplies typed API base URL externally (BuildConfig for Android, meta/build parameter for Web, Xcode scheme/build setting/Info.plist for iOS); production invalid/missing configuration must fail intentionally, not silently target localhost or `api.invalid`. Avoid hardcoded private URLs or secrets in repo.
- One shared environment config boundary used by app composition, network and logging policy; platform adapters may differ. Build names, applicationId suffixes, display names, signing, production URL provisioning, distribution/security settings need explicit owner/product choice before freezing. Avoid building six flavor combinations if only three are genuinely useful.
- Add environment-specific Gradle/config and deployment tests across Android, Web, iOS with actual command verification, no fake staging URL.

## Changes to release gates
- W0: comprehensive file/caller/test audit + freeze addenda A/B/C and compare existing core/build decision docs; document final configuration matrix.
- W1: first code slice only after addenda freeze. Reuse one Ktor client, strengthen validated transport/header/bearer handling from tests.
- Each following wave: update KDoc at changed boundaries and apply same safe logger contract. Environment integration belongs to platform work (W7), with core policy/wiring earlier only when real consumer demands it.
- W8 acceptance: all three environment configurations can select correct API endpoints and log levels, secure fail-closed production setup, no sensitive logs, no additional frameworks/modules, Android/Web/iOS checks by supported hosts.

## Explicit preflight status
**W0 local Git and JVM baseline: verified by owner's posted results.** **W0 source audit: partially complete (Network + key callers and build conventions inspected)**; more test/caller/Gradle review still required. **W1 Kotlin edits: not started.** **No branch creation or backend edits.** These scope amendments must be reconciled with existing frozen base-build decisions before changing production source.
