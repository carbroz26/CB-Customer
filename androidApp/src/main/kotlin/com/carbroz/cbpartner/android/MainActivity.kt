package com.carbroz.cbpartner.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.carbroz.cbpartner.app.App
import com.carbroz.cbpartner.app.configureLogging
import com.carbroz.cbpartner.app.RetainCompatibilityProbe
import com.carbroz.cbpartner.network.ApiBaseUrl
import com.carbroz.cbpartner.network.AppEnvironment

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val runtimeMetadataProvider = AndroidRuntimeMetadataProvider(applicationContext)
        val environment = AppEnvironment.parse(BuildConfig.CBPARTNER_ENVIRONMENT)
        configureLogging(environment)
        val apiBaseUrl = ApiBaseUrl(BuildConfig.CBPARTNER_API_BASE_URL, environment)
        setContent {
            if (BuildConfig.DEBUG && intent.getBooleanExtra("cbpartner.retention_probe", false)) {
                RetainCompatibilityProbe()
            } else {
                App(
                    runtimeMetadataProvider,
                    apiBaseUrl,
                    environment,
                    localDebugUiAllowed = BuildConfig.DEBUG && environment == AppEnvironment.Development,
                )
            }
        }
    }
}
