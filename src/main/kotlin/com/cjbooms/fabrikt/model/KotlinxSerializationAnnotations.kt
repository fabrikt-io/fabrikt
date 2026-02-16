package com.cjbooms.fabrikt.model

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

object KotlinxSerializationAnnotations : SerializationAnnotations {
    private const val DEFAULT_JSON_CLASS_DISCRIMINATOR = "type"

    /**
     * Polymorphic class discriminators are added as annotations in kotlinx serialization.
     * Including them in the class definition causes compilation errors since the property name
     * will conflict with the class discriminator name.
     */
    override val supportsBackingPropertyForDiscriminator = false

    /**
     * Supporting "additionalProperties: true" for kotlinx serialization requires additional
     * research and work due to Any type in the map (val properties: MutableMap<String, Any?>)
     *
     * Currently, the generated code does not support additional properties.
     *
     * See also https://github.com/Kotlin/kotlinx.serialization/issues/1978
     */
    override val supportsAdditionalProperties = false

    override fun addIgnore(propertySpecBuilder: PropertySpec.Builder) =
        propertySpecBuilder // not applicable

    override fun addGetter(funSpecBuilder: FunSpec.Builder) =
        funSpecBuilder // not applicable

    override fun addSetter(funSpecBuilder: FunSpec.Builder) =
        funSpecBuilder // not applicable

    override fun addProperty(
        propertySpecBuilder: PropertySpec.Builder,
        oasKey: String,
        kotlinTypeInfo: KotlinTypeInfo
    ): PropertySpec.Builder {
        if (needsContextualAnnotation(kotlinTypeInfo)) {
            propertySpecBuilder.addAnnotation(AnnotationSpec.builder(Contextual::class).build())
        }
        return propertySpecBuilder.addAnnotation(
            AnnotationSpec.builder(SerialName::class).addMember("%S", oasKey).build()
        )
    }

    override fun annotateArrayElementType(elementType: TypeName, elementTypeInfo: KotlinTypeInfo): TypeName =
        if (needsContextualAnnotation(elementTypeInfo)) {
            val contextualAnnotation = AnnotationSpec.builder(Contextual::class).build()
            elementType.copy(annotations = listOf(contextualAnnotation))
        } else {
            elementType
        }

    private fun needsContextualAnnotation(typeInfo: KotlinTypeInfo): Boolean =
        when (typeInfo) {
            is KotlinTypeInfo.Numeric,
            is KotlinTypeInfo.Uri,
            is KotlinTypeInfo.Uuid,
            is KotlinTypeInfo.ByteArray,
            is KotlinTypeInfo.Instant,
            is KotlinTypeInfo.Date,
            is KotlinTypeInfo.DateTime,
            is KotlinTypeInfo.LocalDateTime -> true
            else -> false
        }

    override fun addParameter(
        propertySpecBuilder: PropertySpec.Builder,
        oasKey: String,
        isRequired: Boolean,
        typeInfo: KotlinTypeInfo,
    ) = propertySpecBuilder // not applicable

    override fun addClassAnnotation(typeSpecBuilder: TypeSpec.Builder) =
        typeSpecBuilder.addAnnotation(AnnotationSpec.builder(Serializable::class).build())

    @OptIn(ExperimentalSerializationApi::class)
    override fun addBasePolymorphicTypeAnnotation(typeSpecBuilder: TypeSpec.Builder, propertyName: String) =
        if (propertyName != DEFAULT_JSON_CLASS_DISCRIMINATOR) {
            typeSpecBuilder.addAnnotation(
                AnnotationSpec.builder(JsonClassDiscriminator::class).addMember("%S", propertyName).build()
            )
            val experimentalSerializationApiAnnotation = AnnotationSpec.builder(
                // necessary because ExperimentalSerializationApi "can only be used as an annotation or as an argument to @OptIn"
                ClassName("kotlinx.serialization", "ExperimentalSerializationApi")
            ).build()
            typeSpecBuilder.addAnnotation(experimentalSerializationApiAnnotation)
        } else typeSpecBuilder

    override fun addPolymorphicSubTypesAnnotation(typeSpecBuilder: TypeSpec.Builder, mappings: Map<String, TypeName>) =
        typeSpecBuilder // not applicable

    override fun addSubtypeMappingAnnotation(typeSpecBuilder: TypeSpec.Builder, mapping: String) =
        typeSpecBuilder.addAnnotation(AnnotationSpec.builder(SerialName::class).addMember("%S", mapping).build())

    override fun addEnumPropertyAnnotation(propSpecBuilder: PropertySpec.Builder) =
        propSpecBuilder // not applicable

    override fun addEnumConstantAnnotation(enumSpecBuilder: TypeSpec.Builder, enumValue: String) =
        enumSpecBuilder.addAnnotation(AnnotationSpec.builder(SerialName::class).addMember("%S", enumValue).build())

    override fun addEnumDefaultAnnotation(enumSpecBuilder: TypeSpec.Builder, enumValue: String) =
        enumSpecBuilder // not applicable
}
