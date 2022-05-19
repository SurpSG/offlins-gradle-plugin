package com.sergnat.offlins

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.testing.Test
import java.io.File
import java.nio.file.Path

class TestTasksConfigurator(
    private val project: Project,
    private val jacocoRuntimeConf: Configuration
) {

    fun configure(
        instrumentClassesTask: InstrumentClassesOfflineTask,
        testTask: Test,
        execFile: Path
    ) {
        testTask.dependsOn(instrumentClassesTask.name)
        substituteInstrumentedArtifacts(
            instrumentClassesTask,
            testTask,
            execFile
        )
    }

    private fun substituteInstrumentedArtifacts(
        instrumentClassesTask: InstrumentClassesOfflineTask,
        testTask: Test,
        execFile: Path
    ) = project.gradle.taskGraph.whenReady { graph ->
        if (graph.hasTask(instrumentClassesTask)) {
            testTask.doFirstOnTestTask {
                systemProperty("jacoco-agent.destfile", execFile)

                substituteInstrumentedClassesToClasspath(instrumentClassesTask.instrumentedClassesDir)
                removeFromClasspathUninstrumentedJars()
                classpath += jacocoRuntimeConf
            }
        }
    }

    private fun Test.substituteInstrumentedClassesToClasspath(dirWithInstrumentedClasses: File) {
        classpath -= project.files(project.getMainSourceSetClassFilesDir())
        classpath += project.files(dirWithInstrumentedClasses)
    }

    private fun Test.removeFromClasspathUninstrumentedJars() {
        val newFileCollection: FileCollection = project.files()
        val recursiveOnProjectDependencyJars: FileCollection = recursiveOnProjectDependencies(project)
            .asSequence()
            .map { it.tasks.getByName(JavaPlugin.JAR_TASK_NAME) }
            .map { it.outputs.files }
            .fold(newFileCollection) { allFiles, nextPart ->
                allFiles.plus(nextPart)
            }
        classpath -= recursiveOnProjectDependencyJars
    }

    private fun Test.doFirstOnTestTask(action: Test.() -> Unit) {
        doFirst { testTask ->
            (testTask as Test).action()
        }
    }

}
