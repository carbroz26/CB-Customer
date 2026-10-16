plugins {
    id("cbpartner.kmp")
    id("cbpartner.compose")
}

kotlin {
    androidLibrary {
        androidResources.enable = true
    }

    targets.withType<com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget>().configureEach {
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(project(":domain"))
            implementation(project(":navigation"))
            implementation(libs.kermit)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

compose.resources {
    packageOfResClass = "com.carbroz.cbpartner.feature.dynamic.generated.resources"
    generateResClass = always
}
