rootProject.name = "offlins-conventions"

enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
        maven("https://jitpack.io")
    }
    versionCatalogs {
        create("buildDeps") {
            from(files("../gradle/buildsrc-deps.versions.toml"))
        }
    }
}
