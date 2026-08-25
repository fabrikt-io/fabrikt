import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val generationDir = "$buildDir/generated"
val nullableGenerationDir = "$buildDir/generated-nullable"
val apiFile = "${rootProject.projectDir}/src/test/resources/examples/okHttpClient/api.yaml"
val nullableApiFile = "${rootProject.projectDir}/src/test/resources/examples/customExtensions/api.yaml"

sourceSets {
    main {
        java.srcDirs(
            "$generationDir/src/main/kotlin",
            "$nullableGenerationDir/src/main/kotlin",
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
            "--http-model-opts", "DISABLE_SEALED_INTERFACES_FOR_ONE_OF",
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
    }

    withType<Test> {
        useJUnitPlatform()
        jvmArgs = listOf("--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.util=ALL-UNNAMED")
    }
}
