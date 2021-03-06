package io.github.surpsg

import org.assertj.core.util.Files
import org.gradle.testkit.runner.GradleRunner
import java.io.File

abstract class BaseOfflinsTest {

//    @TempDir
    lateinit var tempTestDir: File

    lateinit var rootProjectDir: File
    lateinit var buildFile: File
    lateinit var gradleRunner: GradleRunner

    /**
     * should be invoked in @Before test class method
     */
    fun initializeGradleTest() {
        tempTestDir = Files.newTemporaryFolder()
        rootProjectDir = tempTestDir.copyDirFromResources<BaseOfflinsTest>(resourceTestProject())
        buildFile = rootProjectDir.resolve("build.gradle")

        gradleRunner = buildGradleRunner(rootProjectDir)
    }

    abstract fun resourceTestProject(): String

}
