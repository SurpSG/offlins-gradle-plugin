package io.github.surpsg.offlins

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Nested
import javax.inject.Inject

open class OfflinsExtension @Inject constructor(
    objectFactory: ObjectFactory
) {

    val jacocoVersion: Property<String> = objectFactory.property(String::class.java)

    val report: ReportsExtension = objectFactory.newInstance(ReportsExtension::class.java)

    fun reports(action: Action<ReportsExtension>) {
        action.execute(report)
    }
}

open class ReportsExtension @Inject constructor(
    objectFactory: ObjectFactory,
) {
    @Nested
    val html: CoverageDirReport = objectFactory.newInstance(CoverageDirReport::class.java, "html")

    @Nested
    val xml: CoverageFileReport = objectFactory.newInstance(CoverageFileReport::class.java, "report.xml")

    @Nested
    val csv: CoverageFileReport = objectFactory.newInstance(CoverageFileReport::class.java, "report.csv")

    @Input
    val excludeClasses: ListProperty<String> = objectFactory
        .listProperty(String::class.javaObjectType)
        .convention(emptyList())
}

abstract class AbstractReport<T : FileSystemLocation>(
    objectFactory: ObjectFactory
) {
    @Input
    val enabled: Property<Boolean> = objectFactory.property(Boolean::class.javaObjectType)
        .convention(false)

    @get:InputFile
    abstract val location: Property<T>

    internal companion object {
        const val DEFAULT_PATH = "reports/jacoco"
    }
}

open class CoverageFileReport @Inject constructor(
    project: Project,
    defaultName: String,
) : AbstractReport<RegularFile>(project.objects) {

    override val location: Property<RegularFile> = project.objects.fileProperty().convention(
        project.layout.buildDirectory.file("$DEFAULT_PATH/$defaultName")
    )
}

open class CoverageDirReport @Inject constructor(
    project: Project,
    defaultName: String,
) : AbstractReport<Directory>(project.objects) {

    override val location: Property<Directory> = project.objects.directoryProperty().convention(
        project.layout.buildDirectory.dir("$DEFAULT_PATH/$defaultName")
    )
}
