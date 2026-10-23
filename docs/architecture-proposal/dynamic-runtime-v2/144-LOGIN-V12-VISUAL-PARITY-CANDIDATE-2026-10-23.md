# 144 — Login Dynamic Visual Parity v12 (Preview Candidate)

**Date:** 2026-10-23
**Frontend branch:** `architecture/dynamic-runtime-v2-discussion-freeze`
**Parent at authoring:** `d9ab0af8f5280d8037f54e3a39b16cad23ca06ef`
**Status:** VERSIONED VISUAL CANDIDATE — implementation source/JSON committed for Android/Web preview; build, exact reference parity and owner device acceptance PENDING. Not a new product spec freeze.

## Goal and authority

After India-only validation freeze (Doc143) and keyboard/safe-area acceptance snapshot (Doc142), improve **actual typed Dynamic Login** toward owner-supplied `Login(10).jpeg` reference. Earlier Doc135 design-board typography and palette are preserved where a reference image cannot give trustworthy exact metrics. Docs138/140 supersede reserved field-message space and protect the standalone VisualLoginPreview.

## JSON-only targeted changes (v11 → v12)

- Keep existing `form_template` hero resource `images/partner_login_hero_02` and `contentScale=crop`. Trial `backgroundImage.alignment=topCenter → center` using the already-supported generic alignment property to investigate excess sky / worker and car occlusion. This is an experiment and may produce no vertical change depending on source dimensions; do not claim final asset framing.
- Brand remains centered in the existing StackComponent. Increase `brand_name.fontSize=20 → 22`, `lineHeight=28 → 30`, leading bundled logo `32×32 → 44×44` dp and logo-to-label content spacing `9 → 11` dp as preview-only approximations to the prominent mockup branding. These supersede Doc135 numbers **only as a candidate**, not as an accepted replacement specification.
- `welcome_title` and `welcome_subtitle`: change `textAlign=center → start` and set `fillMaxWidth=true`. Both keys are necessary in the existing generic TextElement because `textAlign` alone does not move wrap-content text across a center-aligned column. Preserve `32/40`sp title and `16/24`sp subtitle authored typography, colors and spacing until device review.

## Exactly unchanged

- `data.template.components[1]` (entire white Login form, mobile field, button, legal text, errors, focus and request action).
- All `data.presentations` Country Picker content and behavior, `initialBindings`, `theme`, schema, screen identity and template structure.
- Generic source Kotlin, common renderers, Store, validation, navigation, Splash, Android window/IME and backend.
- India-only `phone_number` validator remains registered; non-IN choices remain preview-only and fail validation.

**Changed JSON paths (10):**

- `data.template.properties.backgroundImage.alignment`
- `data.template.components.0.elements.0.properties.fontSize`
- `data.template.components.0.elements.0.properties.leading.0.properties.width`
- `data.template.components.0.elements.0.properties.leading.0.properties.height`
- `data.template.components.0.elements.0.properties.leading.0.properties.spacing.end`
- `data.template.components.0.elements.0.properties.lineHeight`
- `data.template.components.0.elements.1.properties.textAlign`
- `data.template.components.0.elements.1.properties.fillMaxWidth`
- `data.template.components.0.elements.2.properties.textAlign`
- `data.template.components.0.elements.2.properties.fillMaxWidth`

## Verification gate

1. Current active Web preview fixture is byte-identical to v12; historical v11 is immutable.
2. JSON parsed successfully and structural diff restricted to the above ten paths. This static check is not a substitute for Gradle/device.
3. On Android A35: normal portrait with keyboard closed, compare logo, text left edge, skyline height, worker head/arm/car, card start and links; then focused input/IME, invalid submit/clearOnEdit and Country Picker/search.
4. Test `:data:jvmTest :feature:dynamic:jvmTest :feature:dynamic:compileAndroidMain :webApp:wasmJsBrowserDevelopmentWebpack :androidApp:installDevelopmentDebug` on owner's Windows machine, launch `--ez cbpartner.login_preview true`.
5. If `center` crop does not improve framing or harms sky/worker layout, restore `topCenter` or investigate the source image composition before adding **any** new generic image-crop API. A fixed/unknown-color, font-family, image crop or arbitrary offset must not be invented.

**This is P0 visual investigation, not a declaration of full Login parity or backend end-to-end support.**
