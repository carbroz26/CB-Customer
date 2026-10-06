package com.carbroz.cbpartner.web

import com.carbroz.cbpartner.core.runtime.RuntimeMetadata
import com.carbroz.cbpartner.core.runtime.RuntimeMetadataDefaults
import com.carbroz.cbpartner.core.runtime.RuntimeMetadataProvider
import kotlinx.browser.window
import kotlin.uuid.Uuid

class WebRuntimeMetadataProvider : RuntimeMetadataProvider {
    private val deviceId = window.localStorage.getItem(DEVICE_ID_KEY)
        ?: Uuid.random().toString().also { generated ->
            window.localStorage.setItem(DEVICE_ID_KEY, generated)
        }

    override fun current(): RuntimeMetadata = RuntimeMetadata(
        platform = "WEB",
        appVersion = RuntimeMetadataDefaults.APP_VERSION,
        buildNumber = RuntimeMetadataDefaults.BUILD_NUMBER,
        deviceId = deviceId,
    )

    private companion object {
        const val DEVICE_ID_KEY = "cbpartner.deviceId"
    }
}
