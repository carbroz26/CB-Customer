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
import com.carbroz.cbpartner.app.LocalLoginPreview
import com.carbroz.cbpartner.app.parseLocalLoginPreview
import com.carbroz.cbpartner.core.runtime.RuntimeMetadataProvider
import com.carbroz.cbpartner.network.ApiBaseUrl
import com.carbroz.cbpartner.network.AppEnvironment
import kotlinx.browser.window
import kotlinx.coroutines.await

/**
 * Web-only entry to the shared local Login fixture; uses the production
 * mapper, Dynamic Store, renderer registry and application composition.
 */
@Composable
internal fun LoginOfflinePreview(
    metadata: RuntimeMetadataProvider,
    apiBaseUrl: ApiBaseUrl,
) {
    var loaded by remember { mutableStateOf<Result<LocalLoginPreview>?>(null) }
    LaunchedEffect(Unit) {
        loaded = runCatching {
            val response = window.fetch("/preview/partner-login.json").await()
            require(response.ok) { "Web Login preview JSON is not served by the Web app" }
            parseLocalLoginPreview(response.text().await().toString())
        }
    }
    val preview = loaded?.getOrNull()
    when {
        loaded == null -> CircularProgressIndicator()
        preview != null -> App(
            runtimeMetadataProvider = metadata,
            apiBaseUrl = apiBaseUrl,
            environment = AppEnvironment.Development,
            localLoginPreview = preview,
        )
        else -> Text("Offline Login preview could not load. Check the browser Console.")
    }
}
