package com.carbroz.cbpartner.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.carbroz.cbpartner.app.App
import com.carbroz.cbpartner.network.ApiBaseUrl
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val configuredApiBaseUrl = window.document
        .querySelector("meta[name='cbpartner-api-base-url']")
        ?.getAttribute("content")
        ?.takeIf { it.isNotBlank() }
    val apiBaseUrl = ApiBaseUrl(
        configuredApiBaseUrl
            ?: "${window.location.protocol}//${window.location.hostname}:3000",
    )
    ComposeViewport(viewportContainerId = "webApp") {
        App(WebRuntimeMetadataProvider(), apiBaseUrl)
    }
}
