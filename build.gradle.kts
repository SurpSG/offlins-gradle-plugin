import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")

    `java-gradle-plugin`
    alias(deps.plugins.pluginPublish)
    `maven-publish`

    id("io.github.surpsg.offlins.plugin-test")
    alias(deps.plugins.detekt)
}

group = "io.github.surpsg"
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
            id = "io.github.surpsg.offlins"
            displayName = "JaCoCo Offline Instrumentation"
            description = "Plugin that applies JaCoCo offline instrumentation"
            implementationClass = "io.github.surpsg.offlins.OfflinsPlugin"
        }
    }
}
pluginBundle {
    website = "https://github.com/SurpSG/offlins-gradle-plugin"
    vcsUrl = "https://github.com/SurpSG/offlins-gradle-plugin.git"
    tags = listOf("coverage", "jacoco", "offline", "instrumentation")
}

dependencies {

    testImplementation(testDeps.assertj.core)
    functionalTestImplementation(testDeps.assertj.core)
    functionalTestImplementation(testDeps.jacoco.core)

}
