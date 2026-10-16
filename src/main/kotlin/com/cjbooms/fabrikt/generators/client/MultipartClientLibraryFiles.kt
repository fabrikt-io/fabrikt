package com.cjbooms.fabrikt.generators.client

import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.GeneratorUtils.hasMultipartRequestBody
import com.cjbooms.fabrikt.model.Destinations
import com.cjbooms.fabrikt.model.GeneratedFile
import com.cjbooms.fabrikt.model.MultipartParameter
import com.cjbooms.fabrikt.model.OpenApiOperation
import com.cjbooms.fabrikt.model.SimpleFile
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.requestOperations
import com.squareup.kotlinpoet.FileSpec
import java.nio.file.Path
import java.util.logging.Logger

internal fun OpenApiOperation.jsonMultipartContentType(parameter: MultipartParameter): String {
    val declared =
        requestBody.contentMediaTypes.entries
            .firstOrNull { it.key.startsWith("multipart/form-data") }
            ?.value
            ?.encodingContentType(parameter.partName)
    if (declared != null && declared != "application/json") {
        Logger.getGlobal().warning(
            "Multipart part '${parameter.partName}' declares unsupported content type '$declared'; " +
                "using application/json with the configured JSON serializer. " +
                "Set encoding.${parameter.partName}.contentType to application/json to match.",
        )
    }
    return "application/json"
}

internal object MultipartClientLibraryFiles {
    fun feign(
        packages: Packages,
        api: SourceApi,
    ): Collection<GeneratedFile> = library(packages, api, "FeignMultipartEncoder")

    fun spring(
        packages: Packages,
        api: SourceApi,
        srcPath: Path = Destinations.MAIN_KT_SOURCE,
    ): Collection<GeneratedFile> = library(packages, api, "JsonPart", srcPath)

    private fun library(
        packages: Packages,
        api: SourceApi,
        name: String,
        srcPath: Path = Destinations.MAIN_KT_SOURCE,
    ): Collection<GeneratedFile> {
        val hasMultipart =
            api.openApi3.paths.values.any { path ->
                api.requestOperations(path).any { (_, op) -> op.hasMultipartRequestBody() }
            }
        if (!hasMultipart) return emptyList()
        val template =
            checkNotNull(javaClass.getResourceAsStream("/client/$name.kt.txt"))
                .bufferedReader()
                .use { it.readText() }
        return listOf(
            SimpleFile(
                srcPath.resolve(Path.of(packages.client.replace('.', '/'), "$name.kt")),
                template.replace(
                    "package {{package}}",
                    FileSpec
                        .builder(packages.client, name)
                        .build()
                        .toString()
                        .trim(),
                ),
            ),
        )
    }
}
