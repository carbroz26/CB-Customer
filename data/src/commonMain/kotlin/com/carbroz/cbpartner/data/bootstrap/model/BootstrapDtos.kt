package com.carbroz.cbpartner.data.bootstrap.model

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
    val startup: StartupDto?,
)

@Serializable
data class BootstrapConfigDto(
    val version: String?,
    val maintenance: MaintenanceDto?,
    val update: UpdateDto?,
    val features: FeatureDto?,
)

@Serializable
data class MaintenanceDto(
    val enabled: Boolean?,
    val title: String?,
    val message: String?,
)

@Serializable
data class UpdateDto(
    val required: Boolean?,
    val optional: Boolean?,
    val minimumVersion: String?,
    val latestVersion: String?,
    val storeUrl: String?,
)

@Serializable
data class FeatureDto(
    val registrationEnabled: Boolean?,
    val individualPartnerEnabled: Boolean?,
    val organizationPartnerEnabled: Boolean?,
)

@Serializable
data class StartupDto(
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
