package io.github.surpsg.offlins

import io.github.surpsg.offlins.gradletest.applyPlugin
import io.github.surpsg.offlins.gradletest.newProject
import io.github.surpsg.offlins.gradletest.testJavaProject
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldNotBeNull
import org.gradle.api.artifacts.Dependency
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.JavaPlugin
import org.junit.jupiter.api.Test

class OfflinsPluginConfigurationTest {

    @Test
    fun `should add dependency with config jacoco instrumented on impl dependency project`() {
        // GIVEN
        val parentProj: ProjectInternal = testJavaProject(attachSettings = true) {
            val subProj = newProject {
                withName("sub1")
                withParent(this@testJavaProject)
            }
            val subSubProj = newProject {
                applyPlugin<JavaPlugin>()
                withName("sub2")
                withParent(subProj)
            }

            applyPlugin<OfflinsPlugin>()
            this.dependencies.add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, subSubProj)
        }

        // WHEN
        val jacocoInstrConf = parentProj.configurations.findByName(OfflinsPlugin.JACOCO_INSTRUMENTED_CONFIGURATION)

        // THEN
        val expectedDep: Dependency = parentProj.dependencies.project(
            mapOf(
                "path" to ":sub1:sub2",
                "configuration" to OfflinsPlugin.JACOCO_INSTRUMENTED_CONFIGURATION,
            )
        )
        jacocoInstrConf.shouldNotBeNull()
            .allDependencies
            .shouldContain(expectedDep)
    }
}
