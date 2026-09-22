package com.cjbooms.fabrikt.model

import java.time.Instant

data class GenerationMetadata(
    val date: Instant = Instant.now(),
    val version: String = fabriktVersion,
) {
    companion object {
        private val fabriktVersion: String =
            GenerationMetadata::class.java
                .getResourceAsStream("/META-INF/fabrikt-version.txt")
                ?.bufferedReader()
                ?.use { it.readText().trim() }
                ?: GenerationMetadata::class.java.`package`.implementationVersion
                ?: "development"
    }
}
