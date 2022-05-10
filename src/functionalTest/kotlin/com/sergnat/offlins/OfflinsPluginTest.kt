package com.sergnat.offlins

import com.sergnat.offlins.OfflinsPlugin.Companion.ASSEMBLE_INSTRUMENTED_JAR_TASK
import com.sergnat.offlins.OfflinsPlugin.Companion.INSTRUMENTED_JAR_SUFFIX
import com.sergnat.offlins.OfflinsPlugin.Companion.INSTRUMENT_CLASSES_TASK
import com.sergnat.offlins.OfflinsPlugin.Companion.OFFLINS_TASK
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.util.zip.ZipFile


class OfflinsPluginTest : BaseOfflinsTest() {

    companion object {
        const val TEST_PROJECT_RESOURCE_NAME = "single-module-test-project"
        const val CLASS_FILE_EXT = ".class"
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

        buildFile.appendText(
            """
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
            .assertThatTaskStatusIs(OFFLINS_TASK, TaskOutcome.SUCCESS)
            .assertOutputContainsStrings(*expectedDependencies)
            .assertOutputContainsStrings("Currently, I do nothing")
    }

    @Test
    fun `instrumentClassesOffline task must instrument classes`() {
        // run // assert
        gradleRunner
            .runTask(INSTRUMENT_CLASSES_TASK)
            .assertThatTaskStatusIs(INSTRUMENT_CLASSES_TASK, TaskOutcome.SUCCESS)

        val instrumentedClassesDir = rootProjectDir.resolve("build/${InstrumentClassesOfflineTask.OUTPUT_DIR_NAME}")
        assertThat(instrumentedClassesDir)
            .isDirectoryRecursivelyContaining { file ->
                file.name.endsWith(CLASS_FILE_EXT) // assert directory contains .class files
            }
            .isDirectoryRecursivelyContaining { file ->
                file.name.endsWith(CLASS_FILE_EXT) && isInstrumented(file) // assert .class files are instrumented
            }
    }

    @Test
    fun `assembleInstrumentedJar task must create jar with instrumented classes`() {
        // setup
        val instrumentedJarFileName = "${TEST_PROJECT_RESOURCE_NAME}-$INSTRUMENTED_JAR_SUFFIX.jar"
        val expectedJar: File = rootProjectDir.resolve("build/libs/$instrumentedJarFileName")

        // run // assert
        gradleRunner
            .runTask(ASSEMBLE_INSTRUMENTED_JAR_TASK)
            .assertThatTaskStatusIs(ASSEMBLE_INSTRUMENTED_JAR_TASK, TaskOutcome.SUCCESS)

        assertThat(expectedJar).isFile
        assertThat(readJarClasses(expectedJar))
            .isNotEmpty
            .`as`("Class is not instrumented").allMatch { classFileContent ->
                isInstrumented(classFileContent)
            }
    }

    private fun readJarClasses(jarFile: File): List<ByteArray> {
        val file = ZipFile(jarFile)
        return file.entries().asSequence()
            .filter { it.name.endsWith(CLASS_FILE_EXT) }
            .map { entry ->
                file.getInputStream(entry).use { it.readBytes() }
            }
            .toList()
    }

    override fun buildTestConfiguration() = TestConfiguration(
        TEST_PROJECT_RESOURCE_NAME,
        "build.gradle"
    )

}
