package io.github.surpsg

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
    fun `offlins must collect coverage from custom unit test task`() {
        // run
        gradleRunner.runTask("unitTest")

        // assert
        rootProjectDir.assertModuleHasCoverageDataForClasses(
            "mod1/build/jacoco/unitTest.exec",
            ClassCov("com/java/test/Class1", Covered.FULLY),
            ClassCov("com/test/mod2/Module2", Covered.FULLY),
            ClassCov("com/test/mod3/Module3", Covered.FULLY)
        )
    }

    @Test
    fun `offlins must collect coverage from custom integration test task`() {
        // run
        gradleRunner.runTask("intTest")

        // assert
        rootProjectDir.assertModuleHasCoverageDataForClasses(
            "mod1/build/jacoco/intTest.exec",
            ClassCov("com/java/test/Class1", Covered.PARTIALLY),
            ClassCov("com/test/mod2/Module2", Covered.FULLY),
            ClassCov("com/test/mod3/Module3", Covered.FULLY)
        )
    }

    override fun resourceTestProject() = TEST_PROJECT_RESOURCE_NAME

}
