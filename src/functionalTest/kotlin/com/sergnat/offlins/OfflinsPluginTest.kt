package com.sergnat.offlins

import com.sergnat.offlins.OfflinsPlugin.Companion.OFFLINS_TASK
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OfflinsPluginTest : BaseOfflinsTest() {

    companion object {
        const val TEST_PROJECT_RESOURCE_NAME = "single-module-test-project"
    }

    @BeforeEach
    fun setup() {
        initializeGradleTest()
    }

    @Test
    fun `verify plugin works`() {
        // setup
        buildFile.appendText(
            """
                println 'hello'
        """.trimIndent()
        )

        // run // assert
        gradleRunner.runTask(OFFLINS_TASK)
            .assertOfflinsStatusEqualsTo(TaskOutcome.SUCCESS)
            .assertOutputContainsStrings("Currently, I do nothing")
    }

    override fun buildTestConfiguration() = TestConfiguration(
        TEST_PROJECT_RESOURCE_NAME,
        "build.gradle"
    )

}
