package io.github.surpsg

import org.assertj.core.api.AbstractObjectAssert
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.InstanceOfAssertFactories.list
import org.jacoco.core.data.ExecutionData
import org.jacoco.core.data.ExecutionDataStore
import org.jacoco.core.tools.ExecFileLoader
import java.io.File
import java.util.function.Function


enum class Covered {
    PARTIALLY, FULLY, NO
}

data class ClassCov(val className: String, val covered: Covered)

fun File.assertModuleHasCoverageDataForClasses(vararg classes: ClassCov) {
    assertModuleHasCoverageDataForClasses("build/jacoco/test.exec", *classes)
}

fun File.assertModuleHasCoverageDataForClasses(execFilePath: String, vararg classes: ClassCov) {
    val execFile: File = resolve(execFilePath)
    execFile.assertExecFileIsNotEmpty()

    val executionData: ExecutionDataStore = loadExecutionData(execFile)
    assertThat(executionData)
        .assertContainsCoverageForClasses(*classes)
        .assertProbesForClasses(*classes)
}

private fun File.assertExecFileIsNotEmpty() = assertThat(this).hasExtension("exec").isFile.isNotEmpty

private fun AbstractObjectAssert<*, ExecutionDataStore>.assertContainsCoverageForClasses(
    vararg expectedCoverages: ClassCov
): AbstractObjectAssert<*, ExecutionDataStore> {
    extracting { it.contents }
        .asInstanceOf(list(ExecutionData::class.java))
        .isNotEmpty
        .flatExtracting(ExecutionData::getName)
        .containsExactlyInAnyOrder(*expectedCoverages.map { it.className }.toTypedArray())
    return this
}

private fun AbstractObjectAssert<*, ExecutionDataStore>.assertProbesForClasses(
    vararg classes: ClassCov
): AbstractObjectAssert<*, ExecutionDataStore> {
    val names: Map<String, Covered> = classes.associate { it.className to it.covered }
    extracting { it.contents }
        .asInstanceOf(list(ExecutionData::class.java))
        .filteredOn { names.contains(it.name) }
        .hasSize(names.size)
        .map(Function { it.name to it.probes })
        .allSatisfy { nameToProbes ->
            val covered: Covered = names.getValue(nameToProbes.first)
            val actualProbes = assertThat(nameToProbes.second.toList())
            when (covered) {
                Covered.NO -> actualProbes.allMatch { it == false }
                Covered.FULLY -> actualProbes.allMatch { it == true }
                Covered.PARTIALLY -> {
                    actualProbes.anyMatch { it == false }
                    actualProbes.anyMatch { it == true }
                }
            }
        }
    return this
}

private fun loadExecutionData(execFile: File): ExecutionDataStore {
    return ExecFileLoader().apply {
        load(execFile)
    }.executionDataStore
}
