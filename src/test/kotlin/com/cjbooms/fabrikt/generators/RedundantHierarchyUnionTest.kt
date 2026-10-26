package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType
import com.cjbooms.fabrikt.cli.SerializationLibrary
import com.cjbooms.fabrikt.cli.ValidationLibrary
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.model.ModelGenerator
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import com.cjbooms.fabrikt.util.YamlObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.net.URLClassLoader
import java.nio.file.Path

class RedundantHierarchyUnionTest {
    @TempDir
    lateinit var directory: Path

    private fun generate(
        library: SerializationLibrary,
        contracts: Boolean = false,
        explicitMappings: Boolean = true,
    ) = run {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.HTTP_MODELS),
            serializationLibrary = library,
            validationLibrary = ValidationLibrary.NO_VALIDATION,
            modelOptions = if (contracts) setOf(ModelCodeGenOptionType.SHARED_COMPOSITION_CONTRACTS) else emptySet(),
        )
        ModelNameRegistry.clear()
        val spec = readTextResource("/examples/redundantHierarchyUnion/api.yaml")
        val root = YamlObjectMapper.instance.readTree(spec)
        if (!explicitMappings) {
            (
                root
                    .path("components")
                    .path("schemas")
                    .path("Base")
                    .path("discriminator") as ObjectNode
            ).remove("mapping")
        }
        ModelGenerator(
            Packages("example"),
            SourceApi(YamlObjectMapper.instance.writeValueAsString(root)),
        ).generate().files
    }

    @ParameterizedTest
    @EnumSource(value = SerializationLibrary::class, names = ["JACKSON_2", "JACKSON_3"])
    fun `repeated unions reuse the discriminated base regardless of alternative order`(library: SerializationLibrary) {
        val files = generate(library).associate { it.name to it.toString() }

        assertThat(files.keys).containsExactlyInAnyOrder("Base", "First", "Second", "Wrapper")
        assertThat(files.getValue("Base")).doesNotContain("Wrapper", "NamedUnion")
        assertThat(files.getValue("Wrapper")).contains(
            "baseFirst: Base?",
            "baseMiddle: Base?",
            "baseLast: Base?",
            "values: List<Base?>?",
            "named: Base?",
        )
    }

    @ParameterizedTest
    @CsvSource("false,true", "true,true", "false,false", "true,false")
    fun `compiled wrapper properties deserialize through the existing discriminator`(
        contracts: Boolean,
        explicitMappings: Boolean,
    ) {
        val source = directory.resolve("source")
        val files = generate(SerializationLibrary.JACKSON_2, contracts, explicitMappings)
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
                *files.map { source.resolve("example/models/${it.name}.kt").toString() }.toTypedArray(),
            )
        assertThat(result).withFailMessage(diagnostics.toString()).isEqualTo(ExitCode.OK)
        URLClassLoader(arrayOf(output.toUri().toURL()), javaClass.classLoader).use { loader ->
            val wrapper = loader.loadClass("example.models.Wrapper")
            val base = loader.loadClass("example.models.Base")
            val mapper =
                com.fasterxml.jackson.databind
                    .ObjectMapper()
                    .registerModule(
                        com.fasterxml.jackson.module.kotlin.KotlinModule
                            .Builder()
                            .build(),
                    )
            listOf("first" to "First", "second" to "Second").forEach { (kind, type) ->
                val discriminator = if (explicitMappings) kind else type
                val value = """{"kind":"$discriminator","id":"example","enabled":true}"""
                val json = """{"baseFirst":$value,"baseMiddle":$value,"baseLast":$value,"values":[$value,null],"named":$value}"""
                val decoded = mapper.readValue(json, wrapper)
                listOf("BaseFirst", "BaseMiddle", "BaseLast", "Named").forEach { property ->
                    val getter = wrapper.getMethod("get$property")
                    assertThat(getter.returnType).isEqualTo(base)
                    val actual = getter.invoke(decoded)
                    assertThat(actual.javaClass.simpleName).isEqualTo(type)
                    assertThat(base.getMethod("getKind").invoke(actual)).isEqualTo(discriminator)
                    assertThat(base.getMethod("getId").invoke(actual)).isEqualTo("example")
                    assertThat(actual.javaClass.getMethod("getEnabled").invoke(actual)).isEqualTo(true)
                }
                val values = wrapper.getMethod("getValues").invoke(decoded) as List<*>
                assertThat(values.first()!!.javaClass.simpleName).isEqualTo(type)
                assertThat(values.last()).isNull()
                assertThat(mapper.readValue(mapper.writeValueAsString(decoded), wrapper)).isEqualTo(decoded)
            }
        }
    }
}
