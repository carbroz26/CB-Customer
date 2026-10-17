# 105 — W7-ENV-001 Exact File-by-File Environment Implementation Plan

**Status: OWNER REVIEW / DRAFT — NOT FROZEN; NO PRODUCTION IMPLEMENTATION AUTHORIZED.**
**Read-only source baseline:** `a4ca86bd3812c0c6b97b6266e08a042ac6b501e0` on `architecture/dynamic-runtime-v2-discussion-freeze` (2026-10-17).
**Exclusive authority:** folder `docs/architecture-proposal/dynamic-runtime-v2/`: doc20 W7; doc23 §§B,C,E frozen requirements; doc104 W7 source gap audit. Other project docs are explanation/evidence only, not plan authority.
**Boundary:** environment identification, externally configured trusted API origin, Android flavors, Web/iOS environment inputs; defer detailed logging severity policy to W7-LOG-001. Preserve shared HttpClient, networkModule, bootstrap, DynamicSession, Store, navigation, actions, DI composition.

## 1. Source reconciled current state
- `build-logic/.../AndroidApplicationConventionPlugin.kt`: only default debug and release URL fields, via legacy `cbpartner.api.baseUrl`; release default `https://api.invalid`; no flavor dimension.
- `androidApp/build.gradle.kts`: applicationId canonical; minSdk/targetSdk; no flavors.
- `androidApp/.../MainActivity.kt`: reads `BuildConfig.CBPARTNER_API_BASE_URL`; current debug-only retention probe remains.
- `webApp/.../Main.kt`: reads existing `cbpartner-api-base-url` meta; falls back to browser host port 3000 in any environment. `webApp/.../resources/index.html` meta initially empty.
- `app/src/iosMain/.../Main.kt`: bundle base URL or localhost fallback; `iosApp/.../project.pbxproj`: Debug localhost / Release `https://api.invalid`; no env key.
- `network/ApiBaseUrl.kt`: already validates protocol, host, origin-only path, no query/fragment. Does not assert HTTPS, trusted non-placeholder origin, or env identity.
- `app/App.kt`: root receives `RuntimeMetadataProvider` and `ApiBaseUrl`; existing session retention and DI closure. No need for new coordinator.
- `network/ApiBaseUrlTest.kt`: three origin parsing/rejection tests. Tests can be expanded in same source set; no new testing framework.

## 2. Frozen behavior to preserve and enforce
- Exactly `development`, `staging`, `production`; identical shared business/UI code and one Ktor transport.
- Android: one `environment` flavor dimension, independent Debug/Release build types (six combinations), suffix `.dev` / `.staging` / production canonical package.
- Development API default `http://10.0.2.2:3000` on Android and current browser-host `:3000` on Web; iOS localhost development allowed. Existing `-Pcbpartner.api.baseUrl=...` development override should remain compatible.
- Staging/production: explicit external trusted origin, no localhost/loopback or `api.invalid` placeholder, no automatic development fallback; fail closed on missing/invalid config. **Same rule for productionDebug as productionRelease.**
- Web: existing metadata configuration seam; iOS: existing Info.plist/build setting seam; no new duplicated transport, manager, env-specific business code.
- AppEnvironment is a small immutable `enum` or equivalent, not global state/manager. Precise platform config must fail explicitly for unknown environment, not silently assume development.
- Logging environment policy is separately scoped W7-LOG-001; ENV-001 must expose the environment without modifying Kermit sources or logging levels.

## 3. Proposed exact source-file changes

| File | Proposed action | Exact responsibility / restrictions |
|---|---|---|
| **NEW** `network/src/commonMain/kotlin/com/carbroz/cbpartner/network/AppEnvironment.kt` | ADD minimal `enum class AppEnvironment { Development, Staging, Production }` and one exact-value parser if needed | Shared strict identifier mapping from three lower-case wire/build strings. Do **not** implement URL transport, logger, token management, mutable singleton |
| `network/src/commonMain/kotlin/com/carbroz/cbpartner/network/ApiBaseUrl.kt` | MODIFY narrowly | Preserve existing generic origin parsing; add explicitly environment-aware validation entry (method or companion factory) requiring HTTPS and non-local/non-placeholder trusted host for staging/production. Do not make generic local HTTP tests fail; avoid permissive suffix blacklist alone; validate scheme, user-info, disallowed reserved/local origins and host under reviewed security policy |
| `network/src/commonTest/kotlin/com/carbroz/cbpartner/network/ApiBaseUrlTest.kt` | EXTEND | Table-driven cases: development http localhost/emulator allowed, HTTPS prod/staging allowed, missing/invalid scheme/path/query/fragment/credentials/local/placeholder rejected; unknown environment parsing fails |
| `build-logic/src/main/kotlin/com/carbroz/cbpartner/buildlogic/AndroidApplicationConventionPlugin.kt` | MODIFY | One flavor dimension `environment`; 3 flavors; set suffix `.dev` / `.staging`; add `BuildConfig.CBPARTNER_ENVIRONMENT`; per-flavor API URL sourced from explicit Gradle properties; development fallback; preserve build type debug/release and old dev override. Validate provided URLs and fail missing staging/prod at **variant packaging/distribution**, not global configuration; no `https://api.invalid` |
| `androidApp/build.gradle.kts` | **KEEP** unless exact convention configuration forces a small app-specific edit | Already canonical applicationId; flavors should live in the established convention plugin, not duplicated at app module |
| `androidApp/src/main/kotlin/com/carbroz/cbpartner/android/MainActivity.kt` | MODIFY minimally | Parse `BuildConfig.CBPARTNER_ENVIRONMENT`; validate `BuildConfig.CBPARTNER_API_BASE_URL` under environment; pass it to App; keep debug retention probe and runtime metadata unchanged |
| `webApp/src/wasmJsMain/kotlin/com/carbroz/cbpartner/web/Main.kt` | MODIFY | Read new `cbpartner-environment` meta (strict exact lower-case), default development **only in an explicitly development-configured deployment**. Select existing API URL meta: fallback current `:3000` in development only; staging/prod require explicit trusted URL; pass environment to App; retention probe untouched |
| `webApp/src/wasmJsMain/resources/index.html` | MODIFY small | Add one `cbpartner-environment` meta seam, default/placeholder policy with documented deployment replacement. Avoid accidentally serving development default as production: production artifact/deploy must override both meta values and pass preflight. No alternate network stack |
| `app/src/iosMain/kotlin/com/carbroz/cbpartner/app/Main.kt` | MODIFY | Read `CBPARTNER_ENVIRONMENT` from Info.plist; strict environment selection; development localhost fallback only, stage/prod require trusted URL; pass to App. No changes to device metadata or UIViewController lifecycle |
| `iosApp/CB-Partner.xcodeproj/project.pbxproj` | MODIFY carefully | Wire `CBPARTNER_ENVIRONMENT` and `CBPARTNER_API_BASE_URL` as external Xcode build settings, preserve Debug/Release with development/production defaults only when correct; add minimal staging build config/scheme or explicitly documented environment override per frozen doc23; remove Release `api.invalid`; do not hardcode real staging/prod URL |
| `app/src/commonMain/kotlin/com/carbroz/cbpartner/app/App.kt` | MODIFY minimally | Accept immutable AppEnvironment in addition to existing ApiBaseUrl, key environment-sensitive Koin composition appropriately; **do not reset or rewrite** retention/session/navigation. Avoid unused global DI registration unless W7-LOG has a justified consumer |
| `network/src/commonMain/kotlin/com/carbroz/cbpartner/network/NetworkModule.kt` | NO EDIT | One existing Ktor client; environment URL validation occurs before this shared module |
| `feature/*`, `data/*`, `navigation/*`, `domain/*`, `RequestHandler.kt`, `ApplicationContent.kt` | NO EDIT | No changes to business functionality, actions, bearer behavior, or runtime |
| Existing `README` or W7 folder document | ADD validation recipes **only** in this authoritative folder | No production behavior change; keep historical docs intact |

## 4. Two design details to settle before freeze (not permission to redesign)
**ENV-D1 — validation boundary / trusted origin:** Document23 requires trustworthy HTTPS origins and failure for local/dummy staging/production. Suggested minimal rule: HTTPS only; host is DNS hostname with a genuine non-reserved public suffix (or enterprise-authorized explicitly supplied origin), no localhost, loopback, private IP, `.invalid`, `.test`, `.example`, `.localhost`, or userinfo. **Note:** this is an implementation proposal, not an approved exhaustive origin trust guarantee; client-side hostname checks do not prove DNS routing or TLS deployment correctness. Explicit property supply + HTTPS remains the core frozen contract; confirm acceptable hostname treatment in owner review.
**ENV-D2 — build gating and deployment:** Android debug staging/production may need to compile for IDE testing without credentials, but **cannot run under development fallback**. Choose variant-scoped preflight failure on requested staging/prod package/assemble, not all-project configuration: invoking developmentDebug and `:network:jvmTest` must not need production credentials. Web deploy must explicitly replace/read environment meta and base URL; iOS settings must similarly fail startup/build without stage/prod URL. Define exact Gradle provider/configuration handling before implementation to avoid eager `.get()` failures for unrelated variants.
**ENV-D3 — staging iOS exposure:** Doc23 allows schemes/configuration or build settings; prefer minimum build-settings based approach if it can unambiguously select staging independently of debug/release, otherwise add one staging configuration with clear overrides. No six-Xcode-config matrix unless needed.

## 5. Test/build/acceptance gate (after owner approval and freeze)
1. Static Gradle variant discovery: `./gradlew :androidApp:tasks --all`; verify `developmentDebug`, `developmentRelease`, `stagingDebug`, `stagingRelease`, `productionDebug`, `productionRelease` and install/applicationId suffix.
2. Development backward compatibility: `.\\gradlew.bat :androidApp:assembleDevelopmentDebug -Pcbpartner.api.baseUrl=http://10.0.2.2:3000`; local Android Wi-Fi/reverse base URL still externally overridable. **Old `:androidApp:assembleDebug` will no longer be unique after flavors; update only documented command recipes.**
3. Fail-closed staging/prod: build attempted with missing URL must fail clearly; with explicitly configured trusted HTTPS URL must pass the relevant build (test origin can be a controlled real trusted test fixture; `.test` domain not accepted for deploy).
4. `.\\gradlew.bat :network:jvmTest :app:jvmTest :feature:dynamic:jvmTest :navigation:jvmTest :webApp:wasmJsBrowserDevelopmentWebpack`, plus Android target compilation; repeat Web runtime dev smoke.
5. Web: dev fallback only under explicit development marker; staging/prod missing, local, dummy, malformed URLs rejected before App creation.
6. iOS: source/settings inspection on Windows only; real Xcode build/config/simulator gate on macOS later—never label it PASS from Windows.
7. Review complete changed files, caller tree, no new manager, shared Ktor singleton, valid diagnostic policy, zero changes to auth, no unexpected session replacement, exact diff, `git diff --check`.

## 6. Status and freeze gate
This is a proposed **minimal file scope**, not authorization. Owner reviews ENV-D1/2/3 and the file list. Freeze only after those choices are settled; implementation afterward on this existing branch in a cohesive bounded change, then owner-run tests. No backend source changes, no new branch, no production edits yet.
