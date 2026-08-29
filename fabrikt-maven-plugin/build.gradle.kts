plugins {
    id("org.gradlex.maven-plugin-development") version "1.0.3"
    `maven-publish`
    signing
}

group = rootProject.group
version = rootProject.version
description = "Official Maven plugin for Fabrikt code generation"
val mavenVersion = libs.maven.plugin.api.get().versionConstraint.requiredVersion

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}

val pluginDescriptorDependencies by configurations.creating
pluginDescriptorDependencies.isTransitive = false
val mavenDistribution by configurations.creating

val integrationTestSourceSet = sourceSets.create("integrationTest")
configurations[integrationTestSourceSet.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[integrationTestSourceSet.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())

dependencies {
    compileOnly(libs.maven.plugin.api)
    compileOnly(libs.maven.core)
    compileOnly(libs.maven.plugin.annotations)
    runtimeOnly(project(":"))
    pluginDescriptorDependencies(project(":"))

    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.bundles.junit)
    testImplementation(libs.assertj.core)
    testImplementation(libs.maven.core)

    mavenDistribution("org.apache.maven:apache-maven:$mavenVersion:bin@zip")
}

mavenPlugin {
    goalPrefix.set("fabrikt")
    dependencies.set(pluginDescriptorDependencies)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    dependsOn(rootProject.tasks.named("shadowJar"))
}

val extractMaven by tasks.registering(Sync::class) {
    from(provider { zipTree(mavenDistribution.singleFile) })
    into(layout.buildDirectory.dir("maven-distribution"))
}

val integrationTestRepository = layout.buildDirectory.dir("integration-test-repository")
val fabriktJar = rootProject.tasks.named<AbstractArchiveTask>("shadowJar").flatMap { it.archiveFile }
val fabriktPom = rootProject.layout.buildDirectory.file("publications/fabrikt/pom-default.xml")
val pluginJar = tasks.named<AbstractArchiveTask>("jar").flatMap { it.archiveFile }
val pluginPom = layout.buildDirectory.file("publications/mavenPlugin/pom-default.xml")

val prepareIntegrationTestRepository by tasks.registering {
    dependsOn(
        rootProject.tasks.named("shadowJar"),
        rootProject.tasks.named("generatePomFileForFabriktPublication"),
        tasks.named("jar"),
        tasks.named("generatePomFileForMavenPluginPublication"),
    )
    inputs.files(fabriktJar, fabriktPom, pluginJar, pluginPom)
    outputs.dir(integrationTestRepository)

    doLast {
        val repository = integrationTestRepository.get().asFile
        val artifactVersion = project.version.toString()
        val fabriktDirectory = repository.resolve("io/fabrikt/fabrikt/$artifactVersion")
        val pluginDirectory = repository.resolve("io/fabrikt/fabrikt-maven-plugin/$artifactVersion")

        copy {
            from(fabriktJar)
            into(fabriktDirectory)
            rename { "fabrikt-$artifactVersion.jar" }
        }
        copy {
            from(fabriktPom)
            into(fabriktDirectory)
            rename { "fabrikt-$artifactVersion.pom" }
        }
        copy {
            from(pluginJar)
            into(pluginDirectory)
            rename { "fabrikt-maven-plugin-$artifactVersion.jar" }
        }
        copy {
            from(pluginPom)
            into(pluginDirectory)
            rename { "fabrikt-maven-plugin-$artifactVersion.pom" }
        }
    }
}

val integrationTest by tasks.registering(Test::class) {
    description = "Runs the Maven plugin against real Maven consumer projects."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    testClassesDirs = integrationTestSourceSet.output.classesDirs
    classpath = integrationTestSourceSet.runtimeClasspath
    dependsOn(prepareIntegrationTestRepository, extractMaven)
    shouldRunAfter(tasks.test)
    systemProperty("fabrikt.plugin.version", project.version.toString())
    systemProperty("fabrikt.test.repository", integrationTestRepository.get().asFile.toURI())
    systemProperty(
        "fabrikt.maven.home",
        layout.buildDirectory.dir("maven-distribution/apache-maven-$mavenVersion").get().asFile,
    )
}

tasks.check {
    dependsOn(integrationTest)
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
        create<MavenPublication>("mavenPlugin") {
            from(components["java"])

            pom {
                name.set("Fabrikt Maven Plugin")
                description.set(project.description)
                url.set("https://github.com/fabrikt-io/fabrikt")
                inceptionYear.set("2020")
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://opensource.org/licenses/Apache-2.0")
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
                    connection.set("scm:https://fabrikt-io@github.com/fabrikt-io/fabrikt.git")
                    developerConnection.set("scm:git://github.com/fabrikt-io/fabrikt.git")
                    url.set("scm:https://fabrikt-io@github.com/fabrikt-io/fabrikt.git")
                }
            }
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["mavenPlugin"])
}
