package dev.lounres.pluginTest.plugin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration


class TestComponentRegistrar(
    private val defaultString: String,
    private val defaultFile: String,
) : CompilerPluginRegistrar() {
    
    constructor() : this(
        defaultString = "Hello, World!",
        defaultFile = "file.txt",
    )
    
    override val supportsK2: Boolean get() = true
    
    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val messageCollector = configuration.get(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        val string = configuration.get(TestCommandLineProcessor.ARG_STRING, defaultString)
        val file = configuration.get(TestCommandLineProcessor.ARG_FILE, defaultFile)
        
        IrGenerationExtension.registerExtension(TestIrGenerationExtension(messageCollector, string, file))
    }
}