package dev.lounres.pluginTest.plugin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name


class TestIrGenerationExtension(
    private val messageCollector: MessageCollector,
    private val string: String,
    private val file: String,
) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val timeSourceClass = pluginContext.referenceClass(ClassId(FqName("kotlin.time"), Name.identifier("TimeSource")))
        println(
            """
            =========================
            $timeSourceClass
            =========================
            """.trimIndent()
        )
        val monotonicClass = pluginContext.referenceClass(ClassId(FqName("kotlin.time.TimeSource"), Name.identifier("Monotonic")))
        println(
            """
            =========================
            $monotonicClass
            =========================
            """.trimIndent()
        )
    }
}
