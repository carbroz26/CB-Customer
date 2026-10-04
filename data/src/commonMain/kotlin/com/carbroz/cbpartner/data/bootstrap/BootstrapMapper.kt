package com.carbroz.cbpartner.data.bootstrap

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapConfig
import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapFailure
import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapOutput
import com.carbroz.cbpartner.domain.model.bootstrap.FeatureConfig
import com.carbroz.cbpartner.domain.model.bootstrap.MaintenanceConfig
import com.carbroz.cbpartner.domain.model.bootstrap.StartupConfig
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
        val nextScreen = startup.nextScreen
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing next screen config"))

        val configVersion = config.version
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
        val screenId = nextScreen.screenId
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing next screen id"))
        val templateId = nextScreen.templateId
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing template id"))
        val templateType = nextScreen.templateType
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing template type"))
        val endpoint = nextScreen.endpoint
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing next screen endpoint"))
        val method = nextScreen.method
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing next screen method"))
        val authentication = nextScreen.authentication
            ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing next screen authentication"))

        return Result.success(
            BootstrapOutput(
                config = BootstrapConfig(
                    version = configVersion,
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
                ),
                startup = StartupConfig(
                    authenticated = authenticated,
                    nextScreen = DynamicDestination(
                        screenId = screenId,
                        templateId = templateId,
                        templateType = templateType,
                        endpoint = endpoint,
                        method = method,
                        authentication = authentication,
                    ),
                ),
            ),
        )
    }
}
