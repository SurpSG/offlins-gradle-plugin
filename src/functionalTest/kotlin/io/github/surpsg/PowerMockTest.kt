package io.github.surpsg

import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class PowerMockTest : BaseOfflinsTest() {

    companion object {
        const val TEST_PROJECT_RESOURCE_NAME = "powermock-test-project"
    }

    @BeforeEach
    fun setup() {
        initializeGradleTest()
    }

    @Test
    fun `coverageReport task must generate html report`() {
        gradleRunner
            .runTask("test")
            .assertThatTaskStatusIs("test", TaskOutcome.SUCCESS)

        rootProjectDir.assertModuleHasCoverageDataForClasses(
            ClassCov("com/java/test/Class1", Covered.FULLY),
            ClassCov("com/java/test/StaticClass", Covered.PARTIALLY)
        )
    }

    override fun resourceTestProject() = TEST_PROJECT_RESOURCE_NAME

}
