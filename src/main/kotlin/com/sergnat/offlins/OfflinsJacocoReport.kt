package com.sergnat.offlins

import com.sergnat.offlins.utils.orElseProvider
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.internal.CollectionCallbackActionDecorator
import org.gradle.api.internal.project.IsolatedAntBuilder
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.reporting.DirectoryReport
import org.gradle.api.reporting.SingleFileReport
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.jacoco.AntJacocoReport
import org.gradle.internal.jacoco.JacocoReportsContainerImpl
import org.gradle.internal.reflect.Instantiator
import java.nio.file.Path
import javax.inject.Inject

abstract class OfflinsJacocoReport : DefaultTask() {

    @get:Input
    abstract val execDataFile: Property<Path>

    @get:Input
    abstract val reportsExtension: Property<ReportsExtension>

    init {
        description = "Generates JaCoCo code coverage reports"
    }

    @TaskAction
    open fun generate() {
        val jacocoReport: JacocoReportsContainerImpl = getInstantiator().newInstance(
            JacocoReportsContainerImpl::class.java,
            this,
            CollectionCallbackActionDecorator.NOOP
        )

        val reportsDir: DirectoryProperty = project.objects.directoryProperty()
        reportsDir.set(
            project.buildDir.resolve(RELATIVE_REPORT_DIR).apply {
                mkdirs()
            }
        )

        configureHtmlReport(
            reportsDir,
            reportsExtension.map { it.html }.get(),
            jacocoReport.html
        )
        configureFileReport(
            reportsDir,
            reportsExtension.map { it.xml }.get(),
            jacocoReport.xml
        )
        configureFileReport(
            reportsDir,
            reportsExtension.map { it.csv }.get(),
            jacocoReport.csv
        )
        AntJacocoReport(getAntBuilder()).execute(
            project.configurations.getAt(OfflinsPlugin.JACOCO_CONFIGURATION),
            project.name,
            project.files(project.getMainSourceSetClassFilesDir()).filter { it.exists() },
            project.getMainSourceSetSources().filter { it.exists() },
            project.files(execDataFile).filter { it.exists() },
            jacocoReport
        )
    }

    private fun configureHtmlReport(
        baseReportDir: DirectoryProperty,
        offlinsCoverageReport: CoverageReport,
        jacocoReport: DirectoryReport
    ) {
        with(jacocoReport) {
            val reportEnabled = offlinsCoverageReport.enabled.orElseProvider(project.provider { true })
            when {
                project.gradleVersion >= GRADLE_6_1 -> {
                    required.set(reportEnabled)

                    val directoryProvider: Provider<Directory> = project.layout.dir(offlinsCoverageReport.location)
                        .orElse(baseReportDir.dir(name))
                    outputLocation.set(directoryProvider)
                }
                else -> {
                    isEnabled = reportEnabled.get()

                    setDestination(
                        offlinsCoverageReport.location
                            .orElseProvider(baseReportDir.file(name).map { it.asFile })
                    )
                }
            }

        }
    }

    private fun configureFileReport(
        baseReportDir: DirectoryProperty,
        offlinsCoverageReport: CoverageReport,
        jacocoReport: SingleFileReport
    ) {
        val reportEnabled = offlinsCoverageReport.enabled.orElseProvider(project.provider { false })
        val defaultReportFileName = "$name.${jacocoReport.name}"
        when {
            project.gradleVersion >= GRADLE_6_1 -> {
                jacocoReport.required.set(reportEnabled)

                val fileProvider: Provider<RegularFile> = project.layout.file(offlinsCoverageReport.location)
                    .orElse(baseReportDir.file(defaultReportFileName))
                jacocoReport.outputLocation.set(fileProvider)
            }
            else -> {
                jacocoReport.isEnabled = reportEnabled.get()

                jacocoReport.setDestination(
                    offlinsCoverageReport.location
                        .orElseProvider(baseReportDir.file(defaultReportFileName).map { it.asFile })
                )
            }
        }
    }

    @Inject
    protected abstract fun getAntBuilder(): IsolatedAntBuilder?

    @Inject
    protected abstract fun getInstantiator(): Instantiator

    companion object {
        const val RELATIVE_REPORT_DIR = "reports/jacoco"
        const val GENERATE_JACOCO_REPORTS_TASK = "coverageReport"
    }

}
