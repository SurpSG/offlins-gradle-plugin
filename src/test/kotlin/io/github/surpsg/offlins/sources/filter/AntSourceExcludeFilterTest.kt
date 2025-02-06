package io.github.surpsg.offlins.sources.filter

import io.github.surpsg.offlins.gradletest.testJavaProject
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSingleElement
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class AntSourceExcludeFilterTest {
    private val project: Project = testJavaProject()

    @Test
    fun `should return origin sources if filters collection is empty`() {
        // GIVEN
        val expectedFiles = arrayOf("file.1", "file.2")

        val emptyFilters = AntSourceExcludeFilter(emptyList())

        val inputSource: FileCollection = project.files(*expectedFiles)

        // WHEN
        val actualFilteredFiles: FileCollection = emptyFilters.filter(inputSource)

        // THEN
        assertSoftly(actualFilteredFiles) {
            map { it.name }.shouldContainExactlyInAnyOrder(*expectedFiles)
        }
    }

    @ParameterizedTest
    @CsvSource(
        "exclude-1.txt, **/exclude-1.txt",
        "exclude-2.txt, **/exclude-*.txt",
        "a/exclude-3.txt, **/exclude-*.txt",
        "a/exclude-4.txt, **/a/*",
        "a/b/exclude-5.txt, **/a/b/**",
        "a/b/exclude-6.txt, **/b/**",
        "a/b/c/d/exclude-7.txt, **/a/**/d/**",
    )
    fun `should filter exclude file if matched to filter`(
        filePathToExclude: String,
        excludePattern: String,
    ) {
        // GIVEN
        val expectedFile = "file-to-keep.txt"
        val excludeFilters = AntSourceExcludeFilter(listOf(excludePattern))
        val inputSource: ConfigurableFileCollection = project.files(
            project.layout.buildDirectory.file(filePathToExclude).get().asFile,
            expectedFile
        ).onEach {
            it.parentFile.mkdirs()
            it.createNewFile()
        }

        // WHEN
        val actualFilteredFiles: FileCollection = excludeFilters.filter(inputSource)

        // THEN
        assertSoftly(actualFilteredFiles) {
            map { it.name } shouldHaveSingleElement expectedFile
        }
    }
}
