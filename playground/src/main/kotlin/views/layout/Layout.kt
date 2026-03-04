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

fun HTML.mainLayout(version: String, content: FlowContent.() -> Unit) = run {
    head {
        title { +"Fabrikt Playground" }
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
            href = "/static/main.css?v=$version"
        }
        script { src = "/static/main.js?v=$version" }
        meta ( name = "viewport", content = "width=device-width, initial-scale=1" )
    }
    body {
        content()
    }
}
