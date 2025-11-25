plugins {
    application
    kotlin("jvm")
    id("com.gradleup.shadow") version "8.3.9"
    id("com.palantir.git-version") version "3.0.0"
}

application {
    mainClass.set("PlaygroundApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

repositories {
    mavenCentral()
}

val ktorVersion: String by rootProject.extra

dependencies {
    implementation(project(":"))

    // ktor server
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-html-builder:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")

    // logging
    implementation("ch.qos.logback:logback-classic:1.5.6")

    implementation("com.squareup:kotlinpoet:1.14.2") { exclude(module = "kotlin-stdlib-jre7") }

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
    dependsOn("shadowJar")
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "PlaygroundApplicationKt"
    }
}

tasks.withType<CreateStartScripts> {
    dependsOn(tasks.shadowJar)
}

tasks.named("shadowJar") {
    dependsOn(":shadowJar")
}

val gitVersion: groovy.lang.Closure<*> by extra
version = gitVersion.call()

val generatedDir = layout.buildDirectory.dir("generated/version")

tasks.register("generateVersionFile") {
    val versionCode = gitVersion.call().toString()
    val outputDir = generatedDir.get().asFile
    inputs.property("gitVersion", versionCode)
    outputs.dir(outputDir)
    doLast {
        val file = outputDir.resolve("Version.kt")
        file.parentFile.mkdirs()
        file.writeText(
            """
            object Version {
                const val GIT_VERSION = "$versionCode"
            }
            """.trimIndent()
        )
    }
}

sourceSets {
    main {
        java.srcDir(generatedDir)
    }
}

tasks.named("compileKotlin") {
    dependsOn("generateVersionFile")
}

tasks.named("shadowJar", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
    archiveBaseName.set("playground")
    archiveClassifier.set("all")
    archiveVersion.set("")
}
