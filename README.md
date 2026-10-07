# CB-Partner

CB-Partner is the CarBroz partner application built with Kotlin Multiplatform and Compose Multiplatform.

This README is the first-time setup and run guide for developers/agents working with the repository.

> **Important:** The repository contains Android, iOS, Desktop, and Web targets. The `iosApp/` directory is the native Xcode/iOS application boundary; it is not a Gradle `:iosApp` module.

---

## 1. Repository

Frontend repository:

- GitHub: https://github.com/carbroz26/CB-Partner
- Main development branch: `development`
- Dynamic UI development branch: `feature/dynamic-ui`

Clone the repository:

```powershell
git clone https://github.com/carbroz26/CB-Partner.git
cd CB-Partner
```

If you need a specific development branch:

```powershell
git fetch origin --prune
git checkout development
git pull --ff-only origin development
```

For the Dynamic UI work:

```powershell
git fetch origin --prune
git checkout feature/dynamic-ui
git pull --ff-only origin feature/dynamic-ui
```

Check the repository state:

```powershell
git status --short
git branch --show-current
git log -5 --oneline
```

---

## 2. Required Development Environment

### Common

Install/configure:

- Git
- JDK 17
- Android Studio for Android development
- Xcode on macOS for iOS development
- A supported browser for Web development

The project currently uses:

- Gradle **9.7.1**
- Kotlin **2.4.0**
- JDK **17**

### Windows

Android and Web development can be performed on Windows.

Verify Java:

```powershell
java -version
```

The output should report Java 17.

The repository uses the Gradle Wrapper. **Do not install or depend on a different global Gradle version for normal project builds.**

Verify the wrapper:

```powershell
.\gradlew.bat --version
```

Expected Gradle version:

```text
Gradle 9.7.1
```

If the wrapper is not executable from a fresh checkout, verify that `gradlew.bat`, `gradle/wrapper/`, and the wrapper files are present before changing project configuration.

### macOS

Install Xcode and its command-line tools:

```bash
xcode-select --install
```

Verify:

```bash
java -version
xcodebuild -version
```

Open Xcode once and accept the required license/setup prompts.

---

## 3. First-Time Project Setup

After cloning:

### Step 1 — Verify Git

```powershell
git --version
```

### Step 2 — Verify Java

Windows:

```powershell
java -version
```

macOS/Linux:

```bash
java -version
```

Use JDK 17.

### Step 3 — Verify Gradle Wrapper

Windows:

```powershell
.\gradlew.bat --version
```

macOS/Linux:

```bash
./gradlew --version
```

### Step 4 — Inspect Gradle Projects

Windows:

```powershell
.\gradlew.bat projects
```

macOS/Linux:

```bash
./gradlew projects
```

The expected Gradle modules include:

```text
:androidApp
:core
:data
:desktopApp
:domain
:feature:dynamic
:feature:splash
:navigation
```

The `iosApp/` directory is a native Xcode boundary and is intentionally not listed as a Gradle module.

### Step 5 — Build/Test the Shared Modules

Windows:

```powershell
.\gradlew.bat :core:jvmTest :domain:jvmTest :data:jvmTest :navigation:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest
```

Or run the complete frozen Dynamic UI test matrix:

```powershell
.\gradlew.bat :data:jvmTest :network:jvmTest :navigation:jvmTest :feature:dynamic:jvmTest
```

macOS/Linux:

```bash
./gradlew :core:jvmTest :domain:jvmTest :data:jvmTest :navigation:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest
```

---

## 4. Web — Recommended First Run

Web is the easiest way to verify the application flow without an Android emulator or iOS device.

From the repository root:

```powershell
.\gradlew.bat :webApp:wasmJsBrowserDevelopmentRun
```

On macOS/Linux:

```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

The task starts the Web development server and opens/serves the Web application.

If the browser does not open automatically, use the local URL printed by Gradle.

### Stop the Web application

Press:

```text
Ctrl+C
```

### Web debugging

The Web application executes in the browser. Browser-side logs may appear in the browser's Developer Tools console rather than the Gradle terminal.

Open browser Developer Tools:

```text
F12
```

Then select the **Console** and **Network** tabs when debugging API calls or runtime behavior.

---

## 5. Android — Run from Android Studio

Android development requires Android Studio, an Android SDK, and either a physical Android device or an Android emulator.

### Open the project

Open the repository root in Android Studio:

```text
D:\CarBroz\CB-Partner
```

Android Studio should import the Gradle project using the repository's Gradle Wrapper.

### Verify Android Gradle tasks

From the repository root:

```powershell
.\gradlew.bat :androidApp:tasks
```

### Build the Android application

```powershell
.\gradlew.bat :androidApp:assembleDebug
```

### Install the Debug application on a connected device/emulator

With an Android device or emulator available:

```powershell
.\gradlew.bat :androidApp:installDebug
```

You can then launch the application from Android Studio or the device.

### Android Studio run

1. Open the project root.
2. Wait for Gradle sync to finish.
3. Select the Android application run configuration.
4. Select an emulator or connected Android device.
5. Press **Run**.

If Android SDK or emulator components are missing, install them through Android Studio's SDK Manager/Device Manager rather than changing project Gradle configuration.

---

## 6. iOS — Run from Xcode

iOS development requires **macOS + Xcode**.

The repository intentionally keeps `iosApp/` as the native iOS/Xcode boundary. Do not create or expect a Gradle `:iosApp` module.

### Open the iOS application

From the repository root on macOS:

```bash
open iosApp
```

If Xcode does not open the project/workspace automatically, open the Xcode project/workspace contained inside `iosApp/` from Finder or Xcode.

### Select a simulator/device

In Xcode:

1. Select the `iosApp` application scheme.
2. Select an available iPhone simulator or connected iPhone.
3. Build and Run.

Use:

```text
Cmd + R
```

### iOS shared-code verification

Kotlin Multiplatform iOS compilation/tests must be executed on macOS because the iOS toolchain is provided by Xcode.

For example:

```bash
./gradlew compileKotlinIosArm64
```

The exact iOS target/task can vary with the selected device/simulator architecture; use Gradle task discovery when needed:

```bash
./gradlew tasks --all
```

**Windows note:** iOS simulator tasks are disabled on Windows because the Apple toolchain is unavailable. This is expected and is not a project test failure.

---

## 7. Desktop

The repository also contains a Desktop application.

Inspect available Desktop tasks:

```powershell
.\gradlew.bat :desktopApp:tasks
```

Use the corresponding generated run task shown by Gradle.

For example, when available:

```powershell
.\gradlew.bat :desktopApp:run
```

---

## 8. Useful Gradle Commands

### List all projects

Windows:

```powershell
.\gradlew.bat projects
```

macOS/Linux:

```bash
./gradlew projects
```

### List all tasks

```powershell
.\gradlew.bat tasks --all
```

### Clean

```powershell
.\gradlew.bat clean
```

### Build

```powershell
.\gradlew.bat build
```

### Run tests

```powershell
.\gradlew.bat test
```

### Run a specific module's JVM tests

```powershell
.\gradlew.bat :data:jvmTest
```

### Run Dynamic UI tests

```powershell
.\gradlew.bat :data:jvmTest :network:jvmTest :navigation:jvmTest :feature:dynamic:jvmTest
```

### See available tasks for a module

```powershell
.\gradlew.bat :androidApp:tasks
.\gradlew.bat :desktopApp:tasks
.\gradlew.bat :feature:dynamic:tasks
```

---

## 9. Git Workflow

Before starting work:

```powershell
git status --short
git fetch origin --prune
git pull --ff-only origin <your-branch>
```

Check the current branch:

```powershell
git branch --show-current
```

Check recent commits:

```powershell
git log -10 --oneline
```

After making and testing a change:

```powershell
git status --short
git diff
git diff --check
```

Commit:

```powershell
git add <files>
git commit -m "type(scope): description"
```

Push:

```powershell
git push origin <your-branch>
```

Refresh remote references:

```powershell
git fetch origin --prune
```

For this project, do not force-push, reset, rebase, or merge unrelated branches unless the task explicitly requires it.

---

## 10. Clean Checkout / Recovery

If you have no local work that needs to be preserved and need to synchronize a branch with the remote:

```powershell
git fetch origin --prune
git status --short
git pull --ff-only origin <your-branch>
```

Do not use destructive commands such as `git reset --hard` unless you have explicitly confirmed that all local changes can be discarded.

If Gradle state appears stale, first try:

```powershell
.\gradlew.bat clean
```

Then rerun the required build/test command.

---

## 11. Project Structure

The main frontend structure is:

```text
CB-Partner/
├── androidApp/       # Native Android application boundary
├── iosApp/           # Native iOS/Xcode application boundary
├── desktopApp/       # Desktop application
├── webApp/           # Web application
├── build-logic/      # Gradle convention/build logic
├── core/             # Shared core infrastructure
├── data/             # Data layer
├── domain/           # Domain layer
├── navigation/       # Navigation
├── feature/
│   ├── splash/       # Splash feature
│   └── dynamic/      # Dynamic UI feature
├── docs/              # Project architecture and implementation documentation
├── gradle/             # Gradle configuration/wrapper support
├── gradlew
├── gradlew.bat
└── settings.gradle.kts
```

Architecture and implementation rules are documented separately under `docs/`. This README is only the developer setup/run entry point; it does not replace the authoritative architecture documents.

---

## 12. Dynamic UI Documentation

For Dynamic UI work, use the authoritative documents in this order:

1. `docs/dynamic/DYNAMIC-ARCHITECTURE.md`
2. `docs/dynamic/DYNAMIC-IMPLEMENTATION-PLAN.md`
3. `docs/dynamic/DYNAMIC-IMPLEMENTATION-STATUS.md`

Older Dynamic documentation under historical feature/reconciliation locations must not override the authoritative documents above.

---

## 13. First-Time Verification Checklist

After cloning, a developer should be able to complete this sequence:

### Common

```powershell
git clone https://github.com/carbroz26/CB-Partner.git
cd CB-Partner
git status --short
java -version
.\gradlew.bat --version
.\gradlew.bat projects
```

### Shared tests

```powershell
.\gradlew.bat :data:jvmTest :network:jvmTest :navigation:jvmTest :feature:dynamic:jvmTest
```

### Web

```powershell
.\gradlew.bat :webApp:wasmJsBrowserDevelopmentRun
```

### Android

```powershell
.\gradlew.bat :androidApp:assembleDebug
.\gradlew.bat :androidApp:installDebug
```

### iOS

On macOS:

```bash
open iosApp
```

Then run the `iosApp` scheme from Xcode.

---

## 14. Important Notes

- Prefer the repository Gradle Wrapper over a globally installed Gradle version.
- Use JDK 17 for the project.
- Do not add an artificial Gradle `:iosApp` module. `iosApp/` is the native Xcode boundary.
- Windows cannot execute iOS simulator tests; run iOS builds/tests on macOS.
- Web runtime logs are browser-side; use browser Developer Tools when terminal output is not available.
- Do not commit local IDE/build artifacts or unrelated files.
- Keep architecture changes governed by the project's documented discussion/research/decision/plan/implementation gates.
