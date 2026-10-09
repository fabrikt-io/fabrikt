import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val generationDir = "$buildDir/generated"
val nullableGenerationDir = "$buildDir/generated-nullable"
val compositionGenerationDir = "$buildDir/generated-composition"
val compositionUnionsGenerationDir = "$buildDir/generated-composition-unions"
val apiFile = "${rootProject.projectDir}/src/test/resources/examples/okHttpClient/api.yaml"
val nullableApiFile = "${rootProject.projectDir}/src/test/resources/examples/customExtensions/api.yaml"

sourceSets {
    main {
        java.srcDirs(
            "$generationDir/src/main/kotlin",
            "$nullableGenerationDir/src/main/kotlin",
            "$compositionGenerationDir/src/main/kotlin",
            "$compositionUnionsGenerationDir/src/main/kotlin",
        )
    }
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
    implementation(platform(libs.jackson3.bom))
    implementation(libs.jackson3.module.kotlin)
    implementation(libs.jackson3.databind)
    implementation(libs.jackson.databind.nullable)
    implementation(libs.okhttp)
    implementation(libs.jakarta.validation.api)

    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.bundles.junit)
    testImplementation(libs.assertj.core)
    testImplementation(libs.bundles.wiremock)
}

tasks {
    val generateCompositionUnionsCode by creating(JavaExec::class) {
        val spec = "${rootProject.projectDir}/src/test/resources/examples/sharedCompositionUnions/api.yaml"
        inputs.files(spec)
        outputs.dir(compositionUnionsGenerationDir)
        classpath = rootProject.files("./build/libs/fabrikt-${rootProject.version}.jar")
        mainClass.set("io.fabrikt.cli.CodeGen")
        args = listOf(
            "--output-directory", compositionUnionsGenerationDir,
            "--base-package", "com.example.compositionunions",
            "--api-file", spec,
            "--targets", "http_models",
            "--serialization-library", "jackson_3",
            "--validation-library", "jakarta_validation",
            "--http-model-opts", "SHARED_COMPOSITION_CONTRACTS",
        )
        dependsOn(":shadowJar")
    }
    val generateCompositionCode by creating(JavaExec::class) {
        val spec = "${rootProject.projectDir}/src/test/resources/examples/sharedCompositionContracts/api.yaml"
        inputs.files(spec)
        outputs.dir(compositionGenerationDir)
        classpath = rootProject.files("./build/libs/fabrikt-${rootProject.version}.jar")
        mainClass.set("io.fabrikt.cli.CodeGen")
        args = listOf(
            "--output-directory", compositionGenerationDir,
            "--base-package", "com.example.composition",
            "--api-file", spec,
            "--targets", "http_models",
            "--serialization-library", "jackson_3",
            "--validation-library", "jakarta_validation",
            "--http-model-opts", "SHARED_COMPOSITION_CONTRACTS",
        )
        dependsOn(":shadowJar")
    }
    val generateCode by creating(JavaExec::class) {
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
            "--targets", "client",
            "--serialization-library", "jackson_3",
        )
        dependsOn(":jar")
        dependsOn(":shadowJar")
    }

    val generateNullableCode by creating(JavaExec::class) {
        inputs.files(nullableApiFile)
        outputs.dir(nullableGenerationDir)
        outputs.cacheIf { true }
        classpath = rootProject.files("./build/libs/fabrikt-${rootProject.version}.jar")
        mainClass.set("io.fabrikt.cli.CodeGen")
        args = listOf(
            "--output-directory", nullableGenerationDir,
            "--base-package", "com.example.nullable",
            "--api-file", nullableApiFile,
            "--targets", "http_models",
            "--serialization-library", "jackson_3",
        )
        dependsOn(":jar")
        dependsOn(":shadowJar")
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
        dependsOn(generateCode)
        dependsOn(generateNullableCode)
        dependsOn(generateCompositionCode)
        dependsOn(generateCompositionUnionsCode)
    }

    withType<Test> {
        useJUnitPlatform()
        jvmArgs = listOf("--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.util=ALL-UNNAMED")
    }
}
