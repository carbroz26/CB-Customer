# 133 — P0 FormFieldSection implementation and verified build handoff

**Date:** 2026-10-23  
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`  
**Current implementation HEAD before this status doc:** `8102600b41720a77eddb27551f7d701462b247e8`  
**Decision authority:** `132-P0-FORM-FIELD-SECTION-EXACT-CONTRACT-SRP-AUDIT-AND-FREEZE-2026-10-23.md`.  
**Current status (owner-supplied Windows logs, 2026-10-23):** **BUILD SUCCESSFUL** for all four P0 Gradle verification tasks after compiler fix. **Browser visual review is pending; P0 is not acceptance-frozen**.

## Implementation

- New concrete `FormFieldSectionDto`, `FormFieldSection` Domain and `FormFieldSection` renderer registered in the **existing Section registry**.
- New typed `FormFieldControlSurface` owns normal/focused/invalid outer border, height, background and clipping.
- New Section coordinates Label → direct Input or composite StackGroup → reserved optional validation message row. The style and dimensions are entirely JSON-authored.
- StackGroup Domain, GroupMapper, Renderer and DynamicLayout no longer own field focus, error borders, messages or space reservations. Existing ListSection selected-row styling intentionally retained as P1-only exception.
- Existing DynamicFormValidator obtains presented-field binding from FormFieldSection; required/pattern/named validation and existing Store edit revalidation remain unchanged.
- Existing `partner-login-country-picker-parity-v8-P0-PLAN-ONLY.json` is byte-identical to now-active `webApp/src/wasmJsMain/resources/preview/partner-login.json` and the new versioned `partner-login-country-picker-parity-v8.json`. Preserve v7 for historical comparison.
- No backend, `VisualLoginPreview.kt`, Login actions, Store, Country Picker selection/fallback, theme, hero, assets or P1 implementation changes.

## Source-level checks performed

1. Entire JSON outside `data.template.components[login_form].sections[0..1]` matches v7 by normalized object comparison.
2. All `data.presentations` (Country Picker) JSON unchanged.
3. GroupMapper rejects removed focus/validation Group contract; Domain/Renderer no longer declare/read those Group fields.
4. Existing Group selectedStyle still reads list row-selected state; existing strict six registries unchanged except one concrete Section registration.
5. Control border precedence authored: invalid > focus > normal. Label-to-control gap 8dp; field-owned control surface height 56dp; reserved error row 24dp; number-only error tint `#FFF1F2`.
6. New/updated test source covers direct first/last-name field, composite phone, malformed field structures, old-group presentation rejection, Store 12→11→10 revalidation, border priority and registry registration.

**These are source checks, not evidence of successful Kotlin compilation, test execution or screenshot parity.**

## Required Windows test gate (user-run)

```powershell
cd D:\CarBroz\CB-Partner
git pull --ff-only origin architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :data:jvmTest :feature:dynamic:jvmTest :feature:dynamic:compileAndroidMain :webApp:wasmJsBrowserDevelopmentWebpack
```

If the build fails, stop visual acceptance and correct the exact error on the same branch without unrelated edits. For successful build, start the existing Web preview and hard refresh at `http://localhost:8080/?cbpartnerLoginPreview=1`. Compare against `?cbpartnerVisualPreview=1` at the identical 411x891 viewport and check:
- Empty phone submit -> required message, border red, only editable area pink; no form height jump.
- 12-digit failed submit -> length message, red border/pink area; 11 still invalid, valid 10 clears.
- Country code, flag, chevron, divider, Search, selected check, list empty fallback, hero, logo, legal lines and actions visually unchanged.
- First/Last name fixture is for generic mapping/contract testing, not a new product screen.
- Verify one-line error message fits authored 24dp reserved height without clipping.

## Local Windows verification evidence (2026-10-23)

After the owner ran `git pull --ff-only origin architecture/dynamic-runtime-v2-discussion-freeze`, the first four-task Gradle run failed with `SMARTCAST_IMPOSSIBLE` on cross-module nullable `DynamicValidationLengthMessage.minLength/maxLength` properties in `DynamicFormValidator.kt` (line 135/136). The isolated fix was committed as `81c17638f8ecf75aa2f2472138a134539d113b8b`: capture each nullable property in a local `val` before comparing, with no message/validation semantics change.

The owner pulled this fix and reran:

```powershell
.\gradlew.bat :data:jvmTest :feature:dynamic:jvmTest :feature:dynamic:compileAndroidMain :webApp:wasmJsBrowserDevelopmentWebpack
```

Observed result: **BUILD SUCCESSFUL in 4m 9s**, 145 actionable tasks (34 executed, 111 up-to-date); all four requested verification tasks passed. Remaining non-fatal compiler/tool warnings: redundant always-true condition in `FormFieldSection.kt:59`, WebLoginPreview's WasmJs interop opt-in, Windows-disabled iOS simulator, Android host-test configuration note, npm `ws` version resolution and Webpack expression-dependency warning. These do not block this successful test gate. Do not claim the warnings have been remediated.

**Next gate:** Compare matching-viewport dynamic `?cbpartnerLoginPreview=1` versus immutable `?cbpartnerVisualPreview=1` screenshots in default, empty-invalid, 12 digits invalid, 11 digits invalid, 10 valid; verify fixed error-slot height, border/background, Country Picker and hero. No runtime screenshots have been supplied yet.

## Pending

A Compose/Web runtime screenshot or pixel comparison is not available from GitHub-only source access. Do **not** claim screenshot parity or P0 acceptance until owner supplies it. P1 cleanup findings stay only in Doc 132; no P1 code work authorized.
