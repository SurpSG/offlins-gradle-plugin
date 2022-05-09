import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    alias(deps.plugins.pluginPublish)
    id("com.sergnat.offlins.plugin-test")
    alias(deps.plugins.detekt)
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

dependencies {

    functionalTestImplementation(testDeps.assertj.core)

}
