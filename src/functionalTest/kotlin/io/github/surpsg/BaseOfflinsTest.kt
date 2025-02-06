package io.github.surpsg

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class BaseOfflinsTest {

    @TempDir
    lateinit var tempTestDir: File

    lateinit var rootProjectDir: File
    lateinit var buildFile: File
    lateinit var gradleRunner: GradleRunner

    /**
     * should be invoked in @Before test class method
     */
    fun initializeGradleTest() {
        rootProjectDir = tempTestDir.copyDirFromResources<BaseOfflinsTest>(resourceTestProject())
        buildFile = rootProjectDir.resolve("build.gradle")

        gradleRunner = buildGradleRunner(rootProjectDir)
    }

    abstract fun resourceTestProject(): String


    companion object {

        @JvmStatic
        fun supportedGradleVersions(): List<String> {
            return listOf(
                "8.11",
                "8.12.1", // the latest release or release candidate
            )
        }
    }
}
