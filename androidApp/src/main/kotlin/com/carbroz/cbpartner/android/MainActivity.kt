package com.carbroz.cbpartner.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.carbroz.cbpartner.app.App
import com.carbroz.cbpartner.network.ApiBaseUrl

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val runtimeMetadataProvider = AndroidRuntimeMetadataProvider(applicationContext)
        val apiBaseUrl = ApiBaseUrl(BuildConfig.CBPARTNER_API_BASE_URL)
        setContent {
            App(runtimeMetadataProvider, apiBaseUrl)
        }
    }
}
