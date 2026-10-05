import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

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

    sourceSets {
        commonMain.dependencies {
            implementation(project(":domain"))
            implementation(project(":data"))
            implementation(project(":network"))
            implementation(project(":navigation"))
            implementation(project(":feature:splash"))
            implementation(project(":feature:dynamic"))
            implementation(libs.koin.core)
            implementation(compose.runtime)
            implementation(compose.material3)
        }
    }
}
