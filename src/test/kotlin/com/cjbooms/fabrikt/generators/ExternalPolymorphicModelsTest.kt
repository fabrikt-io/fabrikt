package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.SerializationLibrary
import com.cjbooms.fabrikt.cli.ValidationLibrary
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.model.ModelGenerator
import com.cjbooms.fabrikt.model.Models
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.YamlObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.net.URLClassLoader
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class ExternalPolymorphicModelsTest {
    @TempDir
    lateinit var directory: Path

    @Test
    fun selectedSubtypesBringTheirExternalDependencies() {
        val documents = directory.resolve("documents").createDirectories()
        val api = javaClass.getResource("/examples/externalPolymorphicDiscriminator/api.yaml")!!
        val external = javaClass.getResource("/examples/externalPolymorphicDiscriminator/external-models.yaml")!!
        documents.resolve("api.yaml").writeText(api.readText())
        val root = YamlObjectMapper.instance.readTree(external.readText())
        val properties = root.at("/components/schemas/ConcreteImplOne/allOf/1/properties") as ObjectNode
        properties.putObject("extra").put("${'$'}ref", "extra.yaml#/components/schemas/Extra")
        documents.resolve("external-models.yaml").writeText(YamlObjectMapper.instance.writeValueAsString(root))
        documents.resolve("extra.yaml").writeText(
            """
            openapi: 3.0.0
            info: {title: Extra, version: '1'}
            paths: {}
            components:
              schemas:
                Extra:
                  type: object
                  properties:
                    value: {type: string}
                Unrelated: {type: object, properties: {unused: {type: string}}}
            """.trimIndent(),
        )

        val models = generate("api.yaml", SerializationLibrary.JACKSON_2, documents).files.associate { it.name to it.toString() }

        assertThat(models.keys).contains("Extra", "ConcreteImplOne").doesNotContain("Unrelated")
        assertThat(models.getValue("ConcreteImplOne")).contains("extra: Extra?")
    }

    @ParameterizedTest
    @EnumSource(value = SerializationLibrary::class, names = ["JACKSON_2", "JACKSON_3"])
    fun externalAndInlineSchemasGenerateIdenticalModels(library: SerializationLibrary) {
        val external = generate("api.yaml", library).files.associate { it.name to it.toString() }
        val inline = generate("inline.yaml", library).files.associate { it.name to it.toString() }

        assertThat(external.keys).containsExactlyInAnyOrder(
            "Wrapper",
            "PolymorphicEnumDiscriminator",
            "EnumDiscriminator",
            "ConcreteImplOne",
            "ConcreteImplTwo",
            "ConcreteImplThree",
        )
        assertThat(external).isEqualTo(inline)
    }

    @Test
    fun compiledExternalModelsDeserializeEveryDiscriminatorMapping() {
        val source = directory.resolve("source")
        val files = generate("api.yaml", SerializationLibrary.JACKSON_2).files
        files.forEach { it.writeTo(source) }
        val output = directory.resolve("classes")
        val diagnostics = ByteArrayOutputStream()
        val classpath =
            listOf(
                Unit::class.java,
                com.fasterxml.jackson.annotation.JsonTypeInfo::class.java,
                com.fasterxml.jackson.databind.ObjectMapper::class.java,
                com.fasterxml.jackson.core.JsonFactory::class.java,
            ).map {
                Path
                    .of(
                        it.protectionDomain.codeSource.location
                            .toURI(),
                    ).toString()
            }.distinct()
                .joinToString(java.io.File.pathSeparator)
        val result =
            K2JVMCompiler().exec(
                PrintStream(diagnostics),
                "-no-stdlib",
                "-no-reflect",
                "-jvm-target",
                "17",
                "-classpath",
                classpath,
                "-d",
                output.toString(),
                *files.map { source.resolve("audit/models/${it.name}.kt").toString() }.toTypedArray(),
            )
        assertThat(result).withFailMessage(diagnostics.toString()).isEqualTo(ExitCode.OK)
        URLClassLoader(arrayOf(output.toUri().toURL()), javaClass.classLoader).use { loader ->
            val wrapper = loader.loadClass("audit.models.Wrapper")
            val mappings =
                mapOf(
                    "obj_one_only" to "ConcreteImplOne",
                    "obj_two_first" to "ConcreteImplTwo",
                    "obj_two_second" to "ConcreteImplTwo",
                    "obj_three" to "ConcreteImplThree",
                )
            mappings.forEach { (value, expectedType) ->
                val extra = if (expectedType == "ConcreteImplThree") "" else ""","some_prop":"example""""
                val json = """{"polymorph":{"some_enum":"$value"$extra}}"""
                val decoded =
                    com.fasterxml.jackson.databind
                        .ObjectMapper()
                        .registerModule(
                            com.fasterxml.jackson.module.kotlin.KotlinModule
                                .Builder()
                                .build(),
                        ).readValue(json, wrapper)
                val polymorph = wrapper.getMethod("getPolymorph").invoke(decoded)
                assertThat(polymorph.javaClass.simpleName).isEqualTo(expectedType)
                assertThat(
                    polymorph.javaClass
                        .getMethod("getSomeEnum")
                        .invoke(polymorph)
                        .toString(),
                ).isEqualTo(value)
                if (expectedType != "ConcreteImplThree") {
                    assertThat(polymorph.javaClass.getMethod("getSomeProp").invoke(polymorph)).isEqualTo("example")
                }
            }
        }
    }

    private fun generate(
        file: String,
        library: SerializationLibrary,
        documents: Path? = null,
    ): Models {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.HTTP_MODELS),
            serializationLibrary = library,
            validationLibrary = ValidationLibrary.NO_VALIDATION,
        )
        ModelNameRegistry.clear()
        val resource =
            documents?.resolve(file)?.toUri()?.toURL() ?: javaClass.getResource("/examples/externalPolymorphicDiscriminator/$file")!!
        return ModelGenerator(Packages("audit"), SourceApi(resource.readText(), resource.toURI())).generate()
    }
}
