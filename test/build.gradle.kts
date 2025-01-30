import org.jetbrains.kotlin.gradle.plugin.PLUGIN_CLASSPATH_CONFIGURATION_NAME

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()
    js {
        browser()
    }
}

dependencies {
    add(
        PLUGIN_CLASSPATH_CONFIGURATION_NAME,
        project(":plugin")
    )
}