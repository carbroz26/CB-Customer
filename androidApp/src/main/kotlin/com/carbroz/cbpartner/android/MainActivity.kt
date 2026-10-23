package com.carbroz.cbpartner.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.Modifier
import com.carbroz.cbpartner.app.App
import com.carbroz.cbpartner.app.configureLogging
import com.carbroz.cbpartner.app.parseLocalLoginPreview
import com.carbroz.cbpartner.app.RetainCompatibilityProbe
import com.carbroz.cbpartner.network.ApiBaseUrl
import com.carbroz.cbpartner.network.AppEnvironment

/** Normal entry point; the local Login preview requires an explicit debug-only intent. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val runtimeMetadataProvider = AndroidRuntimeMetadataProvider(applicationContext)
        val environment = AppEnvironment.parse(BuildConfig.CBPARTNER_ENVIRONMENT)
        configureLogging(environment)
        val apiBaseUrl = ApiBaseUrl(BuildConfig.CBPARTNER_API_BASE_URL, environment)

        // Never load a preview fixture in release or staging/production builds.
        val previewRequested = BuildConfig.DEBUG &&
            environment == AppEnvironment.Development &&
            intent.getBooleanExtra("cbpartner.login_preview", false)
        val preview = if (previewRequested) {
            val raw = assets.open("partner-login.json").bufferedReader().use { it.readText() }
            parseLocalLoginPreview(raw)
        } else null

        setContent {
            // The Android host owns the system navigation-bar safe area. Keep the
            // status-bar/top layout and backend-authored Dynamic spacing unchanged.
            Box(Modifier.fillMaxSize().navigationBarsPadding()) {
                if (BuildConfig.DEBUG && intent.getBooleanExtra("cbpartner.retention_probe", false)) {
                    RetainCompatibilityProbe()
                } else {
                    App(
                        runtimeMetadataProvider = runtimeMetadataProvider,
                        apiBaseUrl = apiBaseUrl,
                        environment = environment,
                        localLoginPreview = preview,
                    )
                }
            }
        }
    }
}
