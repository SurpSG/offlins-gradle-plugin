package com.sergnat.offlins

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class OfflinsPluginMultiModuleTest : BaseOfflinsTest() {

    companion object {
        const val TEST_PROJECT_RESOURCE_NAME = "multi-module-proj"
    }

    @BeforeEach
    fun setup() {
        initializeGradleTest()
    }

    @ParameterizedTest
    @ValueSource(strings = ["7.4.2", "6.9.1"])
    fun `test task must collect coverage data`(gradleVersion: String) {
        // run
        gradleRunner
            .withGradleVersion(gradleVersion)
            .runTask("test")

        // assert
        sequenceOf(
            "mod1-dep-on-mod2-and-mod3" to arrayOf(
                ClassCov("com/test/mod1/Module1", Covered.PARTIALLY),
                ClassCov("com/test/mod2/Module2", Covered.PARTIALLY),
                ClassCov("com/test/mod3/Module3", Covered.PARTIALLY),
            ),
            "mod2-dep-on-mod3" to arrayOf(
                ClassCov("com/test/mod2/Module2", Covered.FULLY),
                ClassCov("com/test/mod3/Module3", Covered.PARTIALLY)
            ),
            "mod3" to arrayOf(
                ClassCov("com/test/mod3/Module3", Covered.PARTIALLY)
            )
        ).forEach {
            rootProjectDir.resolve(it.first)
                .assertModuleHasCoverageDataForClasses(*it.second)
        }
    }

    override fun buildTestConfiguration() = TestConfiguration(
        TEST_PROJECT_RESOURCE_NAME,
        "build.gradle"
    )

}
