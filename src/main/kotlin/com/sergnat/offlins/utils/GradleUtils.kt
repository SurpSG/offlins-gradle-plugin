package com.sergnat.offlins.utils

import org.gradle.api.provider.Provider

fun <T> Provider<T>.orElseProvider(default: Provider<T>): Provider<T> {
    return if (isPresent) {
        this
    } else {
        default
    }
}
