plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(11)
    sourceSets {
        main {
            dependencies {
                compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin-api:${libs.versions.kotlin.asProvider().get()}")
            }
        }
    }
}