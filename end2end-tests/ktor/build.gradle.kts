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

val jacksonVersion: String by rootProject.extra
val junitVersion: String by rootProject.extra
val ktorVersion: String by rootProject.extra

dependencies {
    implementation(platform("com.fasterxml.jackson:jackson-bom:$jacksonVersion"))
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("javax.validation:validation-api:2.0.1.Final")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // ktor server
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-data-conversion:$ktorVersion")

    // ktor test
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-server-auth:$ktorVersion")
    testImplementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    testImplementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.20")

    testImplementation("io.mockk:mockk:1.13.7")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation("org.assertj:assertj-core:3.24.2")

    testImplementation("ch.qos.logback:logback-classic:1.4.3")
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
    mainClass.set("com.cjbooms.fabrikt.cli.CodeGen")
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
