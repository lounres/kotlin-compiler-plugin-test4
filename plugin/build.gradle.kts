import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    java
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap")
}

sourceSets {
    main {
//        java.setSrcDirs(listOf("src"))
//        resources.setSrcDirs(listOf("resources"))
    }
    test {
        java.setSrcDirs(listOf("src/test/java", "build/generated/kotlinCompilerPluginTestGenerator/test"))
    }
}

dependencies {
    val kotlinVersion = libs.versions.kotlin.asProvider().get()
    
    "org.jetbrains.kotlin:kotlin-compiler:$kotlinVersion".let {
        compileOnly(it)
        testImplementation(it)
    }
    
    testRuntimeOnly("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testRuntimeOnly("org.jetbrains.kotlin:kotlin-script-runtime:$kotlinVersion")
    testRuntimeOnly("org.jetbrains.kotlin:kotlin-annotations-jvm:$kotlinVersion")
    
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-compiler-internal-test-framework:$kotlinVersion")
    testImplementation("junit:junit:4.13.2")
    
    testImplementation(platform("org.junit:junit-bom:5.8.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.platform:junit-platform-commons")
    testImplementation("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.platform:junit-platform-runner")
    testImplementation("org.junit.platform:junit-platform-suite-api")
}

tasks.test {
    dependsOn(project(":plugin-runtime").tasks.getByName("jvmJar"))
    useJUnitPlatform()
    doFirst {
        setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib", "kotlin-stdlib")
        setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib-jdk8", "kotlin-stdlib-jdk8")
        setLibraryProperty("org.jetbrains.kotlin.test.kotlin-reflect", "kotlin-reflect")
        setLibraryProperty("org.jetbrains.kotlin.test.kotlin-test", "kotlin-test")
        setLibraryProperty("org.jetbrains.kotlin.test.kotlin-script-runtime", "kotlin-script-runtime")
        setLibraryProperty("org.jetbrains.kotlin.test.kotlin-annotations-jvm", "kotlin-annotations-jvm")
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        optIn.add("org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI")
    }
}

val generateTests by tasks.creating(JavaExec::class) {
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("dev.lounres.pluginTest.plugin.GenerateTestsKt")
}

val compileTestKotlin by tasks.getting {
    doLast {
        generateTests.exec()
    }
}

fun Test.setLibraryProperty(propName: String, jarName: String) {
    val path = project.configurations
        .testRuntimeClasspath.get()
        .files
        .find { """$jarName-\d.*jar""".toRegex().matches(it.name) }
        ?.absolutePath
        ?: return
    systemProperty(propName, path)
}