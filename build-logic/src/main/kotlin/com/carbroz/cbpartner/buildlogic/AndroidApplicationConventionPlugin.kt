package com.carbroz.cbpartner.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("com.android.application")
        target.pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        target.extensions.configure<ApplicationExtension> {
            namespace = "com.carbroz.cbpartner.android"
            compileSdk = 37
        }
    }
}
