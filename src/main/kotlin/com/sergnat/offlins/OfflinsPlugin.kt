package com.sergnat.offlins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

class OfflinsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.addConfigurationWithDependency("jacoco", JACOCO_ANT)
        project.addConfigurationWithDependency("jacocoRuntime", JACOCO_AGENT)

        project.tasks.create(OFFLINS_TASK).doLast {
            println("Currently, I do nothing")
        }
    }

    private fun Project.addConfigurationWithDependency(
        configurationName: String,
        jacocoDependency: JacocoDependency
    ) {
        val jacocoRuntimeConfiguration: Configuration = configurations.create(configurationName)
        dependencies.add(
            jacocoRuntimeConfiguration.name,
            jacocoDependency.buildDependency(dependencies)
        )
    }

    companion object {
        const val OFFLINS_TASK = "jacocoReport"
    }

}
