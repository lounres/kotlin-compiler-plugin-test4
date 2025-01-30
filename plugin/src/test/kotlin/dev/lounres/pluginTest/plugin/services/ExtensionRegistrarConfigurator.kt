package dev.lounres.pluginTest.plugin.services

import dev.lounres.pluginTest.plugin.TestCommandLineProcessor
import dev.lounres.pluginTest.plugin.TestIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar.ExtensionStorage
import org.jetbrains.kotlin.config.CommonConfigurationKeys


class ExtensionRegistrarConfigurator(testServices: TestServices) : EnvironmentConfigurator(testServices) {
    override fun ExtensionStorage.registerCompilerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration
    ) {
//        FirExtensionRegistrarAdapter.registerExtension(SimplePluginRegistrar())
        IrGenerationExtension.registerExtension(
            TestIrGenerationExtension(
                messageCollector = configuration.get(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE),
                string = configuration.get(TestCommandLineProcessor.ARG_STRING, ""),
                file = configuration.get(TestCommandLineProcessor.ARG_FILE, "")
            )
        )
    }
}
