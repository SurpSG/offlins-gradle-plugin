package com.sergnat.offlins

import com.sergnat.offlins.OfflinsPlugin.Companion.JACOCO_CONFIGURATION
import groovy.lang.Closure
import groovy.lang.GroovyObject
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction
import java.io.File

open class InstrumentClassesOfflineTask : DefaultTask() {

    init {
        description = "JaCoCo offline instrumentation"
        dependsOn += "classes"
    }

    @OutputDirectory
    val instrumentedClassesDir: File = project.buildDir.resolve(OUTPUT_DIR_NAME)

    @TaskAction
    fun executeAction() {
        ant.invokeMethod(
            "taskdef",
            mapOf(
                "name" to "instrument",
                "classname" to "org.jacoco.ant.InstrumentTask",
                "classpath" to project.configurations.getByName(JACOCO_CONFIGURATION).asPath
            )
        )

        ant.invokeWithBody("instrument", mapOf("destdir" to instrumentedClassesDir)) {
            getClassFiles().addToAntBuilder(this, "resources")
        }
    }

    private fun GroovyObject.invokeWithBody(
        name: String,
        args: Map<String, Any>,
        body: GroovyObject.() -> Unit
    ) {
        invokeMethod(
            name,
            listOf(args, toClosure(body))
        )
    }

    private fun GroovyObject.toClosure(body: GroovyObject.() -> Unit): Closure<Any?> {
        return object : Closure<Any?>(this) {
            @Suppress("UNUSED_PARAMETER")
            fun doCall(ignore: Any?): Any? {
                body()
                return null
            }
        }
    }

    private fun getClassFiles(): FileCollection {
        val javaExt: JavaPluginExtension = project.extensions.getByType(JavaPluginExtension::class.java)
        val classesDirectory: Provider<Directory> = javaExt.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
            .java.classesDirectory

        return project.fileTree(classesDirectory) {
            it.setIncludes(listOf("**/*.class"))
        }
    }

    companion object {
        const val OUTPUT_DIR_NAME = "classes-instrumented"
    }

}
