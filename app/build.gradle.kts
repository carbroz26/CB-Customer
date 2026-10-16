import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("cbpartner.kmp")
    id("cbpartner.compose")
}

kotlin {
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "CBPartnerApp"
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(project(":domain"))
            implementation(project(":data"))
            implementation(project(":network"))
            implementation(project(":navigation"))
            implementation(project(":feature:splash"))
            implementation(project(":feature:dynamic"))
            implementation(libs.kermit)
            implementation(libs.koin.core)
            implementation(compose.runtime)
            implementation(compose.material3)
        }
    }
}
