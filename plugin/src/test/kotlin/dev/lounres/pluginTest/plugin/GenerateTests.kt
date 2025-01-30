package dev.lounres.pluginTest.plugin

import org.jetbrains.kotlin.generators.generateTestGroupSuiteWithJUnit5
import dev.lounres.pluginTest.plugin.runners.AbstractBoxTest
import dev.lounres.pluginTest.plugin.runners.AbstractDiagnosticTest

fun main() {
    generateTestGroupSuiteWithJUnit5 {
        testGroup(testDataRoot = "src/test/data", testsRoot = "build/generated/kotlinCompilerPluginTestGenerator/test") {
            testClass<AbstractDiagnosticTest> {
                model("diagnostics")
            }

            testClass<AbstractBoxTest> {
                model("box")
            }
        }
    }
}
