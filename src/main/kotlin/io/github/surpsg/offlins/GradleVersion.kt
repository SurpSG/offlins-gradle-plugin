package io.github.surpsg.offlins

import org.gradle.api.Project

val GRADLE_8_11 = GradleVersion("8.11")

class GradleVersion(
    value: String
) : Comparable<GradleVersion> {

    private val intValue: Int = toComparableInt(value)

    override fun compareTo(other: GradleVersion): Int {
        return intValue.compareTo(other.intValue)
    }

    private fun toComparableInt(value: String): Int {
        return normalizeToThreeDigitNumber(value).splitToSequence(".")
            .map { it.toInt() }
            .fold(0) { sum, next ->
                sum * VERSION_NUMBER_WEIGHT + next
            }
    }

    private fun normalizeToThreeDigitNumber(value: String): String {
        val numericVersionPart = value.substringBefore("-")
        return if (numericVersionPart.count { it == '.' } == 1) {
            "$numericVersionPart.0"
        } else {
            numericVersionPart
        }
    }

    private companion object {
        private const val VERSION_NUMBER_WEIGHT = 100
    }

}


val Project.gradleVersion: GradleVersion
    get() = GradleVersion(gradle.gradleVersion)
