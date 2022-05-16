package com.sergnat.offlins

import com.sergnat.offlins.TestTasksConfigurator.Companion.DEFAULT_RELATIVE_JACOCO_EXEC_LOCATION
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.nio.file.Paths

open class OfflinsJacocoReport : JacocoReport() {

    init {
        description = "Generates JaCoCo code coverage reports"

        jacocoClasspath = project.configurations.getAt(OfflinsPlugin.JACOCO_CONFIGURATION)

        sourceDirectories.from(project.getMainSourceSetSources())
        classDirectories.from(project.getMainSourceSetClassFilesDir())
        executionData.from(Paths.get(project.buildDir.path, DEFAULT_RELATIVE_JACOCO_EXEC_LOCATION))

        reports.html.apply {
            val targetLocation = project.buildDir.resolve(RELATIVE_HTML_REPORT_LOCATIONS)
            when {
                GradleVersion(project.gradle.gradleVersion) >= GRADLE_6_1 -> {
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
    }

}
