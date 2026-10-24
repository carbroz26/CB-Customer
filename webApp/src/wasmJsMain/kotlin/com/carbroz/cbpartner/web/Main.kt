package com.carbroz.cbpartner.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.carbroz.cbpartner.app.App
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
    ComposeViewport(viewportContainerId = "webApp") {
        if (environment == AppEnvironment.Development &&
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
                    "phoneNumber" to com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue.StringValue("9876543210"),
                    "challengeId" to com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue.StringValue("development-preview"),
                ),
            )
        } else if (environment == AppEnvironment.Development &&
            window.location.search.contains("cbpartnerLoginPreview=1")
        ) {
            DynamicOfflinePreview(WebRuntimeMetadataProvider(), apiBaseUrl, fixture = "partner-login.json")
        } else if (window.location.search.contains("cbpartnerRetentionProbe=1")) {
            RetainCompatibilityProbe()
        } else {
            App(WebRuntimeMetadataProvider(), apiBaseUrl, environment)
        }
    }
}
