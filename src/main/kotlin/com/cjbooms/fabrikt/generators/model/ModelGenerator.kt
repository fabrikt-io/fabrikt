package com.cjbooms.fabrikt.generators.model

import com.cjbooms.fabrikt.cli.ExternalReferencesResolutionMode
import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType
import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType.SEALED_INTERFACES_FOR_ONE_OF
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.ClassSettings
import com.cjbooms.fabrikt.generators.GeneratorUtils.toClassName
import com.cjbooms.fabrikt.generators.GeneratorUtils.toKDoc
import com.cjbooms.fabrikt.generators.GeneratorUtils.toObjectTypeSpec
import com.cjbooms.fabrikt.generators.MutableSettings
import com.cjbooms.fabrikt.generators.PropertyUtils.addToClass
import com.cjbooms.fabrikt.generators.PropertyUtils.isNullable
import com.cjbooms.fabrikt.generators.TypeFactory.createList
import com.cjbooms.fabrikt.generators.TypeFactory.createMapOfMapsStringToStringAny
import com.cjbooms.fabrikt.generators.TypeFactory.createMapOfStringToNonNullType
import com.cjbooms.fabrikt.generators.TypeFactory.createMapOfStringToType
import com.cjbooms.fabrikt.generators.TypeFactory.createMutableMapOfMapsStringToStringType
import com.cjbooms.fabrikt.generators.TypeFactory.createMutableMapOfStringToType
import com.cjbooms.fabrikt.generators.TypeFactory.createSet
import com.cjbooms.fabrikt.generators.ValidationAnnotations
import com.cjbooms.fabrikt.generators.model.JacksonMetadata.basePolymorphicType
import com.cjbooms.fabrikt.generators.model.JacksonMetadata.polymorphicSubTypes
import com.cjbooms.fabrikt.model.SerializationAnnotations
import com.cjbooms.fabrikt.model.Destinations.modelsPackage
import com.cjbooms.fabrikt.model.GeneratedType
import com.cjbooms.fabrikt.model.KotlinTypeInfo
import com.cjbooms.fabrikt.model.ModelType
import com.cjbooms.fabrikt.model.Models
import com.cjbooms.fabrikt.model.PropertyInfo
import com.cjbooms.fabrikt.model.PropertyInfo.Companion.HTTP_SETTINGS
import com.cjbooms.fabrikt.model.PropertyInfo.Companion.topLevelProperties
import com.cjbooms.fabrikt.model.SchemaInfo
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.KaizenParserExtensions.findOneOfSuperInterface
import com.cjbooms.fabrikt.util.KaizenParserExtensions.getDiscriminatorForInLinedObjectUnderAllOf
import com.cjbooms.fabrikt.util.KaizenParserExtensions.getSchemaRefName
import com.cjbooms.fabrikt.util.KaizenParserExtensions.getSuperType
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isComplexTypedAdditionalProperties
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isInlinedEnumDefinition
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isInlinedObjectDefinition
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isInlinedOneOfSuperInterface
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isInlinedTypedAdditionalProperties
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isOneOfWhereAllTypesInheritFromACommonAllOfSuperType
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isOneOfSuperInterface
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isOneOfResolvingToAnyType
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isPolymorphicSubType
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isPolymorphicSuperType
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isSimpleType
import com.cjbooms.fabrikt.util.KaizenParserExtensions.mappingKeyForSchemaName
import com.cjbooms.fabrikt.util.KaizenParserExtensions.mappingKeys
import com.cjbooms.fabrikt.util.KaizenParserExtensions.safeName
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.NormalisedString.toEnumName
import com.reprezen.jsonoverlay.Overlay
import com.reprezen.kaizen.oasparser.OpenApi3Parser
import com.reprezen.kaizen.oasparser.model3.Discriminator
import com.reprezen.kaizen.oasparser.model3.OpenApi3
import com.reprezen.kaizen.oasparser.model3.Schema
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import java.io.Serializable
import java.net.MalformedURLException
import java.net.URL
import java.util.logging.Logger

class ModelGenerator(
    private val packages: Packages,
    private val sourceApi: SourceApi,
) {
    private val options = MutableSettings.modelOptions
    private val validationAnnotations: ValidationAnnotations = MutableSettings.validationLibrary.annotations
    private val serializationAnnotations: SerializationAnnotations = MutableSettings.effectiveSerializationAnnotations
    private val externalRefResolutionMode: ExternalReferencesResolutionMode = MutableSettings.externalRefResolutionMode

    companion object {
        private val logger = Logger.getGlobal()
        fun toModelType(
            basePackage: String,
            typeInfo: KotlinTypeInfo,
            isNullable: Boolean = false,
        ): TypeName {
            val className =
                toClassName(
                    basePackage,
                    typeInfo,
                )
            val typeName = when (typeInfo) {
                is KotlinTypeInfo.Array -> {
                    val elementType = toModelType(
                        basePackage,
                        typeInfo.parameterizedType,
                        typeInfo.isParameterizedTypeNullable
                    )
                    val annotatedElementType = MutableSettings.serializationLibrary.serializationAnnotations
                        .annotateArrayElementType(elementType, typeInfo.parameterizedType)
                    if (typeInfo.hasUniqueItems) {
                        createSet(annotatedElementType)
                    } else {
                        createList(annotatedElementType)
                    }
                }

                is KotlinTypeInfo.Map ->
                    when (val paramType = typeInfo.parameterizedType) {
                        is KotlinTypeInfo.UntypedObjectAdditionalProperties -> createMapOfMapsStringToStringAny()
                        is KotlinTypeInfo.UnknownAdditionalProperties ->
                            createMapOfStringToType(
                                toClassName(
                                    basePackage,
                                    paramType,
                                ),
                            )

                        is KotlinTypeInfo.GeneratedTypedAdditionalProperties ->
                            createMapOfStringToType(
                                toClassName(
                                    basePackage,
                                    paramType,
                                ),
                            )

                        else -> createMapOfStringToType(
                            toModelType(
                                basePackage,
                                paramType,
                            ),
                        )
                    }

                is KotlinTypeInfo.UntypedObject -> createMapOfStringToType(className)
                is KotlinTypeInfo.UnknownAdditionalProperties -> createMutableMapOfStringToType(className)
                is KotlinTypeInfo.UntypedObjectAdditionalProperties -> createMutableMapOfStringToType(className)
                is KotlinTypeInfo.GeneratedTypedAdditionalProperties -> createMutableMapOfStringToType(className)
                is KotlinTypeInfo.MapTypeAdditionalProperties -> createMutableMapOfMapsStringToStringType(
                    toModelType(
                        basePackage,
                        typeInfo.parameterizedType,
                    ),
                )
                is KotlinTypeInfo.SimpleTypedAdditionalProperties -> createMutableMapOfStringToType(
                    toModelType(
                        basePackage,
                        typeInfo.parameterizedType,
                    ),
                )

                else -> className
            }
            return if (isNullable) typeName.copy(nullable = true) else typeName
        }

        private fun toClassName(basePackage: String, typeInfo: KotlinTypeInfo): ClassName =
            when {
                typeInfo.modelKClass == GeneratedType::class ->
                    generatedType(basePackage, typeInfo.generatedModelClassName!!)

                typeInfo is KotlinTypeInfo.MapTypeAdditionalProperties ->
                    generatedType(basePackage, typeInfo.parameterizedType.generatedModelClassName!!)

                else -> typeInfo.modelKClass.asTypeName()
            }

        fun generatedType(basePackage: String, modelName: String) = ClassName(modelsPackage(basePackage), modelName)
    }

    private val externalApiSchemas = mutableMapOf<String, MutableSet<String>>()

    fun generate(): Models {
        val models: MutableSet<TypeSpec> = createModels(sourceApi.openApi3, sourceApi.allSchemas)
        externalApiSchemas.forEach { externalReferences ->
            val api = OpenApi3Parser().parse(URL(externalReferences.key))
            val schemas = api.schemas.entries.map { (key, schema) -> SchemaInfo(key, schema) }
                .filterByExternalRefResolutionMode(externalReferences)
            val externalModels = createModels(api, schemas)
            externalModels.forEach { additionalModel ->
                if (models.none { it.name == additionalModel.name }) models.add(additionalModel)
            }
        }
        return Models(models.map { ModelType(it, packages.base) })
    }

    private fun createModels(api: OpenApi3, schemas: List<SchemaInfo>) = schemas
        .filterNot { it.schema.isSimpleType() }
        .filterNot { it.schema.isOneOfWhereAllTypesInheritFromACommonAllOfSuperType() && SEALED_INTERFACES_FOR_ONE_OF !in options }
        .filterNot { it.schema.isOneOfResolvingToAnyType() }
        .flatMap { schemaInfo ->
            val properties = schemaInfo.schema.topLevelProperties(HTTP_SETTINGS, api, schemaInfo.schema)
            when {
                properties.isNotEmpty() ||
                schemaInfo.typeInfo is KotlinTypeInfo.Enum ||
                schemaInfo.schema.findOneOfSuperInterface(schemas.map { it.schema }).isNotEmpty() -> {
                    val primaryModel = buildPrimaryModel(api, schemaInfo, properties, schemas)
                    val inlinedModels = buildInLinedModels(properties, schemaInfo.schema, schemaInfo.schema.getDocumentUrl())
                    listOf(primaryModel) + inlinedModels
                }
                schemaInfo.typeInfo is KotlinTypeInfo.Array -> {
                    buildInlinedListDefinition(
                        schema = schemaInfo.schema,
                        schemaName = schemaInfo.schema.safeName(),
                        enclosingSchema = schemaInfo.schema,
                        apiDocUrl = schemaInfo.schema.getDocumentUrl(),
                    )
                }
                else -> {
                    emptyList()
                }
            }
        }.toMutableSet()

    private fun buildPrimaryModel(
        api: OpenApi3,
        schemaInfo: SchemaInfo,
        properties: Collection<PropertyInfo>,
        allSchemas: List<SchemaInfo>,
    ): TypeSpec {
        val modelName = ModelNameRegistry.getOrRegister(schemaInfo)
        val schemaName = schemaInfo.schema.getSchemaRefName()
        return when {
            schemaInfo.schema.isOneOfSuperInterface() && SEALED_INTERFACES_FOR_ONE_OF in options -> oneOfSuperInterface(
                modelName = modelName,
                discriminator = schemaInfo.schema.discriminator,
                allSchemas = allSchemas,
                members = schemaInfo.schema.oneOfSchemas,
                oneOfSuperInterfaces = schemaInfo.schema.findOneOfSuperInterface(allSchemas.map { it.schema }),
            )

            schemaInfo.schema.isPolymorphicSuperType() && schemaInfo.schema.isPolymorphicSubType(api) ->
                polymorphicSuperSubType(
                    modelName,
                    schemaName,
                    properties,
                    checkNotNull(schemaInfo.schema.getDiscriminatorForInLinedObjectUnderAllOf()),
                    schemaInfo.schema.getSuperType(api)!!.let { SchemaInfo(it.name, it) },
                    schemaInfo.schema.extensions,
                    schemaInfo.schema.findOneOfSuperInterface(allSchemas.map { it.schema }),
                    allSchemas,
                )

            schemaInfo.schema.isPolymorphicSuperType() -> polymorphicSuperType(
                modelName,
                schemaName,
                properties,
                schemaInfo.schema.discriminator,
                schemaInfo.schema.extensions,
                schemaInfo.schema.findOneOfSuperInterface(allSchemas.map { it.schema }),
                allSchemas,
            )

            schemaInfo.schema.isPolymorphicSubType(api) -> polymorphicSubType(
                modelName,
                schemaName,
                properties,
                schemaInfo.schema.getSuperType(api)!!.let { SchemaInfo(it.name, it) },
                schemaInfo.schema.extensions,
                schemaInfo.schema.findOneOfSuperInterface(allSchemas.map { it.schema }),
            )

            schemaInfo.typeInfo is KotlinTypeInfo.Enum -> buildEnumClass(schemaInfo.schema, schemaInfo.typeInfo)
            else -> standardDataClass(
                modelName = modelName,
                schemaName = schemaName,
                properties = properties,
                schema = schemaInfo.schema,
                oneOfInterfaces = schemaInfo.schema.findOneOfSuperInterface(allSchemas.map { it.schema }),
            )
        }
    }



    private fun buildInLinedModels(
        topLevelProperties: Collection<PropertyInfo>,
        enclosingSchema: Schema,
        apiDocUrl: String,
    ): List<TypeSpec> = topLevelProperties.flatMap {
        if (it.schema.isInExternalDocument(apiDocUrl)) {
            it.schema.captureMissingExternalSchemas(apiDocUrl)
            emptySet()
        } else {
            when (it) {
                is PropertyInfo.ObjectInlinedField -> {
                    when {
                        it.isInherited -> {
                            emptySet() // Rely on the parent definition
                        }
                        it.schema.isOneOfSuperInterface() && SEALED_INTERFACES_FOR_ONE_OF in options -> {
                            setOf(
                                oneOfSuperInterface(
                                    modelName = ModelNameRegistry.getOrRegister(it.schema, enclosingSchema),
                                    discriminator = it.schema.discriminator,
                                    allSchemas = sourceApi.allSchemas,
                                    members = it.schema.oneOfSchemas,
                                    oneOfSuperInterfaces = it.schema.findOneOfSuperInterface(sourceApi.allSchemas.map { it.schema })
                                )
                            )
                        }
                        else -> {
                            val props = it.schema.topLevelProperties(HTTP_SETTINGS, sourceApi.openApi3, enclosingSchema)
                            val currentModel = standardDataClass(
                                ModelNameRegistry.getOrRegister(it.schema, enclosingSchema),
                                it.name,
                                props,
                                it.schema,
                                oneOfInterfaces = emptySet(),
                            )
                            val inlinedModels = buildInLinedModels(props, enclosingSchema, apiDocUrl)
                            inlinedModels + currentModel
                        }
                    }
                }

                is PropertyInfo.ObjectRefField -> emptySet() // Not an inlined definition, so do nothing
                is PropertyInfo.MapField ->
                    buildMapModel(it)?.let { mapModel -> setOf(mapModel) } ?: emptySet()

                is PropertyInfo.AdditionalProperties ->
                    if (it.schema.isComplexTypedAdditionalProperties("additionalProperties")) {
                        setOf(
                            standardDataClass(
                                modelName = ModelNameRegistry.getOrRegister(it.schema, valueSuffix = it.schema.isInlinedTypedAdditionalProperties()),
                                schemaName = it.name,
                                properties = it.schema.topLevelProperties(HTTP_SETTINGS, sourceApi.openApi3, enclosingSchema),
                                schema = it.schema,
                                oneOfInterfaces = emptySet(),
                            ),
                        )
                    } else {
                        emptySet()
                    }

                is PropertyInfo.Field ->
                    if (it.typeInfo is KotlinTypeInfo.Enum && !it.isInherited) {
                        setOf(buildEnumClass(it.schema, it.typeInfo))
                    } else {
                        emptySet()
                    }

                is PropertyInfo.ListField ->
                    if (it.isInherited) {
                        emptySet() // Rely on the parent definition
                    } else {
                        buildInlinedListDefinition(it.schema, it.name, enclosingSchema, apiDocUrl)
                    }
                is PropertyInfo.OneOfAny -> emptySet()
            }
        }
    }

    private fun buildInlinedListDefinition(
        schema: Schema,
        schemaName: String,
        enclosingSchema: Schema,
        apiDocUrl: String,
    ): Collection<TypeSpec> =
        schema.itemsSchema.let { items ->
            when {
                items.isInlinedObjectDefinition() ->
                    items.topLevelProperties(HTTP_SETTINGS, sourceApi.openApi3, enclosingSchema).let { props ->
                        buildInLinedModels(
                            topLevelProperties = props,
                            enclosingSchema = enclosingSchema,
                            apiDocUrl = apiDocUrl,
                        ) + standardDataClass(
                            modelName = ModelNameRegistry.getOrRegister(schema, enclosingSchema),
                            schemaName = schemaName,
                            properties = props,
                            schema = schema,
                            oneOfInterfaces = emptySet(),
                        )
                    }

                items.isInlinedEnumDefinition() ->
                    setOf(
                        buildEnumClass(
                            items,
                            KotlinTypeInfo.from(items, "items", enclosingSchema) as KotlinTypeInfo.Enum,
                        ),
                    )

                items.isInlinedOneOfSuperInterface() && SEALED_INTERFACES_FOR_ONE_OF in options ->
                    setOf(
                        oneOfSuperInterface(
                            modelName = ModelNameRegistry.getOrRegister(schema, enclosingSchema),
                            discriminator = items.discriminator,
                            allSchemas = sourceApi.allSchemas,
                            members = items.oneOfSchemas,
                            oneOfSuperInterfaces = items.findOneOfSuperInterface(sourceApi.allSchemas.map { it.schema })
                        )
                    )

                else -> emptySet()
            }
        }

    private fun Schema.captureMissingExternalSchemas(apiDocUrl: String, depth: Int = 0) {
        nestedSchemas().forEach { schema ->
            val docUrl = schema.getDocumentUrl()
            if (docUrl != apiDocUrl) {
                try {
                    externalApiSchemas.getOrPut(docUrl) { mutableSetOf() }.add(schema.safeName())
                } catch (_: MalformedURLException) {
                    // skip
                }
            }
            if (depth < 10) schema.captureMissingExternalSchemas(apiDocUrl, depth + 1)
        }
    }

    private fun Schema.isInExternalDocument(apiDocUrl: String): Boolean {
        val docUrl = getDocumentUrl()
        return docUrl != apiDocUrl
    }

    private fun Schema.getDocumentUrl(): String {
        val positionInfo = Overlay.of(this).positionInfo?.orElse(null)
        return positionInfo?.documentUrl ?: "Not Found"
    }

    private fun Schema.nestedSchemas() =
        (
            allOfSchemas + anyOfSchemas + oneOfSchemas + itemsSchema + additionalPropertiesSchema +
                this + this.properties.map { it.value }
            )
            .filterNotNull()
            .filter { Overlay.of(it).isPresent }

    private fun buildEnumClass(schema: Schema, enum: KotlinTypeInfo.Enum): TypeSpec {
        val enumType = generatedType(packages.base, enum.enumClassName)
        val classBuilder = TypeSpec
            .enumBuilder(enumType)
            .apply { schema.toKDoc()?.let { addKdoc(it) } }
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("value", String::class)
                    .build(),
            )
            .addQuarkusReflectionAnnotation()
            .addMicronautIntrospectedAnnotation()
            .addMicronautReflectionAnnotation()

        enum.entries.forEach {
            val enumConstantBuilder = TypeSpec.anonymousClassBuilder()
                .addSuperclassConstructorParameter("%S", it)
            serializationAnnotations.addEnumConstantAnnotation(enumConstantBuilder, it)
            classBuilder.addEnumConstant(
                it.toEnumName(),
                enumConstantBuilder.build(),
            )
        }

        val valuePropSpecBuilder = PropertySpec.builder("value", String::class).initializer("value")
        serializationAnnotations.addEnumPropertyAnnotation(valuePropSpecBuilder)
        classBuilder.addProperty(valuePropSpecBuilder.build())

        // Add toString() override that returns the value property
        val toStringBuilder = FunSpec.builder("toString")
            .addModifiers(KModifier.OVERRIDE)
            .returns(String::class)
            .addStatement("return value")

        classBuilder.addFunction(toStringBuilder.build())

        val companion = TypeSpec.companionObjectBuilder()
            .addProperty(
                PropertySpec.builder("mapping", createMapOfStringToNonNullType(enumType))
                    .initializer("entries.associateBy(%T::value)", enumType)
                    .addModifiers(KModifier.PRIVATE)
                    .build(),
            )
            .addFunction(
                FunSpec.builder("fromValue")
                    .addParameter(ParameterSpec.builder("value", String::class).build())
                    .returns(enumType.copy(nullable = true))
                    .addStatement("return mapping[value]")
                    .build(),
            )
            .build()

        // Only Micronaut Serde needs @Serdeable on enums; Jackson and kotlinx don't annotate enum classes
        if (options.contains(ModelCodeGenOptionType.MICRONAUT_SERDEABLE)) {
            serializationAnnotations.addClassAnnotation(classBuilder)
        }

        return classBuilder.addType(companion).build()
    }

    private fun buildMapModel(mapField: PropertyInfo.MapField): TypeSpec? =
        if (mapField.schema.additionalPropertiesSchema.isComplexTypedAdditionalProperties("additionalProperties")) {
            val schema = mapField.schema.additionalPropertiesSchema
            standardDataClass(
                modelName = ModelNameRegistry.getOrRegister(schema, valueSuffix = schema.isInlinedTypedAdditionalProperties()),
                schemaName = schema.safeName(),
                properties = mapField.schema.additionalPropertiesSchema.topLevelProperties(HTTP_SETTINGS, sourceApi.openApi3),
                schema = schema,
                oneOfInterfaces = emptySet(),
            )
        } else {
            null
        }

    private fun standardDataClass(
        modelName: String,
        schemaName: String,
        properties: Collection<PropertyInfo>,
        schema: Schema,
        oneOfInterfaces: Set<Schema>,
    ): TypeSpec {
        val name = generatedType(packages.base, modelName)
        val generateObject = properties.isEmpty()
        val builder =
            if (generateObject) {
                TypeSpec.objectBuilder(name)
            } else {
                TypeSpec.classBuilder(name)
            }
        val classBuilder = builder
            .apply { schema.toKDoc()?.let { addKdoc(it) } }
            .addSerializableInterface()
            .addQuarkusReflectionAnnotation()
            .addMicronautIntrospectedAnnotation()
            .addMicronautReflectionAnnotation()
            .addCompanionObject()
        for (oneOfInterface in oneOfInterfaces) {
            classBuilder
                .addSuperinterface(generatedType(packages.base, ModelNameRegistry.getOrRegister(oneOfInterface)))

            // determine the mapping key for this schema as a subtype of the oneOf interface
            val mappingKey = oneOfInterface.discriminator.mappingKeyForSchemaName(schemaName)
            if (mappingKey != null) {
                serializationAnnotations.addSubtypeMappingAnnotation(classBuilder, mappingKey)
            }
        }

        if (!generateObject) {
            if (oneOfInterfaces.size == 1) {
                properties.addToClass(
                    schemaName = schemaName,
                    classBuilder = classBuilder,
                    classType = ClassSettings(ClassSettings.PolymorphyType.ONE_OF, schema.extensions),
                )
            } else {
                properties.addToClass(
                    schemaName = schemaName,
                    classBuilder = classBuilder,
                    classType = ClassSettings(ClassSettings.PolymorphyType.NONE, schema.extensions),
                )
            }
        }

        serializationAnnotations.addClassAnnotation(classBuilder)

        val classTypeSpec = classBuilder.build()

        return if (classBuilder.propertySpecs.isNotEmpty()) {
            classTypeSpec
        } else {
            // properties have been filtered out in generation process so return an object instead
            classTypeSpec.toObjectTypeSpec()
        }
    }

    private fun polymorphicSuperSubType(
        modelName: String,
        schemaName: String,
        properties: Collection<PropertyInfo>,
        discriminator: Discriminator,
        superType: SchemaInfo,
        extensions: Map<String, Any>,
        oneOfSuperInterfaces: Set<Schema>,
        allSchemas: List<SchemaInfo>,
    ): TypeSpec = with(FunSpec.constructorBuilder()) {
        TypeSpec.classBuilder(generatedType(packages.base, modelName))
            .buildPolymorphicSubType(
                schemaName,
                properties.filter(PropertyInfo::isInherited),
                superType,
                extensions,
                oneOfSuperInterfaces,
                this,
            )
            .buildPolymorphicSuperType(
                modelName,
                schemaName,
                properties.filterNot(PropertyInfo::isInherited),
                discriminator,
                extensions,
                oneOfSuperInterfaces,
                allSchemas,
                this,
            )
            .build()
    }

    private fun oneOfSuperInterface(
        modelName: String,
        discriminator: Discriminator?,
        allSchemas: List<SchemaInfo>,
        members: List<Schema>,
        oneOfSuperInterfaces: Set<Schema>,
    ): TypeSpec {
        val interfaceBuilder = TypeSpec.interfaceBuilder(generatedType(packages.base, modelName))
            .addModifiers(KModifier.SEALED)

        serializationAnnotations.addClassAnnotation(interfaceBuilder)

        if (discriminator != null && discriminator.propertyName != null) {
            serializationAnnotations.addBasePolymorphicTypeAnnotation(interfaceBuilder, discriminator.propertyName)

            val mappings = getDiscriminatorMappingsOrDefault(discriminator, members, allSchemas, modelName)

            val kotlinMappings = mappings.mapValues { (_, schema) ->
                toModelType(
                    packages.base,
                    KotlinTypeInfo.from(schema.schema, schema.name),
                )
            }
            serializationAnnotations.addPolymorphicSubTypesAnnotation(interfaceBuilder, kotlinMappings)
        }

        for (oneOfSuperInterface in oneOfSuperInterfaces) {
            interfaceBuilder.addSuperinterface(generatedType(packages.base, oneOfSuperInterface.name))
        }

        interfaceBuilder
            .addQuarkusReflectionAnnotation()
            .addMicronautIntrospectedAnnotation()
            .addMicronautReflectionAnnotation()

        return interfaceBuilder.build()
    }

    private fun getDiscriminatorMappingsOrDefault(
        discriminator: Discriminator,
        members: List<Schema>,
        allSchemas: List<SchemaInfo>,
        modelName: String
    ): Map<String, SchemaInfo> {
        val mappings = if (discriminator.mappings.isNullOrEmpty()) {
            // No explicit mappings: default to schema name matching
            members.mapNotNull { member ->
                val schema = allSchemas.find { it.name == member.name }
                if (schema == null) {
                    logger.warning("Could not find schema for member ${member.name} in oneOf super interface $modelName!")
                    null
                } else {
                    member.name to schema  // Key by member name
                }
            }.toMap()
        } else {
            // Explicit mappings present: key by the mapping key (discriminator value)
            discriminator.mappings.mapNotNull { (mappingKey, ref) ->
                val schema = allSchemas.find { ref.endsWith("/${it.name}") }
                if (schema == null) {
                    logger.warning("Mapping $mappingKey -> $ref does not match any schema in oneOf super interface $modelName!")
                    null
                } else {
                    mappingKey to schema  // Key by mapping key (not by schema name)
                }
            }.toMap()
        }
        return mappings
    }

    private fun polymorphicSuperType(
        modelName: String,
        schemaName: String,
        properties: Collection<PropertyInfo>,
        discriminator: Discriminator,
        extensions: Map<String, Any>,
        oneOfSuperInterfaces: Set<Schema>,
        allSchemas: List<SchemaInfo>,
    ): TypeSpec = TypeSpec.classBuilder(generatedType(packages.base, modelName))
        .buildPolymorphicSuperType(
            modelName = modelName,
            schemaName = schemaName,
            properties = properties,
            discriminator = discriminator,
            extensions = extensions,
            oneOfSuperInterfaces = oneOfSuperInterfaces,
            allSchemas = allSchemas,
        )
        .build()

    private fun TypeSpec.Builder.buildPolymorphicSuperType(
        modelName: String,
        schemaName: String,
        properties: Collection<PropertyInfo>,
        discriminator: Discriminator,
        extensions: Map<String, Any>,
        oneOfSuperInterfaces: Set<Schema>,
        allSchemas: List<SchemaInfo>,
        constructorBuilder: FunSpec.Builder = FunSpec.constructorBuilder(),
    ): TypeSpec.Builder {
        this.addModifiers(KModifier.SEALED)
            .addAnnotation(basePolymorphicType(discriminator.propertyName))
            .modifiers.remove(KModifier.DATA)

        for (oneOfSuperInterface in oneOfSuperInterfaces) {
            this.addSuperinterface(generatedType(packages.base, oneOfSuperInterface.name))
        }

        val subTypes = allSchemas
            .filter { model ->
                model.schema.allOfSchemas.any { allOfRef ->
                    ModelNameRegistry.getOrRegister(allOfRef) == modelName &&
                        (
                            allOfRef.discriminator == discriminator ||
                                allOfRef.allOfSchemas.any { it.discriminator == discriminator }
                            )
                }
            }

        val mappings: Map<String, TypeName> = subTypes.map { schemaInfo ->
            discriminator.getDiscriminatorMappings(schemaInfo)
        }.toMapList()
            .firstValueMap()

        val maybeEnumDiscriminator = properties
            .firstOrNull { it.name == discriminator.propertyName }?.typeInfo as? KotlinTypeInfo.Enum

        this.addAnnotation(polymorphicSubTypes(mappings, maybeEnumDiscriminator))
            .addQuarkusReflectionAnnotation()
            .addMicronautIntrospectedAnnotation()
            .addMicronautReflectionAnnotation()

        properties.addToClass(
            schemaName,
            constructorBuilder,
            this,
            ClassSettings(ClassSettings.PolymorphyType.SUPER, extensions),
        )

        serializationAnnotations.addClassAnnotation(this)
        return this
    }

    private fun polymorphicSubType(
        modelName: String,
        schemaName: String,
        properties: Collection<PropertyInfo>,
        superType: SchemaInfo,
        extensions: Map<String, Any>,
        oneOfSuperInterfaces: Set<Schema>,
    ): TypeSpec = TypeSpec.classBuilder(generatedType(packages.base, modelName))
        .buildPolymorphicSubType(schemaName, properties, superType, extensions, oneOfSuperInterfaces).build()

    private fun TypeSpec.Builder.buildPolymorphicSubType(
        schemaName: String,
        allProperties: Collection<PropertyInfo>,
        superType: SchemaInfo,
        extensions: Map<String, Any>,
        oneOfSuperInterfaces: Set<Schema>,
        constructorBuilder: FunSpec.Builder = FunSpec.constructorBuilder(),
    ): TypeSpec.Builder {
        this.addSerializableInterface()
            .addQuarkusReflectionAnnotation()
            .addMicronautIntrospectedAnnotation()
            .addMicronautReflectionAnnotation()
            .addCompanionObject()
            .superclass(
                toModelType(
                    packages.base,
                    KotlinTypeInfo.from(superType.schema, superType.name),
                ),
            )

        for (oneOfSuperInterface in oneOfSuperInterfaces) {
            this.addSuperinterface(generatedType(packages.base, oneOfSuperInterface.name))
        }

        val properties = superType.schema.getDiscriminatorForInLinedObjectUnderAllOf()?.let { discriminator ->
            allProperties.filterNot {
                when (it) {
                    is PropertyInfo.Field ->
                        it.isPolymorphicDiscriminator && it.name != discriminator.propertyName

                    else -> false
                }
            }
        } ?: allProperties

        properties.addToClass(
            schemaName,
            constructorBuilder,
            this,
            ClassSettings(ClassSettings.PolymorphyType.SUB, extensions),
        )

        serializationAnnotations.addClassAnnotation(this)
        return this
    }

    private fun Collection<PropertyInfo>.addToClass(
        schemaName: String,
        constructorBuilder: FunSpec.Builder = FunSpec.constructorBuilder(),
        classBuilder: TypeSpec.Builder,
        classType: ClassSettings,
    ): TypeSpec.Builder {
        this.forEach {
            it.addToClass(
                schemaName = schemaName,
                type = toModelType(
                    packages.base,
                    it.typeInfo,
                    it.isNullable(),
                ),
                parameterizedType = toClassName(
                    packages.base,
                    it.typeInfo,
                ),
                classBuilder = classBuilder,
                constructorBuilder = constructorBuilder,
                classSettings = classType,
                validationAnnotations = validationAnnotations,
                serializationAnnotations = serializationAnnotations,
            )
        }
        if (constructorBuilder.parameters.isNotEmpty() && classBuilder.modifiers.isEmpty()) {
            classBuilder.addModifiers(
                KModifier.DATA,
            )
        }
        sortConstructorParameters(constructorBuilder, classType)
        return classBuilder.primaryConstructor(constructorBuilder.build())
    }

    private fun sortConstructorParameters(constructorBuilder: FunSpec.Builder, classType: ClassSettings) {
        if (classType.polymorphyType != ClassSettings.PolymorphyType.NONE) {
            constructorBuilder.parameters.sortBy { it.defaultValue?.toString() != "null" && it.defaultValue != null }
        }
    }

    private fun Discriminator.getDiscriminatorMappings(schemaInfo: SchemaInfo): Map<String, TypeName> =
        mappingKeys(schemaInfo.schema)
            .filter { it.value == schemaInfo.schema.name || it.key == schemaInfo.schema.name }
            .map {
                it.key to toModelType(
                    packages.base,
                    KotlinTypeInfo.from(schemaInfo.schema, schemaInfo.name),
                )
            }
            .toMap()

    private fun <K, V> List<Map<K, V>>.toMapList(): Map<K, List<V>> =
        asSequence()
            .flatMap {
                it.asSequence()
            }.groupBy({ it.key }, { it.value })

    private fun <K, V> Map<K, List<V>>.firstValueMap(): Map<K, V> =
        map { it.key to it.value.first() }.toMap()

    private fun TypeSpec.Builder.addSerializableInterface(): TypeSpec.Builder {
        if (options.any { it == ModelCodeGenOptionType.JAVA_SERIALIZATION }) {
            this.addSuperinterface(Serializable::class)
        }
        return this
    }

    private fun TypeSpec.Builder.addQuarkusReflectionAnnotation(): TypeSpec.Builder =
        this.addOptionalAnnotation(
            ModelCodeGenOptionType.QUARKUS_REFLECTION,
            "RegisterForReflection".toClassName("io.quarkus.runtime.annotations"),
        )

    private fun TypeSpec.Builder.addMicronautIntrospectedAnnotation(): TypeSpec.Builder =
        this.addOptionalAnnotation(
            ModelCodeGenOptionType.MICRONAUT_INTROSPECTION,
            "Introspected".toClassName("io.micronaut.core.annotation"),
        )

    private fun TypeSpec.Builder.addMicronautReflectionAnnotation(): TypeSpec.Builder =
        this.addOptionalAnnotation(
            ModelCodeGenOptionType.MICRONAUT_REFLECTION,
            "ReflectiveAccess".toClassName("io.micronaut.core.annotation"),
        )

    private fun TypeSpec.Builder.addCompanionObject(): TypeSpec.Builder {
        if (options.any { it == ModelCodeGenOptionType.INCLUDE_COMPANION_OBJECT }) {
            this.addType(TypeSpec.companionObjectBuilder().build())
        }
        return this
    }

    private fun TypeSpec.Builder.addOptionalAnnotation(
        optionType: ModelCodeGenOptionType,
        type: ClassName,
    ): TypeSpec.Builder {
        if (options.any { it == optionType }) {
            this.addAnnotation(
                AnnotationSpec.builder(type).build(),
            )
        }
        return this
    }

    private fun List<SchemaInfo>.filterByExternalRefResolutionMode(
        externalReferences: Map.Entry<String, MutableSet<String>>,
    ) = when (externalRefResolutionMode) {
            ExternalReferencesResolutionMode.TARGETED -> this.filter { apiSchema -> externalReferences.value.contains(apiSchema.name) }
            else -> this
        }
}
