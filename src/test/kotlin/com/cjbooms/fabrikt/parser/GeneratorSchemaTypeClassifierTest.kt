package com.cjbooms.fabrikt.parser

import com.cjbooms.fabrikt.model.OasType
import com.cjbooms.fabrikt.model.OasType.Companion.toOasType
import com.cjbooms.fabrikt.util.YamlUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class GeneratorSchemaTypeClassifierTest {
    @ParameterizedTest
    @ValueSource(
        strings = [
            "Text",
            "Date",
            "DateTime",
            "Uuid",
            "Uri",
            "Bytes",
            "Binary",
            "Float",
            "Double",
            "Number",
            "Int32",
            "Int64",
            "Integer",
            "Boolean",
            "Array",
            "Set",
            "Object",
            "Map",
            "Anything",
            "Enum",
        ],
    )
    fun `matches legacy classification for basic OpenAPI 3_0 schemas`(name: String) {
        val input = openApi("3.0.4", commonSchemas)
        val legacySchema = YamlUtils.parseOpenApi(input).schemas.getValue(name)
        val adapted = LegacyGeneratorSchemaAdapter().adapt(legacySchema)

        assertThat(GeneratorSchemaTypeClassifier.classify(adapted))
            .isEqualTo(
                GeneratorSchemaTypeClassification.Resolved(
                    com.cjbooms.fabrikt.model
                        .OpenApiSchema(legacySchema)
                        .toOasType(name),
                    legacySchema.isNullable,
                ),
            )
    }

    @ParameterizedTest
    @ValueSource(strings = ["3.0.4", "3.1.2", "3.2.0"])
    fun `classifies basic schema types across OpenAPI versions`(version: String) {
        val schemas = parseSchemas(version, commonSchemas)

        assertResolved(schemas, "Text", OasType.Text)
        assertResolved(schemas, "Date", OasType.Date)
        assertResolved(schemas, "DateTime", OasType.DateTime)
        assertResolved(schemas, "Uuid", OasType.Uuid)
        assertResolved(schemas, "Uri", OasType.Uri)
        assertResolved(schemas, "Bytes", OasType.Base64String)
        assertResolved(schemas, "Binary", OasType.Binary)
        assertResolved(schemas, "Float", OasType.Float)
        assertResolved(schemas, "Double", OasType.Double)
        assertResolved(schemas, "Number", OasType.Number)
        assertResolved(schemas, "Int32", OasType.Int32)
        assertResolved(schemas, "Int64", OasType.Int64)
        assertResolved(schemas, "Integer", OasType.Integer)
        assertResolved(schemas, "Boolean", OasType.Boolean)
        assertResolved(schemas, "Array", OasType.Array)
        assertResolved(schemas, "Set", OasType.Set)
        assertResolved(schemas, "Object", OasType.Object)
        assertResolved(schemas, "Map", OasType.Map)
        assertResolved(schemas, "Anything", OasType.UntypedObject)
        assertResolved(schemas, "Enum", OasType.Enum)
        assertResolved(schemas, "InferredObject", OasType.Object)
        assertResolved(schemas, "InferredArray", OasType.Array)
    }

    @ParameterizedTest
    @ValueSource(strings = ["3.1.2", "3.2.0"])
    fun `classifies nullable and unsupported JSON Schema forms explicitly`(version: String) {
        val schemas = parseSchemas(version, jsonSchemaForms)

        assertThat(GeneratorSchemaTypeClassifier.classify(schemas.getValue("Nullable")))
            .isEqualTo(GeneratorSchemaTypeClassification.Resolved(OasType.Text, true))
        assertThat(GeneratorSchemaTypeClassifier.classify(schemas.getValue("Union")))
            .isEqualTo(
                GeneratorSchemaTypeClassification.Unsupported(
                    GeneratorSchemaTypeClassification.Reason.MULTIPLE_NON_NULL_TYPES,
                ),
            )
        assertThat(GeneratorSchemaTypeClassifier.classify(schemas.getValue("Never")))
            .isEqualTo(GeneratorSchemaTypeClassification.Uninhabitable)
        assertThat(GeneratorSchemaTypeClassifier.classify(schemas.getValue("Any")))
            .isEqualTo(GeneratorSchemaTypeClassification.Resolved(OasType.Any, false))
        assertThat(GeneratorSchemaTypeClassifier.classify(schemas.getValue("AllOfNever")))
            .isEqualTo(GeneratorSchemaTypeClassification.Uninhabitable)
        assertThat(GeneratorSchemaTypeClassifier.classify(schemas.getValue("AnyOfNever")))
            .isEqualTo(GeneratorSchemaTypeClassification.Uninhabitable)
        assertThat(GeneratorSchemaTypeClassifier.classify(schemas.getValue("OneOfNever")))
            .isEqualTo(GeneratorSchemaTypeClassification.Uninhabitable)
        assertThat(GeneratorSchemaTypeClassifier.classify(schemas.getValue("OneOfPossible")))
            .isEqualTo(GeneratorSchemaTypeClassification.Resolved(OasType.Text, false))
        assertThat(GeneratorSchemaTypeClassifier.classify(schemas.getValue("MixedComposition")))
            .isEqualTo(
                GeneratorSchemaTypeClassification.Unsupported(
                    GeneratorSchemaTypeClassification.Reason.INCONSISTENT_COMPOSITION_TYPES,
                ),
            )
    }

    private fun assertResolved(
        schemas: Map<String, SourceSchema>,
        name: String,
        expected: OasType,
    ) {
        assertThat(GeneratorSchemaTypeClassifier.classify(schemas.getValue(name)))
            .isEqualTo(GeneratorSchemaTypeClassification.Resolved(expected, false))
    }

    private fun parseSchemas(
        version: String,
        schemas: String,
    ): Map<String, SourceSchema> = SourceOpenApiDocumentParser.parse(openApi(version, schemas)).componentSchemas

    private fun openApi(
        version: String,
        schemas: String,
    ): String =
        """
        openapi: $version
        info:
          title: Test
          version: "1.0"
        paths: {}
        components:
          schemas:
        """.trimIndent() + "\n" + schemas.prependIndent("    ")

    private val commonSchemas =
        """
        Text: { type: string }
        Date: { type: string, format: date }
        DateTime: { type: string, format: date-time }
        Uuid: { type: string, format: uuid }
        Uri: { type: string, format: uri }
        Bytes: { type: string, format: byte }
        Binary: { type: string, format: binary }
        Float: { type: number, format: float }
        Double: { type: number, format: double }
        Number: { type: number }
        Int32: { type: integer, format: int32 }
        Int64: { type: integer, format: int64 }
        Integer: { type: integer }
        Boolean: { type: boolean }
        Array: { type: array, items: { type: string } }
        Set: { type: array, uniqueItems: true, items: { type: string } }
        Object: { type: object, properties: { value: { type: string } } }
        Map: { type: object, additionalProperties: { type: string } }
        Anything: { type: object }
        Enum: { type: string, enum: [one, two] }
        InferredObject: { properties: { value: { type: string } } }
        InferredArray: { items: { type: string } }
        """.trimIndent()

    private val jsonSchemaForms =
        """
        Nullable: { type: [string, 'null'] }
        Union: { type: [string, integer, 'null'] }
        Never: false
        Any: true
        AllOfNever:
          allOf: [false]
        AnyOfNever:
          anyOf: [false, false]
        OneOfNever:
          oneOf: [false, false]
        OneOfPossible:
          oneOf: [false, { type: string }]
        MixedComposition:
          oneOf:
            - { type: string }
            - { type: integer }
        """.trimIndent()
}
