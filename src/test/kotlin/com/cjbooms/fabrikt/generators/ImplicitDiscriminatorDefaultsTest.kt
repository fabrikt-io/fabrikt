package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType
import com.cjbooms.fabrikt.cli.SerializationLibrary
import com.cjbooms.fabrikt.cli.ValidationLibrary
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.model.ModelGenerator
import com.cjbooms.fabrikt.model.Models
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.YamlObjectMapper
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.net.URLClassLoader
import java.nio.file.Path
import kotlin.io.path.writeText

class ImplicitDiscriminatorDefaultsTest {
    @TempDir
    lateinit var directory: Path

    @ParameterizedTest
    @EnumSource(value = SerializationLibrary::class, names = ["JACKSON_2", "JACKSON_3"])
    fun implicitMappingDefaultsUseSchemaNames(library: SerializationLibrary) {
        val files = generate(library = library).files.associate { it.name to it.toString() }

        assertThat(files.getValue("Dog")).contains("petType: String = \"Dog\"")
        assertThat(files.getValue("Cat")).contains("petType: String = \"Cat\"")
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "Dto"])
    fun implicitValuesKeepOriginalSchemaNames(suffix: String) {
        val document = document()
        val schemas = document.at("/components/schemas") as ObjectNode
        schemas.set<ObjectNode>("dog_record", schemas.remove("Dog"))
        (schemas.at("/Pet/oneOf/0") as ObjectNode).put("${'$'}ref", "#/components/schemas/dog_record")
        val files = generate(document, suffix = suffix).files.associate { it.name to it.toString() }

        assertThat(files.getValue("DogRecord$suffix")).contains("petType: String = \"dog_record\"")
        assertThat(files.getValue("Pet$suffix")).contains("name = \"dog_record\"")
    }

    @Test
    fun explicitMappingsKeepTheirValues() {
        val document = document()
        (document.at("/components/schemas/Pet/discriminator") as ObjectNode)
            .putObject("mapping")
            .put("canine", "#/components/schemas/Dog")
            .put("feline", "#/components/schemas/Cat")
        val files = generate(document).files.associate { it.name to it.toString() }

        assertThat(files.getValue("Dog")).contains("petType: String = \"canine\"")
        assertThat(files.getValue("Cat")).contains("petType: String = \"feline\"")
    }

    @Test
    fun conflictingComposedMappingsDoNotChooseADefault() {
        val document = document("composed")
        val schemas = document.at("/components/schemas") as ObjectNode
        val otherUnion = (schemas.get("Pet") as ObjectNode).deepCopy()
        (otherUnion.get("discriminator") as ObjectNode)
            .putObject("mapping")
            .put("canine", "#/components/schemas/Dog")
            .put("feline", "#/components/schemas/Cat")
        schemas.set<ObjectNode>("OtherPet", otherUnion)
        val files = generate(document).files.associate { it.name to it.toString() }

        assertThat(files.getValue("Dog")).contains("petType: String,").doesNotContain("petType: String =")
        assertThat(files.getValue("Cat")).contains("petType: String,").doesNotContain("petType: String =")
    }

    @Test
    fun enumWithoutMatchingImplicitValueDoesNotGetAnInvalidDefault() {
        val document = document("enum")
        (document.at("/components/schemas/PetKind") as ObjectNode).putArray("enum").add("canine").add("feline")
        val files = generate(document).files.associate { it.name to it.toString() }

        assertThat(files.getValue("Dog")).contains("petType: PetKind,").doesNotContain("petType: PetKind =")
        assertThat(files.getValue("Cat")).contains("petType: PetKind,").doesNotContain("petType: PetKind =")
    }

    @ParameterizedTest
    @ValueSource(strings = ["flat", "composed", "enum"])
    fun compiledConstructorsAndJacksonAgreeOnImplicitValues(shape: String) {
        val files = generate(document(shape), contracts = shape == "composed").files
        val source = directory.resolve("source")
        files.forEach { it.writeTo(source) }
        val probe = source.resolve("DefaultsProbe.kt")
        probe.writeText(
            """
            package audit.models
            object DefaultsProbe {
                @JvmStatic fun create(): Array<Any> = arrayOf(Dog(bark = "woof"), Cat(meow = "purr"))
            }
            """.trimIndent(),
        )
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
                probe.toString(),
                *files.map { source.resolve("audit/models/${it.name}.kt").toString() }.toTypedArray(),
            )
        assertThat(result).withFailMessage(diagnostics.toString()).isEqualTo(ExitCode.OK)

        URLClassLoader(arrayOf(output.toUri().toURL()), javaClass.classLoader).use { loader ->
            val values = loader.loadClass("audit.models.DefaultsProbe").getMethod("create").invoke(null) as Array<*>
            val union = loader.loadClass("audit.models.Pet")
            val mapper = jacksonObjectMapper()
            values.forEach { value ->
                val json = mapper.writerFor(union).writeValueAsString(value)
                assertThat(mapper.readTree(json).get("petType").asText()).isEqualTo(value!!.javaClass.simpleName)
                assertThat(mapper.readValue(json, union)).isEqualTo(value)
            }
            assertThrows<InvalidTypeIdException> { mapper.readValue("""{"bark":"woof"}""", union) }
            assertThrows<InvalidTypeIdException> { mapper.readValue("""{"petType":"unknown","bark":"woof"}""", union) }
        }
    }

    private fun document(shape: String = "flat"): ObjectNode {
        val resource = javaClass.getResource("/examples/implicitDiscriminatorDefaults/api.yaml")!!
        val document = YamlObjectMapper.instance.readTree(resource.readText()) as ObjectNode
        val schemas = document.at("/components/schemas") as ObjectNode
        if (shape == "composed") {
            val fields = schemas.putObject("PetFields")
            fields.put("type", "object")
            fields.putArray("required").add("petType")
            fields.putObject("properties").putObject("petType").put("type", "string")
            listOf("Dog" to "bark", "Cat" to "meow").forEach { (name, field) ->
                val original = schemas.get(name) as ObjectNode
                (original.get("properties") as ObjectNode).remove("petType")
                original.putArray("required").add(field)
                val allOf = schemas.putObject(name).putArray("allOf")
                allOf.addObject().put("${'$'}ref", "#/components/schemas/PetFields")
                allOf.add(original)
            }
        } else if (shape == "enum") {
            val kind = schemas.putObject("PetKind")
            kind.put("type", "string")
            kind.putArray("enum").add("Dog").add("Cat")
            listOf("Dog", "Cat").forEach { name ->
                (schemas.at("/$name/properties") as ObjectNode)
                    .putObject("petType")
                    .putArray("allOf")
                    .addObject()
                    .put("${'$'}ref", "#/components/schemas/PetKind")
            }
        }
        return document
    }

    private fun generate(
        document: ObjectNode = document(),
        library: SerializationLibrary = SerializationLibrary.JACKSON_2,
        suffix: String = "",
        contracts: Boolean = false,
    ): Models {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.HTTP_MODELS),
            serializationLibrary = library,
            validationLibrary = ValidationLibrary.NO_VALIDATION,
            modelSuffix = suffix,
            modelOptions = if (contracts) setOf(ModelCodeGenOptionType.SHARED_COMPOSITION_CONTRACTS) else emptySet(),
        )
        ModelNameRegistry.clear()
        return ModelGenerator(Packages("audit"), SourceApi(YamlObjectMapper.instance.writeValueAsString(document))).generate()
    }
}
