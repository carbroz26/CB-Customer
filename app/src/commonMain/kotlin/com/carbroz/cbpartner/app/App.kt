package com.carbroz.cbpartner.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.retain.RetainedEffect
import com.carbroz.cbpartner.core.runtime.RuntimeMetadataProvider
import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import com.carbroz.cbpartner.domain.repository.BootstrapRepository
import com.carbroz.cbpartner.domain.model.error.AppFailure
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicListCatalog
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
    localDynamicPreview: LocalDynamicPreview? = null,
    localBootstrapPreview: LocalBootstrapPreview? = null,
) {
    // Preview is impossible to enable outside the development environment.
    val preview = localDynamicPreview?.takeIf { environment == AppEnvironment.Development }
    val bootPreview = localBootstrapPreview?.takeIf { environment == AppEnvironment.Development }
    val retainedSession = retain {
        mutableStateOf<DynamicSession?>(
            preview?.takeIf { bootPreview == null }?.let {
                val screen = requireNotNull(it.response.data) { "Local preview requires a Dynamic screen" }
                DynamicSession(
                    diagnosticSessionId = "local-dynamic-preview",
                    initialDestination = DynamicDestination(
                        screenId = screen.screenId,
                        templateId = screen.template.id,
                        templateType = screen.template.type,
                        endpoint = LOCAL_PREVIEW_DYNAMIC_ENDPOINT,
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
    // One DI graph per retained app lifetime, not per Activity configuration instance.
    val koin = retain {
        koinApplication {
            modules(
                module {
                    single<RuntimeMetadataProvider> { runtimeMetadataProvider }
                },
                networkModule(apiBaseUrl),
                serializationModule,
                if (bootPreview == null) bootstrapModule else module {
                    single<BootstrapRepository> {
                        object : BootstrapRepository {
                            override suspend fun fetch() = bootPreview.outcome
                        }
                    }
                },
                // In offline preview, preserve the same Store, mapper, use case, renderer and
                // action executor; replace only their network-backed repository and catalog.
                if (preview == null) dynamicDataModule else module {
                    single<DynamicRepository> {
                        object : DynamicRepository {
                            private var resendCount = 0

                            override suspend fun fetch(
                                request: com.carbroz.cbpartner.domain.model.dynamic.DynamicRequest,
                            ): Result<DynamicResponse> = when {
                                request.endpoint == LOCAL_PREVIEW_DYNAMIC_ENDPOINT ->
                                    Result.success(preview.response)

                                request.endpoint == LOCAL_PREVIEW_OTP_ENDPOINT &&
                                    preview.nextScreen != null ->
                                    Result.success(preview.nextScreen)

                                preview.networkFailure != null ->
                                    Result.failure(AppFailure(preview.networkFailure))

                                // Development-only Send OTP: initial Login navigates to the
                                // actual mapped OTP fixture, while OTP Resend stays on OTP.
                                request.endpoint == "/api/v1/partner/auth/send_otp" &&
                                    request.method.equals("POST", ignoreCase = true) &&
                                    (preview.response.data?.screenId in setOf("partner_login", "partner_otp")) -> {
                                    val phone = (request.body as? DynamicValue.ObjectValue)
                                        ?.value?.get("phoneNumber") as? DynamicValue.StringValue
                                    // Distinguish first send vs resend by the existing
                                    // request contract, not by the initial fixture's screen.
                                    val loginSend = request.responseMode == "destination"
                                    val otpScreen = preview.nextScreen?.data
                                    if (phone == null) {
                                        Result.failure(IllegalArgumentException("Missing preview phoneNumber"))
                                    } else if (loginSend && otpScreen == null) {
                                        Result.failure(IllegalStateException("OTP preview screen is unavailable"))
                                    } else if (!loginSend && preview.resendFailureMode) {
                                        Result.failure(AppFailure(AppFailure.Category.CONNECTION))
                                    } else {
                                        resendCount++
                                        Result.success(DynamicResponse(
                                            status = 200,
                                            code = "SUCCESS",
                                            message = "Development preview OTP sent",
                                            data = null,
                                            traceId = null,
                                            dataPayload = DynamicValue.ObjectValue(mapOf(
                                                "challengeId" to DynamicValue.StringValue(
                                                    "development-send-" + resendCount,
                                                ),
                                                "phoneNumber" to phone,
                                                "resendAfterSeconds" to DynamicValue.NumberValue(
                                                    LOCAL_PREVIEW_RESEND_SECONDS,
                                                ),
                                            )),
                                            destination = if (loginSend) DynamicDestination(
                                                screenId = requireNotNull(otpScreen).screenId,
                                                templateId = otpScreen.template.id,
                                                templateType = otpScreen.template.type,
                                                endpoint = LOCAL_PREVIEW_OTP_ENDPOINT,
                                            ) else null,
                                        ))
                                    }
                                }

                                else -> Result.failure(
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

    RetainedEffect(koin) {
        onRetire { koin.close() }
    }

    ApplicationContent(koin.koin, runtimeMetadataProvider, retainedSession, apiBaseUrl.toString())
}

/** Preview-only destination; rejected by the offline repository for all mutations. */
private const val LOCAL_PREVIEW_DYNAMIC_ENDPOINT = "/api/v1/partner/screen/auth_login"
private const val LOCAL_PREVIEW_OTP_ENDPOINT = "/api/v1/partner/screen/auth_otp"
