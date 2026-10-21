# B4 — Generic Button Origin and Loading-Flags Contract (FROZEN)

**Date:** 2026-10-22
**Status:** CONTRACT FROZEN — implementation NOT authorized by this document
**Repo / branch:** `carbroz26/CB-Partner` / `architecture/dynamic-runtime-v2-discussion-freeze`
**Reconciled source baseline:** `8f3c924d6878b9f3795b983a37a6f36da6711473`
**Scope:** L-07 / B4 only. This document closes the origin-identity and supported loading-flag gates of the reviewed B4 implementation plan. It is not authorization to implement B4 or another batch.

## 1. Architecture and responsibility

- Dynamic Runtime knows only the six existing typed UI categories: Template, Component, Section, Group, Element and ElementContent, plus generic Actions and state/lifecycle infrastructure. Never branch on `screenId`, Login, OTP, KYC, Dashboard, country, phone number, or a backend product workflow.
- `ButtonElement` remains the sole concrete Button Element type. `DynamicButtonLoading` and `DynamicLoadingIndicator.Circular` already exist; no new loading DTO schema, additional action type, ViewModel, manager, registry, navigation stack, or network layer.
- The existing `DynamicStore.actionJob` remains the single-flight action execution owner; `DynamicActionExecutor` and `RequestActionHandler` do not become presentation/UI state owners.
- Pending is transient UI state owned by the current Store/Screen, not a `$state` binding, navigation-entry retained value, backend value, or global singleton.

## 2. Origin identity (FROZEN)

An originating Button is resolved by the complete local UI identity:

    (navigation entry instance, render scope, ButtonElement.id)

where the render scope is:

- **Screen:** the currently rendered Template instance of that navigation entry.
- **Presentation:** the specific active Presentation **open generation** of that entry, in addition to the `presentationId`. A dismissed/reopened Presentation with an identical ID is a different instance.

One Store may have only one action in flight. Its pending origin (when present) is one Button in one exact scope. An identical Button ID in Screen and Presentation is allowed because the scope differs; an identical Button ID in different navigation entries is independent.

**Supported-scope uniqueness:** Inside one Screen/Template tree, Button IDs must be nonblank and unique across all currently supported Body components, Header/Footer slot components, Sections and Groups. Inside any one Presentation tree, Button IDs must likewise be nonblank and unique across its supported components/sections/groups. This includes sibling Components. Duplicate IDs within the same scope must fail clearly; no last-wins mapping, no spinner shown for all matches. Other Element types do not participate in this Button-only identity uniqueness rule.

**No future promises:** Dynamic ListSection repeated-item instance identity, `$item` row keys and list virtualization are Phase 4; do not claim they are covered by B4, and do not invent a generic tree-visitor or row key scheme. Identity validation may traverse only the currently implemented concrete composition.

**Open generation:** Assign a new transient token on each successful Presentation open. A Presentation-scope callback must carry the token from the exact UI instance that produced it; deriving it solely from `presentationId` at the later action-processing time would allow stale callbacks from an old instance to target a reopened Presentation. The Store verifies ID and token together. The token is not sent to the backend and is never persisted.

## 3. Supported backend loading configuration (FROZEN)

Only the following L-07 variant is approved for initial implementation; numeric/color values are illustrative apart from the approved `20` and `#FFFFFF` example:

```json
{
  "loading": {
    "whileActionPending": true,
    "disableWhilePending": true,
    "replaceContent": true,
    "indicator": {
      "type": "circular",
      "size": 20,
      "color": "#FFFFFF"
    }
  }
}
```

- A Button with **no** `loading` property keeps its prior rendering and action behavior.
- If `loading` is supplied, its shape must be an object containing explicit booleans for all three flags and an `indicator` object. `null`, wrong types, missing required fields or unsupported keys are invalid; do not silently ignore the whole configuration.
- For B4, all three flags must be exactly `true`. Every `false` combination is **unsupported** and must fail mapping deterministically, not silently coerce to `true`, render an unexplained spinner position, or pretend broader generic behavior is implemented.
- `indicator.type` must be `circular`; `size` is a positive finite number, and `color` must be a supported `#RRGGBB` or `#AARRGGBB` hexadecimal color. The value is backend-provided; the runtime must not substitute size/color defaults.
- The approved pending trigger is a Button `onClick` whose Action type is `request`. A declared B4 loading configuration on a Button without this action is unsupported; fail clearly rather than producing an inert or misleading config. Other action kinds, long-press loading, `replaceContent=false`, `disableWhilePending=false`, and `whileActionPending=false` need separate owner decisions before support.
- During pending, the circular indicator replaces text **and** leading/trailing Content, with existing Button dimensions, background, shape and layout otherwise preserved. No hardcoded loading label, alternative colors, new spacing tokens or fake artwork. All button interactions (including long click) are disabled while this origin is pending, and disabled semantics are exposed to accessibility.
- Do not introduce an artificial minimum spinner duration. A fast operation may complete before a visible spinner frame.

## 4. Lifecycle and action semantics (FROZEN)

1. UI passes the existing `DynamicAction` plus a **UI-only Button origin** for the approved `onClick` path; other Elements continue using the existing `onAction`. No payload/Action DTO mutation.
2. Capture and verify the full origin, including Presentation generation, **at acceptance**. A stale or unrecognized origin is rejected before starting work. `actionJob` remains the single-flight gate; repeated taps cannot enqueue a duplicate action.
3. Execute the existing request `validate` handling first. If validation is rejected, malformed, or fails, preserve the existing validation UX and do **not** enter pending.
4. For accepted, validated actions, mark **only that Button** pending immediately before invoking `DynamicActionExecutor.execute`. No other Button or scope acquires the spinner.
5. Use the current accepted bindings snapshot and existing request handling unchanged; pending is not a new request parameter and cannot change the atomic retained-state/effect commit semantics.
6. On successful completion, failed fetch, result rejection, exception, or cancellation, clear the matching origin **only if** that Store and scope are still current. A `finally` must not resurrect a disposed Screen or an old Presentation. Pending also clears when its originating scope closes.
7. Dismissing a Presentation during its originating Request invalidates/cancels the Presentation-origin action and suppresses its stale completion/effects. Reopening the same `presentationId` creates a new generation; no prior action can update it or commit an effect. Dismissing an unrelated Presentation does not cancel an independent Screen-origin action.
8. Closing the Store / navigating away cancels its job and prevents stale state/effect commits; pending never survives Store recreation, screen entry changes, or Presentation reopen.
9. Do not turn failed validation or other Action types into a loading event, and do not alter existing sanitized diagnostics by logging payloads, bindings, URLs or secrets.

## 5. Source boundaries for separately authorized B4 implementation

- `data/.../mapper/ElementMapper.kt`: strict present/invalid flags, indicator shape and color/size validation; ensure a supported Request-click binding.
- Current typed Screen and Presentation mapping/validation boundary: enforce scoped nonblank/unique Button IDs for actually supported hierarchy, with a narrow helper only if needed. No new public generic traversal abstraction.
- `feature/dynamic/.../store/DynamicScreenState.kt` and `DynamicStore.kt`: transient pending origin and Presentation-open generation, origin verification, validation-before-pending, cancellation and single-flight lifecycle.
- `DynamicContainer.kt`, `renderer/DynamicRenderer.kt`, `presentation/DynamicPresentationHost.kt`, and `runtime/DynamicRenderContext.kt`: forward Button origin and scope-specific pending access with minimal callback additions; retain existing generic action dispatch for all other renderers.
- `sdui/element/ButtonElement.kt`: render the declared indicator, replace accessories/text and suppress gestures while pending, preserving layout and accessibility.
- Domain models, `DynamicActionExecutor`, `RequestActionHandler`, `DynamicNavigationEntry`, networking, Theme and unrelated Element/Content renderers are **KEEP**. No B3/B3b or Phase 4 refactors.

## 6. Required verification / completion gates

- Mapper tests: absent loading; exact approved combination; all unsupported false variants; null/missing/wrong-type and extra fields; circular-only; invalid/nonfinite/nonpositive size; invalid colors; missing/non-Request onClick; Button ID duplicates in Screen Header/Body/Footer and nested Sections/Groups; independent Presentation scope.
- Store tests: validation success vs rejected; Request start ordering with `StandardTestDispatcher`; only originating Button pending; second tap dropped; Request success/failure/cancellation; failed effect handoff; navigation closure and Store close; no leakage across entries.
- Presentation tests: close during Request, cancel stale work, dismiss→reopen same ID, reject callback carrying previous generation, independent same-ID Button in Screen vs Presentation.
- UI checks: spinner uses backend-declared size/color, replaces all Button content, button disabled for click/long-click with accessibility semantics, button layout unchanged; explicit Android/Web smoke tests. iOS device behavior cannot be verified on Windows.
- `:data:jvmTest`, `:feature:dynamic:jvmTest`, `:app:jvmTest`, `:feature:dynamic:compileAndroidMain`, `:webApp:wasmJsBrowserDevelopmentWebpack`, and Git whitespace/diff scope checks.

## 7. Deferred / not authorized

**B4 implementation is NOT authorized by freezing this document.** Separate owner authorization is required to change runtime source. Multi-variant loading presentation (all `false` flag cases), repeated ListSection row origin, arbitrary action-kind loading, additional indicator styles, Theme/background image changes, backend migration and other B5/B6 tasks are out of scope.

**Precedence:** For L-07/B4 identity and supported loading flags, this document closes the explicit open gates in the reviewed frozen L-01–L-15 implementation plan without rewriting that plan. Other previously frozen contracts and scope limitations remain in force.
