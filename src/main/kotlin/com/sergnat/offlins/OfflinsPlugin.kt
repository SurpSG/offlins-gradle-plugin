package com.sergnat.offlins

import com.sergnat.offlins.InstrumentClassesOfflineTask.Companion.INSTRUMENT_CLASSES_TASK
import com.sergnat.offlins.InstrumentedJar.Companion.ASSEMBLE_INSTRUMENTED_JAR_TASK
import com.sergnat.offlins.OfflinsJacocoReport.Companion.GENERATE_JACOCO_REPORTS_TASK
import com.sergnat.offlins.utils.orElseProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME
import org.gradle.api.plugins.JavaPlugin.RUNTIME_ONLY_CONFIGURATION_NAME
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.Test
import java.nio.file.Path
import java.nio.file.Paths

class OfflinsPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        val offlinsExtension = extensions.create(OFFLINS_EXTENSION, OfflinsExtension::class.java)
        val jacocoVersion: Provider<String> = offlinsExtension.jacocoVersion.orElseProvider(
            provider { DEFAULT_JACOCO_VERSION }
        )

        addConfigurationWithDependency(
            JACOCO_CONFIGURATION,
            jacocoAntDependency(jacocoVersion)
        )
        val jacocoRuntimeConf: Configuration = addConfigurationWithDependency(
            JACOCO_RUNTIME_CONFIGURATION,
            jacocoAgentDependency(jacocoVersion)
        )

        val instrumentClassesTask = tasks.create(INSTRUMENT_CLASSES_TASK, InstrumentClassesOfflineTask::class.java)
        setupInstrumentedJarTask(instrumentClassesTask)

        val testTask: Test = tasks.getByName(JavaPlugin.TEST_TASK_NAME) as Test
        val execFile: Path = testTask.execFileLocation()
        TestTasksConfigurator(project, jacocoRuntimeConf).configure(instrumentClassesTask, testTask, execFile)

        tasks.create(
            GENERATE_JACOCO_REPORTS_TASK,
            OfflinsJacocoReport::class.java
        ) {
            it.execDataFile.set(execFile)
            it.reportsExtension.set(provider {
                offlinsExtension.report
            })
        }
    }

    private fun Project.setupInstrumentedJarTask(instrumentClassesTask: InstrumentClassesOfflineTask) {
        val instrumentedJar = tasks.create(ASSEMBLE_INSTRUMENTED_JAR_TASK, InstrumentedJar::class.java) { jar ->
            jar.dependsOn += INSTRUMENT_CLASSES_TASK
            jar.from(instrumentClassesTask.instrumentedClassesDir)
        }

        val instrumentedJarConfiguration: Configuration = configurations.create(JACOCO_INSTRUMENTED_CONFIGURATION) {
            it.isCanBeConsumed = true
            it.isCanBeResolved = false
            it.extendsFrom(
                configurations.getByName(IMPLEMENTATION_CONFIGURATION_NAME),
                configurations.getByName(RUNTIME_ONLY_CONFIGURATION_NAME)
            )
        }
        artifacts.add(instrumentedJarConfiguration.name, instrumentedJar)

        setTestsToDependOnInstrumentedJars()
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
        const val DEFAULT_JACOCO_VERSION = "0.8.8"

        const val OFFLINS_EXTENSION = "offlinsCoverage"

        const val JACOCO_CONFIGURATION = "jacoco"
        const val JACOCO_RUNTIME_CONFIGURATION = "jacocoRuntime"
        const val JACOCO_INSTRUMENTED_CONFIGURATION = "jacocoInstrumented"
    }

}
