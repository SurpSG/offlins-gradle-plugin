import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.20"
    base
    `jvm-test-suite`

    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.18.0"

    jacoco
    id("pl.droidsonroids.jacoco.testkit") version "1.0.8"
}

group = "com.sergnat"
version = "0.1.0"

repositories {
    mavenCentral()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

gradlePlugin {
    plugins {
        create("offlins-gradle-plugin") {
            id = "com.sergnat.offlins"
            displayName = "JaCoCo Offline Instrumentation"
            description = "Plugin that applies JaCoCo offline instrumentation"
            implementationClass = "com.sergnat.offlins.OfflinsPlugin"
        }
    }
}
pluginBundle {
    website = "https://github.com/SurpSG/offlins-gradle-plugin"
    vcsUrl = "https://github.com/SurpSG/offlins-gradle-plugin.git"
    tags = listOf("coverage", "jacoco", "offline", "instrumentation")
}


testing.suites {
    val test by getting(JvmTestSuite::class) {
        useJUnitJupiter()

        dependencies {
        }
    }

    val functionalTest by registering(JvmTestSuite::class) {
        useJUnitJupiter()
        testType.set(TestSuiteType.FUNCTIONAL_TEST)

        sources {
            java {
                setSrcDirs(listOf("src/functionalTests/kotlin"))
                resources.srcDirs("src/funcTest/resources")
            }
        }
        configure<GradlePluginDevelopmentExtension> {
            testSourceSets(sources)
        }

        dependencies {
            implementation(project)
            implementation("org.assertj:assertj-core:3.20.2")
        }
    }
}

val functionalTest = tasks.named("functionalTest")
functionalTest.configure {
    dependsOn(tasks.generateJacocoTestKitProperties)
}

tasks.check.configure {
    dependsOn(functionalTest)
}
tasks.named("functionalTest") {
    dependsOn(tasks.generateJacocoTestKitProperties)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test, functionalTest)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

configure<pl.droidsonroids.gradle.jacoco.testkit.JacocoTestKitExtension> {
    applyTo("functionalTestRuntimeOnly", functionalTest)
}

