plugins {
    id("cbpartner.web.application")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":data"))
            implementation(project(":domain"))
            implementation(project(":navigation"))
            implementation(project(":feature:splash"))
            implementation(project(":feature:dynamic"))
            implementation(libs.koin.core)
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.material3)
        }
    }
}
