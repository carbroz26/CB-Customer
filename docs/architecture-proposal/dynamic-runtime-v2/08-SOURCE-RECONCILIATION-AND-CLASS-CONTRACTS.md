# 08 — Source Reconciliation and Class Contracts, Pass 1

**Stage:** Implementation planning / source reconciliation in progress. **Not PLAN FROZEN; no source changes authorized.**
**Scope verified:** settings.gradle.kts plus app composition, Dynamic DI, data DI, navigation stack, action-handler interface and six-category renderer registry at the architecture branch head before this commit.
**Objective:** Reach the V2 architecture exactly, preserving existing production capabilities rather than allowing legacy implementation shape to constrain the target.

## 1. Evidence versus design
Observed modules already match the proposed broad module set: core, domain, network, data, navigation, app, feature:splash, feature:dynamic, androidApp, webApp. The Xcode iOS boundary is not declared in settings.gradle.kts. **No new Gradle module is currently justified.** This is evidence from settings, not proof of builds.

Observed `ApplicationContent` currently retrieves a Koin SplashStore, switches on Splash success, initializes navigation and retained runtime state, creates a renderer registry, constructs context metadata and creates DynamicStore via a storeFactory lambda. That is a working composition path to preserve, while refactoring ownership into a smaller app composition/session and entry-scoped Store contract. In V2, do not let a Composable own unbounded global lifecycle resources.

Observed `DynamicModule` already registers Navigate, Request, ExternalUri, Dismiss, Present and State handlers, plus executor/validator/use case. The executor supports `sequence` in its own code. These are feature parity requirements, not candidates for deletion. Moving registrations to `feature/dynamic/di/DynamicModule.kt` is a proposed ownership/path change, not a reason to duplicate or delete behavior.

Observed `DynamicNavigationState` owns Compose SnapshotStateList back stack and a distinct presentation entry, with setRoot/push/replace/present/dismissPresentation/pop. V2 adds explicit entry ID, template ID lookup, popTo and refresh policies but must preserve existing modal presentation semantics.

Observed `DynamicRegistry` already has **six category maps and registration builder with duplicate rejection**, keyed by concrete KClass. **Do not rewrite this just for naming:** retain its proven dispatch approach if it meets V2's type safety and contract requirements; change API/implementation only for measured gaps. This does not weaken the target architecture.

Observed `DynamicDataModule` constructs DynamicResponseMapper, remote data source, repository. V2 replaces mixed screen/action response mapping with operation-specific decoding while retaining one common network transport and one destination mapping contract.

## 2. Precise first-pass replacement map
| Existing path | Proposed target path/owner | Change | SRP notes |
|---|---|---|---|
| `app/.../ApplicationContent.kt` | `app/PartnerApp.kt`, `app/session/DynamicSession.kt`, composition content | Rewrite/split only true lifetime vs UI composition responsibilities | Composable renders and coordinates transitions; session owns navigation/retained values; no raw DTO/action code |
| `feature/dynamic/DynamicModule.kt` | `feature/dynamic/di/DynamicModule.kt` | Move/rewrite registrations, preserve handler inventory | DI only, no business logic |
| `navigation/DynamicNavigationState.kt` | `navigation/NavigationState.kt` | Rewrite/extend for entry identity and popTo/refresh, preserve present/dismiss | Single stack authority, no fetch |
| `navigation/DynamicNavigationEntry.kt` | `navigation/NavigationEntry.kt` | Separate visit-scoped ID from unique templateId | No Store or response data inside entry |
| `navigation/DynamicNavigationHost.kt` | `navigation/DynamicNavigationHost.kt` or host ownership after audit | Verify Compose-specific responsibility and presentation behavior before rename | Display entries/effects only |
| `feature/dynamic/action/DynamicActionHandler.kt` | same package, typed handler contract | Rewrite contract as needed for independent handler registration | Handler executes its own action, returns outcomes |
| `feature/dynamic/action/DynamicActionExecutor.kt` | same package | Rewrite dispatch/sequence integration without duplicating action-specific logic | Handler selection/ordered composition only |
| `feature/dynamic/registry/DynamicRegistry.kt` | `feature/dynamic/renderer/RendererRegistry.kt` (proposed) | Reuse or move six-map implementation after assessing source; no duplicate registry | Registry knows model types and renderers, never API |
| `data/di/DynamicDataModule.kt` | `data/di/DataModule.kt` or retained focused module(s) | Rewire DTO mapper/repositories once | DI only |
| `data/dynamic/mapper/DynamicResponseMapper.kt` | `data/dynamic/mapper/ScreenDocumentMapper.kt` and `ActionResponseMapper.kt` | Replace shape-based branching by caller-known result type | Screen mapping and action response mapping separate; no dual authoritative payload |
| `domain/model/dynamic/DynamicResponse.kt` | `ScreenDocument` and `RequestOutcome` | Replace confusing mixed response representation | Keep wire envelope only in data |
| `domain/model/DynamicDestination.kt` | `ScreenDestination` | Preserve template identity and add backend-approved request metadata | Separate navigation entry ID |
| `feature/dynamic/runtime/DynamicReferenceResolver.kt` | `runtime/ReferenceResolver.kt` | Make one typed expression resolver; preserve all four namespaces | No duplicate resolver in action package |
| `feature/dynamic/action/DynamicActionValueResolver.kt` | no independent duplicate service | Delete after callers are migrated to shared resolver | Remove pass-through wrapper only after tests |
| `feature/dynamic/store/DynamicStore.kt` | `store/DynamicStore.kt` | Rewrite entry-scoped state/actions/async lifecycle | All network/JSON/navigation mutation delegated |
| `feature/dynamic/renderer/DynamicRenderer.kt` | `renderer/ScreenRenderer.kt` | Separate identity validation at load boundary; render typed nodes and theme | Never silently return blank on mismatch |
| `feature/dynamic/sdui/*` | `renderer/{template,component,section,group,element,content}/*Renderer.kt` | Migrate rendering, preserve existing visual behavior and direct/nested children | One concrete renderer per supported type where warranted |
| `feature/splash/SplashStore.kt` | `feature/splash/SplashStore.kt` | Rewrite only startup handling/lifecycle | Never manipulate Dynamic stack |
| `network/http/RequestHandler.kt` | `network/http/KtorHttpTransport.kt` | Replace transport contract; preserve headers/metadata and cancellation | No Dynamic response interpretation |

This table is *not exhaustive*. Subsequent passes must inspect all model, DTO, mapper, renderer, network/bootstrap, Compose, test and Gradle files line-by-line, then enumerate every old/new/delete path.

## 3. Important new-architecture safeguards
1. Keep `external_uri`, `present`, `dismiss`, `state`, `sequence` and existing content click behavior. Integrate as independent typed handlers/sequence orchestrator without a monolithic executor. No reduction to request/navigate only.
2. Preserve `IconElement` if currently supported: source tree contains DTO and Domain IconElement, so V2's initial concrete type inventory must include it unless backend and owner authorize deprecation.
3. `templateId` unique per server template identity; separate unique navigation entry key; support backend-controlled targeted returns with explicit popTo/refresh semantics after contract decision.
4. Keep current Login/OTP JSON fixture behavior while new typed DTO/domain representation and header/body/footer schema transition are introduced. New slot schema is *proposed*, not yet live.
5. Shared Ktor infrastructure, env base URL, approved metadata/auth and safe structured logging. No raw request/response body logs.
6. Generic tests for all supported Input/Text/Button/image/Content properties, mappings, actions, reference/path error behavior and platform UI; no business-specific production classes.

## 4. Next source audit passes
- Pass 2: all network + bootstrap files, metadata headers/base URL, DI and tests; final HTTP class signatures.
- Pass 3: Domain six categories + all data DTOs/mappers + fixtures; final typed contracts/JSON compatibility.
- Pass 4: entire Dynamic action/store/runtime and tests; state/effect/cancellation/sequence contracts.
- Pass 5: renderer classes/theme/asset/layout, navigation host, app/platform entries and Gradle; safe area/header slots/compatibility.
- Pass 6: complete per-file change manifest, integration strategy and test matrix; review, correct plan defects and request plan freeze.

## 5. Definition of done for planning
No implementation authorization until (a) complete source audit and caller graph, (b) explicit backend wire/migration decisions, (c) exact new+deleted file manifest, (d) Kotlin API signatures that connect without circular dependencies, (e) test and rollback plan, and (f) owner's PLAN FROZEN decision.
