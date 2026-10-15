# 15 — Pass 5: Renderers, Template Slots, Theme, Assets and Platforms

**Status:** SOURCE AUDIT AND V2 TARGET IMPLEMENTATION PLANNING DRAFT; NOT PLAN FROZEN. Source files were read from the architecture branch. No Kotlin source edited; no Gradle/device/simulator tests run.
**Authority:** frozen V2 target and canonical existing Login/OTP JSON, document 12. Never revise structural categories or fixed JSON just to accommodate legacy source. Add Header/Footer through an approved additive Template extension; legacy template.properties.padding applies to Body only.

## Audited paths

- `feature/dynamic/sdui/template/FormTemplate.kt`
- `feature/dynamic/sdui/{component/StackComponent,section/StackSection,group/StackGroup}.kt`
- `feature/dynamic/sdui/element/{TextElement,InputElement,ButtonElement,ImageElement,DecorationElements}.kt`
- `feature/dynamic/sdui/content/{ElementContent,DynamicAssetImage}.kt`
- `feature/dynamic/runtime/DynamicLayout.kt`
- `feature/dynamic/registry/{DynamicRendererContracts,DynamicRendererRegistration,DynamicRegistry}.kt`
- `feature/dynamic/renderer/DynamicRenderer.kt`, `feature/dynamic/runtime/DynamicRenderContext.kt`
- `feature/dynamic/DynamicContainer.kt`, `navigation/DynamicNavigationHost.kt`
- `app/App.kt`, `app/src/iosMain/.../Main.kt`, Android MainActivity, Web Main.kt, `feature/dynamic/build.gradle.kts`.

### Source observations / required V2 behavior

| ID | Verified source observation | Target-first V2 requirement |
|---|---|---|
| V01 | FormTemplate chooses Row/Column by orientation, applies template.padding to entire layout, traverses template.components. | Preserve orientation and multi-components. Introduce TemplateShell with optional independent Header/Footer and Body, and route existing template padding to Body only, not Header/Footer. |
| V02 | Component renderer always renders all direct elements then all sections; Section renderer all direct elements then all groups. | Fixed JSON fields remain; freeze/document deterministic ordering when both child arrays populated; do not pretend JSON interleaving is supported. |
| V03 | Group owns only elements. | Preserve this leaf-of-layout ownership. Element may own nested leading/trailing content. |
| V04 | All Stack renderers duplicate near-identical Row/Column branches. | Small shared Stack layout modifier/composable if justified by actual reuse, without an unnecessary universal renderer. Each category keeps independent children and visual ownership. |
| V05 | Text renderer composes spans, style, clickable links, leading/trailing; uses Color.Unspecified and TextAlign.Unspecified when absent. | Preserve text and span actions, use one action callback and typed expression resolver; honor disabledColor/enabled present in OTP JSON; no hardcoded OTP. |
| V06 | Input renderer returns without UI if binding absent; maps phone/number/text keyboard, emits typed binding and onValueChange; segmented mode has backend dimensions but always Number keyboard and ignores some event types. | Validate binding in mapping or document optional mode; support backend-defined keyboard consistently, typed validation/errors/focus/submit semantics, accessibility and user-defined dimensions. |
| V07 | Button uses clickable Box, respects height, width, text/background/shape, content. | Preserve backed visuals but add enabled/loading/semantics and action presentation only when declared; avoid visible no-op clicks and hidden default styles. |
| V08 | Image renderer uses primary URL/fallback URL **and ships painterResource(dynamic_image_fallback)**. IconElement and IconContent likewise use built-in fallback painter. | Previous owner policy is backend URL then backend fallbackUrl then **no asset**; remove injected native fallback images unless owner changes this design. No frontend glyph registry. |
| V09 | Content Image/Divider are distinct renderer types; content spacing applied separately. Divider renderer returns silently on missing color/dimensions. | Preserve nested IconContent/DividerContent and content spacing; reject malformed required parameters at mapper/validation rather than silently disappearing. |
| V10 | DynamicAssetImage owns Coil AsyncImage state and onError advances url→fallbackUrl→built-in painter. | One shared asset display primitive may remain; validate relative/absolute URI policy and failure semantics; never invent local visual fallback. |
| V11 | DynamicLayout.kt collects layout overloads for Template, Component, Section, Group and Elements, plus spacing/padding, shape/color/gradient helpers. | Split purely by responsibility: focused LayoutModifiers and VisualConverters; no universal Utils.kt, no per-type duplication, no package-level one-file-per-property proliferation. |
| V12 | Current DynamicBackground.LinearGradient conversion uses Brush.linearGradient(colors) and drops backend `angle` and `stop` positions. DynamicRenderer.screenTheme uses angle and draws with drawWithCache, but also ignores stops. | Typed gradient with angle and stop positions (owner JSON includes stop=0,0.28,0.55,1). Built-in Modifier.background(brush) preferred over manual drawWithCache; accuracy tested for gradient coordinate convention and stop distribution. |
| V13 | DynamicRenderer returns without error if screen or expected screen/template IDs/types mismatch. | Fail load explicitly; never show empty screen for invalid/mismatched backend ScreenDocument. |
| V14 | DynamicRenderContext combines resolution helpers and child registry dispatch, but does not fetch/mutate navigation. | Prefer an immutable context with narrow render callbacks; change only if concrete SRP violation, no speculative extra context. |
| V15 | Renderer registry already registers FormTemplate, StackComponent, StackSection, StackGroup, Text/Input/Button/Image/Icon Elements and Icon/Divider Content by KClass. | Keep six category registration capability, exact parity for all concrete classes; new Grid/List types register without modifying existing renderers; avoid duplicate registries. |
| V16 | DynamicNavigationHost uses Navigation3 NavDisplay and a separate Compose Dialog for present; onBack calls state.pop. | Preserve actual independent modal presentation and platform back priority, integrate future Header/Footer layout without modal inset issues. |
| V17 | App starts Koin inside remembered Composable; Android MainActivity injects BuildConfig API URL, Web entry reads meta override or :3000 origin, iOS shared Main reads Info.plist API URL or localhost. | Preserve platform environment injection, but make DI/session lifetime explicit. Verify production HTTPS and browser deployment and iOS simulator/device base URL. |
| V18 | Feature Dynamic depends on Compose, Coil Ktor3 image networking, Navigation module and Domain. | No new Gradle module required; verify KMP-compatible adapters, optional permissions and engine support; no separate Dynamic HttpClient for API. |

## Final V2 presentation ownership and planned source paths

| V2 path (logical) | Single responsibility | Sources to replace/reuse |
|---|---|---|
| `feature/dynamic/renderer/ScreenRenderer.kt` | Render validated ScreenDocument/theme through category registry | DynamicRenderer.kt |
| `feature/dynamic/renderer/template/FormTemplateRenderer.kt` | Form layout, exactly one Template/response with multiple components and optional slots | sdui/template/FormTemplate.kt |
| `feature/dynamic/renderer/template/TemplateShell.kt` | Shell background, window inset coordination, independent Header/Body/Footer placement; no business-state ownership | New |
| `feature/dynamic/renderer/template/BodySlotRenderer.kt` | Existing template.components and template padding, now body-only; scroll policy | New or private composable if one type only |
| `feature/dynamic/renderer/template/HeaderSlotRenderer.kt` | Optional header component presentation and scroll collapse/fade based on approved typed backend config | New after slot contract approved |
| `feature/dynamic/renderer/template/FooterSlotRenderer.kt` | Optional footer content and bottom inset; no own nav stack | New after slot contract approved |
| `feature/dynamic/renderer/{component,section,group}/Stack*Renderer.kt` | Own stack layout and category-allowed children | Existing sdui Stack* |
| `feature/dynamic/renderer/element/{Text,Input,Button,Image,Icon}ElementRenderer.kt` | One concrete Element each | Existing sdui elements |
| `feature/dynamic/renderer/content/{Icon,Divider}ContentRenderer.kt` | One concrete Content each | Existing sdui/content/ElementContent.kt |
| `feature/dynamic/renderer/asset/DynamicAssetImage.kt` | Backend asset URL selection and loading only | Existing DynamicAssetImage.kt |
| `feature/dynamic/renderer/layout/LayoutModifiers.kt` | Shared Compose modifier mapping from typed Domain visual/layout values | Existing runtime/DynamicLayout.kt |
| `feature/dynamic/renderer/style/VisualConverters.kt` | Color, gradient stops, border/shape → Compose typed values | Existing runtime/DynamicLayout.kt and theme draw code |
| `feature/dynamic/registry/RendererRegistry.kt` | Six category-specific renderer registration/dispatch | Existing DynamicRegistry.kt (reuse mechanism) |
| `feature/dynamic/registry/RendererRegistration.kt` | Registry composition of supported concrete renderers | Existing registration |
| `feature/dynamic/DynamicContainer.kt` | Display entry UI/Store state with lifecycle | Existing container |

Avoid `BaseRenderer`, `UniversalElementRenderer`, separate handler for every trivial modifier, duplicate Header-specific element renderer families, native login/OTP screens, duplicate image fallback code.

## Header/Footer extension semantics (additive, NOT a wire format replacement)

Current canonical template has `id,type,properties,components[]`. **Keep `template.components[]` in place** (do not move it under body.components). Add optional `template.header` and `template.footer` slots after final backend schema decision. Body is **implicitly current components[]**, applying existing `template.properties.padding` only to Body. Header and Footer own independent padding and may reuse existing component renderers. Header may have fixed or collapsible behavior driven by typed backend declarations; UI-local animation scroll progress should not flood MVI Store. Template background extends to screen shell; platform window insets independent of backend slot padding.

**Proposed additive example — NOT a frozen wire contract:**
```json
{
  "template": {
    "id": "tpl_example",
    "type": "form_template",
    "properties": {"padding":{"start":24,"top":20,"end":24,"bottom":20}},
    "components": [{"id":"body_component","type":"stack_component","properties":{},"elements":[]}],
    "header": {"properties":{"padding":{"all":0}},"components":[]},
    "footer": {"properties":{"padding":{"all":0}},"components":[]}
  }
}
```
Slot component cardinality, actions, fixed/sticky/scroll modes, top/bottom safe area ownership, keyboard interaction and visual defaults require backend/UI contract confirmation. No silent legacy padding change outside Body.

## Gradient/asset correctness

- Parse gradient as type + angle + list of color/stop pairs; reject invalid ranges/empty lists; do not silently drop invalid stops or colors.
- Compute Compose Brush linearGradient endpoints from the declared angle and bounded canvas size (size-dependent Brush creation as needed), preserve stop fractions. Built-in background modifier uses Brush; avoid custom direct drawRect unless essential.
- Image and Icon URLs: backend primary required + fallbackUrl optional, then no asset. Relative paths need validated base asset URL resolution without attaching bearer credentials to untrusted external hosts.
- Critical fixture mismatch: existing Login/OTP JSON IconContent includes `properties.name` (edit, arrow_forward) without `url`, contradicting URL-only contract. Backend fixture migration or explicit supported name semantics decision is necessary for visual parity; this conflict cannot be resolved by pretending fallback PNG equals requested glyph.
- OTP Text `enabled:false` and `disabledColor` in sample JSON currently absent from Domain Text model/mapping; V2 must represent backend-declared enabled/disabled state and block action dispatch when disabled.

## Platform test and lifecycle matrix
- Android: common Compose host, Activity+Koin lifecycle, correct BuildConfig origin, status/navigation bar and IME insets; real device screenshot for Login/OTP, long body + footer.
- Web/Wasm: ComposeViewport boot, base URL meta override, CORS, Coil network engine, browser tab/history/back, keyboard/focus, viewport resizing; no parallel router.
- iOS: ComposeUIViewController, Info.plist URL, vendor ID metadata policy, platform image loading, real keyboard/safe area/modal and HTTP/HTTPS policy; actual Xcode/macOS build/simulator required.
- Tests: category registry missing/duplicate concrete renderer; layout orientation/order; all Text/Input/Button property combinations; leading/trailing Content actions; Image/URL fallback; gradient stop/angle, invalid color; Header/Body/Footer independent padding/insets, long scroll, sticky footer and collapse; field validation error display and disabled input/button event blocking.

## Pass 5 gaps prior to final PLAN FROZEN
1. Complete per-source test and resources audit for renderer and platform entry (screenshots/Compose instrumentation and Web/iOS targets).
2. Resolve the canonical fixture `name` versus URL-only icon conflict through explicit owner/backend contract update, not by changing V2 architecture to legacy.
3. Freeze **additive** header/footer slot schema and semantics; keep fixed existing JSON unchanged.
4. Freeze gradient angle unit/convention and exact stop handling, missing style/default policies.
5. Ensure new V2 renderer and Store signatures compose without dependency cycles and that all source paths/Gradle tasks exist.
6. Make exact create/modify/delete/caller migration matrix and precise test verification commands in Pass 6.

**Outcome:** Pass 5 source-level architectural audit documented. This does not assert rendered UI parity, passing tests, or plan freeze.
