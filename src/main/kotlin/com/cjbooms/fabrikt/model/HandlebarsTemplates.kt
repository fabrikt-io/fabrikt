package com.cjbooms.fabrikt.model

import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Helper
import com.github.jknack.handlebars.Options
import com.github.jknack.handlebars.Template
import com.github.jknack.handlebars.helper.ConditionalHelpers
import com.github.jknack.handlebars.helper.StringHelpers
import java.nio.file.Path

object HandlebarsTemplates {

    private val handlebars = Handlebars()
        .registerHelper("lower", StringHelpers.lower)
        .registerHelper("capitalizeFirst", StringHelpers.capitalizeFirst)
        .registerHelper("eq", ConditionalHelpers.eq)
        .registerHelper("neq", ConditionalHelpers.neq)
        .registerHelper("and", ConditionalHelpers.and)
        .registerHelper(
            FirstOccurrenceChecker.NAME,
            FirstOccurrenceChecker.INSTANCE
        )

    // Client templates
    val clientApiModels = handlebars.compile("/templates/client-code/api-models.kt")!!
    val clientOAuth = handlebars.compile("/templates/client-code/oauth.kt")!!
    val clientHttpUtils = handlebars.compile("/templates/client-code/http-util.kt")!!
    val clientHttpResilience4jUtils = handlebars.compile("/templates/client-code/http-resilience4j-util.kt")!!
    val ktorClientApiModels = handlebars.compile("/templates/client-code/ktor-api-models.kt")!!
    val ktorClientApiConfiguration = handlebars.compile("/templates/client-code/ktor-api-configuration.kt")!!

    fun applyTemplate(
        template: Template,
        input: Any,
        path: Path,
        fileName: String,
        postProcessor: (String) -> String = { it }
    ): SimpleFile {
        val fileContents = postProcessor(template.apply(input))
        FirstOccurrenceChecker.STATE_BAG.clear()
        return SimpleFile(path.resolve(fileName), fileContents)
    }
}

private class FirstOccurrenceChecker : Helper<String> {

    companion object {
        const val NAME: String = "firstOccurrence"
        val INSTANCE: Helper<String> = FirstOccurrenceChecker()
        val STATE_BAG: MutableSet<String> = mutableSetOf()
    }

    override fun apply(context: String, options: Options): Any {
        return if (!STATE_BAG.contains(context)) {
            STATE_BAG.add(context)
            options.fn()
        } else options.inverse()
    }
}
