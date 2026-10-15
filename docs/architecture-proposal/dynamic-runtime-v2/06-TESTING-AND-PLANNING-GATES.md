# 06 — Testing, Verification and Next Planning Phase

**State:** Discussion frozen, implementation plan pending. Nothing here claims compilation, device execution, or complete coverage already achieved.

## Required tests by concern
| Area | Test matrix |
|---|---|
| Bootstrap | Successful config/startup; missing/invalid fields; maintenance/required update/optional update; retry; cancellation; duplicate fetch prevention |
| Shared HTTP | Base URL and relative endpoints; host allowlist; GET/POST body; content-type/accept; common headers; optional metadata; authenticated/unauthenticated; missing bearer token; HTTP failures; cancellation; no sensitive logging |
| Response/DTO | Exact bootstrap/screen/action envelope; invalid application code despite HTTP 200; missing data; destination method/auth metadata; malformed JSON |
| Structural mappers | Each current concrete type; direct elements and nested Section/Group; multiple Components; leading/trailing Content; unsupported types and schema |
| Renderer | Each supported node's backend properties; invalid configs; visual themes, layout, arrangement, background, line breaks, text spans; accessibility/semantics |
| Input generic | Empty/required, min/max length, pattern, keyboard types, segmented count/spacing, recomposition, focus, disabled, multiple independent bindings; generic examples beyond phone/OTP |
| References | All four namespaces, nested arrays/objects, dotted paths, optional vs required missing, malformed/unknown namespace |
| Request action | Correct method, endpoint, auth, typed resolved body, responseMode none/destination, retention paths; API error; missing destination, retries, duplicate tap/cancellation |
| Navigate action | push/replace/back target semantics; effect delivery and ordering; no HTTP in Navigate |
| Navigation | Unique template IDs, separate entry IDs, direct popTo A from C, absent target, repeat visits, optional refresh and binding policy, Android/Web back |
| Store | Initial/Loading/Ready/Failure, input changes, validation preventing request, submission state, recoverable error while preserving screen, cancellation/disposal, no late navigation |
| Header/Footer | Each slot independent padding, only Body receives body padding, backgrounds/full-width, safe-area/keyboard, fixed footer, long scroll; future collapse only when approved |
| Integration | Synthetic Bootstrap -> generic form screen -> request -> retained values -> next generic form -> verify request, without business-specific production classes |
| Platforms | JVM test matrix, Android compile/device, Web/Wasm browser run, macOS iOS build/simulator/device, platform HTTP/keyboard/inset behavior |

Treat login/OTP as example fixtures only; generic contract test parameters must cover multiple usages of the same Input/Text/Button/Template.

## Definition of test completeness
Cover supported properties and meaningful normal/invalid/boundary/lifecycle paths, not arbitrary line coverage 100%. Test files should reflect supported contracts, not duplicate production implementation details. Golden/screenshot tests where visual precision matters, plus accessibility and user interaction tests.

## Planning requirements
After discussion freeze, write a **separate exact implementation plan** with:
1. Repository and existing frozen-doc reconciliation before authorizing source changes.
2. Explicit decisions for breaking backend JSON changes and compatibility.
3. File-by-file create/modify/delete list, constructor API, dependencies, SRP notes and non-responsibilities.
4. Unit/integration/UI/platform test files and required commands, including iOS macOS gate.
5. Small vertical implementation units: network/bootstrap -> navigation/host -> screen DTO/domain -> renderer -> bindings -> request/navigate -> retention -> end-to-end.
6. Diff scope, logging/privacy, migration/rollback and stop conditions.
7. PLAN FROZEN only after owner review; implementation requires separate authorization.

## Open implementation-contract decisions (not silently resolved)
- Exact `templateId` uniqueness scope, repeated navigation visits and `popTo` missing/duplicate target behavior.
- `refresh` API schema, old entry Store/bindings preservation, whether refreshing API should call destination from target or instruction.
- Final header/body/footer wire schema migration from current template direct `components` and `properties.padding`.
- Which future Template concrete types support slots/animations; persistent app-level footer ownership.
- Exact secure/common header names, optional Device Name and ID policy, approved API host rules and environments.
- Application-level `code` failure shape, DTO error policy, required field/missing binding semantics.
- Renderer registry type-safe dispatch implementation details; whether IconElement exists independent of IconContent.
- Asset URL/fallback/icon backend contract (no name-to-native-glyph fallbacks without approval).
- Typed expression migration and concrete Kotlin numeric model.
- Action registry type safety, state effect durability, future timer/visibility semantics; only Request/Navigate initial.
- Platform app lifecycle, iOS configuration, Web browser history and process restore.
- Decision 003 cancellation work has source commits but requires local test/build verification before declaring code freeze.

## Conflicts with existing authority
The legacy AI_START_HERE and project docs describe a four-field destination, current action capabilities (including external_uri), existing mapper/registry naming and feature work. The proposal discusses six destination fields, typed expressions, two initial actions, slots and new navigation behavior. These are **not already agreed backend/source migrations**. Report and resolve during reconciliation; this discussion freeze does not automatically override other decisions or authorize deleting existing functionality.

## Exit criterion for this folder
All agreed discussion concepts durably documented. No claim that this is an implementation freeze, source migration, completed tests or final overall Dynamic-feature freeze.
