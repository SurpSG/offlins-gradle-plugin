package io.github.surpsg.offlins

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.testing.Test
import java.nio.file.Path
import java.nio.file.Paths

internal class TestTasksConfigurator {

    fun configure(offlinsContext: OfflinsContext) {
        val project: Project = offlinsContext.project

        val configurationContext: ConfigurationContext = createConfigurationContext(offlinsContext)
        val testTasks: TaskCollection<Test> = project.testTasks()
        testTasks.configureEach { testTask ->
            configureTestTask(configurationContext, testTask)
        }

        setTestsToDependOnInstrumentedJars(project, testTasks)
    }

    private fun createConfigurationContext(offlinsContext: OfflinsContext): ConfigurationContext {
        val recursiveDeps: Provider<Set<Project>> = offlinsContext.project.provider {
            recursiveOnProjectDependencies(offlinsContext.project) + offlinsContext.project
        }
        return ConfigurationContext(
            offlinsContext,
            recursiveDeps
        )
    }

    private fun configureTestTask(
        configurationContext: ConfigurationContext,
        testTask: Test
    ) {
        val offlinsContext: OfflinsContext = configurationContext.offlinsContext
        val execFile: Path = testTask.execFileLocation()
        offlinsContext.execFiles.add(execFile)

        testTask.dependsOn(offlinsContext.instrumentedClassesTask)

        offlinsContext.project.gradle.taskGraph.whenReady { graph ->
            val instrumentClassesTask: InstrumentClassesOfflineTask = offlinsContext.instrumentedClassesTask.get()
            if (graph.hasTask(instrumentClassesTask)) {
                testTask.doFirst(
                    SubstituteInstrumentedArtifactsAction(
                        offlinsContext,
                        configurationContext,
                        execFile,
                    )
                )
            }
        }
    }

    private fun Project.testTasks(): TaskCollection<Test> {
        return project.tasks.withType(Test::class.java)
    }

    private fun setTestsToDependOnInstrumentedJars(
        proj: Project,
        testTasks: TaskCollection<Test>
    ) = proj.afterEvaluate { project ->
        val onProjectDeps: Set<Dependency> = getOnProjectDependencies(project).asSequence()
            .map { onProjDep ->
                project.createOnProjectDependency(
                    onProjDep.name,
                    OfflinsPlugin.JACOCO_INSTRUMENTED_CONFIGURATION
                )
            }
            .toSet()

        testTasks.configureEach { testTask ->
            val testTaskImplementation = "${testTask.name}Implementation"
            onProjectDeps.forEach { dependency ->
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
        private val offlinsContext: OfflinsContext,
        private val configurationContext: ConfigurationContext,
        private val execFile: Path
    ) : Action<Task> {

        override fun execute(task: Task) = with(task as Test) {
            systemProperty("jacoco-agent.destfile", execFile)

            substituteInstrumentedClassesToClasspath()
            removeFromClasspathUninstrumentedJars()
            classpath += offlinsContext.offlinsConfigurations.jacocoRuntimeConfiguration.get()
        }

        private fun Test.substituteInstrumentedClassesToClasspath() {
            classpath -= project.files(project.getMainSourceSetClassFilesDir())
            classpath += offlinsContext.instrumentedClassesTask.map {
                project.files(it.instrumentedClassesDir)
            }.get()
        }

        private fun Test.removeFromClasspathUninstrumentedJars() {
            val newFileCollection: FileCollection = project.files()
            classpath -= configurationContext.projectRecursiveDependencies.get()
                .asSequence()
                .map { it.tasks.getByName(JavaPlugin.JAR_TASK_NAME) }
                .map { it.outputs.files }
                .fold(newFileCollection) { allFiles, nextPart ->
                    allFiles.plus(nextPart)
                }
        }

    }

    private class ConfigurationContext(
        val offlinsContext: OfflinsContext,
        val projectRecursiveDependencies: Provider<Set<Project>>
    )

}
