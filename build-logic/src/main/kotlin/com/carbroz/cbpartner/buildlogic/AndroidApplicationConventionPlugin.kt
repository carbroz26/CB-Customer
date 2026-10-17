package com.carbroz.cbpartner.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Owns Android build variants and externally supplied deployment origins.
 * Debuggability and deployment environment remain independent dimensions.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("com.android.application")
        target.pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        val developmentUrl = target.providers.gradleProperty("cbpartner.api.baseUrl")
            .orElse("http://10.0.2.2:3000").get()
        val stagingUrl = target.providers.gradleProperty("cbpartner.api.stagingBaseUrl").orElse("").get()
        val productionUrl = target.providers.gradleProperty("cbpartner.api.productionBaseUrl").orElse("").get()

        target.extensions.configure<ApplicationExtension> {
            namespace = "com.carbroz.cbpartner.android"
            compileSdk = 37
            buildFeatures { buildConfig = true }
            flavorDimensions += "environment"
            productFlavors {
                create("development") {
                    dimension = "environment"
                    applicationIdSuffix = ".dev"
                    buildConfigField("String", "CBPARTNER_ENVIRONMENT", "\"development\"")
                    buildConfigField("String", "CBPARTNER_API_BASE_URL", "\"$developmentUrl\"")
                }
                create("staging") {
                    dimension = "environment"
                    applicationIdSuffix = ".staging"
                    buildConfigField("String", "CBPARTNER_ENVIRONMENT", "\"staging\"")
                    buildConfigField("String", "CBPARTNER_API_BASE_URL", "\"$stagingUrl\"")
                }
                create("production") {
                    dimension = "environment"
                    buildConfigField("String", "CBPARTNER_ENVIRONMENT", "\"production\"")
                    buildConfigField("String", "CBPARTNER_API_BASE_URL", "\"$productionUrl\"")
                }
            }
        }

        // Run validation on the selected package/bundle task, not during Gradle configuration.
        // This keeps developmentDebug builds independent of staging/production credentials.
        target.tasks.configureEach {
            val variant = when {
                name.startsWith("packageStaging") || name.startsWith("bundleStaging") -> "staging" to stagingUrl
                name.startsWith("packageProduction") || name.startsWith("bundleProduction") -> "production" to productionUrl
                else -> null
            }
            if (variant != null) {
                doFirst {
                    val (environment, origin) = variant
                    val parsed = runCatching { java.net.URI(origin) }.getOrNull()
                    val host = parsed?.host?.lowercase()
                    val forbidden = listOf(".invalid", ".test", ".example", ".localhost", ".local", ".internal")
                    if (parsed?.scheme != "https" || host.isNullOrBlank() ||
                        !host.contains('.') || host.none { it in 'a'..'z' } || host.any { it !in 'a'..'z' && it !in '0'..'9' && it != '.' && it != '-' } ||
                        forbidden.any { host.endsWith(it) } ||
                        parsed.rawUserInfo != null || !parsed.rawPath.isNullOrEmpty() ||
                        parsed.rawQuery != null || parsed.rawFragment != null
                    ) {
                        throw GradleException(
                            "Explicit trusted HTTPS API origin required for $environment; " +
                                "configure cbpartner.api.${if (environment == "staging") "stagingBaseUrl" else "productionBaseUrl"}",
                        )
                    }
                }
            }
        }
    }
}
