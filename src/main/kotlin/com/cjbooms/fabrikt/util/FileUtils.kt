package com.cjbooms.fabrikt.util

import com.cjbooms.fabrikt.cli.OutputOptionType
import com.cjbooms.fabrikt.generators.MutableSettings
import com.squareup.kotlinpoet.FileSpec
import java.io.InputStream
import java.nio.file.Path

object FileUtils {
    fun InputStream.writeFileTo(path: Path) {
        path.toFile().outputStream().use { this.copyTo(it) }
    }

    fun FileSpec.Builder.addFileDisclaimer(): FileSpec.Builder {
        if (MutableSettings.outputOptions.contains(OutputOptionType.ADD_FILE_DISCLAIMER)) {
            addFileComment(
                """

                This file was generated from an OpenAPI specification by Fabrikt.
                DO NOT EDIT. Any changes will be overwritten the next time the code is generated.
                To update, modify the specification and re-generate.

                """.trimIndent(),
            )
        }
        return this
    }
}
