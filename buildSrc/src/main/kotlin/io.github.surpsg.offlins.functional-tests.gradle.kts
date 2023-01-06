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
                maxParallelForks = 4
                systemProperty("junit.jupiter.testinstance.lifecycle.default", "per_class")
            }
        }

    }

}

configure<pl.droidsonroids.gradle.jacoco.testkit.JacocoTestKitExtension> {
    applyTo("functionalTestRuntimeOnly", tasks.named("functionalTest"))
}
