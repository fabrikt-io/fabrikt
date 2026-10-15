package examples.multipartJson.client

import examples.multipartJson.models.JsonPayload
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.MultipartBody.Builder
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.jacksonTypeRef
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.Map
import kotlin.jvm.Throws

@Suppress("unused")
public class JsonClient(
    private val objectMapper: JsonMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     *
     * @param request
     * @param details
     * @param optionalRequest
     * @param template
     */
    @Throws(ApiException::class)
    public fun uploadJson(
        request: JsonPayload,
        details: JsonPayload?,
        optionalRequest: JsonPayload?,
        template: RequestBodyWithFilename,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/json"
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
        multipartBuilder.addFormDataPart(
            "request",
            null,
            objectMapper.writeValueAsString(request).toRequestBody("application/json".toMediaType()),
        )
        details?.let {
            multipartBuilder.addFormDataPart(
                "details",
                null,
                objectMapper.writeValueAsString(details).toRequestBody("application/json".toMediaType()),
            )
        }
        optionalRequest?.let {
            multipartBuilder.addFormDataPart(
                "optionalRequest",
                null,
                objectMapper.writeValueAsString(optionalRequest).toRequestBody("application/json".toMediaType()),
            )
        }
        multipartBuilder.addFormDataPart("template", template.filename, template.requestBody)
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
