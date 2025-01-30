plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm {
        withJava()
    }
    js {
        browser()
    }
}