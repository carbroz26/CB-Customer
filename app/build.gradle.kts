plugins {
    id("cbpartner.kmp")
    id("cbpartner.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":domain"))
            implementation(project(":data"))
            implementation(project(":navigation"))
            implementation(project(":feature:splash"))
            implementation(project(":feature:dynamic"))
            implementation(libs.koin.core)
            implementation(compose.runtime)
            implementation(compose.material3)
        }
    }
}
