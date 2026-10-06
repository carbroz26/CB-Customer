# DYNAMIC UI — Typed Contract Implementation Plan Freeze

**Date:** 2026-10-06  
**Branch:** `feature/dynamic-ui`  
**Status:** PLAN FROZEN  
**Scope:** Typed SDUI UI-contract migration

## 1. Frozen Objective

Migrate the Dynamic UI pipeline from generic UI nodes using `type + properties: Map<String, DynamicValue>` to concrete, reusable typed domain UI models.

Target pipeline:

`Backend JSON -> DTO -> DynamicResponseMapper -> typed domain UI model -> DynamicRegistry -> typed renderer -> Compose`

This is a contract migration, not a redesign of the Dynamic Store/action/navigation architecture.

## 2. Frozen UI Types

Exactly these reusable UI types are in scope:

1. `FormTemplate`
2. `StackComponent`
3. `StackSection`
4. `StackGroup`
5. `TextElement`
6. `ImageElement`
7. `InputElement`
8. `ButtonElement`
9. `IconElement`
10. `DividerElement`

No screen-specific UI classes such as `LoginTemplate`, `LoginInput`, or `LoginButton`.

## 3. Frozen Typed Fields

### FormTemplate
`id`, `orientation`, `verticalArrangement`, `horizontalAlignment`, `padding`, `fillMaxSize`, `semanticRole`, `components`

### StackComponent
`id`, `orientation`, `verticalArrangement`, `horizontalArrangement`, `horizontalAlignment`, `verticalAlignment`, `padding`, `fillMaxWidth`, `elements`, `sections`

### StackSection
`id`, `orientation`, `verticalArrangement`, `horizontalAlignment`, `padding`, `fillMaxWidth`, `elements`, `groups`

### StackGroup
`id`, `orientation`, `verticalArrangement`, `horizontalArrangement`, `horizontalAlignment`, `verticalAlignment`, `padding`, `height`, `fillMaxWidth`, `background`, `border`, `shape`, `elements`

### TextElement
`id`, `semanticRole`, `text`, `spans`, `fontSize`, `fontWeight`, `letterSpacing`, `lineHeight`, `color`, `textAlign`, `fillMaxWidth`, `leading`, `trailing`, `actions`

### ImageElement
`id`, `semanticRole`, `url`, `width`, `height`, `maxWidth`, `fillMaxWidth`, `contentScale`

### InputElement
`id`, `semanticRole`, `placeholder`, `keyboardType`, `maxLength`, `weight`, `validation`, `binding`, `actions`

### ButtonElement
`id`, `semanticRole`, `text`, `height`, `fillMaxWidth`, `fontSize`, `fontWeight`, `textColor`, `background`, `shape`, `leading`, `trailing`, `actions`

### IconElement
`id`, `name`, `size`, `color`

### DividerElement
`id`, `orientation`, `width`, `height`, `thickness`, `color`

## 4. Frozen Shared Value Models

Small typed value objects are allowed and preferred over generic property bags:

- `DynamicPadding`
- `DynamicArrangement`
- `DynamicBackground`
- `DynamicGradientStop`
- `DynamicBorder`
- `DynamicShape`
- `DynamicTextSpan`

No generic `*Properties` bags.

Existing `DynamicValidation` and `DynamicBinding` are reused.

## 5. DynamicValue Boundary

`DynamicValue` remains in the raw/dynamic boundary.

It may continue to represent arbitrary action payloads, request bodies, `$binding`, `$context`, `$response`, future schema extensions, and unknown dynamic values.

Known UI properties must be represented by typed fields. Renderers must not interpret generic `DynamicValue` property maps for known UI fields.

## 6. Frozen File/Class Work

### Domain
Primary existing file:
- `domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/DynamicModels.kt`

Replace the generic UI-node contract with the frozen typed models. Supporting typed value models may be split into focused files if that keeps the implementation clean.

### Data DTO
Existing:
- `data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/model/DynamicResponseDto.kt`

Keep backend DTOs flexible enough to decode the current response shape. Do not prematurely force backend JSON into renderer/domain sealed hierarchies.

### Mapping
Existing:
- `data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/mapper/DynamicResponseMapper.kt`

Make this the authoritative DTO -> typed domain mapping boundary. Known UI types map to their concrete typed models. Unknown/unsupported UI types use the existing Dynamic error/result convention; do not introduce unrelated architecture.

### Renderer Contracts
Existing:
- `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/registry/DynamicRendererContracts.kt`
- `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/registry/DynamicRegistry.kt`

Keep registry lookup/selection architecture. Registry resolves a renderer; renderer receives a typed domain model. Registry must not become a property parser.

### Existing Renderers
Migrate the existing renderer files to consume typed models:

- `.../sdui/template/FormTemplateRenderer.kt`
- `.../sdui/template/StackTemplate.kt`
- `.../sdui/component/StackComponent.kt`
- `.../sdui/section/StackSection.kt`
- `.../sdui/group/StackGroup.kt`
- `.../sdui/element/TextElement.kt`
- `.../sdui/element/ImageElement.kt`
- `.../sdui/element/InputElement.kt`
- `.../sdui/element/ButtonElement.kt`
- `.../sdui/element/DecorationElements.kt`

The existing renderer hierarchy is reused; this is not a renderer rewrite.

### DynamicRenderer
Existing:
- `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/renderer/DynamicRenderer.kt`

Make only the changes required to consume typed template/domain models while preserving screen validation and existing screen-level behavior.

### Store/Container
Existing:
- `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/store/DynamicStore.kt`
- `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/DynamicContainer.kt`

No architectural change. Modify only if compilation/integration requires it.

## 7. Frozen Login Composition

The current Login response must map into reusable types, approximately:

```
FormTemplate
├── StackComponent: brand_content
│   ├── ImageElement: brand_logo
│   ├── TextElement: brand_name
│   ├── TextElement: partner_label
│   │   ├── DividerElement
│   │   └── DividerElement
│   ├── TextElement: brand_tagline
│   ├── TextElement: welcome_title
│   └── TextElement: welcome_subtitle
├── StackComponent: login_content
│   ├── StackSection: mobile_field_section
│   │   └── StackGroup: mobile_field
│   │       ├── TextElement: country_code
│   │       │   └── DividerElement
│   │       └── InputElement: mobile_number
│   └── StackSection: action_section
│       ├── ButtonElement: continue_button
│       │   └── IconElement
│       └── TextElement: legal_text
│           └── spans/actions
└── StackComponent: hero_content
    └── ImageElement: hero_car
```

The exact backend JSON remains the source for actual values; the hierarchy above defines the typed-domain target.

## 8. Actions and Runtime Architecture

No changes to the frozen action architecture.

Preserve:

`User interaction -> DynamicStore -> DynamicActionExecutor -> DynamicActionHandler -> DynamicActionResult -> DynamicStore`

Do not recreate `ExecuteDynamicActionUseCase`, `ActionManager`, `ActionCoordinator`, `PreparedAction`, a second binding store, or a separate navigation executor.

## 9. Frozen Tests

Update/add tests for:

1. DTO -> typed domain mapping.
2. All 10 typed UI types.
3. Login response typed hierarchy.
4. Typed fields for known UI properties.
5. Unknown/unsupported UI type handling.
6. Renderer registry resolution.
7. Existing request/binding behavior.
8. Existing action executor sequence recursion.
9. Existing DynamicStore behavior.

Existing action/store tests remain part of the verification set.

## 10. Frozen Implementation Order

1. Introduce typed shared value models.
2. Introduce typed template/component/section/group models.
3. Introduce typed element models.
4. Migrate DTO -> domain mapping.
5. Migrate renderer contracts.
6. Migrate template/component/section/group renderers.
7. Migrate element renderers.
8. Reconcile registry and `DynamicRenderer`.
9. Verify Login JSON compatibility.
10. Add/update mapping, registry, and contract tests.
11. Remove obsolete generic UI-model usage only after replacement is proven.
12. Run Gradle verification and perform final reconciliation.

Migration is incremental: introduce -> map -> render -> test -> remove obsolete usage. Do not delete the generic model first.

## 11. Explicitly Out of Scope

Do not change unless compilation strictly requires it:

- DynamicStore architecture
- action vocabulary/executor architecture
- navigation architecture
- external URI boundary
- network transport
- bootstrap architecture
- authentication architecture
- backend contracts
- Koin/DI architecture
- unrelated warning cleanup
- unrelated modules/files

## 12. Freeze Gate

**Typed UI Contract:** FROZEN  
**Implementation Plan:** FROZEN  
**Code Changes from this plan:** NONE at freeze time  
**Next gate:** Implementation authorization

No further architecture discussion is required for this plan unless implementation reveals a concrete contract contradiction or compilation-level incompatibility.
