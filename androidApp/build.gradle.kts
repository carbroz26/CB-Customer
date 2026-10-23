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

/** Copies the canonical Web Login fixture into one generated Android asset directory. */
abstract class GenerateDevelopmentLoginPreviewAsset : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val jsonFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val destination = outputDirectory.file("partner-login.json").get().asFile
        destination.parentFile.mkdirs()
        jsonFile.get().asFile.copyTo(destination, overwrite = true)
    }
}

val generateDevelopmentLoginPreviewAsset =
    tasks.register<GenerateDevelopmentLoginPreviewAsset>("generateDevelopmentLoginPreviewAsset") {
        jsonFile.set(
            rootProject.layout.projectDirectory.file(
                "webApp/src/wasmJsMain/resources/preview/partner-login.json",
            ),
        )
        outputDirectory.set(layout.buildDirectory.dir("generated/assets/developmentLoginPreview"))
    }

/** No source fixture duplication; this generated directory belongs only to developmentDebug. */
androidComponents {
    onVariants(selector().withName("developmentDebug")) { variant ->
        checkNotNull(variant.sources.assets) {
            "Android developmentDebug assets source is unavailable"
        }.addGeneratedSourceDirectory(
            generateDevelopmentLoginPreviewAsset,
            GenerateDevelopmentLoginPreviewAsset::outputDirectory,
        )
    }
}
