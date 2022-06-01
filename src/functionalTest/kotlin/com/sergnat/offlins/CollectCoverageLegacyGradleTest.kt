package com.sergnat.offlins

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource


class CollectCoverageLegacyGradleTest : BaseOfflinsTest() {

    companion object {
        const val TEST_PROJECT_RESOURCE_NAME = "multi-module-compile-deps"
    }

    @BeforeEach
    fun setup() {
        initializeGradleTest()
    }

    @ParameterizedTest
    @ValueSource(strings = ["5.1", "5.6.4", "6.9.1"])
    fun `test task must collect coverage data`(gradleVersion: String) {
        // run
        gradleRunner
            .withGradleVersion(gradleVersion)
            .runTask("test")

        // assert
        sequenceOf(
            "mod1-dep-on-mod2" to arrayOf(
                ClassCov("com/test/mod1/Module1", Covered.PARTIALLY),
                ClassCov("com/test/mod2/Module2", Covered.PARTIALLY)
            ),
            "mod2" to arrayOf(
                ClassCov("com/test/mod2/Module2", Covered.FULLY)
            )
        ).forEach {
            rootProjectDir.resolve(it.first)
                .assertModuleHasCoverageDataForClasses(*it.second)
        }
    }

    override fun resourceTestProject() = TEST_PROJECT_RESOURCE_NAME

}
