package io.github.surpsg.offlins

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
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
        project.gradle.taskGraph.whenReady { graph ->
            if (graph.hasTask(instrumentClassesTask)) {
                testTask.doFirst(
                    SubstituteInstrumentedArtifactsAction(
                    instrumentClassesTask.instrumentedClassesDir,
                    execFile,
                    jacocoRuntimeConf
                )
                )
            }
        }
    }

    private class SubstituteInstrumentedArtifactsAction(
        private val instrumentClassesDir: File,
        private val execFile: Path,
        private val jacocoRuntimeDependencies: FileCollection
    ) : Action<Task> {
        override fun execute(task: Task) = with(task as Test) {
            systemProperty("jacoco-agent.destfile", execFile)

            substituteInstrumentedClassesToClasspath(instrumentClassesDir)
            removeFromClasspathUninstrumentedJars()
            classpath += jacocoRuntimeDependencies
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

    }

}
