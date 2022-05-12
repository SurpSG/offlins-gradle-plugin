package com.sergnat.offlins

import org.jacoco.core.data.ExecutionDataStore
import org.jacoco.core.tools.ExecFileLoader
import java.io.File

fun loadExecutionData(execFile: File): ExecutionDataStore {
    return ExecFileLoader().apply {
        load(execFile)
    }.executionDataStore
}
