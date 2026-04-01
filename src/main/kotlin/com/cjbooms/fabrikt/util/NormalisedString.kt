package com.cjbooms.fabrikt.util

import java.util.Locale
import javax.lang.model.SourceVersion

object NormalisedString {

    fun String.pascalCase(): String =
        replaceSpecialCharacters()
            .split("_")
            .joinToString("") { it.capitalized() }

    private fun String.replaceSpecialCharacters() =
        Regex("[^A-Za-z0-9øæåØÆÅ]").replace(this, "_")

    private fun String.camelToSnake() =
        Regex("[a-zøæå][A-ZØÆÅ]")
            .replace(this) { pair ->
                val characters = pair.value.toCharArray()
                "${characters[0]}_${characters[1]}"
            }

    fun String.camelCase(): String {
        val strippedLeading = trimStart('_')
        val leadingUnderscores = "_".repeat(length - strippedLeading.length)
        val strippedTrailing = strippedLeading.trimEnd('_')
        val trailingUnderscores = "_".repeat(strippedLeading.length - strippedTrailing.length)
        return if (strippedTrailing.isEmpty()) leadingUnderscores + trailingUnderscores
        else leadingUnderscores + strippedTrailing.pascalCase().decapitalized() + trailingUnderscores
    }

    fun String.toModelClassName(parentModelName: String = ""): String = parentModelName + this.pascalCase()

    fun String.toMapValueClassName(): String = "${this.pascalCase()}Value"

    fun String.toEnumName(): String =
        replaceSpecialCharacters()
            .camelToSnake()
            .toUpperCase()
            .quoteIfNotValidIdentifier()

    fun String.toKotlinParameterName(): String = this.camelCase()

    fun String.isValidJavaPackage(): Boolean {
        val pieces = split(".")
        return all { if (it.isLetter()) it.isLowerCase() else true } && pieces.all { SourceVersion.isIdentifier(it) }
    }
}
// Replace deprecated Kotlin String functions with concise equivalents
fun String.toUpperCase() = uppercase(Locale.getDefault())
fun String.toLowerCase() = lowercase(Locale.getDefault())
fun String.capitalized() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
fun String.decapitalized() = replaceFirstChar { it.lowercase(Locale.getDefault()) }
fun String.quoteIfNotValidIdentifier() = if (first().isLetter() || first() == '_') this else "`$this`"
