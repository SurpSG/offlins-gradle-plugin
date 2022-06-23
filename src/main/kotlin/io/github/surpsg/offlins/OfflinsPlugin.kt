package io.github.surpsg.offlins

import io.github.surpsg.offlins.InstrumentClassesOfflineTask.Companion.INSTRUMENT_CLASSES_TASK
import io.github.surpsg.offlins.InstrumentedJar.Companion.ASSEMBLE_INSTRUMENTED_JAR_TASK
import io.github.surpsg.offlins.OfflinsJacocoReport.Companion.GENERATE_JACOCO_REPORTS_TASK
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME
import org.gradle.api.plugins.JavaPlugin.RUNTIME_ONLY_CONFIGURATION_NAME
import org.gradle.api.provider.Provider
import java.io.File

class OfflinsPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        if (project.gradleVersion < GRADLE_5_1) {
            throw IllegalStateException("Gradle ${project.gradle.gradleVersion} is not supported.")
        }

        // TODO add logs!
        val offlinsContext: OfflinsContext = createOfflinsConfigurations()

        createInstrumentedClassesTask(offlinsContext)
        createInstrumentedJarTask(offlinsContext)
        createCoverageReportTask(offlinsContext)

        TestTasksConfigurator(offlinsContext).configure()
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
        return configuration
    }

    private fun createInstrumentedClassesTask(offlinsContext: OfflinsContext) {
        // TODO use .register(...) instead of .create(...) ?
        val instrumentClassesTask: InstrumentClassesOfflineTask = offlinsContext.project.tasks.create(
            INSTRUMENT_CLASSES_TASK,
            InstrumentClassesOfflineTask::class.java
        )
        offlinsContext.instrumentedClassesTask.set(instrumentClassesTask)
    }

    private fun createInstrumentedJarTask(offlinsContext: OfflinsContext) {
        val instrumentedClassesDir: Provider<File> = offlinsContext.instrumentedClassesTask.map {
            it.instrumentedClassesDir
        }
        with(offlinsContext.project) {
            // TODO use .register(...) instead of .create(...) ?
            val instrumentedJar = tasks.create(ASSEMBLE_INSTRUMENTED_JAR_TASK, InstrumentedJar::class.java) { jar ->
                jar.dependsOn += INSTRUMENT_CLASSES_TASK
                jar.from(instrumentedClassesDir)
            }
            artifacts.add(
                offlinsContext.offlinsConfigurations.jacocoInstrumentedConfiguration.name,
                instrumentedJar
            )
            offlinsContext.instrumentedJar.set(instrumentedJar)
        }
    }

    private fun createCoverageReportTask(offlinsContext: OfflinsContext) {
        offlinsContext.project.tasks.create( // TODO use .register(...) instead of .create(...) ?
            GENERATE_JACOCO_REPORTS_TASK,
            OfflinsJacocoReport::class.java
        ) {
            it.execDataFiles.addAll(offlinsContext.execFiles)
            it.reportsExtension.set(offlinsContext.project.provider {
                offlinsContext.offlinsExtension.report
            })
        }
    }

    companion object {
        const val DEFAULT_JACOCO_VERSION = "0.8.8"

        const val OFFLINS_EXTENSION = "offlinsCoverage"

        const val JACOCO_CONFIGURATION = "jacoco"
        const val JACOCO_RUNTIME_CONFIGURATION = "jacocoRuntime"
        const val JACOCO_INSTRUMENTED_CONFIGURATION = "jacocoInstrumented"
    }

}
