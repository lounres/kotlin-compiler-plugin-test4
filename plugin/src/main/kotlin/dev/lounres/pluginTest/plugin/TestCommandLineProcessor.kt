package dev.lounres.pluginTest.plugin

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey


@OptIn(ExperimentalCompilerApi::class)
class TestCommandLineProcessor : CommandLineProcessor {
    companion object {
        private const val OPTION_STRING = "string"
        private const val OPTION_FILE = "file"
        
        val ARG_STRING = CompilerConfigurationKey<String>(OPTION_STRING)
        val ARG_FILE = CompilerConfigurationKey<String>(OPTION_FILE)
    }
    
    override val pluginId: String = "dev.lounres.pluginTest"
    
    override val pluginOptions: Collection<CliOption> = listOf(
        CliOption(
            optionName = OPTION_STRING,
            valueDescription = "string",
            description = "sample string argument",
            required = false,
        ),
        CliOption(
            optionName = OPTION_FILE,
            valueDescription = "file",
            description = "sample file argument",
            required = false,
        ),
    )
    
    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration,
    ) = when (option.optionName) {
        OPTION_STRING -> configuration.put(ARG_STRING, value)
        OPTION_FILE -> configuration.put(ARG_FILE, value)
        else -> throw IllegalArgumentException("Unexpected config option ${option.optionName}")
    }
}