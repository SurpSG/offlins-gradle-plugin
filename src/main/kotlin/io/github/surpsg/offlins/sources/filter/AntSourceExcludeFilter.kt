package io.github.surpsg.offlins.sources.filter

import org.gradle.api.file.FileCollection
import org.gradle.api.internal.file.pattern.PatternMatcher
import org.gradle.api.internal.file.pattern.PatternMatcherFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

internal class AntSourceExcludeFilter(
    private val patterns: List<String>
) : SourceFilter {

    private val matcher: PatternMatcher = PatternMatcherFactory.getPatternsMatcher(true, false, patterns)

    override fun filter(input: FileCollection): FileCollection {
        return if (patterns.isEmpty()) {
            input
        } else {
            log.info("Applied exclude patterns {} to classes", patterns)
            filterCollectionFiles(input)
        }
    }

    fun matchFile(segments: Array<String>, isFile: Boolean): Boolean {
        return !matcher.test(segments, isFile)
    }

    private fun filterCollectionFiles(originSources: FileCollection): FileCollection {
        return originSources.asFileTree
            .filter { file ->
                val segments: Array<String> = file.obtainSegments()
                matchFile(segments, file.isFile)
            }
    }

    private fun File.obtainSegments(): Array<String> {
        return this.toPath()
            .toAbsolutePath()
            .asSequence()
            .map { it.toString() }
            .toList()
            .toTypedArray()
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(AntSourceExcludeFilter::class.java)
    }
}
