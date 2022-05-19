package com.sergnat.offlins

import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.Provider

fun jacocoAntDependency(jacocoVersion: Provider<String>): Provider<JacocoDependency> {
    return jacocoDep("org.jacoco.ant", jacocoVersion, "nodeps")
}

fun jacocoAgentDependency(jacocoVersion: Provider<String>): Provider<JacocoDependency> {
    return jacocoDep("org.jacoco.agent", jacocoVersion, "runtime")
}

private fun jacocoDep(
    name: String,
    version: Provider<String>,
    classifier: String
): Provider<JacocoDependency> {
    return version.map { JacocoDependency(name, it, classifier) }
}

class JacocoDependency(
    private val name: String,
    private val version: String,
    private val classifier: String
) {

    private val group: String = "org.jacoco"

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
