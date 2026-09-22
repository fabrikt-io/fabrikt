package com.cjbooms.fabrikt.util

import com.cjbooms.fabrikt.cli.OutputOptionType
import com.cjbooms.fabrikt.generators.MutableSettings
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import io.fabrikt.cli.CodeGen

object GeneratedAnnotations {
    private val generated = ClassName("javax.annotation.processing", "Generated")

    private fun annotation(): AnnotationSpec =
        AnnotationSpec
            .builder(generated)
            .addMember("value = [%S]", CodeGen::class.java.name)
            .addMember("date = %S", MutableSettings.generationMetadata.date.toString())
            .addMember("comments = %S", "Generated with Fabrikt v${MutableSettings.generationMetadata.version.removePrefix("v")}")
            .build()

    fun TypeSpec.addGeneratedAnnotation(): TypeSpec {
        if (OutputOptionType.ADD_GENERATED_ANNOTATION !in MutableSettings.outputOptions) return this
        return toBuilder()
            .apply {
                if ((name != null || KModifier.COMPANION in modifiers) && annotations.none { it.typeName == generated }) {
                    addAnnotation(annotation())
                }
                typeSpecs.clear()
                typeSpecs.addAll(this@addGeneratedAnnotation.typeSpecs.map { it.addGeneratedAnnotation() })
            }.build()
    }

    private fun FunSpec.addGeneratedAnnotation(): FunSpec =
        if (annotations.any { it.typeName == generated }) {
            this
        } else {
            toBuilder()
                .addAnnotation(annotation())
                .build()
        }

    fun FileSpec.addGeneratedAnnotations(): FileSpec {
        if (OutputOptionType.ADD_GENERATED_ANNOTATION !in MutableSettings.outputOptions) return this
        return toBuilder()
            .apply {
                members.clear()
                members.addAll(
                    this@addGeneratedAnnotations.members.map {
                        when (it) {
                            is TypeSpec -> it.addGeneratedAnnotation()
                            is FunSpec -> it.addGeneratedAnnotation()
                            else -> it
                        }
                    },
                )
            }.build()
    }
}
