package com.cjbooms.fabrikt.generators.model

import com.cjbooms.fabrikt.model.KotlinTypeInfo
import com.cjbooms.fabrikt.util.NormalisedString.pascalCase
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName
import com.cjbooms.fabrikt.util.toLowerCase

object JacksonMetadata {
    val TYPE_REFERENCE_IMPORT = Pair("com.fasterxml.jackson.module.kotlin", "jacksonTypeRef")
    private val JSON_PROPERTY_CLASS = ClassName("com.fasterxml.jackson.annotation", "JsonProperty")
    private val JSON_VALUE_CLASS = ClassName("com.fasterxml.jackson.annotation", "JsonValue")
    private val JSON_TYPE_INFO_CLASS = ClassName("com.fasterxml.jackson.annotation", "JsonTypeInfo")
    private val JSON_SUB_TYPES_CLASS = ClassName("com.fasterxml.jackson.annotation", "JsonSubTypes")
    private val JSON_ANY_SETTER = ClassName("com.fasterxml.jackson.annotation", "JsonAnySetter")
    private val JSON_ANY_GETTER = ClassName("com.fasterxml.jackson.annotation", "JsonAnyGetter")
    private val JSON_IGNORE = ClassName("com.fasterxml.jackson.annotation", "JsonIgnore")
    private val JSON_INCLUDE_CLASS = ClassName("com.fasterxml.jackson.annotation", "JsonInclude")

    val JSON_VALUE = AnnotationSpec.builder(JSON_VALUE_CLASS).build()
    val JSON_INCLUDE_NON_NULL = AnnotationSpec
        .builder(JSON_INCLUDE_CLASS)
        .useSiteTarget(AnnotationSpec.UseSiteTarget.PARAM)
        .addMember("%T.Include.NON_NULL", JSON_INCLUDE_CLASS)
        .build()

    val JSON_INCLUDE_ALWAYS = AnnotationSpec
        .builder(JSON_INCLUDE_CLASS)
        .useSiteTarget(AnnotationSpec.UseSiteTarget.PARAM)
        .addMember("%T.Include.ALWAYS", JSON_INCLUDE_CLASS)
        .build()

    fun jacksonPropertyAnnotation(name: String) = AnnotationSpec
        .builder(JSON_PROPERTY_CLASS)
        .useSiteTarget(AnnotationSpec.UseSiteTarget.GET)
        .addMember("%S", name).build()

    fun jacksonParameterAnnotation(name: String, required: Boolean = false) = AnnotationSpec
        .builder(JSON_PROPERTY_CLASS)
        .useSiteTarget(AnnotationSpec.UseSiteTarget.PARAM)
        .addMember("%S", name)
        .apply { if (required) addMember("required = %L", true) }
        .build()

    val anySetter = AnnotationSpec.builder(JSON_ANY_SETTER).build()
    val anyGetter = AnnotationSpec.builder(JSON_ANY_GETTER).build()
    val ignore = AnnotationSpec.builder(JSON_IGNORE)
        .useSiteTarget(AnnotationSpec.UseSiteTarget.GET)
        .build()

    fun basePolymorphicType(discriminatorFieldName: String) = AnnotationSpec
        .builder(JSON_TYPE_INFO_CLASS)
        .addMember(
            "use = %T.Id.NAME",
            JSON_TYPE_INFO_CLASS
        )
        .addMember(
            "include = %T.As.EXISTING_PROPERTY",
            JSON_TYPE_INFO_CLASS
        )
        .addMember("property = %S", discriminatorFieldName)
        .addMember("visible = true")
        .build()

    fun polymorphicSubTypes(typePairs: Map<String, TypeName>, enumDiscriminator: KotlinTypeInfo.Enum?): AnnotationSpec {
        val codeBuilder = CodeBlock.builder()
        typePairs.forEach { (name, type) ->
            if (codeBuilder.isNotEmpty()) codeBuilder.add(",")
            val maybeSchemaEnumValue = enumDiscriminator?.entries?.firstOrNull {
                // lowercase to cover UPPER_SNAKE_CASE
                it.pascalCase() == name || it.toLowerCase().pascalCase() == name
            }
            codeBuilder.add(
                "%T.Type(value = %T::class, name = %S)",
                JSON_SUB_TYPES_CLASS, type, maybeSchemaEnumValue ?: name
            )
        }

        return AnnotationSpec.builder(JSON_SUB_TYPES_CLASS)
            .addMember(codeBuilder.build()).build()
    }
}
