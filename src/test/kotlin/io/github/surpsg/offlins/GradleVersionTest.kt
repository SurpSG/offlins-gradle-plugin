package io.github.surpsg.offlins

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class GradleVersionTest {

    @Test
    fun `gradle version 7-4 must be greater than 7-3-2`() {
        assertThat(GradleVersion("7.4"))
            .isGreaterThan(GradleVersion("7.3.2"))
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "7.4.2, 7.4.1",
            "8.0-rc-5, 7.4.1",
        ]
    )
    fun `newer gradle version must be greater than older version`(
        greaterVersion: String,
        lowerVersion: String
    ) {
        assertThat(GradleVersion(greaterVersion))
            .isGreaterThan(GradleVersion(lowerVersion))
    }

    @Test
    fun `gradle version 2-0 must be greater than 1-12`() {
        assertThat(GradleVersion("2.0"))
            .isGreaterThan(GradleVersion("1.12"))
    }

    @Test
    fun `gradle version 3-0 must be greater than 2-14-1`() {
        assertThat(GradleVersion("3.0"))
            .isGreaterThan(GradleVersion("2.14.1"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["5.0", "7.4.2", "0.9"])
    fun `gradle version must be equal to itself`(version: String) {
        assertThat(GradleVersion(version))
            .isEqualByComparingTo(GradleVersion(version))
    }

}
