package io.github.surpsg.offlins

import io.github.surpsg.offlins.sources.filter.SourceFilter
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.project.IsolatedAntBuilder
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.Describables
import org.gradle.internal.jacoco.AntJacocoReport
import org.gradle.internal.jacoco.JacocoReportsContainerImpl
import org.gradle.internal.reflect.Instantiator
import org.gradle.testing.jacoco.tasks.JacocoReportsContainer
import javax.inject.Inject

open class OfflinsJacocoReport : DefaultTask() {

    @get:InputFiles
    val execDataFiles: ConfigurableFileCollection = project.files()

    @Internal
    val reportsExtension: Property<ReportsExtension> = project.objects.property(ReportsExtension::class.java)

    @get:OutputDirectory
    val htmlReport: DirectoryProperty = project.objects.directoryProperty().convention(
        reportsExtension.flatMap { it.html.location }
    )

    @get:OutputFile
    val csvReport: RegularFileProperty = project.objects.fileProperty().convention(
        reportsExtension.flatMap { it.csv.location }
    )

    @get:OutputFile
    val xmlReport: RegularFileProperty = project.objects.fileProperty().convention(
        reportsExtension.flatMap { it.xml.location }
    )

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
        sequenceOf(
            "HTML" to ReportsExtension::html,
            "XML" to ReportsExtension::xml,
            "CSV" to ReportsExtension::csv,
        ).forEach { (type, reportGetter) ->
            reportsExtension.logReportConfiguration(type, reportGetter)
        }

        configureReports()

        AntJacocoReport(getAntBuilder()).execute(
            project.configurations.getAt(OfflinsPlugin.JACOCO_CONFIGURATION),
            project.name,
            buildFilteredClassesCollection(),
            project.getMainSourceSetSources().filter { it.exists() },
            null,
            execDataFiles.filter { it.exists() },
            reports
        )
    }

    private fun configureReports() {
        val sourceReports = reportsExtension.get()
        with(reports.csv) {
            required.set(sourceReports.csv.enabled)
            outputLocation.set(sourceReports.csv.location)
        }
        with(reports.xml) {
            required.set(sourceReports.xml.enabled)
            outputLocation.set(sourceReports.xml.location)
        }
        with(reports.html) {
            required.set(sourceReports.html.enabled)
            outputLocation.set(sourceReports.html.location)
        }
    }

    private fun Property<ReportsExtension>.logReportConfiguration(
        type: String,
        reportGetter: ReportsExtension.() -> AbstractReport<*>,
    ) = with(get().reportGetter()) {
        logger.info(
            "[{}] Report enabled={}, location={}",
            type,
            enabled.get(),
            location.get().asFile,
        )
    }

    private fun buildFilteredClassesCollection(): FileCollection {
        val classesDir: Provider<Directory> = project.getMainSourceSetClassFilesDir()
        val existingClasses: FileCollection = project.files(classesDir).filter { it.exists() }
        return SourceFilter.build(reportsExtension.get())
            .filter(existingClasses)
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
        const val GENERATE_JACOCO_REPORTS_TASK = "coverageReport"
    }

}
