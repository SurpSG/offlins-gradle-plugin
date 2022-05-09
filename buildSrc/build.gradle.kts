plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(buildDeps.kotlinJvm)
    implementation(kotlin("gradle-plugin"))

    implementation(buildDeps.diffCoverage)
    implementation(buildDeps.jacocoTestkit)
}
