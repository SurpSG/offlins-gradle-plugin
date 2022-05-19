package com.sergnat.offlins

import org.gradle.testing.jacoco.tasks.JacocoReport
import java.nio.file.Path
import javax.inject.Inject

open class OfflinsJacocoReport @Inject constructor(
    execDataFile: Path
) : JacocoReport() {

    init {
        description = "Generates JaCoCo code coverage reports"

        jacocoClasspath = project.configurations.getAt(OfflinsPlugin.JACOCO_CONFIGURATION)

        when {
            project.gradleVersion >= GRADLE_5_1 -> {
                sourceDirectories.from(project.getMainSourceSetSources())
                classDirectories.from(project.getMainSourceSetClassFilesDir())
                executionData.from(execDataFile)
            }
            else -> {
                sourceSets(project.getMainSourceSet())
                executionData(execDataFile)
            }
        }

        reports.html.apply {
            val targetLocation = project.buildDir.resolve(RELATIVE_HTML_REPORT_LOCATIONS)
            when {
                project.gradleVersion >= GRADLE_6_1 -> {
                    required.set(true)
                    outputLocation.set(targetLocation)
                }

                else -> {
                    isEnabled = true
                    destination = targetLocation
                }
            }
        }
    }

    companion object {
        const val RELATIVE_HTML_REPORT_LOCATIONS = "reports/jacoco/html"
        const val GENERATE_JACOCO_REPORTS_TASK = "coverageReport"
    }

}
