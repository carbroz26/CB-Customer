package com.carbroz.cbpartner.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.carbroz.cbpartner.app.App
import com.carbroz.cbpartner.app.configureLogging
import com.carbroz.cbpartner.app.parseLocalDynamicPreview
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
        val preview = if (otpRequested || loginRequested) {
            val filename = if (otpRequested) "partner-otp.json" else "partner-login.json"
            val raw = assets.open(filename).bufferedReader().use { it.readText() }
            val initialState = if (otpRequested) mapOf(
                "phoneNumber" to "9876543210",
                "challengeId" to "development-preview",
            ) else emptyMap()
            val numericInitialState = if (otpRequested) mapOf(
                "resendAfterSeconds" to LOCAL_PREVIEW_RESEND_SECONDS,
            ) else emptyMap()
            parseLocalDynamicPreview(
                raw,
                initialState,
                numericInitialState,
                resendFailureMode = otpRequested &&
                    intent.getBooleanExtra("cbpartner.otp_resend_failure", false),
            )
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
                )
            }
        }
    }
}
