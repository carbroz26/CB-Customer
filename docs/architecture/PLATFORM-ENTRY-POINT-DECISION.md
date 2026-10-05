# CB-Partner — Platform Entry-Point Decision

**Status:** FROZEN
**Date:** 2026-10-05
**Scope:** Android, iOS, Web application entry boundaries
**Branch:** `feature/dynamic-ui`

## 1. Decision

CB-Partner uses a **single shared application root** in the shared `:app` module. Each supported platform owns only its native/platform entry point and immediately launches the shared `App()`.

```text
Android ──┐
          │
 iOS ─────┼──> shared :app -> App() -> ApplicationContent
          │                              │
 Web ─────┘                              ├── Splash / Bootstrap
                                         ├── Navigation
                                         └── Dynamic
```

The native/platform entry point is an adapter/host boundary. It is **not an application screen** and must not own application flow or business logic.

## 2. Android Entry Boundary

`androidApp` is a thin Android application boundary.

```text
Android OS
    ↓
MainActivity
    ↓
setContent { App() }
    ↓
shared :app
```

`MainActivity` is required as the Android launcher/application entry boundary, but it must remain minimal.

### MainActivity must NOT own

- Splash flow
- Bootstrap logic
- Dynamic logic
- Login/OTP/profile/business flows
- Template selection
- Dynamic navigation decisions
- API calls
- Business state

There is no separate `SplashActivity`, `LoginActivity`, `OtpActivity`, `DynamicActivity`, or template-specific Activity.

## 3. iOS Entry Boundary

`iosApp` remains a **native Xcode boundary** and is not a synthetic Gradle module.

```text
iOS native @main entry
    ↓
ComposeUIViewController { App() }
    ↓
shared :app
```

The native iOS entry must remain thin and must not duplicate application flow.

## 4. Web Entry Boundary

`webApp` is the Web platform boundary.

```text
Web main()
    ↓
App()
    ↓
shared :app
```

The Web entry point must not create a separate Web application architecture or duplicate Splash, Bootstrap, Navigation, or Dynamic flow.

## 5. Shared Application Root

The shared `:app` module owns application composition and the common application flow.

```text
:app
  ↓
App()
  ↓
ApplicationContent
  ↓
Splash / Bootstrap
  ↓
Navigation
  ↓
Dynamic Container / Dynamic UI
```

The same shared flow is used regardless of whether the application was entered from Android, iOS, or Web.

## 6. Splash Boundary

Splash is an application feature, not a platform entry point.

The platform launches `App()`. The shared application root decides that the initial application state is Splash/Bootstrap.

Therefore the architecture is:

```text
Android MainActivity ─┐
iOS native entry ──────┼──> App() ──> Splash/Bootstrap
Web main() ───────────┘
```

and NOT:

```text
Android -> SplashActivity
 iOS    -> iOS Splash flow
 Web    -> Web Splash flow
```

## 7. Dynamic Boundary

Dynamic templates are shared Compose UI instances, not native platform screens.

Examples such as `form_template` are rendered inside the shared Dynamic Container. They must never become Android Activities, iOS view-controller screens, or Web-specific screens.

```text
Platform Entry
    ↓
Shared App
    ↓
Splash / Bootstrap
    ↓
Dynamic Container
    ↓
Template Instance
    ↓
Dynamic API / response
    ↓
Template rendering
```

## 8. Explicitly Rejected Approaches

The following are rejected by this decision:

- `SplashActivity`
- `LoginActivity`
- `OtpActivity`
- `DynamicActivity`
- template-specific Android Activities
- platform-specific application startup flows
- platform-specific Dynamic flows
- separate navigation implementations per platform
- a Gradle `:iosApp` module
- a `desktopApp` module

The project has `webApp`; no `desktopApp` is being introduced.

## 9. Why This Boundary

The purpose of the native/platform entry point is only to satisfy the host platform and hand control to the shared application.

This gives the project one application flow:

```text
                    Shared Application Root
                             │
                  ┌──────────┴──────────┐
                  ↓                     ↓
             Application Flow       Platform Host
                  │                     │
        Splash / Bootstrap              │
        Navigation                      │
        Dynamic                         │
                  ↑                     │
                  └─────────────────────┘
```

The result is that Android, iOS, and Web cannot gradually develop different application architectures simply because they have different native entry mechanisms.

## 10. Implementation Rule

Before implementation, the existing `:app`, `androidApp`, `iosApp`, and `webApp` boundaries must be audited against this frozen decision.

Implementation must:

1. Preserve the shared `:app` Application Root.
2. Add only the minimum missing platform entry code.
3. Keep platform entry points thin.
4. Reuse the existing Navigation module.
5. Reuse the existing Splash and Dynamic architecture.
6. Avoid introducing new application flow abstractions.
7. Avoid unrelated cleanup.
8. Verify Android, iOS, and Web entry boundaries after implementation where the host platform permits.

## 11. Audit and Implementation Result

The existing platform boundaries were audited before implementation.

### Existing `:app`

The shared `App()` already composes the existing Koin modules and delegates to `ApplicationContent`. `ApplicationContent` already owns the shared Splash → Dynamic application flow. No replacement application root was introduced.

### Existing `webApp`

`webApp` already had the correct thin entry point:

```kotlin
fun main() {
    ComposeViewport(viewportContainerId = "webApp") {
        App()
    }
}
```

No Web application-flow changes were required.

### Existing `androidApp`

The module previously contained only its convention-plugin Gradle file and had no manifest or source entry point. The minimum Android boundary was added:

```text
androidApp/
└── src/main/
    ├── AndroidManifest.xml
    └── kotlin/com/carbroz/cbpartner/android/MainActivity.kt
```

`MainActivity` only calls `App()` through `setContent`.

### Existing `iosApp`

The native Xcode boundary existed, but its entry point previously installed an empty `UIViewController`. It was changed to launch the shared Compose application through a shared iOS `MainViewController()` wrapper.

The shared `:app` module now exposes an iOS framework named `CBPartnerApp`, and the Xcode project uses Kotlin's direct `embedAndSignAppleFrameworkForXcode` integration.

### Implementation status

```text
Decision                 FROZEN
Audit                    COMPLETE
Android boundary         IMPLEMENTED
Web boundary             ALREADY CORRECT
iOS boundary             IMPLEMENTED
Verification              PENDING LOCAL BUILD/RUN
```

## 12. Frozen Outcome

This document is the authoritative decision record for platform application entry points.

**Decision: FROZEN.**

The platform implementation now follows the frozen boundary. Verification must be completed on the supported host platforms before this work is marked VERIFIED.
