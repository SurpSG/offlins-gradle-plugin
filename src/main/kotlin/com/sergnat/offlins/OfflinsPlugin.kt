package com.sergnat.offlins

import org.gradle.api.Plugin
import org.gradle.api.Project

class OfflinsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.create(OFFLINS_TASK).doLast {
            println("Currently, I do nothing")
        }
    }

    companion object {
        const val OFFLINS_TASK = "jacocoReport"
    }

}
