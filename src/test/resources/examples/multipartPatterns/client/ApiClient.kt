package examples.multipartPatterns.client

import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MultipartBody
import okhttp3.MultipartBody.Builder
import okhttp3.OkHttpClient
import okhttp3.Request
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.jacksonTypeRef
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.Map
import kotlin.jvm.Throws

@Suppress("unused")
public class DynamicClient(
    private val objectMapper: JsonMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     *
     * @param template
     * @param resourceFixed
     *
     * @param dynamicParts Binary uploads keyed by declared patternProperties names; invalid or fixed names throw IllegalArgumentException.
     */
    @Throws(ApiException::class)
    public fun uploadDynamic(
        template: RequestBodyWithFilename,
        resourceFixed: RequestBodyWithFilename?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
        dynamicParts: Map<String, RequestBodyWithFilename> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/dynamic"
                .toHttpUrl()
                .newBuilder()
                .also { builder -> additionalQueryParameters.forEach { builder.queryParam(it.key, it.value) } }
                .build()

        val headerBuilder = Headers.Builder()
        additionalHeaders.forEach { headerBuilder.header(it.key, it.value) }
        val httpHeaders: Headers = headerBuilder.build()

        val multipartBuilder =
            MultipartBody
                .Builder()
                .setType(MultipartBody.FORM)
        multipartBuilder.addFormDataPart("template", template.filename, template.requestBody)
        resourceFixed?.let {
            multipartBuilder.addFormDataPart("resource.fixed", resourceFixed.filename, resourceFixed.requestBody)
        }
        dynamicParts.forEach { (partName, fileData) ->
            require(
                partName.isNotEmpty() &&
                    partName.all {
                        it in "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789._-"
                    },
            ) { "Dynamic multipart names must contain only ASCII letters, digits, '.', '_' or '-'" }
            require(partName !in setOf("template", "resource.fixed")) { "Dynamic multipart name conflicts with a fixed property" }
            require(
                Regex("^resource\\.[A-Za-z0-9][A-Za-z0-9._-]{0,127}${'$'}").containsMatchIn(partName) ||
                    Regex("font").containsMatchIn(partName),
            ) {
                "Dynamic multipart name does not match patternProperties"
            }
            multipartBuilder.addFormDataPart(partName, fileData.filename, fileData.requestBody)
        }
        val multipartBody = multipartBuilder.build()
        val request: Request =
            Request
                .Builder()
                .url(httpUrl)
                .headers(httpHeaders)
                .post(multipartBody)
                .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }
}
