# 05 — Destination Identity, Stack Semantics, Platform Lifecycles

**State:** Discussion frozen; popTo/refresh wire syntax is proposed and must be agreed with backend.

## Identity
Product guarantee: each screen-template instance has globally unique `templateId` within the backend-driven screen flow, even if ten screens all use `form_template`. `templateType` is the concrete renderer contract; `screenId` is screen identity; `endpoint` supplies next screen document.

**Preserve separate concepts:**
- `templateId`: backend identity, useful lookup key to find an existing stack destination.
- `NavigationEntry.id`: unique identity of one stack visit, Store lifecycle and screen-local bindings.
- `templateType`: expected type; response must confirm exact actual type and template ID before rendering.
Repeated visits to the same unique template may need distinct entries; do not assume template ID alone safely captures visits/lifecycle. Define duplicate-template lookup policy before popTo.

## One container / one stack authority
All screens use one DynamicContainer/Host; its active entry creates an entry-scoped DynamicStore and performs one screen fetch using destination endpoint/method/authentication. Neither action handlers nor navigation state call screen APIs. No duplicate dynamic stack or cache.

## Proposed navigation operations
- `push`: create new entry.
- `replace`: replace current entry.
- `pop`: return one entry if allowed.
- `popTo(templateId)`: locate an existing entry, remove those above it, fail explicitly if absent/ambiguous.
- `refresh` (modifier): reload target destination endpoint; independently specify preserve/reset binding policy and Store recreation/reload semantics.
Example backend proposal (NOT current approved contract):
```json
{
  "type": "navigate",
  "destination": {
    "screenId": "screen_a",
    "templateId": "tpl_A",
    "templateType": "form_template",
    "endpoint": "/api/v1/screen/a"
  },
  "payload": {"mode": "popTo", "refresh": true}
}
```
Direct C -> A by template ID is a valid desired capability; exact mode and failure behavior require future plan/backend approval.

## Navigation state and session
```kotlin
data class NavigationEntry(val id: EntryId, val destination: ScreenDestination)
class DynamicSession(val navigation: NavigationState, val retainedState: RetainedState)
```
The session owns cross-screen data and navigation lifecycle. New entry owns screen bindings, validation, in-flight load/action work. Navigation host consumes one-time Store effects and applies navigation; it does not interpret raw JSON.

## Android / iOS / Web
- Android: Activity platform entry + runtime metadata provider; shared PartnerApp.
- iOS: Xcode Swift entry invokes Kotlin shared UI; common runtime; iOS engine and lifecycle tested on macOS. iosApp is not a Gradle module.
- Web: Wasm/browser entry; browser-compatible Ktor engine/CORS and history/back integration; no second independent browser navigation state.
- Platform entry provides base URL/environment, runtime/device metadata, secure token service and platform integrations as needed.
- One Compose screen host and generic renderers across supported platforms. Native back/history delegates to single navigation authority.

## Coroutine/lifecycle rules
- Entry scope is child of owning session/app lifecycle scope; cancel entry work when disposed.
- Do not turn coroutine CancellationException into a network/business failure or navigation effect.
- Prevent stale action completions and navigation after close.
- RetainedState clears on session reset/logout; persistence and process-death restoration are separate explicit product choices.

## Screen verification
Before rendering: received screenId matches expected, template.id matches expected templateId, template concrete type matches templateType, supported schema version matches runtime capability. Violation results in explicit load failure (not empty content).
