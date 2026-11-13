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
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeSpec
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.util.logging.Handler
import java.util.logging.LogRecord
import java.util.logging.Logger

class HierarchyOneOfTest {
    @ParameterizedTest
    @CsvSource(
        "JACKSON_2, false, false",
        "JACKSON_2, false, true",
        "JACKSON_2, true, false",
        "JACKSON_2, true, true",
        "JACKSON_3, false, false",
        "JACKSON_3, false, true",
        "JACKSON_3, true, false",
        "JACKSON_3, true, true",
    )
    fun `singleton types preserve metadata while unsupported subsets warn`(
        library: SerializationLibrary,
        openParents: Boolean,
        sharedContracts: Boolean,
    ) {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.HTTP_MODELS),
            serializationLibrary = library,
            validationLibrary = ValidationLibrary.JAKARTA_VALIDATION,
            modelOptions =
                buildSet {
                    if (openParents) add(ModelCodeGenOptionType.OPEN_CLASSES_FOR_ALL_OF)
                    if (sharedContracts) add(ModelCodeGenOptionType.SHARED_COMPOSITION_CONTRACTS)
                },
        )
        ModelNameRegistry.clear()
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
        val holder =
            try {
                ModelGenerator(Packages("audit"), SourceApi(readTextResource("/examples/singletonHierarchyUnion/api.yaml")))
                    .generate()
                    .files
                    .single { it.name == "Holder" }
                    .members
                    .filterIsInstance<TypeSpec>()
                    .single()
            } finally {
                logger.removeHandler(handler)
            }
        val warnings = messages.filter { it.startsWith("Widening oneOf") }
        assertThat(warnings).hasSize(if (openParents) 3 else 2)
        assertThat(warnings).anySatisfy { assertThat(it).contains("/properties/middle'", "safe concrete leaf", "consumer code") }
        assertThat(warnings).anySatisfy { assertThat(it).contains("/properties/subset'", "complete discriminator family") }
        assertThat(warnings.any { it.contains("/properties/childrenOnly'") }).isEqualTo(openParents)

        val parameters = holder.primaryConstructor!!.parameters.associateBy { it.name }
        assertThat(parameters.getValue("single").type.toString()).isEqualTo("audit.models.Child?")
        assertThat(parameters.getValue("single").defaultValue.toString()).isEqualTo("null")
        for (name in listOf("parent", "middle", "subset", "family", "childrenOnly")) {
            assertThat(parameters.getValue(name).type.toString()).isEqualTo("audit.models.Base?")
        }
        for ((name, nullable) in listOf("values" to false, "nullableValues" to true)) {
            val list = parameters.getValue(name).type as ParameterizedTypeName
            assertThat(list.isNullable).isTrue()
            val element = list.typeArguments.single()
            assertThat(element.copy(annotations = emptyList()).toString()).isEqualTo("audit.models.Child" + if (nullable) "?" else "")
            assertThat(element.annotations.map { it.typeName.toString() }).contains("jakarta.validation.Valid")
        }
    }
}
