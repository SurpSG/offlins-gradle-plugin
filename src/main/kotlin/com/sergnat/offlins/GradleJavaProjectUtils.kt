package com.sergnat.offlins

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet


fun Project.getMainSourceSetClassFiles(): FileCollection {
    val classesDirectory: Provider<Directory> = getMainSourceSet().java.classesDirectory

    return project.fileTree(classesDirectory) {
        it.setIncludes(listOf("**/*.class"))
    }
}

fun Project.getMainSourceSetClassFilesDir(): Provider<Directory> {
    return getMainSourceSet().java.classesDirectory
}

fun Project.getMainSourceSetSources(): FileCollection {
    return getMainSourceSet().java.sourceDirectories
}

private fun Project.getMainSourceSet(): SourceSet {
    val javaExt: JavaPluginExtension = extensions.getByType(JavaPluginExtension::class.java)
    return javaExt.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
}
