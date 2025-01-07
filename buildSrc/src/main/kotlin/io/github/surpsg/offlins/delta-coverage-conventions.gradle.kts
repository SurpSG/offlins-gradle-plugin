package io.github.surpsg.offlins

plugins {
    id("io.github.surpsg.delta-coverage")
}

deltaCoverageReport {
    diffSource.byGit {
        diffBase = project.properties["diffBase"]?.toString() ?: "refs/remotes/origin/main"
        useNativeGit = true
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

