package com.carbroz.cbpartner.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.carbroz.cbpartner.app.App
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
    ComposeViewport(viewportContainerId = "webApp") {
        if (window.location.search.contains("cbpartnerRetentionProbe=1")) {
            RetainCompatibilityProbe()
        } else {
            App(WebRuntimeMetadataProvider(), apiBaseUrl, environment)
        }
    }
}
