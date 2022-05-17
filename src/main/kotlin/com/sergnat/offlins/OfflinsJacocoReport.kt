package com.sergnat.offlins

import com.sergnat.offlins.TestTasksConfigurator.Companion.DEFAULT_RELATIVE_JACOCO_EXEC_LOCATION
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.nio.file.Paths

open class OfflinsJacocoReport : JacocoReport() {

    init {
        description = "Generates JaCoCo code coverage reports"

        jacocoClasspath = project.configurations.getAt(OfflinsPlugin.JACOCO_CONFIGURATION)

        val execDataFile = Paths.get(project.buildDir.path, DEFAULT_RELATIVE_JACOCO_EXEC_LOCATION)
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
    }

}
