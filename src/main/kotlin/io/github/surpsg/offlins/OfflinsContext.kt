package io.github.surpsg.offlins

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import java.nio.file.Path

internal class OfflinsContext(
    val project: Project,
    val offlinsExtension: OfflinsExtension,
    val offlinsConfigurations: OfflinsConfigurations
) {
    val execFiles: ListProperty<Path> = project.objects.listProperty(Path::class.java)

    val instrumentedJar: Property<InstrumentedJar> = project.objects.property(InstrumentedJar::class.java)

    val instrumentedClassesTask: Property<InstrumentClassesOfflineTask> = project.objects.property(
        InstrumentClassesOfflineTask::class.java
    )
}

internal data class OfflinsConfigurations(
    val jacocoConfiguration: Configuration,
    val jacocoRuntimeConfiguration: Configuration,
    val jacocoInstrumentedConfiguration: Configuration
)
