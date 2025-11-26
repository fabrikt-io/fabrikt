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

group = "com.cjbooms"
val gitVersion: groovy.lang.Closure<*> by extra
version = gitVersion.call()

val projectUrl = "https://github.com/cjbooms/fabrikt"
val projectScmUrl = "scm:https://cjbooms@github.com/cjbooms/fabrikt.git"
val projectScmConUrl = "scm:https://cjbooms@github.com/cjbooms/fabrikt.git"
val projectScmDevUrl = "scm:git://github.com/cjbooms/fabrikt.git"
val projectIssueUrl = "https://github.com/cjbooms/fabrikt/issues"
val projectName = "Fabrikt"
val projectDesc = "Fabricates Kotlin code from OpenApi3 specifications"
val projectLicenseName = "Apache License 2.0"
val projectLicenseUrl = "https://opensource.org/licenses/Apache-2.0"

allprojects {
    repositories {
        mavenCentral()
    }
}

val jacksonVersion by extra { "2.15.1" }
val junitVersion by extra { "5.9.2" }
val ktorVersion by extra { "3.0.1" }
val kotlinxSerializationVersion by extra { "1.9.0" }
val kotlinxDateTimeVersion by extra { "0.7.1-0.6.x-compat" }

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.github.jknack:handlebars:4.3.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
    implementation("com.beust:jcommander:1.82")
    implementation("com.reprezen.kaizen:openapi-parser:4.0.4") { exclude(group = "junit") }
    implementation("com.reprezen.jsonoverlay:jsonoverlay:4.0.4")
    implementation("com.squareup:kotlinpoet:1.14.2") { exclude(module = "kotlin-stdlib-jre7") }
    implementation("com.google.flogger:flogger:0.7.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDateTimeVersion")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation("org.assertj:assertj-core:3.24.2")

    // Below dependencies are solely present so code examples in the test resources dir compile
    testImplementation("javax.validation:validation-api:2.0.1.Final")
    testImplementation("jakarta.validation:jakarta.validation-api:3.0.2")
    testImplementation("org.springframework:spring-webmvc:6.0.9")
    testImplementation("org.springframework.security:spring-security-web:6.1.0")
    testImplementation("io.micronaut:micronaut-core:3.8.7")
    testImplementation("io.micronaut:micronaut-http:3.8.7")
    //testCompileOnly("io.micronaut.security:micronaut-security:3.8.7")
    testImplementation("com.squareup.okhttp3:okhttp:4.10.0")
    testImplementation("org.openapitools:jackson-databind-nullable:0.2.6")
    testImplementation("io.ktor:ktor-server-core:$ktorVersion")
    testImplementation("io.ktor:ktor-server-auth:$ktorVersion")

    testImplementation(platform("com.pinterest.ktlint:ktlint-bom:1.7.1"))
    testImplementation("com.pinterest.ktlint:ktlint-rule-engine-core")
    testImplementation("com.pinterest.ktlint:ktlint-rule-engine")
    testImplementation("com.pinterest.ktlint:ktlint-ruleset-standard")
}

tasks {
    val shadowJar by getting(ShadowJar::class) {
        manifest {
            attributes["Main-Class"] = "com.cjbooms.fabrikt.cli.CodeGen"
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
