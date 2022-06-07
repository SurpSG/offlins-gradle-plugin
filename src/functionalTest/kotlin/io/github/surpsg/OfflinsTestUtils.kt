package io.github.surpsg

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

fun buildGradleRunner(
    projectRoot: File
): GradleRunner {
    return GradleRunner.create()
        .withPluginClasspath()
        .withProjectDir(projectRoot)
        .withTestKitDir(projectRoot.resolve("TestKitDir").apply {
            mkdir()
        })
        .apply {
            // gradle testkit jacoco support
            // gradle testkit jacoco support
            javaClass.classLoader.getResourceAsStream("testkit-gradle.properties")?.use { inputStream ->
                File(projectDir, "gradle.properties").outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
}

fun GradleRunner.runTask(task: String): BuildResult = withArguments(task, "-si").build()

fun BuildResult.assertOutputContainsStrings(vararg expectedString: String): BuildResult {
    assertThat(output).contains(*expectedString)
    return this
}

fun BuildResult.assertThatOutputLines(isValid: ListAssert<String>.() -> Unit): BuildResult {
    assertThat(output.lines()).isValid()
    return this
}

fun BuildResult.assertThatTaskStatusIs(taskName: String, status: TaskOutcome): BuildResult {
    assertThat(task(":$taskName"))
        .isNotNull
        .extracting { it?.outcome }
        .isEqualTo(status)
    return this
}
