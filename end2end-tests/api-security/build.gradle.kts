import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val generationDir = "$buildDir/generated"
val apiFile = "${rootProject.projectDir}/src/test/resources/examples/apiSecurity/api.yaml"

sourceSets {
    main { java.srcDirs("$generationDir/src/main/kotlin") }
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
            "--targets", "client",
            "--http-client-opts", "OPENAPI_SECURITY",
        )
        dependsOn(":jar")
        dependsOn(":shadowJar")
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
        dependsOn(generateCode)
    }

    withType<Test> {
        useJUnitPlatform()
    }
}
