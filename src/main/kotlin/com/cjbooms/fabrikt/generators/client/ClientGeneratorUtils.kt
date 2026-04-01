package com.cjbooms.fabrikt.generators.client

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.GeneratorUtils
import com.cjbooms.fabrikt.generators.GeneratorUtils.getBodySuccessResponses
import com.cjbooms.fabrikt.generators.GeneratorUtils.getPrimaryContentMediaType
import com.cjbooms.fabrikt.generators.GeneratorUtils.getPrimaryContentMediaTypeKey
import com.cjbooms.fabrikt.generators.GeneratorUtils.hasAnySuccessResponseSchemas
import com.cjbooms.fabrikt.generators.GeneratorUtils.hasMultipleContentMediaTypes
import com.cjbooms.fabrikt.generators.GeneratorUtils.hasMultipleSuccessResponseSchemas
import com.cjbooms.fabrikt.generators.GeneratorUtils.hasOnlyJsonSuccessResponses
import com.cjbooms.fabrikt.generators.GeneratorUtils.toClassName
import com.cjbooms.fabrikt.generators.GeneratorUtils.toIncomingParameters
import com.cjbooms.fabrikt.generators.OasDefault
import com.cjbooms.fabrikt.generators.controller.metadata.SpringImports.RESPONSE_ENTITY
import com.cjbooms.fabrikt.generators.model.ModelGenerator.Companion.toModelType
import com.cjbooms.fabrikt.model.BodyParameter
import com.cjbooms.fabrikt.model.ClientType
import com.cjbooms.fabrikt.model.HeaderParam
import com.cjbooms.fabrikt.model.IncomingParameter
import com.cjbooms.fabrikt.model.KotlinTypeInfo
import com.cjbooms.fabrikt.model.RequestParameter
import com.fasterxml.jackson.databind.JsonNode
import com.reprezen.kaizen.oasparser.model3.Operation
import com.reprezen.kaizen.oasparser.model3.Path
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import kotlin.reflect.KClass

object ClientGeneratorUtils {
    const val ACCEPT_HEADER_NAME = "Accept"
    const val ACCEPT_HEADER_VARIABLE_NAME = "acceptHeader"
    const val CONTENT_TYPE_HEADER_NAME = "Content-Type"
    const val ADDITIONAL_HEADERS_PARAMETER_NAME = "additionalHeaders"
    const val ADDITIONAL_QUERY_PARAMETERS_PARAMETER_NAME = "additionalQueryParameters"

    /**
     * Gives the Kotlin return type for an API call based on the Content-Types specified in the Operation.
     */
    fun Operation.getReturnType(packages: Packages): TypeName =
        when (val returnType = getReturnType()) {
            is KotlinTypeInfo -> toModelType(packages.base, returnType)
            is KClass<*> -> returnType.asTypeName()
            else -> throw IllegalStateException("Unsupported return type: $returnType")
        }

    /**
     * Determines return the return type, with special handling for multiple response schemas.
     * Returns JsonNode for JSON-only responses, Any for mixed content types.
     */
     fun Operation.getReturnType(): Any {
        return if (!hasAnySuccessResponseSchemas()) {
            Unit::class
        } else if (hasMultipleSuccessResponseSchemas()) {
            if (hasOnlyJsonSuccessResponses()) JsonNode::class else Any::class
        } else {
            this.getPrimaryContentMediaType()?.let {
                KotlinTypeInfo.from(it.value.schema)
            } ?: Unit::class
        }
    }

    fun Operation.toClientReturnType(packages: Packages): TypeName {
        return "ApiResponse".toClassName(packages.client).parameterizedBy(getReturnType(packages))
    }

    fun simpleClientName(resourceName: String) = "$resourceName${ClientType.SIMPLE_CLIENT_SUFFIX}"

    fun enhancedClientName(resourceName: String) = "$resourceName${ClientType.ENHANCED_CLIENT_SUFFIX}"

    fun deriveClientParameters(path: Path, operation: Operation, basePackage: String): List<IncomingParameter> {
        fun needsAcceptHeaderParameter(path: Path, operation: Operation): Boolean {
            val hasAcceptParameter = GeneratorUtils.mergeParameters(path.parameters, operation.parameters)
                .any { parameter ->
                    parameter.`in` == "header" && parameter.name.equals(
                        ACCEPT_HEADER_NAME,
                        ignoreCase = true
                    )
                }
            return operation.hasMultipleContentMediaTypes() == true && !hasAcceptParameter
        }

        val extra = if (needsAcceptHeaderParameter(path, operation)) listOf(
            RequestParameter(
                oasName = ACCEPT_HEADER_VARIABLE_NAME,
                description = null,
                type = toModelType(basePackage, KotlinTypeInfo.Text, false),
                originalName = ACCEPT_HEADER_NAME,
                parameterLocation = HeaderParam,
                typeInfo = KotlinTypeInfo.Text,
                minimum = null,
                maximum = null,
                isRequired = true,
                defaultValue = operation.getPrimaryContentMediaTypeKey(),
            )
        ) else emptyList()

        return operation.toIncomingParameters(
            basePackage,
            path.parameters,
            extra,
        )
    }

    fun FunSpec.Builder.addIncomingParameters(
        parameters: List<IncomingParameter>,
        annotateRequestParameterWith: ((parameter: RequestParameter) -> AnnotationSpec?)? = null,
        annotateBodyParameterWith: ((parameter: BodyParameter) -> AnnotationSpec?)? = null,
    ): FunSpec.Builder {
        val specs = parameters.map {
            val builder = it.toParameterSpecBuilder(treatAnyTypeHeadersAsStrings = true)
            if (it is RequestParameter) {
                if (it.defaultValue != null) OasDefault.from(it.typeInfo, it.type, it.defaultValue)
                    ?.let { t -> builder.defaultValue(t.getDefault()) }
                else if (!it.isRequired) builder.defaultValue("null")
                annotateRequestParameterWith?.invoke(it)?.let { annotationSpec ->
                    builder.addAnnotation(annotationSpec)
                }
            }
            if (it is BodyParameter) {
                annotateBodyParameterWith?.invoke(it)?.let { annotationSpec ->
                    builder.addAnnotation(annotationSpec)
                }
            }
            builder.build()
        }
        return this.addParameters(specs)
    }

    /**
     * Add suspend as modified to method definitions on supported clients, ex. CoroutineFeign, Spring HTTP Interface.
     */
    fun FunSpec.Builder.addSuspendModifier(options: Set<ClientCodeGenOptionType>): FunSpec.Builder {
        if (options.contains(ClientCodeGenOptionType.SUSPEND_MODIFIER)) {
            this.addModifiers(KModifier.SUSPEND)
        }
        return this
    }

    /**
     * Adds a ResponseEntity around the returned object so that we can get headers and statuscodes
     */
    fun TypeName.optionallyParameterizeWithResponseEntity(options: Set<ClientCodeGenOptionType>): TypeName {
        if (options.contains(ClientCodeGenOptionType.SPRING_RESPONSE_ENTITY_WRAPPER)) {
            return RESPONSE_ENTITY.parameterizedBy(this)
        }
        return this
    }
}
