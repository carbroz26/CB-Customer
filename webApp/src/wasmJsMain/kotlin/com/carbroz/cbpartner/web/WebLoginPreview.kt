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
import com.carbroz.cbpartner.app.WebLoginPreview
import com.carbroz.cbpartner.core.runtime.RuntimeMetadataProvider
import com.carbroz.cbpartner.data.dynamic.mapper.DynamicResponseMapper
import com.carbroz.cbpartner.data.dynamic.model.response.DynamicResponseDto
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.network.ApiBaseUrl
import com.carbroz.cbpartner.network.AppEnvironment
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json

/**
 * Explicit Web development route for visually inspecting the frozen Login JSON
 * without starting the Bootstrap request or a backend process.
 * Uses the real DTO mapper and app entrypoint; no duplicate Login renderer.
 */
@Composable
internal fun LoginOfflinePreview(
    metadata: RuntimeMetadataProvider,
    apiBaseUrl: ApiBaseUrl,
) {
    var loaded by remember { mutableStateOf<Result<WebLoginPreview>?>(null) }
    LaunchedEffect(Unit) {
        loaded = runCatching {
            // Same-origin static build resource: no external API, CORS preflight or mock server.
            val response = window.fetch("/preview/partner-login.json").await()
            require(response.ok) { "Web Login preview JSON is not served by the Web app" }
            // Kotlin/Wasm browser Response.text() yields JsString; decode from Kotlin String.
            val raw = response.text().await().toString()
            val json = Json { ignoreUnknownKeys = true; isLenient = true }
            val mapped = DynamicResponseMapper(json).map(json.decodeFromString<DynamicResponseDto>(raw))
            check(mapped.data != null) { "Web preview JSON did not map to a Dynamic screen" }
            WebLoginPreview(
                response = mapped,
                countries = previewCountries(),
            )
        }
    }
    val preview = loaded?.getOrNull()
    when {
        loaded == null -> CircularProgressIndicator()
        preview != null -> App(
            runtimeMetadataProvider = metadata,
            apiBaseUrl = apiBaseUrl,
            environment = AppEnvironment.Development,
            webLoginPreview = preview,
        )
        else -> {
            // Deliberately avoid echoing raw JSON or exception contents to the UI.
            Text("Offline Login preview could not load. Check the browser Console.")
        }
    }
}

/** UI-only example rows, not a production country/OTP support matrix. */
private fun previewCountries(): List<Map<String, DynamicValue>> {
    fun country(id: String, name: String, dialCode: String, flagAsset: String) = mapOf(
        "id" to DynamicValue.StringValue(id),
        "name" to DynamicValue.StringValue(name),
        "dialCode" to DynamicValue.StringValue(dialCode),
        "flagAsset" to DynamicValue.StringValue(flagAsset),
    )
    return listOf(
        country("IN", "India", "+91", "flags/in"),
        country("US", "United States", "+1", "flags/us"),
        country("GB", "United Kingdom", "+44", "flags/gb"),
        country("AE", "United Arab Emirates", "+971", "flags/ae"),
        country("SA", "Saudi Arabia", "+966", "flags/sa"),
        country("QA", "Qatar", "+974", "flags/qa"),
        country("KW", "Kuwait", "+965", "flags/kw"),
        country("OM", "Oman", "+968", "flags/om"),
    )
}
