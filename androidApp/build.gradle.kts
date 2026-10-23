plugins {
    id("cbpartner.android.application")
}

android {
    defaultConfig {
        applicationId = "com.carbroz.cbpartner.android"
        minSdk = 24
        targetSdk = 36
    }

}

dependencies {
    implementation(project(":app"))
    implementation(project(":core"))
    implementation(project(":network"))
    implementation(libs.androidx.activity.compose)
}

/**
 * The native development preview reads the same checked-in JSON as the Web preview.
 * Add it using the AGP variant Sources API so it is packaged into the APK rather
 * than relying on a manually created flavor/build-type source set.
 * Staging and production variants do not receive this directory.
 */
androidComponents {
    onVariants(selector().withName("developmentDebug")) { variant ->
        checkNotNull(variant.sources.assets) {
            "Android developmentDebug assets source is unavailable"
        }.addStaticSourceDirectory(
            rootProject.file("webApp/src/wasmJsMain/resources/preview").absolutePath,
        )
    }
}
