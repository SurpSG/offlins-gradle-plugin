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
        val group = "\${dep.group}"
        val name = "\${dep.name}"
        buildFile.appendText(
            """
                println 'hello'
                println("configurations:")
        project.configurations.forEach {
            println(it.name)
            it.dependencies.forEach { dep ->
                println("\tdepName: ${group}:${name}")
            }
        }
        """.trimIndent()
        )

        // run // assert
        gradleRunner.withArguments(OFFLINS_TASK, "-i").build()
            .apply {
                println(output)
            }
            .assertOfflinsStatusEqualsTo(TaskOutcome.SUCCESS)
            .assertOutputContainsStrings("Currently, I do nothing")
    }

    override fun buildTestConfiguration() = TestConfiguration(
        TEST_PROJECT_RESOURCE_NAME,
        "build.gradle"
    )

}
