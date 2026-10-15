# 27 — W1 Endpoint, Error and Authentication Source-to-Plan Audit

**State: INVESTIGATION COMPLETED FOR LISTED SOURCE; IMPLEMENTATION CHANGE NOT YET AUTHORIZED BY EVIDENCE; W1 OPEN.** No Kotlin/test source changed and no build run in this audit. Last owner-reported JVM test matrix passed at prior branch head.

## Authority
- Frozen 20 §1/§5/§6; 17 W1; 23 A/B/D/E; traceability 26. One branch only: `architecture/dynamic-runtime-v2-discussion-freeze`.
- User requires line-by-line source + actual caller + frozen-plan comparison and change-level explanation. Verify behavior with MockEngine tests before selecting a fix.

## 1. Endpoint security — verified source trace

```text
BootstrapRemoteDataSource.fetch()
 -> constant /api/v1/partner/config/bootstrap
 -> split('/') and filter blank
 -> RequestHandler.executeRequest(urlPathSegments)
 -> Ktor URLBuilder.appendPathSegments(...)
 -> HttpClientBuilder.defaultRequest(base origin)

DynamicRemoteDataSource.fetch(endpoint, method, auth, body)
 -> HttpMethod.parse(method)
 -> endpoint.split('/').filter(nonblank)
 -> RequestHandler.executeRequest(...)
 -> appendPathSegments(...)
```

- `ApiBaseUrl` validates the **configured origin**; it does not validate backend-supplied endpoint strings.
- `DynamicRemoteDataSource.fetch` accepts arbitrary `endpoint` string without a guard for absolute URL, query, fragment, percent-encoding or dot segments; it splits on slashes. Its actual runtime effect depends on Ktor's encoding and URL merging, so **host redirection is a risk to test, not a demonstrated exploit**.
- `RequestHandler` takes a generic List<Any> of path segments and trusts callers; no validator for path traversal/control tokens at this boundary.
- No independent endpoint parser/manager is justified; choose a single contract-enforcement boundary after tests. The frozen contract's intended semantics are relative API paths on trusted origin, not arbitrary external endpoints.
- **Test cases to add before fix:** valid /api/v1/screen, relative path, URL-looking https://other.example/path, //other.example, ../, %2e%2e, query '?a=...', fragment '#...' and empty path; capture final MockEngine request URL and whether network request is prevented. Decide whether query parameters are only allowed via existing typed queryParams (preferred) based on backend fixtures, not assumption.
- **No automatic URL rewrite or slash-normalization fallback** for malformed endpoints.

## 2. Error classification — verified source trace

- `HttpClientBuilder.expectSuccess=true`, so Ktor non-2xx is a `ResponseException`, mapped by status in `toNetworkException`.
- `toNetworkException` calls `response.body<DefaultError>()` and uses its message; no proven sanitization before returning errors toward UI. This may leak untrusted server details if other layers show `error.message`; inspect Store/Splash callers before changing the contract.
- `NetworkException.Connection` and `Timeout` exist; actual `RequestHandler` catches neither explicitly. Generic catch(Throwable) yields `NetworkException.Unknown` for transport failures, unless a preceding handler applies.
- SerializationFailure catches `kotlinx.serialization.SerializationException`; Ktor content-negotiation failure hierarchy must be verified for actual cases, not guessed.
- CancellationException is correctly rethrown, with JVM coverage.
- `HttpClientBuilder` has no HttpTimeout plugin; the proper values/configuration are not in the frozen plan and cannot be invented. Tests and product/platform constraints must determine timeout policy.
- **Proof tests to write:** MockEngine throws a known connection exception, timeout exception, malformed successful JSON with typed response, HTTP 400/401/422/429/500 and malformed error payload, cancellation. Capture observable NetworkResult subclasses. Test redaction of error strings before any logging/UI propagation.

## 3. Authentication — verified source trace

- Dynamic action DTO has no default auth management on its own; mapper transports `type/payload/destination` to Domain. Further resolution of request `authentication` must be traced through `RequestActionHandler` before any DI edit.
- `DynamicRemoteDataSource` forwards `authentication` to RequestHandler; Bootstrap call defaults NONE.
- `RequestHandler` accepts NONE or BEARER; only BEARER reads bearerTokenProvider. No valid provider is bound in `NetworkModule`. Existing test-only provider confirms handler behavior, not production login/session persistence.
- **Do not** use a hardcoded bearer token, store raw token in `RuntimeMetadata`, read arbitrary Dynamic JSON to obtain token, or silently downgrade BEARER to NONE.
- **Remaining proof:** inspect actual request handler and action response token retention/session lifecycle, current secure credential storage abstractions in repo and platform specifics. If none exists, freeze explicit secure token ownership before creating it.

## 4. Frozen-plan matrix

| Requirement | Evidence | Classification |
|---|---|---|
| One Ktor client/shared transport | NetworkModule + both remote data sources | Existing aligned |
| Origin validation | ApiBaseUrl constructor | Partial: origin-only |
| Endpoint validation | No guard in DynamicRemoteDataSource or RequestHandler | **Gap confirmed; exploit behavior not proven** |
| Typed failure cases | Status exceptions mapped, generic Throwable catches other transport failures | **Partial** |
| Cancellation | rethrow + existing test | Verified |
| BEARER header at handler | test cases passed per owner | Verified handler only |
| Production bearer wiring | default null provider | **Incomplete** |
| KDoc | RequestHandler/ApiBaseUrl currently documented | Partial |
| Safe logging | no raw body logging in inspected network sources | Policy respected here; operational event logging not implemented |
| Unused source/deps | no deletions or new deps in this audit | Full repository hygiene not yet established |

## 5. Concrete next bounded changes

1. Add **characterization tests** first in existing RequestHandlerTest and relevant Data remote-source test to capture actual effective URL + exceptions (without modifying production behavior).
2. Based on failing/red proof, add one simple validated endpoint representation/guard at justified boundary, not duplicate validators in Data and Network.
3. Add error-classification tests before implementing typed connection/timeout mapping, then KDoc and safe logs with changed ownership.
4. Resolve secure bearer credentials via session/lifecycle design proof before DI changes. Preserve old unauthenticated flow.

### Handoff
Files changed by this audit: **one documentation file only**. No changes to production classes, imports, Koin registrations, Gradle dependencies or logging. No compiler/test execution by GitHub editing tool. W1 completion remains blocked on the tests and source-level proof above.
