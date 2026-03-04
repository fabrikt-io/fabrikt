import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.2.10"
    id("com.gradleup.shadow") version "8.3.9"
    id("org.jetbrains.dokka") version "1.8.10"
    id("com.palantir.git-version") version "3.0.0"
    id("maven-publish")
    id("signing")

    `java-library` // For API and implementation separation.
}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val executableName = "fabrikt"

group = "io.fabrikt"
val gitVersion: groovy.lang.Closure<*> by extra
version = gitVersion.call()

val projectUrl = "https://github.com/fabrikt-io/fabrikt"
val projectScmUrl = "scm:https://fabrikt-io@github.com/fabrikt-io/fabrikt.git"
val projectScmConUrl = "scm:https://fabrikt-io@github.com/fabrikt-io/fabrikt.git"
val projectScmDevUrl = "scm:git://github.com/fabrikt-io/fabrikt.git"
val projectIssueUrl = "https://github.com/fabrikt-io/fabrikt/issues"
val projectName = "Fabrikt"
val projectDesc = "Fabricates Kotlin code from OpenApi3 specifications"
val projectLicenseName = "Apache License 2.0"
val projectLicenseUrl = "https://opensource.org/licenses/Apache-2.0"

allprojects {
    repositories {
        mavenCentral()
    }
}

dependencies {
    implementation(platform(libs.jackson.bom))
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.handlebars)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.core)
    implementation(libs.jackson.annotations)
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.jcommander)
    implementation(libs.openapi.parser) { exclude(group = "junit") }
    implementation(libs.jsonoverlay)
    implementation(libs.kotlinpoet) { exclude(module = "kotlin-stdlib-jre7") }
    implementation(libs.flogger)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.kotlinx.datetime)

    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.bundles.junit)
    testImplementation(libs.assertj.core)

    // Below dependencies are solely present so code examples in the test resources dir compile
    testImplementation(libs.validation.api)
    testImplementation(libs.jakarta.validation.api)
    testImplementation(libs.spring.webmvc)
    testImplementation(libs.spring.security.web)
    testImplementation(libs.micronaut.core)
    testImplementation(libs.micronaut.http)
    testImplementation(libs.okhttp)
    testImplementation(libs.jackson.databind.nullable)
    testImplementation(libs.ktor.server.core)
    testImplementation(libs.ktor.server.auth)

    testImplementation(platform(libs.ktlint.bom))
    testImplementation(libs.ktlint.rule.engine.core)
    testImplementation(libs.ktlint.rule.engine)
    testImplementation(libs.ktlint.ruleset.standard)
}

tasks {
    val shadowJar by getting(ShadowJar::class) {
        manifest {
            attributes["Main-Class"] = "io.fabrikt.cli.CodeGen"
            attributes["Implementation-Title"] = "fabrikt"
            attributes["Implementation-Version"] = project.version
            attributes["Built-JDK"] = System.getProperty("java.version")
            attributes["Built-Gradle"] = gradle.gradleVersion
        }
        archiveBaseName.set(executableName)
        archiveClassifier.set("")
        // relocate the transitive dependency on an old guava version to prevent conflicts (https://github.com/cjbooms/fabrikt/issues/379)
        relocate("com.google.common", "com.cjbooms.fabrikt.shaded.com.google.common")
        relocate("com.google.thirdparty", "com.cjbooms.fabrikt.shaded.com.google.thirdparty")
    }

    val dokka = getByName<DokkaTask>("dokkaHtml") {
        outputDirectory.set(file("$buildDir/dokka"))
    }

    create("sourcesJar", Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.getByName("main").allSource)
    }

    create("kotlinDocJar", Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Assembles Kotlin docs with Dokka"
        archiveClassifier.set("javadoc")
        from(dokka)
        dependsOn(dokka)
    }

    create("printCodeGenUsage", JavaExec::class) {
        dependsOn(shadowJar)
        classpath = project.files("./build/libs/$executableName-$version.jar")
        mainClass.set("com.cjbooms.fabrikt.cli.CodeGen")
        args = listOf("--help")
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            optIn.add("kotlin.time.ExperimentalTime")
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    withType<Test> {
        useJUnitPlatform()
        jvmArgs = listOf("--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.util=ALL-UNNAMED")
    }
}

publishing {
    repositories {
        maven {
            name = "ossrh-staging-api"
            url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("OSSRH_USER_TOKEN_USERNAME")
                password = System.getenv("OSSRH_USER_TOKEN_PASSWORD")
            }
        }
    }

    publications {
        create<MavenPublication>("fabrikt") {
            artifact(tasks["shadowJar"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["kotlinDocJar"])

            pom {
                name.set(projectName)
                description.set(projectDesc)
                url.set(projectUrl)
                inceptionYear.set("2020")
                licenses {
                    license {
                        name.set(projectLicenseName)
                        url.set(projectLicenseUrl)
                    }
                }
                developers {
                    developer {
                        id.set("cjbooms")
                        name.set("Conor Gallagher")
                        email.set("cjbooms@gmail.com")
                    }
                    developer {
                        id.set("averabaq")
                        name.set("Alejandro Vera-Baquero")
                        email.set("averabaq@gmail.com")
                    }
                }
                scm {
                    connection.set(projectScmConUrl)
                    developerConnection.set(projectScmDevUrl)
                    url.set(projectScmUrl)
                }
            }
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)

    sign(publishing.publications["fabrikt"])
}
