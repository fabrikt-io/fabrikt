package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.model.KotlinTypeInfo
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName

interface ValidationAnnotations {
    val nonNullAnnotation: AnnotationSpec?

    fun fieldValid(): AnnotationSpec?

    fun parameterValid(): AnnotationSpec?

    fun min(value: Long): AnnotationSpec?

    fun max(value: Long): AnnotationSpec?

    fun pattern(pattern: String): AnnotationSpec?

    fun regexPattern(pattern: String): AnnotationSpec?

    fun lengthRestriction(
        min: Int?,
        max: Int?,
    ): AnnotationSpec?

    fun minRestriction(
        min: Number,
        exclusive: Boolean,
    ): AnnotationSpec?

    fun maxRestriction(
        max: Number,
        exclusive: Boolean,
    ): AnnotationSpec?

    fun size(
        min: Int?,
        max: Int?,
    ): AnnotationSpec?

    fun containerElementAnnotations(elementInfo: KotlinTypeInfo): List<AnnotationSpec>
}

abstract class PackageValidationAnnotations(
    packageName: String,
) : ValidationAnnotations {
    private val minClass = ClassName("$packageName.constraints", "Min")
    private val maxClass = ClassName("$packageName.constraints", "Max")
    private val validClass = ClassName(packageName, "Valid")
    private val notNullClass = ClassName("$packageName.constraints", "NotNull")
    private val patternClass = ClassName("$packageName.constraints", "Pattern")
    private val sizeClass = ClassName("$packageName.constraints", "Size")
    private val decimalMinClass = ClassName("$packageName.constraints", "DecimalMin")
    private val decimalMaxClass = ClassName("$packageName.constraints", "DecimalMax")

    override val nonNullAnnotation =
        AnnotationSpec
            .builder(notNullClass)
            .useSiteTarget(AnnotationSpec.UseSiteTarget.GET)
            .build()

    override fun fieldValid() =
        AnnotationSpec
            .builder(validClass)
            .useSiteTarget(AnnotationSpec.UseSiteTarget.GET)
            .build()

    override fun parameterValid() =
        AnnotationSpec
            .builder(validClass)
            .build()

    override fun min(value: Long) =
        AnnotationSpec
            .builder(minClass)
            .addMember("%L", value)
            .build()

    override fun max(value: Long) =
        AnnotationSpec
            .builder(maxClass)
            .addMember("%L", value)
            .build()

    override fun size(
        min: Int?,
        max: Int?,
    ): AnnotationSpec {
        val specBuilder = AnnotationSpec.builder(sizeClass)

        min?.let { specBuilder.addMember("min = %L", it) }
        max?.let { specBuilder.addMember("max = %L", it) }

        return specBuilder.build()
    }

    override fun pattern(pattern: String) =
        AnnotationSpec
            .builder(patternClass)
            .addMember("regexp = %S", matchAnywhere(pattern))
            .build()

    override fun regexPattern(pattern: String) =
        AnnotationSpec
            .builder(patternClass)
            .addMember("regexp = %S", matchAnywhere(pattern))
            .useSiteTarget(AnnotationSpec.UseSiteTarget.GET)
            .build()

    override fun lengthRestriction(
        min: Int?,
        max: Int?,
    ): AnnotationSpec {
        val specBuilder =
            AnnotationSpec.builder(sizeClass).useSiteTarget(AnnotationSpec.UseSiteTarget.GET)

        min?.let { specBuilder.addMember("min = %L", it) }
        max?.let { specBuilder.addMember("max = %L", it) }

        return specBuilder.build()
    }

    override fun minRestriction(
        min: Number,
        exclusive: Boolean,
    ) = AnnotationSpec
        .builder(decimalMinClass)
        .addMember("value = %S", min.toString())
        .addMember("inclusive = %L", !exclusive)
        .useSiteTarget(AnnotationSpec.UseSiteTarget.GET)
        .build()

    override fun maxRestriction(
        max: Number,
        exclusive: Boolean,
    ) = AnnotationSpec
        .builder(decimalMaxClass)
        .addMember("value = %S", max.toString())
        .addMember("inclusive = %L", !exclusive)
        .useSiteTarget(AnnotationSpec.UseSiteTarget.GET)
        .build()

    override fun containerElementAnnotations(elementInfo: KotlinTypeInfo): List<AnnotationSpec> =
        if (elementInfo.isComplexType && !elementInfo.isContainer) {
            listOf(AnnotationSpec.builder(validClass).build())
        } else {
            emptyList()
        }
}

/**
 * An OpenAPI `pattern` is not implicitly anchored: a value is valid if the expression matches anywhere in it.
 * `@Pattern` is checked with `Matcher.matches()`, which needs the whole value to match, so allow any text around it.
 * `[\s\S]` keeps the meaning of `.` inside the pattern, and explicit `^`/`$` anchors still apply to the whole value.
 * An ECMA-262 `$` is the end of input, Java's `\z`; Java's `$` also matches before a final line terminator.
 * Escapes and character classes are copied unchanged, so an escaped or bracketed dollar sign stays literal.
 */
private fun matchAnywhere(pattern: String): String {
    val strict = escapeClassOrEndAnchor.replace(pattern) { if (it.value == "$") "\\z" else it.value }
    return "[\\s\\S]*(?:$strict)[\\s\\S]*"
}

private val escapeClassOrEndAnchor = Regex("""\\.|\[(?:\\.|[^\\\]])*]|\$""")

object JavaxValidationAnnotations : PackageValidationAnnotations("javax.validation")

object JakartaAnnotations : PackageValidationAnnotations("jakarta.validation")

object NoValidationAnnotations : ValidationAnnotations {
    override val nonNullAnnotation: AnnotationSpec? = null

    override fun fieldValid(): AnnotationSpec? = null

    override fun parameterValid(): AnnotationSpec? = null

    override fun min(value: Long): AnnotationSpec? = null

    override fun max(value: Long): AnnotationSpec? = null

    override fun size(
        min: Int?,
        max: Int?,
    ): AnnotationSpec? = null

    override fun pattern(pattern: String): AnnotationSpec? = null

    override fun regexPattern(pattern: String): AnnotationSpec? = null

    override fun lengthRestriction(
        min: Int?,
        max: Int?,
    ): AnnotationSpec? = null

    override fun minRestriction(
        min: Number,
        exclusive: Boolean,
    ): AnnotationSpec? = null

    override fun maxRestriction(
        max: Number,
        exclusive: Boolean,
    ): AnnotationSpec? = null

    override fun containerElementAnnotations(elementInfo: KotlinTypeInfo): List<AnnotationSpec> = emptyList()
}
