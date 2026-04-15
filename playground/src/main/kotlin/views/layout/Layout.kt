package views.layout

import kotlinx.html.FlowContent
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.title

private const val PRISM_VERSION = "1.29.0"
private const val ACE_VERSION = "1.36.5"
private const val HTMX_VERSION = "2.0.3"
private const val NORMALIZE_VERSION = "8.0.1"
private const val BASSCSS = "8.1.0"

fun HTML.mainLayout(content: FlowContent.() -> Unit) = run {
    head {
        title { +"Fabrikt Playground — Kotlin Code Generator for OpenAPI" }
        meta(name = "description", content = "Generate Kotlin data classes, HTTP clients, and server controllers from your OpenAPI 3 spec. Try fabrikt live in your browser.")
        meta { attributes["property"] = "og:title"; attributes["content"] = "Fabrikt Playground" }
        meta { attributes["property"] = "og:description"; attributes["content"] = "Generate Kotlin data classes, HTTP clients, and server controllers from your OpenAPI 3 spec. Try fabrikt live in your browser." }
        meta { attributes["property"] = "og:type"; attributes["content"] = "website" }
        meta { attributes["property"] = "og:url"; attributes["content"] = "https://try.fabrikt.io" }
        meta { attributes["property"] = "og:image"; attributes["content"] = "https://raw.githubusercontent.com/fabrikt-io/fabrikt/master/.github/assets/fabrikt-social-preview.png" }
        meta(name = "twitter:card", content = "summary_large_image")
        meta(name = "twitter:title", content = "Fabrikt Playground")
        meta(name = "twitter:description", content = "Generate Kotlin data classes, HTTP clients, and server controllers from your OpenAPI 3 spec.")
        meta(name = "twitter:image", content = "https://raw.githubusercontent.com/fabrikt-io/fabrikt/master/.github/assets/fabrikt-social-preview.png")
        script { src = "https://cdnjs.cloudflare.com/ajax/libs/prism/$PRISM_VERSION/prism.min.js" }
        script { src = "https://cdnjs.cloudflare.com/ajax/libs/prism/$PRISM_VERSION/components/prism-kotlin.min.js" }
        script { src = "https://cdnjs.cloudflare.com/ajax/libs/ace/$ACE_VERSION/ace.js" }
        script { src = "https://cdnjs.cloudflare.com/ajax/libs/htmx/$HTMX_VERSION/htmx.min.js" }
        link {
            rel = "preconnect"
            href = "https://fonts.googleapis.com"
        }
        link {
            rel = "preconnect"
            href = "https://fonts.gstatic.com"
            attributes["crossorigin"]
        }
        link {
            rel = "stylesheet"
            href = "https://fonts.googleapis.com/css2?family=Roboto:ital,wght@0,100;0,300;0,400;0,500;0,700;0,900;1,100;1,300;1,400;1,500;1,700;1,900&display=swap"
        }
        link {
            rel = "stylesheet"
            href = "https://cdnjs.cloudflare.com/ajax/libs/normalize/$NORMALIZE_VERSION/normalize.min.css"
        }
        link {
            rel = "stylesheet"
            href = "https://cdnjs.cloudflare.com/ajax/libs/basscss/$BASSCSS/css/basscss.min.css"
        }
        link {
            rel = "stylesheet"
            href = "https://cdnjs.cloudflare.com/ajax/libs/prism/$PRISM_VERSION/themes/prism.css"
        }
        link {
            rel = "stylesheet"
            href = "/static/main.css"
        }
        meta ( name = "viewport", content = "width=device-width, initial-scale=1" )
    }
    body {
        content()
    }
}
