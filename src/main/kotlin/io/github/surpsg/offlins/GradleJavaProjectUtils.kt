package io.github.surpsg.offlins

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet


fun Project.getMainSourceSetClassFiles(): FileCollection {
    return project.fileTree(getMainSourceSetClassFilesDir()) {
        it.setIncludes(listOf("**/*.class"))
    }
}

fun Project.getMainSourceSetClassFilesDir(): Provider<Directory> {
    return getMainSourceSet().java.classesDirectory
}

fun Project.getMainSourceSetSources(): FileCollection {
    return getMainSourceSet().java.sourceDirectories
}

fun Project.getMainSourceSet(): SourceSet {
    return when {
        project.gradleVersion >= GRADLE_7_1 -> {
            extensions.getByType(JavaPluginExtension::class.java)
                .sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        }

        else -> convention.getPlugin(JavaPluginConvention::class.java)
            .sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
    }
}
