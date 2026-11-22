import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
}

val fabriktCliJar = rootProject.tasks.named("shadowJar").map { it.outputs.files.singleFile }
val generationDir = layout.buildDirectory.dir("generated")
val apiFile = rootProject.file("src/test/resources/examples/queryOperation/api.yaml")

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
    sourceSets.test {
        kotlin.srcDir(generationDir.map { it.dir("src/main/kotlin") })
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc:4.2.0")
    testImplementation(libs.jackson3.module.kotlin)
    testImplementation(libs.bundles.junit)
    testRuntimeOnly(libs.junit.platform.launcher)
}

val generateCode by tasks.registering(JavaExec::class) {
    inputs.file(apiFile)
    outputs.dir(generationDir)
    classpath = rootProject.files(fabriktCliJar)
    mainClass.set("io.fabrikt.cli.CodeGen")
    args(
        "--output-directory", generationDir.get().asFile.absolutePath,
        "--base-package", "com.example",
        "--api-file", apiFile.absolutePath,
        "--targets", "http_models",
        "--targets", "controllers",
        "--http-controller-target", "SPRING",
        "--serialization-library", "jackson_3",
        "--validation-library", "no_validation",
    )
}

tasks.compileTestKotlin {
    dependsOn(generateCode)
}

tasks.test {
    useJUnitPlatform()
}
