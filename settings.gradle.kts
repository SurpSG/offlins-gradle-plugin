plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}


rootProject.name = "offlins-gradle-plugin"

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

buildCache {
    local.isEnabled = true
}
