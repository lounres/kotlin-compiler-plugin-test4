import org.jetbrains.kotlin.gradle.plugin.PLUGIN_CLASSPATH_CONFIGURATION_NAME
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

dependencies {
    add(
        PLUGIN_CLASSPATH_CONFIGURATION_NAME,
        project(":plugin")
    )
}

kotlin {
    jvm()
    js {
        browser {
            commonWebpackConfig {
                outputFileName = "main.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    // Uncomment and configure this if you want to open a browser different from the system default
                    // open = mapOf(
                    //     "app" to mapOf(
                    //         "name" to "google chrome"
                    //     )
                    // )
                    
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
//                        add(rootDir.path + "/api")
//                        add(rootDir.path + "/client/common")
//                        add(rootDir.path + "/client/web/")
                    }
                }
            }
        }
        binaries.executable()
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":plugin-runtime"))
            }
        }
    }
}