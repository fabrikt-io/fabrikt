package com.cjbooms.fabrikt.parser

import com.cjbooms.fabrikt.util.HttpFetch
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths

internal fun interface SourceDocumentLoader {
    fun load(documentUri: URI): String
}

internal class DefaultSourceDocumentLoader(
    private val headers: List<Pair<String, String>> = emptyList(),
) : SourceDocumentLoader {
    override fun load(documentUri: URI): String =
        when (documentUri.scheme?.lowercase()) {
            "file" -> Files.readString(Paths.get(documentUri))
            "http", "https" -> HttpFetch.fetch(documentUri, headers)
            else -> throw IllegalArgumentException("Unsupported source document URI: $documentUri")
        }
}
