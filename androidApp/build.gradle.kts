import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

plugins {
    id("cbpartner.android.application")
    id("cbpartner.compose")
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

/** Copies canonical Web fixtures into developmentDebug assets; no separate Android JSON. */
abstract class GenerateDevelopmentPreviewAssets : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val loginJsonFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val otpJsonFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        for (source in listOf(loginJsonFile, otpJsonFile)) {
            val input = source.get().asFile
            val destination = outputDirectory.file(input.name).get().asFile
            destination.parentFile.mkdirs()
            input.copyTo(destination, overwrite = true)
        }
    }
}

val generateDevelopmentPreviewAssets =
    tasks.register<GenerateDevelopmentPreviewAssets>("generateDevelopmentPreviewAssets") {
        loginJsonFile.set(
            rootProject.layout.projectDirectory.file(
                "webApp/src/wasmJsMain/resources/preview/partner-login.json",
            ),
        )
        otpJsonFile.set(
            rootProject.layout.projectDirectory.file(
                "webApp/src/wasmJsMain/resources/preview/partner-otp.json",
            ),
        )
        outputDirectory.set(layout.buildDirectory.dir("generated/assets/developmentPreview"))
    }

/** No source fixture duplication; this generated directory belongs only to developmentDebug. */
androidComponents {
    onVariants(selector().withName("developmentDebug")) { variant ->
        checkNotNull(variant.sources.assets) {
            "Android developmentDebug assets source is unavailable"
        }.addGeneratedSourceDirectory(
            generateDevelopmentPreviewAssets,
            GenerateDevelopmentLoginPreviewAsset::outputDirectory,
        )
    }
}
