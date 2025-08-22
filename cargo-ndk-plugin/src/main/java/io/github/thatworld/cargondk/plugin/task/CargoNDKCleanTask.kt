package io.github.thatworld.cargondk.plugin.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

open class CargoNDKCleanTask @Inject constructor(private val execOps: ExecOperations) : DefaultTask() {
    @get:Input
    var cleanTarget: Boolean = false

    @get:InputDirectory
    lateinit var rustSourceFile: File

    /**
     * Clean the output directory and optionally the Cargo build artifacts.
     * This function deletes the compiled shared libraries from the output directory
     * and runs `cargo clean` if the `cleanTarget` flag is set to true
     */
    fun cargoNDKClean(
        rustSourceFile: File,
        cleanTarget: Boolean,
    ) {
        // Clean the Cargo build artifacts
        if (cleanTarget) {
            execOps.exec { exec ->
                exec.commandLine = listOf("cargo", "clean")
                exec.workingDir = rustSourceFile
            }
        }
    }

    @TaskAction
    fun clean() {
        cargoNDKClean(
            rustSourceFile,
            cleanTarget
        )
    }
}