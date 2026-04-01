package views.elements

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
import com.cjbooms.fabrikt.cli.CodeGenTypeOverride
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.ControllerCodeGenOptionType
import com.cjbooms.fabrikt.cli.ControllerCodeGenTargetType
import com.cjbooms.fabrikt.cli.ExternalReferencesResolutionMode
import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType
import com.cjbooms.fabrikt.cli.OutputOptionType
import com.cjbooms.fabrikt.cli.SerializationLibrary
import com.cjbooms.fabrikt.cli.ValidationLibrary
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.hidden
import kotlinx.html.id
import kotlinx.html.script
import kotlinx.html.style
import kotlinx.html.submitInput
import kotlinx.html.textArea
import kotlinx.html.unsafe
import lib.GenerationSettings

fun FlowContent.specForm(settings: GenerationSettings) = div {
    // editor (used by Ace)
    div {
        id = "editor" + ""
        style = "height: 400px; width: 100%;"
    }

    form(action = "/generate", method = FormMethod.post) {
        // backing field for editor
        textArea {
            id = "spec"
            name = "spec"
            hidden = true
            +settings.inputSpec
        }

        // submit button
        submitInput(classes = "mt2 rounded border-none py2 max-width-3 btn-generate") {
            value = "Generate!"
            // htmx setup
            attributes["hx-post"] = "/generate"
            attributes["hx-target"] = "#codeview"
        }

        // configuration options
        div("h3 mt3 mb1") { +"What to generate" }
        enumCheckboxes("genTypes", CodeGenerationType.values(), settings.genTypes.map { it.name }.toSet())

        div("h3 mt3 mb1") { +"Model Options" }
        enumSelectBox("serializationLibrary", SerializationLibrary.values(), settings.serializationLibrary.name)
        enumCheckboxes("modelOptions", ModelCodeGenOptionType.values(), settings.modelOptions.map { it.name }.toSet())
        inputBox("modelSuffix", "Dto", settings.modelSuffix)
        enumCheckboxes("typeOverrides", CodeGenTypeOverride.values(), settings.typeOverrides.map { it.name }.toSet())

        div("h3 mt3 mb1") { +"Client Options" }
        enumSelectBox("clientTarget", ClientCodeGenTargetType.values(), settings.clientTarget.name)
        enumCheckboxes("clientOptions", ClientCodeGenOptionType.values(), settings.clientOptions.map { it.name }.toSet())

        div("h3 mt3 mb1") { +"Server Options" }
        enumSelectBox("controllerTarget", ControllerCodeGenTargetType.values(), settings.controllerTarget.name)
        enumCheckboxes("controllerOptions", ControllerCodeGenOptionType.values(), settings.controllerOptions.map { it.name }.toSet())

        div("h3 mt3 mb1") { +"Validation Options" }
        enumSelectBox("validationLibrary", ValidationLibrary.values(), settings.validationLibrary.name)

        div("h3 mt3 mb1") { +"External References" }
        enumSelectBox("externalRefResolutionMode", ExternalReferencesResolutionMode.values(), settings.externalRefResolutionMode.name)

        div("h3 mt3 mb1") { +"Output Options" }
            enumCheckboxes("outputOptions", OutputOptionType.values(), settings.outputOptions.map { it.name }.toSet())
        }

    // enable Ace editor and connect with backing field
    script {
        unsafe {
            +"""
                // Initialize Ace editor
                var editor = ace.edit("editor");
                var textarea = document.getElementById("spec");
                editor.getSession().setValue(textarea.value);
                editor.getSession().setMode("ace/mode/yaml");
                editor.getSession().setTabSize(2);
                editor.getSession().setUseSoftTabs(true);
                
                // keep the text area in sync with the editor content
                editor.getSession().on('change', function(){
                    textarea.value = editor.getSession().getValue();
                });
            """.trimIndent()
        }
    }
}

fun formatBackticks(input: String): String {
    val regex = Regex("`([^`]+)`")
    return input.replace(regex) { matchResult ->
        val codeText = matchResult.groupValues[1]
        codeText
    }
}
