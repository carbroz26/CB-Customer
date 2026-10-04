package com.carbroz.cbpartner.data.bootstrap.mapper

import com.carbroz.cbpartner.data.bootstrap.model.BootstrapResponseDto
import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapFailure
import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapOutput
import com.carbroz.cbpartner.domain.model.bootstrap.FeatureConfig
import com.carbroz.cbpartner.domain.model.bootstrap.MaintenanceConfig
import com.carbroz.cbpartner.domain.model.bootstrap.UpdateConfig

class BootstrapMapper {
    fun map(response: BootstrapResponseDto): Result<BootstrapOutput> {
        if (response.status !in 200..299) {
            return Result.failure(BootstrapFailure.Http(response.status))
        }
        if (response.code != "SUCCESS") {
            return Result.failure(BootstrapFailure.Api(response.code, response.message))
        }

        val data = response.data
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing bootstrap data"))
        val config = data.config
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing bootstrap config"))
        val startup = data.startup
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing bootstrap startup"))
        val maintenance = config.maintenance
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing maintenance config"))
        val update = config.update
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing update config"))
        val features = config.features
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing feature config"))
        val destination = startup.destination
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing startup destination"))

        val version = config.version
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing config version"))
        val maintenanceEnabled = maintenance.enabled
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing maintenance enabled flag"))
        val updateRequired = update.required
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing update required flag"))
        val updateOptional = update.optional
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing update optional flag"))
        val minimumVersion = update.minimumVersion
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing minimum version"))
        val latestVersion = update.latestVersion
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing latest version"))
        val registrationEnabled = features.registrationEnabled
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing registration flag"))
        val individualPartnerEnabled = features.individualPartnerEnabled
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing individual partner flag"))
        val organizationPartnerEnabled = features.organizationPartnerEnabled
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing organization partner flag"))
        val authenticated = startup.authenticated
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing authenticated flag"))
        val screenId = destination.screenId
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing destination screen id"))
        val templateId = destination.templateId
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing destination template id"))
        val templateType = destination.templateType
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing destination template type"))
        val endpoint = destination.endpoint
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing destination endpoint"))

        return Result.success(
            BootstrapOutput(
                version = version,
                maintenance = MaintenanceConfig(
                    enabled = maintenanceEnabled,
                    title = maintenance.title,
                    message = maintenance.message,
                ),
                update = UpdateConfig(
                    required = updateRequired,
                    optional = updateOptional,
                    minimumVersion = minimumVersion,
                    latestVersion = latestVersion,
                    storeUrl = update.storeUrl,
                ),
                features = FeatureConfig(
                    registrationEnabled = registrationEnabled,
                    individualPartnerEnabled = individualPartnerEnabled,
                    organizationPartnerEnabled = organizationPartnerEnabled,
                ),
                authenticated = authenticated,
                destination = DynamicDestination(
                    screenId = screenId,
                    templateId = templateId,
                    templateType = templateType,
                    endpoint = endpoint,
                ),
            ),
        )
    }
}
