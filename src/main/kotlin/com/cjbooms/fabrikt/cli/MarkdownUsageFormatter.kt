package com.cjbooms.fabrikt.cli

import com.beust.jcommander.DefaultUsageFormatter
import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterDescription
import com.beust.jcommander.Parameterized

/**
 * Overrides [DefaultUsageFormatter] to provide tabular usage output that looks good in unix and renders a table in markdown.
 */
class MarkdownUsageFormatter(
    commander: JCommander,
) : DefaultUsageFormatter(commander) {
    companion object {
        const val FIRST_COL_HEADER = "Parameter"
        const val SECOND_COL_HEADER = "Description"
        const val MULTI_VALUE_HEADER = "CHOOSE ANY OF:"
        const val SINGLE_VALUE_HEADER = "CHOOSE ONE OF:"
        const val START = "| "
        const val BORDER = " | "
        const val END = " |"
        const val SEPARATOR = "-"
        const val NEW_LINE = "\n"
    }

    override fun appendAllParametersDetails(
        out: StringBuilder,
        indentCount: Int,
        indent: String?,
        sortedParameters: List<ParameterDescription>,
    ) {
        if (sortedParameters.isEmpty()) return

        val firstColumnWidth = getFirstColumnWidth(sortedParameters)

        out
            .append(START)
            .append(FIRST_COL_HEADER)
            .append(" ".repeat(firstColumnWidth - FIRST_COL_HEADER.length))
            .append(BORDER)
            .append(SECOND_COL_HEADER)
            .append(END)
            .append(NEW_LINE)

        val titleSeparator = SEPARATOR.repeat(firstColumnWidth)
        out
            .append(START)
            .append(titleSeparator)
            .append(" ".repeat(firstColumnWidth - titleSeparator.length))
            .append(BORDER)
            .append(titleSeparator)
            .append(END)
            .append(NEW_LINE)

        sortedParameters
            .filter { !it.isHelp }
            .forEach { param ->

                val parameterKey = param.getParameterKey()
                out
                    .append(START)
                    .append(parameterKey)
                    .append(" ".repeat(firstColumnWidth - parameterKey.length))
                    .append(BORDER)
                    .append(param.description)
                    .append(END)
                    .append(NEW_LINE)

                val options = getEnumValues(param.parameterized)
                if (options.isNotEmpty()) {
                    val valueHeader = if (param.parameterized.isSingleEnum()) SINGLE_VALUE_HEADER else MULTI_VALUE_HEADER
                    out
                        .append(START)
                        .append(" ".repeat(firstColumnWidth))
                        .append(BORDER)
                        .append(valueHeader)
                        .append(END)
                        .append(NEW_LINE)

                    options.forEach { option ->
                        out
                            .append(START)
                            .append(" ".repeat(firstColumnWidth))
                            .append(BORDER)
                            .append("  $option")
                            .append(END)
                            .append(NEW_LINE)
                    }
                }
            }
    }

    private fun ParameterDescription.getParameterKey() = (if (parameter.required()) "* " else "  ") + "`" + names + "`"

    private fun Parameterized.isSingleEnum() = Enum::class.java.isAssignableFrom(type)

    private fun getFirstColumnWidth(sortedParameters: List<ParameterDescription>): Int =
        sortedParameters.fold(0) { currentMaxWidth, pd ->
            val entry = pd.getParameterKey()
            if (entry.length > currentMaxWidth) entry.length else currentMaxWidth
        }

    @Suppress("UNCHECKED_CAST")
    private fun getEnumValues(parameterized: Parameterized): List<Enum<*>> {
        val maybeEnumClass =
            if (parameterized.isSingleEnum()) {
                parameterized.type
            } else {
                parameterized.findFieldGenericType()?.let { genericType ->
                    (genericType as? Class<*>)?.let { genericClass ->
                        if (Enum::class.java.isAssignableFrom(genericClass)) {
                            genericClass
                        } else {
                            null
                        }
                    }
                }
            }
        return if (maybeEnumClass != null) {
            (maybeEnumClass.enumConstants as Array<out Enum<*>>).toList()
        } else {
            emptyList()
        }
    }
}
