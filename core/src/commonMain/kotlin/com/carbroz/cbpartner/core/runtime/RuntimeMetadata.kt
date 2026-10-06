package com.carbroz.cbpartner.core.runtime

data class RuntimeMetadata(
    val platform: String,
    val appVersion: String,
    val buildNumber: String,
    val deviceId: String,
) {
    fun asContext(): Map<String, String> = mapOf(
        "platform" to platform,
        "appVersion" to appVersion,
        "buildNumber" to buildNumber,
        "deviceId" to deviceId,
    )
}

fun interface RuntimeMetadataProvider {
    fun current(): RuntimeMetadata
}

object RuntimeMetadataDefaults {
    const val APP_VERSION = "1.0.0"
    const val BUILD_NUMBER = "1"
}
