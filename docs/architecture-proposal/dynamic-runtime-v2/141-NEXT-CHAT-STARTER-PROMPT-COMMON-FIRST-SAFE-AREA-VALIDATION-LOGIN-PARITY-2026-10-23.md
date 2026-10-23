# 141 — NEXT CHAT STARTER PROMPT: common-first safe-area, generic validation ownership, Login v11 P0 parity

Copy everything after this sentence into the new chat. The comprehensive authoritative status is doc140; verify latest GitHub HEAD when starting.

---

Continue the existing **CB-Partner Kotlin Multiplatform + Compose Multiplatform Dynamic Runtime V2** project. Conversational explanations in Hindi/Hinglish; documentation, code/KDoc and decisions in English. We need **fast, correct work** with strong Clean Architecture, SOLID/SRP, MVI/UDF and minimal code; avoid circular discussion and unnecessary small freeze documents or elaborate plans for trivial fixes.

## Repositories / active work

- Frontend: https://github.com/carbroz26/CB-Partner
- ONLY active branch: architecture/dynamic-runtime-v2-discussion-freeze
- Windows local root: D:\CarBroz\CB-Partner
- GitHub source checkpoint before this handoff document: 21229a450e4c001554c70ffbdeef982d1bf415ab; inspect CURRENT remote HEAD, do not assume unchanged.
- Backend identity/branch is not to be guessed: historical V2 doc119 mentions carbroz26/carwash-backend feature/backend-migration; other work used carbroz26/CB-Backend development. Verify actual relevant backend source before asserting contract or touching anything; no backend edits without separate authorization.
- Runtime on owner Windows: Android Samsung A35 SM_A356E via Wi-Fi ADB/scrcpy; Web Wasm; iOS requires Mac/Xcode runtime tests.

## REQUIRED first action: understand V2 project correctly

READ **EVERY** file under docs/architecture-proposal/dynamic-runtime-v2/, including README and all numbered planning, frozen, implementation, audit, handoff and JSON files, not just titles. **Mandatory latest documents 138, 139, 140, 141**, and foundation docs 01–08, 12, 16–20, 23, 44–55, 60–81, 98–122, 124–137. Pay special attention to chronology: later owner decisions supersede earlier frozen passages; an old PENDING may be completed, a previous FROZEN may be reopened. This folder plus current source is our architecture authority; do not use unrelated project document trees for design decisions.

READ the actual full source for **all modules, packages and classes**, with special LINE-BY-LINE attention to each affected package and every caller/DI/test before any modifications:
  - core, domain, data (DTOs/mappers/repositories), network, navigation, feature/splash, feature/dynamic (Store, runtime, renderer, registry, actions, presentation), app/commonMain/iosMain, androidApp, iosApp, webApp, build-logic and Gradle.
  - Understand class purpose, why it exists, who calls it, its exact responsibility, state ownership, test coverage and alternatives; do not assume names, comments or historical docs equal reality.
  - Inspect current Login v11 JSON and active Web JSON fixture; compare to v10/v9 and original user-supplied Login reference, preview screenshots; do not modify the protected VisualLoginPreview.kt.
  - Verify actual backend JSON contract when discussing product validation or Request; no guessed URL/region policies.

## NON-NEGOTIABLE frozen owner principles

Kotlin Multiplatform + Compose Multiplatform Android/iOS/Web; **commonMain FIRST** for shared UI/runtime/behavior, and platform-specific code only where OS API genuinely requires it. These are ownership decisions from current source, not an instruction to put everything in app/commonMain. Clean Architecture; one responsibility/class; no duplication/overengineering/underengineering, no ViewModels, no screen-specific hardcoded Login/OTP/phone/name renderer/logic in generic Dynamic runtime. Backend JSON drives styles/validation/actions/navigation. No fabricated font sizes, padding, colors, fallbacks, native glyph registries, arbitrary managers or duplicate network/navigation/store. Maintain single DynamicStore per navigation entry, single DynamicSession, single action executor/registry. Small change sets and tests. Short direct answers; no repeated lengthy planning for tiny compiler fixes; discuss→audit→decide→small freeze→implement→test→review for material contract changes.

## Exact six-category Dynamic UI structure

Exactly one Template per response, with 1..n Components; Components can have direct Elements and/or Sections; Sections have direct Elements and/or Groups; Groups have Elements only; Elements can have leading/trailing Content. Header/Footer are optional template slots using same component registry; Body is the existing template.components array. Theme owns gradient; Template owns hero/background.

ACTUAL registered types:
- Template: FormTemplate
- Component: StackComponent
- Sections: StackSection, FormFieldSection, ListSection
- Group: StackGroup
- Elements: Text, Image, Input, Button, DividerElement, Icon
- Contents: DividerContent, IconContent, ImageContent

Earlier "no DividerElement" prose is obsolete: current source registers DividerElement. Map JSON via typed DTO -> Category Mapper -> Domain -> DynamicStore -> DynamicContainer -> DynamicRenderer -> six-category registry + Compose renderers. No screen-specific mapper or UI class. Country Picker is an existing presentation, ListSection has emptySectionId referencing a sibling fallback StackSection exactly once.

Runtime references ONLY $binding, $context, $state, $literal (never $response). DynamicValue typed incl Object/Array; entry bindings vs presentation bindings vs session retained state distinct. Inputs emit intents; Store orchestrates; executor selects one handler; Request resolves fields/references and delegates networking; state commits before external/navigation effects via session; one NavigationState push/replace/pop. Existing registered action vocabulary request, navigate, present, dismiss, external_uri, state, sequence. **Sequence is inert**, not recursive, under late doc61. State wire/backend mismatch and other deferred action semantics remain separate; don't revive older behavior.

## Verified completed work (don't redo)

- Login JSON v10 design-token mapping and Hero_02 uploaded/registered. Native development-only preview reuses same canonical Web JSON fixture and original Mapper/Store/renderer; no browser/backend needed. Installed package com.carbroz.cbpartner.android.dev; actual Activity com.carbroz.cbpartner.android.MainActivity.
- User explicitly decided **NO reserved validation error slot**. New Login v11 JSON and renderer remove reserveHeight entirely; normal phone input→Continue authored 16dp gap (previous 48dp including 32dp reserved); error message appears only on error. User confirmed correct native screenshot. Tests updated, including follow-up failed DynamicFormValidatorTest fixture. Don't restore stable error-area layout.
- Phone outer red border and number-only pink editable tint remain, clearOnEdit is authored true in Login, errors return upon invalid submit. Country Picker/search/select/emptySectionId preservation critical.
- Android-only navigationBarsPadding at MainActivity was committed 445cfa7a; immediate build error Unresolved foundation/Box; Gradle dependency fix committed 21229a450e4c001554c70ffbdeef982d1bf415ab (apply existing cbpartner.compose plus implementation(compose.foundation)). **No subsequent successful build or actual bottom overlap screenshot provided**. Treat source committed/device verification PENDING.

## Priority open tasks (avoid parallel unrelated refactoring)

(1) FIRST: verify current Android dependency fix builds and that Terms/Privacy appear above Samsung 3-button nav. If build error, narrow edit only.
(2) NEXT: audit **common-first window-insets ownership** across Android, iOS, Web. Current MainActivity-only padding is provisional; likely common app host can own policy if actual platform support/inset consumption is verified. Read App/ApplicationContent, platform entrypoints, Splash/Dynamic renderer, FormTemplate/TemplateShell, nav host, modal dialog/presentation, Compose/Gradle. Background Hero may remain full-bleed while interactive content clears system bars; don't blindly wrap whole app and clip hero, don't double-pad or add Web gap. Decide minimal cross-platform implementation; strip redundant Android code only after correct shared boundary is identified. Write short exact change/test plan, implement when authorized.
(3) ALSO OPEN, separate source/contract gate: WHY does app/commonMain/validation/PartnerPhoneNumberValidator exist? It hardcodes key phone_number, region IN, 10 ASCII digits starting 6–9 and is injected from App.kt. Current Login v11 JSON explicitly references validation.validator.key phone_number and region={$binding:countryId}. DynamicFormValidator in feature/dynamic is already generic and supports required/pattern/lengthMessages/clearOnEdit/named validators. IMPORTANT: lengthMessages selects ERROR COPY ONLY, it does not enforce length itself. User wants **fully dynamic backend-driven validation without phone/name/OTP-specific Kotlin**. Audit complete implementation/DI/tests + actual backend supported-region and error contract before choosing declarative generic validation versus externally supplied named policy. Do not just delete validator, relocate hardcoded regex to runtime, remove rule, or invent supported-country behavior. Maintain server final validation authority.
(4) AFTER inset geometry accepted, reconcile remaining native Login v11 visual gaps to original reference: centered Welcome/subtitle vs original LEFT, logo too small/high, CB-Partner typography and baseline, original subtitle darker gray-blue, original button darker despite design-board #007AFF, legal terms text readability, card inner spacing, top relative to status, hero excess sky/worker arm & back to waist, car and trees BOTH horizontal edges, crop/asset framing. Confirm screenshot/design board conflicts instead of guessing. User specifically wants all font sizes, weights, line heights, font family, alignment, margins, colors and geometry audited, not a cursory list. No screen-specific Kotlin style hacks, preserve v11 history and version JSON for future approved edits.
(5) OTP UI v2, practical timer/resend, other backend wire/State/Present/Sequence, and P1 generic SRP cleanup **DEFERRED** until Login P0 acceptance and separate authorization; do not overextend scope.

## Recent code checkpoints

v10 design 4b8333c6; Hero02 upload c058f355 and integration 33111135; native preview 4813015d; generated Android preview assets 2a27f01d; v11 reserve removal d6157e91, tests fixed f51062ad; safe inset first 445cfa7a, missing Foundation fixed 21229a450e4c001554c70ffbdeef982d1bf415ab. The comprehensive history, source paths, updated acceptance and supersession are in docs/architecture-proposal/dynamic-runtime-v2/140-2026-10-23-COMPLETE-HANDOFF-COMMON-FIRST-VALIDATION-LOGIN-PARITY.md.

## Minimal user Windows commands

~~~powershell
cd D:\CarBroz\CB-Partner
git pull --ff-only origin architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :androidApp:installDevelopmentDebug '-Pcbpartner.api.baseUrl=http://127.0.0.1:3000'
adb shell am start -S -W -n "com.carbroz.cbpartner.android.dev/com.carbroz.cbpartner.android.MainActivity" --ez cbpartner.login_preview true
~~~

For broader implementation gates: :data:jvmTest :feature:dynamic:jvmTest :app:jvmTest :feature:dynamic:compileAndroidMain :webApp:wasmJsBrowserDevelopmentWebpack. iOS runtime requires macOS; never claim it passed on Windows. Use existing ADB wireless device and scrcpy; screenshot via adb shell screencap/pull.

## What to deliver in your first reply

State audited HEAD and which V2 documents & relevant exact source classes you have actually READ, not just promised to read. Summarize only (a) current common/platform inset ownership and build gate, (b) current validator implementation, JSON/back-end dependency and architecture risks, (c) exact Login P0 visual gaps, then give ONE practical prioritized next step with minimal files/tests. Avoid overly long repeated high-level explanations. If allowed and scoped, implement a coherent wave and push after tests/source checks; no unsupported claims of execution or frozen status.

Never repeat a question already answered or start a new unrelated backend/OTP effort.
