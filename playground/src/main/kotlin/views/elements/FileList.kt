import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.li
import kotlinx.html.script
import kotlinx.html.ul
import kotlinx.html.unsafe

private val LABEL_ORDER = listOf("Model", "Client", "Controller", "Config")

private fun String.toHeading() = when (this) {
    "Model" -> "Models"
    "Controller" -> "Controllers"
    else -> this
}

fun FlowContent.fileList(files: List<Pair<String, String>>) {
    val groups = files.sortedBy { it.first }.groupBy { it.second }
    LABEL_ORDER.forEach { label ->
        val groupFiles = groups[label] ?: return@forEach
        div(classes = "file-nav-group") {
            div(classes = "file-nav-heading") { +label.toHeading() }
            ul(classes = "file-nav") {
                groupFiles.forEach { (fileName, _) ->
                    li {
                        a(href = "#$fileName") { +fileName }
                    }
                }
            }
        }
    }
    script {
        unsafe {
            +"""
                document.querySelectorAll('.file-nav a').forEach(function(link) {
                    link.addEventListener('click', function(e) {
                        e.preventDefault();
                        var target = document.getElementById(this.getAttribute('href').slice(1));
                        var codePanel = document.getElementById('codeview') && document.getElementById('codeview').closest('.panel');
                        if (target && codePanel) {
                            var delta = target.getBoundingClientRect().top - codePanel.getBoundingClientRect().top;
                            codePanel.scrollBy({ top: delta });
                        }
                    });
                });
            """.trimIndent()
        }
    }
}
