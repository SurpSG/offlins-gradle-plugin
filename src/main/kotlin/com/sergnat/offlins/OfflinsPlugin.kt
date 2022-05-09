package com.sergnat.offlins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler

class OfflinsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.create(OFFLINS_TASK).doLast {
            println("Currently, I do nothing")
        }
        val jacocoConfiguration: Configuration = project.configurations.create("jacoco")
        project.dependencies.add(
            jacocoConfiguration.name,
            project.dependencies.buildDependency(
                "org.jacoco",
                "org.jacoco.ant",
                "0.8.7",
                "nodeps"
            )
        )
        val jacocoRuntimeConfiguration: Configuration = project.configurations.create("jacocoRuntime")
        project.dependencies.add(
            jacocoRuntimeConfiguration.name,
            project.dependencies.buildDependency(
                "org.jacoco",
                "org.jacoco.agent",
                "0.8.7",
                "runtime"
            )
        )
    }

    private fun DependencyHandler.buildDependency(
        group: String,
        name: String,
        version: String,
        classifier: String,
    ): ExternalModuleDependency = create(
        mapOf(
            "group" to group,
            "name" to name,
            "version" to version,
            "classifier" to classifier
        )
    ) as ExternalModuleDependency

    companion object {
        const val OFFLINS_TASK = "jacocoReport"
    }

}
