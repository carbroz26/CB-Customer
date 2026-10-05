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
    implementation(libs.androidx.activity.compose)
}
