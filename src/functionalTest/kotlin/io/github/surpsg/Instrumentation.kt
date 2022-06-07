package io.github.surpsg

import org.jacoco.core.internal.instr.InstrSupport
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import java.io.File
import java.io.FileInputStream

fun isInstrumented(classfile: File): Boolean {
    return isInstrumented(
        FileInputStream(classfile).readBytes()
    )
}

fun isInstrumented(classFileContent: ByteArray): Boolean {
    val reader: ClassReader = InstrSupport.classReaderFor(classFileContent)
    val methods: MutableSet<String> = HashSet()
    reader.accept(object : ClassVisitor(InstrSupport.ASM_API_VERSION) {
        override fun visitMethod(
            access: Int,
            name: String,
            descriptor: String,
            signature: String?,
            exceptions: Array<String>?
        ): MethodVisitor? {
            methods.add(name)
            return null
        }
    }, 0)
    return methods.contains("\$jacocoInit")
}
