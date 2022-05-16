package com.sergnat.offlins

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.plugins.JavaPlugin

fun Project.createOnProjectDependency(
    projectName: String,
    dependencyConfiguration: String
): Dependency {
    return dependencies.project(
        mapOf(
            "path" to ":$projectName",
            "configuration" to dependencyConfiguration
        )
    )
}

fun recursiveOnProjectDependencies(project: Project): Set<Project> {
    val onProjectDependencies: MutableSet<Project> = mutableSetOf()
    val projectsToScan = ArrayDeque(listOf(project))
    while (projectsToScan.isNotEmpty()) {
        val proj: Project = projectsToScan.removeFirst()
        getOnProjectDependencies(proj)
            .map { it.dependencyProject }
            .forEach { dependencyProject ->
                val notScannedBefore: Boolean = onProjectDependencies.add(dependencyProject)
                if (notScannedBefore) {
                    projectsToScan.addLast(dependencyProject)
                }
            }
    }
    return onProjectDependencies
}

fun getOnProjectDependencies(project: Project): Set<ProjectDependency> {
    return project.configurations.names.asSequence()
        // TODO support older versions: it == COMPILE_CONFIG }
        .filter { it == JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME }
        .map { project.configurations.getByName(it) }
        .flatMap { it.dependencies.withType(ProjectDependency::class.java) }
        .toSet()
}
