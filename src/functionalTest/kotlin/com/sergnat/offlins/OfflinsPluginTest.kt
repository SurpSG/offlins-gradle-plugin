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
    fun `plugin must add jacoco configurations with dependencies`() {
        // setup
        val jacocoVersion = "0.8.8"
        val expectedDependencies = arrayOf(
            "jacoco:org.jacoco:org.jacoco.ant:${jacocoVersion}",
            "jacocoRuntime:org.jacoco:org.jacoco.agent:${jacocoVersion}"
        )

        buildFile.appendText("""
            project.configurations
                .forEach { config ->
                    config.dependencies.forEach { dep ->
                        def dependencyString = [config.name, dep.group, dep.name, dep.version].join(":")
                        println dependencyString
                    }
                }
        """.trimIndent()
        )

        // run // assert
        gradleRunner.runTask(OFFLINS_TASK)
            .assertOfflinsStatusEqualsTo(TaskOutcome.SUCCESS)
            .assertOutputContainsStrings(*expectedDependencies)
            .assertOutputContainsStrings("Currently, I do nothing")
    }

    override fun buildTestConfiguration() = TestConfiguration(
        TEST_PROJECT_RESOURCE_NAME,
        "build.gradle"
    )

}
