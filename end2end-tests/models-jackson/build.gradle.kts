import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val fabrikt: Configuration by configurations.creating

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

dependencies {
    implementation(platform(libs.jackson.bom))
    implementation(libs.jakarta.validation.api)
    implementation(libs.validation.api)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.core)
    implementation(libs.jackson.annotations)
    implementation(libs.jackson.datatype.jsr310)

    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.bundles.junit)
    testImplementation(libs.assertj.core)
}

fun createGenerateCodeTask(name: String, apiFilePath: String, basePackage: String, additionalArgs: List<String> = emptyList()) =
    tasks.create(name, JavaExec::class) {
        inputs.files(file(apiFilePath))
        outputs.dir(generationDir)
        outputs.cacheIf { true }
        classpath = rootProject.files("./build/libs/fabrikt-${rootProject.version}.jar")
        mainClass.set("io.fabrikt.cli.CodeGen")
        args = listOf(
            "--output-directory", generationDir,
            "--base-package", basePackage,
            "--api-file", apiFilePath,
            "--targets", "http_models",
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
    val generateOneOfMarkerInterfaceCodeTask = createGenerateCodeTask(
        "generateOneOfMarkerInterfaceCode",
        "${rootProject.projectDir}/src/test/resources/examples/discriminatedOneOf/api.yaml",
        "com.example.oneof",
    )

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
        dependsOn(generateCodeTask)
        dependsOn(generatePrimitiveTypesCodeTask)
        dependsOn(generateStringFormatOverrideCodeTask)
        dependsOn(generateOneOfMarkerInterfaceCodeTask)
    }

    withType<Test> {
        useJUnitPlatform()
        jvmArgs = listOf("--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.util=ALL-UNNAMED")
    }
}
