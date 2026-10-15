# 17 — Pass 6: Exact V2 Implementation Blueprint (Review Draft)

**State: PLAN DRAFT / CONTRACT CLOSURE REVIEW, NOT PLAN FROZEN. No Kotlin implementation authorized.**
**Freeze review:** [18-PASS6-CONTRACT-CLOSURE-AND-FREEZE-REVIEW.md](18-PASS6-CONTRACT-CLOSURE-AND-FREEZE-REVIEW.md) supersedes this draft's unresolved decision defaults and provides explicit confirmed/recommended/blocked classifications. No proposed new header/footer/popTo wire fields may be implemented as approved until backend fixtures and owner acceptance.

**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`.
**Evidence:** actual GitHub tree of this branch; production sources and tests inspected during Passes 1–5; owner-provided canonical Login/OTP JSON; reconciliations 12–16. This file is a planning contract, **not** a claim of successful compile/tests.

## 0. Binding constraints and design authority

1. Preserve existing Gradle modules: `:core`, `:network`, `:domain`, `:data`, `:navigation`, `:feature:splash`, `:feature:dynamic`, `:app`, `:androidApp`, `:webApp`; iOS is an Xcode entry. Never create a new module for a helper.
2. One screen response contains exactly one Template; that Template contains one or more Components. Component contains Elements and/or Sections; Section contains Elements and/or Groups; Group contains only Elements; Elements optionally have nested leading/trailing Content. No seventh category.
3. **Keep wire contract** `data.template.components[]` and template properties in place. Only Header/Footer may be added as **optional additive** Template fields after exact shape approval. Existing template padding is Body-only when those slots exist.
4. Supported concrete types: `FormTemplate`, `StackComponent`, `StackSection`, `StackGroup`, Text/Image/Input/Button/Icon Elements, Icon/Divider Content. All seven existing action capabilities are mandatory: request, navigate, external_uri, present, dismiss, state, sequence.
5. Backend will migrate name-only icons to URL-based asset references. Remote primary URL → remote fallback URL → optional generic locally bundled image/icon placeholder, with Coil caching as available; no icon-name registry. Render helper logical name `RemoteAssetImage`.
6. Strict SRP, explicit dependency directions, Pure Store MVI/UDF, no ViewModel, no per-screen production flow, no redundant managers, DTOs exclusively in Data, Compose solely presentation/UI adapters.
7. Source snapshot is historical migration input. Target-first decisions cannot silently revert to old design because a legacy caller expects it.
8. If a particular helper becomes a single-line forwarding wrapper in implementation, combine it with its owner instead of building the planned placeholder class.

## 1. Frozen logical data paths (implementation API sketches)

### Screen
```text
NavigationEntry(destination)
 -> DynamicStore.loadScreen
 -> DynamicRepository.loadScreen(destination)
 -> RemoteDynamicRepository + HttpTransport
 -> ApiEnvelopeDto<ScreenDocumentDto> [operation-specific]
 -> ScreenDocumentMapper + six category mappers
 -> ScreenIdentityVerifier
 -> DynamicUiState.Ready(ScreenDocument, entryBindings, errors)
 -> ScreenRenderer -> TemplateRenderer -> TemplateShell
 -> Body via existing template.components[] -> child registries
```
Identity includes screenId, templateId, templateType, supported schemaVersion/targetApp. An invalid document is a typed failure rather than a blank screen.

### Actions
```text
Renderer event -> Store ActionTriggered(typed action)
 -> execution context includes entry bindings + context + retained session snapshot
 -> ActionExecutor (registered typed handlers)
 -> RequestActionHandler / Navigate / ExternalUri / Present / Dismiss / State / Sequence
 -> ActionOutcome(statePatch, orderedEffects)
 -> Store commits retained state
 -> session-safe host effect delivery -> navigation/modal/external URI
```
Request response is mapped with `ActionResponseMapper` instead of heuristic ScreenDocument detection. A validation request nested in Sequence still validates before HTTP.

## 2. Module-wise actual source migration manifest

Paths are relative to module `src/commonMain/kotlin/com/carbroz/cbpartner/` unless an explicit platform path is shown. `Rewrite` means update file/integration, `Replace` means new target then remove obsolete old path only when callers/tests migrated. The exact target paths are the intended package/class locations; source declarations sharing a Kotlin file do not imply unnecessary separate classes.

### `:core` — retain minimal metadata
| Existing | Operation | Target contract |
|---|---|---|
| `core/runtime/RuntimeMetadata.kt` | **Retain/refine** | `RuntimeMetadata(platform,appVersion,buildNumber,deviceId)`, `RuntimeMetadataProvider.current()`. No Dynamic visual models in core. |
| New logging abstraction | **Do not create by default** | Integrate with existing safe logger only on demonstrated callsites. No raw payload logging. |

### `:network` — one Ktor client, no second stack
| Existing | Operation | Target contract |
|---|---|---|
| `network/ApiBaseUrl.kt` | Retain validation | Strict allowed origin and safe URL joining. |
| `network/http/HttpClientBuilder.kt` | Rewrite narrowly | One configured HttpClient; common JSON/timeout/headers, cancellation, auth token policy. |
| `network/http/RequestHandler.kt` | Rename/reshape or retain if SRP already satisfied | Single transport boundary `suspend execute(TransportRequest): TransportResponse`; no bootstrap/screen semantics. Preserve method, endpoint segments, body, query, cancellation/error classes. |
| `network/NetworkModule.kt` | Rewrite wiring | Registers one client and transport. |
| `network/NetworkException.kt`, `NetworkResult.kt`, `http/DefaultError.kt` | Reconcile/reuse | One typed transport failure mapping; remove overlapping wrappers only with all callers updated. |
| `network/Paged.kt` | Retain unless actual callers show dead code | Not part of V2 refactor; no speculative deletion. |
| `network/http/TransportModels.kt` | Create if truly justified | `TransportRequest(method,relativeEndpoint,headers,query,body,auth)`, `TransportResponse(status,body,headers)`. |

### `:domain` — typed API and UI contracts
| Existing | Operation | Target file/classes |
|---|---|---|
| `domain/model/DynamicDestination.kt` | Replace | `domain/dynamic/model/ScreenDestination.kt`: screenId/templateId/templateType/endpoint, method/auth only after optional wire defaults approved. |
| `domain/model/dynamic/DynamicScreen.kt` | Replace | `domain/dynamic/model/ScreenDocument.kt`: screenId/schemaVersion/targetApp/template/theme. |
| `domain/model/dynamic/DynamicRequest.kt` | Replace | `domain/dynamic/model/ActionRequest.kt`: resolved HTTP request with method/auth/body/responseMode. |
| `domain/model/dynamic/DynamicResponse.kt` | Split | `domain/dynamic/model/ActionResponse.kt`/outcome data and `ScreenDocument`; no polymorphic shape guessing. |
| `domain/model/dynamic/value/DynamicRawValue.kt` | Replace | `domain/dynamic/model/DynamicValue.kt`: string/number/bool/object/array/null. |
| New typed expression | Create | `domain/dynamic/model/ValueExpression.kt`: Literal/BindingRef/ContextRef/StateRef/Object/Array expression. |
| `domain/model/dynamic/value/{DynamicArrangement,DynamicDecorations,DynamicPadding,DynamicSpacing,DynamicTextValues}.kt` | Move/refine | `domain/dynamic/model/LayoutProperties.kt`, `VisualProperties.kt`, `TextProperties.kt` only logical groupings genuinely shared; preserve spacing vs padding, gradient stop, angle, segmented presentation and spans. |
| `domain/model/dynamic/validation/DynamicValidation.kt`, `action/DynamicBinding.kt` | Move/refine | `domain/dynamic/model/ValidationRule.kt`, field-key binding model if useful; no duplicate BindableElement abstraction without consumer. |
| `domain/model/dynamic/template/{DynamicTemplate,FormTemplate}.kt` | Replace | `domain/dynamic/node/TemplateNode.kt`, `template/FormTemplateNode.kt`, preserving `components: List<ComponentNode>`. |
| `domain/model/dynamic/component/{DynamicComponent,StackComponent}.kt` | Replace | `domain/dynamic/node/ComponentNode.kt`, `component/StackComponentNode.kt`, Elements+Sections. |
| `domain/model/dynamic/section/{DynamicSection,StackSection}.kt` | Replace | `domain/dynamic/node/SectionNode.kt`, `section/StackSectionNode.kt`, Elements+Groups. |
| `domain/model/dynamic/group/{DynamicGroup,StackGroup}.kt` | Replace | `domain/dynamic/node/GroupNode.kt`, `group/StackGroupNode.kt`, only Elements. |
| `domain/model/dynamic/element/{DynamicElement,TextElement,ImageElement,InputElement,ButtonElement,IconElement}.kt` | Replace | `domain/dynamic/node/ElementNode.kt` + five concrete `element/*ElementNode.kt`. Preserve every Pass 3 field, enabled/disabledColor, binding/actions and styling. |
| `domain/model/dynamic/content/{ElementContent,IconContent,DividerContent}.kt` | Replace | `domain/dynamic/node/ContentNode.kt` + `content/{IconContentNode,DividerContentNode}.kt`. Content-owned spacing; no DividerElement. |
| `domain/model/dynamic/action/DynamicAction.kt` | Replace | `domain/dynamic/action/DynamicAction.kt` with typed Request/Navigate/ExternalUri/Present/Dismiss/State/Sequence; subtype files only when sizable. |
| `domain/repository/DynamicRepository.kt` | Rewrite API | `suspend loadScreen(destination: ScreenDestination): Result<ScreenDocument>` and `suspend executeRequest(request: ActionRequest): Result<ActionResponse>`. No shared fetch response. |
| `domain/bootstrap/ApplicationBootstrap.kt`, `domain/repository/BootstrapRepository.kt`, `domain/model/bootstrap/*` | Simplify | Direct bootstrap repository (or use-case only when substantive policy); preserve typed config/maintenance/update/startup. |

**Node interface restriction:** do not use `sealed interface` when direct implementations live in distinct Kotlin packages; ordinary interfaces plus registered converters are sufficient.

### `:data` — DTO, mapping, repositories
| Existing | Operation | Target |
|---|---|---|
| `data/bootstrap/model/BootstrapDtos.kt`, `mapper/BootstrapMapper.kt`, `BootstrapRepositoryImpl.kt` | Rewrite precisely | `data/bootstrap/dto/BootstrapDtos.kt`, `BootstrapMapper.kt`, `RemoteBootstrapRepository.kt` with all current maintenance/update/features validation. |
| `data/bootstrap/remote/BootstrapRemoteDataSource.kt` | Fold only if passthrough | Direct repository→transport when adapter has no independent responsibility. |
| `data/dynamic/model/response/{DynamicResponseDto,DynamicDestinationDto}.kt` | Split | `data/dynamic/dto/{ApiEnvelopeDto,ScreenDestinationDto,ActionResponseDto}.kt`. Decode by operation, one DestinationMapper. |
| `data/dynamic/model/screen/DynamicScreenDto.kt`, `theme/DynamicThemeDto.kt` | Move | `data/dynamic/dto/{ScreenDocumentDto,ScreenThemeDto}.kt`. |
| `data/dynamic/model/template/FormTemplateDto.kt` | Move | `data/dynamic/dto/node/TemplateDtos.kt`, fixed `components[]`; add optional header/footer fields after approval only. |
| `data/dynamic/model/{component/StackComponentDto,section/StackSectionDto,group/StackGroupDto}.kt` | Move/group | `data/dynamic/dto/node/{ComponentDtos,SectionDtos,GroupDtos}.kt`, field-preserving. |
| `data/dynamic/model/element/{TextElementDto,InputElementDto,ButtonElementDto,ImageElementDto,IconElementDto}.kt` | Move/group | `data/dynamic/dto/node/ElementDtos.kt`, preserving validation/binding/actions raw skeleton. |
| `data/dynamic/model/action/DynamicActionDto.kt`, `validation/DynamicValidationDto.kt` | Move | `data/dynamic/dto/{action/ActionDtos,ValidationDto}.kt`. |
| Existing nested Content JsonObject | Create DTO only if needed | `data/dynamic/dto/node/ContentDtos.kt`: Icon/Divider with strict validation, no silently dropped malformed Content entries. |
| `data/dynamic/mapper/DynamicResponseMapper.kt` | Delete after split | `data/dynamic/mapper/{ScreenDocumentMapper,ActionResponseMapper,DestinationMapper}.kt`. |
| `data/dynamic/mapper/DynamicValueMapper.kt` | Split | `{ValueMapper,ValueExpressionMapper,VisualPropertyMapper}.kt` with scope-limited APIs. |
| `data/dynamic/mapper/{Template,Component,Section,Group,Element,ElementContent}Mapper.kt` | Replace | `{Template,Component,Section,Group,Element,Content}Mapper.kt` six category boundaries, typed concrete conversion and declared child relationships; helper methods instead of deep wrapper forests. |
| `data/dynamic/mapper/DynamicActionMapper.kt` | Rewrite | `ActionMapper.kt` single mapping for events, Content and TextSpan, plus nested Sequence children. |
| `data/dynamic/DynamicRepositoryImpl.kt`, `remote/DynamicRemoteDataSource.kt` | Rewrite/fold | `RemoteDynamicRepository.kt` loads expected ScreenDocument or executes ActionRequest; can fold no-op remote adapter; share transport. |
| `data/di/{BootstrapModule,DynamicDataModule,SerializationModule}.kt` | Rewire | One deliberate JSON policy, constructor-injected typed mappers and repositories; no circular dependencies or redundant Koin catalogs. |

**Concrete Data mapper APIs (draft)**
```kotlin
class ScreenDocumentMapper(private val templates: TemplateMapper, private val themes: ScreenThemeMapper) {
    fun map(dto: ScreenDocumentDto): ScreenDocument
}
class TemplateMapper(private val components: ComponentMapper, private val visuals: VisualPropertyMapper) {
    fun map(raw: JsonObject): TemplateNode
}
// ComponentMapper -> SectionMapper + ElementMapper
// SectionMapper -> GroupMapper + ElementMapper
// GroupMapper -> ElementMapper
// ElementMapper -> ContentMapper + ActionMapper + ValueExpressionMapper
// ContentMapper -> ActionMapper
```
These are conceptual contracts; exact DTO polymorphic dispatch implementation and error types must be compile-reviewed. Avoid dependency cycles by instantiating inner mappers first. Avoid `ScreenThemeMapper` standalone if it would be trivial (inline focused function in ScreenDocumentMapper).

### `:navigation` — one stack authority
| Existing | Operation | Target |
|---|---|---|
| `navigation/DynamicNavigationEntry.kt` | Rewrite | `NavigationEntry(entryId,destination)`, unique entry per visit even when templateId repeats. |
| `navigation/DynamicNavigationState.kt` | Extend | `push/replace/pop/popTo/present/dismissPresentation/setRoot`; distinguish entries, deterministic missing/ambiguous popTo handling. |
| `navigation/DynamicNavigationHost.kt` | Preserve/update | Navigation3 NavDisplay, distinct Dialog/presentation; platform Back and session-safe effect consumption. |
| New `navigation/NavigationOperation.kt` | Create only if useful | typed push/replace/pop/popTo/refresh instructions; no second router. |

### `:feature:splash` — static startup
| Existing | Operation | Target |
|---|---|---|
| `feature/splash/SplashStore.kt` | Rewrite | Explicit Initial/Loading/Ready/Maintenance/RequiredUpdate/Failure, retry/cancel; app state transition. |
| `feature/splash/SplashScreen.kt`, `SplashTheme.kt` | Refine | Static native launch appearance preserved; explicit startup results/error/actions. |
| `feature/splash/SplashModule.kt` | Rewire | Startup-scoped Store and direct dependency policy; no duplicate bootstrap adapter. |

### `:feature:dynamic` — entry Store, runtime, actions, rendering
| Existing | Operation | Target |
|---|---|---|
| `feature/dynamic/store/{DynamicScreenState,DynamicStore}.kt` | Rewrite | `store/{DynamicContract,DynamicStore}.kt`. Intents Load/Retry/Refresh/ValueChanged/ActionTriggered/EffectFailed; Ready(screen, bindings, per-field validation errors, action status/error), Loading, Failure. One entry owner and explicit scope. |
| `feature/dynamic/runtime/{DynamicReferenceResolver,DynamicRuntimeState}.kt` | Replace | `runtime/{ReferenceResolver,RetainedState}.kt` + focused StatePatchApplier if substantial. Immutable values; explicit missing vs null; retained state is session-owned. |
| `feature/dynamic/runtime/DynamicFormValidator.kt` | Rewrite | `runtime/FormValidator.kt`; generic hierarchy traversal, field-key errors; action executor validation precondition handles sequence-contained requests. |
| `feature/dynamic/action/DynamicActionValueResolver.kt` | Delete | Directly use single `ReferenceResolver`. |
| `feature/dynamic/action/{DynamicActionHandler,DynamicActionExecutor,DynamicActionResult}.kt` | Rewrite | `action/{ActionHandler,ActionExecutor,ActionOutcome}.kt`: typed registry, ordered effect result, no JSON payload parsing in executor. |
| `feature/dynamic/action/{Request,Navigate,ExternalUri,Present,Dismiss,State}ActionHandler.kt` | Rewrite/reuse responsibility | Each typed handler does one action, does not directly mutate navigation; no duplicated mode parser. |
| New `feature/dynamic/action/SequenceActionHandler.kt` | Create if justified | Ordered nested execution, state snapshot threading, effect list and bounded depth; avoid executor↔handler DI cycle by child-dispatch callback/port. |
| `feature/dynamic/usecase/LoadDynamicScreenUseCase.kt` | Delete if passthrough | Store can invoke `DynamicRepository.loadScreen` directly unless additional meaningful policy exists. |
| `feature/dynamic/DynamicModule.kt` | Rewire | Typed action handlers, Store dependencies and registry; retain all seven capabilities. |
| `feature/dynamic/DynamicContainer.kt` | Rewrite | Single host with entry Store lifecycle, typed rendering and session-safe effect handoff; no parallel nav authority. |
| `feature/dynamic/renderer/DynamicRenderer.kt` | Rewrite | `renderer/ScreenRenderer.kt`, consumes validated document and registered Template; no blank mismatch returns. |
| `feature/dynamic/registry/{DynamicRegistry,DynamicRendererContracts,DynamicRendererRegistration}.kt` | Reuse/refine | One category-separated registry with typed checked adapters, duplicate registration rejection; no six managers. |
| `feature/dynamic/runtime/DynamicRenderContext.kt` | Move/refine | `renderer/RenderContext.kt` or existing context if still SRP-clean. Render-time values/callbacks and child delegation, no fetch. |
| `feature/dynamic/runtime/DynamicLayout.kt` | Refactor | `renderer/layout/LayoutModifiers.kt`, `renderer/style/VisualConverters.kt`, small focused functions with no over-splitting. |
| `feature/dynamic/sdui/template/FormTemplate.kt` | Replace | `renderer/template/FormTemplateRenderer.kt`; concrete template type only. |
| New `feature/dynamic/renderer/shell/TemplateShell.kt` | Create | Shared header/body/footer placement, body-only padding, safe area/scroll. Individual slot renderer classes NOT required if trivial. |
| `feature/dynamic/sdui/{component/StackComponent,section/StackSection,group/StackGroup}.kt` | Replace | `renderer/{component/StackComponentRenderer,section/StackSectionRenderer,group/StackGroupRenderer}.kt`. |
| `feature/dynamic/sdui/element/{TextElement,InputElement,ButtonElement,ImageElement,DecorationElements}.kt` | Replace | `renderer/element/{Text,Input,Button,Image,Icon}ElementRenderer.kt`. |
| `feature/dynamic/sdui/content/ElementContent.kt` | Replace | `renderer/content/{Icon,Divider}ContentRenderer.kt`. |
| `feature/dynamic/sdui/content/DynamicAssetImage.kt` | Move/rename + refine | `renderer/asset/RemoteAssetImage.kt`: Coil primary/fallback/cached/local generic placeholder. No icon-name lookup. |
| New `feature/dynamic/renderer/shell/` slot helpers | Only as needed | Small Composables inside TemplateShell unless independent collapse/scroll behavior merits classes. |

**Renderer contracts:** `render(node: ConcreteNode, context: RenderContext)`; Element callback `onAction(DynamicAction)`, input callback `onValueChange(bindingKey,DynamicValue)`. Category renderers delegate child traversal through Registry; none execute HTTP or mutate Store directly. Text handles backend enabled/disabledColor, gradients retain stop+angle; Icons use URLs after backend migration.

### `:app` and platforms
| Existing | Operation | Target |
|---|---|---|
| `app/App.kt` | Rewrite lifecycle | Platform-injected composition root; don't repeatedly `startKoin` inside Composable on recreation. |
| `app/ApplicationContent.kt` | Rewrite | Splash→DynamicSession, one shared navigation, renderer registry, context; no direct action handler/DTO parsing. |
| New `app/session/DynamicSession.kt` | Create if lifecycle warrants | Own navigation, retained state, session effect delivery; avoid another repository/store manager. |
| `androidApp/.../MainActivity.kt`, platform metadata | Refine | Proper lifecycle, BuildConfig URL, device/platform metadata and Android back/insets. |
| `app/src/iosMain/.../Main.kt` | Refine | iOS Info.plist URL, ComposeUIViewController, secure network and keyboard/safe areas. |
| `webApp/src/wasmJsMain/.../Main.kt` | Refine | Meta-origin injection, browser History/Back/CORS/viewport/keyboard, no alternate router. |
| `feature/dynamic/build.gradle.kts` + relevant modules | Update only needed deps | Compose + Coil remain; no added Gradle modules. |

## 3. Required exact semantics (before freeze)

| Decision | Proposed behavior | Freeze status |
|---|---|---|
| Template cardinality/slots | Exactly one Template; 1+ Components; existing `template.components[]`; optional Header/Footer sibling slots; body-only template padding | Core fixed, **exact Header/Footer DTO open** |
| Header/Footer scrolling | Body can scroll; Header may be fixed/collapsible; Footer fixed/normal according to typed backend policy | **Open JSON keys and modes** |
| Asset fallback | Coil remote/cache primary, remote/cache fallback, then generic bundled placeholder where approved | Owner direction confirmed; **missing/optional policy needs test** |
| Icon identifiers | URL required, optional fallbackUrl; migrate old `name` assets on backend | Confirmed migration; fixture update pending |
| Navigation `popTo` | Select target by backend templateId; explicit missing/duplicate target failure; refresh optionally refetch | Behavior proposed, **wire syntax and binding policy open** |
| Effects after nav | Ordered effects survive originating entry disposal via session-safe delivery boundary; no duplicate stack | Required; **acknowledgement/ownership mechanism open** |
| Sequence | Continue after successful navigation/URI effect; typed children; all effects ordered; child failure stops later actions, no newly published effects; past HTTP work not rollbackable | Continuation confirmed; **failure semantics needs approval** |
| Value reference | `$binding/$context/$state/$literal`; no `$response`; missing vs null distinguished | Confirmed; missing-path policy per consumer open |
| Response/headers | Bootstrap vs Screen vs Action mapper by operation; secure origin and bearer restrictions; destination four current fields | Confirmed; additive method/auth and error status codes pending |
| Input events | Five supported hooks; segmented presentation uses backend dimensions/keyboard; no hardcoded OTP logic | Required; focus/submit/disabled exact behavior test |
| Gradient | Preserve angle, colors and `stop` values, type-specific implementation | Required; angle convention verification open |

**Do not invent final wire fields in implementation.** The explicit proposed examples in planning docs are NOT backend API contract approval.

## 4. Implementation waves and release gates

| Wave | Build/change order | Must pass before next |
|---|---|---|
| W0 | Lock source SHA, re-read source/docs/fixtures, decide open wire contracts, map all old imports; no source changes | Approved plan, no contradictions, exact paths/replacements |
| W1 | Network & shared transport; secure common header/auth; no second client | `:network:jvmTest`, `:data:jvmTest` transport/DI checks |
| W2 | Bootstrap DTO/mapper/repository + Splash state/UI + Application startup | `:data:jvmTest :feature:splash:jvmTest`; maintenance/update/error |
| W3 | Domain typed values/actions/nodes, Data DTOs + six category mappers, Screen/Action responses + fixture tests | `:domain:jvmTest :data:jvmTest`; owner Login/OTP JSON and backend URL icon fixtures |
| W4 | Session/Navigation entries + popTo/refresh + effect lifecycle | `:navigation:jvmTest :feature:dynamic:jvmTest`; repeated visit, modal/back |
| W5 | Renderer registry, category renderers, Shell slots, gradients, local fallback image, disabled controls | Renderer/UI tests + Android/Web compile and visual checks |
| W6 | Typed action handlers, resolver/validator/sequence/retention and MVI Store | `:feature:dynamic:jvmTest`; sequence/action/Store tests |
| W7 | App+Android/iOS/Web wiring and end-to-end Login→OTP→destination tests; remove legacy implementations and dead adapters | Full JVM matrix, Web development bundle/browser, Android device; macOS iOS build/simulator |
| W8 | Documentation reconciliation, safe logs, source cleanup, Git diff review, no obsolete duplicate stacks | All supported platforms and acceptance criteria, owner signoff |

**Known Gradle commands from prior project state, to revalidate at W0:** `./gradlew :core:jvmTest :domain:jvmTest :data:jvmTest :navigation:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest`; `./gradlew :webApp:wasmJsBrowserDevelopmentWebpack`. On Windows use `.\gradlew.bat`. iOS requires macOS/Xcode. No command was executed as part of this plan.

## 5. Test source/file manifest

Existing test files to **rewrite/extend** instead of duplicating:
- `network/src/commonTest/.../ApiBaseUrlTest.kt`, `http/RequestHandlerTest.kt`.
- `data/src/commonTest/.../bootstrap/{BootstrapMapperTest,BootstrapRemoteDataSourceTest,BootstrapRepositoryImplTest}.kt`, `di/NetworkModuleTest.kt`.
- `data/src/commonTest/.../dynamic/{DynamicBackendFlowContractTest,DynamicContractFixtures,DynamicContractTest}.kt`, `dynamic/mapper/DynamicResponseMapperTest.kt` (split by operation).
- `feature/splash/src/commonTest/.../SplashStoreTest.kt`.
- `navigation/src/commonTest/.../DynamicNavigationStateTest.kt`.
- `feature/dynamic/src/commonTest/.../action/{DynamicActionExecutorTest,DynamicActionValueResolverTest,RequestActionHandlerTest}.kt` (rename/split); `runtime/{DynamicReferenceResolverTest,DynamicRuntimeStateTest}.kt`; `registry/DynamicRegistryTest.kt`; `store/DynamicStoreTest.kt`.

Needed focused **new** tests if not covered by an existing file:
- `data/dynamic/mapper/ScreenDocumentMapperTest.kt`, `ActionResponseMapperTest.kt`, `ActionMapperTest.kt`, `SixCategoryMapperContractTest.kt`.
- `feature/dynamic/runtime/FormValidatorTest.kt`, `feature/dynamic/action/SequenceActionHandlerTest.kt`, `feature/dynamic/action/RetainedValueExtractorTest.kt` (where owned), `feature/dynamic/store/RefreshAndEffectDeliveryTest.kt`.
- Compose renderer/slot/gradient/image-fallback integration tests where KMP UI test framework and platform source sets support them; do not pretend commonTest JVM can validate pixels, Coil network, Android IME, Web browser or iOS safe areas.
- Separate updated backend canonical fixtures for icon URLs and approved Header/Footer additions; preserve original supplied JSON as historical compatibility evidence.

**Acceptance dimensions:** DTO requiredness and failure, all properties, all child paths, disabled text/button interaction, gradient stop/angle, scroll/insets, primary/fallback/local asset, all 7 actions and 5 event hooks, sequence state/effects ordering, HTTP failure/cancellation, retained state persistence/reset, independent entry bindings, modal/popTo/refresh, lifecycle after disposal, Android/Web/iOS.

## 6. Production-code risk list and stop conditions

**STOP before implementing a wave** if: any fixed JSON field is moved/renamed; a node category/type is silently dropped; a client/router/Store is duplicated; typed action capability is removed; a frontend icon-name registry is introduced; body padding leaks into Header/Footer; Koin/coroutine lifecycle unbounded; invalid response becomes blank UI; test suite for touched boundary fails without understood cause; stale docs contradict owner decisions.

**Not yet fully resolved:** final Header/Footer JSON, navigation popTo/refresh and multi-effect session handoff, sequence partial-success policy, icon URL backend migration sample, platform cache/asset details, typed DTO error policy and current source import verification after code moves. Source inventory is complete at file-list level; compile-proof is intentionally pending.

## 7. Freeze proposal and honest status

- **V2 guiding architecture / module structure / six-category hierarchy / existing wire JSON / supported action set:** stable.
- **Pass 6 draft blueprint:** documented, subject to unresolved contract decisions and compile-oriented review.
- **PLAN FROZEN:** **NO**; owner agreement and the open contract table above are required, followed by a reconciled **PLAN FROZEN** revision.
- **Source implementation authorized:** **NO**; none has happened.

**Next:** review unresolved wire/behavior decisions as one concise batch; revise this precise create/modify/delete blueprint and cross-link authoritative project docs before marking PLAN FROZEN. Any new request to freeze does not waive unresolved contradictions.
