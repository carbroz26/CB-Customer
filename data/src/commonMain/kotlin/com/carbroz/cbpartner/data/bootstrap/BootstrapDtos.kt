package com.carbroz.cbpartner.data.bootstrap

import kotlinx.serialization.Serializable

@Serializable
data class BootstrapResponseDto(
    val status: Int,
    val code: String,
    val message: String,
    val data: BootstrapDataDto?,
    val traceId: String?,
)

@Serializable
data class BootstrapDataDto(
    val config: BootstrapConfigDto?,
    val startup: StartupConfigDto?,
)

@Serializable
data class BootstrapConfigDto(
    val version: String?,
    val maintenance: MaintenanceConfigDto?,
    val update: UpdateConfigDto?,
    val features: FeatureConfigDto?,
)

@Serializable
data class MaintenanceConfigDto(
    val enabled: Boolean?,
    val title: String?,
    val message: String?,
)

@Serializable
data class UpdateConfigDto(
    val required: Boolean?,
    val optional: Boolean?,
    val minimumVersion: String?,
    val latestVersion: String?,
    val storeUrl: String?,
)

@Serializable
data class FeatureConfigDto(
    val registrationEnabled: Boolean?,
    val individualPartnerEnabled: Boolean?,
    val organizationPartnerEnabled: Boolean?,
)

@Serializable
data class StartupConfigDto(
    val authenticated: Boolean?,
    val destination: DynamicDestinationDto?,
)

@Serializable
data class DynamicDestinationDto(
    val screenId: String?,
    val templateId: String?,
    val templateType: String?,
    val endpoint: String?,
)
