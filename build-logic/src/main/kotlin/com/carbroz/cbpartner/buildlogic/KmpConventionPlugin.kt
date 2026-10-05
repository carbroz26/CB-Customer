package com.carbroz.cbpartner.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpConventionPlugin : Plugin<Project> {
    private fun namespaceFor(projectPath: String): String {
        val modulePath = projectPath.removePrefix(":").replace(":", ".")
        return if (modulePath.isBlank()) {
            "com.carbroz.cbpartner"
        } else {
            "com.carbroz.cbpartner.$modulePath"
        }
    }

    override fun apply(target: Project) {
        target.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        target.pluginManager.apply("com.android.kotlin.multiplatform.library")

        target.extensions.configure<KotlinMultiplatformExtension> {
            targets.withType(KotlinMultiplatformAndroidLibraryTarget::class.java).configureEach {
                compileSdk = 37
                namespace = namespaceFor(target.path)
            }
            iosArm64()
            iosSimulatorArm64()
            jvm()
            @OptIn(ExperimentalWasmDsl::class)
            wasmJs {
                browser()
            }
        }
    }
}
