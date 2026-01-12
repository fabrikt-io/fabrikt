package com.cjbooms.fabrikt.model

import com.cjbooms.fabrikt.model.Destinations.clientPackage
import com.cjbooms.fabrikt.model.Destinations.controllersPackage
import com.cjbooms.fabrikt.model.Destinations.modelsPackage
import com.cjbooms.fabrikt.util.FileUtils.addFileDisclaimer
import com.cjbooms.fabrikt.util.NormalisedString.toKotlinParameterName
import com.reprezen.kaizen.oasparser.model3.Parameter
import com.reprezen.kaizen.oasparser.model3.Schema
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName

sealed class GeneratedType(val spec: TypeSpec, val destinationPackage: String) {
    val className = ClassName(destinationPackage, spec.name!!)
}

abstract class KotlinTypes(types: Collection<GeneratedType>) {
    open val files: Collection<FileSpec> = types.map {
        FileSpec.builder(types.first().destinationPackage, it.className.simpleName)
            .addType(it.spec)
            .build()
    }.toSet()
}

class ModelType(spec: TypeSpec, basePackage: String) : GeneratedType(spec, modelsPackage(basePackage))

class ClientType(
    spec: TypeSpec,
    basePackage: String,
    val imports: Set<Pair<String, String>> = emptySet()
) : GeneratedType(spec, clientPackage(basePackage)) {
    companion object {
        const val SIMPLE_CLIENT_SUFFIX = "Client"
        const val ENHANCED_CLIENT_SUFFIX = "Service"
    }
}

class ControllerType(spec: TypeSpec, basePackage: String) : GeneratedType(spec, controllersPackage(basePackage)) {
    companion object {
        const val SUFFIX = "Controller"
    }
}

class ControllerLibraryType(spec: TypeSpec, basePackage: String) :
    GeneratedType(spec, controllersPackage(basePackage))

data class Models(val models: Collection<ModelType>) : KotlinTypes(models) {
    override val files: Collection<FileSpec> = models.toFileSpec()
}

data class Clients(val clients: Collection<ClientType>) : KotlinTypes(clients) {
    override val files: Collection<FileSpec> = clients.map {
        val builder = FileSpec.builder(it.destinationPackage, it.className.simpleName)
            .addType(it.spec)
            .addFileDisclaimer()
        it.imports.forEach { (pkg, name) -> builder
            .addImport(pkg, name) }
        builder.build()
    }.toSet()
}

fun <T : GeneratedType> Collection<T>.toFileSpec(): Collection<FileSpec> = this
    .map {
        FileSpec.builder(it.destinationPackage, it.className.simpleName)
            .addFileDisclaimer()
            .addType(it.spec).build()
    }

/**
 * The IncomingParameter class is intended to represent a given name and type
 * of an incoming request, be it either a header, url param, path param, or body
 */
sealed class IncomingParameter(val oasName: String, val description: String?, val type: TypeName) {
    val name: String = oasName.toKotlinParameterName()
    open fun toParameterSpecBuilder(treatAnyTypeHeadersAsStrings: Boolean = false): ParameterSpec.Builder =
        ParameterSpec.builder(name, type)
}

class BodyParameter(oasName: String, description: String?, type: TypeName, val schema: Schema) :
    IncomingParameter(oasName, description, type)

class MultipartParameter(
    oasName: String,
    description: String?,
    type: TypeName,
    val schema: Schema,
    val isFile: Boolean,
    val isRequired: Boolean,
    val contentType: String? = null,
) : IncomingParameter(oasName, description, type)

class RequestParameter(
    oasName: String,
    description: String?,
    type: TypeName,
    var originalName: String,
    val parameterLocation: RequestParameterLocation,
    val typeInfo: KotlinTypeInfo,
    val minimum: Number? = null,
    val maximum: Number? = null,
    val isRequired: Boolean = false,
    val explode: Boolean? = null,
    val defaultValue: Any? = null,
) : IncomingParameter(oasName, description, type) {
    constructor(oasName: String, description: String?, type: TypeName, parameter: Parameter) : this(
        oasName = oasName,
        description = description,
        type = type,
        originalName = parameter.name,
        typeInfo = KotlinTypeInfo.from(parameter.schema, oasName),
        minimum = parameter.schema.minimum,
        maximum = parameter.schema.maximum,
        parameterLocation = RequestParameterLocation(parameter.`in`),
        isRequired = parameter.isRequired,
        explode = parameter.explode,
        defaultValue = parameter.schema.default
    )

    override fun toParameterSpecBuilder(treatAnyTypeHeadersAsStrings: Boolean): ParameterSpec.Builder =
        if (treatAnyTypeHeadersAsStrings && parameterLocation == HeaderParam && typeInfo == KotlinTypeInfo.AnyType) {
            ParameterSpec.builder(
                name = name,
                type = if (isRequired) String::class.asTypeName() else String::class.asTypeName().copy(nullable = true)
            )
        } else super.toParameterSpecBuilder(treatAnyTypeHeadersAsStrings)
}
