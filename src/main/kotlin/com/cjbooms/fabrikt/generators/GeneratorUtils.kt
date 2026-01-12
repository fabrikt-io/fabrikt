package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.generators.model.ModelGenerator.Companion.toModelType
import com.cjbooms.fabrikt.model.BodyParameter
import com.cjbooms.fabrikt.model.HeaderParam
import com.cjbooms.fabrikt.model.IncomingParameter
import com.cjbooms.fabrikt.model.KotlinTypeInfo
import com.cjbooms.fabrikt.model.PathParam
import com.cjbooms.fabrikt.model.QueryParam
import com.cjbooms.fabrikt.model.RequestParameter
import com.cjbooms.fabrikt.util.KaizenParserExtensions.safeName
import com.cjbooms.fabrikt.util.NormalisedString.camelCase
import com.cjbooms.fabrikt.util.NormalisedString.toKotlinParameterName
import com.cjbooms.fabrikt.util.capitalized
import com.cjbooms.fabrikt.util.decapitalized
import com.reprezen.kaizen.oasparser.model3.MediaType
import com.reprezen.kaizen.oasparser.model3.Operation
import com.reprezen.kaizen.oasparser.model3.Parameter
import com.reprezen.kaizen.oasparser.model3.RequestBody
import com.reprezen.kaizen.oasparser.model3.Response
import com.reprezen.kaizen.oasparser.model3.Schema
import com.cjbooms.fabrikt.model.MultipartParameter
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import java.util.function.Predicate

object GeneratorUtils {
    /**
     * It resolves the API operation body request to its body type. If multiple content medias are found, then it will
     * resolve to the schema reference of the first media type, otherwise it assumes no request body defined for
     * the given operation.
     */
    fun RequestBody.toBodyParameterSpec(basePackage: String): List<ParameterSpec> =
        this.toBodyRequestSchema().map {
            val modelType = toModelType(
                basePackage = basePackage,
                typeInfo = KotlinTypeInfo.from(it),
                isNullable = !this.isRequired
            )
            ParameterSpec.builder(
                it.toVarName(),
                modelType
            ).build()
        }

    /**
     * It resolves the given either `query` of `form` parameter to its corresponding function param specification.
     */
    fun Parameter.toParameterSpec(basePackage: String): ParameterSpec =
        ParameterSpec.builder(
            this.name.toKCodeName(),
            toModelType(
                basePackage = basePackage,
                typeInfo = KotlinTypeInfo.from(this.schema),
                isNullable = !this.isRequired && this.schema.default == null
            )
        ).build()

    /**
     * It converts any string to a variable or function name by removing all non-letter-or-digit characters and transforms
     * the in-between resulting words into `camel case`.
     * e.g. `GET /my-resource/path/{param}` -> getMyResourcePathParam
     */
    fun String.toKCodeName(): String {
        val delimiters = this.partition(Char::isLetterOrDigit).second.toCharArray().map(Char::toString).toTypedArray()
        return this.splitToSequence(*delimiters)
            .mapNotNull(String::capitalized)
            .joinToString("")
            .decapitalized()
    }

    /**
     * It resolves the schema for the given API operation. If multiple content medias are found, then it will
     * resolve to the schema reference of the first media type.
     */
    fun RequestBody.toBodyRequestSchema(): List<Schema> =
        listOfNotNull(this.getPrimaryContentMediaType()?.value?.schema)

    fun mergeParameters(path: List<Parameter>, operation: List<Parameter>): List<Parameter> =
        path.filter { pp -> !operation.any { op -> pp.name == op.name && pp.`in` == op.`in` } } + operation

    fun Operation.toKdoc(parameters: List<IncomingParameter>): CodeBlock {
        val kdoc = CodeBlock.builder().add("${this.summary.orEmpty()}\n${this.description.orEmpty()}\n")

        parameters.forEach {
            kdoc.add("@param %L %L\n", it.name.toKCodeName(), it.description.orEmpty())
        }

        return kdoc.build()
    }

    fun Schema.toKDoc(): CodeBlock? {
        return this.description?.takeIf(String::isNotEmpty)?.let { description ->
            CodeBlock.builder().add("%L", "$description\n")
        }?.build()
    }

    fun TypeSpec.Builder.primaryPropertiesConstructor(vararg properties: PropertySpec): TypeSpec.Builder {
        val propertySpecs = properties.map { it.toBuilder().initializer(it.name).build() }
        val parameters = propertySpecs.map { ParameterSpec.builder(it.name, it.type).build() }
        val constructor = FunSpec.constructorBuilder().addParameters(parameters).build()
        return this.primaryConstructor(constructor).addProperties(propertySpecs)
    }

    fun <T> FunSpec.Builder.addOptionalParameter(
        parameterSpec: ParameterSpec,
        input: T,
        predicate: Predicate<T>
    ) = apply {
        if (predicate.test(input))
            this.addParameter(parameterSpec)
    }

    fun functionName(op: Operation, resource: String, verb: String) = op.operationId?.camelCase() ?: "$verb $resource".toKCodeName()

    fun Schema.toVarName() = this.name?.toKCodeName() ?: this.toClassName().simpleName.toKCodeName()

    private fun Schema.toClassName() = KotlinTypeInfo.from(this).modelKClass.asTypeName()

    fun String.toClassName(basePackage: String) = ClassName(packageName = basePackage, this)

    fun RequestBody.getPrimaryContentMediaType(): Map.Entry<String, MediaType>? =
        this.contentMediaTypes.entries.firstOrNull()

    fun Response.getPrimaryContentMediaType(): Map.Entry<String, MediaType>? =
        this.contentMediaTypes.entries.firstOrNull()

    private const val MULTIPART_FORM_DATA = "multipart/form-data"

    fun RequestBody.isMultipartFormData(): Boolean =
        contentMediaTypes.keys.any { it.equals(MULTIPART_FORM_DATA, ignoreCase = true) }

    fun RequestBody.getMultipartFormDataMediaType(): MediaType? =
        contentMediaTypes.entries.firstOrNull { it.key.equals(MULTIPART_FORM_DATA, ignoreCase = true) }?.value

    fun Operation.hasMultipartRequestBody(): Boolean =
        requestBody?.isMultipartFormData() == true

    fun Schema.isBinaryType(): Boolean =
        type == "string" && format == "binary"

    fun Response.hasMultipleContentMediaTypes(): Boolean = this.contentMediaTypes.entries.size > 1

    fun Operation.firstResponse(): Response? = this.getBodyResponses().firstOrNull()

    fun Operation.getPrimaryContentMediaType(): Map.Entry<String, MediaType>? {
        val responses = getBodySuccessResponses().ifEmpty { getBodyResponses() }
        return responses.map { response -> response.getPrimaryContentMediaType() }.firstOrNull()
    }

    fun Operation.getPrimaryContentMediaTypeKey(): String? = this.firstResponse()?.getPrimaryContentMediaType()?.key

    fun Operation.hasMultipleContentMediaTypes(): Boolean? = this.firstResponse()?.hasMultipleContentMediaTypes()

    fun Operation.hasAnySuccessResponseSchemas(): Boolean = getBodySuccessResponses().isNotEmpty()

    fun Operation.hasMultipleSuccessResponseSchemas(): Boolean =
            getBodySuccessResponses().flatMap { it.contentMediaTypes.values }.map { it.schema.name }.distinct().size > 1

    fun Operation.getPathParams(): List<Parameter> = this.filterParams("path")

    fun Operation.getQueryParams(): List<Parameter> = this.filterParams("query")

    fun Operation.getHeaderParams(): List<Parameter> = this.filterParams("header")

    private fun Operation.getBodyResponses(): List<Response> =
        this.responses.filter { it.key != "default" }.values.filter(Response::hasContentMediaTypes)

    private fun Operation.getBodySuccessResponses(): List<Response> =
        getSuccessResponses().values.filter(Response::hasContentMediaTypes)

    private fun Operation.getSuccessResponses(): Map<String, Response> =
        this.responses.filter { it.key.toIntOrNull()?.let { status -> status in 200..399 } ?: false }

    private fun Operation.filterParams(paramType: String): List<Parameter> = this.parameters.filter { it.`in` == paramType }

    /**
     * Returns a list of IncomingParameters, ordering logic should be
     * encapsulated here to ensure the order of parameters align between
     * services and controllers
     */
    fun Operation.toIncomingParameters(
        basePackage: String,
        pathParameters: List<Parameter>,
        extraParameters: List<IncomingParameter>,
    ): List<IncomingParameter> {

        val bodies = if (requestBody.isMultipartFormData()) {
            toMultipartParameters(basePackage)
        } else {
            requestBody.contentMediaTypes.values
                .map {
                    BodyParameter(
                        it.schema.safeName().toKotlinParameterName().ifEmpty { it.schema.toVarName() },
                        requestBody.description,
                        toModelType(basePackage, KotlinTypeInfo.from(it.schema), applySerializationAnnotations = false),
                        it.schema
                    )
                }
                .distinctBy { it.schema.safeName().toKotlinParameterName().ifEmpty { it.schema.toVarName() } }
        }

        val parameters = mergeParameters(pathParameters, parameters)
            .map {
                RequestParameter(
                    it.name,
                    it.description,
                    toModelType(basePackage, KotlinTypeInfo.from(it.schema), isNullable(it), applySerializationAnnotations = false),
                    it
                )
            }
            .sortedBy { it.type.isNullable }

        return detectAndAvoidNameClashes(bodies + parameters + extraParameters)
    }

    private fun Operation.toMultipartParameters(basePackage: String): List<MultipartParameter> {
        val mediaType = requestBody.getMultipartFormDataMediaType() ?: return emptyList()
        val schema = mediaType.schema ?: return emptyList()
        val requiredFields = schema.requiredFields ?: emptyList()

        return schema.properties.map { (propertyName, propertySchema) ->
            val isFile = propertySchema.isBinaryType() ||
                (propertySchema.type == "array" && propertySchema.itemsSchema?.isBinaryType() == true)
            val isRequired = requiredFields.contains(propertyName)
            val typeInfo = KotlinTypeInfo.from(propertySchema)
            val type = toModelType(basePackage, typeInfo, isNullable = !isRequired, applySerializationAnnotations = false)

            MultipartParameter(
                oasName = propertyName,
                description = propertySchema.description,
                type = type,
                schema = propertySchema,
                isFile = isFile,
                isRequired = isRequired,
                contentType = null,
            )
        }
    }

    private fun List<IncomingParameter>.hasNameClashes(): Boolean =
        map { it.name }.toSet().size != size

    private fun detectAndAvoidNameClashes(parameters: List<IncomingParameter>): List<IncomingParameter> {
        if (!parameters.hasNameClashes()) {
            return parameters
        }

        return parameters.map { p ->
            when (p) {
                is BodyParameter -> BodyParameter(
                    "body_${p.oasName}".toKotlinParameterName(),
                    p.description,
                    p.type,
                    p.schema,
                )
                is MultipartParameter -> MultipartParameter(
                    "multipart_${p.oasName}".toKotlinParameterName(),
                    p.description,
                    p.type,
                    p.schema,
                    p.isFile,
                    p.isRequired,
                    p.contentType,
                )
                is RequestParameter -> RequestParameter(
                    "${p.parameterLocation}_${p.oasName}".toKotlinParameterName(),
                    p.description,
                    p.type,
                    p.originalName,
                    p.parameterLocation,
                    p.typeInfo,
                    p.minimum,
                    p.maximum,
                    p.isRequired,
                    p.explode,
                    p.defaultValue,
                )
            }
        }
    }

    data class IncomingParametersByType(
        val pathParams: List<RequestParameter>,
        val queryParams: List<RequestParameter>,
        val headerParams: List<RequestParameter>,
        val bodyParams: List<BodyParameter>,
        val multipartParams: List<MultipartParameter> = emptyList(),
    )

    fun List<IncomingParameter>.splitByType(): IncomingParametersByType {
        val requestParams = this.filterIsInstance<RequestParameter>()

        return IncomingParametersByType(
            pathParams = requestParams.filter { it.parameterLocation is PathParam },
            queryParams = requestParams.filter { it.parameterLocation is QueryParam },
            headerParams = requestParams.filter { it.parameterLocation is HeaderParam },
            bodyParams = this.filterIsInstance<BodyParameter>(),
            multipartParams = this.filterIsInstance<MultipartParameter>(),
        )
    }

    private fun isNullable(parameter: Parameter): Boolean = !parameter.isRequired && parameter.schema.default == null

    /**
     * Converts a TypeSpec to an object by copying over all properties, functions, etc.
     */
    fun TypeSpec.toObjectTypeSpec(): TypeSpec {
        require(name != null) { "Name must be set to convert to object" }

        val objectBuilder = TypeSpec.objectBuilder(name!!)
            .addAnnotations(annotations)
            .addModifiers(modifiers)
            .superclass(superclass)
            .addProperties(propertySpecs)
            .addFunctions(funSpecs)
            .addKdoc(kdoc)

        for ((typeName, _) in superinterfaces) {
            objectBuilder.addSuperinterface(typeName)
        }

        if (initializerBlock.isNotEmpty()) {
            objectBuilder.addInitializerBlock(initializerBlock)
        }

        for (nestedType in typeSpecs) {
            objectBuilder.addType(nestedType)
        }

        return objectBuilder.build()
    }

    fun TypeName.isUnit(): Boolean = this == Unit::class.asTypeName()
}
