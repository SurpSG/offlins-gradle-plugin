package io.github.surpsg.offlins

plugins {
    id("io.github.surpsg.delta-coverage")
}

val isGithub = project.hasProperty("github")

deltaCoverageReport {
    diffSource.byGit {
        diffBase = project.properties["diffBase"]?.toString() ?: "refs/remotes/origin/main"
        useNativeGit = true
    }

    if (isGithub) {
        coverageBinaryFiles = fileTree("tests-artifacts/jacoco") { include("**/*.exec") }
    }

    reports {
        html = true
        xml = true
        console = true
        markdown = true
        fullCoverageReport = true
    }

    violationRules.failIfCoverageLessThan(0.9)
}

