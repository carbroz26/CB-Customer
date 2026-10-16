# 86 — G1 Retained Cleanup Experiment

**Status:** Experiment only, not production implementation; result pending device/browser logs.
**Base:** frozen plan 85. No changes to App, Koin, Stores, navigation, action or backend.

Existing opt-in `RetainCompatibilityProbe` now compares a `DisposableEffect` attach/detach log with a `RetainedEffect` retained/retired log, keyed to the same `retain` identity.

## Expected / evidence to collect
- Recompose: same identity, counter increases, no additional retained/retired events.
- Android physical rotation: same identity; detach/attach may fire; `event=retired` must **not** fire for that identity between rotations if host retains it.
- Remove proof content: normal composition removal is NOT a transient configuration change. Observe `event=retired` for removed identity and check it fires once; Show proof content constructs a new identity. Cleanup can be deferred by retention mechanisms; record what actually happens.
- Android force-stop: process death does not guarantee a retirement callback. Do not treat absence as a leak.
- Web recompose/resize: same identity, no retirement. Hide/show may retire; browser refresh creates a new page and does not imply callback before termination.
- The observed callback lifecycle verifies isolated Compose-retained objects **only**. Koin recreation, closed-graph safety and Store dependency injection require distinct follow-on tests; no Koin graph is retained by this probe.
- iOS runtime remains unverified.

## Android commands
```powershell
git pull --ff-only origin architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :androidApp:assembleDebug :webApp:wasmJsBrowserDevelopmentWebpack
$device = "adb-RZCX5118SMW-8yXAeX._adb-tls-connect._tcp"
adb -s $device install -r .\androidApp\build\outputs\apk\debug\androidApp-debug.apk
adb -s $device shell am force-stop com.carbroz.cbpartner.android
adb -s $device logcat -c
adb -s $device shell am start -n com.carbroz.cbpartner.android/.MainActivity --ez cbpartner.retention_probe true
adb -s $device logcat -d -v time | Select-String 'CBPartnerRetention|AndroidRuntime'
```
Record screenshot identity before/after each operation and filtered log. Do not log any business data.
## Web
Start `:webApp:wasmJsBrowserDevelopmentRun`, open `?cbpartnerRetentionProbe=1`, use browser console and the Remove/Show buttons. Browser resize is not viewport destruction.
