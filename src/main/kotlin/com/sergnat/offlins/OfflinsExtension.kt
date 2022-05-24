package com.sergnat.offlins

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import java.io.File
import javax.inject.Inject

abstract class OfflinsExtension @Inject constructor(
    objectFactory: ObjectFactory
) {

    abstract val jacocoVersion: Property<String>

    val report: ReportsExtension

    init {
        report = objectFactory.newInstance(
            ReportsExtension::class.java,
            objectFactory.newInstance(CoverageReport::class.java),
            objectFactory.newInstance(CoverageReport::class.java),
            objectFactory.newInstance(CoverageReport::class.java)
        )
    }

    fun reports(action: Action<ReportsExtension>) {
        action.execute(report)
    }

}

abstract class ReportsExtension @Inject constructor(
    val html: CoverageReport,
    val xml: CoverageReport,
    val csv: CoverageReport
)

abstract class CoverageReport {
    abstract val enabled: Property<Boolean>
    abstract val location: Property<File>
}
