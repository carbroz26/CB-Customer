plugins {
    id("cbpartner.kmp")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(libs.navigation3.runtime)
            implementation(libs.navigation3.ui)
        }
    }
}
