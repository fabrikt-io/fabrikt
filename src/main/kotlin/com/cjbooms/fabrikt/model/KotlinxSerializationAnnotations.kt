package com.cjbooms.fabrikt.model

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonDecoder

object KotlinxSerializationAnnotations : SerializationAnnotations {
    private const val DEFAULT_JSON_CLASS_DISCRIMINATOR = "type"
    private const val FAULT_TOLERANT_ENUM_SERIALIZER_NAME = "Serializer"

    /**
     * Polymorphic class discriminators are added as annotations in kotlinx serialization.
     * Including them in the class definition causes compilation errors since the property name
     * will conflict with the class discriminator name.
     */
    override val supportsBackingPropertyForDiscriminator = false

    /**
     * kotlinx serialization includes superclass backing fields in the subclass serial state,
     * so a subtype overriding a supertype property with its own backing field would produce a
     * duplicate serial name. Super type properties must be abstract instead.
     */
    override val supportsInheritedBackingProperties = false

    /**
     * Supporting "additionalProperties: true" for kotlinx serialization requires additional
     * research and work due to Any type in the map (val properties: MutableMap<String, Any?>)
     *
     * Currently, the generated code does not support additional properties.
     *
     * See also https://github.com/Kotlin/kotlinx.serialization/issues/1978
     */
    override val supportsAdditionalProperties = false

    override fun addIgnore(propertySpecBuilder: PropertySpec.Builder) = propertySpecBuilder // not applicable

    override fun addGetter(funSpecBuilder: FunSpec.Builder) = funSpecBuilder // not applicable

    override fun addSetter(funSpecBuilder: FunSpec.Builder) = funSpecBuilder // not applicable

    override fun addProperty(
        propertySpecBuilder: PropertySpec.Builder,
        oasKey: String,
        kotlinTypeInfo: KotlinTypeInfo,
    ): PropertySpec.Builder {
        if (kotlinTypeInfo is KotlinTypeInfo.Custom && kotlinTypeInfo.kotlinxSerializer != null) {
            propertySpecBuilder.addAnnotation(serializerAnnotation(kotlinTypeInfo.kotlinxSerializer))
        } else if (needsContextualAnnotation(kotlinTypeInfo)) {
            propertySpecBuilder.addAnnotation(AnnotationSpec.builder(Contextual::class).build())
        }
        return propertySpecBuilder.addAnnotation(
            AnnotationSpec.builder(SerialName::class).addMember("%S", oasKey).build(),
        )
    }

    override fun annotateArrayElementType(
        elementType: TypeName,
        elementTypeInfo: KotlinTypeInfo,
    ): TypeName = annotateType(elementType, elementTypeInfo)

    override fun annotateMapValueType(
        valueType: TypeName,
        valueTypeInfo: KotlinTypeInfo,
    ): TypeName = annotateType(valueType, valueTypeInfo)

    private fun annotateType(
        type: TypeName,
        typeInfo: KotlinTypeInfo,
    ): TypeName =
        if (typeInfo is KotlinTypeInfo.Custom && typeInfo.kotlinxSerializer != null) {
            type.copy(annotations = listOf(serializerAnnotation(typeInfo.kotlinxSerializer)))
        } else if (needsContextualAnnotation(typeInfo)) {
            val contextualAnnotation = AnnotationSpec.builder(Contextual::class).build()
            type.copy(annotations = listOf(contextualAnnotation))
        } else {
            type
        }

    private fun serializerAnnotation(serializer: ClassName): AnnotationSpec =
        AnnotationSpec.builder(Serializable::class).addMember("with = %T::class", serializer).build()

    private fun needsContextualAnnotation(typeInfo: KotlinTypeInfo): Boolean =
        when (typeInfo) {
            is KotlinTypeInfo.Numeric,
            is KotlinTypeInfo.Uri,
            is KotlinTypeInfo.Uuid,
            is KotlinTypeInfo.ByteArray,
            is KotlinTypeInfo.Instant,
            is KotlinTypeInfo.Date,
            is KotlinTypeInfo.DateTime,
            is KotlinTypeInfo.LocalDateTime,
            -> true
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
    override fun addBasePolymorphicTypeAnnotation(
        typeSpecBuilder: TypeSpec.Builder,
        propertyName: String,
    ) = if (propertyName != DEFAULT_JSON_CLASS_DISCRIMINATOR) {
        typeSpecBuilder.addAnnotation(
            AnnotationSpec.builder(JsonClassDiscriminator::class).addMember("%S", propertyName).build(),
        )
        val experimentalSerializationApiAnnotation =
            AnnotationSpec
                .builder(
                    // necessary because ExperimentalSerializationApi "can only be used as an annotation or as an argument to @OptIn"
                    ClassName("kotlinx.serialization", "ExperimentalSerializationApi"),
                ).build()
        typeSpecBuilder.addAnnotation(experimentalSerializationApiAnnotation)
    } else {
        typeSpecBuilder
    }

    override fun addPolymorphicSubTypesAnnotation(
        typeSpecBuilder: TypeSpec.Builder,
        mappings: Map<String, TypeName>,
        enumDiscriminator: KotlinTypeInfo.Enum?,
    ) = typeSpecBuilder // not applicable — subtypes carry a @SerialName mapping instead

    override fun addPolymorphicSubTypeDeductionAnnotation(
        typeSpecBuilder: TypeSpec.Builder,
        subTypes: List<TypeName>,
    ) = typeSpecBuilder // not applicable — kotlinx requires a class discriminator field

    override fun addSubtypeMappingAnnotation(
        typeSpecBuilder: TypeSpec.Builder,
        mapping: String,
    ) = typeSpecBuilder.addAnnotation(AnnotationSpec.builder(SerialName::class).addMember("%S", mapping).build())

    override fun addEnumPropertyAnnotation(propSpecBuilder: PropertySpec.Builder) = propSpecBuilder // not applicable

    override fun addEnumConstantAnnotation(
        enumSpecBuilder: TypeSpec.Builder,
        enumValue: String,
    ) = enumSpecBuilder.addAnnotation(AnnotationSpec.builder(SerialName::class).addMember("%S", enumValue).build())

    override fun addEnumDefaultAnnotation(
        enumSpecBuilder: TypeSpec.Builder,
        enumValue: String,
    ) = enumSpecBuilder // not applicable — see addFaultTolerantEnumSerializer

    /**
     * kotlinx has no default enum constant, so the built-in enum serializer throws on unknown values.
     * A nested serializer maps them to the UNRECOGNIZED fallback instead.
     * It honours `Json { decodeEnumsCaseInsensitive = true }`, which only applies to enum-kind descriptors.
     */
    override fun addFaultTolerantEnumSerializer(
        enumSpecBuilder: TypeSpec.Builder,
        enumType: ClassName,
    ): TypeSpec.Builder {
        val serializerType = enumType.nestedClass(FAULT_TOLERANT_ENUM_SERIALIZER_NAME)
        val serializer =
            TypeSpec
                .objectBuilder(serializerType)
                .addSuperinterface(KSerializer::class.asClassName().parameterizedBy(enumType))
                .addProperty(
                    PropertySpec
                        .builder("descriptor", SerialDescriptor::class, KModifier.OVERRIDE)
                        .initializer(
                            "%M(%S, %T.STRING)",
                            MemberName("kotlinx.serialization.descriptors", "PrimitiveSerialDescriptor"),
                            enumType.canonicalName,
                            PrimitiveKind::class,
                        ).build(),
                ).addFunction(
                    FunSpec
                        .builder("serialize")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("encoder", Encoder::class)
                        .addParameter("value", enumType)
                        .addStatement("encoder.encodeString(value.value)")
                        .build(),
                ).addFunction(
                    FunSpec
                        .builder("deserialize")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("decoder", Decoder::class)
                        .returns(enumType)
                        .addStatement("val value = decoder.decodeString()")
                        .addStatement(
                            "val ignoreCase = (decoder as? %T)?.json?.configuration?.decodeEnumsCaseInsensitive == true",
                            JsonDecoder::class,
                        ).addStatement("return entries.firstOrNull { it.value.equals(value, ignoreCase) } ?: UNRECOGNIZED")
                        .build(),
                ).build()
        return enumSpecBuilder
            .addAnnotation(serializerAnnotation(serializerType))
            .addType(serializer)
    }
}
