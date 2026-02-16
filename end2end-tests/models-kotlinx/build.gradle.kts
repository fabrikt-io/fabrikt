import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val fabrikt: Configuration by configurations.creating

val generationDir = "$buildDir/generated"

sourceSets {
    main { java.srcDirs("$generationDir/src/main/kotlin") }
    test { java.srcDirs("$generationDir/src/test/kotlin") }
}

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.0.20"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val junitVersion: String by rootProject.extra
val kotlinxSerializationVersion: String by rootProject.extra
val kotlinxDateTimeVersion: String by rootProject.extra

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDateTimeVersion")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation("org.assertj:assertj-core:3.24.2")
}

fun createGenerateCodeTask(name: String, apiFilePath: String, basePackage: String, additionalArgs: List<String> = emptyList()) =
    tasks.create(name, JavaExec::class) {
        inputs.files(file(apiFilePath))
        outputs.dir(generationDir)
        outputs.cacheIf { true }
        classpath = rootProject.files("./build/libs/fabrikt-${rootProject.version}.jar")
        mainClass.set("com.cjbooms.fabrikt.cli.CodeGen")
        args = listOf(
            "--output-directory", generationDir,
            "--base-package", basePackage,
            "--api-file", apiFilePath,
            "--validation-library", "NO_VALIDATION",
            "--targets", "http_models",
            "--serialization-library", "KOTLINX_SERIALIZATION",
            "--instant-library", "KOTLIN_TIME_INSTANT",
        ).plus(additionalArgs)
        dependsOn(":jar")
        dependsOn(":shadowJar")
    }

tasks {
    val generateCodeTask = createGenerateCodeTask(
        "generateCode",
        "$projectDir/openapi/api.yaml",
        "com.example"
    )
    val generatePrimitiveTypesCodeTask = createGenerateCodeTask(
        "generatePrimitiveTypesCode",
        "${rootProject.projectDir}/src/test/resources/examples/primitiveTypes/api.yaml",
        "com.example.primitives"
    )
    val generateStringFormatOverrideCodeTask = createGenerateCodeTask(
        "generateStringFormatOverrideCode",
        "${rootProject.projectDir}/src/test/resources/examples/primitiveTypes/api.yaml",
        "com.example.stringformat",
        listOf(
            "--type-overrides", "UUID_AS_STRING",
            "--type-overrides", "URI_AS_STRING",
            "--type-overrides", "BYTE_AS_STRING",
            "--type-overrides", "BINARY_AS_STRING",
            "--type-overrides", "DATE_AS_STRING",
            "--type-overrides", "DATETIME_AS_STRING"
        )
    )

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            optIn.add("kotlinx.serialization.ExperimentalSerializationApi")
            optIn.add("kotlin.time.ExperimentalTime")
            jvmTarget.set(JvmTarget.JVM_17)
        }
        dependsOn(generateCodeTask)
        dependsOn(generatePrimitiveTypesCodeTask)
        dependsOn(generateStringFormatOverrideCodeTask)
    }

    withType<Test> {
        useJUnitPlatform()
        jvmArgs = listOf("--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.util=ALL-UNNAMED")
    }
}
