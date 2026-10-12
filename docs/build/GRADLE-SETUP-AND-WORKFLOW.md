# CB-Partner — Gradle Setup and Workflow

**Status:** AUTHORITATIVE
**Scope:** Frontend repository
**Last Updated:** 2026-10-12

## 1. Purpose

This is the single authoritative operational document for Gradle in CB-Partner. It defines the selected version, Wrapper usage, Windows setup, normal build/test commands, current project topology, troubleshooting and version-change procedure.

Do not maintain competing Gradle setup instructions in other documents.

## 2. Authoritative Version

**CB-Partner Gradle version: 9.7.1**

| Item | Value |
|---|---|
| Selected Gradle | **9.7.1** |
| Wrapper target | **9.7.1** |
| Windows normal build | `.\gradlew.bat` |
| macOS/Linux normal build | `./gradlew` |
| System Gradle | Bootstrap/update Wrapper only |
| Project build authority | Gradle Wrapper |

Gradle 9.5.0 is superseded and is not the project baseline.

## 3. Core Rule

```text
System Gradle
    ↓
bootstrap / generate / update Wrapper only

Gradle Wrapper 9.7.1
    ↓
all normal CB-Partner builds, tests and checks
```

Do not use a different system Gradle for ordinary project builds.

## 4. Required Wrapper Files

```text
gradlew
gradlew.bat
gradle/wrapper/gradle-wrapper.jar
gradle/wrapper/gradle-wrapper.properties
```

The Wrapper is operational and must remain the normal project build authority.

## 5. Windows System Gradle

The project owner has verified Gradle 9.7.1 on Windows.

```powershell
$env:GRADLE_HOME = "C:\Gradle\gradle-9.7.1"
$env:Path = "$env:GRADLE_HOME\bin;$env:Path"
gradle --version
```

Expected:

```text
Gradle 9.7.1
```

Use the repository Wrapper after bootstrap.

## 6. Current Gradle Project Topology

Current `settings.gradle.kts` registers:

```text
:core
:domain
:network
:data
:navigation
:app
:feature:splash
:feature:dynamic
:androidApp
:webApp
```

`iosApp/` is a native Xcode application boundary and is not a Gradle module.

There is currently no `:desktopApp` Gradle project on `feature/dynamic-ui`. Older documentation that describes Desktop is historical foundation wording and must not be used to add a Desktop module without an explicit architecture decision.

## 7. Standard Pull / Compile / Test

```powershell
cd D:\CarBroz\CB-Partner
git fetch origin
git pull --ff-only origin feature/dynamic-ui
```

Current focused compilation:

```powershell
.\gradlew.bat :network:compileKotlinJvm
.\gradlew.bat :data:compileKotlinJvm
.\gradlew.bat :feature:splash:compileKotlinJvm
.\gradlew.bat :feature:dynamic:compileKotlinJvm
.\gradlew.bat :app:compileKotlinJvm
```

Current focused JVM regression suite:

```powershell
.\gradlew.bat :network:jvmTest :data:jvmTest :domain:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest
```

## 8. Latest Verification — 2026-10-04

The current focused compile and JVM test commands passed on `feature/dynamic-ui`:

```text
BUILD SUCCESSFUL
54 actionable tasks: 15 executed, 39 up-to-date
```

The earlier Bootstrap HTTP-status test failure was resolved by configuring the MockEngine test `HttpClient` with `expectSuccess = true`.

## 9. BASE-ARCH-014 Shared Baseline

When verifying the frozen foundation, use the approved commands from the BASE-ARCH-014 plan. Do not replace the baseline with an invented task set.

Typical shared JVM baseline:

```powershell
.\gradlew.bat :core:jvmTest :domain:jvmTest :data:jvmTest :navigation:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest
```

## 10. General Commands

```powershell
.\gradlew.bat --version
.\gradlew.bat projects
.\gradlew.bat tasks --all
.\gradlew.bat build
.\gradlew.bat check
.\gradlew.bat test
```

Use `clean` only when justified:

```powershell
.\gradlew.bat clean
```

Do not use `clean` as a routine fix for an unexplained failure.

## 11. Diagnostics

```powershell
.\gradlew.bat <task> --stacktrace
.\gradlew.bat <task> --info
```

Use `--debug` only when necessary. Diagnose the actual task failure rather than changing configuration blindly.

## 12. Platform Verification

### Android

Use the exact Android task required by the active feature plan. Do not invent task names.

### iOS

`iosApp/` is native Xcode. Gradle compilation on Windows does not constitute iOS build verification. iOS simulator testing requires macOS.

### Web/Wasm

`webApp` is the current web application boundary. Use the exact Web/Wasm task exposed by the current repository when the active feature requires it.

## 13. Platform API base URL configuration

The shared Ktor client receives an explicit `ApiBaseUrl` from the platform entry point. Do not restore a hard-coded host in `:network`.

### Android Emulator

The Android default is:

```text
http://10.0.2.2:3000
```

This address reaches port 3000 on the Windows host from the standard Android Emulator. No override is needed when the backend runs on the same Windows PC and listens on the expected port.

### Physical Android device

1. Run `ipconfig` on Windows and identify the IPv4 address of the active Wi-Fi/Ethernet adapter.
2. Ensure the backend listens on an interface reachable from the LAN, not only `127.0.0.1`.
3. Allow inbound TCP port 3000 through Windows Firewall for the trusted development network.
4. Keep the phone and PC on the same reachable network.
5. Supply the PC's actual LAN address when building/installing:

```powershell
.\gradlew.bat :androidApp:installDebug -Pcbpartner.api.baseUrl=http://192.168.1.25:3000
```

Replace `192.168.1.25` with the real PC IPv4 address. The Gradle property overrides the default for that build; do not commit a machine-specific LAN address.

Android declares `INTERNET` permission. Cleartext HTTP is enabled only in the debug manifest; release builds should use HTTPS.

### Web/Wasm

Web derives the API hostname from `window.location.hostname` and uses port 3000. Thus a browser opened at `localhost` uses `localhost:3000`, while a browser opened through the PC's LAN address uses that hostname. If the backend is served from a different origin, configure backend CORS for the Web origin. HTTPS deployments should use an HTTPS API origin; do not mix an HTTPS page with an HTTP API.

### iOS Simulator and physical iPhone

The Xcode target exposes `CBPARTNER_API_BASE_URL` as an app Info.plist value. In Xcode Build Settings, set it to the Windows PC's actual LAN URL when testing against the Windows-hosted backend, for example `http://192.168.1.25:3000`. The default `localhost` is only appropriate when the backend runs on the Mac hosting the simulator.

Physical iPhones must be able to reach the Windows PC over the LAN and approve the iOS Local Network prompt. On iOS 17 and later, HTTP connections to an IP address may also require a narrowly scoped App Transport Security exception for that exact IP. Do not disable ATS globally; prefer HTTPS for production.

### Production

Use the deployed HTTPS API origin for production on every platform. The emulator/LAN HTTP URLs are development settings only. Never commit a developer's private LAN address as a shared default.

## 14. Expected Windows Warnings

### iOS simulator

```text
iosSimulatorArm64Test is disabled
simulator tests require macOS
```

Expected on Windows. Do not suppress it merely to make logs quieter.

### Android host tests

A warning may state that `commonTest` exists but Android host tests are not enabled. This is a warning, not a failed JVM build/test.

### Kotlin redundant cast

`Cast is redundant` is a warning and does not invalidate a successful test run.

## 15. Git Safety Around Gradle Changes

Before modification:

```powershell
git status
git branch --show-current
git diff --stat
```

After modification:

```powershell
git diff --check
git diff --stat
git diff
git status
```

Never use destructive Git commands to resolve Gradle uncertainty.

## 16. Version Change Procedure

A Gradle baseline change is a tooling decision.

1. Record why the change is needed.
2. Check Kotlin/AGP/Compose/JDK compatibility.
3. Obtain explicit approval for a baseline change.
4. Update this authoritative document.
5. Update routing/status documents.
6. Update the Wrapper.
7. Run project/build/test verification.
8. Record actual results.
9. Review the diff.
10. Commit through the approved workflow.

Never downgrade/upgrade only because a different local version is installed.

## 17. Troubleshooting

### `gradle` not recognized

```powershell
$env:GRADLE_HOME
$env:Path
java -version
gradle --version
```

### Wrapper missing

Use system Gradle 9.7.1 only to generate the official Wrapper:

```powershell
gradle :wrapper --gradle-version 9.7.1 --distribution-type bin
```

### Wrong Wrapper version

```powershell
.\gradlew.bat --version
Get-Content gradle\wrapper\gradle-wrapper.properties
```

### iOS simulator warning on Windows

Expected host limitation. Do not treat it as a code failure.

## 18. Related Documents

- `AI_START_HERE.md`
- `docs/project-management/04-PROJECT-MASTER-STATE.md`
- `docs/project-management/04-PROJECT-MASTER-STATE.md`
- `docs/architecture/BASE-ARCH-014-IMPLEMENTATION-PLAN.md`
- `docs/architecture/ARCHITECTURE.md`
