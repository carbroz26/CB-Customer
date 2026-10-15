# 04 — Runtime Values, Store, Actions and Stateful Extensions

**State:** Discussion frozen and superseded by source reconciliation for action scope; all seven existing action capabilities must be preserved. Detailed implementation plan pending.

## Runtime owners
- Entry-scoped Store `Ready.bindings: Map<String, DynamicValue>`: UI-input values, independently owned per navigation visit.
- App/session `context`: read-only approved device/platform/app metadata exposed explicitly to backend expressions.
- Session `retainedState`: only successfully committed values selected from earlier action responses; clear on session reset.
- Literals are backend-defined expressions, not another mutable store.
- Input, checkbox, picker, dropdown may all write typed values; no per-element value plumbing into Button. Future file/image upload needs distinct asset-transfer contract, not arbitrary String path assumption.

## Proposed types
```kotlin
sealed interface DynamicValue {
    data class Text(val value: String) : DynamicValue
    data class Number(val value: Double) : DynamicValue
    data class Bool(val value: Boolean) : DynamicValue
    data class ObjectValue(val properties: Map<String, DynamicValue>) : DynamicValue
    data class ArrayValue(val elements: List<DynamicValue>) : DynamicValue
    data object Null : DynamicValue
}
sealed interface ValueExpression {
    data class Literal(val value: DynamicValue) : ValueExpression
    data class Reference(val source: ReferenceSource, val path: String) : ValueExpression
    data class ObjectExpression(val fields: Map<String, ValueExpression>) : ValueExpression
    data class ArrayExpression(val items: List<ValueExpression>) : ValueExpression
}
enum class ReferenceSource { BINDING, CONTEXT, STATE }
data class RuntimeValues(
    val bindings: Map<String, DynamicValue>,
    val context: Map<String, DynamicValue>,
    val state: Map<String, DynamicValue>,
)
```
Backend syntax `$binding`, `$context`, `$state`, `$literal` is decoded into typed expressions once. `$response` forbidden. Single ReferenceResolver handles nested object/array/path resolution. Missing required request references fail before HTTP; optional display fallbacks follow a documented consumer policy.

## MVI state/events/effects
```kotlin
sealed interface DynamicIntent {
    data object Load : DynamicIntent
    data object Retry : DynamicIntent
    data class ValueChanged(val key: String, val value: DynamicValue) : DynamicIntent
    data class ActionTriggered(val action: DynamicAction) : DynamicIntent
}
sealed interface DynamicUiState {
    data object Initial : DynamicUiState
    data object Loading : DynamicUiState
    data class Ready(
        val document: ScreenDocument,
        val bindings: Map<String, DynamicValue>,
        val validationErrors: Map<String, String>,
        val submitting: Boolean,
        val actionError: String?,
    ) : DynamicUiState
    data class Failure(val message: String) : DynamicUiState
}
sealed interface DynamicEffect {
    data class Navigate(val destination: ScreenDestination, val mode: NavigationMode) : DynamicEffect
}
```
One Store per navigation entry; no ViewModel. Renderers emit Intent; Store orchestrates load, input reduction, validation, action work and effects. Stores do not serialize JSON, call Compose, mutate navigation stack or own a dedicated HttpClient. Duplicate taps blocked before launching. On disposal, cancel entry-owned work and reject stale completion/navigation; never cancel a shared injected scope. State changes precede any navigation effect.

## Generic validation
The FormValidator traverses permitted hierarchy, discovers bindable inputs and backend-provided required/pattern/range rules, and returns field-keyed failures; Every RequestAction enforces its declared validation precondition, including requests nested inside Sequence. Do not hardcode phone/OTP/first name logic; tests use varied configurations (length, regex, segmented presentation, multiple inputs). Invalid fields prevent requests. Validation UI remains on current screen.

## Action architecture
Extensible Command + registered handlers; each handler has one action responsibility. Preserve all currently supported action types, with typed handlers:
- `RequestAction`: endpoint/method/authentication/validation, expression body, response mode none/destination, retain paths; request handler uses ReferenceResolver and DynamicRepository, returns ActionResult only.
- `NavigateAction`: carries destination and navigation mode; handler returns navigation instruction, never calls HTTP or navigation stack directly.
Required types beyond Request/Navigate: ExternalUri, Present, Dismiss, State, Sequence. Sequence executes typed nested actions in order, threads retained state and preserves all ordered effects. A single `DynamicActionExecutor` dispatches to registered handlers by stable backend type, rejects duplicates and unsupported types, and owns checked typed-dispatch adapter. Adding action requires concrete DTO/domain decoding + handler + registration + tests; existing handlers do not change.

```text
Element interaction -> Intent -> Store
 -> validate if declared -> ActionExecutor -> handler
 -> Repository (request only) -> application-success outcome
 -> extract declared retained data -> ActionResult
 -> Store commits retained changes -> emits effect
 -> Host applies navigation -> next entry loads its screen
```

## Retention
Response-mode `destination` requires valid destination; `none` stays on screen. Retained paths such as `data.challengeId` are evaluated against successful action API data; missing mandatory paths are contract errors, not implicit null. No general previous-response cache. No partial retention/navigation on normal failure or cancellation.

## Other future extensions
Countdown/timer and additional genuinely new actions can be added as independent handlers; existing `state` and `sequence`, plus `external_uri`, `present`, `dismiss`, are required in initial migration. Their authoritative state transitions live in Store/session state; widgets re-render based on state rather than direct element-to-element mutation. Pure scroll animation progress may remain local renderer presentation state. Resend cooldown/rate limiting must remain backend-enforced, with UI timer merely displaying the approved deadline; timer API/state/clock/test contract is future work, not frozen executable code.

## Action request example
Backend `body.phoneNumber = {$binding: mobileNumber}`, `body.deviceId = {$context: deviceId}`. Input renderer updates `Ready.bindings`; Button renderer dispatches its RequestAction; resolver materializes both values; RequestActionHandler calls repository; successful result returns selected `challengeId`/`phoneNumber` and destination. Store commits state then host navigates. No login/OTP classes.
