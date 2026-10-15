# 28 — W1 MockEngine Test-First Evidence Gate

**State: TESTS COMMITTED, EXECUTION NOT VERIFIED, PRODUCTION CORRECTIONS NOT AUTHORIZED BY TEST EVIDENCE YET.**
**Branch:** architecture/dynamic-runtime-v2-discussion-freeze. Frozen authority: 17 W1 as amended by 20; 23 KDoc/logging/hygiene addenda; 26–27 source-to-plan audits.

## Why this test change
Read full RequestHandler and existing tests, DefaultError, NetworkException/NetworkResult and the caller chain from DynamicRemoteDataSource/BootstrapRemoteDataSource. Ktor receives a split list of endpoint strings. We have a demonstrated **lack of explicit validation**, but not a proven escaping/redirection exploit. The next correct action is to observe the real Ktor MockEngine request, then repair only confirmed unwanted cases.

## Exact file change
`network/src/commonTest/kotlin/com/carbroz/cbpartner/network/http/RequestHandlerTest.kt`
- Added `canonicalRelativeEndpointKeepsConfiguredOriginAndPath`: Ktor defaultRequest origin and actual captured MockEngine host/path must agree for ordinary split segments.
- Added `endpointSegmentsMustNotCarryUrlAuthorityOrTraversal`: supplies six authority/traversal/query/fragment cases through the **actual DynamicRemoteDataSource splitting rule**; requires no outgoing HTTP for unsafe input. **This test expresses the frozen security requirement, and is expected to expose a current validation gap (RED possible/likely).** A failure proves contract nonconformance, not by itself remote-host redirection; inspect captured request and stack before changing production.
- Added `httpErrorCodesAreClassifiedByStatus`: cases 401/422/429/500, asserts existing typed classes and status codes. This is behavior verification, not new error mapping.
- Imports added: `defaultRequest`, `URLProtocol` only for MockEngine configured origin.
- No production source, API, Gradle deps, Koin wiring, KDoc or logging changed. No unused old class or dependency removed without caller proof. Existing production privacy no-raw-body logging unchanged.

## Exact next acceptance process
1. Owner run `.\gradlew.bat :network:jvmTest :data:jvmTest :feature:dynamic:jvmTest`.
2. If RED, inspect `network/build/test-results/jvmTest/TEST-com.carbroz.cbpartner.network.http.RequestHandlerTest.xml` or Gradle `--tests` output. Identify **which endpoint** and whether invalid path reached MockEngine or URL build failed; distinguish a test's asserted security policy from empirically observed Ktor behavior.
3. Only then modify the single best justified owner boundary (potentially RequestHandler.executeRequest and/or central endpoint parser) and update the relevant KDoc, sanitized logging and same test; no duplicated validator or speculative token store.
4. Validate other status tests and unaffected Bootstrap/Dynamic calls.
5. Test Connection/Timeout behavior separately after the correct KMP exception types are established. Do not invent platform timeout values.
6. After any source change: complete unused import/variable/object/dependency/DI hygiene, diff check, frozen-plan traceability and file-by-file educational handoff.

**Do not claim tests passed.** A RED outcome is intentional as a possible test-first gate. W1 not complete.
