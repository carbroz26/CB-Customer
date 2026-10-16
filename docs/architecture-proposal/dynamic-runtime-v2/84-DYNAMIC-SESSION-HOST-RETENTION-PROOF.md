# DYNAMIC-SESSION-LIFECYCLE-001 — Host Retention Proof

**Status:** Isolated experiment, NOT a production implementation and NOT an architecture freeze.
**Scope:** commonMain probe + opt-in Android debug host + opt-in Web host. iOS is not yet runnable here.

## Observation
A common composable calls `retain { Uuid.random().toString() }`, displays the stable identity, and offers a recomposition button. Console shows attached/detached identity. No backend, DI, navigation, Store, or sensitive state is involved. The counter is intentionally `remember`-owned as a non-retained control.

## Android host experiment
Build/debug install. Launch with the intent boolean extra:
```powershell
adb -s $device shell am force-stop com.carbroz.cbpartner.android
adb -s $device shell am start -n com.carbroz.cbpartner.android/.MainActivity --ez cbpartner.retention_probe true
```
Capture `adb -s $device logcat -d -v time | Select-String 'CBPartnerRetention|AndroidRuntime'`.
Tap Recompose, note the retained UUID, rotate the physical device, and compare the UUID. Distinguish real Activity recreation (check lifecycle/identity) from mere viewport resize.
Normal launch without extra continues to use App unchanged.

## Web experiment
Run the Wasm browser dev server and open the served app page with `?cbpartnerRetentionProbe=1`. Read browser console for `CBPartnerRetention`. Tap Recompose, then test browser viewport resize. A hard refresh destroys the JS page context and is NOT expected to retain in-memory identity. To assert a genuine host *recreation*, a separate controlled ComposeViewport teardown/recreation harness would be required; a browser resize does not prove that case.

## Interpretation
An unchanged ID after a real host recreation demonstrates object retention only for that host and lifecycle; a changed ID shows this host did not retain it. Neither result establishes navigation/screen state preservation automatically. iOS ComposeUIViewController host recreation is untested. No business-specific assumptions or storage persistence.

## Exit
Collect Android device and Web console observations, settle platform-specific host coverage, remove this probe and opt-in code before production implementation. Run Android and Web compilers first, without claiming iOS native compilation on Windows.
