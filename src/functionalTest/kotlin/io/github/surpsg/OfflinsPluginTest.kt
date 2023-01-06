package io.github.surpsg

import io.github.surpsg.offlins.InstrumentClassesOfflineTask
import io.github.surpsg.offlins.InstrumentClassesOfflineTask.Companion.INSTRUMENT_CLASSES_TASK
import io.github.surpsg.offlins.InstrumentedJar.Companion.ASSEMBLE_INSTRUMENTED_JAR_TASK
import io.github.surpsg.offlins.InstrumentedJar.Companion.INSTRUMENTED_JAR_SUFFIX
import io.github.surpsg.offlins.OfflinsPlugin.Companion.JACOCO_INSTRUMENTED_CONFIGURATION
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.nio.file.Paths
import java.util.zip.ZipFile


class OfflinsPluginTest : BaseOfflinsTest() {

    companion object {
        const val TEST_PROJECT_RESOURCE_NAME = "single-module-test-project"
        const val CLASS_FILE_EXT = ".class"
    }

    private val markerToken = javaClass.simpleName

    @BeforeEach
    fun setup() {
        initializeGradleTest()
    }

    @Test
    fun `applying of plugin must fail if gradle version is less than 5-1`() {
        val gradleVersion = "5.0"

        gradleRunner
            .withGradleVersion(gradleVersion)
            .withArguments("tasks", "--dry-run")
            .buildAndFail()
            .assertOutputContainsStrings(
                "$gradleVersion is not supported"
            )
    }

    @Test
    fun `plugin must add jacoco configurations with dependencies`() {
        // setup
        val jacocoVersion = "0.8.7"
        val expectedDependencies = arrayOf(
            "jacoco:org.jacoco:org.jacoco.ant:${jacocoVersion}",
            "jacocoRuntime:org.jacoco:org.jacoco.agent:${jacocoVersion}"
        )

        buildFile.appendText(
            """
            offlinsCoverage {
                jacocoVersion = '$jacocoVersion'
            }

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
        gradleRunner.withArguments("tasks", "--dry-run").build()
            .assertOutputContainsStrings(*expectedDependencies)
    }

    @Test
    fun `plugin must add jacoco instrumented configuration with instrumented jar artifact`() {
        // setup
        val markerToken = javaClass.simpleName
        val expectedArtifact = "$markerToken:$TEST_PROJECT_RESOURCE_NAME-$INSTRUMENTED_JAR_SUFFIX.jar"
        buildFile.appendText(
            """
            configurations.getByName("$JACOCO_INSTRUMENTED_CONFIGURATION")
                .artifacts.forEach {
                    def confArtifact = ["$markerToken", it.file.name].join(":")
                    println confArtifact
                }
        """.trimIndent()
        )

        // run // assert
        gradleRunner.withArguments("tasks", "--dry-run").build()
            .assertOutputContainsStrings(expectedArtifact)
    }

    @Test
    fun `test classpath must contain instrumented artifacts`() {
        // setup
        buildFile.appendText(
            """
            tasks.withType(Test) {
                doLast {
                    classpath.forEach {
                        println '$markerToken:' + it 
                    }
                }
            }
        """.trimIndent()
        )

        // run
        val buildResult: BuildResult = gradleRunner.runTask("test")

        // assert
        buildResult.assertThatOutputLines {
            val expectedClassesDir = Paths.get("build", InstrumentClassesOfflineTask.OUTPUT_DIR_NAME).toString()
            `as`("contains instrumented classes dir $expectedClassesDir").anyMatch {
                it.startsWith(markerToken) && it.endsWith(expectedClassesDir)
            }
        }

        rootProjectDir.assertModuleHasCoverageDataForClasses(ClassCov("com/java/test/Class1", Covered.PARTIALLY))
    }

    @Test
    fun `instrumentClassesOffline task must instrument classes`() {
        // run // assert
        gradleRunner
            .runTask(INSTRUMENT_CLASSES_TASK)
            .assertThatTaskStatusIs("classes", TaskOutcome.SUCCESS)
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

    @ParameterizedTest
    @ValueSource(strings = [
        "5.1",
        "5.6.4",
        "6.9.1",
        "7.4.2",
        "7.6",
        "8.0-rc-1"
    ])
    fun `assembleInstrumentedJar task must create jar with instrumented classes`(gradleVersion: String) {
        // setup
        val instrumentedJarFileName = "$TEST_PROJECT_RESOURCE_NAME-$INSTRUMENTED_JAR_SUFFIX.jar"
        val expectedJar: File = rootProjectDir.resolve("build/libs/$instrumentedJarFileName")

        // run // assert
        gradleRunner
            .withGradleVersion(gradleVersion)
            .runTask(ASSEMBLE_INSTRUMENTED_JAR_TASK)
            .assertThatTaskStatusIs(INSTRUMENT_CLASSES_TASK, TaskOutcome.SUCCESS)
            .assertThatTaskStatusIs(ASSEMBLE_INSTRUMENTED_JAR_TASK, TaskOutcome.SUCCESS)

        assertThat(expectedJar).exists().isFile
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

    override fun resourceTestProject() = TEST_PROJECT_RESOURCE_NAME

}
