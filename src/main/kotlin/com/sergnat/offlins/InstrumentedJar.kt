package com.sergnat.offlins

import org.gradle.jvm.tasks.Jar

open class InstrumentedJar : Jar() {

    init {
        description = "Assemble Jar with instrumented classes"

        val archiveName = "${project.name}-$INSTRUMENTED_JAR_SUFFIX"
        when {
            project.gradleVersion >= GRADLE_5_1 -> archiveBaseName.set(archiveName)
            else -> baseName = archiveName
        }
    }

    companion object {
        const val ASSEMBLE_INSTRUMENTED_JAR_TASK = "assembleInstrumentedJar"
        const val INSTRUMENTED_JAR_SUFFIX = "instrumented"
    }

}
