package io.github.surpsg.offlins

import io.github.surpsg.offlins.InstrumentClassesOfflineTask.Companion.INSTRUMENT_CLASSES_TASK
import io.github.surpsg.offlins.InstrumentedJar.Companion.ASSEMBLE_INSTRUMENTED_JAR_TASK
import io.github.surpsg.offlins.OfflinsJacocoReport.Companion.GENERATE_JACOCO_REPORTS_TASK
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.logging.LogLevel
import org.gradle.api.plugins.JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME
import org.gradle.api.plugins.JavaPlugin.RUNTIME_ONLY_CONFIGURATION_NAME
import org.gradle.api.provider.Provider
import java.io.File

class OfflinsPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        if (project.gradleVersion < GRADLE_5_1) {
            throw IllegalStateException("Gradle ${project.gradle.gradleVersion} is not supported.")
        }

        val offlinsContext: OfflinsContext = createOfflinsConfigurations()

        createInstrumentedClassesTask(offlinsContext)
        createInstrumentedJarTask(offlinsContext)
        createCoverageReportTask(offlinsContext)

        TestTasksConfigurator().configure(offlinsContext)
    }

    private fun Project.createOfflinsConfigurations(): OfflinsContext {
        val offlinsExtension = extensions.create(OFFLINS_EXTENSION, OfflinsExtension::class.java, objects)
        val jacocoVersion: Provider<String> = offlinsExtension.jacocoVersion.convention(DEFAULT_JACOCO_VERSION)

        val configurations = OfflinsConfigurations(
            jacocoConfiguration = addConfigurationWithDependency(
                JACOCO_CONFIGURATION,
                jacocoAntDependency(jacocoVersion)
            ),
            jacocoRuntimeConfiguration = addConfigurationWithDependency(
                JACOCO_RUNTIME_CONFIGURATION,
                jacocoAgentDependency(jacocoVersion)
            ),
            jacocoInstrumentedConfiguration = configurations.create(JACOCO_INSTRUMENTED_CONFIGURATION) {
                log(msg = "Created configuration '$JACOCO_INSTRUMENTED_CONFIGURATION' in project '${project.name}'")
                it.isCanBeConsumed = true
                it.isCanBeResolved = false
                it.extendsFrom(
                    configurations.getByName(IMPLEMENTATION_CONFIGURATION_NAME),
                    configurations.getByName(RUNTIME_ONLY_CONFIGURATION_NAME)
                )
            }
        )

        return OfflinsContext(project, offlinsExtension, configurations)
    }

    private fun Project.addConfigurationWithDependency(
        configurationName: String,
        jacocoDependency: Provider<JacocoDependency>
    ): Configuration {
        // TODO .register instead of .create ?
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
        log(msg = "Created configuration '$configurationName' in project '${project.name}'")
        return configuration
    }

    private fun createInstrumentedClassesTask(context: OfflinsContext) {
        // TODO use .register(...) instead of .create(...) ?
        val instrumentClassesTask: InstrumentClassesOfflineTask = context.project.tasks.create(
            INSTRUMENT_CLASSES_TASK,
            InstrumentClassesOfflineTask::class.java
        )
        context.instrumentedClassesTask.set(instrumentClassesTask)
        context.project.log(msg = "Added task '${instrumentClassesTask.name}' to '${context.project.name}'")
    }

    private fun createInstrumentedJarTask(context: OfflinsContext) {
        val instrumentedClassesDir: Provider<File> = context.instrumentedClassesTask.map {
            it.instrumentedClassesDir
        }
        with(context.project) {
            // TODO use .register(...) instead of .create(...) ?
            val instrumentedJar = tasks.create(ASSEMBLE_INSTRUMENTED_JAR_TASK, InstrumentedJar::class.java) { jar ->
                jar.dependsOn += INSTRUMENT_CLASSES_TASK
                jar.from(instrumentedClassesDir)
            }
            artifacts.add(
                context.offlinsConfigurations.jacocoInstrumentedConfiguration.name,
                instrumentedJar
            )
            context.instrumentedJar.set(instrumentedJar)
            log(msg = "Added task '${instrumentedJar.name}' to '${context.project.name}'")
        }
    }

    private fun createCoverageReportTask(context: OfflinsContext) {
        context.project.tasks.create( // TODO use .register(...) instead of .create(...) ?
            GENERATE_JACOCO_REPORTS_TASK,
            OfflinsJacocoReport::class.java
        ) {
            it.project.log(msg = "Added task '${it.name}' to '${context.project.name}'")
            it.execDataFiles.addAll(context.execFiles)
            it.reportsExtension.set(context.project.provider {
                context.offlinsExtension.report
            })
        }
    }

    private fun Project.log(logLevel: LogLevel = LogLevel.DEBUG, msg: String) {
        println("======================")
        println(msg)
        logger.log(logLevel, msg)
    }

    companion object {
        const val DEFAULT_JACOCO_VERSION = "0.8.8"

        const val OFFLINS_EXTENSION = "offlinsCoverage"

        const val JACOCO_CONFIGURATION = "jacoco"
        const val JACOCO_RUNTIME_CONFIGURATION = "jacocoRuntime"
        const val JACOCO_INSTRUMENTED_CONFIGURATION = "jacocoInstrumented"
    }

}
