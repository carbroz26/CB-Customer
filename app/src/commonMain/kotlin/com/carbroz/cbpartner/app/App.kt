package com.carbroz.cbpartner.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.retain.RetainedEffect
import com.carbroz.cbpartner.core.runtime.RuntimeMetadataProvider
import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicListCatalog
import com.carbroz.cbpartner.app.validation.PartnerPhoneNumberValidator
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicValidator
import com.carbroz.cbpartner.data.di.bootstrapModule
import com.carbroz.cbpartner.data.di.dynamicDataModule
import com.carbroz.cbpartner.data.di.serializationModule
import com.carbroz.cbpartner.feature.dynamic.dynamicModule
import com.carbroz.cbpartner.feature.splash.splashModule
import com.carbroz.cbpartner.network.ApiBaseUrl
import com.carbroz.cbpartner.network.AppEnvironment
import com.carbroz.cbpartner.network.networkModule
import org.koin.dsl.koinApplication
import org.koin.dsl.module

@Composable
fun App(
    runtimeMetadataProvider: RuntimeMetadataProvider,
    apiBaseUrl: ApiBaseUrl,
    environment: AppEnvironment = AppEnvironment.Development,
    webLoginPreview: WebLoginPreview? = null,
) {
    // Preview is impossible to enable outside the development environment.
    val preview = webLoginPreview?.takeIf { environment == AppEnvironment.Development }
    val retainedSession = retain {
        mutableStateOf<DynamicSession?>(
            preview?.let {
                val screen = requireNotNull(it.response.data) { "Web preview requires a Dynamic screen" }
                DynamicSession(
                    diagnosticSessionId = "web-login-preview",
                    initialDestination = DynamicDestination(
                        screenId = screen.screenId,
                        templateId = screen.template.id,
                        templateType = screen.template.type,
                        endpoint = it.endpoint,
                    ),
                ).also { session ->
                    session.runtimeState.setAll(it.initialState)
                }
            },
        )
    }
    RetainedEffect(retainedSession) {
        onRetire { retainedSession.value?.close() }
    }
    val koin = remember(runtimeMetadataProvider, apiBaseUrl, environment, preview) {
        koinApplication {
            modules(
                module {
                    single<RuntimeMetadataProvider> { runtimeMetadataProvider }
                    factory<DynamicValidator> { PartnerPhoneNumberValidator() }
                },
                networkModule(apiBaseUrl),
                serializationModule,
                bootstrapModule,
                // In offline preview, preserve the same Store, mapper, use case, renderer and
                // action executor; replace only their network-backed repository and catalog.
                if (preview == null) dynamicDataModule else module {
                    single<DynamicRepository> {
                        object : DynamicRepository {
                            override suspend fun fetch(
                                request: com.carbroz.cbpartner.domain.model.dynamic.DynamicRequest,
                            ) = if (request.endpoint == preview.endpoint) {
                                Result.success(preview.response)
                            } else {
                                Result.failure<com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse>(
                                    IllegalStateException("Offline preview cannot send network requests"),
                                )
                            }
                        }
                    }
                    single<DynamicListCatalog> {
                        DynamicListCatalog { key ->
                            if (key == "countries") preview.countries.map { it.toMap() } else null
                        }
                    }
                },
                dynamicModule,
                splashModule,
            )
        }
    }

    DisposableEffect(koin) {
        onDispose { koin.close() }
    }

    ApplicationContent(koin.koin, runtimeMetadataProvider, retainedSession, apiBaseUrl.toString())
}

