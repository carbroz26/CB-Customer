# 110 — T2 Terminal SDUI Structure, Decode & Mapping Diagnostics — Exact Plan

**State: PLAN REVIEW / OWNER FREEZE PENDING; SOURCE IMPLEMENTATION NOT AUTHORIZED.**
Branch: `architecture/dynamic-runtime-v2-discussion-freeze` only. Backend read-only. T1 operation correlation is implemented. Doc23 safe Kermit policy prevails. Docs107/108 describe an earlier guarded in-app inspection path; Doc108 is superseded. T2 does **not** reintroduce a UI inspector or claim the Doc107 exception permits Kermit output.

## Source-level findings

1. `DynamicRepositoryImpl.fetch` receives `NetworkResult<String>`, calls `json.decodeFromString<DynamicResponseDto>(response.result)` and `mapper.map(dto)` in a *single* `runCatching`; it does not currently distinguish decode failure from domain mapping failure in operational logs. Cancellation is rethrown.
2. `DynamicResponseDto` wraps `status/code/message/data/traceId`; `data` is nullable `JsonElement`, not invariably a screen. `DynamicResponseMapper.map` recognizes a screen only when `data` is a `JsonObject` containing all `screenId/schemaVersion/targetApp/template`. A destination may also be extracted; non-screen data becomes `dataPayload`. Do not misclassify successful non-screen responses.
3. `TemplateMapper` handles `form_template` and its `header/components/footer`. `ComponentMapper`, `SectionMapper`, `GroupMapper` handle `stack_component/stack_section/stack_group` and both direct elements and nested collections. `ElementMapper` handles `text/image/input/button/icon`. `ElementContentMapper` handles `icon/divider` from `properties.leading/trailing`. Existing mapper recursion is source truth; do not invent Grid/List renderers or flatten children.
4. The typed DTOs retain `JsonObject properties` and nested `JsonObject` lists. Their contents can include free text, personally identifying strings, resource URLs, bindings and action values. **Never stringify DTO, domain model, or raw response into Kermit.**
5. T1's network `requestId` is internally generated in `RequestHandler` but is not passed back in `NetworkResult`. T2 may correlate with existing `sessionId/actionId/parentActionId` from `DynamicRequest`; **must not fabricate or relabel a requestId at repository boundary**. A new request ID propagation feature is outside T2.
6. Existing `DynamicResponseMapperTest`, `DynamicContractTest`, `DynamicBackendFlowContractTest` and `DynamicContractFixtures` cover parsing contracts; extend focused tests instead of rebuilding fixture stacks.

## Frozen candidate diagnostic schema (owner review before freeze)

Emit through existing Kermit operational logging path only; short, bounded **structured events**. No raw body or serialized JSON. Use new tag `CBPartnerSDUI` (existing Kermit, not a new logger class); env severity remains centrally controlled.

- `event=sdui.decode.started sessionId=<safe> actionId=<safe> parentActionId=<safe>` **only after an HTTP transport success and immediately before actual decoding**.
- `event=sdui.decode.completed ... outcome=success|failed category=none|serialization|unknown`. A completed success means typed response wrapper decoding succeeded, not mapping.
- `event=sdui.map.started ...` **only after successful decode**.
- `event=sdui.map.completed ... outcome=success|failed category=none|unsupported_type|validation|unknown responseKind=screen|destination|payload|empty|mixed|unknown` (responseKind only derived from mapped result; on failures use `unknown`). Exception messages, backend message/code, URLs and user fields never logged.
- `event=sdui.structure ...` **only after successfully mapped screen**, with bounded, sanitized structural projection from actual typed **domain** tree. No claim that this is the raw backend JSON. On non-screen successful payload emit `event=sdui.structure.skipped reason=non_screen` rather than fake screen nodes.
- No unconditional `event=sdui.decode` for HTTP failures; existing `http.response` carries transport failure. Cancellation rethrows and emits an appropriate `outcome=cancelled` for the active stage if practicable, never a success.
- Output can be multiple bounded `event=sdui.node` records with common trace context and real nesting `path` in code-owned index grammar, or a single bounded event tree. **Avoid arbitrary user IDs, JSON keys or JSON values in the node path**. Use numeric indices and allowlisted property labels only. Never pretend logs are a byte-for-byte serialized backend response.

### Structural projection contract

Only known type tokens, code-owned relationship names, numeric collection indices, numeric counts, and a strict property-presence/type whitelist. Suggested node format:
`event=sdui.node path=template/components[0]/sections[0]/groups[0]/elements[0] category=element type=button children=0`
with `slot=body|header|footer`, `accessory=leading|trailing` when applicable. Treat IDs, raw `properties`, arbitrary text, `actions` body, `binding` values, URLs, validation pattern/message, `traceId` and backend `message` as **not allowed**. Unknown type strings map to `other`; successful mapping should otherwise establish supported types. `properties` safe projection means only schema-backed boolean/numeric/enum **presence or value type**, not arbitrary free-text values. Sensitive field values must not appear through `toString()`, exception content, composite field, unbounded path or unknown keys.

Preserve actual template slot ordering and direct-element alternatives:
- screen → template (one) → body/header/footer components (0..n per actual slot DTO) → component sections/elements → section groups/elements → group elements → each element leading/trailing content.
- The six conceptual categories are template, component, section, group, element, content; the screen is container metadata, not a seventh render category.
- Respect a strict log budget (proposal: max 100 structural nodes and maximum total output 16 KiB per screen; emit `truncated=true` after limit). Node limit must not truncate the business model or alter UI; diagnostics must fail safely without altering outcomes.
- Do not report DTO type parsing as successful domain mapping. Domain structure is not renderer selection or Compose commit.
- If exhaustive full SDUI property values are demanded, pause for separate privacy/field contract authorization; not T2.

## Exact file-by-file implementation plan

1. **EDIT** `data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/DynamicRepositoryImpl.kt`: separate existing decode/map calls to record actual outcomes around each operation while retaining the same Result type, cancellation propagation, remote error mapping, transport body encoding, and exceptions. Reuse `DynamicRequest.sessionId/actionId/parentActionId` with `safeDiagnosticId`. Add bounded structural event only for successfully mapped screen. No logger at every mapper and no network transport changes.
2. **NEW (only if needed after focused design)** `data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/diagnostics/SduiStructureProjection.kt`: pure bounded traversal of existing **domain** tree to produce safe, source-backed structural node strings, with strict fixed vocabulary and fail-closed redaction. This is a narrowly scoped projection function/class, **not** an Inspector manager, logger wrapper, serializer or renderer. Before implementation, audit exact domain sealed/concrete model shapes and choose file-vs-private helper based on tested complexity. If this can be done simply inside one existing file without violating SRP, avoid new file.
3. **EDIT** `data/src/commonTest/kotlin/com/carbroz/cbpartner/data/dynamic/mapper/DynamicResponseMapperTest.kt`: assert actual responseKind for screen/destination/payload/empty/mixed variants using existing fixtures as feasible; do not add logging to mapper itself.
4. **NEW** focused `data/src/commonTest/kotlin/com/carbroz/cbpartner/data/dynamic/DynamicRepositoryDiagnosticsTest.kt` testing MockEngine transport, decoded/mapped success and errors, safe stage ordering, non-screen handling, cancellation and no secret logging. Use existing `libs.kermit.test` in data; avoid new dependencies.
5. **NEW** `data/src/commonTest/kotlin/com/carbroz/cbpartner/data/dynamic/diagnostics/SduiStructureProjectionTest.kt` **only if** a separate projection source exists; verify body/header/footer, direct elements, nested sections/groups, both content lists, size budgets, unsupported/free-text/redacted fields. Otherwise keep relevant focused tests beside repository diagnostics tests.

**Explicit non-edits:** RequestHandler, DynamicRemoteDataSource, all seven existing typed mappers and their DTO wire models, Domain interfaces, Store, Actions, Renderer, navigation, app composition, Android/Web/iOS entry, DI modules, frontend/ backend endpoint JSON. Any requirement to modify these is a change-control trigger before coding.

## Risks and pre-implementation checks

- Validate actual `DynamicRequest`, `DynamicResponse`, `FormTemplate`, `StackComponent`, `StackSection`, `StackGroup`, `DynamicElement`, and `ElementContent` domain model traversal shapes **before writing projection code**. The source review so far fully covered repository/mapper and representative DTOs, but not yet each domain class's exact constructor/content property contract.
- Confirm Kermit environment sink: Production severity settings must not emit debug-only structures; **no structure event in staging/production** even if a configured logger admits Info. Explicit environment gating at the data owner boundary requires a source-backed environment signal; do not assume Logger severity alone constitutes eligibility.
- Reconcile Doc107: the formerly authorized *in-app* local exception is not a permission for text terminal dumps; maintain Doc23.
- Verify `DynamicResponseMapper` classification with fixtures. Map failure categories must derive from known exception types only, not free-form error text.
- If no legitimate environment signal exists in data without new DI/config plumbing, postpone structural event output rather than introducing a global environment singleton or violating Doc23.

## Gate and evidence

**Do not implement until this plan is reviewed/frozen by owner, and a second explicit T2 implementation authorization is received.** Run `:data:jvmTest :network:jvmTest :feature:dynamic:jvmTest :app:jvmTest`, `:androidApp:assembleDevelopmentDebug :webApp:wasmJsBrowserDevelopmentWebpack`, then `git diff --check`; source review and A35 real Logcat evidence of one screen and one non-screen response. Redaction tests must contain fake OTP, token, phone, URL, free text and mixed unknown keys and assert none leak. Windows cannot validate iOS simulator; report this gap.
