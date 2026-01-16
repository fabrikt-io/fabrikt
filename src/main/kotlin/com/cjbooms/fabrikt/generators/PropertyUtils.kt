package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.JacksonNullabilityMode
import com.cjbooms.fabrikt.generators.GeneratorUtils.toKDoc
import com.cjbooms.fabrikt.generators.TypeFactory.maybeMakeMapValueNullable
import com.cjbooms.fabrikt.generators.model.JacksonMetadata
import com.cjbooms.fabrikt.model.JacksonAnnotations
import com.cjbooms.fabrikt.model.KotlinTypeInfo
import com.cjbooms.fabrikt.model.PropertyInfo
import com.cjbooms.fabrikt.model.SerializationAnnotations
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName

data class ClassSettings(
    val polymorphyType: PolymorphyType,
    val extensions: Map<String, Any> = emptyMap(),
) {

    val isMergePatchPattern = extensions["x-json-merge-patch"] as? Boolean ?: false
    val addJsonIncludeNonNullAnnotation = extensions["x-jackson-include-non-null"] as? Boolean ?: false

    enum class PolymorphyType {
        NONE,
        SUPER,
        SUB,
        ONE_OF,
    }
}

object PropertyUtils {
    fun PropertyInfo.addToClass(
        schemaName: String,
        type: TypeName,
        parameterizedType: TypeName,
        classBuilder: TypeSpec.Builder,
        constructorBuilder: FunSpec.Builder,
        classSettings: ClassSettings = ClassSettings(ClassSettings.PolymorphyType.NONE),
        validationAnnotations: ValidationAnnotations = JavaxValidationAnnotations,
        serializationAnnotations: SerializationAnnotations = JacksonAnnotations,
        jacksonNullabilityMode: JacksonNullabilityMode = JacksonNullabilityMode.NONE,
    ) {
        if (this.typeInfo is KotlinTypeInfo.UntypedObject && !serializationAnnotations.supportsAdditionalProperties)
            throw UnsupportedOperationException("Untyped objects not supported by selected serialization library (${this.oasKey}: ${this.schema})")

        val wrappedType =
            if (classSettings.isMergePatchPattern && !this.isRequired) {
                ClassName(
                    "org.openapitools.jackson.nullable",
                    "JsonNullable",
                ).parameterizedBy(type.copy(nullable = this.schema.isNullable))
            } else {
                type
            }
        val property = PropertySpec.builder(name, wrappedType)
            .apply {
                schema.toKDoc()?.let { addKdoc(it) }
            }

        if (this is PropertyInfo.AdditionalProperties) {
            if (!serializationAnnotations.supportsAdditionalProperties)
                throw UnsupportedOperationException("Additional properties not supported by selected serialization library")

            property.initializer(name)
            serializationAnnotations.addIgnore(property)
            val constructorParameter: ParameterSpec.Builder = ParameterSpec.builder(name, wrappedType)
            constructorParameter.defaultValue("mutableMapOf()")
            constructorBuilder.addParameter(constructorParameter.build())

            val value =
                when (typeInfo) {
                    is KotlinTypeInfo.MapTypeAdditionalProperties -> {
                        Map::class.asTypeName()
                            .parameterizedBy(String::class.asTypeName(), parameterizedType.maybeMakeMapValueNullable())
                    }
                    is KotlinTypeInfo.SimpleTypedAdditionalProperties -> {
                        typeInfo.parameterizedType.modelKClass.asTypeName()
                            .maybeMakeMapValueNullable()
                    }
                    else -> {
                        parameterizedType
                    }
                }.maybeMakeMapValueNullable()

            val getterSpecBuilder = FunSpec.builder("get")
                .returns(Map::class.asTypeName().parameterizedBy(String::class.asTypeName(), value))
                .addStatement("return $name")
            serializationAnnotations.addGetter(getterSpecBuilder)
            classBuilder.addFunction(getterSpecBuilder.build())

            val setterSpecBuilder = FunSpec.builder("set")
                .addParameter("name", String::class)
                .addParameter("value", value)
                .addStatement("$name[name] = value")
            serializationAnnotations.addSetter(setterSpecBuilder)
            classBuilder.addFunction(setterSpecBuilder.build())
        } else {
            when (classSettings.polymorphyType) {
                ClassSettings.PolymorphyType.SUPER -> {
                    if (this is PropertyInfo.Field && isPolymorphicDiscriminator) {
                        property.addModifiers(KModifier.ABSTRACT)
                    } else {
                        property.addModifiers(KModifier.OPEN)
                    }
                }

                ClassSettings.PolymorphyType.SUB -> {
                    if (this is PropertyInfo.Field && isPolymorphicDiscriminator) {
                        property.addModifiers(KModifier.OVERRIDE)
                    } else {
                        if (isInherited) {
                            property.addModifiers(KModifier.OVERRIDE)
                            classBuilder.addSuperclassConstructorParameter(name)
                        }
                        serializationAnnotations.addParameter(property, oasKey, isRequired, typeInfo)
                    }
                    serializationAnnotations.addProperty(property, oasKey, typeInfo)
                    property.addValidationAnnotations(this, validationAnnotations)
                }

                ClassSettings.PolymorphyType.NONE -> {
                    serializationAnnotations.addParameter(property, oasKey, isRequired, typeInfo)
                    serializationAnnotations.addProperty(property, oasKey, typeInfo)
                    property.addValidationAnnotations(this, validationAnnotations)
                }

                ClassSettings.PolymorphyType.ONE_OF -> {
                    serializationAnnotations.addProperty(property, oasKey, typeInfo)
                    property.addValidationAnnotations(this, validationAnnotations)
                }
            }

            if (isDiscriminatorFieldWithSingleKnownValue(classSettings, schemaName)) {
                this as PropertyInfo.Field
                if (classSettings.polymorphyType in listOf(ClassSettings.PolymorphyType.SUB, ClassSettings.PolymorphyType.ONE_OF)) {
                    if (!serializationAnnotations.supportsBackingPropertyForDiscriminator) {
                        return // Skip adding the property to the class
                    } else {
                        property.initializer(name)
                        serializationAnnotations.addParameter(property, oasKey, isRequired, typeInfo)
                        val constructorParameter: ParameterSpec.Builder = ParameterSpec.builder(name, wrappedType)
                        val discriminators = maybeDiscriminator.getDiscriminatorMappings(schemaName)
                        when (val discriminator = discriminators.first()) {
                            is PropertyInfo.DiscriminatorKey.EnumKey ->
                                constructorParameter.defaultValue("%T.%L", wrappedType, discriminator.enumKey)

                            is PropertyInfo.DiscriminatorKey.StringKey ->
                                constructorParameter.defaultValue("%S", discriminator.stringValue)
                        }
                        constructorBuilder.addParameter(constructorParameter.build())
                    }
                }
            } else {
                property.initializer(name)
                val constructorParameter: ParameterSpec.Builder = ParameterSpec.builder(name, wrappedType)
                val oasDefault = getDefaultValue(this, parameterizedType)

                val enforceNonNull = jacksonNullabilityMode in setOf(
                    JacksonNullabilityMode.ENFORCE_OPTIONAL_NON_NULL,
                    JacksonNullabilityMode.STRICT
                )

                val enforceRequiredNullable = jacksonNullabilityMode in setOf(
                    JacksonNullabilityMode.ENFORCE_REQUIRED_NULLABLE,
                    JacksonNullabilityMode.STRICT
                )

                if (enforceRequiredNullable && isRequired && schema.isNullable) {
                    property.addAnnotation(JacksonMetadata.JSON_INCLUDE_ALWAYS)
                }

                if (!isRequired) {
                    if (classSettings.addJsonIncludeNonNullAnnotation || (enforceNonNull && !schema.isNullable)) {
                        property.addAnnotation(JacksonMetadata.JSON_INCLUDE_NON_NULL)
                    }
                    if (oasDefault != null) {
                        val wrappedDefault =
                            if (classSettings.isMergePatchPattern) {
                                OasDefault.JsonNullableValue(oasDefault)
                            } else {
                                oasDefault
                            }
                        constructorParameter.defaultValue(wrappedDefault.getDefault())
                    } else {
                        val undefinedDefault = if (classSettings.isMergePatchPattern) {
                            "JsonNullable.undefined()"
                        } else {
                            "null"
                        }
                        constructorParameter.defaultValue(undefinedDefault)
                    }
                }
                constructorBuilder.addParameter(constructorParameter.build())
            }
        }

        classBuilder.addProperty(property.build())
    }

    private fun PropertyInfo.isDiscriminatorFieldWithSingleKnownValue(
        classSettings: ClassSettings,
        schemaName: String,
    ) = this is PropertyInfo.Field &&
        isPolymorphicDiscriminator &&
        !isSubTypeDiscriminatorWithNoValue(classSettings) &&
        !isSubTypeDiscriminatorWithMultipleValues(classSettings, schemaName)

    private fun Map<String, PropertyInfo.DiscriminatorKey>?.getDiscriminatorMappings(
        schemaName: String,
    ): List<PropertyInfo.DiscriminatorKey> =
        this?.filter {
            it.value.stringValue == schemaName || it.value.modelName == schemaName
        }?.map { it.value }.orEmpty()

    private fun PropertyInfo.Field.isSubTypeDiscriminatorWithNoValue(classType: ClassSettings) =
        classType.polymorphyType == ClassSettings.PolymorphyType.SUB &&
            isPolymorphicDiscriminator &&
            maybeDiscriminator == null

    private fun PropertyInfo.Field.isSubTypeDiscriminatorWithMultipleValues(
        classType: ClassSettings,
        schemaName: String,
    ) =
        classType.polymorphyType == ClassSettings.PolymorphyType.SUB &&
            isPolymorphicDiscriminator &&
            maybeDiscriminator.getDiscriminatorMappings(schemaName).size > 1

    private fun getDefaultValue(propTypeInfo: PropertyInfo, parameterizedType: TypeName): OasDefault? {
        return when (propTypeInfo) {
            is PropertyInfo.Field -> propTypeInfo.schema.default?.let {
                val className = parameterizedType as? ClassName
                OasDefault.from(propTypeInfo.typeInfo, className, it)
            }

            else -> null
        }
    }

    fun PropertyInfo.isNullable() = when (this) {
        is PropertyInfo.Field -> !isRequired && schema.default == null || schema.isNullable
        is PropertyInfo.ListField, is PropertyInfo.MapField,
        is PropertyInfo.ObjectRefField, is PropertyInfo.ObjectInlinedField ->
            !isRequired || schema.isNullable
        else -> !isRequired
    }

    /**
     * Current Open API v3 Spec validation keys:
     *
     *   multipleOf             - Not Supported. No equivalent javax validation. We could add our own as a
     *   maximum                - Supported
     *   minimum                - Supported
     *   exclusiveMaximum       - Support - Used to mark the above as exclusive checks
     *   exclusiveMinimum       - Support - Used to mark the above as exclusive checks
     *   maxLength              - Supported
     *   minLength              - Supported
     *   pattern                - Supported
     *   maxItems               - Supported
     *   minItems               - Supported
     *   uniqueItems            - Not Supported. No equivalent javax validation. We could add our own as a
     *   maxProperties          - Not Supported. No equivalent javax validation. We could add our own as a
     *   minProperties          - Not Supported. No equivalent javax validation. We could add our own as a
     *   enum                   - Not currently supported. Possible to do as a regex maybe.
     */
    private fun PropertySpec.Builder.addValidationAnnotations(
        info: PropertyInfo,
        validationAnnotations: ValidationAnnotations,
    ) {
        if (!info.isNullable()) maybeAddAnnotation(validationAnnotations.nonNullAnnotation)
        when (info) {
            is PropertyInfo.Field -> {
                // Regex validation pattern to validate string input
                info.pattern?.let { maybeAddAnnotation(validationAnnotations.regexPattern(it)) }

                // Size Restrictions for Strings
                val (min, max) = Pair(info.minLength, info.maxLength)
                if (min != null || max != null) {
                    maybeAddAnnotation(
                        validationAnnotations.lengthRestriction(min, max),
                    )
                }

                // Numeric value validation
                info.minimum?.let {
                    maybeAddAnnotation(
                        validationAnnotations.minRestriction(it, info.exclusiveMinimum ?: false),
                    )
                }
                info.maximum?.let {
                    maybeAddAnnotation(
                        validationAnnotations.maxRestriction(it, info.exclusiveMaximum ?: false),
                    )
                }
            }

            is PropertyInfo.CollectionValidation -> {
                // Size Restrictions for collections
                val (minCollLen, maxCollLen) = Pair(info.minItems, info.maxItems)
                if (minCollLen != null || maxCollLen != null) {
                    maybeAddAnnotation(
                        validationAnnotations.lengthRestriction(
                            minCollLen,
                            maxCollLen,
                        ),
                    )
                }
            }

            else -> {}
        }

        when (val typeInfo = info.typeInfo) {
            is KotlinTypeInfo.Map -> {
                if (typeInfo.parameterizedType.isComplexType) {
                    maybeAddAnnotation(validationAnnotations.fieldValid())
                }
            }

            is KotlinTypeInfo.Array -> {
                if (typeInfo.parameterizedType.isComplexType) {
                    maybeAddAnnotation(validationAnnotations.fieldValid())
                }
            }

            else -> {
                if (typeInfo.isComplexType) {
                    maybeAddAnnotation(validationAnnotations.fieldValid())
                }
            }
        }
    }

    private fun PropertySpec.Builder.maybeAddAnnotation(annotationSpec: AnnotationSpec?) =
        if (annotationSpec != null) addAnnotation(annotationSpec) else this
}
