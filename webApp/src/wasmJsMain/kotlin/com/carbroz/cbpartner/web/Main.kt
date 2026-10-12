package com.carbroz.cbpartner.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.carbroz.cbpartner.app.App
import com.carbroz.cbpartner.network.ApiBaseUrl
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val apiBaseUrl = ApiBaseUrl(
        "${window.location.protocol}//${window.location.hostname}:3000",
    )
    ComposeViewport(viewportContainerId = "webApp") {
        App(WebRuntimeMetadataProvider(), apiBaseUrl)
    }
}
