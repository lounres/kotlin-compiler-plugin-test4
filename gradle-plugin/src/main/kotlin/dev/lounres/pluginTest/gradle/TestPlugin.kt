package dev.lounres.pluginTest.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption


class TestPlugin : KotlinCompilerPluginSupportPlugin {
    override fun apply(target: Project): Unit = with(target) {
        extensions.create<TestExtension>("template")
    }
    
    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true
    
    override fun getCompilerPluginId(): String = "dev.lounres.pluginTest"
    
    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact(
            groupId = "dev.lounres",
            artifactId = "pluginTest",
            version = "0.0.0"
        )
    
    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val extension = project.extensions.getByType<TestExtension>()
        return project.provider {
            listOf(
                SubpluginOption(key = "string", value = extension.stringProperty.get()),
                SubpluginOption(key = "file", value = extension.fileProperty.get().asFile.path),
            )
        }
    }
}