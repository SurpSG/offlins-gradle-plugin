package com.sergnat.offlins

import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler

const val DEFAULT_JACOCO_VERSION = "0.8.8"

val JACOCO_ANT = JacocoDependency(
    "org.jacoco",
    "org.jacoco.ant",
    DEFAULT_JACOCO_VERSION,
    "nodeps"
)

val JACOCO_AGENT = JacocoDependency(
    "org.jacoco",
    "org.jacoco.agent",
    DEFAULT_JACOCO_VERSION,
    "runtime"
)

data class JacocoDependency(
    private val group: String,
    private val name: String,
    private val version: String,
    private val classifier: String
) {

    fun buildDependency(dependencyHandler: DependencyHandler): ExternalModuleDependency {
        val dependencyProperties = mapOf(
            "group" to group,
            "name" to name,
            "version" to version,
            "classifier" to classifier
        )
        return dependencyHandler.create(dependencyProperties) as ExternalModuleDependency
    }

}
