package examples.externalReferences.aggressive.client

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.Any
import kotlin.Boolean
import kotlin.ByteArray
import kotlin.Pair
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.jvm.Throws
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody

@Suppress("unused")
public fun <T : Any> HttpUrl.Builder.queryParam(key: String, `value`: T?): HttpUrl.Builder {
    if (value != null) this.addQueryParameter(key, value.toString())
    return this
}

@Suppress("unused")
public fun <T : Any> FormBody.Builder.formParam(key: String, `value`: T?): FormBody.Builder {
    if (value != null) this.add(key, value.toString())
    return this
}

@Suppress("unused")
public fun HttpUrl.Builder.queryParam(
    key: String,
    values: List<Any>?,
    explode: Boolean = true,
): HttpUrl.Builder {
    if (values != null) {
        if (explode) values.forEach { addQueryParameter(key, it.toString()) }
        else addQueryParameter(key, values.joinToString(","))
    }
    return this
}

@Suppress("unused")
public fun Headers.Builder.`header`(key: String, `value`: Any?): Headers.Builder {
    if (value != null) this.add(key, value.toString())
    return this
}

@Throws(ApiException::class)
public fun <T> Request.execute(
    client: OkHttpClient,
    objectMapper: ObjectMapper,
    typeRef: TypeReference<T>,
): ApiResponse<T> = doRequest(client) { responseBody ->
    responseBody?.deserialize(objectMapper, typeRef)
}

@Throws(ApiException::class)
public fun Request.execute(client: OkHttpClient): ApiResponse<ByteArray> =
        doRequest(client) { responseBody ->
    responseBody?.deserialize()
}

private fun <T> Request.doRequest(client: OkHttpClient, bodyReader: (ResponseBody?) -> T?):
        ApiResponse<T> = client.newCall(this).execute().use { response ->
    when {
        response.isSuccessful ->
            ApiResponse(response.code, response.headers, bodyReader(response.body))
        response.isRedirection() ->
            throw ApiRedirectException(response.code, response.headers, response.errorMessage())
        response.isBadRequest() ->
            throw ApiClientException(response.code, response.headers, response.errorMessage())
        response.isServerError() ->
            throw ApiServerException(response.code, response.headers, response.errorMessage())
        else -> throw ApiException("[${response.code}]: ${response.errorMessage()}")
    }
}

@Suppress("unused")
public fun String.pathParam(vararg params: Pair<String, Any>): String =
        params.fold(this) { acc, param ->
    acc.replace(param.first, param.second.toString())
}

public fun <T> ResponseBody.deserialize(objectMapper: ObjectMapper, typeRef: TypeReference<T>): T? =
        this.string().isNotBlankOrNull()?.let { objectMapper.readValue(it, typeRef) }

public fun ResponseBody.deserialize(): ByteArray? = this.byteStream().readAllBytes()

public fun String?.isNotBlankOrNull(): String? = if (this.isNullOrBlank()) null else this

private fun Response.errorMessage(): String = this.body?.string() ?: this.message

private fun Response.isBadRequest(): Boolean = this.code in 400..499

private fun Response.isServerError(): Boolean = this.code in 500..599

private fun Response.isRedirection(): Boolean = this.code in 300..399

public data class RequestBodyWithFilename(
    public val requestBody: RequestBody,
    public val filename: String,
)
