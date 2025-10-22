package examples.okHttpClientPostWithoutRequestBody.client

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody

@Suppress("unused")
fun <T : Any> HttpUrl.Builder.queryParam(key: String, value: T?): HttpUrl.Builder = this.apply {
    if (value != null) this.addQueryParameter(key, value.toString())
}

@Suppress("unused")
fun <T : Any> FormBody.Builder.formParam(key: String, value: T?): FormBody.Builder = this.apply {
    if (value != null) this.add(key, value.toString())
}

@Suppress("unused")
fun HttpUrl.Builder.queryParam(key: String, values: List<Any>?, explode: Boolean = true) = this.apply {
    if (values != null) {
        if (explode) values.forEach { addQueryParameter(key, it.toString()) }
        else addQueryParameter(key, values.joinToString(","))
    }
}

@Suppress("unused")
fun Headers.Builder.header(key: String, value: String?): Headers.Builder = this.apply {
    if (value != null) this.add(key, value)
}

@Throws(ApiException::class)
fun <T> Request.execute(client: OkHttpClient, objectMapper: ObjectMapper, typeRef: TypeReference<T>): ApiResponse<T> =
    client.newCall(this).execute().use { response ->
        when {
            response.isSuccessful ->
                ApiResponse(response.code, response.headers, response.body?.deserialize(objectMapper, typeRef))
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
fun String.pathParam(vararg params: Pair<String, Any>): String = params.fold(this) { acc, param ->
    acc.replace(param.first, param.second.toString())
}

fun <T> ResponseBody.deserialize(objectMapper: ObjectMapper, typeRef: TypeReference<T>): T? =
    this.string().isNotBlankOrNull()?.let { objectMapper.readValue(it, typeRef) }

fun String?.isNotBlankOrNull() = if (this.isNullOrBlank()) null else this

private fun Response.errorMessage(): String = this.body?.string() ?: this.message

private fun Response.isBadRequest(): Boolean = this.code in 400..499

private fun Response.isServerError(): Boolean = this.code in 500..599

private fun Response.isRedirection(): Boolean = this.code in 300..399
