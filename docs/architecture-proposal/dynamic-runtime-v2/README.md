# CB-Partner — Dynamic Runtime V2: Discussion Freeze

**State:** PLAN FROZEN via [20-PLAN-FROZEN-V2-IMPLEMENTATION-CONTRACT.md](20-PLAN-FROZEN-V2-IMPLEMENTATION-CONTRACT.md); Kotlin implementation NOT YET AUTHORIZED. Historical earlier drafts are superseded wherever inconsistent with 19/20.
**Source baseline:** `feature/dynamic-ui` at `f59577337ac78855da3f9cb474483f0211ed1f30`.
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`
**Nature:** Greenfield reference architecture. No implementation has been authorized; this does not replace existing accepted/frozen architecture or feature status automatically.

## Frozen planning authority
- [20 — PLAN FROZEN V2 Implementation Contract](20-PLAN-FROZEN-V2-IMPLEMENTATION-CONTRACT.md) — authoritative signed-off scope, exact schema, semantics, waves and test gates.
- [19 — Backend Migration Independent Contract](19-BACKEND-MIGRATION-INDEPENDENT-CONTRACT.md) — old and new backend fixture compatibility.
- [17 — Detailed File-by-File Blueprint](17-PASS6-EXACT-IMPLEMENTATION-BLUEPRINT-DRAFT.md) — create/modify/delete manifest as corrected by document 20.
- [18 — Historical Closure Review](18-PASS6-CONTRACT-CLOSURE-AND-FREEZE-REVIEW.md) — earlier proposals, superseded where doc 20 differs.

## Purpose
Record the product owner's completed Hindi-language design discussion in English, before a file-by-file implementation plan. The reference design must be reconciled against source, tests, and older accepted decisions. Conflicts are explicit review items, not silent changes.

## Documents
1. [01-PRINCIPLES-AND-MODULES.md](01-PRINCIPLES-AND-MODULES.md) — rules, modules, DI, KDoc, logging.
2. [02-BOOTSTRAP-NETWORK-AND-CONTRACTS.md](02-BOOTSTRAP-NETWORK-AND-CONTRACTS.md) — bootstrap, HTTP, headers, base URLs, envelopes.
3. [03-DYNAMIC-STRUCTURE-AND-RENDERING.md](03-DYNAMIC-STRUCTURE-AND-RENDERING.md) — six categories, typed mapping/registry/rendering, shell/slots/theme.
4. [04-RUNTIME-BINDINGS-AND-ACTIONS.md](04-RUNTIME-BINDINGS-AND-ACTIONS.md) — MVI, typed values, references, actions, retained state, timers.
5. [05-NAVIGATION-AND-PLATFORM-LIFECYCLE.md](05-NAVIGATION-AND-PLATFORM-LIFECYCLE.md) — destination identity, pop-to/refresh, Android/iOS/Web.
6. [06-TESTING-AND-PLANNING-GATES.md](06-TESTING-AND-PLANNING-GATES.md) — tests, acceptance criteria, unresolved decisions, next phase.

## Illustrative end-to-end path
```mermaid
flowchart TD
 P[Android iOS Web entry] --> S[Splash Store]
 S --> B[Bootstrap Repository]
 B --> N[Shared HTTP transport]
 B --> D[Startup destination]
 D --> Stack[Navigation state]
 Stack --> H[One Dynamic Container]
 H --> Store[Entry-scoped Dynamic Store]
 Store --> R[Dynamic Repository]
 R --> N
 R --> Doc[Validated screen document]
 Doc --> Render[Category-specific renderers]
 Render --> Intent[Binding changed or action clicked]
 Intent --> Store
 Store --> Exec[Action executor and registered handler]
 Exec --> R
 Exec --> Outcome[Retained values and navigation effect]
 Outcome --> Stack
```

## Scope and preservation
- Discussion covers all application screens through generic mechanisms, using login/OTP only as illustrative fixtures.
- Supported existing action capabilities to preserve in V2: `request`, `navigate`, `external_uri`, `present`, `dismiss`, `state`, `sequence`. Timers, upload, and new action types remain future extensions. Header/Footer have an agreed additive direction but their exact wire schema remains pending.
- No native login/OTP-specific classes or hardcoded business flows.
- No parallel navigation, network, mapper or cache infrastructure.
- Source/test execution was **not** performed for these documents.
- Treat the source baseline as historical evidence only: Decision 003 changes exist but remain to be verified; earlier frozen decisions retain their existing authority until explicit reconciliation.
- This folder is a **proposal discussion record**, not a second Master State or a replacement for `AI_START_HERE.md`.


## 2026-10-16 current implementation and runtime addendum (read this before older historical statements)

**This section supersedes only this index's earlier pre-implementation status language.** Doc 20 was the historical V2 plan freeze, not a claim that no subsequent implementation occurred. Read the later numbered documents for wave-specific truth; never declare all W1–W8 complete just because initial slices passed.

- Frontend active branch `architecture/dynamic-runtime-v2-discussion-freeze`; last owner-confirmed local fast-forward HEAD `6f67e567`. The owner repaired a corrupted loose `refs/remotes/origin/architecture/dynamic-runtime-v2-discussion-freeze` ref, fetched and fast-forwarded, and `git fsck --full --no-reflogs` completed with only dangling commits reported.
- W1 network, W2 bootstrap, W3 category/slot, W4 navigation and selected action work have later source/verification documents. Relevant most recent action evidence: docs 60–81. The Sequence handler remains an **inert registered placeholder** under later owner decision, superseding the historical executable-Sequence aspiration in plan 20. State implementation is deferred; Present semantics are deferred. Request action and N1 modal-only Dismiss are implemented/verified; resolver consolidation done; obsolete EffectFailed cleanup completed under docs 79–81. Preserve effect batching and independent error ownership. Do not implement deferred features based on older prose.
- Owner-confirmed physical Samsung Android launch on 2026-10-16: Bootstrap then Login screen rendered when APK was built with `-Pcbpartner.api.baseUrl=http://127.0.0.1:3000` and wireless ADB reverse `tcp:3000 tcp:3000`. Web Bootstrap/Login also observed working. Direct phone → Windows LAN IP connection was not resolved; adb reverse is development workaround. The phone's ADB Wi-Fi port changes; always obtain exact device serial from `adb devices -l`. See root README for verified commands.
- Owner-confirmed **Kermit network summary logs** from Android Logcat after local HEAD `6f67e567`: two `I/CBPartnerNetwork` GET successes with statuses 200 and durations 268ms / 58ms. Earlier blank Logcat was due to a stale local APK after corrupted Git fetch. App-only Logcat, not Antigravity Output panel, is presently verified. SLF4J NOP warnings are observed but do not negate Kermit logging.
- **CURRENT DISCUSSION ONLY — NOT APPROVED/FROZEN/IMPLEMENTED:** Improve developer observability with short, visually scan-friendly, correlatable network/request/response/screen navigation/action/state/method-boundary events, showing source→target destination (screenId/templateId/templateType/endpoint), request status/timing, and structured pretty JSON where safely possible. User wants readable color distinctions and asks whether tooling exists for visualization. Colorized Antigravity terminal / viewer capabilities and privacy-safe structured logging are to be researched and source-audited before choosing an implementation. The current frozen safe-logging policy in document 23 prohibits raw bodies, user data, tokens, OTP, device identifiers, arbitrary paths or URL and untrusted server text in **all** environments. No blanket raw JSON log, generic blacklist-only redactor, ANSI escape insertion into Logcat, new logging framework/manager, speculative tooling, or source edits without owner authorization. Proposed pretty JSON must use an explicitly reviewed allowlisted diagnostic projection, or the owner must separately approve a documented policy amendment after risk review.
- **NEXT GATE:** prepare a line-by-line read-only source audit of Kermit setup, `RequestHandler`, `HttpClientBuilder`, `NetworkModule`, app/platform composition, BootstrapRepository/Mapper, SplashStore, DynamicRepository/Mapper, DynamicStore/action executor/handlers and navigation state/effect handoff; inventory the event and destination ownership, existing diagnostics/SLF4J paths, Android/Web output capabilities and available tests. Compare actual code against documents 20, 23, 34, 60–81 and decision logs. Then propose concise sample output, minimal touched-file plan and testing/redaction policy for owner review. Do **not** code until the distinct observability plan is explicitly frozen and implemented separately.

## Latest handoff

- [82 — Current State, Logging Discussion and Next Chat Handoff](82-LOGGING-DISCUSSION-AND-NEXT-CHAT-HANDOFF.md) — 2026-10-16 runtime evidence, source pointers, backend boundaries, pending observability discussion; not implementation authorization.
