package lib

import com.beust.jcommander.Parameter
import com.cjbooms.fabrikt.cli.CodeGenArgs
import com.cjbooms.fabrikt.cli.CodeGenerationType
import kotlinx.html.div
import kotlinx.html.stream.createHTML
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import views.elements.specForm

/**
 * Every CLI option that influences the generated code must be reachable from the playground:
 * it needs a field on [GenerationSettings] and a form control in the spec form.
 *
 * Options that only make sense for a local CLI run (file paths, remote fetching, help) are listed
 * explicitly below with the reason they are exempt.
 */
class CliArgsReachableFromPlaygroundTest {

    private val cliOnlyArgs = mapOf(
        "--help" to "prints usage",
        "--output-directory" to "the playground renders in the browser instead of writing files",
        "--base-package" to "the playground generates under a fixed package",
        "--api-file" to "the spec is provided through the editor",
        "--api-fragment" to "the playground has a single spec editor",
        "--auth" to "the playground never fetches remote specs",
        "--src-path" to "the playground renders in the browser instead of writing files",
        "--resources-path" to "the playground renders in the browser instead of writing files",
        "--json-schema-file" to "the playground has no file input; only --api-file's editor",
        "--json-schema-root-name" to "only meaningful with --json-schema-file; playground has no file input",
    )

    /** CLI field names that are deliberately named differently in the playground. */
    private val renamedFields = mapOf(
        "targets" to "genTypes",
    )

    @Test
    fun `every generation-affecting CLI arg has a form control in the playground`() {
        val expectedFields = CodeGenArgs::class.java.declaredFields
            .mapNotNull { field -> field.getAnnotation(Parameter::class.java)?.let { field.name to it.names.first() } }
            .filterNot { (_, cliName) -> cliName in cliOnlyArgs }
            .map { (fieldName, cliName) -> cliName to (renamedFields[fieldName] ?: fieldName) }

        val settingsFields = GenerationSettings::class.java.declaredFields.map { it.name }.toSet()
        val formHtml = createHTML().div {
            specForm(GenerationSettings(genTypes = setOf(CodeGenerationType.HTTP_MODELS), modelOptions = emptySet(), inputSpec = ""))
        }

        val missing = expectedFields.mapNotNull { (cliName, fieldName) ->
            when {
                fieldName !in settingsFields -> "$cliName: no '$fieldName' field on GenerationSettings"
                !formHtml.contains("name=\"$fieldName\"") -> "$cliName: no form control named '$fieldName' in specForm"
                else -> null
            }
        }

        assertTrue(missing.isEmpty(), "CLI args not reachable from the playground:\n" + missing.joinToString("\n"))
    }
}
