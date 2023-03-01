package io.github.surpsg.offlins

import org.gradle.api.Project

val GRADLE_6_1 = GradleVersion("6.1")
val GRADLE_6_8 = GradleVersion("6.8")
val GRADLE_7_1 = GradleVersion("7.1")

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
