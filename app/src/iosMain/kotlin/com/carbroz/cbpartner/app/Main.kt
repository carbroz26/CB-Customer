package com.carbroz.cbpartner.app

import androidx.compose.ui.window.ComposeUIViewController
import com.carbroz.cbpartner.core.runtime.RuntimeMetadataDefaults
import com.carbroz.cbpartner.core.runtime.RuntimeMetadata
import com.carbroz.cbpartner.core.runtime.RuntimeMetadataProvider
import com.carbroz.cbpartner.network.ApiBaseUrl
import com.carbroz.cbpartner.network.AppEnvironment
import platform.Foundation.NSBundle
import platform.UIKit.UIDevice
import platform.UIKit.UIViewController

private class IosRuntimeMetadataProvider : RuntimeMetadataProvider {
    override fun current(): RuntimeMetadata = RuntimeMetadata(
        platform = "IOS",
        appVersion = RuntimeMetadataDefaults.APP_VERSION,
        buildNumber = RuntimeMetadataDefaults.BUILD_NUMBER,
        deviceId = UIDevice.currentDevice.identifierForVendor?.UUIDString
            ?: "ios-unavailable",
    )
}

private fun iosApiBaseUrl(environment: AppEnvironment): ApiBaseUrl {
    val configuredUrl = NSBundle.mainBundle.objectForInfoDictionaryKey("CBPARTNER_API_BASE_URL") as? String
    val url = configuredUrl?.takeIf(String::isNotBlank)
        ?: if (environment == AppEnvironment.Development) "http://localhost:3000"
           else error("Staging and production require an explicit API origin")
    return ApiBaseUrl(url, environment)
}

fun MainViewController(): UIViewController {
    val environment = AppEnvironment.parse(
        (NSBundle.mainBundle.objectForInfoDictionaryKey("CBPARTNER_ENVIRONMENT") as? String)
            ?: error("Application environment is missing"),
    )
    val runtimeMetadataProvider = IosRuntimeMetadataProvider()
    val apiBaseUrl = iosApiBaseUrl(environment)
    return ComposeUIViewController {
        App(runtimeMetadataProvider, apiBaseUrl, environment)
    }
}
