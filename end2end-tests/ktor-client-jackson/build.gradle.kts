import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val generationDir = "$buildDir/generated"

sourceSets {
    main { java.srcDirs("$generationDir/src/main/kotlin") }
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

val junitVersion: String by rootProject.extra
val ktorVersion: String by rootProject.extra

dependencies {
    // ktor client
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")

    // ktor test
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    testImplementation("io.mockk:mockk:1.13.7")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.wiremock:wiremock:3.3.1")
    testImplementation("com.marcinziolo:kotlin-wiremock:2.1.1")
}

tasks {
    val generateCode = createGenerateCodeTask(
        "generateKtorCode",
        "src/test/resources/examples/ktorClient/api.yaml"
    )

    val generateMultipartCode = createGenerateCodeTask(
        "generateMultipartCode",
        "src/test/resources/examples/multipartFormData/api.yaml",
        listOf("--base-package", "com.example.multipart")
    )

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        dependsOn(generateCode)
        dependsOn(generateMultipartCode)
    }

    withType<Test> {
        useJUnitPlatform()
        jvmArgs = listOf("--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.util=ALL-UNNAMED")
        dependsOn(generateCode)
        dependsOn(generateMultipartCode)
    }
}

fun createGenerateCodeTask(name: String, apiFilePath: String, additionalArgs: List<String> = emptyList()) =
    tasks.create(name, JavaExec::class) {
    val apiFile = "${rootProject.projectDir}/$apiFilePath"
    inputs.files(apiFile)
    outputs.dir(generationDir)
    outputs.cacheIf { true }
    classpath = rootProject.files("./build/libs/fabrikt-${rootProject.version}.jar")
    mainClass.set("com.cjbooms.fabrikt.cli.CodeGen")
    val basePackage = if (additionalArgs.contains("--base-package")) {
        additionalArgs[additionalArgs.indexOf("--base-package") + 1]
    } else {
        "com.example"
    }
    val filteredAdditionalArgs = additionalArgs.filterNot { it == "--base-package" || it == basePackage }
    args = listOf(
        "--output-directory", generationDir,
        "--base-package", basePackage,
        "--api-file", apiFile,
        "--targets", "http_models",
        "--targets", "client",
        "--http-client-target", "ktor",
        "--serialization-library", "jackson",
        "--validation-library", "no_validation"
    ).plus(filteredAdditionalArgs)
    dependsOn(":jar")
    dependsOn(":shadowJar")
}
