# Dynamic UI Architecture

Status: FROZEN — 2026-10-07
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
Registries are separated by responsibility:
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
1. load `url`;
2. if primary loading fails and `fallbackUrl` exists, load `fallbackUrl`;
3. if both fail, render no asset.

No frontend-bundled default asset is part of this contract.

Asset presentation properties remain backend-controlled. The existing Dynamic hierarchy and renderer/registry boundaries are unchanged.
