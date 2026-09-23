package com.example.client

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import javax.`annotation`.processing.Generated
import kotlin.Any
import kotlin.Boolean
import kotlin.ByteArray
import kotlin.Pair
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.jvm.Throws

@Suppress("unused")
@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    comments = "Generated with Fabrikt v27.0.1",
)
public fun <T : Any> HttpUrl.Builder.queryParam(
    key: String,
    `value`: T?,
): HttpUrl.Builder {
    if (value != null) this.addQueryParameter(key, value.toString())
    return this
}

@Suppress("unused")
@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    comments = "Generated with Fabrikt v27.0.1",
)
public fun <T : Any> FormBody.Builder.formParam(
    key: String,
    `value`: T?,
): FormBody.Builder {
    if (value != null) this.add(key, value.toString())
    return this
}

@Suppress("unused")
@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    comments = "Generated with Fabrikt v27.0.1",
)
public fun HttpUrl.Builder.queryParam(
    key: String,
    values: List<Any>?,
    explode: Boolean = true,
): HttpUrl.Builder {
    if (values != null) {
        if (explode) {
            values.forEach { addQueryParameter(key, it.toString()) }
        } else {
            addQueryParameter(key, values.joinToString(","))
        }
    }
    return this
}

@Suppress("unused")
@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    comments = "Generated with Fabrikt v27.0.1",
)
public fun Headers.Builder.`header`(
    key: String,
    `value`: Any?,
): Headers.Builder {
    if (value != null) this.add(key, value.toString())
    return this
}

@Throws(ApiException::class)
@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    comments = "Generated with Fabrikt v27.0.1",
)
public fun <T> Request.execute(
    client: OkHttpClient,
    objectMapper: ObjectMapper,
    typeRef: TypeReference<T>,
): ApiResponse<T> =
    doRequest(client) { responseBody ->
        responseBody?.deserialize(objectMapper, typeRef)
    }

@Throws(ApiException::class)
@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    comments = "Generated with Fabrikt v27.0.1",
)
public fun Request.execute(client: OkHttpClient): ApiResponse<ByteArray> =
    doRequest(client) { responseBody ->
        responseBody?.deserialize()
    }

@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    comments = "Generated with Fabrikt v27.0.1",
)
private fun <T> Request.doRequest(
    client: OkHttpClient,
    bodyReader: (ResponseBody?) -> T?,
): ApiResponse<T> =
    client.newCall(this).execute().use { response ->
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
@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    comments = "Generated with Fabrikt v27.0.1",
)
public fun String.pathParam(vararg params: Pair<String, Any>): String =
    params.fold(this) { acc, param ->
        acc.replace(param.first, param.second.toString())
    }

@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    comments = "Generated with Fabrikt v27.0.1",
)
public fun <T> ResponseBody.deserialize(
    objectMapper: ObjectMapper,
    typeRef: TypeReference<T>,
): T? = this.string().isNotBlankOrNull()?.let { objectMapper.readValue(it, typeRef) }

@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    comments = "Generated with Fabrikt v27.0.1",
)
public fun ResponseBody.deserialize(): ByteArray? = this.byteStream().readAllBytes()

@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    comments = "Generated with Fabrikt v27.0.1",
)
public fun String?.isNotBlankOrNull(): String? = if (this.isNullOrBlank()) null else this

@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    comments = "Generated with Fabrikt v27.0.1",
)
private fun Response.errorMessage(): String = this.body?.string() ?: this.message

@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    comments = "Generated with Fabrikt v27.0.1",
)
private fun Response.isBadRequest(): Boolean = this.code in 400..499

@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    comments = "Generated with Fabrikt v27.0.1",
)
private fun Response.isServerError(): Boolean = this.code in 500..599

@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    comments = "Generated with Fabrikt v27.0.1",
)
private fun Response.isRedirection(): Boolean = this.code in 300..399

@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    comments = "Generated with Fabrikt v27.0.1",
)
public data class RequestBodyWithFilename(
    public val requestBody: RequestBody,
    public val filename: String,
)
