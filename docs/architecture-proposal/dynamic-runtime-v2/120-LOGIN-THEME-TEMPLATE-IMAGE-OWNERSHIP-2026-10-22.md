# L-06 Login Theme and Template image ownership — JSON revision
**Date:** 2026-10-22  
**Scope:** Frontend integrated Login JSON and offline runtime fixture only; backend remains untouched.

## Source and frozen-decision reconciliation
- V2 Doc112 §18 is the later B3b owner correction: Theme owns its gradient; Template owns the hero URL image.
- `DynamicRenderer.screenTheme` applies the gradient from `theme.properties.gradient`.
- `TemplateMapper` requires `template.properties.backgroundImage.source` to have `type: "url"`, and validates `contentScale` and `alignment`.
- `FormTemplate` paints the Template background image independently of the Theme.
- Doc118 explicitly keeps the historical B5 canonical target immutable and requires a separately versioned integration JSON.

## Revised target and exact data changes
- Historical audit baseline unchanged: `docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-current-target.json`.
- **Revised integrated target:** `docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-integration-v2.json`.
- Web offline runtime fixture replaced with the same revised JSON: `webApp/src/wasmJsMain/resources/preview/partner-login.json`.
- The independent `cbpartnerVisualPreview=1` dummy UI remains unchanged.

Only these two object locations were edited; all unrelated values were verified identical:
1. `data.theme.properties`: remove obsolete `backgroundImage`; set the **exact owner-provided** 135-degree linear gradient with stops 0, 0.28, 0.55 and 1.
2. `data.template.properties.backgroundImage`: add typed URL source with `crop` and `topCenter`.

### Exact Theme
```json
{
  "theme": "light",
  "statusBar": "transparent",
  "properties": {
    "gradient": {
      "type": "linear",
      "angle": 135,
      "colors": [
        {
          "color": "#DDF8F6",
          "stop": 0
        },
        {
          "color": "#F7FEFD",
          "stop": 0.28
        },
        {
          "color": "#FFFFFF",
          "stop": 0.55
        },
        {
          "color": "#D9F7F4",
          "stop": 1
        }
      ]
    }
  }
}
```

### Template hero contract
```json
{
  "source": {
    "type": "url",
    "value": "/images/partner_login_hero.jpg"
  },
  "contentScale": "crop",
  "alignment": "topCenter"
}
```

## Asset gate
The relative `/images/partner_login_hero.jpg` is taken from the earlier **user-provided Login JSON draft**. It is an example path, not a verified hosted or licensed hero asset. Because the trusted API origin is used for image resolution, the photo can remain absent in offline preview. The independent Theme gradient can still render. G2 hero asset verification stays open; no default remote image or additional fallback is invented.

## Verification scope
The revised JSON was parsed and structurally checked: after restoring the prior Theme and removing the Template image, the entire revised object equals the original B5 JSON, including all actions, bindings, country picker, and legal links. The Web fixture matches the versioned target. Kotlin compilation and browser screenshot verification were not performed as part of this data-only change.
