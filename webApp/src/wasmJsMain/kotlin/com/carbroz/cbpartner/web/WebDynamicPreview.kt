package com.carbroz.cbpartner.web

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.carbroz.cbpartner.app.App
import com.carbroz.cbpartner.app.LocalDynamicPreview
import com.carbroz.cbpartner.app.parseLocalDynamicPreview
import com.carbroz.cbpartner.core.runtime.RuntimeMetadataProvider
import com.carbroz.cbpartner.network.ApiBaseUrl
import com.carbroz.cbpartner.network.AppEnvironment
import kotlinx.browser.window
import kotlinx.coroutines.await

/**
 * Web-only entry to a shared local Dynamic fixture; uses the production
 * mapper, Dynamic Store, renderer registry and application composition.
 */
@Composable
internal fun DynamicOfflinePreview(
    metadata: RuntimeMetadataProvider,
    apiBaseUrl: ApiBaseUrl,
    fixture: String,
    initialState: Map<String, com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue> = emptyMap(),
) {
    var loaded by remember(fixture) { mutableStateOf<Result<LocalDynamicPreview>?>(null) }
    LaunchedEffect(fixture) {
        loaded = runCatching {
            val response = window.fetch("/preview/$fixture").await()
            require(response.ok) { "Web Dynamic preview JSON is not served by the Web app" }
            parseLocalDynamicPreview(response.text().await().toString(), initialState)
        }
    }
    val preview = loaded?.getOrNull()
    when {
        loaded == null -> CircularProgressIndicator()
        preview != null -> App(
            runtimeMetadataProvider = metadata,
            apiBaseUrl = apiBaseUrl,
            environment = AppEnvironment.Development,
            localDynamicPreview = preview,
        )
        else -> Text("Offline Dynamic preview could not load. Check the browser Console.")
    }
}
