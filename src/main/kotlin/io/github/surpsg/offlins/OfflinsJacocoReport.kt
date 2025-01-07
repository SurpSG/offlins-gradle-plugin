package io.github.surpsg.offlins

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.internal.project.IsolatedAntBuilder
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.reporting.DirectoryReport
import org.gradle.api.reporting.SingleFileReport
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.Describables
import org.gradle.internal.jacoco.AntJacocoReport
import org.gradle.internal.jacoco.JacocoReportsContainerImpl
import org.gradle.internal.reflect.Instantiator
import org.gradle.testing.jacoco.tasks.JacocoReportsContainer
import java.nio.file.Path
import javax.inject.Inject

open class OfflinsJacocoReport : DefaultTask() {

    @get:Input
    val execDataFiles: ListProperty<Path> = project.objects.listProperty(Path::class.java)

    @get:Input
    val reportsExtension: Property<ReportsExtension> = project.objects.property(ReportsExtension::class.java)

    private val reports: JacocoReportsContainer

    init {
        group = "verification"
        description = "Generates JaCoCo code coverage reports"

        reports = project.objects.newInstance(
            JacocoReportsContainerImpl::class.java,
            Describables.quoted("Task", identityPath),
        )
    }

    @TaskAction
    open fun generate() {
        val reportsDir: DirectoryProperty = project.objects.directoryProperty()
        reportsDir.set(
            project.buildDir.resolve(RELATIVE_REPORT_DIR).apply {
                mkdirs()
            }
        )

        configureHtmlReport(
            reportsDir,
            reportsExtension.map { it.html }.get(),
            reports.html
        )
        configureFileReport(
            reportsDir,
            reportsExtension.map { it.xml }.get(),
            reports.xml
        )
        configureFileReport(
            reportsDir,
            reportsExtension.map { it.csv }.get(),
            reports.csv
        )
        AntJacocoReport(getAntBuilder()).execute(
            project.configurations.getAt(OfflinsPlugin.JACOCO_CONFIGURATION),
            project.name,
            project.files(project.getMainSourceSetClassFilesDir()).filter { it.exists() },
            project.getMainSourceSetSources().filter { it.exists() },
            null,
            project.files(execDataFiles).filter { it.exists() },
            reports
        )
    }

    private fun configureHtmlReport(
        baseReportDir: DirectoryProperty,
        offlinsCoverageReport: CoverageDirReport,
        jacocoReport: DirectoryReport
    ) {
        with(jacocoReport) {
            val reportEnabled: Property<Boolean> = offlinsCoverageReport.enabled.convention(true)
            val location: Property<Directory> = offlinsCoverageReport.location.convention(baseReportDir.dir(name))
            required.set(reportEnabled)
            outputLocation.set(location)
        }
    }

    private fun configureFileReport(
        baseReportDir: DirectoryProperty,
        offlinsCoverageFileReport: CoverageFileReport,
        jacocoReport: SingleFileReport
    ) {
        val reportEnabled: Property<Boolean> = offlinsCoverageFileReport.enabled.convention(false)
        val defaultReportFileName = "$name.${jacocoReport.name}"
        val reportLocation: Property<RegularFile> = offlinsCoverageFileReport.location
            .convention(baseReportDir.file(defaultReportFileName))
        jacocoReport.required.set(reportEnabled)
        jacocoReport.outputLocation.set(reportLocation)
    }

    @Inject
    protected open fun getAntBuilder(): IsolatedAntBuilder {
        throw UnsupportedOperationException("Expected not to be invoked")
    }

    @Inject
    protected open fun getInstantiator(): Instantiator {
        throw UnsupportedOperationException("Expected not to be invoked")
    }

    companion object {
        const val RELATIVE_REPORT_DIR = "reports/jacoco"
        const val GENERATE_JACOCO_REPORTS_TASK = "coverageReport"
    }

}
