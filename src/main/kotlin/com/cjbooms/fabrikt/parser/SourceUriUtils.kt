package com.cjbooms.fabrikt.parser

import java.net.URI

internal fun String.resolveAgainst(baseUri: URI): URI? {
    if (isEmpty()) return baseUri
    return runCatching { baseUri.resolve(URI(this)).normalize().toAsciiUri() }.getOrNull()
}

internal fun URI.withoutFragment(): URI = rawFragment?.let { URI(toString().substringBeforeLast('#')) } ?: this

internal fun URI.withFragment(fragment: String): URI {
    val encodedFragment = URI(null, null, null, fragment).rawFragment
    return URI("${withoutFragment()}#$encodedFragment").toAsciiUri()
}

internal fun URI.toAsciiUri(): URI = URI(toASCIIString())
