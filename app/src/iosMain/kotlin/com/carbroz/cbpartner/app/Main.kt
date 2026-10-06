package com.carbroz.cbpartner.app

import androidx.compose.ui.window.ComposeUIViewController
import com.carbroz.cbpartner.core.runtime.RuntimeMetadataDefaults
import com.carbroz.cbpartner.core.runtime.RuntimeMetadata
import com.carbroz.cbpartner.core.runtime.RuntimeMetadataProvider
import platform.UIKit.UIDevice

private class IosRuntimeMetadataProvider : RuntimeMetadataProvider {
    override fun current(): RuntimeMetadata = RuntimeMetadata(
        platform = "IOS",
        appVersion = RuntimeMetadataDefaults.APP_VERSION,
        buildNumber = RuntimeMetadataDefaults.BUILD_NUMBER,
        deviceId = UIDevice.currentDevice.identifierForVendor?.UUIDString
            ?: "ios-unavailable",
    )
}

fun MainViewController() = ComposeUIViewController {
    App(IosRuntimeMetadataProvider())
}
