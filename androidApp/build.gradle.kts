plugins {
    id("cbpartner.android.application")
}

android {
    defaultConfig {
        applicationId = "com.carbroz.cbpartner.android"
        minSdk = 24
        targetSdk = 36
    }

    // One shared JSON fixture: package the Web preview file as a native asset
    // ONLY in the developmentDebug APK, never in staging or production builds.
    sourceSets.maybeCreate("developmentDebug").assets.srcDir(
        rootProject.file("webApp/src/wasmJsMain/resources/preview"),
    )
}

dependencies {
    implementation(project(":app"))
    implementation(project(":core"))
    implementation(project(":network"))
    implementation(libs.androidx.activity.compose)
}
