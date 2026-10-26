# 161 — Generic Toast Presentation V1 — Owner-Approved Frontend Contract and Single-Batch Manifest

**Approval**: 2026-10-26. **Source baseline**: `739b6b34737f7cbdf26e22e7c15c81df2cdbc762`, branch `architecture/dynamic-runtime-v2-discussion-freeze`. **Implementation status at freeze**: not implemented; source commits and tests required separately. Backend `carbroz26/CB-Backend/development`: READ-ONLY.
**Source of truth**: this document plus latest approved V2 source/contract, not an invitation to change earlier Login v19 / OTP v3 UI or network. No unrelated cleanup, no new UI category, no new networking/Store/Nav framework.

## 1. Why this is a ToastPresentation, not Dialog or Android Toast
- **New concrete type only in the existing presentation family**: `ToastPresentation : DynamicPresentation`. Render its **existing** Components / Sections / Groups / Elements with the **existing** registry and `DynamicPresentationHost`; add one nonmodal branch. No `ToastElement`, `ProgressElement`, `ToastStore`, `ToastManager`, platform-native Toast or second action dispatcher.
- UI: **white card**, rounded border radius, small green circular badge with **small white tick**, green two-line text, **NO X/CLOSE ICON**, white bottom progress track, green segment starting at **0%** and filling **left to right to 100%** by authored duration; **auto-dismiss at 100%**. No tap-to-dismiss or outside/back dismissal; no dimming or blocking touches.
- **App root owns transient overlay**; one toast at once, newest accepted toast replaces previous. On navigation/rotation preserve the same start time and remaining elapsed duration; no replay of request or toast. When backgrounded time advances; expired toast does not return.
- Scoped screen-defined `data.presentations[]` + **existing** `present {presentationId}` works across the app. Only after successful request and accepted state changes, an optional `successPresentationId` on `request` fires; no general nested `sequence` or arbitrary nested `onSuccess` executor. Also support immediate response-level `presentation` as Toast, using the existing mapper (not only Dialog), with response presentation taking precedence over local successPresentationId. Request destination + Toast is invalid in same response.
- **No fake close button** or invented Offline Mode / fixed country code in runtime. OTP screen can author `+91` as preview text as existing OTP demo already does; when migrating Backend, authoritative formatted phone display is a separate dependency. Do not log personal phone/OTP text.

## 2. Typed wire schema

Toast `{id,type:"toast",properties,initialBindings,components}`. Properties:
- `placement:"top"` (V1 only), `durationMillis:5000` (positive 500–15000), `background:{color:"#FFFFFF"}`, `shape:{type:"roundedCorner",cornerRadius:20}`, `padding` and `spacing` as existing typed objects;
- `progress:{height:4,trackColor:"#FFFFFF",fillColor:"#079553",padding:{start:8,end:8}}` required; no `direction` property because V1 always start-to-end; no `dismissible` property.
- components must be nonempty; exactly one `StackComponent` for notification row; no property bag. Content: `StackSection` horizontal, `IconBadgeElement` with asset `icons/toast_success` (transparent tightly cropped white tick), `TextElement` with styled spans, **no close icon**. Title/text/color/size authored in JSON, not hardcoded Composable defaults.
- Domain models: `ToastPresentation`, `ToastProgress` typed in existing presentation domain file. Data: `PresentationMapper` explicit `toast` branch with strict validation. Immediate response `DynamicResponse.presentation` generic typed `DynamicPresentation?`, but only `DialogPresentation` and `ToastPresentation` accepted at response level.
- `RequestActionHandler` validates `successPresentationId` only when `responseMode = none`, and only emits success trigger on an actually successful network operation. `DynamicStore` looks up that ID in **its own currently loaded screen**; only `ToastPresentation` is permitted in this success-only contract. Commit state and toast effect as one ordered accepted session transaction to preserve challenge ID/cooldown before notification. No retry mutation, no new action vocabulary.
- `DynamicActionEffect.Toast` carries mapped presentation and immutable binding/state snapshots; existing `DynamicSession` claims/acknowledges effect with no new queue. `ApplicationContent` consumes and displays via `DynamicPresentationHost`, dismisses solely by its elapsed monotonic duration. `present` on Toast does not occupy modal `activePresentationId`; Dialog/BottomSheet remain unchanged.

## 3. Frozen implementation manifest

| Phase | Module | Exact files |
|---|---|---|
| P0 | docs/fixtures | This 161 file, review OTP V3 unchanged baseline; add `toast-v1-otp-resend-success.json` example + same `data.presentations[]` in OTP V3 & Web preview mirror |
| P1 | domain/data | `domain/model/dynamic/presentation/DynamicPresentation.kt`, `domain/model/dynamic/DynamicResponse.kt`, `data/dynamic/mapper/PresentationMapper.kt`, `data/dynamic/mapper/DynamicResponseMapper.kt` |
| P2 | feature/dynamic | `action/DynamicActionResult.kt`, `action/RequestActionHandler.kt`, `store/DynamicStore.kt`, `presentation/DynamicPresentationHost.kt` |
| P3 | app | `app/ApplicationContent.kt` and one small `app/ActiveToast.kt` transient data class; `app/DynamicSession.kt` existing ordered effect code reused, no required structural edits |
| P4 | resources/previews | `feature/dynamic/sdui/asset/DynamicPackagedAssets.kt` + one tightly cropped PNG drawable; approved OTP V3 JSON `docs/architecture-proposal/dynamic-runtime-v2/partner-otp-ui-contract-v3.json` and mirror `webApp/src/wasmJsMain/resources/preview/partner-otp.json`. Existing Android preview uses Web-mirrored source per current MainActivity code; confirm source mapping |
| P5 | tests | Extend existing focused mapper/action/Store/App tests, add minimal Toast contract/elapsed-time test where appropriate; consolidated Gradle command, install Samsung A35, verify exact white background, 0%→100% and auto-dismiss/rotation/no double success |

Any additional file/class/property/action must be justified by an observed, material source-backed blocker and separately approved amendment before introduction. Keep all unrelated Login, OTP timer, Country Picker, 6 Dialogs, Splash and Backend behavior untouched.

## 4. Acceptance
- Toast only on successful Resend; resend challenge and countdown commit first; failures use existing dialogs.
- White card / small green badge / white tick, dark-green two-line text, **no close icon**; **white track** with **green growth from 0 to 100**, exactly one notification; auto-dismiss at duration and on background expiry. Respect system safe insets / portrait-landscape content wrap.
- Booking/KYC later can author different `ToastPresentation` JSON (not new Kotlin classes) and use `present` from loaded screen or successful response.
- No hardcoded presentation colors/sizes outside JSON. Unit tests and Android/Web builds must actually pass; device screenshot comparison is separate owner gate; do not claim runtime success without tests.

**Consolidated command (Windows):**
```powershell
.\gradlew.bat :domain:jvmTest :data:jvmTest :feature:dynamic:jvmTest :feature:splash:jvmTest :app:jvmTest :androidApp:assembleDevelopmentDebug :webApp:wasmJsBrowserDevelopmentWebpack
.\gradlew.bat :androidApp:installDevelopmentDebug
```

## Implementation status — 2026-10-26

The frozen Frontend changes were authored to `architecture/dynamic-runtime-v2-discussion-freeze` after this freeze: typed Domain/Mapper ToastPresentation, request successPresentationId routing with ordered effect, app-root retained monotonic expiry, nonmodal DynamicPresentationHost branch with white track/green 0→100 fill, canonical OTP v3 JSON and Web mirror, tightly cropped success-check PNG asset, regression tests. **Not yet compiled or executed in this environment**; Gradle + Samsung A35 owner verification is the next required gate. Backend remains untouched. Confirm actual run before changing status from implemented-source/pending-test to verified.

**Preview country-code limitation:** The OTP v3 preview explicitly authors '+91 ' just as its existing OTP display does; the common Toast renderer never hardcodes a dial code. Production formatted recipient from the server or shared phone-state contract must be reconciled during Backend migration. The production request handler preserves the existing OTP challenge/cooldown contract.

## Subsequent owner acceptance — 2026-10-26 (supersedes earlier implementation-status checkpoint)

The owner reported Android Development Debug successfully built/installed on Samsung A35 after the preview module-dependency fix and personally verified BOTH Login initial Send OTP success Toast and OTP Resend success Toast, as well as rotation with **continuing progress** and no dismiss/restart. This closes the current **preview/UI owner acceptance** gate for Toast V1/V1.1. For exact navigating-success scope and evidence see Doc 162; this does NOT certify live production Backend, TLS classification, verified current-HEAD consolidated JVM/Web tests, or unrelated process-death cases. Preserve the approved white card, small green tick, no X, white track and green 0→100% elapsed timer. No further Toast redesign is in scope.
