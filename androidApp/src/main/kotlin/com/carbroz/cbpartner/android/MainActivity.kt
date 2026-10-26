package com.carbroz.cbpartner.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.carbroz.cbpartner.app.App
import com.carbroz.cbpartner.app.configureLogging
import com.carbroz.cbpartner.app.parseLocalDynamicPreview
import com.carbroz.cbpartner.app.parseLocalBootstrapPreview
import com.carbroz.cbpartner.app.simulatedLocalBootstrapFailure
import com.carbroz.cbpartner.app.LOCAL_PREVIEW_RESEND_SECONDS
import com.carbroz.cbpartner.app.RetainCompatibilityProbe
import com.carbroz.cbpartner.network.ApiBaseUrl
import com.carbroz.cbpartner.network.AppEnvironment

/** Normal entry point; the local Dynamic preview requires an explicit debug-only intent. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val runtimeMetadataProvider = AndroidRuntimeMetadataProvider(applicationContext)
        val environment = AppEnvironment.parse(BuildConfig.CBPARTNER_ENVIRONMENT)
        configureLogging(environment)
        val apiBaseUrl = ApiBaseUrl(BuildConfig.CBPARTNER_API_BASE_URL, environment)

        // Opt-in development-only fixture. Production always uses the real backend.
        val loginRequested = BuildConfig.DEBUG &&
            environment == AppEnvironment.Development &&
            intent.getBooleanExtra("cbpartner.login_preview", false)
        val otpRequested = BuildConfig.DEBUG &&
            environment == AppEnvironment.Development &&
            intent.getBooleanExtra("cbpartner.otp_preview", false)
        val bootstrapScenario = if (BuildConfig.DEBUG && environment == AppEnvironment.Development) {
            intent.getStringExtra("cbpartner.bootstrap_preview")?.takeIf {
                it in setOf("normal", "maintenance", "required_update", "optional_update")
            }
        } else null
        val bootstrapError = if (BuildConfig.DEBUG && environment == AppEnvironment.Development) {
            intent.getStringExtra("cbpartner.bootstrap_error")?.takeIf {
                it.uppercase() in setOf("CONNECTION", "SERVER", "SECURE_CONNECTION", "UNKNOWN")
            }
        } else null
        val bootstrapPreview = when {
            bootstrapError != null -> simulatedLocalBootstrapFailure(bootstrapError)
            bootstrapScenario != null -> {
                val fixture = assets.open("bootstrap-$bootstrapScenario.json")
                    .bufferedReader().use { it.readText() }
                parseLocalBootstrapPreview(fixture)
            }
            else -> null
        }
        val preview = if (otpRequested || loginRequested || bootstrapPreview != null) {
            val filename = if (otpRequested) "partner-otp.json" else "partner-login.json"
            val raw = assets.open(filename).bufferedReader().use { it.readText() }
            val initialState = if (otpRequested) mapOf(
                "phoneNumber" to "9876543210",
                "challengeId" to "development-preview",
            ) else emptyMap()
            val numericInitialState = if (otpRequested) mapOf(
                "resendAfterSeconds" to LOCAL_PREVIEW_RESEND_SECONDS,
            ) else emptyMap()
            val mapped = parseLocalDynamicPreview(
                raw,
                initialState,
                numericInitialState,
                resendFailureMode = otpRequested &&
                    intent.getBooleanExtra("cbpartner.otp_resend_failure", false),
                networkFailure = intent.getStringExtra("cbpartner.preview_network_failure"),
            )
            if (filename == "partner-login.json") {
                val otpRaw = assets.open("partner-otp.json")
                    .bufferedReader().use { it.readText() }
                mapped.copy(nextScreen = parseLocalDynamicPreview(otpRaw).response)
            } else mapped
        } else null

        setContent {
            if (BuildConfig.DEBUG && intent.getBooleanExtra("cbpartner.retention_probe", false)) {
                RetainCompatibilityProbe()
            } else {
                App(
                    runtimeMetadataProvider = runtimeMetadataProvider,
                    apiBaseUrl = apiBaseUrl,
                    environment = environment,
                    localDynamicPreview = preview,
                    localBootstrapPreview = bootstrapPreview,
                )
            }
        }
    }
}
