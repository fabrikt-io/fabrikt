package views.elements

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.pre
import kotlinx.html.span

fun FlowContent.cliCommandView(command: String) {
    div(classes = "cli-command-section") {
        div(classes = "cli-command-header") {
            attributes["onclick"] = "toggleCli(this)"
            div(classes = "cli-command-title") {
                span { +"▶" }
                +" CLI"
            }
            div(classes = "cli-copy-btn") {
                attributes["onclick"] = "event.stopPropagation(); copyCli(this)"
                +"Copy"
            }
        }
        pre(classes = "cli-command-code") {
            attributes["style"] = "display: none;"
            +command
        }
    }
}
