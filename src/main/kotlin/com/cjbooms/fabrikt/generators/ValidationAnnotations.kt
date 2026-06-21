package com.cjbooms.fabrikt.generators

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName

interface ValidationAnnotations {
    val nonNullAnnotation: AnnotationSpec?
    fun fieldValid(): AnnotationSpec?
    fun parameterValid(): AnnotationSpec?
    fun min(value: Long): AnnotationSpec?
    fun max(value: Long): AnnotationSpec?
    fun regexPattern(pattern: String): AnnotationSpec?
    fun lengthRestriction(min: Int?, max: Int?): AnnotationSpec?
    fun minRestriction(min: Number, exclusive: Boolean): AnnotationSpec?
    fun maxRestriction(max: Number, exclusive: Boolean): AnnotationSpec?
    fun size(min: Int?, max: Int?): AnnotationSpec?
}

abstract class PackageValidationAnnotations(packageName: String) : ValidationAnnotations {
    private val minClass = ClassName("$packageName.constraints", "Min")
    private val maxClass = ClassName("$packageName.constraints", "Max")
    private val validClass = ClassName(packageName, "Valid")
    private val notNullClass = ClassName("$packageName.constraints", "NotNull")
    private val patternClass = ClassName("$packageName.constraints", "Pattern")
    private val sizeClass = ClassName("$packageName.constraints", "Size")
    private val decimalMinClass = ClassName("$packageName.constraints", "DecimalMin")
    private val decimalMaxClass = ClassName("$packageName.constraints", "DecimalMax")

    override val nonNullAnnotation = AnnotationSpec
        .builder(notNullClass)
        .useSiteTarget(AnnotationSpec.UseSiteTarget.GET)
        .build()

    override fun fieldValid() = AnnotationSpec
        .builder(validClass)
        .useSiteTarget(AnnotationSpec.UseSiteTarget.GET)
        .build()

    override fun parameterValid() = AnnotationSpec
        .builder(validClass)
        .build()

    override fun min(value: Long) = AnnotationSpec
        .builder(minClass)
        .addMember("%L", value)
        .build()

    override fun max(value: Long) = AnnotationSpec
        .builder(maxClass)
        .addMember("%L", value)
        .build()

    override fun size(min: Int?, max: Int?): AnnotationSpec {
        val specBuilder = AnnotationSpec.builder(sizeClass)

        min?.let { specBuilder.addMember("min = %L", it) }
        max?.let { specBuilder.addMember("max = %L", it) }

        return specBuilder.build()
    }

    override fun regexPattern(pattern: String) = AnnotationSpec
        .builder(patternClass)
        .addMember("regexp = %S", pattern)
        .useSiteTarget(AnnotationSpec.UseSiteTarget.GET)
        .build()

    override fun lengthRestriction(min: Int?, max: Int?): AnnotationSpec {
        val specBuilder =
            AnnotationSpec.builder(sizeClass).useSiteTarget(AnnotationSpec.UseSiteTarget.GET)

        min?.let { specBuilder.addMember("min = %L", it) }
        max?.let { specBuilder.addMember("max = %L", it) }

        return specBuilder.build()
    }

    override fun minRestriction(min: Number, exclusive: Boolean) = AnnotationSpec
        .builder(decimalMinClass)
        .addMember("value = %S", min.toString())
        .addMember("inclusive = %L", !exclusive)
        .useSiteTarget(AnnotationSpec.UseSiteTarget.GET)
        .build()

    override fun maxRestriction(max: Number, exclusive: Boolean) = AnnotationSpec
        .builder(decimalMaxClass)
        .addMember("value = %S", max.toString())
        .addMember("inclusive = %L", !exclusive)
        .useSiteTarget(AnnotationSpec.UseSiteTarget.GET)
        .build()
}

object JavaxValidationAnnotations : PackageValidationAnnotations("javax.validation")

object JakartaAnnotations : PackageValidationAnnotations("jakarta.validation")

object NoValidationAnnotations : ValidationAnnotations {
    override val nonNullAnnotation: AnnotationSpec? = null

    override fun fieldValid(): AnnotationSpec? = null

    override fun parameterValid(): AnnotationSpec? = null

    override fun min(value: Long): AnnotationSpec? = null

    override fun max(value: Long): AnnotationSpec? = null

    override fun size(min: Int?, max: Int?): AnnotationSpec? = null

    override fun regexPattern(pattern: String): AnnotationSpec? = null

    override fun lengthRestriction(min: Int?, max: Int?): AnnotationSpec? = null

    override fun minRestriction(min: Number, exclusive: Boolean): AnnotationSpec? = null

    override fun maxRestriction(max: Number, exclusive: Boolean): AnnotationSpec? = null
}
