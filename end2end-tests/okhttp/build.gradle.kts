import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val fabrikt: Configuration by configurations.creating

val generationDir = "$buildDir/generated"
val basicGenerationDir = "$buildDir/generated-basic"
val apiFile = "${rootProject.projectDir}/src/test/resources/examples/okHttpClient/api.yaml"

sourceSets {
    main { java.srcDirs("$generationDir/src/main/kotlin", "$basicGenerationDir/src/main/kotlin") }
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
    implementation(libs.okhttp)
    implementation(libs.jakarta.validation.api)
    implementation(libs.validation.api)
    implementation(libs.jackson3.module.kotlin)
    implementation(libs.jackson3.databind)

    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.bundles.junit)
    testImplementation(libs.assertj.core)
    testImplementation(libs.bundles.wiremock)
}

tasks {
    val generateBasicCode by creating(JavaExec::class) {
        val spec = rootProject.file("src/test/resources/examples/headerApiKeySecurity/api.yaml")
        inputs.file(spec)
        outputs.dir(basicGenerationDir)
        classpath = rootProject.files("./build/libs/fabrikt-${rootProject.version}.jar")
        mainClass.set("io.fabrikt.cli.CodeGen")
        args = listOf(
            "--output-directory", basicGenerationDir,
            "--base-package", "com.example.basic",
            "--api-file", spec.path,
            "--targets", "client",
            "--http-client-target", "ok_http",
            "--serialization-library", "jackson_3",
            "--http-client-opts", "OPENAPI_BASIC_AUTHENTICATION",
            "--http-client-opts", "OPENAPI_API_KEY_AUTHENTICATION",
            "--http-client-opts", "OPENAPI_BEARER_AUTHENTICATION",
            "--http-client-opts", "OPENAPI_OAUTH2_AUTHENTICATION",
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
        )
        dependsOn(":jar")
        dependsOn(":shadowJar")
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
        dependsOn(generateCode, generateBasicCode)
    }


    withType<Test> {
        useJUnitPlatform()
        jvmArgs = listOf("--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.util=ALL-UNNAMED")

    }
}
