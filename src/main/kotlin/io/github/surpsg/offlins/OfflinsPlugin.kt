package io.github.surpsg.offlins

import io.github.surpsg.offlins.InstrumentClassesOfflineTask.Companion.INSTRUMENT_CLASSES_TASK
import io.github.surpsg.offlins.InstrumentedJar.Companion.ASSEMBLE_INSTRUMENTED_JAR_TASK
import io.github.surpsg.offlins.OfflinsJacocoReport.Companion.GENERATE_JACOCO_REPORTS_TASK
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.logging.LogLevel
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import java.io.File

class OfflinsPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        if (gradleVersion < GRADLE_8_11) {
            throw IllegalStateException("Gradle ${gradle.gradleVersion} is not supported.")
        }

        val offlinsContext: OfflinsContext = createOfflinsConfigurations()
        setDependencyOnInstrumentedSubprojects()

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
            jacocoInstrumentedConfiguration = configurations.register(JACOCO_INSTRUMENTED_CONFIGURATION) {
                configurations.getByName(JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME).extendsFrom(it)
            }
        )

        return OfflinsContext(project, offlinsExtension, configurations)
    }

    private fun Project.setDependencyOnInstrumentedSubprojects() = afterEvaluate { project ->
        val onProjectDeps: Set<Dependency> = getOnProjectDependencies(project).asSequence()
            .map { onProjDep ->
                project.createOnProjectDependency(
                    onProjDep.name,
                    JACOCO_INSTRUMENTED_CONFIGURATION
                )
            }
            .toSet()

        onProjectDeps.forEach { dependency ->
            project.dependencies.add(JACOCO_INSTRUMENTED_CONFIGURATION, dependency)
        }
    }

    private fun Project.addConfigurationWithDependency(
        configurationName: String,
        jacocoDependency: Provider<JacocoDependency>
    ): NamedDomainObjectProvider<Configuration> {
        val configuration: NamedDomainObjectProvider<Configuration> = configurations.register(configurationName)
        dependencies.add(
            configuration.name,
            jacocoDependency.map { it.buildDependency(dependencies) }
        )
        log(msg = "Created configuration '$configurationName' in project '${project.name}'")
        return configuration
    }

    private fun createInstrumentedClassesTask(context: OfflinsContext) {
        val instrumentClassesTask: TaskProvider<InstrumentClassesOfflineTask> = context.project.tasks.register(
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
            val instrumentedJar = tasks.register(ASSEMBLE_INSTRUMENTED_JAR_TASK, InstrumentedJar::class.java) { jar ->
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
        context.project.tasks.register(
            GENERATE_JACOCO_REPORTS_TASK,
            OfflinsJacocoReport::class.java
        ) {
            it.project.log(msg = "Added task '${it.name}' to '${context.project.name}'")
            it.execDataFiles.from(context.execFiles)
            it.reportsExtension.set(context.offlinsExtension.report)
        }
    }

    private fun Project.log(logLevel: LogLevel = LogLevel.DEBUG, msg: String) {
        logger.log(logLevel, msg)
    }

    companion object {
        const val DEFAULT_JACOCO_VERSION = "0.8.12"

        const val OFFLINS_EXTENSION = "offlinsCoverage"

        const val JACOCO_CONFIGURATION = "jacoco"
        const val JACOCO_RUNTIME_CONFIGURATION = "jacocoRuntime"
        const val JACOCO_INSTRUMENTED_CONFIGURATION = "jacocoInstrumented"
    }

}
