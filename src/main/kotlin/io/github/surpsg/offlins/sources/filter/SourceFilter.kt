package io.github.surpsg.offlins.sources.filter

import io.github.surpsg.offlins.ReportsExtension
import org.gradle.api.file.FileCollection

internal fun interface SourceFilter {

    fun filter(inputSource: FileCollection): FileCollection

    companion object {

        private val NOOP_FILTER: SourceFilter = SourceFilter { it }

        fun build(reportsExtension: ReportsExtension): SourceFilter {
            val excludes: List<String> = reportsExtension.excludeClasses.get()
            return if (excludes.isEmpty()) {
                NOOP_FILTER
            } else {
                AntSourceExcludeFilter(excludes)
            }
        }
    }
}
