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
    testImplementation(libs.hibernate.validator)
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
    val generateOpenEnumCodeTask = createGenerateCodeTask(
        "generateOpenEnumCode",
        "${rootProject.projectDir}/src/test/resources/examples/openEnum/api.yaml",
        "com.example.openenum",
        listOf("--http-model-opts", "FAULT_TOLERANT_OPEN_ENUMS")
    )
    val generateCompositionCodeTask = createGenerateCodeTask(
        "generateCompositionCode",
        "${rootProject.projectDir}/src/test/resources/examples/sharedCompositionContracts/api.yaml",
        "com.example.composition",
        listOf("--http-model-opts", "SHARED_COMPOSITION_CONTRACTS", "--validation-library", "jakarta_validation", "--serialization-library", "jackson")
    )

    val generateValidationCodeTask = createGenerateCodeTask(
        "generateValidationCode",
        "$projectDir/openapi/validation.yaml",
        "com.example.validation",
        listOf("--validation-library", "jakarta_validation")
    )

    val generateValidationStringOverrideCodeTask = createGenerateCodeTask(
        "generateValidationStringOverrideCode",
        "$projectDir/openapi/validation.yaml",
        "com.example.validationstrings",
        listOf("--validation-library", "jakarta_validation", "--type-overrides", "UUID_AS_STRING")
    )

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
        // Generated models place cascading `@Valid` on container type arguments (e.g. `List<@Valid Foo>`).
        // Kotlin only emits type-argument annotations into bytecode with this flag, which is required for
        // Hibernate Validator to see the cascade at runtime.
        compilerOptions.freeCompilerArgs.add("-Xemit-jvm-type-annotations")
        dependsOn(generateCodeTask)
        dependsOn(generatePrimitiveTypesCodeTask)
        dependsOn(generateStringFormatOverrideCodeTask)
        dependsOn(generateOneOfMarkerInterfaceCodeTask)
        dependsOn(generateOpenEnumCodeTask)
        dependsOn(generateCompositionCodeTask)
        dependsOn(generateValidationCodeTask)
        dependsOn(generateValidationStringOverrideCodeTask)
    }

    withType<Test> {
        useJUnitPlatform()
        jvmArgs = listOf("--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.util=ALL-UNNAMED")
    }
}
