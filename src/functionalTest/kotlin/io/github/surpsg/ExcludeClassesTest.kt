package io.github.surpsg

import io.github.surpsg.offlins.OfflinsJacocoReport.Companion.GENERATE_JACOCO_REPORTS_TASK
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.file.shouldBeAFile
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.string.shouldNotContain
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ExcludeClassesTest : BaseOfflinsTest() {

    companion object {
        const val TEST_PROJECT_RESOURCE_NAME = "single-module-test-project"
    }

    @BeforeEach
    fun setup() {
        initializeGradleTest()
    }

    @Test
    fun `exclude classes config must exclude enumerated classes from report`() {
        // GIVEN
        buildFile.appendText(
            """
            offlinsCoverage {
                reports {
                    csv.enabled.set true
                    excludeClasses.set(['**/test/Class1*'])
                }
            }
        """.trimIndent()
        )

        // WHEN
        gradleRunner
            .withArguments("test", GENERATE_JACOCO_REPORTS_TASK, "-s")
            .build()
            .apply {
                println(output)
            }
            .assertThatTaskStatusIs(GENERATE_JACOCO_REPORTS_TASK, TaskOutcome.SUCCESS)

        // THEN
        assertSoftly(rootProjectDir.resolve("build/reports/jacoco/").resolve("report.csv")) {
            shouldExist()
            shouldBeAFile()
            readText().shouldNotContain("Class1")
        }
    }

    override fun resourceTestProject() = TEST_PROJECT_RESOURCE_NAME
}
