plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(buildDeps.kotlinJvm)
    implementation(kotlin("gradle-plugin"))
    implementation(buildDeps.pluginPublish)

    implementation(buildDeps.diffCoverage)
    implementation(buildDeps.jacocoTestkit)
}
