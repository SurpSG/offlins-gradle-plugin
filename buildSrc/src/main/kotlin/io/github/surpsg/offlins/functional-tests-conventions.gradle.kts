package io.github.surpsg.offlins

import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.base
import org.gradle.kotlin.dsl.`jvm-test-suite`

plugins {
    base
    `jvm-test-suite`
    id("pl.droidsonroids.jacoco.testkit")
}

testing.suites {

    val functionalTest by registering(JvmTestSuite::class) {
        useJUnitJupiter()
        testType.set(TestSuiteType.FUNCTIONAL_TEST)

        sources {
            java {
                setSrcDirs(listOf("src/functionalTests/kotlin"))
                resources.srcDirs("src/funcTest/resources")
            }
        }

        dependencies {
            implementation(project())
        }

        targets.all {
            testTask.configure {
                outputs.apply {
                    upToDateWhen { false }
                    cacheIf { false }
                }

                description = "Runs the functional tests."
                group = "verification"

                maxParallelForks = 4

                systemProperty("junit.jupiter.testinstance.lifecycle.default", "per_class")
                systemProperty("junit.jupiter.execution.parallel.enabled", true)
                systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
                systemProperty("junit.jupiter.execution.parallel.mode.classes.default", "same_thread")
                testLogging {
                    events(TestLogEvent.SKIPPED, TestLogEvent.FAILED, TestLogEvent.PASSED)
                    showStandardStreams = true
                }
            }
        }

    }

}

configure<pl.droidsonroids.gradle.jacoco.testkit.JacocoTestKitExtension> {
    applyTo("functionalTestRuntimeOnly", tasks.named("functionalTest"))
}
