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
    }
}
