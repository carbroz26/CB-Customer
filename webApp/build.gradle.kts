plugins {
    id("cbpartner.web.application")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":app"))
            implementation(project(":domain"))
            implementation(project(":data"))
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(compose.material3)
            implementation(compose.runtime)
            implementation(compose.ui)
        }
        wasmJsMain.dependencies {
            implementation(project(":core"))
            implementation(project(":network"))
            implementation("org.jetbrains.kotlinx:kotlinx-browser:0.3.0")
        }
    }
}
