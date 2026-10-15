# 07 — Full Replacement Migration and Implementation Plan (Draft)

**State:** PLAN DRAFT — NOT FROZEN; NO SOURCE IMPLEMENTATION AUTHORIZED.
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`
**Target:** Discussion-frozen V2 architecture (documents 01–06), independent of legacy implementation shape.
**Migration input baseline:** `f59577337ac78855da3f9cb474483f0211ed1f30`.
**Goal:** Replace legacy architecture completely while keeping all currently user-observable flows, contracts, and required platform behaviors working, plus implementing the approved V2 design. Legacy constraints cannot veto a valid target design; bugs/contradictions in V2 must be fixed on their merits.

## 1. Non-negotiable execution strategy
1. **Target-first replacement, not incremental legacy beautification.** Each legacy class is categorized: replace, move/rename with rewrite, preserve only proven infrastructure, or delete. No twin old/new runtime is allowed at completion.
2. **One vertical slice at a time:** newly compiled functionality replaces old entry path in the same branch; avoid indefinite compatibility layers or two Stores/HTTP stacks.
3. **Do not claim 100% correct before gates.** Each wave must compile, pass relevant tests, and demonstrate behavior parity. No source changes under this draft until plan review/freeze and authorization.
4. **At the final gate** verify every production source belongs to the V2 ownership map; remove obsolete handlers/DTOs/duplicate helpers and old documentation pointers after approved migration. No silent loss of external URI, present/dismiss, retained values or other existing capabilities: explicitly classify them as preservation, V2 extension or versioned deprecation, not accidental removal.
5. **No legacy-driven design drift:** rewrite broad scope when required for correctness; only shorten execution by reusing correct, proven utilities/code whose semantics already match V2.
6. **One source of truth:** update existing authoritative project documentation after implementation verification; this proposal folder is not another current-state tracker.

## 2. Baseline inventory observed from GitHub tree
Modules already exist: `app, core, network, domain, data, navigation, feature/splash, feature/dynamic, androidApp, webApp`; `iosApp` is Xcode boundary. Approximately 150 Kotlin files in repository at baseline. Key legacy files include:
- `app/.../ApplicationContent.kt`, `App.kt`; `feature/splash/SplashStore.kt`, `SplashModule.kt`
- `network/ApiBaseUrl.kt`, `network/http/RequestHandler.kt`, `HttpClientBuilder.kt`, `NetworkModule.kt`
- `data/bootstrap/{BootstrapRepositoryImpl,remote/BootstrapRemoteDataSource,mapper/BootstrapMapper,model/BootstrapDtos}.kt`
- `data/dynamic/{DynamicRepositoryImpl,remote/DynamicRemoteDataSource,mapper/DynamicResponseMapper,mapper/TemplateMapper,mapper/ComponentMapper,mapper/SectionMapper,mapper/GroupMapper,mapper/ElementMapper,mapper/ElementContentMapper,mapper/DynamicActionMapper,mapper/DynamicValueMapper}.kt`
- `domain/model/dynamic/{DynamicResponse,DynamicScreen,DynamicRequest}.kt`, six category Domain families and generic DynamicValue
- `navigation/{DynamicNavigationEntry,DynamicNavigationState,DynamicNavigationHost}.kt`
- `feature/dynamic/{DynamicContainer,DynamicModule,store/DynamicStore,action/*,registry/*,renderer/DynamicRenderer,runtime/*,sdui/*}.kt`
A full **line-by-line final dependency audit** of all relevant classes, imports, tests and Gradle source sets is required before finalizing every deletion/rename path. Tree-level inventory alone is not proof that a class can safely be deleted.

## 3. Target class/module ownership
| Module/package | Target production classes / responsibility |
|---|---|
| `:core/runtime` | `RuntimeInfo`, `RuntimeInfoProvider`: approved platform metadata |
| `:core/logging` | `AppLogger` + safe event/config policy, reusing Kermit if suitable |
| `:network/config` | `ApiConfiguration`: injected environment base URL |
| `:network/http` | `HttpTransport`, `KtorHttpTransport`, `TransportRequest/Response`, typed transport failures; one Ktor stack |
| `:network/header` | `CommonHeaderProvider`, `AuthenticationHeaderProvider` only where separate testable logic warrants them |
| `:domain/bootstrap` | `BootstrapResult`, `BootstrapConfiguration`, `BootstrapRepository` |
| `:domain/dynamic/model` | `ScreenDestination`, `ScreenDocument`, `DynamicValue`, `ValueExpression`, typed request/outcome/failures |
| `:domain/dynamic/node` | Six category interfaces and concrete nodes with common/unique properties |
| `:domain/dynamic/action` | `DynamicAction`, `RequestAction`, `NavigateAction`, navigation mode/response mode |
| `:domain/dynamic/repository` | `DynamicRepository.loadScreen(destination)`, `executeRequest(actionRequest)` |
| `:data/bootstrap` | Bootstrap DTOs, mapper, remote repository; no duplicate custom transport |
| `:data/dynamic/dto` | Distinct screen/action API envelopes, destination DTO and six category concrete DTO contracts |
| `:data/dynamic/mapper` | ScreenDocumentMapper, six category mappers, ActionMapper, ValueExpressionMapper, DestinationMapper, shared visual property conversion |
| `:data/dynamic` | RemoteDynamicRepository; decode by caller intent (screen vs action), not JSON shape-guessing |
| `:navigation` | NavigationEntry, NavigationState, DynamicNavigationHost; pop/push/replace/popTo/refresh under one stack owner |
| `:feature/splash` | SplashContract (intent/state), SplashStore, SplashScreen; bootstrap orchestration only |
| `:feature/dynamic/store` | DynamicContract (intent/state/effect), DynamicStore, entry-scoped factory if needed |
| `:feature/dynamic/runtime` | RuntimeValues, ReferenceResolver, RetainedState, FormValidator; extracted retention helper only if nontrivial |
| `:feature/dynamic/action` | DynamicActionHandler, DynamicActionExecutor, RequestActionHandler, NavigateActionHandler, ActionResult |
| `:feature/dynamic/renderer` | ScreenRenderer, RenderContext, six category renderer contracts/registry/registration, concrete Form/Stack/Text/Input/Button/Image/Icon/Divider renderers |
| `:feature/dynamic/di` | DynamicModule with testable registrations; `:app/di` assembles modules only |
| `:app/session` | DynamicSession (navigation and retained values), session creation/reset |
| `androidApp / iosApp / webApp` | Platform startup, metadata, engine/platform integration; shared Compose app |

**Avoid extra files:** no BaseStore/BaseRepository, Manager/Factory layers without a concrete reason, universal utilities/constants, Login/Otp production screen classes, mirrored binding stores, or a second Dynamic transport.

## 4. Required contracts before source wave
- Destination: screenId, templateId (unique business template identity), templateType (expected concrete type), endpoint, method, authentication; separate visit-scoped entry ID. Backend confirmation for optional defaults.
- Endpoint-specific API mapping: bootstrap `data` configuration/startup, screen `data` ScreenDocument, action `data` generic payload plus optional typed destination; application code must be validated.
- Typed `ValueExpression` from four namespaces `$binding/$context/$state/$literal`. Binding entry-local, context read-only and approved, retained state session-scoped. Mandatory unresolved references fail before POST.
- Six structural categories with concrete types and supported child relationships, no generic all-fields model. Class-key renderer registration for each category, duplicate/unsupported-type handling.
- Template Shell with header/body/footer independent slots: body padding never insets header/footer. Migration contract for old `template.components` and `properties.padding` must be tested, not silently broken. Theme/gradient uses built-in Compose background where possible.
- Action registry: all seven existing capabilities `request`, `navigate`, `external_uri`, `present`, `dismiss`, `state`, `sequence` are required at migration release; preserve and type them, none may be omitted. Existing backend fixtures contain external_uri; do not treat it as compatible with just two actions.
- Navigation target/templateId lookup, separate entryId, optional popTo+refresh semantics; preserve state and browser/native back behavior. Proposal syntax is not automatically backend contract.
- Safety: redact/omit sensitive values in all staging/debug logging, never log raw requests/responses; cancellation propagates; no stale navigation after Store close.

## 5. Fast vertical replacement waves
| Wave | Replacement work (class boundaries) | Gate |
|---|---|---|
| W0 | Capture baseline: commits, docs, current production flow fixture set; inventory all source/imports and test names; specify target contract and compatibility differences | No code edit; sign-off exact file-list/deletions |
| W1 | Network core/shared transport: `ApiConfiguration`, metadata/header/auth policy, transport/errors; migrate bootstrap+Dynamic callers together without parallel transports | Ktor MockEngine headers/auth/URL/errors/cancellation; network JVM and Android/Web compile |
| W2 | Bootstrap pipeline: typed DTO/repository/config/startup result, SplashStore/Screen, app startup transition; share destination decoder | Bootstrap happy/maintenance/update/retry/error/cancellation fixture; Splash UI tests |
| W3 | Domain+Data UI model: ScreenDocument, typed values/expressions, node contracts; screen/action-specific DTO decoding and complete six-category mapper chain; remove old shape guessing | Complete existing login/OTP screen fixtures; all direct/nested node and unknown type tests |
| W4 | Navigation/session: single stack, separate entry ID vs unique templateId, popTo/refresh policy, entry/scope lifetime | Push/replace/pop/popTo/refresh/stale entry tests; Android/Web back parity |
| W5 | Renderer rewrite: single host; six category registry, concrete template/component/section/group/element/content renderers; body padding, theme, insets and asset rules | Generic Compose UI tests for Text/Input/Button/Image/content + visual golden/snapshot where supported |
| W6 | Runtime+MVI: screen-local bindings, context, retained state, reference resolution, validation and entry scope | All references, field types/rules, invalid inputs, recomposition, disposal/cancellation tests |
| W7 | Action layer: typed registry, Request/Navigate, response outcome, retained-value commit before navigation; explicit disposition for previous action types | Request body/method/auth, business error, response none/destination, duplicate click, cancellation, effect ordering |
| W8 | App wiring/platform completion: Koin registrations by module; one root session; Android/Web/iOS wiring; delete superseded sources and unused dependencies | JVM suite, Android device, Web browser, macOS iOS build/simulator; end-to-end fixture flow |
| W9 | Final source reconciliation, code/KDoc/log audit, production/staging policy, obsolete files/docs removal, owner review | Source tree V2 parity, no duplicate infrastructure, clean diff, documentation freeze |

Sequence can be optimized by grouping tightly dependent waves into one tested change set, but never leave broken compilation or two competing implementations as an accepted gate. Prefer an *integrated vertical branch* and a few coherent commits over hundreds of cosmetic rename commits. Never remove existing working behavior without an explicit parity decision.

## 6. Definition of compatibility and "same working app"
- Same bootstrap startup behavior for configured maintenance/updates; validated initial destination.
- Same existing Login and OTP JSON fixture display; generic UI controls remain interactive with backend-provided visual properties, validation and binding.
- Send OTP action sends correctly resolved phone/device body; retained phone/challenge become usable on next screen; verify OTP sends challenge/phone/otp/device.
- Previously supported action capabilities appearing in backend JSON (including legal links) must work or have an approved backend-schema migration with explicit acceptance; reducing initial V2 handlers to two is not by itself feature parity.
- Repeated taps, in-flight close/cancel, response app errors, missing references, unknown node/action types are deterministic and testable.
- Header/footer V2 slots and visual behavior only claim completeness once matching backend schema, layout and UI tests exist.
- Android/Web/iOS device acceptance not inferred from only JVM compilation.

## 7. Required verification commands / environments
Windows examples:
```powershell
.\gradlew.bat :core:jvmTest :network:jvmTest :domain:jvmTest :data:jvmTest :navigation:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest
.\gradlew.bat :androidApp:assembleDebug :webApp:wasmJsBrowserDevelopmentWebpack
git diff --check <baseline>..HEAD
git diff --stat <baseline>..HEAD
```
Use target Gradle task availability as verified, not assumed. Compose UI tests run on supported targets/test configurations. iOS requires macOS/Xcode runner for build and simulator verification. Browser end-to-end and Android physical device are distinct acceptance gates.

## 8. Detailed-plan completion gates still required
This draft provides **replacement ownership and wave order**, but is not yet an exact compile-ready per-file implementation plan. Before freeze, obtain actual class-level reads for all production/test source, dependency imports, Gradle scripts and target-specific files; finish class-by-class:
- `old path → new path → create/rewrite/delete → signatures/callers → dependencies → tests → migration order`.
- Resolve backend wire changes (slot schema, legacy action parity, destination fields, icon URL contracts, popTo) explicitly.
- Verify Kotlin multiplatform and Compose API compatibility and CI/test tasks.
- Review full planned diff and record owner approval.

**NEXT STATE: DETAILED PLAN**, not IMPLEMENT. No source tests or builds were executed by writing this document.
