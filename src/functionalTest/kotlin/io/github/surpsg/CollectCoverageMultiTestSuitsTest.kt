package io.github.surpsg

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CollectCoverageMultiTestSuitsTest : BaseOfflinsTest() {

    companion object {
        const val TEST_PROJECT_RESOURCE_NAME = "multi-test-suits-project"
    }

    @BeforeEach
    fun setup() {
        initializeGradleTest()
    }

    @Test
    fun `offlins must collect coverage from default test tasks`() {
        // run
        gradleRunner.runTask("test")

        // assert
        rootProjectDir.assertModuleHasCoverageDataForClasses(
            "build/jacoco/test.exec",
            ClassCov("com/java/test/Class1", Covered.PARTIALLY)
        )
    }

    @Test
    fun `offlins must collect coverage from integration test tasks`() {
        // run
        gradleRunner.runTask("integrationTest")

        // assert
        rootProjectDir.assertModuleHasCoverageDataForClasses(
            "build/jacoco/integrationTest.exec",
            ClassCov("com/java/test/Class1", Covered.PARTIALLY)
        )
    }

    override fun resourceTestProject() = TEST_PROJECT_RESOURCE_NAME

}
