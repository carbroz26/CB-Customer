package com.carbroz.cbpartner.android

import android.content.Context
import com.carbroz.cbpartner.core.runtime.RuntimeMetadata
import com.carbroz.cbpartner.core.runtime.RuntimeMetadataDefaults
import com.carbroz.cbpartner.core.runtime.RuntimeMetadataProvider
import kotlin.uuid.Uuid

class AndroidRuntimeMetadataProvider(
    context: Context,
) : RuntimeMetadataProvider {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val deviceId = preferences.getString(DEVICE_ID_KEY, null)
        ?: Uuid.random().toString().also { generated ->
            preferences.edit().putString(DEVICE_ID_KEY, generated).apply()
        }

    override fun current(): RuntimeMetadata = RuntimeMetadata(
        platform = "ANDROID",
        appVersion = RuntimeMetadataDefaults.APP_VERSION,
        buildNumber = RuntimeMetadataDefaults.BUILD_NUMBER,
        deviceId = deviceId,
    )

    private companion object {
        const val PREFERENCES_NAME = "cbpartner_runtime_metadata"
        const val DEVICE_ID_KEY = "device_id"
    }
}
