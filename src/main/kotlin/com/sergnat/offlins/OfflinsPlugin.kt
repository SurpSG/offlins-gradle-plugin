package com.sergnat.offlins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.TaskContainer
import org.gradle.jvm.tasks.Jar
import java.io.File

class OfflinsPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        addConfigurationWithDependency(JACOCO_CONFIGURATION, JACOCO_ANT)
        val jacocoRuntimeConf: Configuration = addConfigurationWithDependency(
            JACOCO_RUNTIME_CONFIGURATION, JACOCO_AGENT
        )
        val jacocoInstrumentedConf: Configuration = configurations.create(JACOCO_INSTRUMENTED_CONFIGURATION)

        with(tasks) {
            val instrumentClassesTask = create(INSTRUMENT_CLASSES_TASK, InstrumentClassesOfflineTask::class.java)
            val instrumentedJarTask = createAssembleInstrumentedJarTask(instrumentClassesTask.instrumentedClassesDir)
            artifacts.add(jacocoInstrumentedConf.name, instrumentedJarTask)

            TestTasksConfigurator(project, jacocoRuntimeConf).configure(
                instrumentClassesTask,
                instrumentedJarTask
            )
        }
    }

    private fun TaskContainer.createAssembleInstrumentedJarTask(instrumentedClassesDir: File): Jar {
        return create(ASSEMBLE_INSTRUMENTED_JAR_TASK, Jar::class.java).apply {
            description = "Assemble Jar with instrumented classes"
            dependsOn += INSTRUMENT_CLASSES_TASK

            from(instrumentedClassesDir)
            archiveBaseName.set("${project.name}-$INSTRUMENTED_JAR_SUFFIX")
        }
    }

    private fun Project.addConfigurationWithDependency(
        configurationName: String,
        jacocoDependency: JacocoDependency
    ): Configuration {
        val configuration: Configuration = configurations.create(configurationName)
        dependencies.add(
            configuration.name,
            jacocoDependency.buildDependency(dependencies)
        )
        return configuration
    }

    companion object {
        const val INSTRUMENT_CLASSES_TASK = "instrumentClassesOffline"

        const val ASSEMBLE_INSTRUMENTED_JAR_TASK = "assembleInstrumentedJar"
        const val INSTRUMENTED_JAR_SUFFIX = "instrumented"

        const val JACOCO_CONFIGURATION = "jacoco"
        const val JACOCO_RUNTIME_CONFIGURATION = "jacocoRuntime"
        const val JACOCO_INSTRUMENTED_CONFIGURATION = "jacocoInstrumented"
    }

}
