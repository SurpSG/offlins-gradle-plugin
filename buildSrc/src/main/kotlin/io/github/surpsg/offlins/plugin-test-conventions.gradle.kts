package io.github.surpsg.offlins

import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    base
    `jvm-test-suite`
    `java-gradle-plugin`

    id("io.github.surpsg.offlins.functional-tests-conventions")
    id("io.github.surpsg.offlins.delta-coverage-conventions")
}

testing.suites {

    val test by getting(JvmTestSuite::class) {
        useJUnitJupiter()

        targets.all {
            testTask.configure {
                outputs.apply {
                    upToDateWhen { false }
                    cacheIf { false }
                }

                systemProperty("junit.jupiter.testinstance.lifecycle.default", "per_class")
                systemProperty("junit.jupiter.execution.parallel.enabled", true)
                systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
                systemProperty("junit.jupiter.execution.parallel.mode.classes.default", "concurrent")
                testLogging {
                    events(TestLogEvent.SKIPPED, TestLogEvent.FAILED, TestLogEvent.PASSED)
                    showStandardStreams = true
                }
            }
        }
    }
}

val functionalTestSuite: JvmTestSuite = testing.suites.getByName("functionalTest") as JvmTestSuite
configure<GradlePluginDevelopmentExtension> {
    testSourceSet(functionalTestSuite.sources)
}

tasks.check.configure {
    functionalTestSuite.targets.forEach { suiteTarget ->
        dependsOn(suiteTarget.testTask)
    }
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    functionalTestSuite.targets.forEach { suiteTarget ->
        dependsOn(suiteTarget.testTask)
    }

    executionData.setFrom(fileTree(buildDir).include("/jacoco/*.exec"))
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
