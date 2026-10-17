# 107 — Local Development SDUI Inspection Exception — Decision Frozen

**State:** OWNER DECISION FROZEN (2026-10-17); source-level implementation plan and separate implementation authorization PENDING. **This document is not approval to change Kotlin, backend, or logging code.**
**Frontend:** `carbroz26/CB-Partner`; only branch `architecture/dynamic-runtime-v2-discussion-freeze`. Backend READ ONLY.

## Precedence and reconciliation

This narrowly scoped decision supplements **23-W0-ADDENDA-FROZEN-KDOC-LOGGING-ENVIRONMENTS.md**, section B and section E item 5, and **83-V1.1-LIFECYCLE-LOGGING-001-PLAN-FROZEN.md**. It does **not** repeal their default prohibition on sensitive values in Kermit, Logcat, operational diagnostic events, or persistent logs. Source truth and the three deployment environments remain unchanged. When a reader sees “all environments prohibit raw request/response bodies” in Doc23, that statement still governs **application logging**. An explicitly opted-in, eligible **local inspection surface** is a separate capability, not a new permission to log raw payloads. Historical requests for unrestricted raw JSON/OTP are recorded as user debugging goals, **not** authority to persist plaintext secrets.

## Purpose and visibility

Local frontend developers need to inspect the real backend-driven screen flow:
HTTP request/response and dynamic endpoint identity; actual-present request metadata; complete typed SDUI nesting including template header/body/footer, components, sections, groups, elements and leading/trailing content; styles, binding/reference *structure*, action definitions and destination structure; DTO decode, domain mapping, registry lookup, Compose commit, interaction origin, action handler, navigation and subsequent screen load. A 200 response or render invocation must not be presented as successful parsing or committed rendering.

Distinguish:
1. **Safe Kermit diagnostics**: existing structured operational events; no request/response bodies, arbitrary URL/query, phone, OTP, token, device ID or untrusted exception text; remains usable in all environments under Doc23 severity policy.
2. **Explicit local inspection**: a separately enabled read-only representation of SDUI/HTTP data, with faithful structure and real source-backed execution milestones; no automatic Logcat/Kermit duplication of raw values. Structure and safe value visibility should be maximized rather than reducing the output to HTTP status and counts.
3. **Sensitive ephemeral debugger values**: request to see OTP/token/Aadhaar/plaintext credentials in logs is **not approved**. Secret values must not be printed, copied to persistent terminal history or written to inspection archives. A separate secure, non-logging debugger workflow would require its own review and authorization.

### Inspection output contract

- Identify actual source and HTTP method; dynamic backend endpoint identity is important and must not be replaced by an invented static operation. Exact endpoint display is eligible **only** after local-only gating and per-field safety review; arbitrary query strings, credentials and personal identifiers are excluded. Fallback to an opaque endpoint identifier when unsafe.
- Show fields only when actually present; never synthesize `Authorization`, `Content-Type`, headers, body or server properties. Preserve JSON object/array nesting and order where available.
- For *known-safe* SDUI fields, show actual validated IDs, registered type strings, booleans, numbers, structural style definitions and action/event types. Dynamic free text, binding values, passwords, OTP, credentials, device/user identifiers, token-bearing URLs, unrestricted payload values and untrusted server messages must be redacted or omitted. Unknown properties retain safe structure when defensible; otherwise show an explicit omission marker. The output is labeled `SANITIZED SDUI INSPECTION`, never `RAW SERVER JSON` unless raw equivalence is provable and separately authorized.
- Report parsing/mapping acceptance separately from renderer selection and Compose `render.committed` (SideEffect, not on-screen pixels). Show actual element/content origin and event without user-entered values. Existing action/result/effect and navigation ownership remain intact.
- Maintain existing generated correlation: session ID, per-operation action ID, HTTP request ID and parent action ID; distinguish bootstrap, screen load and user action. No static fabricated method traces or fake destination values.

## Eligibility: fail closed

Inspection is OFF by default and requires **all** conditions: `AppEnvironment.Development`, debuggable build, verified local backend connection, and an explicit developer opt-in. Development pointing at remote origin is NOT eligible; staging, production and any release build are NOT eligible. Android ADB reverse, emulator, Web and iOS local-network schemes each need source-backed validation; a string containing “localhost” or `127.0.0.1` alone is not sufficient proof. Inspection state must be impossible to enable on excluded combinations. Removing the local backend or switching to a remote deployment must disable inspection without relying on manual log cleanup. Do not introduce a fourth deployment environment.

## Lifetime, safety and architecture

- Inspection is read-only, development-only and ephemeral; no default file export, clipboard write, crash-report attachment, analytics upload or persistent logging of the inspected structure.
- Redaction and field allowlisting are applied **before** material enters the inspection sink. Unknown/unsupported fields fail closed. Access-control eligibility must be enforced where data is obtained, not merely visually hidden by the PowerShell viewer.
- Existing Kermit and single Ktor/RequestHandler stack remain authoritative for safe operational tracing. No second HTTP client/interceptor, independent navigation owner, ViewModel, generic manager or per-mapper/per-renderer logger framework.
- Keep exact typed six-category DTO/domain/mapper/renderer contracts, no new backend JSON meaning, UI defaults or action wire changes. Logging/inspection failure must never change app outcomes, including cancellation behavior.
- Staging `Info` and Production `Warn` levels unchanged. Kermit `Debug` in Development is **not** itself eligibility for local inspection.
- Any diagnostic exception to this policy requires a separately frozen source-specific security amendment, not an inferred “local means safe” assumption.

## Bounded implementation plan — DRAFT ONLY, not yet authorized

L1: source audit and exact file plan for environment/debuggability/local connection/opt-in and ephemeral sink; negative eligibility tests.
L2: source-backed dynamic endpoint and present-only request fields; redaction and no raw transport logger; concurrency/requestId tests.
L3: typed SDUI/DTO-domain structure projection, with explicit allowlist and truncation/size budget; nested header/body/footer and element content tests.
L4: mapping, renderer registry, screen identity mismatch and actual render milestones; no per-class logging duplication.
L5: action origin for click, long-click, text-link, input value change/focus/submit, and content actions; no user-entered value logging.
L6: six-stage chronological colored-text, no-background PowerShell viewing of approved structured events, not a privacy boundary.
L7: focused JVM/Android/Web privacy, concurrency, cancellation, regression and real-device tests; iOS runtime separately pending on macOS.

**Remaining implementation gates:** enumerate exact platform sink and local trust evidence; settle per-field safe JSON allowlist and size limit; enumerate all origin callback paths; select exact files; owner freeze exact file-by-file plan; request separate implementation authorization; verify after each unit before proceeding.

## Existing B1 evidence and limits

VIEW-001B B1 lifecycle events are already implemented in `SplashStore`, `DynamicStore` and `DynamicActionExecutor` on the same branch. Owner reported passing splash/dynamic/network/app JVM tests, Android development assembly and Wasm webpack. Android Logcat verified bootstrap and screen-load event-to-HTTP correlation. Handler selection / Login-to-OTP runtime was not yet demonstrated in the supplied capture. This decision does not mark B1's remaining runtime acceptance complete.
