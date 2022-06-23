package io.github.surpsg.offlins

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.testing.Test
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

internal class TestTasksConfigurator(
    private val offlinsContext: OfflinsContext
) {

    fun configure() {
        offlinsContext.project.tasks.withType(Test::class.java).forEach { testTask ->
            configureTestTask(testTask)
        }
        offlinsContext.project.setTestsToDependOnInstrumentedJars()
    }

    private fun configureTestTask(testTask: Test) {
        val execFile: Path = testTask.execFileLocation()
        offlinsContext.execFiles.add(execFile)

        // TODO use provider instead of .get() ?
        val instrumentClassesTask: InstrumentClassesOfflineTask = offlinsContext.instrumentedClassesTask.get()
        testTask.dependsOn(instrumentClassesTask.name)

        offlinsContext.project.gradle.taskGraph.whenReady { graph ->
            if (graph.hasTask(instrumentClassesTask)) {
                testTask.doFirst(
                    SubstituteInstrumentedArtifactsAction( // TODO this is heavy task. Shouldn't be invoked in loop
                        instrumentClassesTask.instrumentedClassesDir,
                        execFile,
                        offlinsContext.offlinsConfigurations.jacocoRuntimeConfiguration
                    )
                )
            }
        }
    }

    private fun Project.setTestsToDependOnInstrumentedJars() = afterEvaluate {
        getOnProjectDependencies(project).forEach { onProjectDep ->
            dependencies.add(
                JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, // TODO other test tasks
                project.createOnProjectDependency(onProjectDep.name, OfflinsPlugin.JACOCO_INSTRUMENTED_CONFIGURATION)
            )
        }
    }

    private fun Test.execFileLocation(): Path = Paths.get(
        project.buildDir.path,
        "jacoco",
        "$name.exec"
    )

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
