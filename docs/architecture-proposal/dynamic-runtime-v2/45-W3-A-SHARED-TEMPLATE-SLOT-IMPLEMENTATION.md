# 45 — W3-A Shared Template Header/Footer Mapping

**Status:** W3-A Kotlin and focused tests committed, local JVM/Web validation pending. Branch `architecture/dynamic-runtime-v2-discussion-freeze`.

**Owner refinement:** Header and Footer are common to all template kinds, regardless of title, location, notification, action components, etc. Only currently existing `FormTemplate` is concretely updated; **no** hypothetical GridTemplate or StackTemplate classes. This refines doc 44 and follows frozen doc 20 §2. Body stays `template.components[]`, header/footer optional and independent. No seventh Dynamic category.

## Implementation
- `DynamicTemplate`: shared nullable `header` and `footer` contract.
- New small `TemplateSlot` Domain object with optional `DynamicPadding` and `List<DynamicComponent>`.
- `FormTemplate`: implements shared fields with default null at end of constructor to preserve positional callers.
- New `TemplateSlotDto`: optional `properties`, default empty `components`, no invented wrapper.
- `FormTemplateDto`: optional nullable `header/footer`.
- `TemplateMapper`: existing `ComponentMapper` reused for body and slots; same `DynamicValueMapper.padding`; no new mapper factory or renderer.
- `DynamicResponseMapperTest`: independent Body/Header/Footer padding and components, valid empty Footer, legacy no-slots screen, unsupported Header component fails.

## Scope and next steps
W3-A only parses Domain/DTO slots; **it does not yet render them**. W5 must introduce a single common `TemplateShell` placing Header → Body → Footer using the existing Component registry, respecting independent padding and Body scrolling as declared. Backend today need not emit the optional fields. W3-B separate: ImageElement URL required, malformed content array validation, no unrelated actions/navigation refactor. Backend migration doc 34 M01 remains target.

## Validation pending
```powershell
cd D:\CarBroz\CB-Partner
git status --short
git fetch origin --prune
git merge --ff-only origin/architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :domain:jvmTest :data:jvmTest :feature:dynamic:jvmTest :webApp:wasmJsBrowserDevelopmentWebpack
git diff --check
```
No claims of passing new tests until owner shares output. Android/iOS runtime remain deferred.
