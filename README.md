# CB-Partner

CB-Partner is the CarBroz partner app built with Kotlin Multiplatform and Compose Multiplatform.

Use this README to sync the code and run Android/Web locally. Architecture and build policy remain in the authoritative documents linked below.

## Project setup

**Current working branch:** `architecture/dynamic-runtime-v2-discussion-freeze`  
**Gradle Wrapper:** 9.7.1  
**JDK:** 17

On Windows, open PowerShell in `D:\CarBroz\CB-Partner`.

~~~powershell
git switch architecture/dynamic-runtime-v2-discussion-freeze
git fetch origin --prune
git pull --ff-only origin architecture/dynamic-runtime-v2-discussion-freeze
java -version
.\gradlew.bat --version
~~~

Use the repository Gradle Wrapper (`gradlew.bat` on Windows, `./gradlew` on macOS/Linux) for normal builds and tests.

## Android — physical device over Wi-Fi

### 1. Pair and connect with Wireless debugging

1. Connect the phone and development PC to the same reachable Wi-Fi network.
2. On the phone, enable **Developer options → Wireless debugging**.
3. Tap **Pair device with pairing code**. Use the IP and **pairing port** shown in that dialog:

   ~~~powershell
   adb pair PHONE_IP:PAIRING_PORT
   ~~~

   Enter the pairing code when prompted.
4. Return to the main Wireless debugging screen. Use its IP and **connection port** (this port can differ from the pairing port):

   ~~~powershell
   adb connect PHONE_IP:CONNECTION_PORT
   adb devices -l
   ~~~

The phone should show status `device`. If the same phone appears more than once, target one transport explicitly in the commands below. Pairing and connection ports are displayed by Android; do not guess them.

### 2. Choose the backend connection method

The development backend listens on port `3000`. A real phone **cannot** use the Android emulator's default `http://10.0.2.2:3000`. Use either of these **development-only** configurations:

- **Recommended when the phone can connect to ADB over Wi-Fi, but direct LAN API requests are blocked:** ADB reverse. It forwards the phone's localhost port 3000 through ADB to the development PC. Build the Android APK with `http://127.0.0.1:3000`. Keep ADB connected; repeat `adb reverse` after disconnect/reconnect.
- **Direct LAN:** Find the PC's active IPv4 address using `ipconfig`, verify the phone can open `http://PC_LAN_IP:3000`, then build with `http://PC_LAN_IP:3000`. Phone and PC must be on a reachable network; Windows Firewall and router client isolation can prevent access even if the PC can access its own LAN IP.

For the ADB reverse workflow:

~~~powershell
$device = "PHONE_IP:CONNECTION_PORT" # copy serial exactly from adb devices -l
adb -s $device reverse tcp:3000 tcp:3000
adb -s $device reverse --list
~~~

The expected mapping is `tcp:3000 tcp:3000`. On the phone's Chrome browser, `http://127.0.0.1:3000/api/v1/partner/config/bootstrap` can verify connectivity. **A 500 `Missing update config for undefined` for a browser GET means the request lacks the app's platform header; it does not by itself indicate transport failure.** The app adds `X-CarBroz-Platform` and version headers. On the development PC, a diagnostic request is:

~~~powershell
curl.exe -i -H "X-CarBroz-Platform: ANDROID" -H "X-CarBroz-App-Version: 1.0.0" -H "X-CarBroz-Build-Number: 1" http://127.0.0.1:3000/api/v1/partner/config/bootstrap
~~~

Use the actual app version/build for a production-equivalent check. Never log actual identity, session tokens, phone/OTP values, or response bodies.

### 3. Build, install, and launch on the selected physical device

From the repository root in **Antigravity's PowerShell terminal**:

~~~powershell
$device = "PHONE_IP:CONNECTION_PORT" # e.g. the current Wi-Fi serial listed by adb
adb -s $device reverse tcp:3000 tcp:3000
.\gradlew.bat :androidApp:assembleDebug "-Pcbpartner.api.baseUrl=http://127.0.0.1:3000"
adb -s $device install -r .\androidApp\build\outputs\apk\debug\androidApp-debug.apk
adb -s $device shell am force-stop com.carbroz.cbpartner.android
adb -s $device shell monkey -p com.carbroz.cbpartner.android -c android.intent.category.LAUNCHER 1
~~~

For direct LAN **instead**, replace the Gradle property with `"-Pcbpartner.api.baseUrl=http://PC_LAN_IP:3000"` and omit `adb reverse`. The Android Gradle convention defaults to emulator `10.0.2.2` when the property is not provided. Rebuild **and reinstall** if you change the base URL. `assembleDebug` alone does not update an APK already installed on the device.

Always include `adb -s $device`: Wireless debugging can show the same physical Samsung device twice, through an IP connection and an mDNS/TLS connection. `adb install` without `-s` then fails with `more than one device/emulator`. Never guess pairing and connection ports.

### 4. Android logging in Antigravity's terminal

Open a **second terminal** and run this while the app is open:

~~~powershell
$device = "PHONE_IP:CONNECTION_PORT"
$package = "com.carbroz.cbpartner.android"
$appPid = adb -s $device shell pidof -s $package
if ($appPid) {
    adb -s $device logcat --pid=$($appPid.Trim()) -v time
} else {
    Write-Host "App is not running: launch it, then re-run PID capture"
}
~~~

`Ctrl+C` stops live logs. The PID changes after restart; rerun the command. If an early crash prevents PID lookup, **before launching**, collect broader errors:

~~~powershell
adb -s $device logcat -c
adb -s $device logcat -v time | Select-String 'AndroidRuntime|FATAL EXCEPTION|CBPartner|Network|ConnectException|UnknownHost|Cleartext'
~~~

Then launch in the first terminal. Use **`adb -s $device logcat`**, not `adb -s $device -c` or an empty `$device` variable.

The Android-only process filter avoids unrelated Samsung/system logs. `SLF4J(W): No SLF4J providers were found` can occur from third-party logging and is **not proof that the API failed**. Ktor's raw transport body/header logging must stay disabled to protect tokens, OTPs, phone numbers and other user data; use only privacy-safe diagnostic events supported by the project.

### 5. Run/debug from Antigravity

- Keep the phone paired over Wireless debugging; confirm its exact serial with `adb devices -l`.
- Use the **Terminal** tab for the Gradle/build/ADB commands above and a second terminal for Logcat. The **Output** panel does not automatically stream app Logcat without an IDE/device-log integration.
- If selecting an IDE run target, ensure it points to the intended ADB transport and correct Gradle URL configuration. Otherwise install explicitly with `adb -s` as above.
- On a reconnect, re-establish `adb reverse`, then relaunch; if an API attempt appears to fail, test Backend connectivity and confirm the installed APK's configured URL before changing app source.

## Web — browser development server

From the repository root, run:

~~~powershell
.\gradlew.bat :webApp:wasmJsBrowserDevelopmentRun
~~~

Open the URL printed by Gradle. Keep the terminal running while testing; press `Ctrl+C` to stop the server. Use browser Developer Tools (`F12`), especially **Console** and **Network**, to inspect errors and API requests.

### Web API address

Web does not use the Android Gradle property above. By default, the browser hostname is used with port `3000`: a page opened on `localhost` targets `localhost:3000`, while a page opened through the PC's LAN hostname targets that hostname on port `3000`.

If the API is hosted at a different origin, set the non-empty `cbpartner-api-base-url` meta value in `webApp/src/wasmJsMain/resources/index.html` to the reachable API origin and configure backend CORS for the Web origin. Do not mix an HTTPS page with an HTTP API.

## iOS — macOS and Xcode only

`iosApp/` is the native Xcode application boundary, not a Gradle `:iosApp` module. Native iOS builds and simulator runs require macOS and Xcode; Windows cannot run the iOS simulator.

1. On macOS, open the Xcode project/workspace inside `iosApp/`.
2. Select the `iosApp` scheme and an iPhone simulator or connected iPhone, then press **Cmd+R**.
3. To use a backend running on the Windows development PC, set Xcode Build Settings `CBPARTNER_API_BASE_URL` to `http://PC_LAN_IP:3000`, replacing `PC_LAN_IP` with the PC's actual LAN IPv4 address. The iPhone and PC must be able to reach each other; approve the iOS Local Network prompt if shown.
4. For production, use the deployed HTTPS API origin. Do not disable App Transport Security globally.

## Useful checks

Run the focused JVM regression suite:

~~~powershell
.\gradlew.bat :network:jvmTest :data:jvmTest :domain:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest
~~~

Discover available tasks:

~~~powershell
.\gradlew.bat projects
.\gradlew.bat tasks --all
~~~

Windows may report that iOS simulator tests are disabled because they require macOS; this is an expected host limitation.

## Project structure

- `:app` — shared application composition
- `:androidApp` — Android application boundary
- `:webApp` — Web/Wasm application boundary
- `:core`, `:network`, `:data`, `:domain`, `:navigation` — shared infrastructure and layers
- `:feature:splash`, `:feature:dynamic` — shared features
- `iosApp/` — native Xcode/iOS boundary

There is no `:desktopApp` Gradle module on `architecture/dynamic-runtime-v2-discussion-freeze`.

## Authoritative project documents

- `docs/build/GRADLE-SETUP-AND-WORKFLOW.md` — Gradle version, platform API configuration, build/test guidance
- `docs/project-management/07-DEVELOPMENT-WORKFLOW.md` — development, testing, and review gates
- `docs/dynamic/DYNAMIC-ARCHITECTURE.md` — frozen Dynamic UI architecture
- `docs/dynamic/DYNAMIC-IMPLEMENTATION-PLAN.md` — Dynamic UI implementation plan
- `docs/dynamic/DYNAMIC-IMPLEMENTATION-STATUS.md` — current Dynamic UI status

Keep this README focused on setup and running the apps. Do not commit machine-specific IP addresses, local IDE/build outputs, or secrets.
