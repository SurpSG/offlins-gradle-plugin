rootProject.name = "offlins-gradle-plugin"

enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    versionCatalogs {
        create("deps") {
            from(files("gradle/deps.versions.toml"))
        }
        create("testDeps") {
            from(files("gradle/test-deps.versions.toml"))
        }
    }
}

