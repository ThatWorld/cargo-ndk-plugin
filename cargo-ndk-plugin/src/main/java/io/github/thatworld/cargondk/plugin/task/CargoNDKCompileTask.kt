package io.github.thatworld.cargondk.plugin.task

import io.github.thatworld.cargondk.plugin.bean.CargoToml
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

open class CargoNDKCompileTask @Inject constructor(private val execOps: ExecOperations) : DefaultTask() {
    @get:Input
    lateinit var variantName: String

    @get:Input
    lateinit var abiFilters: Set<String>

    @get:InputDirectory
    lateinit var rustSourceFile: File

    @get:InputFile
    lateinit var manifestFile: File

    @get:OutputDirectory
    lateinit var outputFile: File

    @get:Input
    var platform: Int = 21

    @get:Input
    lateinit var environment: Map<String, String>

    @get:Input
    lateinit var args: Set<String>

    @get:Internal
    lateinit var cargoToml: CargoToml

    /**
     * Compile the Rust project using cargo-ndk.
     * This function checks if cargo and cargo-ndk are installed, installs them if necessary,
     * checks for required targets, and runs the cargo-ndk command to compile the project.
     */
    fun cargoNDKCompile(
        cargoToml: CargoToml,
        abiFilters: Set<String>,
        variantName: String,
        rustSourceFile: File,
        manifestFile: File,
        outputFile: File,
        platform: Int,
        args: Set<String>,
    ) {
        val errorStream = ByteArrayOutputStream()
        val standardStream = ByteArrayOutputStream()

        // Check if cargo is installed
        try {
            errorStream.reset()
            standardStream.reset()
            execOps.exec { exec ->
                exec.commandLine = listOf("cargo", "--version")
                exec.errorOutput = errorStream
                exec.standardOutput = standardStream
            }
        } catch (e: Exception) {
            throw RuntimeException("cargo not found, did you install rust? https://www.rust-lang.org/tools/install")
        }

        // Check if cargo-ndk is installed
        try {
            errorStream.reset()
            standardStream.reset()
            execOps.exec { exec ->
                exec.commandLine = listOf("cargo", "ndk", "--version")
                exec.errorOutput = errorStream
                exec.standardOutput = standardStream
            }
        } catch (e: Exception) {
            println("installing cargo-ndk...")
            execOps.exec { exec ->
                exec.commandLine = listOf("cargo", "install", "cargo-ndk")
                exec.errorOutput = System.err
                exec.standardOutput = System.out
            }
        }

        // Parse the output to find required targets
        val targets = mutableMapOf(
            "arm64-v8a" to "aarch64-linux-android",
            "armeabi-v7a" to "armv7-linux-androideabi",
            "x86_64" to "x86_64-linux-android",
            "x86" to "i686-linux-android",
        ).run {
            if (abiFilters.isNotEmpty()) {
                filter {
                    abiFilters.contains(it.key)
                }
            } else {
                this
            }
        }.toMutableMap()

        // Check if toolchains are installed
        try {
            errorStream.reset()
            standardStream.reset()
            execOps.exec { exec ->
                exec.commandLine = listOf("rustup", "target", "list")
                exec.errorOutput = errorStream
                exec.standardOutput = standardStream
            }

            val requiredTargets = mutableListOf<String>()
            val entries = targets.entries
            val lines = standardStream.toString().trim().split('\n')
            for (line in lines) {
                if (line.contains("(installed)")) continue
                val firstOrNull = entries.firstOrNull { line.contains(it.value) }
                if (firstOrNull != null) requiredTargets.add(firstOrNull.value)
            }

            // If there are any missing targets, install them
            if (requiredTargets.isNotEmpty()) {
                println(
                    "installing missing targets: ${requiredTargets.joinToString(", ")}...\n" +
                            "If the speed is slow, you can also run `rustup target add ${requiredTargets.joinToString(" ")}` manually to install them."
                )
                execOps.exec { exec ->
                    exec.commandLine = listOf("rustup", "target", "add") + requiredTargets
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("rustup not found, did you install rust? https://www.rust-lang.org/tools/install")
        }

        // Run cargo-ndk to compile the project
        try {
            errorStream.reset()
            standardStream.reset()

            val compileCommand = mutableListOf("cargo", "ndk")

            // --target
            compileCommand += targets.keys.flatMap { target -> listOf("--target", target) }

            // --out-dir
            compileCommand += listOf("--output-dir", outputFile.absolutePath)

            // --platform
            compileCommand += listOf("--platform", "$platform")

            // --manifest-path
            compileCommand += listOf(
                "--manifest-path",
                manifestFile.absolutePath
            )

            // args
            compileCommand += args

            // build
            compileCommand += if (variantName == "release") {
                listOf("build", "--release")
            } else {
                listOf("build")
            }

            println("Running cargo-ndk with command: `${compileCommand.joinToString(" ")}`")

            execOps.exec { exec ->
                exec.commandLine = compileCommand
                exec.workingDir = rustSourceFile
                exec.environment.putAll(environment)
            }
        } catch (e: Exception) {
            throw RuntimeException("cargo-ndk failed to run, please check the error message: $errorStream")
        }
    }

    @TaskAction
    fun compile() {
        cargoNDKCompile(
            cargoToml = cargoToml,
            abiFilters = abiFilters,
            variantName = variantName,
            rustSourceFile = rustSourceFile,
            manifestFile = manifestFile,
            outputFile = outputFile,
            platform = platform,
            args = args,
        )
    }
}