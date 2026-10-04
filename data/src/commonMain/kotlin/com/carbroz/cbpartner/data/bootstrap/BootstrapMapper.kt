package com.carbroz.cbpartner.data.bootstrap

import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapConfig
import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapFailure
import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapOutput
import com.carbroz.cbpartner.domain.model.bootstrap.FeatureConfig
import com.carbroz.cbpartner.domain.model.bootstrap.MaintenanceConfig
import com.carbroz.cbpartner.domain.model.bootstrap.StartupConfig
import com.carbroz.cbpartner.domain.model.bootstrap.UpdateConfig
import com.carbroz.cbpartner.domain.model.dynamic.DynamicDestination

internal fun BootstrapResponseDto.toDomain(): Result<BootstrapOutput> {
    if (status !in 200..299) return Result.failure(BootstrapFailure.Http(status))
    if (code != "SUCCESS") return Result.failure(BootstrapFailure.Api(code, message))

    val bootstrapData = data ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing bootstrap data"))
    val config = bootstrapData.config ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing bootstrap config"))
    val startup = bootstrapData.startup ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing bootstrap startup"))
    val maintenance = config.maintenance ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing maintenance config"))
    val update = config.update ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing update config"))
    val features = config.features ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing feature config"))
    val nextScreen = startup.nextScreen ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing next screen config"))

    return Result.success(
        BootstrapOutput(
            config = BootstrapConfig(
                version = config.version ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing config version")),
                maintenance = MaintenanceConfig(
                    enabled = maintenance.enabled ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing maintenance enabled flag")),
                    title = maintenance.title,
                    message = maintenance.message,
                ),
                update = UpdateConfig(
                    required = update.required ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing update required flag")),
                    optional = update.optional ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing update optional flag")),
                    minimumVersion = update.minimumVersion ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing minimum version")),
                    latestVersion = update.latestVersion ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing latest version")),
                    storeUrl = update.storeUrl,
                ),
                features = FeatureConfig(
                    registrationEnabled = features.registrationEnabled ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing registration flag")),
                    individualPartnerEnabled = features.individualPartnerEnabled ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing individual partner flag")),
                    organizationPartnerEnabled = features.organizationPartnerEnabled ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing organization partner flag")),
                ),
            ),
            startup = StartupConfig(
                authenticated = startup.authenticated ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing authenticated flag")),
                nextScreen = DynamicDestination(
                    screenId = nextScreen.screenId ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing next screen id")),
                    templateId = nextScreen.templateId ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing template id")),
                    templateType = nextScreen.templateType ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing template type")),
                    endpoint = nextScreen.endpoint ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing next screen endpoint")),
                    method = nextScreen.method ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing next screen method")),
                    authentication = nextScreen.authentication ?: return Result.failure(BootstrapFailure.InvalidResponse("Missing next screen authentication")),
                ),
            ),
        ),
    )
}
