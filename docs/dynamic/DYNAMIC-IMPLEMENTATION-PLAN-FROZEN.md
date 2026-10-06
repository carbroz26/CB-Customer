# CB-Partner Dynamic UI — Implementation Plan Frozen

**Status:** PLAN FROZEN  
**Date:** 2026-10-06  
**Branch:** `feature/dynamic-ui`

This is the single active implementation-plan contract for the current Dynamic implementation phase.

## 1. Governing architecture

Implementation follows `DYNAMIC-ARCHITECTURE-FROZEN.md`.

No implementation may introduce:

- a second Dynamic navigation framework;
- an empty template/instance abstraction;
- ExecuteDynamicActionUseCase;
- duplicate action executors;
- duplicate binding/state stores;
- business-specific screen classes;
- speculative manager/coordinator/router layers;
- reflection/package scanning.

## 2. Current implementation boundary

The implementation contains:

```
:data
  Dynamic DTOs
  Dynamic mappers
  repository/data-source boundary

:domain
  Dynamic contracts/models
  DynamicDestination
  DynamicResponse
  typed UI domain models

:feature:dynamic
  Dynamic Container
  Store/runtime
  action executor/handlers
  registries
  use cases
  renderer

:navigation
  application navigation boundary
```

## 3. Typed contract

DTOs decode the backend response. Mappers convert DTOs into concrete typed domain models.

Current concrete vocabulary:

```
FormTemplate
StackComponent
StackSection
StackGroup

TextElement
ImageElement
InputElement
ButtonElement
IconElement
DividerElement
```

The screen-level theme is a sibling of the template.

### ElementContent boundary

First-class `DynamicElement` remains the main UI hierarchy and keeps a required non-null `id`.

Nested/attached UI content is represented separately as `ElementContent`. Its `id` is optional because the current backend may omit ids from nested content.

Initial concrete content types required by the current backend:

```
IconContent
DividerContent
```

Existing `IconElement` and `DividerElement` remain first-class elements for direct `elements` arrays.

### Universal leading/trailing contract — FROZEN

`leadingContent` and `trailingContent` are capabilities of **every first-class DynamicElement**, not properties restricted to TextElement or ButtonElement.

Conceptually:

```
DynamicElement
  ├── leadingContent: List<ElementContent>
  └── trailingContent: List<ElementContent>
```

Therefore TextElement, ImageElement, InputElement, ButtonElement, IconElement, DividerElement, and future DynamicElement types may expose these properties.

The mapper must extract leading/trailing content through a common element-mapping path rather than implementing separate TextElement/ButtonElement-only handling.

Element renderers decide how their own leading/trailing content participates in layout. The contract does not mandate one universal visual layout.

## 4. ElementContent mapping boundary

Main and nested mapping remain separate:

```
DynamicElementMapper
  ↓
first-class DynamicElement

ElementContentMapper
  ↓
ElementContent
```

Nested content must not be mapped through the normal DynamicElement mapper because current backend nested objects such as Icon/Divider may not contain an element id.

Do not make `DynamicElement.id` nullable as a workaround.

Do not put nested content into the normal ElementRegistry.

## 5. Content registry / renderer boundary

The Dynamic registry gains a separate content renderer lookup:

```
DynamicRegistry
  ├── template(...)
  ├── component(...)
  ├── section(...)
  ├── group(...)
  ├── element(...)
  └── content(...)
```

First-class elements use ElementRegistry/ElementRenderer.

Nested ElementContent uses ContentRegistry/ElementContentRenderer.

ElementContent actions, where present, use the existing DynamicAction pipeline. No second action system is introduced.

## 6. DynamicRenderContext

`DynamicRenderContext` keeps separate rendering paths:

```
renderElements(List<DynamicElement>)
  ↓
ElementRegistry

renderContent(List<ElementContent>)
  ↓
ContentRegistry
```

Element renderers may call `renderContent` for their leading/trailing content.

## 7. Dynamic text-span value correction

Backend text spans may contain dynamic objects such as:

```json
{
  "text": {
    "$context": "authFlow.phoneNumber"
  }
}
```

The text-span model and mapper must preserve this typed dynamic value instead of coercing the object to an empty String.

Resolution continues through the existing generic DynamicRenderContext value-resolution mechanism.

No AuthFlow-specific model is introduced.

## 8. Destination lifecycle

```
DynamicDestination
  ↓
Dynamic Container
  ↓
TemplateRegistry(templateType)
  ↓
LoadDynamicScreenUseCase(endpoint)
  ↓
Dynamic API
  ↓
complete Dynamic response
  ↓
typed domain model
  ↓
DynamicRenderer
```

Login and OTP screen endpoints remain backend-provided dynamically. Only the Bootstrap/config entry endpoint is static.

## 9. Action lifecycle

```
interaction
  ↓
DynamicStore
  ↓
DynamicActionExecutor
  ↓
action-specific handler
  ↓
DynamicActionResult
  ↓
DynamicStore
```

Request actions resolve references/bindings before request construction.

Existing reference types remain authoritative:

- `$binding`
- `$context`
- `$response`
- `$literal`

The OTP verify contract using `$response:data.challengeId` remains unchanged.

No new action architecture is introduced for ElementContent.

## 10. Test scope

The implementation acceptance suite must cover:

### Bootstrap
- successful bootstrap mapping;
- startup destination mapping;
- maintenance/update/authenticated flags;
- HTTP/network/serialization failure classification;
- bootstrap headers and endpoint contract.

### Dynamic mapping
- screen/template/theme mapping;
- component/section/group ordering;
- all six known first-class element types;
- typed element properties/binding/actions;
- universal leading/trailing content mapping;
- IconContent and DividerContent mapping;
- optional nested content ids;
- separate ElementRegistry and ContentRegistry behavior;
- unsupported element/content rejection;
- response-level destination;
- arbitrary response payload preservation.

### Dynamic text
- literal span text;
- `$context` span text;
- OTP phone-number span resolution.

### Binding/reference
- `$binding`;
- `$context`;
- `$response`;
- `$literal`;
- missing/invalid references;
- multiple bindings.

### Actions
- request;
- navigate;
- response destination;
- present;
- dismiss;
- state;
- external_uri;
- sequence and recursive sequence execution;
- ElementContent action delegation through the existing action pipeline.

### Store/runtime
- loading lifecycle;
- binding updates;
- action orchestration;
- action result/effect application;
- retry/error paths.

### Backend-shaped flow
```
Bootstrap
  ↓
backend-provided Login destination
  ↓
Login screen
  ↓
mobileNumber binding
  ↓
send_otp request
  ↓
OTP destination
  ↓
OTP screen
  ↓
authFlow.phoneNumber / challengeId context or response reference
  ↓
otp binding
  ↓
verify_otp request
```

## 11. Fixture policy

Backend-shaped JSON fixtures must represent the current DTO contract exactly.

Reusable fixtures are preferred over giant duplicated inline JSON.

Screen theme belongs at `data.theme`, not inside `data.template`.

Response navigation destination belongs at `data.destination`, not inside `data.template`.

Nested leading/trailing objects are ElementContent-shaped backend objects and must not be forced to satisfy the first-class DynamicElement id contract.

Fixture changes must not weaken production contract tests.

## 12. Verification

Required verification remains the project's standard data, Dynamic, splash, and broader JVM gates.

The implementation must additionally verify the real backend-shaped Bootstrap → Login → Send OTP → OTP → Verify contract and the nested content/text-span corrections.

## 13. Acceptance

The Dynamic implementation is accepted only when:

- source matches frozen architecture;
- universal leading/trailing ElementContent contract is implemented;
- DTO fixtures match the current backend contract;
- data tests are green;
- feature/dynamic tests are green;
- backend-shaped Bootstrap → Login → Send OTP → OTP → Verify flow is covered and green;
- nested Icon/Divider content is not incorrectly decoded as first-class DynamicElement;
- dynamic text span objects are preserved and resolved correctly;
- no prohibited duplicate architecture exists;
- final documentation status is updated.

A test failure must be diagnosed from the contract/source before changing production code.

**This plan is now frozen. No implementation changes are included in this document.**
