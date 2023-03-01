package io.github.surpsg.offlins

plugins {
    id("com.form.diff-coverage")
}

val isGithub = project.hasProperty("github")

diffCoverageReport {
    diffSource {
        git.diffBase = project.properties["diffBase"]?.toString() ?: "refs/remotes/origin/main"
    }

    if (isGithub) {
        jacocoExecFiles = fileTree("tests-artifacts/jacoco") { include("**/*.exec") }
    }

    reports {
        html = true
        xml = true
    }

    violationRules.failIfCoverageLessThan(0.9)
}
