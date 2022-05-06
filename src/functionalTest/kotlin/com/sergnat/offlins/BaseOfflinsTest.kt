package com.sergnat.offlins

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
        val configuration: TestConfiguration = buildTestConfiguration()
        tempTestDir = Files.newTemporaryFolder()
        rootProjectDir = tempTestDir.copyDirFromResources<BaseOfflinsTest>(configuration.resourceTestProject)
        buildFile = rootProjectDir.resolve(configuration.rootBuildFilePath)

        gradleRunner = buildGradleRunner(rootProjectDir).apply {
            runTask("test")
        }
    }

    abstract fun buildTestConfiguration(): TestConfiguration
}

class TestConfiguration(
    val resourceTestProject: String,
    val rootBuildFilePath: String
)
