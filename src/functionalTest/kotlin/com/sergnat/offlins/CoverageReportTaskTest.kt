package com.sergnat.offlins

import com.sergnat.offlins.OfflinsJacocoReport.Companion.RELATIVE_HTML_REPORT_LOCATIONS
import com.sergnat.offlins.OfflinsPlugin.Companion.GENERATE_JACOCO_REPORTS_TASK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File


class CoverageReportTaskTest : BaseOfflinsTest() {

    companion object {
        const val TEST_PROJECT_RESOURCE_NAME = "single-module-test-project"
    }

    @BeforeEach
    fun setup() {
        initializeGradleTest()
    }

    @ParameterizedTest
    @ValueSource(strings = ["4.10.3", "5.6.4", "6.9.1", "7.4.2"])
    fun `coverageReport task must generate html report`(gradleVersion: String) {
        gradleRunner
            .withGradleVersion(gradleVersion)
            .withArguments("test", GENERATE_JACOCO_REPORTS_TASK, "-s")
            .build()

        val reportDir: File = rootProjectDir.resolve("build").resolve(RELATIVE_HTML_REPORT_LOCATIONS)
        assertThat(reportDir)
            .isDirectory
            .isDirectoryRecursivelyContaining { it.name == "index.html" && it.isFile }
            .isDirectoryRecursivelyContaining { it.name == "com.java.test" && it.isDirectory }
            .isDirectoryRecursivelyContaining { it.name == "Class1.html" && it.isFile }
            .isDirectoryRecursivelyContaining { it.name == "Class1.java.html" && it.isFile }
    }

    override fun resourceTestProject() = TEST_PROJECT_RESOURCE_NAME

}
