package com.carbroz.cbpartner.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.carbroz.cbpartner.app.App
import com.carbroz.cbpartner.app.LOCAL_PREVIEW_RESEND_SECONDS
import com.carbroz.cbpartner.app.configureLogging
import com.carbroz.cbpartner.app.RetainCompatibilityProbe
import com.carbroz.cbpartner.network.ApiBaseUrl
import com.carbroz.cbpartner.network.AppEnvironment
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val environment = AppEnvironment.parse(
        window.document.querySelector("meta[name='cbpartner-environment']")
            ?.getAttribute("content")
            ?.takeIf { it.isNotBlank() }
            ?: error("Deployment environment meta tag is missing"),
    )
    val configuredApiBaseUrl = window.document
        .querySelector("meta[name='cbpartner-api-base-url']")
        ?.getAttribute("content")
        ?.takeIf { it.isNotBlank() }
    val url = configuredApiBaseUrl ?: when (environment) {
        AppEnvironment.Development -> "${window.location.protocol}//${window.location.hostname}:3000"
        else -> error("Staging and production require an explicit API origin")
    }
    val apiBaseUrl = ApiBaseUrl(url, environment)
    configureLogging(environment)
    val previewNetwork = window.location.search.substringAfter("cbpartnerPreviewNetwork=", "")
        .substringBefore("&").takeIf { it.isNotBlank() }
    val bootstrapScenario = window.location.search.substringAfter("cbpartnerSplashDialog=", "")
        .substringBefore("&").takeIf {
            it in setOf("normal", "maintenance", "required_update", "optional_update")
        }
    val bootstrapError = window.location.search.substringAfter("cbpartnerBootstrapError=", "")
        .substringBefore("&").takeIf {
            it.uppercase() in setOf("CONNECTION", "SERVER", "SECURE_CONNECTION", "UNKNOWN")
        }
    ComposeViewport(viewportContainerId = "webApp") {
        if (environment == AppEnvironment.Development &&
            (bootstrapScenario != null || bootstrapError != null)
        ) {
            BootstrapOfflinePreview(
                WebRuntimeMetadataProvider(), apiBaseUrl, bootstrapScenario, bootstrapError,
            )
        } else if (environment == AppEnvironment.Development &&
            window.location.search.contains("cbpartnerVisualPreview=1")
        ) {
            VisualLoginPreview()
        } else if (environment == AppEnvironment.Development &&
            window.location.search.contains("cbpartnerOtpPreview=1")
        ) {
            DynamicOfflinePreview(
                WebRuntimeMetadataProvider(),
                apiBaseUrl,
                fixture = "partner-otp.json",
                initialState = mapOf(
                    "phoneNumber" to "9876543210",
                    "challengeId" to "development-preview",
                ),
                numericInitialState = mapOf(
                    "resendAfterSeconds" to LOCAL_PREVIEW_RESEND_SECONDS,
                ),
                resendFailureMode = window.location.search.contains("cbpartnerOtpResendFailure=1"),
                networkFailure = previewNetwork,
            )
        } else if (environment == AppEnvironment.Development &&
            window.location.search.contains("cbpartnerLoginPreview=1")
        ) {
            DynamicOfflinePreview(
                WebRuntimeMetadataProvider(), apiBaseUrl, fixture = "partner-login.json",
                networkFailure = previewNetwork,
            )
        } else if (window.location.search.contains("cbpartnerRetentionProbe=1")) {
            RetainCompatibilityProbe()
        } else {
            App(WebRuntimeMetadataProvider(), apiBaseUrl, environment)
        }
    }
}
