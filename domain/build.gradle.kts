plugins {
    id("cbpartner.kmp")
}

kotlin {
    targets.withType<com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget>().configureEach {
        withHostTest {}
    }
}
