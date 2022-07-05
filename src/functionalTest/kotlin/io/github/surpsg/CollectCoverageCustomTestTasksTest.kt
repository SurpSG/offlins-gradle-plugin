package io.github.surpsg

import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CollectCoverageCustomTestTasksTest : BaseOfflinsTest() {

    companion object {
        const val TEST_PROJECT_RESOURCE_NAME = "custom-test-tasks-with-filters-project"
    }

    @BeforeEach
    fun setup() {
        initializeGradleTest()
    }

    @Test
    fun `offlins must collect coverage from custom test tasks`() {
        // run
        gradleRunner.withArguments("intTest", "unitTest")
            .build()
            .assertThatTaskStatusIs("unitTest", TaskOutcome.SUCCESS)
            .assertThatTaskStatusIs("intTest", TaskOutcome.SUCCESS)

        // assert
        rootProjectDir.assertModuleHasCoverageDataForClasses(
            "build/jacoco/unitTest.exec",
            ClassCov("com/java/test/Class1", Covered.PARTIALLY)
        )
        rootProjectDir.assertModuleHasCoverageDataForClasses(
            "build/jacoco/intTest.exec",
            ClassCov("com/java/test/Class1", Covered.PARTIALLY)
        )
    }

    override fun resourceTestProject() = TEST_PROJECT_RESOURCE_NAME

}
