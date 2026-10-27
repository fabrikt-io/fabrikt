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
import com.squareup.kotlinpoet.FileSpec
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.net.URLClassLoader
import java.nio.file.Path
import java.util.logging.Handler
import java.util.logging.LogRecord
import java.util.logging.Logger

class RedundantHierarchyUnionTest {
    @TempDir
    lateinit var directory: Path

    private fun generate(
        library: SerializationLibrary,
        selfMapping: Boolean = false,
        contracts: Boolean = false,
        unions: Boolean = true,
        explicitMappings: Boolean = true,
        open: Boolean = false,
        sealed: Boolean = false,
        change: (ObjectNode) -> Unit = {},
    ) = run {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.HTTP_MODELS),
            serializationLibrary = library,
            validationLibrary = ValidationLibrary.NO_VALIDATION,
            modelOptions =
                buildSet {
                    if (contracts) add(ModelCodeGenOptionType.SHARED_COMPOSITION_CONTRACTS)
                    if (open) add(ModelCodeGenOptionType.OPEN_CLASSES_FOR_ALL_OF)
                    if (sealed) add(ModelCodeGenOptionType.SEALED_CLASSES_FOR_ALL_OF)
                },
        )
        ModelNameRegistry.clear()
        val spec = YamlObjectMapper.instance.readTree(readTextResource("/examples/redundantHierarchyUnion/api.yaml"))
        if (!explicitMappings) {
            (spec.at("/components/schemas/Base/discriminator") as ObjectNode).remove("mapping")
        }
        if (selfMapping) {
            (spec.at("/components/schemas/Base/discriminator/mapping") as ObjectNode)
                .put("base", "#/components/schemas/Base")
        }
        if (!unions) {
            (spec.at("/components/schemas") as ObjectNode).remove(listOf("NamedUnion", "Wrapper"))
        }
        change(spec as ObjectNode)
        ModelGenerator(
            Packages("example"),
            SourceApi(YamlObjectMapper.instance.writeValueAsString(spec)),
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
        withCompiledModels(generate(SerializationLibrary.JACKSON_2, contracts = contracts, explicitMappings = explicitMappings)) { loader ->
            val wrapper = loader.loadClass("example.models.Wrapper")
            val base = loader.loadClass("example.models.Base")
            val mapper =
                com.fasterxml.jackson.databind.ObjectMapper().registerModule(
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

    @ParameterizedTest
    @EnumSource(value = SerializationLibrary::class, names = ["JACKSON_2", "JACKSON_3", "KOTLINX_SERIALIZATION"])
    fun `sealed is the default and wins conflicts while kotlinx ignores open`(library: SerializationLibrary) {
        val files = generate(library, selfMapping = true).associate { it.name to it.toString() }
        assertThat(files.getValue("Base")).contains("sealed class Base")
        assertThat(generate(library, selfMapping = true, sealed = true).map { it.toString() }).containsExactlyElementsOf(files.values)
        assertThat(generate(library, open = true, sealed = true).first { it.name == "Base" }.toString()).contains("sealed class Base")
        assertThat(generate(library, unions = false, open = true).first { it.name == "Base" }.toString())
            .contains(if (library == SerializationLibrary.KOTLINX_SERIALIZATION) "sealed class Base" else "open class Base")
    }

    @ParameterizedTest
    @CsvSource("false,false", "false,true", "true,false", "true,true")
    fun `concrete parent values and children deserialize with explicit or implicit mappings`(
        selfMapping: Boolean,
        contracts: Boolean,
    ) {
        withCompiledModels(generate(SerializationLibrary.JACKSON_2, selfMapping, contracts, open = true)) { loader ->
            val base = loader.loadClass("example.models.Base")
            val wrapper = loader.loadClass("example.models.Wrapper")
            val mapper =
                com.fasterxml.jackson.databind.ObjectMapper().registerModule(
                    com.fasterxml.jackson.module.kotlin.KotlinModule
                        .Builder()
                        .build(),
                )
            val parentKind = if (selfMapping) "base" else "Base"
            listOf(parentKind to "Base", "first" to "First", "second" to "Second").forEach { (kind, type) ->
                val value = """{"kind":"$kind","id":"Alice","description":null}"""
                val json = """{"baseFirst":$value,"values":[$value,null]}"""
                val decoded = mapper.readValue(json, wrapper)
                assertThat(mapper.readValue(value, base).javaClass.simpleName).isEqualTo(type)
                val actual = wrapper.getMethod("getBaseFirst").invoke(decoded)
                assertThat(actual.javaClass.simpleName).isEqualTo(type)
                assertThat(base.getMethod("getKind").invoke(actual)).isEqualTo(kind)
                val roundTrip = mapper.readValue(mapper.writeValueAsString(decoded), wrapper)
                val restored = wrapper.getMethod("getBaseFirst").invoke(roundTrip)
                assertThat(restored.javaClass).isEqualTo(actual.javaClass)
                assertThat(restored).isEqualTo(actual)
                assertThat(restored.hashCode()).isEqualTo(actual.hashCode())
                assertThat(base.getMethod("getId").invoke(restored)).isEqualTo("Alice")
                assertThat(base.getMethod("getDescription").invoke(restored)).isNull()
            }
            assertThatThrownBy { mapper.readValue("""{"kind":"unknown","id":"Alice"}""", base) }
                .isInstanceOf(com.fasterxml.jackson.databind.exc.InvalidTypeIdException::class.java)
            assertThatThrownBy { mapper.readValue("""{"id":"Alice"}""", base) }
                .isInstanceOf(com.fasterxml.jackson.databind.exc.InvalidTypeIdException::class.java)
            val parent = mapper.readValue("""{"kind":"$parentKind","id":"Alice"}""", base)
            val first = mapper.readValue("""{"kind":"first","id":"Alice","enabled":true}""", base)
            val different = mapper.readValue("""{"kind":"first","id":"Alice","enabled":false}""", base)
            assertThat(parent.equals(first)).isFalse()
            assertThat(first.equals(parent)).isFalse()
            val sameProperties = base.constructors.first { it.parameterCount == 3 }.newInstance("Alice", null, "first")
            assertThat(sameProperties.equals(first)).isFalse()
            assertThat(first.equals(sameProperties)).isFalse()
            assertThat(first).isNotEqualTo(different)
            assertThat(setOf(first, different)).hasSize(2)
            assertThat(parent.toString()).contains("Base(", "id=Alice", "description=null")
            assertThat(first.toString()).contains("enabled=true")
            if (contracts) assertThat(loader.loadClass("example.models.BaseComposite").isInstance(parent)).isTrue()
        }
    }

    @Test
    fun `open parents work without unions and default their discriminator`() {
        withCompiledModels(generate(SerializationLibrary.JACKSON_2, unions = false, open = true)) { loader ->
            val base = loader.loadClass("example.models.Base")
            val constructed =
                base.constructors
                    .first { it.parameterCount == 5 }
                    .newInstance("Alice", null, null, 6, null)
            assertThat(base.getMethod("getKind").invoke(constructed)).isEqualTo("Base")
        }
    }

    @Test
    fun `enum parent supports a dollar discriminator and its constructor default`() {
        val field = "${'$'}type"
        val files =
            generate(SerializationLibrary.JACKSON_2, open = true, unions = false) { root ->
                val parent = root.at("/components/schemas/Base") as ObjectNode
                val properties = parent.path("properties") as ObjectNode
                val type = properties.remove("kind") as ObjectNode
                type
                    .putArray("enum")
                    .add("Base")
                    .add("first")
                    .add("second")
                properties.set<ObjectNode>(field, type)
                parent.putArray("required").add(field).add("id")
                (parent.path("discriminator") as ObjectNode).put("propertyName", field)
            }
        withCompiledModels(files) { loader ->
            val base = loader.loadClass("example.models.Base")
            val value = base.constructors.first { it.parameterCount == 5 }.newInstance("Alice", null, null, 6, null)
            assertThat(base.getMethod("getType").invoke(value).toString()).isEqualTo("Base")
            val mapper =
                com.fasterxml.jackson.databind.ObjectMapper().registerModule(
                    com.fasterxml.jackson.module.kotlin.KotlinModule
                        .Builder()
                        .build(),
                )
            assertThat(mapper.readValue(mapper.writeValueAsString(value), base)).isEqualTo(value)
        }
    }

    @Test
    fun `conflicting values and enum restrictions warn and leave the open discriminator required`() {
        val messages = mutableListOf<String>()
        val handler =
            object : Handler() {
                override fun publish(record: LogRecord) {
                    messages.add(record.message)
                }

                override fun flush() = Unit

                override fun close() = Unit
            }
        val logger = Logger.getGlobal()
        logger.addHandler(handler)
        try {
            listOf(false, true).forEach { enum ->
                val files =
                    generate(SerializationLibrary.JACKSON_2, open = true) { root ->
                        if (enum) {
                            (root.at("/components/schemas/Base/properties/kind") as ObjectNode).putArray("enum").add("first").add("second")
                        } else {
                            (
                                root.at(
                                    "/components/schemas/Base/discriminator/mapping",
                                ) as ObjectNode
                            ).put("Base", "#/components/schemas/First")
                        }
                    }
                val source = files.first { it.name == "Base" }.toString()
                assertThat(
                    source,
                ).contains("open class Base").doesNotContain("value = Base::class", "defaultImpl", "kind: String =", "kind: BaseKind =")
                withCompiledModels(files) { loader ->
                    val base = loader.loadClass("example.models.Base")
                    assertThat(base.constructors.map { it.parameterCount }).contains(3)
                }
            }
            assertThat(messages).anyMatch { "mapped to another schema" in it }
            assertThat(messages).anyMatch { "enum has no member" in it }
            generate(SerializationLibrary.JACKSON_2, open = true, sealed = true)
            generate(SerializationLibrary.KOTLINX_SERIALIZATION, open = true)
            assertThat(messages).anyMatch { "Both allOf class options" in it }
            assertThat(messages).anyMatch { "Ignoring OPEN_CLASSES_FOR_ALL_OF" in it }
        } finally {
            logger.removeHandler(handler)
        }
    }

    private fun withCompiledModels(
        files: Collection<FileSpec>,
        block: (ClassLoader) -> Unit,
    ) {
        val source = directory.resolve("source")
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
            block(loader)
        }
    }
}
