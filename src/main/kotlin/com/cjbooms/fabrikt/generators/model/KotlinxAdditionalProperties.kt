package com.cjbooms.fabrikt.generators.model

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.joinToCode

internal object KotlinxAdditionalProperties {
    val mutableMapOfJsonElement: TypeName =
        ClassName("kotlin.collections", "MutableMap").parameterizedBy(
            String::class.asTypeName(),
            ClassName("kotlinx.serialization.json", "JsonElement"),
        )

    fun helperSpec(): TypeSpec {
        val t = TypeVariableName("T", Any::class)
        val kSerializerT = ClassName("kotlinx.serialization", "KSerializer").parameterizedBy(t)
        val additionalPropertiesType = LambdaTypeName.get(parameters = arrayOf(t), returnType = mutableMapOfJsonElement)
        val discriminatorType = String::class.asTypeName().copy(nullable = true)

        val strategyType = ClassName("", "Strategy")
        val strategySerializerType = ClassName("", "StrategySerializer")
        val serializersModule = ClassName("kotlinx.serialization.modules", "SerializersModule")
        val serializersModuleBuilder = ClassName("kotlinx.serialization.modules", "SerializersModuleBuilder")
        val contextualMember = MemberName("kotlinx.serialization.modules", "contextual")

        val strategyInterface =
            TypeSpec
                .interfaceBuilder("Strategy")
                .addModifiers(KModifier.PUBLIC)
                .addFunction(
                    FunSpec
                        .builder("deserialize")
                        .addModifiers(KModifier.PUBLIC, KModifier.ABSTRACT)
                        .addTypeVariable(t)
                        .addParameter("decoder", ClassName("kotlinx.serialization.json", "JsonDecoder"))
                        .addParameter("generated", kSerializerT)
                        .addParameter("additionalProperties", additionalPropertiesType)
                        .addParameter("discriminator", discriminatorType)
                        .returns(t)
                        .build(),
                ).addFunction(
                    FunSpec
                        .builder("serialize")
                        .addModifiers(KModifier.PUBLIC, KModifier.ABSTRACT)
                        .addTypeVariable(t)
                        .addParameter("encoder", ClassName("kotlinx.serialization.json", "JsonEncoder"))
                        .addParameter("value", t)
                        .addParameter("generated", kSerializerT)
                        .addParameter("additionalProperties", additionalPropertiesType)
                        .build(),
                ).build()

        val strategySerializerClass =
            TypeSpec
                .classBuilder("StrategySerializer")
                .addModifiers(KModifier.PUBLIC)
                .addSuperinterface(ClassName("kotlinx.serialization", "KSerializer").parameterizedBy(strategyType))
                .primaryConstructor(
                    FunSpec
                        .constructorBuilder()
                        .addParameter("strategy", strategyType)
                        .build(),
                ).addProperty(
                    PropertySpec
                        .builder("strategy", strategyType, KModifier.PUBLIC)
                        .initializer("strategy")
                        .build(),
                ).addProperty(
                    PropertySpec
                        .builder(
                            "descriptor",
                            ClassName("kotlinx.serialization.descriptors", "SerialDescriptor"),
                            KModifier.OVERRIDE,
                        ).initializer(
                            "%M(%S, %T.STRING)",
                            MemberName("kotlinx.serialization.descriptors", "PrimitiveSerialDescriptor"),
                            "AdditionalPropertiesSerializer.Strategy",
                            ClassName("kotlinx.serialization.descriptors", "PrimitiveKind"),
                        ).build(),
                ).addFunction(
                    FunSpec
                        .builder("deserialize")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("decoder", ClassName("kotlinx.serialization.encoding", "Decoder"))
                        .returns(strategyType)
                        .addStatement("return strategy")
                        .build(),
                ).addFunction(
                    FunSpec
                        .builder("serialize")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("encoder", ClassName("kotlinx.serialization.encoding", "Encoder"))
                        .addParameter("value", strategyType)
                        .build(),
                ).build()

        val companionObject =
            TypeSpec
                .companionObjectBuilder()
                .addModifiers(KModifier.PUBLIC)
                .addFunction(
                    FunSpec
                        .builder("module")
                        .addModifiers(KModifier.PUBLIC)
                        .addParameter("strategy", strategyType)
                        .returns(serializersModule)
                        .addStatement(
                            "return %T { %M(%T::class, %T(strategy)) }",
                            serializersModule,
                            contextualMember,
                            strategyType,
                            strategySerializerType,
                        ).build(),
                ).addFunction(
                    FunSpec
                        .builder("additionalPropertiesStrategy")
                        .addModifiers(KModifier.PUBLIC)
                        .receiver(serializersModuleBuilder)
                        .addParameter("strategy", strategyType)
                        .addStatement(
                            "%M(%T::class, %T(strategy))",
                            contextualMember,
                            strategyType,
                            strategySerializerType,
                        ).build(),
                ).addFunction(
                    FunSpec
                        .builder("getAdditionalPropertiesStrategy")
                        .addModifiers(KModifier.PUBLIC)
                        .receiver(serializersModule)
                        .returns(strategyType.copy(nullable = true))
                        .addStatement(
                            "return (getContextual(%T::class) as? %T)?.strategy",
                            strategyType,
                            strategySerializerType,
                        ).build(),
                ).build()

        val primaryConstructor =
            FunSpec
                .constructorBuilder()
                .addParameter("generated", kSerializerT)
                .addParameter("additionalProperties", additionalPropertiesType)
                .addParameter(
                    ParameterSpec
                        .builder("discriminator", discriminatorType)
                        .defaultValue("null")
                        .build(),
                ).build()

        val generatedProperty =
            PropertySpec
                .builder("generated", kSerializerT, KModifier.PRIVATE)
                .initializer("generated")
                .build()

        val additionalPropertiesProperty =
            PropertySpec
                .builder("additionalProperties", additionalPropertiesType, KModifier.PRIVATE)
                .initializer("additionalProperties")
                .build()

        val discriminatorProperty =
            PropertySpec
                .builder("discriminator", discriminatorType, KModifier.PRIVATE)
                .initializer("discriminator")
                .build()

        val descriptorProperty =
            PropertySpec
                .builder(
                    "descriptor",
                    ClassName("kotlinx.serialization.descriptors", "SerialDescriptor"),
                    KModifier.OVERRIDE,
                ).initializer("generated.descriptor")
                .build()

        val knownKeysFunction =
            FunSpec
                .builder("knownKeys")
                .addModifiers(KModifier.PRIVATE)
                .addParameter("json", ClassName("kotlinx.serialization.json", "Json"))
                .returns(ClassName("kotlin.collections", "Set").parameterizedBy(String::class.asTypeName()))
                .addStatement("val strategy = json.configuration.namingStrategy")
                .beginControlFlow("val names = (0 until descriptor.elementsCount).map { index ->")
                .addStatement("val name = descriptor.getElementName(index)")
                .addStatement("strategy?.serialNameForJson(descriptor, index, name) ?: name")
                .endControlFlow()
                .addStatement("return (names + listOfNotNull(discriminator)).toSet()")
                .build()

        val deserializeFunction =
            FunSpec
                .builder("deserialize")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("decoder", ClassName("kotlinx.serialization.encoding", "Decoder"))
                .returns(t)
                .addStatement(
                    "val input = decoder as? %T ?: return generated.deserialize(decoder)",
                    ClassName("kotlinx.serialization.json", "JsonDecoder"),
                ).addStatement("val strategy = input.serializersModule.getAdditionalPropertiesStrategy()")
                .beginControlFlow("if (strategy != null)")
                .addStatement("return strategy.deserialize(input, generated, additionalProperties, discriminator)")
                .endControlFlow()
                .addStatement("val known = knownKeys(input.json)")
                .addStatement(
                    "val (declared, extras) = input.decodeJsonElement().%M.entries.partition { it.key in known }",
                    MemberName("kotlinx.serialization.json", "jsonObject"),
                ).addStatement(
                    "val value = input.json.decodeFromJsonElement(generated, %T(declared.filter { it.key != discriminator }.associate { it.key to it.value }))",
                    ClassName("kotlinx.serialization.json", "JsonObject"),
                ).addStatement("additionalProperties(value).putAll(extras.associate { it.key to it.value })")
                .addStatement("return value")
                .build()

        val serializeFunction =
            FunSpec
                .builder("serialize")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("encoder", ClassName("kotlinx.serialization.encoding", "Encoder"))
                .addParameter("value", t)
                .beginControlFlow("val output = encoder as? %T ?: run", ClassName("kotlinx.serialization.json", "JsonEncoder"))
                .addStatement("generated.serialize(encoder, value)")
                .addStatement("return")
                .endControlFlow()
                .addStatement("val strategy = output.serializersModule.getAdditionalPropertiesStrategy()")
                .beginControlFlow("if (strategy != null)")
                .addStatement("strategy.serialize(output, value, generated, additionalProperties)")
                .addStatement("return")
                .endControlFlow()
                .addStatement(
                    "val declared = output.json.encodeToJsonElement(generated, value).%M",
                    MemberName("kotlinx.serialization.json", "jsonObject"),
                ).addStatement(
                    "output.encodeJsonElement(%T(additionalProperties(value) + declared))",
                    ClassName("kotlinx.serialization.json", "JsonObject"),
                ).build()

        return TypeSpec
            .classBuilder("AdditionalPropertiesSerializer")
            .addModifiers(KModifier.PUBLIC)
            .addAnnotation(
                AnnotationSpec
                    .builder(ClassName("kotlin", "OptIn"))
                    .addMember("%T::class", ClassName("kotlinx.serialization", "ExperimentalSerializationApi"))
                    .build(),
            ).addTypeVariable(t)
            .addSuperinterface(kSerializerT)
            .primaryConstructor(primaryConstructor)
            .addProperty(generatedProperty)
            .addProperty(additionalPropertiesProperty)
            .addProperty(discriminatorProperty)
            .addProperty(descriptorProperty)
            .addType(strategyInterface)
            .addType(strategySerializerClass)
            .addType(companionObject)
            .addFunction(knownKeysFunction)
            .addFunction(deserializeFunction)
            .addFunction(serializeFunction)
            .build()
    }

    fun addSerializer(
        builder: TypeSpec.Builder,
        model: ClassName,
        mapProperty: String,
        discriminator: String?,
    ): TypeSpec.Builder {
        builder.annotations.removeAll { it.typeName == ClassName("kotlinx.serialization", "Serializable") }
        builder.addAnnotation(
            AnnotationSpec
                .builder(ClassName("kotlin", "OptIn"))
                .addMember("%T::class", ClassName("kotlinx.serialization", "ExperimentalSerializationApi"))
                .build(),
        )
        builder.addAnnotation(ClassName("kotlinx.serialization", "KeepGeneratedSerializer"))
        builder.addAnnotation(
            AnnotationSpec
                .builder(ClassName("kotlinx.serialization", "Serializable"))
                .addMember("with = %T::class", model.nestedClass("Serializer"))
                .build(),
        )
        val arguments =
            listOfNotNull(
                CodeBlock.of("%T.generatedSerializer()", model),
                CodeBlock.of("%T::%N", model, mapProperty),
                discriminator?.let { CodeBlock.of("%S", it) },
            ).joinToCode()
        val serializerObject =
            TypeSpec
                .objectBuilder("Serializer")
                .addSuperinterface(
                    ClassName("kotlinx.serialization", "KSerializer").parameterizedBy(model),
                    CodeBlock.of("%T(%L)", ClassName(model.packageName, "AdditionalPropertiesSerializer"), arguments),
                ).build()
        builder.addType(serializerObject)
        return builder
    }

    fun usesHelper(spec: TypeSpec): Boolean =
        spec.annotations.any { it.typeName == ClassName("kotlinx.serialization", "KeepGeneratedSerializer") }
}
