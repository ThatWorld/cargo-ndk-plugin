package io.github.thatworld.cargondk.plugin.task

import io.github.thatworld.cargondk.plugin.InjectedExecOps
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class CargoNDKCleanTask : DefaultTask() {
    @get:Input
    var cleanTarget: Boolean = false

    @get:InputDirectory
    lateinit var rustSourceFile: File

    /**
     * Clean the output directory and optionally the Cargo build artifacts.
     * This function deletes the compiled shared libraries from the output directory
     * and runs `cargo clean` if the `cleanTarget` flag is set to true
     */
    fun Project.cargoNDKClean(
        rustSourceFile: File,
        cleanTarget: Boolean,
    ) {
        val execOps = this.objects.newInstance(InjectedExecOps::class.java).execOps

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
        project.cargoNDKClean(
            rustSourceFile,
            cleanTarget
        )
    }
}