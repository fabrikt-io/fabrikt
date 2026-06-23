package com.cjbooms.fabrikt.generators.controller

import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.ValidationAnnotations
import com.cjbooms.fabrikt.model.ControllerType
import com.cjbooms.fabrikt.model.RequestParameter
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.KaizenParserExtensions.basePath
import com.cjbooms.fabrikt.util.toUpperCase
import com.reprezen.kaizen.oasparser.model3.Operation
import com.reprezen.kaizen.oasparser.model3.Path
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec

abstract class AnnotationBasedControllerInterfaceGenerator(
    private val packages: Packages,
    private val api: SourceApi,
    private val validationAnnotations: ValidationAnnotations,
) {
    abstract fun buildFunction(path: Path, op: Operation, verb: String): FunSpec

    abstract fun controllerBuilder(className: String, basePath: String): TypeSpec.Builder

    fun buildController(resourceName: String, paths: Collection<Path>): ControllerType {
        val typeBuilder: TypeSpec.Builder = controllerBuilder(
            className = ControllerGeneratorUtils.controllerName(resourceName),
            basePath = api.openApi3.basePath()
        )

        paths.flatMap { path ->
            path.operations
                .filter { it.key.toUpperCase() != "HEAD" }
                .map { op ->
                    buildFunction(
                        path,
                        op.value,
                        op.key,
                    )
                }
        }.forEach { typeBuilder.addFunction(it) }

        return ControllerType(
            typeBuilder.build(),
            packages.base
        )
    }

    fun ParameterSpec.Builder.addValidationAnnotations(parameter: RequestParameter): ParameterSpec.Builder {
        if (parameter.minimum != null) this.maybeAddAnnotation(validationAnnotations.min(parameter.minimum.toLong()))
        if (parameter.maximum != null) this.maybeAddAnnotation(validationAnnotations.max(parameter.maximum.toLong()))
        if (parameter.minLength != null || parameter.maxLength != null) this.maybeAddAnnotation(
            (validationAnnotations.size(parameter.minLength?.toInt(), parameter.maxLength?.toInt()))
        )
        if (parameter.typeInfo.isComplexType) this.maybeAddAnnotation(validationAnnotations.parameterValid())
        return this
    }

    fun ParameterSpec.Builder.maybeAddAnnotation(annotation: AnnotationSpec?) =
        if (annotation != null) this.addAnnotation(annotation) else this
}
