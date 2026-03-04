import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val fabrikt: Configuration by configurations.creating

val generationDir = "$buildDir/generated"

sourceSets {
    main { java.srcDirs("$generationDir/src/main/kotlin") }
    test { java.srcDirs("$generationDir/src/test/kotlin") }
}

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.1.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(platform(libs.jackson.bom))
    implementation(libs.jakarta.validation.api)
    implementation(libs.validation.api)
    implementation(libs.kotlinx.datetime.legacy)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.core)
    implementation(libs.jackson.annotations)
    implementation(libs.jackson.datatype.jsr310)

    // ktor server
    implementation(libs.ktor.server.content.negotiation.jvm)
    implementation(libs.ktor.serialization.jackson)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.data.conversion)

    // ktor test
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.server.auth)
    testImplementation(libs.ktor.server.auth.jwt)
    testImplementation(libs.ktor.server.status.pages)
    testImplementation(libs.kotlin.test)

    testImplementation(libs.mockk)

    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.bundles.junit)
    testImplementation(libs.assertj.core)

    testImplementation(libs.logback.classic.legacy)
}

tasks {
    val generateKtorCode = createCodeGenerationTask(
        "generateKtorCode",
        "src/test/resources/examples/githubApi/api.yaml"
    )

    val generateKtorAuthCode = createCodeGenerationTask(
        "generateKtorAuthCode",
        "src/test/resources/examples/authentication/api.yaml",
        listOf("--http-controller-opts", "AUTHENTICATION")
    )

    val generateKtorInstantDateTimeCode = createCodeGenerationTask(
        "generateKtorInstantDateTimeCode",
        "src/test/resources/examples/instantDateTime/api.yaml",
        listOf("--serialization-library", "KOTLINX_SERIALIZATION")
    )

    val generateKtorQueryParametersCode = createCodeGenerationTask(
        "generateKtorQueryParametersCode",
        "src/test/resources/examples/queryParameters/api.yaml",
        listOf("--serialization-library", "KOTLINX_SERIALIZATION")
    )

    val generateKtorPathParametersCode = createCodeGenerationTask(
        "generateKtorPathParametersCode",
        "src/test/resources/examples/pathParameters/api.yaml",
        listOf("--serialization-library", "KOTLINX_SERIALIZATION")
    )

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            optIn.add("kotlin.time.ExperimentalTime")
            jvmTarget.set(JvmTarget.JVM_17)
        }
        dependsOn(generateKtorCode)
        dependsOn(generateKtorAuthCode)
        dependsOn(generateKtorInstantDateTimeCode)
        dependsOn(generateKtorQueryParametersCode)
        dependsOn(generateKtorPathParametersCode)
    }


    withType<Test> {
        useJUnitPlatform()
        jvmArgs = listOf("--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.util=ALL-UNNAMED")
    }
}

fun TaskContainer.createCodeGenerationTask(
    name: String, apiFilePath: String, opts: List<String> = emptyList()
): TaskProvider<JavaExec> = register(name, JavaExec::class) {
    val apiFile = "${rootProject.projectDir}/$apiFilePath"
    inputs.files(apiFile)
    outputs.dir(generationDir)
    outputs.cacheIf { true }
    classpath = rootProject.files("./build/libs/fabrikt-${rootProject.version}.jar")
    mainClass.set("io.fabrikt.cli.CodeGen")
    args = listOf(
        "--output-directory", generationDir,
        "--base-package", "com.example",
        "--api-file", apiFile,
        "--targets", "http_models",
        "--targets", "controllers",
        "--http-controller-target", "ktor",
        "--instant-library", "KOTLINX_INSTANT",
        "--http-model-opts", "DISABLE_SEALED_INTERFACES_FOR_ONE_OF"
    ) + opts
    dependsOn(":jar")
    dependsOn(":shadowJar")
}
