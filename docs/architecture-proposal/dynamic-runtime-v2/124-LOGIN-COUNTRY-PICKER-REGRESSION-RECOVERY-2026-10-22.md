# 124 — Login and Country Picker regression recovery

**Date:** October 22, 2026  
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`  
**Owner request:** Restore the previously approved working Login and Country Picker; retain only the approved change to the Login phone validation border. Do not change backend.

## Non-destructive recovery

- Source baseline: commit `c6832e672b943bfb55e4cecda85b319f8fa6c037`, the final OTP v2 UI-only contract revision **before** the unverified P0–P5 renderer implementation wave.
- The recovery commit has the current branch as its parent, **not** a force reset, and copies the complete tree from the baseline, except:
  1. `webApp/src/wasmJsMain/kotlin/com/carbroz/cbpartner/web/VisualLoginPreview.kt`: on invalid phone, the **whole** rounded phone row receives the red border `#DC2626`; **only editable number area** has the red-tinted `#FFF1F2` background. No inner second red border.
  2. This recovery document.
- Full-width white Login panel, full-width Country Picker sheet, mock flags/icons/selection/search, legal text wrapping, static skyline and visual styling are inherited **unchanged from the approved baseline**.
- Historical Login JSON integration v2, OTP UI contract v1 and v2, Doc120/121/122, and the actual Dynamic UI renderer baseline are preserved.
- The P0–P5 unverified implementation commits are retained in Git history, but their changes are intentionally absent from the recovered tree, including the draft v3 fixtures, preview-only OTP route, and Doc123. They must not be called implemented after this recovery.

## Scope and important preview distinction

- `?cbpartnerVisualPreview=1` is the previously approved **standalone visual dummy** with the working Country Picker, now with correct entire-row error border.
- `?cbpartnerLoginPreview=1` is the separate **real frozen JSON / Dynamic Runtime** preview, which was *never* visually equivalent to the standalone dummy. This rollback does not claim to make those previews visually identical.
- Future implementation should use the visual dummy as reference and incrementally reconcile the generic renderer and JSON, gated by tests and screenshot comparisons. Do not break the working dummy in the process.

## Deferred work

OTP JSON v2 remains UI-only frozen. Segmented all-six-box validation styling, reserved message space, positional digit editing, enabled/disabled buttons and native OTP preview must be implemented/tested separately before being considered done. Timer/Resend request remains deferred; no confirmation dialog or OTP server/network alert card is permitted. Backend remains untouched.

## Required local verification

```powershell
git pull --ff-only origin architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :domain:jvmTest :data:jvmTest :feature:dynamic:jvmTest :app:jvmTest :feature:dynamic:compileAndroidMain :webApp:wasmJsBrowserDevelopmentWebpack
.\gradlew.bat :webApp:wasmJsBrowserDevelopmentRun
```

Open `?cbpartnerVisualPreview=1` (not `?cbpartnerLoginPreview=1`) to compare against the approved dummy. Verify Country Picker open/search/select/dismiss; invalid phone checks; valid phone; full-width Login and sheet; legal links.
Source/Git tree matching does not prove a fresh local build or pixel acceptance.
