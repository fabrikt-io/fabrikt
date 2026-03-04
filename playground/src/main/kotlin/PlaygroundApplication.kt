import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType
import com.cjbooms.fabrikt.model.GeneratedFile
import com.cjbooms.fabrikt.model.KotlinSourceSet
import com.cjbooms.fabrikt.model.ResourceFile
import com.cjbooms.fabrikt.model.ResourceSourceSet
import com.cjbooms.fabrikt.model.SimpleFile
import data.sampleOpenApiSpec
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.header
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.script
import kotlinx.html.stream.appendHTML
import kotlinx.html.style
import kotlinx.html.unsafe
import lib.generateCodeSynchronized
import lib.GenerationSettings.Companion.receiveGenerationSettings
import views.elements.cliCommandView
import views.elements.codeView
import views.elements.fileView
import views.elements.fileViewForFile
import views.elements.specForm
import views.layout.columnPanel
import views.layout.mainLayout
import views.respondHtmlFragmentDiv

fun main() {
    embeddedServer(Netty, port = System.getenv("PORT")?.toIntOrNull() ?: 8080) {
        install(CallLogging)

        routing {
            staticResources("/static", "static")

            /**
             * GET endpoint to render the playground
             */
            get("/") {
                val generationSettings = call.queryParameters.receiveGenerationSettings()
                    .copy(inputSpec = sampleOpenApiSpec) // set the sample spec
                    .run {
                        // if no settings in query params we configure default
                        // to ensure something is generated with just the sample spec
                        if (call.queryParameters.isEmpty()) {
                            copy(genTypes = setOf(CodeGenerationType.HTTP_MODELS))
                        } else {
                            this
                        }
                    }

                call.respondHtml {
                    mainLayout(Version.GIT_VERSION) {
                        columnPanel(
                            flexSizes = listOf(1.0, 0.4, 1.5),
                            // first column: spec form
                            {
                                specForm(generationSettings)
                            },
                            // second column: file sidebar
                            {
                                style = "flex: 0.4; padding: 0; overflow-y: hidden; display: flex; flex-direction: column; border-left: 1px solid #e0e0e0; border-right: 1px solid #e0e0e0;"
                                div {
                                    id = "file-sidebar"
                                    style = "flex: 1; overflow-y: auto; min-height: 0; padding: 10px 0;"
                                    div(classes = "file-sidebar-empty") {
                                        +"// Files will appear here"
                                    }
                                }
                                div {
                                    id = "cli-command"
                                    style = "flex-shrink: 0; border-top: 1px solid #e0e0e0;"
                                }
                                div {
                                    style = "flex-shrink: 0; padding: 8px 10px; border-top: 1px solid #e0e0e0; font-size: 12px; color: #999;"
                                    a(href = "https://github.com/fabrikt-io/fabrikt", target = "_blank") {
                                        +"Fabrikt on GitHub"
                                    }
                                    div { +"v${Version.GIT_VERSION}" }
                                }
                            },
                            // third column: code output
                            {
                                codeView { fileView("// Output will appear here") }
                            }
                        )
                    }
                }
            }

            /**
             * POST endpoint to generate code from a spec
             *
             * Renders only the div containing the generated code.
             *
             * Loaded via AJAX with HTMX.
             */
            post("/generate") {
                val generationSettings = call.receiveParameters().receiveGenerationSettings()

                // validate input
                val inputSpec = generationSettings.inputSpec
                if (inputSpec.isBlank()) {
                    return@post call.respondText {
                        buildString { appendHTML().div {
                            fileView("// Error: No spec provided")
                            script { unsafe { +"Prism.highlightAll();" } } // trigger syntax highlighting
                        } }
                    }
                }

                runCatching {
                    generateCodeSynchronized(generationSettings)
                }.onSuccess { generatedFiles ->
                    val pathParams: String = generationSettings.toQueryParams()
                    call.response.header("HX-Replace-Url", "/?$pathParams")

                    val fileNames = generatedFiles.fileNamesWithLabels()

                    call.respondHtmlFragmentDiv {
                        if (generatedFiles.isEmpty()) {
                            fileView("// No files generated. Try adjusting your settings.")
                        } else {
                            generatedFiles.forEach {
                                fileViewForFile(it)
                            }
                        }
                        script { unsafe { +"Prism.highlightAll();" } } // trigger syntax highlighting
                        // OOB swap: update the file sidebar
                        div {
                            id = "file-sidebar"
                            attributes["hx-swap-oob"] = "innerHTML"
                            if (fileNames.isNotEmpty()) fileList(fileNames)
                        }
                        // OOB swap: update the CLI command
                        div {
                            id = "cli-command"
                            attributes["hx-swap-oob"] = "innerHTML"
                            cliCommandView(generationSettings.toCliCommand())
                        }
                    }
                }.onFailure { error ->
                    call.respondHtmlFragmentDiv {
                        fileView("// Error: ${error.message}")
                        script { unsafe { +"Prism.highlightAll();" } } // trigger syntax highlighting
                        // OOB swap: clear the file sidebar on error
                        div {
                            id = "file-sidebar"
                            attributes["hx-swap-oob"] = "innerHTML"
                        }
                    }
                }
            }
        }
    }.start(wait = true)
}

private fun List<GeneratedFile>.fileNamesWithLabels(): List<Pair<String, String>> = this.flatMap { generatedFile ->
    when (generatedFile) {
        is KotlinSourceSet -> {
            val label = generatedFile.files.firstOrNull()?.packageName?.let { pkg ->
                when {
                    "models" in pkg -> "Model"
                    "client" in pkg -> "Client"
                    "controllers" in pkg -> "Controller"
                    else -> "Config"
                }
            } ?: "Config"
            generatedFile.files.map { it.name to label }
        }
        is SimpleFile -> listOf(generatedFile.path.fileName.toString() to "Config")
        is ResourceFile -> listOf(generatedFile.fileName to "Config")
        is ResourceSourceSet -> generatedFile.files.map { it.fileName to "Config" }
    }
}
