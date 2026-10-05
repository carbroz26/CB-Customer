# Dynamic UI — 32-Step Implementation Status

Branch: `feature/dynamic-ui`
Plan: `CB-Partner-Dynamic-UI-32-Step-Implementation-Plan.md`
Mode: continuous implementation pass.

| Step | Status | Notes |
|---|---|---|
| 1 | COMPLETE | Baseline synchronized at commit `671ffec`; Android debug assembly and `clean test` were verified by the developer on Windows. Known iOS simulator and Android host-test warnings are non-failing. |
| 2 | COMPLETE | Removed artificial `RuntimeFoundationTest`; removed unused coroutines/serialization Core dependencies. Core now retains only Kermit, which is its actual current responsibility. |
| 3 | COMPLETE | Removed unused Domain → Core dependency. |
| 4 | COMPLETE | Repository code search found no usage of generic `network.Response<T>`; removed the competing envelope. Dynamic response DTO remains the canonical `{status, code, message, data, traceId}` data boundary. |
| 5 | COMPLETE | Added explicit HTTP error taxonomy and status mapping; arbitrary HTTP failures are no longer mapped to NotFound. |
| 6 | COMPLETE | Added `DynamicRequest` so request metadata is separate from `DynamicDestination`; request actions no longer construct fake action destinations. |
| 7 | COMPLETE | `NONE` sends no Authorization header; `BEARER` now uses a minimal injected credential provider without introducing token/session architecture. |
| 8 | COMPLETE | `DynamicResponseDto` directly matches the real response envelope and remains the data-layer serialization boundary. |
| 9 | COMPLETE | Dynamic domain hierarchy retains screen theme as sibling of template and preserves template/component/section/group/element nesting. |
| 10 | COMPLETE | `DynamicResponseMapper` remains DTO → domain only; no rendering, navigation or network execution was added. |
| 11 | COMPLETE | Added type-safe renderer capability contracts for template/component/section/group/element. |
| 12 | COMPLETE | Registry now stores typed renderer maps and has no rendering/default-registration side effects. |
| 13 | COMPLETE | Added explicit `dynamicRendererRegistry()` composition point; registration now includes `form_template`, stack containers and current element capabilities. |
| 14 | COMPLETE | Added focused `DynamicRenderContext` for registry access, runtime bindings/context values and UI callbacks. |
| 15 | COMPLETE | `DynamicRenderer` now only validates destination identity, applies screen theme and delegates the template tree. |
| 16 | IN_PROGRESS | Screen-level theme gradient is applied independently of template and respects backend angle; status-bar platform effect remains pending. |
| 17 | COMPLETE | Added shared multiplatform layout/style translation for padding, size, maxWidth, fill, alignment, arrangement, background, border and shape. Row weight is intentionally kept in RowScope only. |
| 18 | COMPLETE | Added `FormTemplateRenderer` for the actual `form_template` contract. |
| 19 | COMPLETE | `StackComponent` applies backend layout properties and renders elements/sections in response order. |
| 20 | COMPLETE | `StackSection` applies backend layout properties and renders elements/groups in response order. |
| 21 | COMPLETE | `StackGroup` supports vertical/horizontal layout and the supplied Login/OTP group properties. |
| 22 | COMPLETE | Text styling, spans, font properties, clickable `LinkAnnotation` spans and nested leading/trailing decoration definitions are supported. |
| 23 | COMPLETE | Added Coil 3.6.3 Compose Multiplatform + Ktor3 support and replaced image placeholder text with `AsyncImage`; relative URLs can use runtime `baseUrl`. |
| 24 | COMPLETE | Added bound phone/number input rendering, maxLength, keyboard type, OTP segmented presentation and value-change action dispatch. |
| 25 | COMPLETE | Added dynamic button rendering, gradient background, shape, enabled state and `onClick` action dispatch. |
| 26 | COMPLETE | Added divider and icon renderer capabilities and registered them for root elements and text decorations. |
| 27 | COMPLETE | Store owns runtime state/orchestration: loading, bindings, context, previous response and action/effect coordination. Validation is isolated in `DynamicFormValidator`. |
| 28 | COMPLETE | Sequence handling moved into `DynamicActionExecutor`, so composite actions recursively use the same dispatcher instead of constructing handlers directly. |
| 29 | COMPLETE | Input changes update bindings before dispatching `onValueChange`; button/text/span actions dispatch through the same DynamicAction pipeline. |
| 30 | COMPLETE | External URI effects are consumed at `DynamicContainer`; navigation/dismiss/present remain behind `DynamicNavigationState`, not platform APIs. |
| 31 | IN_PROGRESS | Added Login/OTP contract tests covering envelope, template/theme, input binding, segmented OTP, request action and destination action. Full Gradle execution remains to be run on Windows. |
| 32 | PENDING | Final reconciliation and Windows Gradle verification remain. |

## Execution rule

For every step: implement → verify → update this status → immediately continue. Stop only for a genuine blocker or contract contradiction.

## Verification note

Repository edits are being applied directly to `feature/dynamic-ui`. Full Gradle verification must be run from the developer's Windows checkout because this environment cannot execute the repository's Gradle wrapper.
