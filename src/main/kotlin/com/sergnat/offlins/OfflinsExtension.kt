package com.sergnat.offlins

import org.gradle.api.Action
import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

open class OfflinsExtension @Inject constructor(
    objectFactory: ObjectFactory
) {

    val jacocoVersion: Property<String> = objectFactory.property(String::class.java)

    val report: ReportsExtension = ReportsExtension(objectFactory)

    fun reports(action: Action<ReportsExtension>) {
        action.execute(report)
    }

}

class ReportsExtension(
    objectFactory: ObjectFactory
) {
    val html: CoverageDirReport = CoverageDirReport(objectFactory)
    val xml: CoverageFileReport = CoverageFileReport(objectFactory)
    val csv: CoverageFileReport = CoverageFileReport(objectFactory)
}

abstract class AbstractReport<T : FileSystemLocation>(
    objectFactory: ObjectFactory
) {
    val enabled: Property<Boolean> = objectFactory.property(Boolean::class.javaObjectType)

    abstract val location: Property<T>
}

class CoverageFileReport(
    objectFactory: ObjectFactory
) : AbstractReport<RegularFile>(objectFactory) {

    override val location: Property<RegularFile> = objectFactory.fileProperty()

}

class CoverageDirReport(
    objectFactory: ObjectFactory
) : AbstractReport<Directory>(objectFactory) {

    override val location: Property<Directory> = objectFactory.directoryProperty()

}
