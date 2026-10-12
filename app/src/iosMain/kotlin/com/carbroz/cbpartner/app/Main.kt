package com.carbroz.cbpartner.app

import androidx.compose.ui.window.ComposeUIViewController
import com.carbroz.cbpartner.core.runtime.RuntimeMetadataDefaults
import com.carbroz.cbpartner.core.runtime.RuntimeMetadata
import com.carbroz.cbpartner.core.runtime.RuntimeMetadataProvider
import com.carbroz.cbpartner.network.ApiBaseUrl
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

private fun iosApiBaseUrl(): ApiBaseUrl {
    val configuredUrl = NSBundle.mainBundle.objectForInfoDictionaryKey("CBPARTNER_API_BASE_URL") as? String
    return ApiBaseUrl(configuredUrl ?: "http://localhost:3000")
}

fun MainViewController(): UIViewController {
    val runtimeMetadataProvider = IosRuntimeMetadataProvider()
    val apiBaseUrl = iosApiBaseUrl()
    return ComposeUIViewController {
        App(runtimeMetadataProvider, apiBaseUrl)
    }
}
