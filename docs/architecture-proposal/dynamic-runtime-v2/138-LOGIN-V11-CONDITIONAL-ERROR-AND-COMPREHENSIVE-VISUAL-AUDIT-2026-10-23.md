# 138 — Login v11 conditional validation message and full visual audit

**Date:** 2026-10-23  
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`  
**Decision:** Owner explicitly removes reserved validation-message space. **Supersedes the fixed reserved-slot and stable-height requirements only** in Docs 131, 132, 133, 134, and 135. Keep all their other SRP, validation and visual contracts.  
**Status:** Source/fixture/tests committed; device rendering and Gradle tests require user verification.

## Conditional validation message

`FormFieldSection` remains the sole label/control/message presenter. It renders message using its authored `DynamicValidationMessageStyle` **only when** `context.validationErrors[inputBinding]` contains an error. No error => no empty Box or reserved height. `DynamicValidationMessageStyle.reserveHeight` is removed from the typed model and mapper; unknown `reserveHeight` JSON is rejected. The inline message still has authored text style and `spacing.top:6`. Focus/invalid border precedence and editable-only background, existing Store, named/length/required validation, `clearOnEdit:true`, Country Picker, actions and renderer registry are unchanged.

Login retains `FormFieldSection.padding.bottom:16dp`, so **normal input-to-Continue gap = 16dp**, down from `32dp reserved + 16dp padding = 48dp`. When an error is shown, the message appears between control and Continue and its actual text layout + 6dp top spacing extends the form; this **intentional size shift** supersedes the previous no-jump expectation. No new hardcoded screen-specific UI behavior, no duplicate Store.

Versioned v10 JSON is retained as historical snapshot. New `partner-login-country-picker-parity-v11.json` matches active Web fixture byte-for-byte; the **only JSON difference from v10** is removal of the `reserveHeight:32` key. Native Android development preview packages the same active Web fixture, and will pick up v11 on rebuild.

## Comprehensive parity audit — owner original mockup versus Samsung A35 native screenshot

Original full-height mocked phone and native Samsung screenshot use different display scale and status/navigation insets; visual differences are qualitative, not exact measured original dp. Design-board specification and screenshot sometimes disagree; **do not silently change colors/font**. Audit every item:

| Item | Current v11 JSON (unchanged from v10 except reserved height) | Visible/pending difference |
| --- | --- | --- |
| Logo/brand group | 24dp side inset, 48dp top, 32dp bottom, centered | Mockup branding sits noticeably lower; check Android status inset before changing. Logo and brand both appear smaller in real device |
| Logo | 32×32dp, right spacing 9dp, Fit | Original icon appears significantly larger and more visually prominent; identify normalized physical target |
| Brand text | 20sp, 700, lineHeight28sp, #0A3A8D, centered | Compare actual font family/letter shape and logo-text baseline; original dark navy. No explicit fontFamily contract |
| Brand→Welcome gap | 12dp | Mockup vertical hierarchy/gap not visually identical |
| Welcome heading | 32sp, 700, lineHeight40sp, #0A3A8D, center textAlign | Standalone mockup heading is LEFT-aligned with horizontal margin; current Android centered. Separately approve JSON alignment change |
| Welcome→subtitle gap | 8dp | Compare text baselines after normalization |
| Subtitle | 16sp, 400, lineHeight24sp, #6B7280, centered | Original LEFT-aligned and appears stronger/bluer; unresolved screenshot-vs-board authority |
| Hero | `images/partner_login_hero_02`, `crop`, `topCenter`, full template background | Too much upper sky; skyline, palms, car and worker/arm/waist lower than mockup; both lateral cropping and white-card occlusion. Need asset and crop geometry jointly after insets |
| Card | white, top radius24dp, padding start/end/top16dp, bottom24dp | Bottom Terms overlap Android 3-button nav; generic system bottom inset investigation still pending |
| Field label | 14sp,600, #18263B, spacing.bottom8dp | Reference label appears larger/darker navy |
| Input | 56dp height, radius16dp, border1dp #CBD5E1 | Almost aligned; check horizontal card inset and width |
| Country selector | flag24×18dp; dial15sp/600/#18263B, chevron12dp, divider28dp | Slight accessory proportions; preserve selection and layout |
| Input typing/placeholder | 16sp/400/lineHeight24sp; placeholder #9CA3AF | Font rendering/gray contrast require actual matched viewport |
| Input→Continue | **16dp empty**, no reserved error box | Owner-approved gap correction; error inserts text and shifts button |
| Continue | 56dp height, 16dp radius, label16sp/600/24sp white, fill #007AFF | Mockup button appears darker/deeper, conflict with design-board #007AFF |
| Continue→legal | Section bottom padding16dp; description bottom spacing4dp | Consent layout offset, compare after safe inset |
| Description | 12sp/400, #6B7280, center | Reference looks slightly larger/stronger |
| Legal spans | 14sp/500/20sp, #007AFF, underlined, centered | Reference darker/heavier, links currently obscured by Samsung nav buttons |
| Android status/navigation | No established generic safe-area guarantee | Navigation overlap and relative top-offset need isolated generic template/host safe inset investigation |

**Do not claim:** an exact original fontFamily, precise original dp from a mockup, fully resolved backend parity, tested errors/picker/loading, or a fixed Android navigation inset. P1 SRP cleanup remains blocked until P0 visual acceptance.

## Modified files/gates

- Domain `FormFieldSection` + `DynamicValidationMessageStyle`
- Mapper `SectionMapper`
- Renderer `FormFieldSection`
- Data mapper contract tests, feature Section contract tests, feature Store regression fixture
- Active Login JSON fixture and new v11 versioned JSON; historic v10 preserved

```powershell
cd D:\CarBroz\CB-Partner
git pull --ff-only origin architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :data:jvmTest :feature:dynamic:jvmTest :feature:dynamic:compileAndroidMain :androidApp:installDevelopmentDebug '-Pcbpartner.api.baseUrl=http://127.0.0.1:3000'
adb shell am start -S -W -n "com.carbroz.cbpartner.android.dev/com.carbroz.cbpartner.android.MainActivity" --ez cbpartner.login_preview true
```

Acceptance: normal input-to-Continue gap ~16dp; submit empty -> red message/tint/border appear and content may shift; first edit -> validation clears and gap returns; Country Picker and hero behavior unchanged. Additional bottom safe-inset/hero/typography changes require separate confirmation.
