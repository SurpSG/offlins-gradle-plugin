package com.sergnat.offlins

import com.sergnat.offlins.OfflinsJacocoReport.Companion.GENERATE_JACOCO_REPORTS_TASK
import com.sergnat.offlins.OfflinsJacocoReport.Companion.RELATIVE_REPORT_DIR
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File


class CoverageReportTaskTest : BaseOfflinsTest() {

    companion object {
        const val TEST_PROJECT_RESOURCE_NAME = "single-module-test-project"
    }

    @BeforeEach
    fun setup() {
        initializeGradleTest()
    }

    @ParameterizedTest
    @ValueSource(strings = ["4.10.3", "5.6.4", "6.9.1", "7.4.2"])
    fun `coverageReport task must generate html report`(gradleVersion: String) {
        val htmlLocation = "build/custom/jacocoReportDir"
        buildFile.appendText(
            """
            offlinsCoverage {
                reports {
                    csv.enabled = true
                    xml.enabled = true
                    html.enabled = true
                    html.location = project.file('$htmlLocation')
                }
            }
        """.trimIndent()
        )

        gradleRunner
            .withGradleVersion(gradleVersion)
            .withArguments("test", GENERATE_JACOCO_REPORTS_TASK, "-s")
            .build()
            .assertThatTaskStatusIs(GENERATE_JACOCO_REPORTS_TASK, TaskOutcome.SUCCESS)

        val reportDir: File = rootProjectDir.resolve("build").resolve(RELATIVE_REPORT_DIR)
        assertThat(rootProjectDir.resolve(htmlLocation))
            .isDirectory
            .isDirectoryRecursivelyContaining { it.name == "index.html" && it.isFile }
            .isDirectoryRecursivelyContaining { it.name == "com.java.test" && it.isDirectory }
            .isDirectoryRecursivelyContaining { it.name == "Class1.html" && it.isFile }
            .isDirectoryRecursivelyContaining { it.name == "Class1.java.html" && it.isFile }
        assertThat(reportDir.resolve("coverageReport.csv")).isFile.isNotEmpty
        assertThat(reportDir.resolve("coverageReport.xml")).isFile.isNotEmpty
    }

    override fun resourceTestProject() = TEST_PROJECT_RESOURCE_NAME

}
