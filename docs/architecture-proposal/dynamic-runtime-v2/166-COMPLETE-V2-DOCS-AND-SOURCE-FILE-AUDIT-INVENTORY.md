# 166 — Source File and V2 Documentation Inventory for Mandatory New-Chat Audit

**Snapshot:** Frontend 169c2aa2ef2ec8ac71351be259f25aed3db363ca on architecture/dynamic-runtime-v2-discussion-freeze, 2026-10-26. The lists below are filenames from the recursive GitHub source tree, not substitute reviews. New chat MUST open the contents of every source class line by line, understand callers/callees/why each exists, and read every V2 document in full; use pagination/batched reads as needed. Commit changes only after source-level reconciliation.

## All Kotlin source/test files (224)

### app/ — 15 Kotlin files

~~~text
app/src/commonMain/kotlin/com/carbroz/cbpartner/app/ActiveToast.kt
app/src/commonMain/kotlin/com/carbroz/cbpartner/app/App.kt
app/src/commonMain/kotlin/com/carbroz/cbpartner/app/AppDialogEntry.kt
app/src/commonMain/kotlin/com/carbroz/cbpartner/app/ApplicationContent.kt
app/src/commonMain/kotlin/com/carbroz/cbpartner/app/DynamicSession.kt
app/src/commonMain/kotlin/com/carbroz/cbpartner/app/LocalDynamicPreview.kt
app/src/commonMain/kotlin/com/carbroz/cbpartner/app/LoggingConfiguration.kt
app/src/commonMain/kotlin/com/carbroz/cbpartner/app/RetainCompatibilityProbe.kt
app/src/commonTest/kotlin/com/carbroz/cbpartner/app/AppDialogFixtureParityTest.kt
app/src/commonTest/kotlin/com/carbroz/cbpartner/app/DynamicSessionEffectsTest.kt
app/src/commonTest/kotlin/com/carbroz/cbpartner/app/DynamicSessionLifecycleTest.kt
app/src/commonTest/kotlin/com/carbroz/cbpartner/app/LocalDynamicPreviewTest.kt
app/src/commonTest/kotlin/com/carbroz/cbpartner/app/LoggingConfigurationTest.kt
app/src/iosMain/kotlin/com/carbroz/cbpartner/app/Main.kt
app/src/jvmTest/kotlin/com/carbroz/cbpartner/app/DynamicSessionConcurrencyTest.kt
~~~

### androidApp/ — 2 Kotlin files

~~~text
androidApp/src/main/kotlin/com/carbroz/cbpartner/android/MainActivity.kt
androidApp/src/main/kotlin/com/carbroz/cbpartner/android/RuntimeMetadataProvider.kt
~~~

### webApp/ — 4 Kotlin files

~~~text
webApp/src/wasmJsMain/kotlin/com/carbroz/cbpartner/web/Main.kt
webApp/src/wasmJsMain/kotlin/com/carbroz/cbpartner/web/RuntimeMetadataProvider.kt
webApp/src/wasmJsMain/kotlin/com/carbroz/cbpartner/web/VisualLoginPreview.kt
webApp/src/wasmJsMain/kotlin/com/carbroz/cbpartner/web/WebDynamicPreview.kt
~~~

### core/ — 4 Kotlin files

~~~text
core/src/commonMain/kotlin/com/carbroz/cbpartner/core/diagnostics/DiagnosticId.kt
core/src/commonMain/kotlin/com/carbroz/cbpartner/core/diagnostics/TraceContext.kt
core/src/commonMain/kotlin/com/carbroz/cbpartner/core/runtime/RuntimeMetadata.kt
core/src/commonTest/kotlin/com/carbroz/cbpartner/core/diagnostics/DiagnosticIdTest.kt
~~~

### network/ — 12 Kotlin files

~~~text
network/src/commonMain/kotlin/com/carbroz/cbpartner/network/ApiBaseUrl.kt
network/src/commonMain/kotlin/com/carbroz/cbpartner/network/AppEnvironment.kt
network/src/commonMain/kotlin/com/carbroz/cbpartner/network/NetworkException.kt
network/src/commonMain/kotlin/com/carbroz/cbpartner/network/NetworkModule.kt
network/src/commonMain/kotlin/com/carbroz/cbpartner/network/NetworkResult.kt
network/src/commonMain/kotlin/com/carbroz/cbpartner/network/Paged.kt
network/src/commonMain/kotlin/com/carbroz/cbpartner/network/http/DefaultError.kt
network/src/commonMain/kotlin/com/carbroz/cbpartner/network/http/HttpClientBuilder.kt
network/src/commonMain/kotlin/com/carbroz/cbpartner/network/http/RequestHandler.kt
network/src/commonTest/kotlin/com/carbroz/cbpartner/network/ApiBaseUrlTest.kt
network/src/commonTest/kotlin/com/carbroz/cbpartner/network/http/RequestHandlerLoggingTest.kt
network/src/commonTest/kotlin/com/carbroz/cbpartner/network/http/RequestHandlerTest.kt
~~~

### domain/ — 48 Kotlin files

~~~text
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/DynamicDestination.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/bootstrap/BootstrapFailure.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/bootstrap/BootstrapOutput.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/DynamicCountdown.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/DynamicRequest.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/DynamicResponse.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/DynamicScreen.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/action/DynamicAction.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/action/DynamicBinding.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/component/DynamicComponent.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/component/StackComponent.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/content/DividerContent.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/content/ElementContent.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/content/IconContent.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/content/ImageContent.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/element/ButtonElement.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/element/DividerElement.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/element/DynamicElement.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/element/IconBadgeElement.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/element/IconElement.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/element/ImageElement.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/element/InputElement.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/element/TextElement.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/group/DynamicGroup.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/group/StackGroup.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/presentation/DynamicPresentation.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/section/DynamicListCatalog.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/section/DynamicSection.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/section/FormFieldSection.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/section/ListSection.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/section/StackSection.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/template/DynamicTemplate.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/template/FormTemplate.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/template/TemplateBackgroundImage.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/template/TemplateSlot.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/theme/DynamicTheme.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/validation/DynamicValidation.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/value/DynamicArrangement.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/value/DynamicAssetSource.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/value/DynamicDecorations.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/value/DynamicPadding.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/value/DynamicRawValue.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/value/DynamicSpacing.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/value/DynamicTextValues.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/value/DynamicUiStateValues.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/error/AppFailure.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/repository/BootstrapRepository.kt
domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/repository/DynamicRepository.kt
~~~

### data/ — 63 Kotlin files

~~~text
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/bootstrap/BootstrapRepositoryImpl.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/bootstrap/mapper/BootstrapMapper.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/bootstrap/model/BootstrapDtos.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/bootstrap/remote/BootstrapRemoteDataSource.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/di/BootstrapModule.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/di/DynamicDataModule.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/di/SerializationModule.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/DynamicRepositoryImpl.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/catalog/PartnerCountryCatalog.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/diagnostics/SanitizedResponseJson.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/diagnostics/SduiTraceProjection.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/mapper/ComponentMapper.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/mapper/DynamicActionMapper.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/mapper/DynamicResponseMapper.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/mapper/DynamicValueMapper.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/mapper/ElementContentMapper.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/mapper/ElementMapper.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/mapper/GroupMapper.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/mapper/PresentationMapper.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/mapper/SectionMapper.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/mapper/TemplateMapper.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/model/action/DynamicActionDto.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/model/component/StackComponentDto.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/model/element/ButtonElementDto.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/model/element/DividerElementDto.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/model/element/IconBadgeElementDto.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/model/element/IconElementDto.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/model/element/ImageElementDto.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/model/element/InputElementDto.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/model/element/TextElementDto.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/model/group/StackGroupDto.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/model/presentation/DynamicPresentationDto.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/model/response/DynamicDestinationDto.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/model/response/DynamicResponseDto.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/model/screen/DynamicCountdownDto.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/model/screen/DynamicScreenDto.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/model/section/FormFieldSectionDto.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/model/section/ListSectionDto.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/model/section/StackSectionDto.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/model/template/FormTemplateDto.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/model/template/TemplateSlotDto.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/model/theme/DynamicThemeDto.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/model/validation/DynamicValidationDto.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/remote/DynamicRemoteDataSource.kt
data/src/commonMain/kotlin/com/carbroz/cbpartner/data/error/NetworkFailureMapper.kt
data/src/commonTest/kotlin/com/carbroz/cbpartner/data/bootstrap/BootstrapMapperTest.kt
data/src/commonTest/kotlin/com/carbroz/cbpartner/data/bootstrap/BootstrapRemoteDataSourceTest.kt
data/src/commonTest/kotlin/com/carbroz/cbpartner/data/bootstrap/BootstrapRepositoryImplTest.kt
data/src/commonTest/kotlin/com/carbroz/cbpartner/data/bootstrap/remote/BootstrapRemoteDataSourceLoggingTest.kt
data/src/commonTest/kotlin/com/carbroz/cbpartner/data/di/NetworkModuleTest.kt
data/src/commonTest/kotlin/com/carbroz/cbpartner/data/dynamic/DynamicBackendFlowContractTest.kt
data/src/commonTest/kotlin/com/carbroz/cbpartner/data/dynamic/DynamicContractFixtures.kt
data/src/commonTest/kotlin/com/carbroz/cbpartner/data/dynamic/DynamicContractTest.kt
data/src/commonTest/kotlin/com/carbroz/cbpartner/data/dynamic/diagnostics/SanitizedResponseJsonTest.kt
data/src/commonTest/kotlin/com/carbroz/cbpartner/data/dynamic/diagnostics/SduiTraceProjectionTest.kt
data/src/commonTest/kotlin/com/carbroz/cbpartner/data/dynamic/mapper/BottomSheetLayoutContractTest.kt
data/src/commonTest/kotlin/com/carbroz/cbpartner/data/dynamic/mapper/DynamicCountdownMapperTest.kt
data/src/commonTest/kotlin/com/carbroz/cbpartner/data/dynamic/mapper/DynamicResponseMapperTest.kt
data/src/commonTest/kotlin/com/carbroz/cbpartner/data/dynamic/mapper/DynamicValidationMessageContractTest.kt
data/src/commonTest/kotlin/com/carbroz/cbpartner/data/dynamic/mapper/GenericDialogContractTest.kt
data/src/commonTest/kotlin/com/carbroz/cbpartner/data/dynamic/mapper/InputSurfaceLayoutContractTest.kt
data/src/commonTest/kotlin/com/carbroz/cbpartner/data/dynamic/remote/DynamicRemoteDataSourceTest.kt
data/src/commonTest/kotlin/com/carbroz/cbpartner/data/error/NetworkFailureMapperTest.kt
~~~

### navigation/ — 4 Kotlin files

~~~text
navigation/src/commonMain/kotlin/com/carbroz/cbpartner/navigation/DynamicNavigationEntry.kt
navigation/src/commonMain/kotlin/com/carbroz/cbpartner/navigation/DynamicNavigationHost.kt
navigation/src/commonMain/kotlin/com/carbroz/cbpartner/navigation/DynamicNavigationState.kt
navigation/src/commonTest/kotlin/com/carbroz/cbpartner/navigation/DynamicNavigationStateTest.kt
~~~

### feature/dynamic/ — 63 Kotlin files

~~~text
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/DynamicContainer.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/DynamicModule.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/action/DismissActionHandler.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/action/DynamicActionExecutor.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/action/DynamicActionHandler.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/action/DynamicActionResult.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/action/ExternalUriActionHandler.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/action/NavigateActionHandler.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/action/PresentActionHandler.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/action/RequestActionHandler.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/action/SequenceActionHandler.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/action/StateActionHandler.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/presentation/DynamicPresentationHost.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/registry/DynamicRegistry.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/registry/DynamicRendererContracts.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/registry/DynamicRendererRegistration.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/renderer/DynamicRenderer.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicCountdownClock.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicFormValidator.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicLayout.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicListItems.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicReferenceResolver.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicRenderContext.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicRuntimeState.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/DynamicGestureModifier.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/asset/DynamicAssetImage.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/asset/DynamicPackagedAssets.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/component/StackComponent.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/content/ElementContent.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/content/ImageContent.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/element/ButtonElement.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/element/DecorationElements.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/element/DividerElement.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/element/IconBadgeElement.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/element/ImageElement.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/element/InputElement.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/element/TextElement.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/group/StackGroup.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/section/FormFieldSection.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/section/ListSection.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/section/StackSection.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/template/FormTemplate.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/template/TemplateShell.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/store/DynamicScreenState.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/store/DynamicStore.kt
feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/usecase/LoadDynamicScreenUseCase.kt
feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/action/DynamicActionExecutorTest.kt
feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/action/NavigateActionHandlerTest.kt
feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/action/PresentDismissActionHandlerTest.kt
feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/action/RequestActionHandlerTest.kt
feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/action/StateActionHandlerTest.kt
feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/registry/DynamicRegistryTest.kt
feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicCountdownClockTest.kt
feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicFormValidatorTest.kt
feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicListItemsTest.kt
feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicReferenceResolverTest.kt
feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicRuntimeStateTest.kt
feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/asset/DynamicAssetImageTest.kt
feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/element/SegmentedEditTest.kt
feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/section/FormFieldSectionContractTest.kt
feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/template/TemplateBackgroundAlignmentTest.kt
feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/store/DynamicCountdownStoreTest.kt
feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/store/DynamicStoreTest.kt
~~~

### feature/splash/ — 5 Kotlin files

~~~text
feature/splash/src/commonMain/kotlin/com/carbroz/cbpartner/feature/splash/SplashModule.kt
feature/splash/src/commonMain/kotlin/com/carbroz/cbpartner/feature/splash/SplashScreen.kt
feature/splash/src/commonMain/kotlin/com/carbroz/cbpartner/feature/splash/SplashStore.kt
feature/splash/src/commonMain/kotlin/com/carbroz/cbpartner/feature/splash/SplashTheme.kt
feature/splash/src/commonTest/kotlin/com/carbroz/cbpartner/feature/splash/SplashStoreTest.kt
~~~

### build-logic/ — 4 Kotlin files

~~~text
build-logic/src/main/kotlin/com/carbroz/cbpartner/buildlogic/AndroidApplicationConventionPlugin.kt
build-logic/src/main/kotlin/com/carbroz/cbpartner/buildlogic/ComposeConventionPlugin.kt
build-logic/src/main/kotlin/com/carbroz/cbpartner/buildlogic/KmpConventionPlugin.kt
build-logic/src/main/kotlin/com/carbroz/cbpartner/buildlogic/WebApplicationConventionPlugin.kt
~~~

## Every Markdown document in authoritative V2 corpus (163)

~~~text
docs/architecture-proposal/dynamic-runtime-v2/01-PRINCIPLES-AND-MODULES.md
docs/architecture-proposal/dynamic-runtime-v2/02-BOOTSTRAP-NETWORK-AND-CONTRACTS.md
docs/architecture-proposal/dynamic-runtime-v2/03-DYNAMIC-STRUCTURE-AND-RENDERING.md
docs/architecture-proposal/dynamic-runtime-v2/04-RUNTIME-BINDINGS-AND-ACTIONS.md
docs/architecture-proposal/dynamic-runtime-v2/05-NAVIGATION-AND-PLATFORM-LIFECYCLE.md
docs/architecture-proposal/dynamic-runtime-v2/06-TESTING-AND-PLANNING-GATES.md
docs/architecture-proposal/dynamic-runtime-v2/07-FULL-REPLACEMENT-MIGRATION-PLAN.md
docs/architecture-proposal/dynamic-runtime-v2/08-SOURCE-RECONCILIATION-AND-CLASS-CONTRACTS.md
docs/architecture-proposal/dynamic-runtime-v2/09-NETWORK-BOOTSTRAP-SPLASH-SOURCE-AUDIT.md
docs/architecture-proposal/dynamic-runtime-v2/10-DOMAIN-DTO-MAPPER-SOURCE-AUDIT.md
docs/architecture-proposal/dynamic-runtime-v2/11-PASS3-FIELD-MATRIX-AND-EARLIER-PASS-RECHECK.md
docs/architecture-proposal/dynamic-runtime-v2/12-CANONICAL-JSON-FIXED-CONTRACT.md
docs/architecture-proposal/dynamic-runtime-v2/13-PASS4-ACTIONS-RUNTIME-STORE-SOURCE-AUDIT.md
docs/architecture-proposal/dynamic-runtime-v2/14-PASS4-FINAL-VERIFICATION.md
docs/architecture-proposal/dynamic-runtime-v2/15-PASS5-RENDERERS-THEME-PLATFORMS-AUDIT.md
docs/architecture-proposal/dynamic-runtime-v2/16-PASS5-FINAL-AND-PASS1-5-CROSS-AUDIT.md
docs/architecture-proposal/dynamic-runtime-v2/17-PASS6-EXACT-IMPLEMENTATION-BLUEPRINT-DRAFT.md
docs/architecture-proposal/dynamic-runtime-v2/18-PASS6-CONTRACT-CLOSURE-AND-FREEZE-REVIEW.md
docs/architecture-proposal/dynamic-runtime-v2/19-BACKEND-MIGRATION-INDEPENDENT-CONTRACT.md
docs/architecture-proposal/dynamic-runtime-v2/20-PLAN-FROZEN-V2-IMPLEMENTATION-CONTRACT.md
docs/architecture-proposal/dynamic-runtime-v2/21-W0-IMPLEMENTATION-PREFLIGHT.md
docs/architecture-proposal/dynamic-runtime-v2/22-W0-SOURCE-EVIDENCE-KDOC-LOGGING-ENVIRONMENTS.md
docs/architecture-proposal/dynamic-runtime-v2/23-W0-ADDENDA-FROZEN-KDOC-LOGGING-ENVIRONMENTS.md
docs/architecture-proposal/dynamic-runtime-v2/24-W1-NETWORK-FIRST-SLICE-IMPLEMENTATION.md
docs/architecture-proposal/dynamic-runtime-v2/25-W1-BEARER-SOURCE-EVIDENCE-AND-TESTS.md
docs/architecture-proposal/dynamic-runtime-v2/26-W1-FROZEN-PLAN-TO-SOURCE-TRACEABILITY.md
docs/architecture-proposal/dynamic-runtime-v2/27-W1-ENDPOINT-ERROR-AUTH-SOURCE-PLAN-AUDIT.md
docs/architecture-proposal/dynamic-runtime-v2/28-W1-MOCKENGINE-CHARACTERIZATION-GATE.md
docs/architecture-proposal/dynamic-runtime-v2/29-W1-ENDPOINT-RED-RECONCILIATION.md
docs/architecture-proposal/dynamic-runtime-v2/30-W1-ERROR-AUTH-CALLER-EVIDENCE.md
docs/architecture-proposal/dynamic-runtime-v2/31-W1-TIMEOUT-TYPED-ERRORS-AUTH-OWNERSHIP.md
docs/architecture-proposal/dynamic-runtime-v2/32-W1-CONNECTION-TYPED-FAILURE-AUTH-OWNER.md
docs/architecture-proposal/dynamic-runtime-v2/33-W1-SECURE-BEARER-LOGIN-OTP-SESSION-RECONCILIATION.md
docs/architecture-proposal/dynamic-runtime-v2/34-BACKEND-MIGRATION-MASTER-CONTRACT-AND-READINESS.md
docs/architecture-proposal/dynamic-runtime-v2/35-W1-BEARER-BLANK-CREDENTIAL-FAIL-CLOSED.md
docs/architecture-proposal/dynamic-runtime-v2/36-W2-BOOTSTRAP-SPLASH-SOURCE-AUDIT-LIFECYCLE.md
docs/architecture-proposal/dynamic-runtime-v2/37-W2-MAINTENANCE-REQUIRED-UPDATE-GATE.md
docs/architecture-proposal/dynamic-runtime-v2/38-W2-STARTUP-ERROR-SANITIZATION.md
docs/architecture-proposal/dynamic-runtime-v2/39-W2-SPLASH-HANDOFF-BOOTSTRAP-ERROR-CLEANUP.md
docs/architecture-proposal/dynamic-runtime-v2/40-W2-APPLICATION-BOOTSTRAP-BOUNDARY-RECONCILIATION.md
docs/architecture-proposal/dynamic-runtime-v2/41-W2-APPROVED-DIRECT-BOOTSTRAP-REPOSITORY.md
docs/architecture-proposal/dynamic-runtime-v2/42-W2-REQUIRED-UPDATE-URI-REUSE-SOURCE-AUDIT.md
docs/architecture-proposal/dynamic-runtime-v2/43-W2-FINAL-SOURCE-AUDIT-AND-VERIFICATION-REGISTER.md
docs/architecture-proposal/dynamic-runtime-v2/44-W3-SIX-CATEGORY-SOURCE-RECONCILIATION.md
docs/architecture-proposal/dynamic-runtime-v2/45-W3-A-SHARED-TEMPLATE-SLOT-IMPLEMENTATION.md
docs/architecture-proposal/dynamic-runtime-v2/46-W3-B-IMAGE-CONTENT-VALIDATION.md
docs/architecture-proposal/dynamic-runtime-v2/47-W3-FINAL-SOURCE-AND-FROZEN-CONTRACT-RECONCILIATION.md
docs/architecture-proposal/dynamic-runtime-v2/48-W4-NAVIGATION-SESSION-SOURCE-AUDIT.md
docs/architecture-proposal/dynamic-runtime-v2/49-W4-EXACT-OWNERSHIP-AND-FILE-PLAN-DRAFT.md
docs/architecture-proposal/dynamic-runtime-v2/50-W4-FREEZE-CANDIDATE-OWNERSHIP-SEMANTICS.md
docs/architecture-proposal/dynamic-runtime-v2/51-W4-PLAN-FREEZE-REVIEW-AND-IMPLEMENTATION-GATES.md
docs/architecture-proposal/dynamic-runtime-v2/52-W4-A-NAVIGATION-ID-MODE-IMPLEMENTATION.md
docs/architecture-proposal/dynamic-runtime-v2/53-W4-B-SESSION-EFFECT-HANDOFF.md
docs/architecture-proposal/dynamic-runtime-v2/54-W4-B-ERROR-W4-C-ENTRY-BINDINGS.md
docs/architecture-proposal/dynamic-runtime-v2/55-W4-D-FOCUSED-INTEGRATION-AND-RECONCILIATION.md
docs/architecture-proposal/dynamic-runtime-v2/56-ACTIONS-SOURCE-AUDIT-AND-REFACTOR-PLAN-DRAFT.md
docs/architecture-proposal/dynamic-runtime-v2/57-ACTIONS-SEQUENCE-INTENT-CORRECTION-AND-CONTRACT-REVIEW.md
docs/architecture-proposal/dynamic-runtime-v2/58-ACTIONS-ARCHITECTURE-CLEANUP-AND-SEQUENCE-REMOVAL-PLAN-DRAFT.md
docs/architecture-proposal/dynamic-runtime-v2/59-ACTIONS-SEQUENCE-REMOVAL-CROSS-REPO-RECONCILIATION.md
docs/architecture-proposal/dynamic-runtime-v2/60-ACTIONS-SEQUENCE-REMOVAL-CONTRACT-FREEZE.md
docs/architecture-proposal/dynamic-runtime-v2/61-INERT-SEQUENCE-FRONTEND-IMPLEMENTATION-AND-DEFERRED-BACKEND-MIGRATION.md
docs/architecture-proposal/dynamic-runtime-v2/62-REQUEST-ACTION-SOURCE-AUDIT-AND-EXACT-PLAN-DRAFT.md
docs/architecture-proposal/dynamic-runtime-v2/63-REQUEST-ACTION-CONTRACT-FREEZE-CANDIDATE.md
docs/architecture-proposal/dynamic-runtime-v2/64-REQUEST-ACTION-PLAN-FROZEN.md
docs/architecture-proposal/dynamic-runtime-v2/65-REQUEST-ACTION-POST-JVM-RECONCILIATION-AND-FOLLOWUP-AUDIT.md
docs/architecture-proposal/dynamic-runtime-v2/66-REQUEST-JVM-WEB-VERIFIED-NAVIGATION-PRESENTATION-AUDIT-DRAFT.md
docs/architecture-proposal/dynamic-runtime-v2/67-NAVIGATION-PRESENTATION-WIRE-RECONCILIATION-AND-CLEANUP-PLAN-DRAFT.md
docs/architecture-proposal/dynamic-runtime-v2/68-NAVIGATION-PRESENTATION-CONTRACT-FREEZE-CANDIDATE.md
docs/architecture-proposal/dynamic-runtime-v2/69-NAVIGATION-PRESENTATION-PLAN-FROZEN.md
docs/architecture-proposal/dynamic-runtime-v2/70-N1-DISMISS-IMPLEMENTATION-VERIFIED-AND-STATE-AUDIT-HANDOFF.md
docs/architecture-proposal/dynamic-runtime-v2/71-STATE-ACTION-SOURCE-AUDIT-AND-EXACT-PLAN-DRAFT.md
docs/architecture-proposal/dynamic-runtime-v2/72-SHARED-ACTION-INFRASTRUCTURE-SRP-SOURCE-AUDIT-DRAFT.md
docs/architecture-proposal/dynamic-runtime-v2/73-RESOLVER-USAGE-INVENTORY-AND-TEST-PARITY-REVIEW.md
docs/architecture-proposal/dynamic-runtime-v2/74-RESOLVER-CONSOLIDATION-EXACT-PLAN-FREEZE-CANDIDATE.md
docs/architecture-proposal/dynamic-runtime-v2/75-RESOLVER-CONSOLIDATION-PLAN-FROZEN.md
docs/architecture-proposal/dynamic-runtime-v2/76-RESOLVER-CONSOLIDATION-POST-VERIFICATION-RECONCILIATION.md
docs/architecture-proposal/dynamic-runtime-v2/77-EFFECT-BATCHING-ERROR-OWNERSHIP-SOURCE-AUDIT-DRAFT.md
docs/architecture-proposal/dynamic-runtime-v2/78-EFFECT-ERROR-REFERENCE-INVENTORY-AND-DECISION-DRAFT.md
docs/architecture-proposal/dynamic-runtime-v2/79-EFFECT-ERROR-INVENTORY-CLOSED-CLEANUP-DECISION-CANDIDATE.md
docs/architecture-proposal/dynamic-runtime-v2/80-EFFECT-FAILED-C1-CLEANUP-PLAN-FROZEN.md
docs/architecture-proposal/dynamic-runtime-v2/81-C1-POST-VERIFICATION-RECONCILIATION.md
docs/architecture-proposal/dynamic-runtime-v2/82-LOGGING-DISCUSSION-AND-NEXT-CHAT-HANDOFF.md
docs/architecture-proposal/dynamic-runtime-v2/83-V1.1-LIFECYCLE-LOGGING-001-PLAN-FROZEN.md
docs/architecture-proposal/dynamic-runtime-v2/84-DYNAMIC-SESSION-HOST-RETENTION-PROOF.md
docs/architecture-proposal/dynamic-runtime-v2/85-DYNAMIC-SESSION-LIFECYCLE-001-PLAN-FROZEN.md
docs/architecture-proposal/dynamic-runtime-v2/86-G1-RETENTION-CLEANUP-PROOF.md
docs/architecture-proposal/dynamic-runtime-v2/87-G1-G2-RED-TEST-SOURCE-PREPARATION.md
docs/architecture-proposal/dynamic-runtime-v2/88-G1-G2-EFFECT-ACCEPT-CLAIM-ACK-API-REVIEW.md
docs/architecture-proposal/dynamic-runtime-v2/89-G1-G2-SESSION-API-D1-D4-FROZEN.md
docs/architecture-proposal/dynamic-runtime-v2/90-G1B-G2B-RED-TEST-CASES-AND-PRODUCTION-DIFF-REVIEW.md
docs/architecture-proposal/dynamic-runtime-v2/91-G1-G2-DYNAMIC-SESSION-EXACT-API-FREEZE.md
docs/architecture-proposal/dynamic-runtime-v2/92-G2B-RED-REVIEW-AND-EXACT-IMPLEMENTATION-PLAN.md
docs/architecture-proposal/dynamic-runtime-v2/93-S1-SOURCE-AUDIT-AND-REMAINING-GATES.md
docs/architecture-proposal/dynamic-runtime-v2/94-S1-FINAL-SYNCHRONIZATION-COMMIT-SAFETY-AUDIT.md
docs/architecture-proposal/dynamic-runtime-v2/95-S1-FINAL-FREEZE-AND-PRODUCTIVITY-RULE.md
docs/architecture-proposal/dynamic-runtime-v2/96-G1B-S2-CONSOLIDATED-IMPLEMENTATION-PLAN.md
docs/architecture-proposal/dynamic-runtime-v2/97-W5-ISOLATED-VISUAL-PREVIEW.md
docs/architecture-proposal/dynamic-runtime-v2/98-W6A-EVENT-HOOK-DISPATCH-PLAN-FROZEN.md
docs/architecture-proposal/dynamic-runtime-v2/99-W6A-ISOLATED-EVENT-PREVIEW.md
docs/architecture-proposal/dynamic-runtime-v2/100-W6A-KEYBOARD-FOCUS-EXTENSION-FROZEN.md
docs/architecture-proposal/dynamic-runtime-v2/101-W6B-ACTION-CROSS-REPO-GAP-MATRIX-DRAFT.md
docs/architecture-proposal/dynamic-runtime-v2/102-W6B-FINAL-RECONCILIATION-AND-EXACT-FRONTEND-PLAN-DRAFT.md
docs/architecture-proposal/dynamic-runtime-v2/103-V2-CLEANUP-TRACEABILITY-AND-OPEN-GATES.md
docs/architecture-proposal/dynamic-runtime-v2/104-W7-ENVIRONMENT-PLATFORM-INTEGRATION-SOURCE-AUDIT.md
docs/architecture-proposal/dynamic-runtime-v2/105-W7-ENV001-EXACT-IMPLEMENTATION-PLAN-DRAFT.md
docs/architecture-proposal/dynamic-runtime-v2/106-W7-ENV-LOG-HANDOFF-2026-10-17.md
docs/architecture-proposal/dynamic-runtime-v2/107-LOCAL-DEVELOPMENT-SDUI-INSPECTION-EXCEPTION-FROZEN.md
docs/architecture-proposal/dynamic-runtime-v2/108-L1-HYBRID-LOCAL-INSPECTION-EXACT-PLAN-FROZEN.md
docs/architecture-proposal/dynamic-runtime-v2/109-T1-TERMINAL-API-IDENTITY-CORRELATION-PLAN-FROZEN.md
docs/architecture-proposal/dynamic-runtime-v2/110-T2-SDUI-STRUCTURE-DECODE-MAPPING-DIAGNOSTICS-PLAN.md
docs/architecture-proposal/dynamic-runtime-v2/111-LIVE-EXECUTION-TRACE-CONSOLIDATED-PLAN-FROZEN.md
docs/architecture-proposal/dynamic-runtime-v2/112-LOGIN-COUNTRY-PICKER-PHASE1-3-CONSOLIDATED-FREEZE-AND-AUDIT-HANDOFF.md
docs/architecture-proposal/dynamic-runtime-v2/113-B4-GENERIC-BUTTON-ORIGIN-AND-LOADING-CONTRACT-FROZEN.md
docs/architecture-proposal/dynamic-runtime-v2/114-B5-L10-EMPTY-SECTION-REFERENCE-SOURCE-AUDIT-AND-EXACT-PLAN-DRAFT.md
docs/architecture-proposal/dynamic-runtime-v2/115-B5-L10-EMPTY-SECTION-REFERENCE-PLAN-FROZEN.md
docs/architecture-proposal/dynamic-runtime-v2/116-B6-REMAINING-BEHAVIOR-AND-PHASE4-DEPENDENCY-SOURCE-AUDIT-DRAFT.md
docs/architecture-proposal/dynamic-runtime-v2/117-B6-CONTRACT-GATES-SOURCE-OWNERSHIP-AND-VERIFICATION-SCOPE-FROZEN.md
docs/architecture-proposal/dynamic-runtime-v2/118-L01-L15-CONSOLIDATED-ONE-WAVE-IMPLEMENTATION-PLAN-FROZEN.md
docs/architecture-proposal/dynamic-runtime-v2/119-LOGIN-L01-L15-ONE-WAVE-STATUS-AND-NEW-CHAT-HANDOFF-2026-10-22.md
docs/architecture-proposal/dynamic-runtime-v2/120-LOGIN-THEME-TEMPLATE-IMAGE-OWNERSHIP-2026-10-22.md
docs/architecture-proposal/dynamic-runtime-v2/121-OTP-UI-FIRST-CONTRACT-FREEZE-AND-DEFERRED-TIMER-RESEND-2026-10-22.md
docs/architecture-proposal/dynamic-runtime-v2/122-OTP-UI-V2-VALIDATION-SCOPE-AND-DIRECT-RESEND-DECISIONS-2026-10-22.md
docs/architecture-proposal/dynamic-runtime-v2/124-LOGIN-COUNTRY-PICKER-REGRESSION-RECOVERY-2026-10-22.md
docs/architecture-proposal/dynamic-runtime-v2/125-LOGIN-COUNTRY-PICKER-DYNAMIC-PARITY-AUDIT-AND-BINDED-SHEET-2026-10-22.md
docs/architecture-proposal/dynamic-runtime-v2/126-LOGIN-OTP-DYNAMIC-PARITY-RECOVERY-AND-NEXT-CHAT-HANDOFF-2026-10-23.md
docs/architecture-proposal/dynamic-runtime-v2/127-NEXT-CHAT-STARTER-PROMPT-LOGIN-OTP-2026-10-23.md
docs/architecture-proposal/dynamic-runtime-v2/128-LOGIN-COUNTRY-PICKER-JSON-V4-VISUAL-PARITY-CORRECTIONS-2026-10-23.md
docs/architecture-proposal/dynamic-runtime-v2/129-LOGIN-P0-INPUT-SURFACE-AND-ALIGNMENT-2026-10-23.md
docs/architecture-proposal/dynamic-runtime-v2/130-LOGIN-P1-LOCAL-ASSETS-AND-TEMPLATE-HERO-2026-10-23.md
docs/architecture-proposal/dynamic-runtime-v2/131-LOGIN-V7-VALIDATION-MESSAGES-RESERVED-HEIGHT-2026-10-23.md
docs/architecture-proposal/dynamic-runtime-v2/132-P0-FORM-FIELD-SECTION-EXACT-CONTRACT-SRP-AUDIT-AND-FREEZE-2026-10-23.md
docs/architecture-proposal/dynamic-runtime-v2/133-P0-FORM-FIELD-SECTION-IMPLEMENTATION-AND-TEST-HANDOFF-2026-10-23.md
docs/architecture-proposal/dynamic-runtime-v2/134-LOGIN-P0-FORMFIELD-VALIDATION-ON-EDIT-AND-SPACING-2026-10-23.md
docs/architecture-proposal/dynamic-runtime-v2/135-LOGIN-V10-DESIGN-SPEC-PALETTE-TYPOGRAPHY-RECONCILIATION-2026-10-23.md
docs/architecture-proposal/dynamic-runtime-v2/136-LOGIN-V10-HERO-02-ASSET-INTEGRATION-2026-10-23.md
docs/architecture-proposal/dynamic-runtime-v2/137-ANDROID-NATIVE-LOGIN-V10-DEVELOPMENT-PREVIEW-2026-10-23.md
docs/architecture-proposal/dynamic-runtime-v2/138-LOGIN-V11-CONDITIONAL-ERROR-AND-COMPREHENSIVE-VISUAL-AUDIT-2026-10-23.md
docs/architecture-proposal/dynamic-runtime-v2/139-ANDROID-NAVIGATION-BAR-SAFE-AREA-HOST-FIX-2026-10-23.md
docs/architecture-proposal/dynamic-runtime-v2/140-2026-10-23-COMPLETE-HANDOFF-COMMON-FIRST-VALIDATION-LOGIN-PARITY.md
docs/architecture-proposal/dynamic-runtime-v2/141-NEXT-CHAT-STARTER-PROMPT-COMMON-FIRST-SAFE-AREA-VALIDATION-LOGIN-PARITY-2026-10-23.md
docs/architecture-proposal/dynamic-runtime-v2/142-SAFE-AREA-P0-IME-ANDROID-VERIFICATION-AND-REMAINING-GATES-2026-10-23.md
docs/architecture-proposal/dynamic-runtime-v2/143-INDIA-ONLY-PHONE-VALIDATION-INTERIM-DECISION-2026-10-23.md
docs/architecture-proposal/dynamic-runtime-v2/144-LOGIN-V12-VISUAL-PARITY-CANDIDATE-2026-10-23.md
docs/architecture-proposal/dynamic-runtime-v2/145-LOGIN-V13-TYPOGRAPHY-COLOR-JSON-ONLY-CANDIDATE-2026-10-23.md
docs/architecture-proposal/dynamic-runtime-v2/146-LOGIN-V15-ASSETS-AND-INLINE-ERROR-ICON-2026-10-24.md
docs/architecture-proposal/dynamic-runtime-v2/148-GENERIC-VALIDATION-EQUALITY-LOGIN-V19-2026-10-24.md
docs/architecture-proposal/dynamic-runtime-v2/149-PARTNER-PHONE-VALIDATOR-REMOVAL-TEST-FIX-2026-10-24.md
docs/architecture-proposal/dynamic-runtime-v2/150-DECLARATIVE-VALIDATION-NAMED-REGISTRY-RETIREMENT-2026-10-24.md
docs/architecture-proposal/dynamic-runtime-v2/151-OTP-V3-LOGIN-PARITY-NO-RESERVED-ERROR-CLOCK-ALIAS-2026-10-24.md
docs/architecture-proposal/dynamic-runtime-v2/152-OTP-V3-LOGIN-V19-PARITY-CONTRACT-DRAFT-2026-10-24.md
docs/architecture-proposal/dynamic-runtime-v2/153-OTP-V3-GENERIC-RUNTIME-PREVIEW-INTEGRATION-2026-10-24.md
docs/architecture-proposal/dynamic-runtime-v2/154-OTP-V3-A35-VISUAL-KEYBOARD-PARITY-2026-10-24.md
docs/architecture-proposal/dynamic-runtime-v2/155-GENERIC-COUNTDOWN-RESEND-OTP-MINIMAL-CONTRACT-FROZEN-2026-10-24.md
docs/architecture-proposal/dynamic-runtime-v2/156-OTP-RESEND-SUCCESS-FAILURE-VERIFICATION-AND-BACKEND-READINESS-2026-10-25.md
docs/architecture-proposal/dynamic-runtime-v2/157-LOGIN-OTP-BACKEND-FRONTEND-EXACT-CONTRACT-RECONCILIATION-FREEZE-2026-10-25.md
docs/architecture-proposal/dynamic-runtime-v2/158-2026-10-25-COMPLETE-CHAT-HANDOFF-ARCHITECTURE-RUNTIME-AND-INTEGRATION-GATES.md
docs/architecture-proposal/dynamic-runtime-v2/159-NEW-CHAT-STARTER-PROMPT-2026-10-25-BACKEND-INTEGRATION-AND-V2-ARCHITECTURE.md
docs/architecture-proposal/dynamic-runtime-v2/160-APPLICATION-WIDE-GENERIC-DIALOG-V1-IMPLEMENTATION-PLAN.md
docs/architecture-proposal/dynamic-runtime-v2/161-GENERIC-TOAST-PRESENTATION-V1-FREEZE.md
docs/architecture-proposal/dynamic-runtime-v2/162-GENERIC-TOAST-V1-1-NAVIGATING-SUCCESS-AMENDMENT.md
docs/architecture-proposal/dynamic-runtime-v2/163-ORDERED-MIXED-CHILDREN-JSON-DOMAIN-MAPPER-CONTRACT-FREEZE.md
docs/architecture-proposal/dynamic-runtime-v2/164-PARTNER-TYPE-SELECTION-ORDERED-JSON-REVIEW.md
docs/architecture-proposal/dynamic-runtime-v2/README.md
~~~

## Required ancillary non-Kotlin artifacts

Read root/settings Gradle conventions, Android Manifest / packaged preview assets, Web resources, app composeResources, and all frozen JSON fixtures. For exact canonical versions: partner-login-country-picker-parity-v19.json, partner-otp-ui-contract-v3.json, partner-type-selection-ordered-v1-CANDIDATE.json, six dialog-v1-*.json plus two _startup.json, Toast samples, bootstrap scenarios and related tests. Inspect resource registry and confirm each referenced image is actually bundled. Inspect Backend read-only PartnerType.ts, partner routes/controller/DTO, strict SDUI schema/builder/action, auth send/verify DTO and bootstrap only as needed to validate open contracts.

## Module responsibility quick index

Core = diagnostics + runtime metadata; Network = shared HTTP and typed errors; Domain = typed contracts; Data = repositories, DTOs, mappers, catalog/error mapping; Navigation = entry/host/state; feature/splash = bootstrap state/UI; feature/dynamic = Store/actions/validators/registry/renderers; app = DI/composition/effects/preview/dialog/toast; androidApp and webApp = platform entry/preview fixtures; build-logic = conventions. For exact responsibilities, read real source and KDoc.

## Audit and discrepancy checklist

Confirm each class's input/output, direct caller, reason for existence, dependency direction, risk of duplicate abstraction and tests. Inspect DTO→Domain mapping, ordered children traversal and backward compatibility; all six category registry entries; actions and effect claim/ack cancellation; lifecycle retention; presentation retry/Cancel; security redaction; preview-only branches and production path. Class inventory is only a map. Never claim that reading filenames = reading classes.
