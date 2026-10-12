# CB-Partner

CB-Partner is the CarBroz partner app built with Kotlin Multiplatform and Compose Multiplatform.

Use this README to sync the code and run Android/Web locally. Architecture and build policy remain in the authoritative documents linked below.

## Project setup

**Current working branch:** `feature/dynamic-ui`  
**Gradle Wrapper:** 9.7.1  
**JDK:** 17

On Windows, open PowerShell in `D:\CarBroz\CB-Partner`.

~~~powershell
git switch feature/dynamic-ui
git fetch origin --prune
git pull --ff-only origin feature/dynamic-ui
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

### 2. Configure the backend address

Find the development PC's LAN IPv4 address with:

~~~powershell
ipconfig
~~~

Use the IPv4 address of the active Wi-Fi/Ethernet adapter—not the phone's IP. Ensure the backend is running on port `3000`, listening on a LAN-reachable interface, and allowed through Windows Firewall. Keep the phone and PC on a network that can reach each other.

The address below is an example only. Replace `192.168.1.25` with the PC's current LAN IP; do not commit a personal LAN address.

### 3. Build, install, and launch

~~~powershell
.\gradlew.bat "-Pcbpartner.api.baseUrl=http://192.168.1.25:3000" :androidApp:assembleDebug
~~~

Set `$device` to the phone's IP and **connection port** from the Wireless debugging screen. The serial must match one entry from `adb devices -l`.

~~~powershell
$device = "PHONE_IP:CONNECTION_PORT"
adb -s $device install -r .\androidApp\build\outputs\apk\debug\androidApp-debug.apk
adb -s $device shell monkey -p com.brocarz.cbpartner.android 1
~~~

If installation reports “more than one device/emulator”, always include `-s $device` with ADB commands. `adb install` installs the app but does not open it; the `monkey` command launches its launcher activity.

### 4. Run/debug from Antigravity

- Keep Wireless debugging enabled and verify the device with `adb devices -l`.
- In Antigravity, select the connected phone in the Android device/run target selector and run/debug the Android app.
- If the device is missing, confirm ADB shows it as `device`, then refresh/reselect the target.

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

There is no `:desktopApp` Gradle module on `feature/dynamic-ui`.

## Authoritative project documents

- `docs/build/GRADLE-SETUP-AND-WORKFLOW.md` — Gradle version, platform API configuration, build/test guidance
- `docs/project-management/07-DEVELOPMENT-WORKFLOW.md` — development, testing, and review gates
- `docs/dynamic/DYNAMIC-ARCHITECTURE.md` — frozen Dynamic UI architecture
- `docs/dynamic/DYNAMIC-IMPLEMENTATION-PLAN.md` — Dynamic UI implementation plan
- `docs/dynamic/DYNAMIC-IMPLEMENTATION-STATUS.md` — current Dynamic UI status

Keep this README focused on setup and running the apps. Do not commit machine-specific IP addresses, local IDE/build outputs, or secrets.
