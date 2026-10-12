package com.carbroz.cbpartner.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("com.android.application")
        target.pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        val configuredApiBaseUrl = target.providers.gradleProperty("cbpartner.api.baseUrl")
        val debugApiBaseUrl = configuredApiBaseUrl
            .orElse("http://10.0.2.2:3000")
            .get()
        val releaseApiBaseUrl = configuredApiBaseUrl
            .orElse("https://api.invalid")
            .get()

        target.extensions.configure<ApplicationExtension> {
            namespace = "com.carbroz.cbpartner.android"
            compileSdk = 37
            buildFeatures {
                buildConfig = true
            }
            defaultConfig {
                buildConfigField(
                    "String",
                    "CBPARTNER_API_BASE_URL",
                    "\"$debugApiBaseUrl\"",
                )
            }
            buildTypes.getByName("release") {
                buildConfigField(
                    "String",
                    "CBPARTNER_API_BASE_URL",
                    "\"$releaseApiBaseUrl\"",
                )
            }
        }
    }
}
