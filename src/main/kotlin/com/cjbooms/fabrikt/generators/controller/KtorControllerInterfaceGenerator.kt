package com.cjbooms.fabrikt.generators.controller

import com.cjbooms.fabrikt.cli.ControllerCodeGenOptionType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.GeneratorUtils.isUnit
import com.cjbooms.fabrikt.generators.GeneratorUtils.splitByType
import com.cjbooms.fabrikt.generators.GeneratorUtils.toIncomingParameters
import com.cjbooms.fabrikt.generators.GeneratorUtils.toKCodeName
import com.cjbooms.fabrikt.generators.controller.ControllerGeneratorUtils.SecuritySupport
import com.cjbooms.fabrikt.generators.controller.ControllerGeneratorUtils.toSuccessResponseType
import com.cjbooms.fabrikt.generators.controller.ControllerGeneratorUtils.securitySupport
import com.cjbooms.fabrikt.model.BodyParameter
import com.cjbooms.fabrikt.model.ControllerLibraryType
import com.cjbooms.fabrikt.model.ControllerType
import com.cjbooms.fabrikt.model.HeaderParam
import com.cjbooms.fabrikt.model.IncomingParameter
import com.cjbooms.fabrikt.model.KotlinTypeInfo
import com.cjbooms.fabrikt.model.KotlinTypes
import com.cjbooms.fabrikt.model.PathParam
import com.cjbooms.fabrikt.model.QueryParam
import com.cjbooms.fabrikt.model.RequestParameter
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.FileUtils.addFileDisclaimer
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isSingleResource
import com.cjbooms.fabrikt.util.KaizenParserExtensions.routeToPaths
import com.cjbooms.fabrikt.util.NormalisedString.camelCase
import com.cjbooms.fabrikt.util.toUpperCase
import com.reprezen.kaizen.oasparser.model3.Operation
import com.reprezen.kaizen.oasparser.model3.Path
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import kotlin.reflect.KClass

private const val TYPED_APPLICATION_CALL_CLASS_NAME = "TypedApplicationCall"

/**
 * Generates controller interface and routing functions for Ktor.
 *
 * Ktor in its nature is very flexible and imposes very few constraints on how you structure your application. In order
 * to be able to generate code we however have to add structure.
 *
 * This generator creates a routing function (as an extension function on Ktor's Route class) which you use to mount
 * the route on your Ktor server. The route function handles basic parsing of parameters and delegates the actual
 * handling of the request to a controller interface which you implement.
 */
class KtorControllerInterfaceGenerator(
    private val packages: Packages,
    private val api: SourceApi,
    private val options: Set<ControllerCodeGenOptionType> = emptySet(),
) : ControllerInterfaceGenerator {
    override fun generate(): KtorControllers {
        val controllerInterfaces = api.openApi3.routeToPaths().map { (resourceName, paths) ->
            val controllerBuilder = TypeSpec.interfaceBuilder(ControllerGeneratorUtils.controllerName(resourceName))

            val routeFunBuilder = FunSpec.builder("${resourceName.camelCase()}Routes")
                .receiver(ClassName("io.ktor.server.routing", "Route"))
                .addParameter(
                    "controller",
                    ClassName(packages.controllers, ControllerGeneratorUtils.controllerName(resourceName))
                )
                .addKdoc("Mounts all routes for the $resourceName resource\n\n")

            paths.forEach { path ->
                path.value.operations
                    .filter { (verb, _) -> verb.toUpperCase() != "HEAD" }
                    .forEach { (verb, operation) ->
                        // add route handler
                        val routeCode = buildRouteCode(operation, verb, path)
                        routeFunBuilder.addCode(routeCode)
                            routeFunBuilder.addKdoc("- ${verb.toUpperCase()} ${path.key} ${(operation.summary ?: operation.description).orEmpty()}\n")

                        // generate controller interface function
                        val controllerFun = buildControllerFun(operation, verb, path)

                        controllerBuilder.addFunction(controllerFun)
                    }
            }

            // add the companion object with the route functions
            controllerBuilder.addType(
                TypeSpec.companionObjectBuilder()
                    .addFunction(routeFunBuilder.build())
                    .addFunction(getTypedFun)
                    .addFunction(getTypedOrFailFun)
                    .addFunction(getOrFailFun)
                    .build()
            )

            controllerBuilder.build()
        }

        return KtorControllers(
            controllerInterfaces.map { ControllerType(it, packages.base) }.toSet()
        )
    }

    /**
     * Builds the base controller function that takes in all parameters.
     */
    private fun buildControllerFun(operation: Operation, verb: String, path: Map.Entry<String, Path>): FunSpec {
        val methodName = getMethodName(operation, verb, path)
        val builder = FunSpec.builder(methodName)
            .addModifiers(setOf(KModifier.SUSPEND, KModifier.ABSTRACT))

        val params = operation.toIncomingParameters(packages.base, path.value.parameters, emptyList())
        val (pathParams, queryParams, headerParams, bodyParams) = params.splitByType()

        headerParams.forEach { param ->
            // TODO: Consider handling header parameter types. Currently everything is a string.
            if (param.isRequired) {
                builder.addParameter(ParameterSpec.builder(param.name, String::class).build())
            } else {
                builder.addParameter(
                    ParameterSpec.builder(param.name, String::class.asTypeName().copy(nullable = true)).build()
                )
            }
        }

        (pathParams + queryParams).forEach { param ->
            if (param.isRequired) {
                builder.addParameter(param.toParameterSpecBuilder().build())
            } else {
                builder.addParameter(
                    ParameterSpec.builder(param.name, param.type.copy(nullable = true)).build()
                )
            }
        }

        bodyParams.forEach { param ->
            builder.addParameter(param.toParameterSpecBuilder().build())
        }

        builder.addKdoc(buildControllerFunKdoc(operation, params))

        if (operation.toSuccessResponseType(packages.base).isUnit()) {
            builder.addParameter("call", ClassName("io.ktor.server.application", "ApplicationCall"))
        } else {
            builder.addParameter(
                "call",
                ClassName(packages.controllers, TYPED_APPLICATION_CALL_CLASS_NAME)
                    .parameterizedBy(operation.toSuccessResponseType(packages.base))
            )
        }

        return builder.build()
    }

    /**
     * Builds the code that goes into the route function.
     */
    private fun buildRouteCode(operation: Operation, verb: String, path: Map.Entry<String, Path>): CodeBlock {
        val builder = CodeBlock.builder()

        val securityOption = operation.securitySupport(globalSecurity)

        val addAuth = securityOption.allowsAuthenticated && options.contains(ControllerCodeGenOptionType.AUTHENTICATION)
        if (addAuth) {
            val authNames = if (operation.hasSecurityRequirements()) {
                operation.securityRequirements
                    .filter { it.requirements.isNotEmpty() }
                    .map { it.requirements.keys.first() }
            } else {
                // Fall back to the global security requirements
                listOf(this.api.openApi3.securityRequirements.first().requirements.keys.first())
            }.joinToString(", ") { "\"$it\"" }

            builder
                .addStatement(
                    "%M($authNames, optional = %L) {",
                    MemberName("io.ktor.server.auth", "authenticate"),
                    securityOption == SecuritySupport.AUTHENTICATION_OPTIONAL,
                )
                .indent()
        }

        val params = operation.toIncomingParameters(packages.base, path.value.parameters, emptyList())
        val (pathParams, queryParams, headerParams, bodyParams) = params.splitByType()

        val methodName = getMethodName(operation, verb, path)

        builder
            .addStatement(
                "%M(%S) {",
                MemberName("io.ktor.server.routing", verb),
                path.key,
            )
            .indent()

        pathParams.forEach { param ->
            val typeName = param.type.copy(nullable = false) // not nullable because we handle that in the queryParameters.get* below
            val queryMethodName = "getTypedOrFail"
            if (param.requiresKtorDataConversionPlugin()) {
                builder.addStatement(
                    "val ${param.name} = %M.parameters.%M<${typeName}>(\"${param.originalName}\", call.application.%M)",
                    MemberName("io.ktor.server.application", "call"),
                    MemberName(packages.controllers, queryMethodName),
                    MemberName("io.ktor.server.plugins.dataconversion", "conversionService"),
                )
            } else {
                builder.addStatement(
                    "val ${param.name} = %M.parameters.%M<${typeName}>(\"${param.originalName}\")",
                    MemberName("io.ktor.server.application", "call"),
                    MemberName(packages.controllers, queryMethodName),
                )
            }
        }

        headerParams.forEach { param ->
            if (param.isRequired) {
                builder.addStatement(
                    "val ${param.name} = %M.request.headers.getOrFail(\"${param.originalName}\")",
                    MemberName("io.ktor.server.application", "call"),
                )
            } else {
                builder.addStatement(
                    "val ${param.name} = %M.request.headers[\"${param.originalName}\"]",
                    MemberName("io.ktor.server.application", "call"),
                )
            }
        }

        queryParams.forEach { param ->
            val typeName = param.type.copy(nullable = false) // not nullable because we handle that in the queryParameters.get* below
            val queryMethodName = if (param.isRequired) "getTypedOrFail" else "getTyped"

            if (param.requiresKtorDataConversionPlugin()) {
                builder.addStatement(
                    "val ${param.name} = %M.request.queryParameters.%M<$typeName>(\"${param.originalName}\", call.application.%M)",
                    MemberName("io.ktor.server.application", "call"),
                    MemberName(packages.controllers, queryMethodName),
                    MemberName("io.ktor.server.plugins.dataconversion", "conversionService"),
                )
            } else {
                builder.addStatement(
                    "val ${param.name} = %M.request.queryParameters.%M<$typeName>(\"${param.originalName}\")",
                    MemberName("io.ktor.server.application", "call"),
                    MemberName(packages.controllers, queryMethodName),
                )
            }
        }

        bodyParams.forEach { param ->
            builder.addStatement(
                "val ${param.name} = %M.%M<%T>()",
                MemberName("io.ktor.server.application", "call"),
                MemberName("io.ktor.server.request", "receive"),
                param.type,
            )
        }

        val methodParameters =
            listOf(headerParams, pathParams, queryParams, bodyParams).asSequence().flatten().map { it.name }
                .joinToString(", ")

        if (operation.toSuccessResponseType(packages.base).isUnit()) {
            builder.addStatement(
                "controller.%L(%L%M)",
                methodName,
                methodParameters.let { if (it.isNotEmpty()) "$it, " else "" },
                MemberName("io.ktor.server.application", "call"),
            )
        } else {
            builder.addStatement(
                "controller.%L(%L%T(%M))", // construct TypedApplicationCall
                methodName,
                methodParameters.let { if (it.isNotEmpty()) "$it, " else "" },
                ClassName(packages.controllers, TYPED_APPLICATION_CALL_CLASS_NAME),
                MemberName("io.ktor.server.application", "call"),
            )
        }

        builder
            .unindent()
            .addStatement("}")

        if (addAuth) {
            builder
                .unindent()
                .addStatement("}")
        }

        return builder.build()
    }

    private fun buildControllerFunKdoc(operation: Operation, parameters: List<IncomingParameter>): CodeBlock {
        val kDoc = CodeBlock.builder()

        // add summary and description
        val methodDesc = listOf(operation.summary.orEmpty(), operation.description.orEmpty()).filter { it.isNotEmpty() }
        if (methodDesc.isNotEmpty()) {
            methodDesc.forEach { kDoc.add("%L\n", it) }
            kDoc.add("\n")
        }

        // document the response
        val toSuccessResponseType = operation.toSuccessResponseType(packages.base)
        if (toSuccessResponseType.isUnit()) {
            kDoc.add("Route is expected to respond with status ${operation.responses.keys.first()}.\n")
            kDoc.add("Use [%M] to send the response.\n\n", MemberName("io.ktor.server.response", "respond", isExtension = true))
        } else {
            kDoc.add(
                "Route is expected to respond with [%L].\nUse [%M] to send the response.\n\n",
                toSuccessResponseType.toString(),
                MemberName(ClassName(packages.controllers, TYPED_APPLICATION_CALL_CLASS_NAME), "respondTyped")
            )
        }

        // document parameters
        parameters.forEach {
            kDoc.add("@param %L %L\n", it.name.toKCodeName(), it.description?.trimIndent().orEmpty()).build()
        }
        if (toSuccessResponseType.isUnit()) {
            kDoc.add("@param call The Ktor application call\n")
        } else {
            kDoc.add("@param call Decorated ApplicationCall with additional typed respond methods\n",)
        }

        return kDoc.build()
    }

    override fun generateLibrary(): Collection<ControllerLibraryType> = setOf(
        buildTypedApplicationCall(),
    )

    /**
     * Builds a typed ApplicationCall that provides type safe variants of the respond functions.
     *
     * Implemented as a decorator for Ktor's ApplicationCall and can be used as a drop-in replacement.
     */
    private fun buildTypedApplicationCall(): ControllerLibraryType {
        val returnType = TypeVariableName("R", Any::class)

        val messageType = TypeVariableName("T", returnType)
            .copy(reified = true)

        val spec = TypeSpec.classBuilder(TYPED_APPLICATION_CALL_CLASS_NAME)
            .addTypeVariable(returnType)
            .addSuperinterface(ClassName("io.ktor.server.application", "ApplicationCall"), delegate = CodeBlock.of("applicationCall"))
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("applicationCall", ClassName("io.ktor.server.application", "ApplicationCall"))
                    .build()
            )
            .addProperty(
                PropertySpec.builder("applicationCall", ClassName("io.ktor.server.application", "ApplicationCall"))
                    .addModifiers(KModifier.PRIVATE)
                    .initializer("applicationCall")
                    .build()
            )
            .addKdoc(
                CodeBlock.builder()
                    .add("Decorator for Ktor's ApplicationCall that provides type safe variants of the [%M] functions.\n\n",
                        MemberName("io.ktor.server.response", "respond", isExtension = true),
                    )
                    .add(
                        "It can be used as a drop-in replacement for [%M].\n\n",
                        MemberName("io.ktor.server.application", "ApplicationCall"),
                    )
                    .add("@param R The type of the response body\n\n")
                    .build()
            )
            .addFunction(
                FunSpec.builder("respondTyped")
                    .addModifiers(KModifier.INLINE, KModifier.SUSPEND)
                    .addTypeVariable(messageType)
                    .addAnnotation(
                        AnnotationSpec.builder(Suppress::class)
                            .addMember("%S", "unused")
                            .build()
                    )
                    .addParameter("message", messageType)
                    .addCode(
                        CodeBlock.builder()
                            .addStatement(
                                "%M(message)",
                                MemberName("io.ktor.server.response", "respond", isExtension = true),
                            )
                            .build()
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("respondTyped")
                    .addModifiers(KModifier.INLINE, KModifier.SUSPEND)
                    .addTypeVariable(messageType)
                    .addAnnotation(
                        AnnotationSpec.builder(Suppress::class)
                            .addMember("%S", "unused")
                            .build()
                    )
                    .addParameter("status", ClassName("io.ktor.http", "HttpStatusCode"))
                    .addParameter("message", messageType)
                    .addCode(
                        CodeBlock.builder()
                            .addStatement(
                                "%M(status, message)",
                                MemberName("io.ktor.server.response", "respond", isExtension = true),
                            )
                            .build()
                    )
                    .build()
            )
            .build()

        return ControllerLibraryType(spec, packages.base)
    }

    /**
     * Function for getting a typed query parameter
     *
     * Similar to Ktor's getOrFail but will not fail on missing parameter
     */
    private val getTypedFun = run {
        val returnType = TypeVariableName("R", Any::class)
            .copy(nullable = true, reified = true)

        val conversionServiceParameter = ParameterSpec.builder("conversionService", ClassName("io.ktor.util.converters", "ConversionService"))
            .defaultValue("%T", ClassName("io.ktor.util.converters", "DefaultConversionService"))
            .build()

        FunSpec.builder("getTyped")
            .addModifiers(KModifier.INLINE, KModifier.PRIVATE)
            .receiver(ClassName("io.ktor.http", "Parameters"))
            .addParameter("name", String::class)
            .addParameter(conversionServiceParameter)
            .addTypeVariable(returnType)
            .returns(returnType)
            .addCode("""
                val values = getAll(name) ?: return null
                val typeInfo = %M<R>()
                return try {
                    @Suppress("UNCHECKED_CAST")
                    conversionService.fromValues(values, typeInfo) as R
                } catch (cause: Exception) {
                    throw %M(name, typeInfo.type.simpleName ?: typeInfo.type.toString(), cause)
                }
            """.trimIndent(),
                MemberName("io.ktor.util.reflect", "typeInfo"),
                MemberName("io.ktor.server.plugins", "ParameterConversionException")
            )
            .addKdoc("""
                Gets parameter value associated with this name or null if the name is not present.
                Converting to type R using ConversionService.
                
                Throws:
                  ParameterConversionException - when conversion from String to R fails
            """.trimIndent()
            )
            .build()
    }

    private val getTypedOrFailFun = run {
        val returnType = TypeVariableName("R", Any::class)
            .copy(nullable = false, reified = true)

        val conversionServiceParameter = ParameterSpec.builder("conversionService", ClassName("io.ktor.util.converters", "ConversionService"))
            .defaultValue("%T", ClassName("io.ktor.util.converters", "DefaultConversionService"))
            .build()

        FunSpec.builder("getTypedOrFail")
            .addModifiers(KModifier.INLINE, KModifier.PRIVATE)
            .receiver(ClassName("io.ktor.http", "Parameters"))
            .addParameter("name", String::class)
            .addParameter(conversionServiceParameter)
            .addTypeVariable(returnType)
            .returns(returnType)
            .addCode("""
                val values = getAll(name) ?: throw %M(name)
                val typeInfo = %M<R>()
                return try {
                    @Suppress("UNCHECKED_CAST")
                    conversionService.fromValues(values, typeInfo) as R
                } catch (cause: Exception) {
                    throw %M(name, typeInfo.type.simpleName ?: typeInfo.type.toString(), cause)
                }
            """.trimIndent(),
                MemberName("io.ktor.server.plugins", "MissingRequestParameterException"),
                MemberName("io.ktor.util.reflect", "typeInfo"),
                MemberName("io.ktor.server.plugins", "ParameterConversionException")
            )
            .addKdoc("""
                Gets parameter value associated with this name or throws if the name is not present.
                Converting to type R using ConversionService.
                
                Throws:
                  MissingRequestParameterException - when parameter is missing
                  ParameterConversionException - when conversion from String to R fails
            """.trimIndent()
            )
            .build()
    }

    /**
     * Function for getting typed header parameter
     */
    private val getOrFailFun =
        FunSpec.builder("getOrFail")
            .addModifiers(KModifier.PRIVATE)
            .receiver(ClassName("io.ktor.http", "Headers"))
            .returns(String::class)
            .addParameter("name", String::class)
            .addCode("""
                return this[name] ?: throw %M("Header " + name + " is required")
            """.trimIndent(), MemberName("io.ktor.server.plugins", "BadRequestException"))
            .addKdoc("""
                Gets first value from the list of values associated with a name.
                
                Throws:
                  BadRequestException - when the name is not present
            """.trimIndent()
            )
            .build()

    private fun getMethodName(
        operation: Operation, verb: String, path: Map.Entry<String, Path>
    ) = ControllerGeneratorUtils.methodName(
        operation, verb, path.value.pathString.isSingleResource()
    )

    private val globalSecurity = this.api.openApi3.securityRequirements.securitySupport()

    data class KtorControllers(
        val controllers: Set<ControllerType>,
    ) : KotlinTypes(controllers) {
        override val files: Collection<FileSpec> = super.files.map { fileSpec ->
            fileSpec.toBuilder()
                .addFileDisclaimer()
                .build()
        }
    }
}

private fun List<IncomingParameter>.splitByType(): IncomingParametersByType {
    val requestParams = this.filterIsInstance<RequestParameter>()

    return IncomingParametersByType(
        pathParams = requestParams.filter { it.parameterLocation is PathParam },
        queryParams = requestParams.filter { it.parameterLocation is QueryParam },
        headerParams = requestParams.filter { it.parameterLocation is HeaderParam },
        bodyParams = this.filterIsInstance<BodyParameter>()
    )
}

private data class IncomingParametersByType(
    val pathParams: List<RequestParameter>,
    val queryParams: List<RequestParameter>,
    val headerParams: List<RequestParameter>,
    val bodyParams: List<BodyParameter>,
)

private fun TypeName.isUnit(): Boolean = this == Unit::class.asTypeName()

private fun RequestParameter.requiresKtorDataConversionPlugin(): Boolean {
    return when (val type = this.typeInfo) {
        is KotlinTypeInfo.Array -> {
            !isPrimitiveType(type.parameterizedType.modelKClass)
        }

        else -> {
            !isPrimitiveType(this.typeInfo.modelKClass)
        }
    }
}

private fun isPrimitiveType(klass: KClass<*>): Boolean {
    return when (klass) {
        Int::class,
        Float::class,
        Double::class,
        Long::class,
        Short::class,
        Char::class,
        Boolean::class,
        String::class -> true
        else -> false
    }
}
