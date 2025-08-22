package io.github.thatworld.cargondk.plugin

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.BaseExtension
import io.github.thatworld.cargondk.plugin.bean.CargoToml
import io.github.thatworld.cargondk.plugin.task.CargoNDKCleanTask
import io.github.thatworld.cargondk.plugin.task.CargoNDKCompileTask
import kotlinx.serialization.decodeFromString
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

fun String.variantFormat(variant: String): String {
    return this
        .replace("{variant}", variant)
        .replace("{Variant}", variant.replaceFirstChar { it.uppercase() })
}

class CargoNDKPlugin : Plugin<Project> {
    val extensionName = "cargoNdk"

    override fun apply(target: Project) {
        with(target) {
            // check if the project is an Android application module
            val appModuleExtension = extensions.getByType(BaseExtension::class.java)
                ?: throw GradleException("$name plugin must be used with android plugin.")
            extensions.create(extensionName, CargoNDKExtension::class.java)

            // register tasks for each variant
            val componentsExtension = extensions.getByType(AndroidComponentsExtension::class.java)
            componentsExtension.onVariants { variant ->
                val cargoNDKExtension = extensions.getByType(CargoNDKExtension::class.java)
                val abiFilters = appModuleExtension.defaultConfig.ndk.abiFilters
                val rustSource = cargoNDKExtension.source
                val manifestPath = cargoNDKExtension.manifestPath
                val environment = cargoNDKExtension.environment
                val output = cargoNDKExtension.output
                val platform = cargoNDKExtension.platform
                val args = cargoNDKExtension.args
                val cargoToml = parseCargoToml(rustSource, manifestPath)

                val variantUpName = variant.name.replaceFirstChar { it.uppercase() }
                tasks.register(
                    "cargoNDK${variantUpName}Compile",
                    CargoNDKCompileTask::class.java
                ) { task ->
                    task.group = "Cargo NDK"
                    task.description = "Compiles the Rust project using cargo-ndk."
                    task.variantName = variant.name
                    task.abiFilters = abiFilters
                    task.rustSourceFile = getRustSourceFile(rustSource)
                    task.manifestFile = getManifestFile(rustSource, manifestPath)
                    task.outputFile = getOutputFile(output.variantFormat(variant.name))
                    task.platform = platform
                    task.environment = environment
                    task.args = args
                    task.cargoToml = cargoToml
                }
            }

            // after evaluate, we can access the tasks and variants
            afterEvaluate {
                // register clean task
                val cargoNDKExtension = extensions.getByType(CargoNDKExtension::class.java)
                val rustSource = cargoNDKExtension.source
                val cleanTarget = cargoNDKExtension.cleanTarget
                tasks.register("cargoNDKClean", CargoNDKCleanTask::class.java) { task ->
                    task.group = "Cargo NDK"
                    task.description = "Cleans the output directory and optionally the Cargo build artifacts."
                    task.rustSourceFile = getRustSourceFile(rustSource)
                    task.cleanTarget = cleanTarget
                }


                // debug
                tasks.named("mergeDebugNativeLibs") { task ->
                    task.finalizedBy("cargoNDKDebugCompile")
                }
                tasks.named("stripDebugDebugSymbols") { task ->
                    task.dependsOn("cargoNDKDebugCompile")
                }

                // release
                tasks.named("mergeReleaseNativeLibs") { task ->
                    task.finalizedBy("cargoNDKReleaseCompile")
                }
                tasks.named("stripReleaseDebugSymbols") { task ->
                    task.dependsOn("cargoNDKReleaseCompile")
                }

                // clean
                tasks.named("clean") { tasks ->
                    tasks.dependsOn("cargoNDKClean")
                }
            }
        }
    }

    /**
     * Get the Rust source file from the project.
     * If the path is relative, it will be resolved against the project directory.
     */
    fun Project.getRustSourceFile(rustSource: String): File {
        val rustSourceFile = File(rustSource)
        return if (rustSourceFile.isAbsolute) {
            rustSourceFile
        } else {
            project.layout.projectDirectory.dir(rustSource).asFile
        }
    }

    /**
     * Get the manifest file (Cargo.toml) from the project.
     * If the path is relative, it will be resolved against the Rust source directory.
     */
    fun Project.getManifestFile(rustSource: String, manifestPath: String): File {
        val manifestFile = File(manifestPath)
        return if (manifestFile.isAbsolute) {
            manifestFile
        } else {
            project.layout.projectDirectory.dir(rustSource).file(manifestPath).asFile
        }
    }

    /**
     * Get the output file from the project.
     * If the path is relative, it will be resolved against the project directory.
     */
    fun Project.getOutputFile(output: String): File {
        val outputFile = File(output)
        return if (outputFile.isAbsolute) {
            outputFile
        } else {
            project.layout.buildDirectory.dir(output).get().asFile
        }
    }

    /**
     * Check if the Cargo.toml file exists in the specified Rust source directory.
     * If not, throw a GradleException with an appropriate message.
     */
    fun Project.parseCargoToml(rustSource: String, manifestPath: String): CargoToml {
        val cargoToml = getManifestFile(rustSource, manifestPath)
        if (!cargoToml.exists() && !cargoToml.isFile)
            throw GradleException(
                "`Cargo.toml` not found in ${cargoToml.absolutePath}, " +
                        "please ensure that the cargoNdk.rustSrc directory is set correctly."
            )

        // parse the Cargo.toml file
        return Toml(
            inputConfig = TomlInputConfig(
                ignoreUnknownNames = true,
            )
        ).decodeFromString<CargoToml>(
            getManifestFile(rustSource, manifestPath)
                .readText()
        ).also {
            // todo: check cargoToml whether it contains [lib] and `crate-type = ["cdylib"]`
        }
    }
}