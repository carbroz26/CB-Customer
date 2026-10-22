# 112 — Login + Country Picker Phase 1–3 Consolidated Freeze, Source Status and Audit Handoff

**Date:** 2026-10-21  
**Frontend repository:** `https://github.com/carbroz26/CB-Partner`  
**Frontend branch:** `architecture/dynamic-runtime-v2-discussion-freeze`  
**Verified source baseline for this record:** `49967f5dc62ddd06cc2e37b75f7a9d30b37aa280`  
**Backend repository:** `https://github.com/carbroz26/carwash-backend`  
**Backend branch:** `feature/backend-migration` — READ ONLY unless separately authorized.

## 1. Purpose and authority

This document consolidates the decisions, source changes, verification results and remaining gaps discussed and implemented during the 2026-10-21 Login + Country Picker work.

It is intentionally placed in the authoritative V2 folder:

`docs/architecture-proposal/dynamic-runtime-v2/`

Rules for reading this record:

1. Existing frozen V2 architecture remains authoritative unless a later explicit owner decision in this document is listed as a supersession.
2. Source proves implementation state; documentation defines the agreed contract.
3. A field being present in the target JSON does **not** mean its runtime behavior is already implemented.
4. Historical draft statements must not override later explicit owner decisions.
5. Current audit work is READ ONLY. No production cleanup or Phase 4 implementation is authorized merely by this record.
6. If source and this contract disagree, stop and reconcile before implementation.

## 2. Mandatory architecture and implementation rules

The runtime remains:

- Kotlin Multiplatform + Compose Multiplatform.
- Clean Architecture.
- Pure Store / MVI / UDF.
- No ViewModel.
- Strict SRP.
- No duplicate network, navigation, Store, mapper, registry or reference-resolution subsystem.
- No speculative Manager / Controller / Facade layer.
- No unnecessary wrapper whose only responsibility is forwarding.
- No new Gradle module for a helper.
- No Login-, OTP- or Country-Picker-specific production renderer/runtime class.
- No frontend business semantics that belong to backend/domain configuration.
- No hardcoded visual defaults where the backend contract owns the value.
- Explicit registries; no reflection.
- One logical navigation authority.
- One retained Dynamic session owner.
- Existing source is changed only after complete caller/DI/test inspection.

Required workflow:

```text
DISCUSS
→ RESEARCH
→ DECIDE
→ PLAN
→ FREEZE
→ IMPLEMENT
→ TEST
→ REVIEW
→ FREEZE
```

Implementation decision rule:

```text
Important ambiguity/risk → discuss
Source-contract conflict → stop + reconcile
Clear frozen requirement → implement directly
No real requirement → do not build it
```

Before modifying a class, inspect its complete file, callers, DI/registry wiring, tests and relevant V2 documents. A class is not unnecessary merely because it is small.

## 3. Six-category architecture

The six logical Dynamic UI categories remain:

```text
Template
Component
Section
Group
Element
ElementContent
```

Hierarchy:

```text
Screen
└── Template exactly 1
    └── Components 1..n
        ├── Elements 0..n
        └── Sections 0..n
            ├── Elements 0..n
            └── Groups 0..n
                └── Elements 1..n
                    └── leading/trailing ElementContent 0..n
```

There is no Component-inside-Component hierarchy and Presentation is not a seventh renderer category.

Current registry policy:

- one category-aware `DynamicRegistry`;
- six independent concrete-type maps;
- `KClass` based explicit registration;
- duplicate registration fails;
- no reflection;
- no six separate registry-manager services.

### 3.1 Current concrete types relevant to this work

**Template**
- `FormTemplate`

**Component**
- `StackComponent`

**Section**
- `StackSection`
- `ListSection` is typed/mapped from Phase 1 but does not yet have a production renderer.

**Group**
- `StackGroup`

**Element**
- `TextElement`
- `ImageElement`
- `InputElement`
- `ButtonElement`
- `IconElement`
- `DividerElement` exists in the current Phase-1 source and is used by the current Login target contract.

**ElementContent**
- `IconContent`
- `DividerContent`
- `ImageContent`

### 3.2 Element versus ElementContent — latest owner direction

Do **not** apply a blanket rule that every Element must have a matching Content type.

The actual requirement is role-based:

- An **Element** participates as a primary child in Component/Section/Group layout.
- **ElementContent** is an accessory owned by an Element, for example leading/trailing content.

The current Login/Country Picker requirement intentionally needs both primary and accessory forms for some visual concepts:

- Divider: `DividerElement` and `DividerContent`.
- Image: `ImageElement` and `ImageContent`.
- Icon: `IconElement` and `IconContent`.
- Text: `TextElement` exists. The owner has explicitly asked that Text-as-Content be reconciled when the design requires that role; **current source contains no `TextContent`**, so it must not be falsely reported as implemented. This is an audit/contract gap to resolve before adding it.

This latest requirement supersedes the older blanket statement that Divider can only ever be Content. However, source completeness still has to be audited: current registry does not yet prove a `DividerElement` renderer path.

## 4. Frozen Login + Country Picker contract

The current frozen screen identity is:

```text
screenId      = partner_login
templateId    = tpl_7K2M9Q
templateType  = form_template
schemaVersion = 3.0.0
targetApp     = PARTNER
```

Initial screen bindings:

```json
{
  "mobileNumber": "",
  "countryId": "IN",
  "countryCode": "+91",
  "countryFlagAsset": "flags/in"
}
```

Primary flow:

```text
Splash
→ bootstrap
→ Login
→ Send OTP Request
→ destination response
→ OTP screen
```

Country selection is a current-screen Presentation:

```text
Login Screen
→ Present(country_picker)
→ bottom_sheet
→ search/filter/list/select
→ selected values written to underlying screen bindings
→ Dismiss
→ Login remains same navigation entry
```

## 5. Template / Component / Section / Group properties frozen by the target

### FormTemplate

Used properties:

- `orientation`
- `fillMaxSize`
- `scrollable`
- `verticalArrangement`
- `padding`

`template.components[]` remains the Body. Header/Footer architecture remains the previously frozen additive shell contract; this Login target does not invent a `body` wrapper.

### StackComponent

Used properties include:

- `orientation`
- `fillMaxWidth`
- `horizontalAlignment`
- `padding`
- `background`
- `shape`

Login uses a white lower surface with per-corner rounded top corners.

### StackSection

Used properties include:

- `orientation`
- `fillMaxWidth`
- `padding`
- `verticalAlignment`
- `horizontalArrangement`

### StackGroup

Used properties include:

- `orientation`
- `fillMaxWidth`
- `height`
- `minHeight`
- `verticalAlignment`
- `horizontalArrangement`
- `padding`
- `background`
- `border`
- `shape`
- `focusPresentation`
- `validationPresentation`
- `selectedStyle`

Phase 3 implements focus/validation presentation behavior. List selected-state behavior remains Phase 4 work.

## 6. Element and Content property contract used by this screen

### TextElement

Target usage includes:

- literal `text`
- reference-backed text
- `fontSize`
- `fontWeight`
- `color`
- `textAlign`
- `lineHeight`
- `fillMaxWidth`
- `weight`
- `minHeight`
- `padding`
- `spacing`
- `maxLines`
- `overflow`
- `spans`
- leading/trailing Content
- click actions where declared

Phase 3 activated generic `minHeight`, `spacing`, `maxLines` and `overflow`.

### ImageElement / ImageContent

Typed source contract supports resource/asset source data in the model.

Target properties include:

- `source`
- `width`
- `height`
- `contentScale`
- `spacing` for Content
- element layout properties as applicable

Important implementation status: the current runtime image renderer still has URL-oriented behavior and complete typed resource-source rendering remains a separate asset-runtime gap. Do not claim bundled country flags already render correctly.

### IconElement / IconContent

Target properties include:

- `source`
- `size`
- `minWidth`
- `minHeight`
- `color`
- `spacing`
- `visibleWhen.selected`
- `reserveSpaceWhenHidden`

Phase 3 activated generic minimum sizing. Selected visibility remains Phase 4 work.

### DividerElement

Login uses a primary Divider Element between the country selector and mobile-number input:

```json
{
  "id": "phone_divider",
  "type": "divider",
  "properties": {
    "orientation": "vertical",
    "height": 28,
    "thickness": 1,
    "color": "#D1D5DB",
    "spacing": {
      "start": 8,
      "end": 12
    }
  }
}
```

The current Phase-1 source includes:

- `DividerElementDto`
- `DividerElement`
- `ElementMapper` branch for `"divider"`

But the current renderer-registration audit does not yet prove a registered `DividerElement` renderer. Treat this as a **source completeness audit item**, not permission to remove DividerElement.

### DividerContent

Divider Content remains valid accessory content in leading/trailing lists. It has its own Content role and does not replace the primary DividerElement requirement.

Target/content properties include:

- `orientation`
- `width`
- `height`
- `thickness`
- `color`
- `spacing`
- actions where meaningful/declared

### InputElement

Target properties include:

- `placeholder`
- `singleLine`
- `keyboardType`
- `inputFilter`
- `maxLength`
- `fontSize`
- `fontWeight`
- `textColor`
- `placeholderColor`
- `weight`
- `width/maxWidth/height`
- `fillMaxWidth`
- `padding`
- `background`
- `border`
- `focusBorder`
- `shape`
- `validationStyle`
- `binding`
- `validation`
- leading/trailing Content
- `onValueChange`, `onFocus`, `onSubmit` when authored

Phone input:

```text
binding      = mobileNumber
keyboardType = phone
inputFilter  = digits
maxLength    = 15
singleLine   = true
weight       = 1
```

### ButtonElement

Target properties include:

- `text`
- `height`
- `fillMaxWidth`
- `fontSize`
- `fontWeight`
- `textColor`
- `background`
- `shape`
- `loading`

The typed loading contract exists, but request-pending Button loading behavior is not part of the completed Phase 3 runtime and must not be reported as implemented.

## 7. Binding and reference contract

### 7.1 Implemented reference namespaces

The currently implemented generic resolver namespaces are:

```text
$binding
$context
$state
$literal
```

`$response` is forbidden.

Nested-path resolution remains typed and preserves String, Number, Boolean, Object, Array and Null values.

### 7.2 Screen binding ownership

Screen bindings belong to the current navigation entry.

Phase 3 precedence:

```text
screen.initialBindings + existing DynamicNavigationEntry.bindings
existing entry value wins
```

Therefore initial JSON values seed a fresh entry but do not overwrite an existing user-edited value on Store recreation/back-stack restoration.

### 7.3 Presentation binding ownership

Presentation `initialBindings` are fresh on every open.

For Country Picker:

```json
{
  "countrySearch": ""
}
```

Presentation-local values are discarded on dismiss. Underlying screen bindings survive.

### 7.4 Future target namespaces

The Country Picker target JSON contains:

- `$item` for row-scoped ListSection values.
- `$config` for app configuration such as legal URLs.

These are target/frozen screen requirements but are **not part of the completed Phase 3 resolver implementation**. They must be introduced only in their approved future phases. Do not silently treat them as already supported.

## 8. Validation and focus contract

Phone validation target:

```json
{
  "required": true,
  "validator": {
    "key": "phone_number",
    "arguments": {
      "region": {
        "$binding": "countryId"
      }
    }
  },
  "message": "Enter a valid mobile number"
}
```

Phase 3 added:

- `DynamicValidator`
- explicit `DynamicValidatorRegistry`
- duplicate-validator rejection
- unknown validator failure
- structured per-binding validation errors
- named validator arguments resolved using the existing generic reference resolver
- screen and presentation validation isolation
- revalidation after edits when errors already exist
- runtime focus state
- Group focus and validation presentation

Validation order:

```text
required
→ pattern
→ named validator
```

Blank optional value does not invoke the named validator.

No concrete `phone_number` business validator was added to Dynamic runtime. That remains a separate domain/provider decision.

Group border precedence:

```text
invalid
> focused
> normal
```

The validation message renders below the fixed-height input surface so the invalid message does not have to fit inside the 56dp field height.

## 9. Parent-owned layout rules implemented in Phase 3

Weight belongs to the parent layout, not to an Element's internal Compose control.

Current intended ownership:

- StackComponent applies child Element/Section weight.
- StackSection applies child Element weight.
- StackGroup applies child Element weight.

Other Phase-3 layout behavior:

- `FormTemplate.scrollable` scrolls the Body.
- Header/Footer remain outside Body scrolling/padding per the existing shell contract.
- Component background/shape is rendered.
- Rounded shape supports individual corners with `cornerRadius` fallback.
- Text minHeight/maxLines/overflow/spacing is rendered.
- Icon minWidth/minHeight is rendered.
- Input digit filter is applied before maxLength.
- Unknown inputFilter fails closed rather than silently behaving differently.

## 10. Action vocabulary and latest semantics

Current action vocabulary:

```text
request
navigate
present
dismiss
external_uri
state
sequence
```

### Sequence

Later V2 decisions supersede the old recursive-sequence design.

Current rule:

- Sequence remains registered.
- Sequence is inert.
- It does not recursively execute child actions.
- Do not resurrect the older executable Sequence behavior.

### Navigate

Navigate remains screen navigation only.

Presentation must never be implemented as a Navigate operation.

### Request

Request owns API execution. A successful response may return a destination and therefore produce navigation without invoking a Navigate handler as a child action.

Validation with `validate:true` blocks request execution when the relevant screen/presentation form is invalid.

## 11. PRESENT-ACTION-001 — FINAL FROZEN

`present` opens a non-navigation current-screen presentation.

Canonical action:

```json
{
  "type": "present",
  "payload": {
    "presentationId": "country_picker"
  }
}
```

Present must not contain:

- destination
- endpoint
- screenId
- templateId
- templateType
- authentication
- targetId

Presentation definition lives in the same screen document under:

```text
data.presentations[]
```

Initial supported types:

- `dialog`
- `bottom_sheet`

Presentation contains Components directly. It does not contain a Template.

Only one presentation may be active. Nested/second presentation is rejected.

Missing/invalid `presentationId` is a controlled action failure.

Duplicate presentation definition IDs are a deterministic mapping/validation failure.

Fresh presentation bindings are created on every open and discarded on close.

## 12. Dismiss — FINAL FROZEN

Canonical Dismiss:

```json
{
  "type": "dismiss"
}
```

Dismiss:

- closes only the active current-screen presentation;
- does not mutate the navigation stack;
- has no destination;
- has no targetId.

System back/outside-click must respect Presentation properties and must not pop the underlying navigation stack while the Presentation is active.

## 13. Phase 1 — typed screen contract

**Status: IMPLEMENTED + VERIFIED**

Freeze/status IDs:

```text
LOGIN-COUNTRY-PICKER-CONTRACT-001 = FINAL FROZEN
PRESENT-ACTION-001 = FINAL FROZEN
PRESENT-DISMISS-SOURCE-MIGRATION-001 = FINAL FROZEN
IMPLEMENTATION-PLAN-001 = FINAL FROZEN
PHASE-1-SOURCE-PLAN-001 = FINAL FROZEN
```

Commits:

- `da890bace4df14d7ace7ef08917b87d7d9ea1a6c` — `feat(dynamic): add phase 1 typed screen contract models`
- `1dae660b88bc232d64d65b16ad985ab6a8c1082d` — `fix(dynamic): project image content in diagnostics`

Phase 1 covered the typed transport/domain/mapping foundation required by the Login/Country Picker target, including:

- screen `initialBindings`;
- `presentations[]`;
- dialog/bottom-sheet Presentation models;
- ListSection typed model/source/filter/selection/separator structures;
- additional layout/style fields;
- validator spec;
- focus/validation/selected/loading presentation values;
- per-corner shape;
- typed asset source;
- `ImageContent`;
- `DividerElement` model/DTO/mapping;
- diagnostics projection update for the new typed content.

Phase 1 intentionally did **not** implement ListSection rendering, Store list selection, `$item`, `$config`, catalog data, asset-resource rendering or Button pending state.

## 14. Phase 2 — local Present/Dismiss runtime

**Status: IMPLEMENTED + VERIFIED + FINAL FROZEN**

Commit:

- `18760b85645faefc28bac8af90a31cfb60f515d5` — `refactor(dynamic): move presentations out of navigation`

Core result:

```text
Present / Dismiss
→ current Dynamic screen state
NOT
→ DynamicNavigationState
```

Implemented ownership:

- Present handler validates `payload.presentationId`.
- Dismiss validates empty local-dismiss semantics.
- Present/Dismiss were removed from app-level navigation effects.
- `DynamicScreenState` owns `activePresentationId` and presentation bindings.
- `DynamicStore` owns presentation open/close and binding scope.
- `DynamicPresentationHost` renders dialog/bottom-sheet.
- Navigation remains screen stack only.
- `ApplicationContent` handles only actual application effects such as Navigate and ExternalUri.
- stale presentation-origin callbacks are rejected.
- presentation binding resolution is local-first then screen.
- request inside a Presentation validates the Presentation tree.

## 15. Phase 3 — binding, validation and layout runtime

**Status: IMPLEMENTED + VERIFIED + FINAL FROZEN**

Main commits:

- `1683bbf0` — validation/layout runtime
- `51d7d2d0` — segmented dimensions
- `1b2ca12e` — placeholder styling
- `417e70a2` — retained screen validation during Presentation
- `f95e28ad` — text line limits
- `897d84dc` — typed-element validation
- `678ed1dc` — Compose collection rendering fix
- `dd1aa805` — validator-reference test fix
- `49967f5d` — common/Wasm validation-map update

Phase 3 implemented:

- initialBindings seeding and entry-over-initial precedence;
- structured validation errors;
- generic named-validator interface/registry;
- screen/presentation validation scope;
- focus state;
- Group focus/error presentation;
- invalid > focused > normal border priority;
- normal Input styling/filter/focus;
- parent-owned weight;
- Component surface;
- per-corner shapes;
- Text line/overflow/min-size/spacing;
- Icon minimum size;
- Form Body scrolling.

Verified by owner:

```text
:feature:dynamic:jvmTest                 PASS
:navigation:jvmTest                      PASS
:app:jvmTest                             PASS
:feature:dynamic:compileAndroidMain       PASS
git diff --check                         CLEAN
:webApp:wasmJsBrowserDevelopmentWebpack  PASS
```

Known non-blocking warnings remain environment/build warnings and are not permission for Gradle cleanup.

## 16. Phase 4 — ListSection runtime

**Status: NOT IMPLEMENTED. CURRENTLY PAUSED FOR ARCHITECTURE AUDIT.**

The latest candidate scope was:

```text
ListSection runtime
+ collection/catalog source
+ $item row context
+ filtering/search
+ stable item identity
+ selection/result mapping
+ selected row state
+ emptySectionId fallback
+ item separator
+ lazy scrolling
```

Do not implement Phase 4 until the Phase 1–3 architecture re-audit is reviewed and its remediation, if any, is closed.

### 16.1 Latest empty-state decision supersedes the older artifact

The generated frozen JSON snapshot in Appendix A still contains an embedded `emptyGroup`.

The later owner-approved Country Picker direction is:

```text
ListSection.properties.emptySectionId
→ references a sibling StackSection
→ referenced Section renders only as the empty fallback
→ it must NOT render again as an ordinary sibling
```

Therefore `emptyGroup` in the Appendix is a known superseded portion of that snapshot.

Do not silently implement the older `emptyGroup` behavior.

## 17. Country Picker future List contract

The target ListSection semantics include:

### Source

```json
{
  "type": "catalog",
  "key": "countries"
}
```

Catalog remains local/shared KMP reference data. Production country records are not yet implemented by this Phase 1–3 work.

### Filter

- query binding: `countrySearch`
- fields: `name`, `dialCode`
- case-insensitive contains when declared
- blank query restores source order
- no frontend sorting unless declared

### Row item scope

Future row values use `$item`:

```json
{"$item":"name"}
{"$item":"dialCode"}
{"$item":"flagAsset"}
```

`$item` is row-local and read-only.

### Selection

Target selection:

```text
mode            = single
itemKey         = id
selectedBinding = countryId
dismissOnSelect = true
```

Result mapping is explicit and atomic:

```text
countryId        ← item.id
countryCode      ← item.dialCode
countryFlagAsset ← item.flagAsset
```

No partial result write is allowed.

Selected state is derived from existing screen binding; no duplicate selection-state owner should be introduced.

### Selected row visuals

`StackGroup.selectedStyle` and `IconElement.visibleWhen.selected` / `reserveSpaceWhenHidden` are typed contract fields. Their runtime behavior remains Phase 4 work.

### Separator

`itemSeparator` belongs to ListSection rendering and appears only between visible rows.

## 18. Theme and asset target — B3b owner correction (22 October 2026)

**This correction supersedes only the background-image placement and source fields in the historical Login example below.** Theme is independent and retains its gradient styling; the image belongs to the **Template** properties, not `theme.properties`. No business screen name is interpreted by Dynamic Runtime.

The Template receives a backend-owned image URL and draws it behind the **entire Template surface**, including Header, Body and Footer, subject to the backend-specified Template bounds. The `source`, `contentScale`, and `alignment` properties are backend-controlled; an optional `fallbackUrl` may be provided for another trusted backend URL. The existing trusted API-origin asset boundary applies.

Illustrative contract only (the URL path is an example, **not** a confirmed backend endpoint):

```json
{
  "template": {
    "type": "form_template",
    "properties": {
      "fillMaxSize": true,
      "backgroundImage": {
        "source": { "type": "url", "value": "/assets/backgrounds/example.webp" },
        "contentScale": "crop",
        "alignment": "topCenter"
      }
    }
  },
  "theme": {
    "properties": {
      "gradient": {
        "angle": 135,
        "colors": [
          { "color": "#123456", "stop": 0 },
          { "color": "#654321", "stop": 1 }
        ]
      }
    }
  }
}
```

**Precedence:** The historical Appendix A still carries a `theme.properties.backgroundImage` resource-source candidate from an earlier discussion. That field is **obsolete**, not a valid current Template background-image payload. Future backend JSON must use `template.properties.backgroundImage` with URL source. The values in the illustrative JSON above do not prescribe a particular background image or gradient.

Resource identifiers used by the target include examples such as:

- `images/cb_partner_logo`
- `images/partner_login_hero`
- `icons/chevron_down`
- `icons/close`
- `icons/search`
- `icons/check_circle_blue`
- `flags/in`

Typed resource-source data is present in the target/domain contract, but complete runtime resource resolution is a separate asset-runtime task. Do not hardcode icon-name → native glyph mappings.

## 19. Legal/app configuration target

The target Login legal spans use `$config`:

```text
legal.termsUrl
legal.privacyUrl
```

This is an approved target concept from the screen design discussion but is not implemented by the Phase-3 resolver. It must be reconciled with the app-configuration contract before runtime implementation.

## 20. DynamicGestureModifier — current audit item

Current file:

`feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/element/DynamicGestureModifier.kt`

Historical Doc98 explicitly approved a small renderer-owned `dynamicGestures` Modifier for conditional click/long-click behavior. Therefore its existence is **not automatically an architecture defect**.

However the current audit must answer:

1. Is `sdui/element/` the correct package when ElementContent also imports it?
2. Is it truly Element-owned or shared renderer-interaction behavior?
3. Does combining gesture application and `reportDynamicOrigin` diagnostics give the file two reasons to change?
4. Is a package move or split a real ownership improvement or merely cosmetic churn?
5. Is there an existing appropriate package that avoids a new layer?
6. Would changing it violate the frozen W6-A behavior or create unnecessary abstraction?

No change is authorized until the full audit provides evidence.

## 21. Mandatory Phase 1–3 re-audit before new implementation

The owner has paused new feature work and requested a complete review of all changes from the pre-Phase-1 baseline through Phase 3.

Audit range:

```text
8fa1e86461666869ad4a562b215d9506a4a84501
...
49967f5dc62ddd06cc2e37b75f7a9d30b37aa280
```

The audit must review every changed production/test file and answer:

- Why does the class exist?
- Who calls it?
- Is responsibility singular?
- Is module/package ownership correct?
- Is there duplicate behavior?
- Is any abstraction unnecessary?
- Did a source change break the six-category architecture?
- Did product-specific behavior leak into generic Dynamic runtime?
- Did a frontend design default appear where backend should own it?
- Is DTO → Domain → Mapper → Registry → Renderer complete?
- Is anything mapped but not rendered?
- Is anything rendered but not mapped?
- Is KDoc missing/stale for non-obvious public/cross-module APIs?
- Did Store absorb a responsibility that belongs elsewhere?
- Is state retained without a proven lifecycle requirement?
- Do tests prove the behavior, or only compilation/mapping?
- Did implementation exactly match its frozen plan?

Known focus areas:

- `DynamicGestureModifier`
- `DividerElement` + registry/render path
- `ImageContent` rendering completeness
- requested Text Content role versus absent source type
- `DynamicLayout.kt` package/responsibility
- `DynamicRenderContext` responsibility growth
- `DynamicStore` responsibility growth
- `DynamicValidator` / registry necessity and DI
- Phase-1 fields that are mapped but intentionally deferred
- forbidden frontend visual defaults
- unused/dead imports, classes, registration and helpers introduced during these phases

First find **all** gaps. Do not fix them one-by-one while auditing.

After owner review, create one exact remediation plan, freeze it, then implement only those confirmed gaps in one cohesive wave.

## 22. Current status summary

```text
Phase 1
= IMPLEMENTED + VERIFIED

Phase 2
= IMPLEMENTED + VERIFIED + FINAL FROZEN

Phase 3
= IMPLEMENTED + VERIFIED + FINAL FROZEN

Phase 4
= REVIEW CANDIDATE / PAUSED
= NOT IMPLEMENTED

Current activity
= READ-ONLY Phase 1–3 architecture / SRP / source-completeness audit

Backend
= UNCHANGED / READ ONLY
```

## Appendix A — exact generated Login + Country Picker PRESENT frozen JSON snapshot

**Important:** This is the exact generated artifact used during this discussion. Its Present/Dismiss contract is current. Its embedded `emptyGroup` is superseded by the later `emptySectionId → sibling StackSection` decision described in §16.1. `$item`, `$config` and typed resource sources are target contract fields whose runtime support is not fully implemented by Phase 3.

```json
{
  "status": 200,
  "code": "SUCCESS",
  "message": "Partner login screen fetched successfully",
  "data": {
    "screenId": "partner_login",
    "schemaVersion": "3.0.0",
    "targetApp": "PARTNER",
    "initialBindings": {
      "mobileNumber": "",
      "countryId": "IN",
      "countryCode": "+91",
      "countryFlagAsset": "flags/in"
    },
    "template": {
      "id": "tpl_7K2M9Q",
      "type": "form_template",
      "properties": {
        "orientation": "vertical",
        "fillMaxSize": true,
        "scrollable": true,
        "verticalArrangement": {
          "type": "spaceBetween"
        },
        "padding": {
          "start": 0,
          "top": 0,
          "end": 0,
          "bottom": 0
        }
      },
      "components": [
        {
          "id": "login_branding",
          "type": "stack_component",
          "properties": {
            "orientation": "vertical",
            "fillMaxWidth": true,
            "horizontalAlignment": "center",
            "padding": {
              "start": 24,
              "top": 48,
              "end": 24,
              "bottom": 32
            }
          },
          "elements": [
            {
              "id": "brand_name",
              "type": "text",
              "properties": {
                "text": "CB-Partner",
                "fontSize": 20,
                "fontWeight": 700,
                "color": "#0A3A8D",
                "textAlign": "center",
                "leading": [
                  {
                    "type": "image",
                    "properties": {
                      "source": {
                        "type": "resource",
                        "value": "images/cb_partner_logo"
                      },
                      "width": 28,
                      "height": 28,
                      "contentScale": "fit",
                      "spacing": {
                        "end": 8
                      }
                    }
                  }
                ],
                "spacing": {
                  "bottom": 32
                }
              }
            },
            {
              "id": "welcome_title",
              "type": "text",
              "properties": {
                "text": "Welcome back",
                "fontSize": 32,
                "fontWeight": 700,
                "color": "#0A3A8D",
                "textAlign": "center",
                "spacing": {
                  "bottom": 8
                }
              }
            },
            {
              "id": "welcome_subtitle",
              "type": "text",
              "properties": {
                "text": "Access your partner account",
                "fontSize": 16,
                "fontWeight": 400,
                "color": "#6B7280",
                "textAlign": "center"
              }
            }
          ]
        },
        {
          "id": "login_form",
          "type": "stack_component",
          "properties": {
            "orientation": "vertical",
            "fillMaxWidth": true,
            "background": {
              "color": "#FFFFFF"
            },
            "shape": {
              "type": "roundedCorner",
              "topStart": 24,
              "topEnd": 24,
              "bottomStart": 0,
              "bottomEnd": 0
            },
            "padding": {
              "start": 24,
              "top": 24,
              "end": 24,
              "bottom": 24
            }
          },
          "sections": [
            {
              "id": "phone_label_section",
              "type": "stack_section",
              "properties": {
                "orientation": "vertical",
                "fillMaxWidth": true,
                "padding": {
                  "bottom": 8
                }
              },
              "elements": [
                {
                  "id": "phone_label",
                  "type": "text",
                  "properties": {
                    "text": "Mobile number",
                    "fontSize": 14,
                    "fontWeight": 600,
                    "color": "#18263B"
                  }
                }
              ]
            },
            {
              "id": "phone_field_section",
              "type": "stack_section",
              "properties": {
                "orientation": "vertical",
                "fillMaxWidth": true,
                "padding": {
                  "bottom": 24
                }
              },
              "groups": [
                {
                  "id": "phone_input_row",
                  "type": "stack_group",
                  "properties": {
                    "orientation": "horizontal",
                    "fillMaxWidth": true,
                    "height": 56,
                    "verticalAlignment": "center",
                    "background": {
                      "color": "#FFFFFF"
                    },
                    "border": {
                      "width": 1,
                      "color": "#CBD5E1"
                    },
                    "shape": {
                      "type": "roundedCorner",
                      "cornerRadius": 12
                    },
                    "focusPresentation": {
                      "binding": "mobileNumber",
                      "border": {
                        "width": 1,
                        "color": "#0A3A8D"
                      }
                    },
                    "validationPresentation": {
                      "binding": "mobileNumber",
                      "errorBorder": {
                        "width": 1,
                        "color": "#DC2626"
                      },
                      "messageStyle": {
                        "fontSize": 12,
                        "fontWeight": 400,
                        "color": "#DC2626",
                        "spacing": {
                          "top": 6
                        }
                      }
                    }
                  },
                  "elements": [
                    {
                      "id": "country_selector",
                      "type": "text",
                      "properties": {
                        "text": {
                          "$binding": "countryCode"
                        },
                        "fontSize": 15,
                        "fontWeight": 600,
                        "color": "#18263B",
                        "minHeight": 44,
                        "padding": {
                          "start": 12,
                          "end": 8
                        },
                        "leading": [
                          {
                            "type": "image",
                            "properties": {
                              "source": {
                                "type": "resource",
                                "value": {
                                  "$binding": "countryFlagAsset"
                                }
                              },
                              "width": 24,
                              "height": 18,
                              "contentScale": "fit",
                              "spacing": {
                                "end": 8
                              }
                            }
                          }
                        ],
                        "trailing": [
                          {
                            "type": "icon",
                            "properties": {
                              "source": {
                                "type": "resource",
                                "value": "icons/chevron_down"
                              },
                              "size": 16,
                              "spacing": {
                                "start": 4
                              }
                            }
                          }
                        ]
                      },
                      "actions": {
                        "onClick": {
                          "type": "present",
                          "payload": {
                            "presentationId": "country_picker"
                          }
                        }
                      }
                    },
                    {
                      "id": "phone_divider",
                      "type": "divider",
                      "properties": {
                        "orientation": "vertical",
                        "height": 28,
                        "thickness": 1,
                        "color": "#D1D5DB",
                        "spacing": {
                          "start": 8,
                          "end": 12
                        }
                      }
                    },
                    {
                      "id": "mobile_number",
                      "type": "input",
                      "properties": {
                        "placeholder": "Enter mobile number",
                        "singleLine": true,
                        "keyboardType": "phone",
                        "inputFilter": "digits",
                        "maxLength": 15,
                        "fontSize": 16,
                        "fontWeight": 400,
                        "textColor": "#111827",
                        "placeholderColor": "#94A3B8",
                        "weight": 1,
                        "padding": {
                          "start": 0,
                          "top": 0,
                          "end": 12,
                          "bottom": 0
                        },
                        "validationStyle": {
                          "background": {
                            "color": "#FFF1F2"
                          }
                        },
                        "height": 56
                      },
                      "validation": {
                        "required": true,
                        "validator": {
                          "key": "phone_number",
                          "arguments": {
                            "region": {
                              "$binding": "countryId"
                            }
                          }
                        },
                        "message": "Enter a valid mobile number"
                      },
                      "binding": {
                        "key": "mobileNumber"
                      }
                    }
                  ]
                }
              ]
            },
            {
              "id": "continue_action_section",
              "type": "stack_section",
              "properties": {
                "orientation": "vertical",
                "fillMaxWidth": true,
                "padding": {
                  "bottom": 12
                }
              },
              "elements": [
                {
                  "id": "continue_button",
                  "type": "button",
                  "properties": {
                    "text": "Continue",
                    "height": 56,
                    "fillMaxWidth": true,
                    "fontSize": 18,
                    "fontWeight": 600,
                    "textColor": "#FFFFFF",
                    "background": {
                      "color": "#0A3A8D"
                    },
                    "shape": {
                      "type": "roundedCorner",
                      "cornerRadius": 16
                    },
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
                  },
                  "actions": {
                    "onClick": {
                      "type": "request",
                      "payload": {
                        "method": "POST",
                        "endpoint": "/api/v1/auth/send_otp",
                        "authentication": "NONE",
                        "validate": true,
                        "body": {
                          "countryCode": {
                            "$binding": "countryCode"
                          },
                          "phoneNumber": {
                            "$binding": "mobileNumber"
                          },
                          "deviceId": {
                            "$context": "deviceId"
                          }
                        },
                        "responseMode": "destination",
                        "mode": "push",
                        "retain": {
                          "challengeId": "data.challengeId",
                          "phoneNumber": "data.phoneNumber"
                        }
                      }
                    }
                  }
                }
              ]
            },
            {
              "id": "legal_section",
              "type": "stack_section",
              "properties": {
                "orientation": "vertical",
                "fillMaxWidth": true
              },
              "elements": [
                {
                  "id": "legal_text",
                  "type": "text",
                  "properties": {
                    "fontSize": 13,
                    "fontWeight": 400,
                    "lineHeight": 19,
                    "color": "#6B7280",
                    "textAlign": "center",
                    "fillMaxWidth": true,
                    "spans": [
                      {
                        "text": "By continuing, you agree to our "
                      },
                      {
                        "text": "Terms & Conditions",
                        "color": "#0A3A8D",
                        "underline": true,
                        "onClick": {
                          "type": "external_uri",
                          "payload": {
                            "uri": {
                              "$config": "legal.termsUrl"
                            }
                          }
                        }
                      },
                      {
                        "text": " and "
                      },
                      {
                        "text": "Privacy Policy",
                        "color": "#0A3A8D",
                        "underline": true,
                        "onClick": {
                          "type": "external_uri",
                          "payload": {
                            "uri": {
                              "$config": "legal.privacyUrl"
                            }
                          }
                        }
                      }
                    ]
                  }
                }
              ]
            }
          ]
        }
      ]
    },
    "presentations": [
      {
        "id": "country_picker",
        "type": "bottom_sheet",
        "properties": {
          "dismissOnOutsideClick": true,
          "dismissOnBack": true,
          "showHandle": true,
          "handle": {
            "width": 36,
            "height": 4,
            "color": "#D1D5DB",
            "cornerRadius": 2,
            "alignment": "center",
            "spacing": {
              "top": 8,
              "bottom": 12
            }
          }
        },
        "initialBindings": {
          "countrySearch": ""
        },
        "components": [
          {
            "id": "picker_content",
            "type": "stack_component",
            "properties": {
              "orientation": "vertical",
              "fillMaxWidth": true,
              "background": {
                "color": "#FFFFFF"
              },
              "shape": {
                "type": "roundedCorner",
                "topStart": 24,
                "topEnd": 24,
                "bottomStart": 0,
                "bottomEnd": 0
              }
            },
            "sections": [
              {
                "id": "picker_header",
                "type": "stack_section",
                "properties": {
                  "orientation": "horizontal",
                  "fillMaxWidth": true,
                  "verticalAlignment": "center",
                  "horizontalArrangement": {
                    "type": "spaceBetween"
                  },
                  "padding": {
                    "start": 24,
                    "top": 12,
                    "end": 16,
                    "bottom": 16
                  }
                },
                "elements": [
                  {
                    "id": "picker_title",
                    "type": "text",
                    "properties": {
                      "text": "Select country code",
                      "fontSize": 20,
                      "fontWeight": 700,
                      "color": "#0A3A8D",
                      "maxLines": 1,
                      "overflow": "ellipsis"
                    }
                  },
                  {
                    "id": "picker_close",
                    "type": "icon",
                    "properties": {
                      "source": {
                        "type": "resource",
                        "value": "icons/close"
                      },
                      "size": 20,
                      "minHeight": 44,
                      "minWidth": 44
                    },
                    "actions": {
                      "onClick": {
                        "type": "dismiss"
                      }
                    }
                  }
                ]
              },
              {
                "id": "picker_search",
                "type": "stack_section",
                "properties": {
                  "orientation": "vertical",
                  "fillMaxWidth": true,
                  "padding": {
                    "start": 16,
                    "top": 8,
                    "end": 16,
                    "bottom": 8
                  }
                },
                "elements": [
                  {
                    "id": "country_search",
                    "type": "input",
                    "properties": {
                      "placeholder": "Search country or code...",
                      "singleLine": true,
                      "keyboardType": "text",
                      "fontSize": 14,
                      "fontWeight": 400,
                      "textColor": "#111827",
                      "placeholderColor": "#94A3B8",
                      "height": 44,
                      "fillMaxWidth": true,
                      "border": {
                        "width": 1,
                        "color": "#DCE6F3"
                      },
                      "focusBorder": {
                        "width": 1,
                        "color": "#0A3A8D"
                      },
                      "shape": {
                        "type": "roundedCorner",
                        "cornerRadius": 10
                      },
                      "padding": {
                        "start": 12,
                        "top": 0,
                        "end": 12,
                        "bottom": 0
                      },
                      "leading": [
                        {
                          "type": "icon",
                          "properties": {
                            "source": {
                              "type": "resource",
                              "value": "icons/search"
                            },
                            "size": 18,
                            "spacing": {
                              "end": 8
                            }
                          }
                        }
                      ]
                    },
                    "binding": {
                      "key": "countrySearch"
                    }
                  }
                ]
              },
              {
                "id": "country_list",
                "type": "list_section",
                "properties": {
                  "fillMaxWidth": true,
                  "source": {
                    "type": "catalog",
                    "key": "countries"
                  },
                  "filter": {
                    "binding": "countrySearch",
                    "fields": [
                      "name",
                      "dialCode"
                    ],
                    "caseInsensitive": true
                  },
                  "selection": {
                    "mode": "single",
                    "itemKey": "id",
                    "selectedBinding": "countryId",
                    "result": {
                      "countryId": "id",
                      "countryCode": "dialCode",
                      "countryFlagAsset": "flagAsset"
                    },
                    "dismissOnSelect": true
                  },
                  "itemSeparator": {
                    "type": "divider",
                    "orientation": "horizontal",
                    "thickness": 1,
                    "color": "#EDF1F5",
                    "spacing": {
                      "start": 16,
                      "end": 16
                    }
                  },
                  "weight": 1
                },
                "groups": [
                  {
                    "id": "country_row",
                    "type": "stack_group",
                    "properties": {
                      "orientation": "horizontal",
                      "fillMaxWidth": true,
                      "verticalAlignment": "center",
                      "minHeight": 44,
                      "padding": {
                        "start": 16,
                        "top": 8,
                        "end": 16,
                        "bottom": 8
                      },
                      "background": {
                        "color": "#FFFFFF"
                      },
                      "selectedStyle": {
                        "background": {
                          "color": "#DCEBFC"
                        }
                      },
                      "horizontalArrangement": {
                        "type": "spacedBy",
                        "spacing": 12
                      }
                    },
                    "elements": [
                      {
                        "id": "country_flag",
                        "type": "image",
                        "properties": {
                          "source": {
                            "type": "resource",
                            "value": {
                              "$item": "flagAsset"
                            }
                          },
                          "width": 24,
                          "height": 18,
                          "contentScale": "fit"
                        }
                      },
                      {
                        "id": "country_name",
                        "type": "text",
                        "properties": {
                          "text": {
                            "$item": "name"
                          },
                          "fontSize": 14,
                          "fontWeight": 500,
                          "color": "#18263B",
                          "weight": 1,
                          "maxLines": 1,
                          "overflow": "ellipsis"
                        }
                      },
                      {
                        "id": "country_dial_code",
                        "type": "text",
                        "properties": {
                          "text": {
                            "$item": "dialCode"
                          },
                          "fontSize": 13,
                          "fontWeight": 400,
                          "color": "#64748B",
                          "maxLines": 1,
                          "overflow": "ellipsis"
                        }
                      },
                      {
                        "id": "selected_indicator",
                        "type": "icon",
                        "properties": {
                          "source": {
                            "type": "resource",
                            "value": "icons/check_circle_blue"
                          },
                          "size": 18,
                          "visibleWhen": {
                            "selected": true
                          },
                          "reserveSpaceWhenHidden": true
                        }
                      }
                    ]
                  }
                ],
                "emptyGroup": {
                  "id": "empty_content",
                  "type": "stack_group",
                  "properties": {
                    "orientation": "vertical",
                    "fillMaxWidth": true,
                    "horizontalAlignment": "center",
                    "padding": {
                      "start": 20,
                      "top": 32,
                      "end": 20,
                      "bottom": 32
                    },
                    "verticalArrangement": {
                      "type": "spacedBy",
                      "spacing": 8
                    }
                  },
                  "elements": [
                    {
                      "id": "empty_icon",
                      "type": "icon",
                      "properties": {
                        "source": {
                          "type": "resource",
                          "value": "icons/search_empty"
                        },
                        "size": 40
                      }
                    },
                    {
                      "id": "empty_title",
                      "type": "text",
                      "properties": {
                        "text": "No countries found",
                        "fontSize": 16,
                        "fontWeight": 600,
                        "color": "#111827",
                        "textAlign": "center"
                      }
                    },
                    {
                      "id": "empty_description",
                      "type": "text",
                      "properties": {
                        "text": "Try searching with a different name or dial code.",
                        "fontSize": 13,
                        "fontWeight": 400,
                        "color": "#64748B",
                        "textAlign": "center",
                        "maxLines": 3,
                        "overflow": "ellipsis"
                      }
                    }
                  ]
                }
              }
            ]
          }
        ]
      }
    ],
    "theme": {
      "theme": "light",
      "statusBar": "transparent",
      "properties": {
        "backgroundImage": {
          "source": {
            "type": "resource",
            "value": "images/partner_login_hero"
          },
          "contentScale": "crop",
          "alignment": "topCenter"
        }
      }
    }
  }
}
```

## 23. B5 / L-10 — Typed empty-section reference (2026-10-22)

B5 follows the owner-frozen [Document 115](115-B5-L10-EMPTY-SECTION-REFERENCE-PLAN-FROZEN.md) and reconciled [Document 114](114-B5-L10-EMPTY-SECTION-REFERENCE-SOURCE-AUDIT-AND-EXACT-PLAN-DRAFT.md). The canonical target JSON, [partner-login-country-picker-current-target.json](partner-login-country-picker-current-target.json), was migrated from the exact user-provided 21 October `partner_login_country_picker_PRESENT_FINAL_FROZEN(1).json` by **only** replacing embedded `emptyGroup` with `ListSection.properties.emptySectionId` and moving the original Group unchanged into a sibling `StackSection`.

The historical Appendix A remains an audit snapshot, not a current mapping contract; its `emptyGroup` is obsolete. B5 implements DTO/domain/mapping/reference validation only. The ListSection renderer, row filtering/selection and conditional fallback visibility remain **Phase 4 pending**. The canonical JSON is a target contract, **not** a claim that the ListSection renders today. Existing unrelated Theme background-image values remain governed by the later B3b correction; B5 does not change them.
