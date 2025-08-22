package io.github.thatworld.cargondk.plugin

import org.gradle.process.ExecOperations
import javax.inject.Inject

// This interface is used to inject ExecOperations into tasks or classes that require it.
// `project.objects.newInstance(InjectedExecOps::class.java).execOps`
interface InjectedExecOps {
    @get:Inject
    val execOps: ExecOperations
}