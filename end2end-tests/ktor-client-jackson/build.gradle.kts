import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val fabriktCliJar = rootProject.tasks.named("shadowJar").map { it.outputs.files.singleFile }

val generationDir = "$buildDir/generated"
val securityGenerationDir = "$buildDir/generated-security"

sourceSets {
    main { java.srcDirs("$generationDir/src/main/kotlin", "$securityGenerationDir/src/main/kotlin") }
    test { java.srcDirs("$generationDir/src/test/kotlin") }
}

plugins {
    kotlin("jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // ktor client
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.jackson)

    // ktor test
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.mockk)

    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.bundles.junit)
    testImplementation(libs.assertj.core)
    testImplementation(libs.bundles.wiremock)
}

tasks {
    val generateCode = createGenerateCodeTask(
        "generateKtorCode",
        "src/test/resources/examples/ktorClient/api.yaml"
    )

    val generateSecurityCode =
        createGenerateCodeTask(
            "generateKtorSecurityCode",
            "src/test/resources/examples/apiSecurity/api.yaml",
            basePackage = "com.example.apisecurity",
            outputDir = securityGenerationDir,
            additionalArgs = listOf("--http-client-opts", "OPENAPI_SECURITY"),
        )

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        dependsOn(generateCode)
        dependsOn(generateSecurityCode)
    }

    withType<Test> {
        useJUnitPlatform()
        jvmArgs = listOf("--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.util=ALL-UNNAMED")
        dependsOn(generateCode)
        dependsOn(generateSecurityCode)
    }
}

fun createGenerateCodeTask(
    name: String,
    apiFilePath: String,
    basePackage: String = "com.example",
    outputDir: String = generationDir,
    additionalArgs: List<String> = emptyList(),
) = tasks.create(name, JavaExec::class) {
    val apiFile = "${rootProject.projectDir}/$apiFilePath"
    inputs.files(apiFile)
    outputs.dir(outputDir)
    outputs.cacheIf { true }
    classpath = rootProject.files(fabriktCliJar)
    mainClass.set("io.fabrikt.cli.CodeGen")
    args = listOf(
        "--output-directory", outputDir,
        "--base-package", basePackage,
        "--api-file", apiFile,
        "--targets", "http_models",
        "--targets", "client",
        "--http-client-target", "ktor",
        "--serialization-library", "jackson",
        "--validation-library", "no_validation",
    ).plus(additionalArgs)
    dependsOn(":jar")
    dependsOn(":shadowJar")
}
