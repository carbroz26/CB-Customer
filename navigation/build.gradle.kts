plugins {
    id("cbpartner.kmp")
    id("cbpartner.compose")
}

kotlin {
    targets.withType<com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget>().configureEach {
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(project(":domain"))
            implementation(libs.navigation3.runtime)
            implementation(libs.navigation3.ui)
            implementation(compose.runtime)
            implementation(compose.ui)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
