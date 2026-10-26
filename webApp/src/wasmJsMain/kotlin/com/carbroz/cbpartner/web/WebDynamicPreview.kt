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
import com.carbroz.cbpartner.app.LocalBootstrapPreview
import com.carbroz.cbpartner.app.parseLocalBootstrapPreview
import com.carbroz.cbpartner.app.simulatedLocalBootstrapFailure
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
    initialState: Map<String, String> = emptyMap(),
    numericInitialState: Map<String, Double> = emptyMap(),
    resendFailureMode: Boolean = false,
    networkFailure: String? = null,
) {
    var loaded by remember(fixture) { mutableStateOf<Result<LocalDynamicPreview>?>(null) }
    LaunchedEffect(fixture) {
        loaded = runCatching {
            val response = window.fetch("/preview/$fixture").await()
            require(response.ok) { "Web Dynamic preview JSON is not served by the Web app" }
            val current = parseLocalDynamicPreview(
                response.text().await().toString(), initialState, numericInitialState, resendFailureMode,
                networkFailure = networkFailure,
            )
            if (fixture == "partner-login.json") {
                val otpResponse = window.fetch("/preview/partner-otp.json").await()
                require(otpResponse.ok) { "Web OTP preview JSON is not served by the Web app" }
                current.withNextScreenFixture(
                    otpResponse.text().await().toString(),
                )
            } else current
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

/** Exercises real SplashStore, Bootstrap mapper and common Dialog host without contacting Backend. */
@Composable
internal fun BootstrapOfflinePreview(
    metadata: RuntimeMetadataProvider,
    apiBaseUrl: ApiBaseUrl,
    scenario: String?,
    failure: String?,
) {
    var loaded by remember(scenario, failure) {
        mutableStateOf<Result<Pair<LocalBootstrapPreview, LocalDynamicPreview>>?>(null)
    }
    LaunchedEffect(scenario, failure) {
        loaded = runCatching {
            val loginResponse = window.fetch("/preview/partner-login.json").await()
            require(loginResponse.ok) { "Local Login JSON is unavailable" }
            val login = parseLocalDynamicPreview(loginResponse.text().await().toString())
            val otpResponse = window.fetch("/preview/partner-otp.json").await()
            require(otpResponse.ok) { "Web OTP preview JSON is unavailable" }
            val loginWithOtp = login.withNextScreenFixture(
                otpResponse.text().await().toString(),
            )
            val bootstrap = if (failure != null) {
                simulatedLocalBootstrapFailure(failure)
            } else {
                require(scenario != null) { "Bootstrap scenario required" }
                val result = window.fetch("/preview/bootstrap-$scenario.json").await()
                require(result.ok) { "Bootstrap scenario JSON is unavailable" }
                parseLocalBootstrapPreview(result.text().await().toString())
            }
            bootstrap to loginWithOtp
        }
    }
    when (val result = loaded) {
        null -> CircularProgressIndicator()
        else -> {
            val pair = result.getOrNull()
            if (pair == null) Text("Bootstrap preview could not load. Check browser Console.")
            else App(
                runtimeMetadataProvider = metadata,
                apiBaseUrl = apiBaseUrl,
                environment = AppEnvironment.Development,
                localDynamicPreview = pair.second,
                localBootstrapPreview = pair.first,
            )
        }
    }
}
