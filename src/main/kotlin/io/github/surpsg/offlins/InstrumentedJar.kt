package io.github.surpsg.offlins

import org.gradle.jvm.tasks.Jar

@Deprecated("Why not excluded from the coverage")
open class InstrumentedJar : Jar() {

    init {
        description = "Assemble Jar with instrumented classes"

        val archiveName = "${project.name}-$INSTRUMENTED_JAR_SUFFIX"
        archiveBaseName.set(archiveName)
        archiveFileName.set("$archiveName.jar")
    }

    companion object {
        const val ASSEMBLE_INSTRUMENTED_JAR_TASK = "assembleInstrumentedJar"
        const val INSTRUMENTED_JAR_SUFFIX = "instrumented"
    }

}
