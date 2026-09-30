plugins {
    id("cbpartner.kmp")
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.koinCompiler)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":domain"))
            implementation(project(":core"))
            implementation(libs.koin.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.engine.defaults)
        }

        commonTest.dependencies {
            implementation(libs.koin.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
