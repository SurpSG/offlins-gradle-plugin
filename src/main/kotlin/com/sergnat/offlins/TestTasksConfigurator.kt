package com.sergnat.offlins

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.testing.Test
import java.io.File
import java.nio.file.Paths

class TestTasksConfigurator(
    private val project: Project,
    private val jacocoRuntimeConf: Configuration
) {

    private val testTaskName: String = JavaPlugin.TEST_TASK_NAME

    fun configure(instrumentClassesTask: InstrumentClassesOfflineTask) {
        project.tasks.getByName(testTaskName).dependsOn(instrumentClassesTask.name)

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
            .map { it.tasks.getByName(JavaPlugin.JAR_TASK_NAME) }
            .map { it.outputs.files }
            .fold(newFileCollection) { allFiles, nextPart ->
                allFiles.plus(nextPart)
            }
        classpath -= recursiveOnProjectDependencyJars
    }

    private fun TaskContainer.doFirstOnTestTask(action: Test.() -> Unit) {
        getByName(testTaskName) {
            it.doFirst { testTask ->
                (testTask as Test).action()
            }
        }
    }

    companion object {
        const val DEFAULT_RELATIVE_JACOCO_EXEC_LOCATION = "jacoco/tests.exec"
    }

}
