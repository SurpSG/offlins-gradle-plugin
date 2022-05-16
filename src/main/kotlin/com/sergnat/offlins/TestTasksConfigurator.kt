package com.sergnat.offlins

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import java.io.File
import java.nio.file.Paths

class TestTasksConfigurator(
    private val project: Project,
    private val jacocoRuntimeConf: Configuration
) {

    fun configure(instrumentClassesTask: InstrumentClassesOfflineTask) {
        project.tasks.withType(Test::class.java) { testTask ->
            testTask.dependsOn(instrumentClassesTask)
        }
        substituteInstrumentedArtifacts(instrumentClassesTask)
    }

    private fun substituteInstrumentedArtifacts(
        instrumentClassesTask: InstrumentClassesOfflineTask
    ) = project.gradle.taskGraph.whenReady { graph ->
        if (graph.hasTask(instrumentClassesTask)) {
            project.tasks.doFirstOnTestTask {
                systemProperty(
                    "jacoco-agent.destfile",
                    Paths.get(project.buildDir.path, DEFAULT_RELATIVE_JACOCO_EXEC_LOCATION)
                )

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
            .flatMap { it.tasks.withType(Jar::class.java) }
            .filter { it.name == "jar" }
            .map { it.outputs.files }
            .fold(newFileCollection) { allFiles, nextPart ->
                allFiles.plus(nextPart)
            }
        classpath -= recursiveOnProjectDependencyJars
    }

    private fun TaskContainer.doFirstOnTestTask(action: Test.() -> Unit) {
        withType(Test::class.java) { testTask ->
            testTask.doFirst {
                testTask.action()
            }
        }
    }

    companion object {
        const val DEFAULT_RELATIVE_JACOCO_EXEC_LOCATION = "jacoco/tests.exec"
    }

}
