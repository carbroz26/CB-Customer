plugins {
    id("cbpartner.web.application")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":app"))
            implementation(compose.runtime)
            implementation(compose.ui)
        }
        wasmJsMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-browser:0.3.0")
        }
    }
}
