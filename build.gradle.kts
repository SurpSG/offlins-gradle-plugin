plugins {
    id("io.github.surpsg.offlins.gradle-plugin-conventions")
    alias(deps.plugins.detekt)
}

repositories {
    mavenCentral()
}

gradlePlugin {
    website.set("https://github.com/SurpSG/offlins-gradle-plugin")
    vcsUrl.set("https://github.com/SurpSG/offlins-gradle-plugin.git")

    plugins {
        create("offlins-gradle-plugin") {
            id = "io.github.surpsg.offlins"
            displayName = "JaCoCo Offline Instrumentation"
            description = "Plugin that applies JaCoCo offline instrumentation"
            implementationClass = "io.github.surpsg.offlins.OfflinsPlugin"
            tags.set(listOf("coverage", "jacoco", "offline", "instrumentation"))
        }
    }
}

dependencies {

    testImplementation(testDeps.assertj.core)
    functionalTestImplementation(testDeps.assertj.core)
    functionalTestImplementation(testDeps.jacoco.core)

}
