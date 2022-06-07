package io.github.surpsg

import io.github.surpsg.offlins.OfflinsJacocoReport.Companion.GENERATE_JACOCO_REPORTS_TASK
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource


class CoverageReportTaskTest : BaseOfflinsTest() {

    companion object {
        const val TEST_PROJECT_RESOURCE_NAME = "single-module-test-project"
    }

    @BeforeEach
    fun setup() {
        initializeGradleTest()
    }

    @ParameterizedTest
    @ValueSource(
        strings = ["5.1", "5.6.4", "6.9.1", "7.4.2"]
    )
    fun `coverageReport task must generate html report`(gradleVersion: String) {
        val htmlLocation = "build/custom/jacocoReportDir"
        val csvLocation = "build/custom/custom_csv.csv"
        val xmlLocation = "build/custom/custom_xml.xml"
        buildFile.appendText(
            """
            offlinsCoverage {
                reports {
                    csv.enabled.set true
                    csv.location.set project.file('$csvLocation')
                    
                    xml.enabled.set true
                    xml.location.set project.file('$xmlLocation')
                    
                    html.enabled.set true
                    html.location.set project.file('$htmlLocation')
                }
            }
        """.trimIndent()
        )

        gradleRunner
            .withGradleVersion(gradleVersion)
            .withArguments("test", GENERATE_JACOCO_REPORTS_TASK, "-s")
            .build()
            .assertThatTaskStatusIs(GENERATE_JACOCO_REPORTS_TASK, TaskOutcome.SUCCESS)

        assertThat(rootProjectDir.resolve(htmlLocation))
            .isDirectory
            .isDirectoryRecursivelyContaining { it.name == "index.html" && it.isFile }
            .isDirectoryRecursivelyContaining { it.name == "com.java.test" && it.isDirectory }
            .isDirectoryRecursivelyContaining { it.name == "Class1.html" && it.isFile }
            .isDirectoryRecursivelyContaining { it.name == "Class1.java.html" && it.isFile }
        assertThat(rootProjectDir.resolve(csvLocation)).isFile.isNotEmpty
        assertThat(rootProjectDir.resolve(xmlLocation)).isFile.isNotEmpty
    }

    override fun resourceTestProject() = TEST_PROJECT_RESOURCE_NAME

}
