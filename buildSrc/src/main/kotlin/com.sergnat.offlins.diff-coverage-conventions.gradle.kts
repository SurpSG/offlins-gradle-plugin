plugins {
    id("com.form.diff-coverage")
}

diffCoverageReport {
    diffSource {
        git.diffBase = project.properties["diffBase"]?.toString() ?: "refs/remotes/origin/main"
    }

    reports {
        html = true
        xml = true
    }

    violationRules.failIfCoverageLessThan(0.9)
}
