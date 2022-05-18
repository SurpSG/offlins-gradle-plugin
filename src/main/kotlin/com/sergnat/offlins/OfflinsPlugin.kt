package com.sergnat.offlins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME
import org.gradle.api.plugins.JavaPlugin.RUNTIME_ONLY_CONFIGURATION_NAME
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class OfflinsPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        val offlinsExtension = extensions.create(OFFLINS_EXTENSION, OfflinsExtension::class.java)
        val jacocoVersion: Provider<String> = provider { offlinsExtension.jacocoVersion }

        addConfigurationWithDependency(
            JACOCO_CONFIGURATION,
            jacocoAntDependency(jacocoVersion)
        )
        val jacocoRuntimeConf: Configuration = addConfigurationWithDependency(
            JACOCO_RUNTIME_CONFIGURATION,
            jacocoAgentDependency(jacocoVersion)
        )

        val instrumentClassesTask = tasks.create(INSTRUMENT_CLASSES_TASK, InstrumentClassesOfflineTask::class.java)

        val instrumentedJar = tasks.createAssembleInstrumentedJarTask(instrumentClassesTask.instrumentedClassesDir)
        setupInstrumentedJarConfiguration(instrumentedJar)
        setTestsToDependOnInstrumentedJars()

        val testTask: Test = tasks.getByName(JavaPlugin.TEST_TASK_NAME) as Test
        val execFile: Path = testTask.execFileLocation()
        TestTasksConfigurator(project, jacocoRuntimeConf).configure(instrumentClassesTask, testTask, execFile)

        tasks.create(
            GENERATE_JACOCO_REPORTS_TASK,
            OfflinsJacocoReport::class.java,
            execFile
        )
    }

    private fun TaskContainer.createAssembleInstrumentedJarTask(instrumentedClassesDir: File): Jar {
        return create(ASSEMBLE_INSTRUMENTED_JAR_TASK, Jar::class.java).apply {
            description = "Assemble Jar with instrumented classes"
            dependsOn += INSTRUMENT_CLASSES_TASK

            from(instrumentedClassesDir)

            val archiveName = "${project.name}-$INSTRUMENTED_JAR_SUFFIX"
            when {
                project.gradleVersion >= GRADLE_5_1 -> archiveBaseName.set(archiveName)
                else -> baseName = archiveName
            }
        }
    }

    private fun Project.setupInstrumentedJarConfiguration(instrumentedJar: Jar) {
        val instrumentedJarConfiguration: Configuration = configurations.create(JACOCO_INSTRUMENTED_CONFIGURATION) {
            it.isCanBeConsumed = true
            it.isCanBeResolved = false
            it.extendsFrom(
                configurations.getByName(IMPLEMENTATION_CONFIGURATION_NAME),
                configurations.getByName(RUNTIME_ONLY_CONFIGURATION_NAME)
            )
        }
        artifacts.add(instrumentedJarConfiguration.name, instrumentedJar)
    }

    private fun Project.setTestsToDependOnInstrumentedJars() = afterEvaluate {
        getOnProjectDependencies(project).forEach { onProjectDep ->
            dependencies.add(
                JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME,
                project.createOnProjectDependency(onProjectDep.name, JACOCO_INSTRUMENTED_CONFIGURATION)
            )
        }
    }

    private fun Project.addConfigurationWithDependency(
        configurationName: String,
        jacocoDependency: Provider<JacocoDependency>
    ): Configuration {
        val configuration: Configuration = configurations.create(configurationName)
        when {
            gradleVersion >= GRADLE_6_8 -> {
                dependencies.add(
                    configuration.name,
                    jacocoDependency.map { it.buildDependency(dependencies) }
                )
            }
            else -> afterEvaluate {
                dependencies.add(configuration.name, jacocoDependency.get().buildDependency(dependencies))
            }
        }
        return configuration
    }

    private fun Test.execFileLocation(): Path = Paths.get(
        project.buildDir.path,
        "jacoco",
        "$name.exec"
    )

    companion object {
        const val OFFLINS_EXTENSION = "offlinsCoverage"

        const val INSTRUMENT_CLASSES_TASK = "instrumentClassesOffline"
        const val ASSEMBLE_INSTRUMENTED_JAR_TASK = "assembleInstrumentedJar"
        const val GENERATE_JACOCO_REPORTS_TASK = "coverageReport"

        const val INSTRUMENTED_JAR_SUFFIX = "instrumented"

        const val JACOCO_CONFIGURATION = "jacoco"
        const val JACOCO_RUNTIME_CONFIGURATION = "jacocoRuntime"
        const val JACOCO_INSTRUMENTED_CONFIGURATION = "jacocoInstrumented"
    }

}
