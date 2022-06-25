package io.github.surpsg.offlins

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.testing.Test
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

internal class TestTasksConfigurator {

    fun configure(context: OfflinsContext) {
        context.project.testTasks().configureEach { testTask ->
            configureTestTask(context, testTask)
        }
        setTestsToDependOnInstrumentedJars(context)
    }

    private fun configureTestTask(context: OfflinsContext, testTask: Test) {
        val execFile: Path = testTask.execFileLocation()
        context.execFiles.add(execFile)

        testTask.dependsOn(context.instrumentedClassesTask)

        context.project.gradle.taskGraph.whenReady { graph ->
            val instrumentClassesTask: InstrumentClassesOfflineTask = context.instrumentedClassesTask.get()
            if (graph.hasTask(instrumentClassesTask)) {
                testTask.doFirst(
                    SubstituteInstrumentedArtifactsAction( // TODO this is heavy task. Shouldn't be invoked in loop
                        instrumentClassesTask.instrumentedClassesDir,
                        execFile,
                        context.offlinsConfigurations.jacocoRuntimeConfiguration
                    )
                )
            }
        }
    }

    private fun Project.testTasks(): TaskCollection<Test> {
        return project.tasks.withType(Test::class.java)
    }

    private fun setTestsToDependOnInstrumentedJars(context: OfflinsContext) = context.project.afterEvaluate { project ->
        val testTasks: TaskCollection<Test> = project.testTasks()
        val onProjectDeps: Set<Dependency> = getOnProjectDependencies(project).asSequence()
            .map { onProjDep ->
                project.createOnProjectDependency(
                    onProjDep.name,
                    OfflinsPlugin.JACOCO_INSTRUMENTED_CONFIGURATION
                )
            }
            .toSet()

        testTasks.configureEach { testTask ->
            onProjectDeps.forEach { dependency ->
                val testTaskImplementation = "${testTask.name}${IMPLEMENTATION_CONFIGURATION_NAME.capitalize()}"
                project.dependencies.add(testTaskImplementation, dependency)
            }
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
            val recursiveDeps: Sequence<Project> = recursiveOnProjectDependencies(project).asSequence() + project
            classpath -= recursiveDeps
                .map { it.tasks.getByName(JavaPlugin.JAR_TASK_NAME) }
                .map { it.outputs.files }
                .fold(newFileCollection) { allFiles, nextPart ->
                    allFiles.plus(nextPart)
                }
        }

    }

}
