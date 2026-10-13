# Dynamic UI Architecture

Status: BASE CONTRACT FROZEN; CONTENT-OWNED SPACING IMPLEMENTED — JVM/ANDROID/WEB BUILD AND TEST GATES PASSED; FINAL SOURCE/DIFF AND VISUAL REVIEW OPEN — 2026-10-12
Scope: Dynamic UI runtime, rendering, actions, references, screen lifecycle, navigation integration.
Authority: This document is the single Dynamic architecture authority.

## 1. Purpose
Dynamic UI is a generic server-driven UI runtime. The frontend renders a server-defined document and executes generic actions. The runtime never knows business meaning such as authentication, booking, OTP, or login.

## 2. Non-negotiable boundaries
- Kotlin Multiplatform + Compose Multiplatform.
- Clean Architecture + MVI/UDF + Pure MVI Store. No ViewModel.
- `:navigation` owns the navigation stack and back-stack entries.
- `:network` owns HTTP infrastructure and transport metadata.
- `:data` owns repositories, remote data sources and DTO mapping.
- `:domain` owns Dynamic contracts/models and use cases.
- `:feature:dynamic` owns Dynamic rendering, runtime resolution, action execution and Dynamic MVI state.
- No Dynamic-specific navigation/network/data/domain duplicate layers.
- No external screen cache.
- No reflection and no universal registry.

## 3. Runtime model
Dynamic Runtime has three conceptual scopes:

Context — read-only application/environment values. Current runtime metadata includes platform, appVersion, buildNumber and deviceId. Context is not business-flow state.

RuntimeState — generic retained DynamicValue data explicitly written by an action. It is not disk persistence and has no frontend-defined business namespaces. Keys/paths are server-defined.

NavigationState — the existing `:navigation` back stack. Each stack entry is an independent Dynamic screen entry.

A screen entry contains:
- DynamicDestination identity
- a NavEntry-scoped DynamicStore
- loaded ScreenDocument
- BindingState
- ValidationState
- loading/error state

The navigation module owns only the entry identity and stack. The mutable Dynamic screen state is scoped to the corresponding Navigation 3 entry content in :feature:dynamic; it is not stored in a separate Dynamic cache.

Returning to an existing entry reveals that entry. Its screen is not reloaded and its binding/validation state remains. Pushing the same screen twice creates two independent entries.

## 4. References
The only Dynamic reference namespaces are:
- `$binding` — mutable values of the current screen entry.
- `$context` — read-only application/environment values.
- `$state` — generic retained RuntimeState.
- `$literal` — explicit literal/recursive value.

Removed:
- `$response`
- `PreviousResponse`
- `previousResponseData`
- business namespaces such as `auth.*`, `login.*`, `otp.*`, `booking.*`.

Action responses are transient execution results. If response data must survive an action or screen transition, the action explicitly retains selected response paths into generic RuntimeState.

All reference resolution preserves DynamicValue type. Objects and arrays never become strings.

## 5. Screen document hierarchy
The standard hierarchy is:
Template
→ Component
→ Section
→ Group
→ Element

Element content is a separate renderable capability:
Element → Content.

First-class types currently include:
- FormTemplate
- StackComponent
- StackSection
- StackGroup
- TextElement
- ImageElement
- InputElement
- ButtonElement
- IconElement
- IconContent
- DividerContent

There is no first-class DividerElement.

## 6. Registries and rendering
The currently frozen implementation uses registries separated by responsibility:
- TemplateRegistry
- ComponentRegistry
- SectionRegistry
- GroupRegistry
- ElementRegistry
- ContentRegistry

The current implementation may expose these through one DynamicRegistry facade, but registration and lookup remain type-specific. Duplicate registration fails deterministically.

DynamicRenderer is generic:
1. validate destination identity against loaded document;
2. resolve the template type through TemplateRegistry;
3. render the registered template;
4. template/component/section/group renderers delegate through their corresponding registry;
5. element content uses its own registry.

DynamicRenderer must not hardcode `form_template`.
Renderers do not perform network calls, navigation, persistence or business logic.

## 7. Compose rendering context
DynamicRenderContext supplies only rendering capabilities:
- registry lookup
- reference resolution
- typed property helpers
- UI action callback
- typed binding-change callback

It must not own screen state, execute actions, perform requests or navigate.

## 8. MVI Store
DynamicStore is the Pure MVI Store boundary. It:
- accepts Dynamic intents;
- loads a screen through LoadDynamicScreenUseCase;
- owns per-entry BindingState and ValidationState;
- owns shared generic RuntimeState;
- supplies read-only Context;
- validates when requested;
- delegates actions to DynamicActionExecutor;
- applies action results to MVI state/effects.

It does not implement navigation algorithms, HTTP, repository logic or renderer logic.

## 9. Actions
Flow:
UI event → DynamicAction → DynamicActionExecutor → one DynamicActionHandler → DynamicActionResult → DynamicStore.

DynamicActionExecutor is the only action dispatch boundary.
Each handler owns exactly one capability:
- request
- navigate
- present
- dismiss
- external_uri
- state

No business-specific action handlers.

Sequence is supported by the executor with a finite execution-depth guard. Duplicate action handler types are rejected instead of silently overwritten.

## 10. Runtime state retention
State retention is explicit and generic.

Conceptual request:
```json
{
  "type": "request",
  "payload": {
    "endpoint": "/api/example",
    "responseMode": "destination",
    "retain": {
      "challengeId": "data.challengeId"
    }
  }
}
```

The frontend does not know what `challengeId` means. The server chooses the key. The selected response value is copied into RuntimeState.

A later screen can read:
```json
{"$state":"challengeId"}
```

Nested state is supported:
```json
{"$state":"session.reference.id"}
```

## 11. Complete reference examples
Current screen input:
```json
{"value":{"$binding":"phone"}}
```

Environment:
```json
{"value":{"$context":"deviceId"}}
```

Retained state:
```json
{"value":{"$state":"challengeId"}}
```

Literal:
```json
{"value":{"$literal":"fixed-value"}}
```

Typed state can be object/array/number/boolean/string:
```json
{
  "retain": {
    "session": "data.session",
    "count": "data.count",
    "enabled": "data.enabled"
  }
}
```

## 12. Bootstrap and screen loading
Bootstrap supplies the initial DynamicDestination. The destination identity is:
- screenId
- templateId
- templateType
- endpoint

Destination identity is not polluted with request metadata or back behavior.

Loading uses the existing LoadDynamicScreenUseCase → domain repository → data/network path.

## 13. Navigation and back
Normal back is a navigation pop. The previous stack entry is revealed with its existing state; no reload occurs.

Backend-defined back is a navigation instruction, not part of DynamicDestination identity. The semantic forms are:
```json
{"navigation":{"back":{"type":"pop"}}}
```
or
```json
{
  "navigation": {
    "back": {
      "type": "destination",
      "destination": {
        "screenId": "target",
        "templateId": "tpl_target",
        "templateType": "form_template",
        "endpoint": "/api/target"
      }
    }
  }
}
```
The exact backend schema may be finalized with the backend after frontend semantics are implemented.

## 14. Generic action/navigation examples
```json
{
  "type": "navigate",
  "destination": {
    "screenId": "next",
    "templateId": "tpl_next",
    "templateType": "form_template",
    "endpoint": "/api/next"
  }
}
```

```json
{
  "type": "request",
  "payload": {
    "endpoint": "/api/submit",
    "method": "POST",
    "body": {
      "phone": {"$binding":"phone"},
      "deviceId": {"$context":"deviceId"},
      "reference": {"$state":"referenceId"}
    },
    "responseMode": "destination",
    "retain": {
      "referenceId": "data.referenceId"
    }
  }
}
```

## 15. Complete screen example
```json
{
  "screenId": "generic_form",
  "schemaVersion": "1.0",
  "targetApp": "PARTNER",
  "template": {
    "id": "tpl_generic",
    "type": "form_template",
    "properties": {},
    "components": [
      {
        "id": "main",
        "type": "stack_component",
        "properties": {},
        "sections": [
          {
            "id": "fields",
            "type": "stack_section",
            "properties": {},
            "groups": [
              {
                "id": "form",
                "type": "stack_group",
                "properties": {},
                "elements": [
                  {
                    "id": "phone",
                    "type": "input",
                    "properties": {
                      "binding": "phone"
                    }
                  },
                  {
                    "id": "submit",
                    "type": "button",
                    "properties": {
                      "text": "Continue"
                    },
                    "actions": {
                      "onClick": {
                        "type": "request",
                        "payload": {
                          "endpoint": "/api/example",
                          "method": "POST",
                          "body": {
                            "value": {"$binding":"phone"},
                            "deviceId": {"$context":"deviceId"}
                          },
                          "responseMode": "destination",
                          "retain": {
                            "referenceId": "data.referenceId"
                          }
                        }
                      }
                    }
                  }
                ]
              }
            ]
          }
        ]
      }
    ]
  }
}
```

## 16. Error/security rules
- Unknown template/component/section/group/element/content types fail or render the defined safe fallback; they never trigger reflection.
- Malformed Dynamic values fail at mapping/resolution boundaries.
- Sensitive request values must not be logged by Dynamic or network logging.
- Dynamic does not access platform storage/APIs directly.
- RuntimeState is in-memory only unless a separate future persistence contract is explicitly approved.
- Request/authentication behavior remains in repository/network boundaries.

## 17. Testing contract
Tests must cover:
- typed binding values;
- typed context values;
- typed RuntimeState;
- nested object/array state;
- state retention from action response;
- binding → request;
- context → request;
- state → request;
- state → rendering;
- state lifecycle across navigation;
- independent state for repeated screen entries;
- normal back without reload;
- generic navigation/back;
- registry duplicate rejection;
- registry-driven template dispatch;
- action handler duplicate rejection;
- sequence depth safety;
- malformed/unknown Dynamic values;
- complete generic JSON mapping.

## 18. Explicit non-goals
- No business-specific auth/OTP/booking state model.
- No external Dynamic screen cache.
- No Dynamic navigation stack.
- No Dynamic network client.
- No Dynamic data layer.
- No ExecuteDynamicActionUseCase.
- No ViewModel.
- No reflection-based component discovery.
- No universal registry.


## 19. Backend-owned Dynamic asset contract

Dynamic visual assets are backend-owned URL resources.

For image and icon elements/content, the backend supplies:
- `url` — required primary asset URL;
- `fallbackUrl` — optional backend-owned fallback asset URL.

The frontend must not interpret asset names, map icon names to glyphs, draw substitute icons, or contain asset URL constants.

Loading behavior is:
1. load `url` when it is present and non-blank;
2. if primary loading fails and `fallbackUrl` is present and non-blank, load `fallbackUrl`;
3. if neither URL is usable or both requests fail, render the appropriate frontend-bundled fallback resource.

The backend contract remains unchanged: backend responses should supply `url` and may supply `fallbackUrl`. The frontend mapper tolerates a missing URL by passing an empty value to the renderer, so a malformed/missing asset URL does not crash the screen. Bundled fallbacks are presentation-only and do not introduce icon-name mapping or backend asset constants.

Asset presentation properties remain backend-controlled. The existing Dynamic hierarchy and renderer/registry boundaries are unchanged.


## 20. Generic text alignment, accessory-owned padding and container padding

- A text element's textAlign value center maps to Compose TextAlign.Center; unknown or absent values remain unspecified.
- When fillMaxWidth is true, the text itself occupies the available width in its parent row while leaving room for leading/trailing content. Text alignment is meaningful only when the text has available width.
- Text accessories use one shared nullable padding: DynamicPadding contract on the existing ElementContent domain contract. DynamicPadding represents the four inset values start, top, end, and bottom; preserve supplied values through DTO mapping and apply them to the accessory's existing root layout modifier.
- Both IconContent and DividerContent use the shared content-owned padding contract. Do not create another padding abstraction, accessory renderer hierarchy, or screen-specific behavior.
- Remove the obsolete parent text-level spacing fields from the frontend contract, mapper, model, renderer, fixtures and tests. They are superseded by accessory-owned padding and must not remain as competing spacing mechanisms.
- Missing accessory padding means no additional accessory-owned padding; do not invent frontend spacing defaults. Parent text properties must not be used to simulate accessory padding.
- DynamicLayout remains responsible for existing node-owned layout modifiers. Accessory content renderers apply their own content padding using the existing padding helper where compatible; do not move content-row spacing responsibility into the generic node-layout helper.
- Group padding is internal to the group's painted background/border. The background and border enclose the configured padding; padding must not be placed outside the painted container.
- Backend-owned spacing/padding values are data, not frontend design defaults. Generic Login/OTP responses must render through the same accessory contract without authentication-specific conditions.



## 21. Frozen content-owned spacing contract — 2026-10-11

**Revision:** DYNAMIC-CONTRACT-REVISION-CONTENT-SPACING-001  
**State:** PLAN FROZEN; IMPLEMENTATION AUTHORIZED 2026-10-12; IMPLEMENTATION IN PROGRESS  
**Supersedes:** the accessory-owned `ElementContent.padding: DynamicPadding?` contract and the text-level `contentSpacing` / `trailingContentSpacing` approach.

- The existing `ElementContent` contract exposes one nullable `spacing: DynamicSpacing?` property, shared by every current content implementation, including `IconContent` and `DividerContent`.
- `DynamicSpacing` is a four-sided value with `start`, `top`, `end`, and `bottom` values. These represent external layout space around the content item, not padding inside the drawn divider/icon and not character letter spacing.
- Backend content JSON supplies `properties.spacing`. It must no longer supply `properties.padding` for leading/trailing content.
- Remove text-parent `contentSpacing` and `trailingContentSpacing` as competing mechanisms. Accessory spacing belongs to the accessory whose gap it controls.
- Keep `DynamicPadding` for ordinary node/container padding where that contract remains applicable; do not globally rename it.
- Frontend mapping preserves all four spacing values. Existing content renderers apply them as outer layout spacing around the content's rendered bounds. The divider itself draws its line without encoding gaps into text or changing line semantics.
- Missing spacing means zero additional external gap. No arbitrary frontend defaults, text whitespace, Login/OTP special cases, new registry, renderer hierarchy, or architecture layer.
- Preserve text `letterSpacing`, text alignment/fill behavior, existing content registration, and the generic Dynamic runtime architecture.
- The owner authorized implementation on 2026-10-12. The plan remains frozen; source changes must remain within this contract.


## 22. Generic element renderer registry consolidation — frozen amendment

**Decision reference:** `DYNAMIC-REGISTRY-ELEMENT-GENERIC-001`  
**Historical state:** this model-class/generic-adapter design was frozen as a candidate on 2026-10-13, then superseded by section 23 after source review and owner approval of the simpler concrete-renderer design.  
**Date:** 2026-10-13

This narrow amendment supersedes earlier clauses requiring five element-specific renderer interfaces, maps/lookups/builder methods, or concrete element class-to-key dispatch in `DynamicRenderContext`. All other frozen Dynamic architecture remains active.

### Frozen contract

- Keep Template, Component, Section, Group, Element, and Content registry responsibilities separate. Do not introduce a universal cross-category registry.
- Add the common domain `DynamicElement` contract to `TextElement`, `ImageElement`, `InputElement`, `ButtonElement`, and `IconElement`; it has no Compose dependency and adds no backend-specific type property.
- Replace the five element-specific renderer contracts with `ElementRenderer<T : DynamicElement>`. Each concrete renderer remains strongly typed to its own model.
- The explicit builder API is `.element(type, modelClass, renderer)`. The normalized backend key is validated and retained for duplicate-key rejection; the explicit `KClass<T>` is the renderer lookup identity.
- `DynamicRegistry` has one element-renderer map keyed by `KClass<out DynamicElement>`. A normalized-key set may be retained solely for blank/duplicate registration validation; it is not a second renderer map.
- `DynamicRenderContext` dispatches by the element model's class identity and must not contain a growing class-to-key `when`.
- One internal adapter captures the registered `KClass<T>`, defensively checks `modelClass.isInstance(element)`, and contains the only unchecked cast. No concrete renderer casts its input. No reflection-based discovery, `Map<String, Any>`, central generic mapper, or unrelated architecture change is permitted.
- Keep `:data` responsible for mapping backend type keys into concrete domain models. Do not retain backend type identifiers in domain models solely to support rendering. Preserve existing unknown-backend-key mapper behavior; an unregistered model class fails deterministically at renderer lookup, and adapter type mismatch produces a useful deterministic diagnostic.
- Change only the component, section, and group element collections from `List<Any>` to `List<DynamicElement>`. Keep the concrete DTO-to-model mapping flow. Leave `ElementContentRenderer` and the content registry unchanged.
- Preserve existing rendering, action, binding, validation, layout/property ownership, and backend contract behavior. Normalize keys; reject blank and duplicate keys without replacing an existing registration when rejection is caught. All supported construction paths must enforce the same invariants.

### Compatibility and implementation gate

Kotlin common API exposes `KClass.isInstance`; the project-specific Gradle/Compose compilation has not yet been run. Before source edits, compile the adapter in the configured common source set and audit every affected collection, mapper, registry construction path, fixture, and test. If the toolchain or actual source constraints reject this design, stop and report; do not substitute a different dispatch algorithm without a new owner decision.

This amendment freezes the model-class dispatch rule and planned scope, not implementation completion. Source code remains unchanged until the implementation gate is explicitly opened.


## 23. Element registry simplification — frozen amendment (2026-10-13)

**Decision:** `DYNAMIC-REGISTRY-ELEMENT-SIMPLIFICATION-001`  
**State:** OPTION A APPROVED; REVISED PLAN FROZEN; SIMPLIFICATION IMPLEMENTATION NOT STARTED  
**Supersedes:** the active implementation direction in section 22, including generic `ElementRenderer<T>`, model-class-keyed registry, normalized-key side set, and `CheckedElementRenderer`.

### Decision

The Element registry must follow the existing straightforward, string-keyed registration and lookup pattern used by Template, Component, Section, Group, and Content. Do not defend or extend the recently implemented generic registry merely because it exists.

### Frozen implementation contract

- Keep the five concrete element domain models and their existing backend-type mapping in `:data`.
- Keep `DynamicElement` only as the common marker needed for typed heterogeneous element collections; it must not drive generic renderer adaptation or contain backend type metadata.
- Replace generic `ElementRenderer<T : DynamicElement>` with five concrete renderer contracts, one for each model: Text, Image, Input, Button, and Icon. Each renderer accepts its own concrete domain model.
- Remove `CheckedElementRenderer`, `KClass`-based element registration/lookup, the unchecked cast, and the separate `elementTypes` validation set.
- Give each concrete renderer category its own string-keyed map and typed lookup/registration method, following the same pattern as the other registry categories. Registration keys use the existing `trim().lowercase()` rule and shared blank/duplicate rejection helper; the normalized key is the lookup key, not bookkeeping-only metadata.
- Register each element explicitly using its backend key and matching concrete renderer. Do not use a generic `.element(type, modelClass, renderer)` API.
- `DynamicRenderContext` uses a small exhaustive dispatch from the concrete domain model to its known backend key and corresponding typed registry lookup. This is accepted for Element because it preserves concrete renderer types without adapters, casts, reflection, or `Map<String, Any>`.
- Keep Template, Component, Section, Group, Element, and Content responsibilities separate. Do not introduce a universal registry or generic registry framework.
- Preserve rendering, actions, binding, validation, layout/property ownership, content rendering, and unknown-backend-key behavior in `:data`.
- Do not change `ElementMapperSupport` as part of this registry amendment. It is data-mapping code, not registry infrastructure; any decision to remove or reshape it requires a separate focused mapper review and must avoid duplicating shared action/content/value mapping.
- Do not add unrelated cleanup or change the backend contract.

### Why this direction

The common generic adapter, model-class registry key, and extra key-validation set were additional machinery not shared by the established registry categories. Concrete typed renderer paths make the runtime type contract explicit and remove the unchecked-cast boundary. A small concrete-model-to-backend-key dispatch is preferred over generic type-erasure infrastructure.

### Completion criteria

- No `CheckedElementRenderer`, `KClass` element registry key, unchecked element renderer cast, or `elementTypes` side set remains.
- Each of the five element types has a concrete renderer contract, normalized string-keyed registration/lookup, and one explicit registration.
- The only element model dispatch is a local exhaustive mapping from the five domain model classes to the existing backend keys; concrete renderer inputs remain typed and require no casts.
- The content registry and `ElementContentRenderer` are unchanged.
- Focused registry and mapper/rendering regression tests pass; the complete diff contains no unrelated changes.


## 24. Dynamic registry and action-effect amendment — plan frozen (2026-10-13)

**Decision:** `DYNAMIC-REGISTRY-ACTION-EFFECT-AMENDMENT-001`  
**State:** COMPLETE PLAN FROZEN; DOCUMENTATION RECONCILIATION IN PROGRESS; SOURCE IMPLEMENTATION NOT STARTED  
**Branch:** `feature/dynamic-ui`  
**Supersedes:** Section 23's Option A registry direction and conflicting earlier registry clauses only where this section explicitly defines a different Element/Content dispatch contract. This section also freezes the distinct action-effect and presentation contract below.

### 24.1 Typed registration-driven Element and Content dispatch

- Reuse the existing domain contracts `DynamicElement` and `ElementContent`. Do not add `DynamicTemplate`, `DynamicComponent`, `DynamicSection`, or `DynamicGroup` interfaces, and do not create a new hierarchy solely for registry dispatch.
- Keep separate registries for Template, Component, Section, Group, Element, and Content. No universal registry, reflection-based discovery, `Map<String, Any>`, backend type fields on domain models, or duplicated mapper/resolver.
- Element renderers are generic typed contracts `ElementRenderer<T : DynamicElement>`; Content renderers are generic typed contracts `ElementContentRenderer<T : ElementContent>`. Each concrete renderer receives its concrete model type and must not cast or silently ignore a mismatched model.
- Element and Content lookup is registration-driven by concrete model-class identity. Each category owns a separate map and typed registration/lookup API. Template/Component/Section/Group remain on their existing string-keyed registries.
- A narrow internal adapter at each heterogeneous registry boundary may perform the one checked model-type validation and unavoidable cast needed to bridge erased generic types. Keep that boundary local, documented, deterministic, and covered by tests. Do not spread casts into renderers or create a general-purpose registry framework.
- Explicitly register Text, Image, Input, Button, and Icon element renderers, and IconContent and DividerContent renderers. The backend type-to-domain-model interpretation remains in `:data`; `DynamicRenderContext` must not contain concrete-model-to-backend-key `when` dispatch for Element or Content.
- Keep `ElementContentMapper` responsible for mapping `icon` and `divider` to concrete content models. Keep `DynamicReferenceResolver` as the single reference traversal boundary.
- Preserve blank/duplicate registration rejection where string-keyed categories use it; model-class-keyed Element/Content registrations must reject duplicate model-class registrations deterministically. Unknown models fail with a useful deterministic diagnostic. No reflection or fallback glyph/asset is introduced.

### 24.2 Action sequence result and ordered effects

- Change the action result contract so one execution result can carry both accumulated `stateChanges: Map<String, DynamicValue>` and an ordered `effects: List<DynamicActionEffect>`.
- Represent navigation, presentation, dismissal, and external URI as explicit effect types. Completed/state-only actions produce no effect but do not erase effects emitted earlier in the same sequence.
- Nested sequences preserve effect order and merge state changes deterministically. State changes from each step are applied before resolving/executing the next step. A genuine handler failure or invalid sequence structure fails the sequence and stops later actions; a terminal effect alone is not a failure or a reason to stop.
- Preserve the existing finite sequence-depth guard, normalized handler names, duplicate-handler rejection, `back` alias to `dismiss`, unknown-action failure, existing repository request boundary, typed DynamicValue behavior, and explicit response retention.
- The Store applies accumulated state changes and forwards all effects in order. Effect delivery must not use a lossy send path, and the UI consumer must not use cancellation semantics that drop earlier effects when a later effect arrives. Keep the queue bounded where practical and use a suspending/reliable delivery contract with deterministic close/failure behavior.
- Handlers return instructions only; they do not mutate navigation state, launch external URIs, or directly present UI.

### 24.3 Distinct modal presentation semantics

- `present` means a modal presentation, not a normal navigation push.
- Keep one active presentation at a time. A second presentation request while one is active is rejected deterministically and does not replace or leak the existing presentation.
- The underlying navigation entry and its entry-scoped Store/bindings/validation remain alive and unchanged while the modal is visible.
- The presented destination receives a new `DynamicNavigationEntry` and the normal entry-scoped Store lifecycle. Reuse existing entry identity and Dynamic Container composition; do not add a destination cache or a second navigation stack.
- Dismissing a presentation removes only the active presentation. Back handling dismisses the active presentation first; when no presentation is active, back uses the existing navigation-stack behavior.
- Normal `navigate` continues to use the existing Push/Replace/Pop semantics and never silently becomes a presentation.
- The navigation module owns presentation state and stack behavior. The Dynamic feature forwards typed effects to the navigation boundary; action handlers and DynamicStore do not mutate navigation state.

### 24.4 Audit and verification contract

Audit source and contract alignment across: registry/hierarchy/renderers; DTO-to-domain mapping and backend type keys; action handlers/executor/results; state propagation and reference resolution; Store effect transport and UI consumption; navigation stack, entry identity and presentation lifecycle; architecture boundaries and duplicate infrastructure; fixtures/tests; documentation consistency. Preserve backend-owned assets/visual properties, content-owned `DynamicSpacing`, generic DynamicValue semantics, the four supported reference namespaces, no `$response`, no ViewModel, no Dynamic-specific HTTP client/navigation cache, and no business-specific Login/OTP models.

The exact file-by-file execution plan, tests, and stop conditions are frozen in `DYNAMIC-IMPLEMENTATION-PLAN.md` section 22.30. The source implementation is not started by this documentation amendment. If source paths or contracts differ, stop and reconcile before changing source.
