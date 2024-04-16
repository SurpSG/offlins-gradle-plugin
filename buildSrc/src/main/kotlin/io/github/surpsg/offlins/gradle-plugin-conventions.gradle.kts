package io.github.surpsg.offlins

import org.gradle.kotlin.dsl.kotlin

plugins {
    kotlin("jvm")

    id("java-gradle-plugin")
    id("io.github.surpsg.offlins.plugin-test-conventions")

    `maven-publish`
    id("com.gradle.plugin-publish")
}

val targetJvmVersion = JavaLanguageVersion.of(8)
kotlin {
    jvmToolchain {
        languageVersion.set(targetJvmVersion)
    }
}

java {
    toolchain {
        languageVersion.set(targetJvmVersion)
    }
}
