package com.sergnat.offlins

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import java.io.File
import java.nio.file.Paths

class TestTasksConfigurator(
    private val project: Project,
    private val jacocoRuntimeConf: Configuration
) {

    fun configure(
        instrumentClassesTask: InstrumentClassesOfflineTask,
        instrumentedJarTask: Jar
    ) {
        project.tasks.withType(Test::class.java) {
            it.dependsOn(instrumentClassesTask, instrumentedJarTask)
        }
        project.substituteInstrumentedArtifacts(instrumentClassesTask, instrumentedJarTask)
    }

    private fun Project.substituteInstrumentedArtifacts(
        instrumentClassesTask: InstrumentClassesOfflineTask,
        instrumentedJarTask: Jar
    ) = gradle.taskGraph.whenReady { graph ->
        if (graph.hasTask(instrumentClassesTask)) {
            tasks.doFirstOnTestTask {
                systemProperty("jacoco-agent.destfile", Paths.get(buildDir.path, "/jacoco/tests.exec"))

                substituteInstrumentedClassesToClasspath(instrumentClassesTask.instrumentedClassesDir)
                substituteInstrumentedJarToClasspath(instrumentedJarTask)
                classpath += jacocoRuntimeConf
            }
        }
    }

    private fun Test.substituteInstrumentedClassesToClasspath(dirWithInstrumentedClasses: File) {
        val javaExt: JavaPluginExtension = project.extensions.getByType(JavaPluginExtension::class.java)
        val classesDirectory: Provider<Directory> = javaExt.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
            .java.classesDirectory
        classpath -= project.files(classesDirectory)
        classpath += project.files(dirWithInstrumentedClasses)
    }

    private fun Test.substituteInstrumentedJarToClasspath(instrumentedJarTask: Jar) {
        val jarsToRemove: FileCollection = moduleDependencies(project)
            .flatMap { it.tasks.withType(Jar::class.java) }
            .map { it.outputs.files }
            .fold(project.files()) { files, candidate ->
                files.plus(candidate)
                files
            }
        classpath.minus(jarsToRemove)
        classpath += instrumentedJarTask.outputs.files
    }

    private fun moduleDependencies(project: Project): Set<Project> {
        val modules: MutableSet<Project> = mutableSetOf()
        val projectsToScan = ArrayDeque(listOf(project))
        while (projectsToScan.isNotEmpty()) {
            val proj: Project = projectsToScan.removeFirst()
            getProjectDependencies(proj)
                .map { it.dependencyProject }
                .forEach { dependencyProject ->
                    val notScannedBefore: Boolean = modules.add(dependencyProject)
                    if (notScannedBefore) {
                        projectsToScan.addLast(dependencyProject)
                    }
                }
        }
        return modules
    }

    private fun getProjectDependencies(project: Project): Set<ProjectDependency> {
        return project.configurations.names.asSequence()
            .filter { it == IMPLEMENTATION_CONFIG || it == COMPILE_CONFIG }
            .map { project.configurations.getByName(it) }
            .flatMap { it.dependencies.withType(ProjectDependency::class.java) }
            .toSet()
    }

    private fun TaskContainer.doFirstOnTestTask(action: Test.() -> Unit) {
        withType(Test::class.java) { testTask ->
            testTask.doFirst {
                (it as Test).action()
            }
        }
    }

    private companion object {
        const val IMPLEMENTATION_CONFIG = "implementation"
        const val COMPILE_CONFIG = "compile"
    }

}
