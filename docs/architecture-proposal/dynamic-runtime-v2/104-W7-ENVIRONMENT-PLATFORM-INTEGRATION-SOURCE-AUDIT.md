# 104 — W7 Environment, Platform and Integration Source Audit

**Status:** READ-ONLY SOURCE AUDIT; no implementation plan frozen or code changes authorized.
**Baseline:** `28ffc9f586272f6de6da3c6181f288cdef21af99`, `architecture/dynamic-runtime-v2-discussion-freeze`.
**Authority:** docs20 W7 and docs23 sections B/C/E. This folder alone governs cleanup; other docs are contextual evidence. Current source and tests are implementation evidence, not authority to override owner decisions.

## Exact frozen requirement versus source matrix

| Requirement | Actual source evidence | Finding / narrow next gate |
|---|---|---|
| Android development/staging/production flavor dimension, separate debug/release | `build-logic/src/main/kotlin/com/carbroz/cbpartner/buildlogic/AndroidApplicationConventionPlugin.kt` sets debug/release API URL only; `androidApp/build.gradle.kts` has no flavors | **CONFIRMED MISSING** frozen flavor dimension and `.dev`/`.staging` application ID suffixes; design minimal Gradle change after owner plan freeze |
| Android explicit staging/production origin, no dummy fallback | Convention plugin defaults release URL to `https://api.invalid`; `MainActivity.kt` reads `BuildConfig.CBPARTNER_API_BASE_URL` | **CONFIRMED POLICY VIOLATION**: release packaging can select dummy fallback; require validated explicit trusted origin for staging/production, independent of debuggability |
| Web environment selection and fail-closed staging/production origin | `webApp/.../Main.kt` reads `cbpartner-api-base-url` meta tag; absent falls back to current host port 3000; `resources/index.html` meta content empty | **PARTIAL**: existing meta injection seam good, environment-aware fallback enforcement absent; don't replace shared Ktor |
| iOS environment and explicit staging/production origin | `app/src/iosMain/.../Main.kt` reads `CBPARTNER_API_BASE_URL` Info.plist key, else localhost; `iosApp/CB-Partner.xcodeproj/project.pbxproj` sets Debug localhost and Release `https://api.invalid` | **CONFIRMED GAP**: no three-environment scheme/build contract; release dummy origin; cannot run iOS on Windows |
| AppEnvironment shared immutable identifier | `network/ApiBaseUrl.kt` validates scheme, host, origin-only path/query/fragment but has no environment identity; `app/App.kt` accepts provider+URL | **MISSING**: approved small AppEnvironment must be integrated without manager/new module; preserve ApiBaseUrl injection |
| Single HTTP transport and platform wiring | `app/App.kt` creates Koin once; `network/NetworkModule.kt` binds a single HttpClient and RequestHandler; Android/Web/iOS call App | **IMPLEMENTED-SOURCE**: no parallel networking or rearchitecture justified |
| Safe diagnostics and environment levels | `network/http/RequestHandler.kt` Kermit summaries avoid URL/path/body/raw headers; `RequestHandlerLoggingTest.kt` asserts redaction for selected cases; `app/ApplicationContent.kt` logs navigation fields with Kermit | **PARTIAL**: Kermit operation logging exists; no evidenced centralized development/staging/production level configuration. Need final all-events privacy and metadata policy review, never raw payloads |
| Bootstrap → retained session and consumer | `app/App.kt` uses `retain`/RetainedEffect; `ApplicationContent.kt` uses BootstrapContent until session, then session effect claim/ack consumer; `app/DynamicSession.kt` owns lease/state/batches | **IMPLEMENTED-SOURCE / VERIFIED-SCOPED** by prior JVM tests, Android Login→OTP orientation and Web limited smoke; no G1/S2 rewrite |
| Entry response and bindings reattachment | `navigation/DynamicNavigationEntry.kt` holds entry-local `screenResponse`, `bindings`; `feature/dynamic/store/DynamicStore.kt` handles restore | **IMPLEMENTED-SOURCE**; exact zero duplicate GET on physical reattach remains unverified |
| Android/Web temporary host probe | `app/RetainCompatibilityProbe.kt` has explicit opt-in; `androidApp/MainActivity.kt` debug intent extra, `webApp/Main.kt` URL query param | **ACTIVE DEBUG ROUTE**, not dead code. W8 removal candidate after remaining lifecycle proof, no automatic deletion |
| Startup required-update store URI | `feature/splash/SplashScreen.kt` has trusted-link guard and LocalUriHandler; previous Web local-overrides gates | **PARTIAL**: real distribution URLs, URI platform acceptance and store policy need separate decision/test; no Dynamic Present redesign |
| Platform completion | Android `MainActivity.kt`, Web `Main.kt`, iOS `Main.kt` + Xcode app delegate | **PARTIAL**: observed Android and Web subflows only; iOS/macOS acceptance outstanding |
| Production authentication wiring | `network/NetworkModule.kt` creates `RequestHandler(get(), runtimeMetadataProvider=get())` without token provider | **REAL GAP BUT OUT-OF-SCOPE TO CODE** until W1/doc33 credential and backend auth contract approval |

## Source-based prioritization
1. **W7 ENV-001**: prepare exact limited Gradle/Kotlin/iOS/Web environment config plan using frozen doc23—Android flavors/build type separation, URL validation, external configuration, AppEnvironment. No code until reviewed/frozen.
2. **W7 LOG-001**: source-first test audit of centralized Kermit level policy and safe correlation IDs. No new logger facade or raw JSON.
3. **W7 PLATFORM-001**: targeted runtime evidence matrix on Android/Web, macOS iOS deferred with explicit status; verify no duplicate GET, URI flows, real reattach. Do not claim compiling equals running.
4. **W8 CLEAN-001**: audit actual consumer references of RetainCompatibilityProbe and all other potential leftovers; only remove after owner agreement.
5. **AUTH**: keep documented blocked credential ownership and SESSION vs BEARER mismatch separate; no speculative token storage/auth manager.

**No source files/tests/Gradle/backend changed.** Historical draft statuses remain intact; this document adds current source evidence, not a competing implementation freeze.
