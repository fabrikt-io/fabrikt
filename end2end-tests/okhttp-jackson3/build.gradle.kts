import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val generationDir = "$buildDir/generated"
val nullableGenerationDir = "$buildDir/generated-nullable"
val compositionGenerationDir = "$buildDir/generated-composition"
val compositionUnionsGenerationDir = "$buildDir/generated-composition-unions"
val compositionExternalGenerationDir = "$buildDir/generated-composition-external"
val compositionRefinementsGenerationDir = "$buildDir/generated-composition-refinements"
val compositionRequestProjectionGenerationDir = "$buildDir/generated-composition-requestprojection"
val compositionResponseProjectionGenerationDir = "$buildDir/generated-composition-responseprojection"
val apiFile = "${rootProject.projectDir}/src/test/resources/examples/okHttpClient/api.yaml"
val nullableApiFile = "${rootProject.projectDir}/src/test/resources/examples/customExtensions/api.yaml"

sourceSets {
    main {
        java.srcDirs(
            "$generationDir/src/main/kotlin",
            "$nullableGenerationDir/src/main/kotlin",
            "$compositionGenerationDir/src/main/kotlin",
            "$compositionUnionsGenerationDir/src/main/kotlin",
            "$compositionExternalGenerationDir/src/main/kotlin",
            "$compositionRefinementsGenerationDir/src/main/kotlin",
            "$compositionRequestProjectionGenerationDir/src/main/kotlin",
            "$compositionResponseProjectionGenerationDir/src/main/kotlin",
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
    val generateCompositionRefinementsCode by creating(JavaExec::class) {
        val spec = "${rootProject.projectDir}/src/test/resources/examples/sharedCompositionRefinements/api.yaml"
        inputs.files(spec)
        outputs.dir(compositionRefinementsGenerationDir)
        classpath = rootProject.files("./build/libs/fabrikt-${rootProject.version}.jar")
        mainClass.set("io.fabrikt.cli.CodeGen")
        args = listOf(
            "--output-directory", compositionRefinementsGenerationDir,
            "--base-package", "com.example.compositionrefinements",
            "--api-file", spec,
            "--targets", "http_models",
            "--serialization-library", "jackson_3",
            "--validation-library", "jakarta_validation",
            "--http-model-opts", "SHARED_COMPOSITION_CONTRACTS",

        )
        dependsOn(":shadowJar")
    }
    val generateCompositionRequestProjectionCode by creating(JavaExec::class) {
        val spec = "${rootProject.projectDir}/src/test/resources/examples/sharedCompositionRequestProjection/api.yaml"
        inputs.files(spec)
        outputs.dir(compositionRequestProjectionGenerationDir)
        classpath = rootProject.files("./build/libs/fabrikt-${rootProject.version}.jar")
        mainClass.set("io.fabrikt.cli.CodeGen")
        args = listOf(
            "--output-directory", compositionRequestProjectionGenerationDir,
            "--base-package", "com.example.compositionrequestprojection",
            "--api-file", spec,
            "--targets", "http_models",
            "--serialization-library", "jackson_3",
            "--validation-library", "jakarta_validation",
            "--http-model-opts", "SHARED_COMPOSITION_CONTRACTS",
            "--http-model-opts", "EXCLUDE_READ_ONLY",
        )
        dependsOn(":shadowJar")
    }
    val generateCompositionResponseProjectionCode by creating(JavaExec::class) {
        val spec = "${rootProject.projectDir}/src/test/resources/examples/sharedCompositionResponseProjection/api.yaml"
        inputs.files(spec)
        outputs.dir(compositionResponseProjectionGenerationDir)
        classpath = rootProject.files("./build/libs/fabrikt-${rootProject.version}.jar")
        mainClass.set("io.fabrikt.cli.CodeGen")
        args = listOf(
            "--output-directory", compositionResponseProjectionGenerationDir,
            "--base-package", "com.example.compositionresponseprojection",
            "--api-file", spec,
            "--targets", "http_models",
            "--serialization-library", "jackson_3",
            "--validation-library", "jakarta_validation",
            "--http-model-opts", "SHARED_COMPOSITION_CONTRACTS",
            "--http-model-opts", "EXCLUDE_WRITE_ONLY",
        )
        dependsOn(":shadowJar")
    }
    val generateCompositionExternalCode by creating(JavaExec::class) {
        val specDir = "${rootProject.projectDir}/src/test/resources/examples/sharedCompositionExternal"
        inputs.files(fileTree(specDir) { include("**/*.yaml") })
        outputs.dir(compositionExternalGenerationDir)
        classpath = rootProject.files("./build/libs/fabrikt-${rootProject.version}.jar")
        mainClass.set("io.fabrikt.cli.CodeGen")
        args = listOf(
            "--output-directory", compositionExternalGenerationDir,
            "--base-package", "com.example.compositionexternal",
            "--api-file", "$specDir/api.yaml",
            "--targets", "http_models",
            "--serialization-library", "jackson_3",
            "--validation-library", "jakarta_validation",
            "--http-model-opts", "SHARED_COMPOSITION_CONTRACTS",
        )
        dependsOn(":shadowJar")
    }
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
        dependsOn(generateCompositionExternalCode)
        dependsOn(generateCompositionRefinementsCode)
        dependsOn(generateCompositionRequestProjectionCode)
        dependsOn(generateCompositionResponseProjectionCode)
    }

    withType<Test> {
        useJUnitPlatform()
        jvmArgs = listOf("--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.util=ALL-UNNAMED")
    }
}
