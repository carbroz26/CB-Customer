# 01 — Principles, Module Ownership, Naming, Documentation

**State:** Discussion frozen; detailed implementation plan pending.

## Hard requirements
- Kotlin Multiplatform; Compose Multiplatform for Android, iOS and Web.
- Clean Architecture; strict SRP; pure Store/MVI/UDF; no ViewModel; one-way UI event/state/effect flow.
- Backend dictates supported UI hierarchy, visual properties, validation, actions and destinations. No login/OTP/profile-specific runtime implementations.
- No speculative Gradle modules, no universal everything-manager, no global utility dump, no duplicate network/navigation/reference machinery.
- Source changes require a subsequent exact file-by-file plan, plan freeze and separate implementation authorization.
- Each class has an explicit single reason to change, constructor inputs, output, constraints and tests.

## Proposed modules and directions
| Module | Owns | Must not own |
|---|---|---|
| `:androidApp` | Android entry, platform services | Business flows |
| `iosApp` (Xcode only) | Swift entry and iOS services | Separate common workflow implementation |
| `:webApp` | Browser entry and platform services | Separate Dynamic navigation engine |
| `:app` | Composition root, startup transition, session lifetime | DTO mapping, action logic |
| `:core` | Narrow shared runtime metadata and safe logging contracts | Arbitrary Utils / domain logic |
| `:network` | One Ktor transport, client/engine selection, base URL, common headers, token application, transport failures | Dynamic request workflows |
| `:domain` | Framework-free typed model and repository contracts | Ktor/Compose/Koin/serialization DTOs |
| `:data` | DTOs, envelope handling, typed mapping, repositories | Store, Compose, navigation mutation |
| `:navigation` | Navigation entries and stack ownership | Screen loading |
| `:feature:splash` | Bootstrap state and splash rendering | Dynamic element rendering |
| `:feature:dynamic` | Dynamic entry Store, runtime values, action dispatch, validation, renderer registry/renderers | HTTP client creation, hardcoded business screens |

Dependencies run `app -> feature/navigation/data/network/core`; `feature -> domain` (Dynamic may use navigation contracts only if necessary); `data -> domain + network`; `network -> core`; `navigation -> domain`. Prevent feature-to-data and Domain-to-framework coupling.

## DI policy
- Each owning module declares its own `di/*Module.kt`: network, data, splash, dynamic.
- `:app/di/AppModule.kt` composes modules and injects platform configuration; it is not a central catalog of every internal object.
- Singleton: stateless transport/mappers/resolver/validator/registries where appropriate; entry-scoped Dynamic Store; application-startup Splash Store; Dynamic-session-owned navigation and retained state.
- No Koin lookups inside renderers or handlers; constructor injection; no global service locator.

## Naming convention
- `*Dto` wire transport; `*Mapper` explicit boundary conversion.
- `ScreenDocument` a UI definition; `*Node` concrete Domain UI type (e.g. `FormTemplateNode`, `InputElementNode`).
- `*Renderer` concrete Compose renderer; `*Repository` Domain-facing data contract; `*Store` MVI orchestrator.
- `*Action`, `*ActionHandler`, `ActionResult`; `*Intent`, `*UiState`, `*Effect`.
- A proposed model name is not yet a migration instruction; source naming may be retained if clean after reconciliation.

## Proposed concise package ownership
```text
core/runtime/RuntimeInfo.kt, RuntimeInfoProvider.kt
core/logging/AppLogger.kt
network/http/HttpTransport.kt, KtorHttpTransport.kt, TransportModels.kt
network/config/ApiConfiguration.kt
network/header/CommonHeaderProvider.kt, AuthenticationHeaderProvider.kt
network/di/NetworkModule.kt
domain/bootstrap/{BootstrapConfiguration,BootstrapResult,BootstrapRepository}.kt
domain/dynamic/model/{ScreenDocument,ScreenDestination,DynamicValue,ValueExpression}.kt
domain/dynamic/node/{TemplateNode,ComponentNode,SectionNode,GroupNode,ElementNode,ContentNode}.kt
domain/dynamic/node/{template,component,section,group,element,content}/<ConcreteNode>.kt
domain/dynamic/action/{DynamicAction,RequestAction,NavigateAction}.kt
domain/dynamic/repository/DynamicRepository.kt
data/bootstrap/{BootstrapDtos,BootstrapMapper,RemoteBootstrapRepository}.kt
data/dynamic/{RemoteDynamicRepository,dto/*,mapper/*}
navigation/{NavigationEntry,NavigationState,DynamicNavigationHost}.kt
feature/splash/{SplashStore,SplashContract,SplashScreen,di/SplashModule}.kt
feature/dynamic/{DynamicContainer,store/*,runtime/*,action/*,renderer/*,di/DynamicModule}
app/{PartnerApp,ApplicationContent,session/DynamicSession,di/AppModule}.kt
```
These are **logical class responsibilities**; short closely-related declarations may share a Kotlin source file. Do not create placeholders.

## KDoc contract
For public contracts and non-obvious algorithms, document purpose, lifecycle/ownership, valid input, failure/cancellation behavior, invariants and non-responsibilities. Do not narrate obvious statements. Keep KDoc aligned with source.

## Safe structured logging
- Prefer existing approved logging capability if already present; assess KMP/library interoperability in planning.
- Debug/staging: structured lifecycle events (`bootstrap.fetch.started`, `screen.load.completed`, `action.started`, `navigation.applied`), metadata only.
- Production: minimal safe errors/operations as policy allows. Never print raw requests/responses, OTPs, tokens, personal data, challenge IDs or unrestricted parameter maps, including in staging.
- Log handler/action type, endpoint operation identifiers or appropriately sanitized paths, status, timings, safe template metadata—not raw arbitrary backend fields.
- Respect Decision 002: **no raw HTTP console body logging**.

## Utility/constants policy
Keep `:core` minimal; utilities/constants live in the concern's owning package. Avoid `Utils.kt`, `AppConstants.kt`, `BaseStore`, `BaseRepository` and unused abstractions.
