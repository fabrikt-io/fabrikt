package examples.multipartFormData.client

import examples.multipartFormData.models.UploadResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.append
import io.ktor.client.request.forms.formData
import io.ktor.client.request.`header`
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.http.isSuccess
import io.ktor.serialization.ContentConvertException
import kotlinx.coroutines.CancellationException
import java.io.IOException
import kotlin.String
import kotlin.collections.List

public class FilesUploadClient(
    private val httpClient: HttpClient,
) {
    /**
     * Upload a single file
     *
     * Parameters:
     *
     * Returns:
     * 	[NetworkResult.Success] with [examples.multipartFormData.models.UploadResponse] if the request
     * was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun uploadFile(
        `file`: FileUpload,
        description: String? = null,
    ): NetworkResult<UploadResponse> {
        val url = """/files/upload"""

        return try {
            val response =
                httpClient.post(url) {
                    `header`("Accept", "application/json")
                    setBody(
                        MultiPartFormDataContent(
                            formData {
                                val fileFilenameValue = `file`.filename ?: "file"
                                append(
                                    "file",
                                    `file`.content,
                                    headersOf(
                                        HttpHeaders.ContentDisposition,
                                        """filename="$fileFilenameValue"""",
                                    ),
                                )
                                description?.let { append("description", it.toString()) }
                            },
                        ),
                    )
                }

            if (response.status.isSuccess()) {
                NetworkResult.Success(response.body())
            } else {
                val errorBody = response.bodyAsText().ifBlank { null }
                NetworkResult.Failure(
                    NetworkError.Http(
                        statusCode = response.status.value,
                        statusDescription = response.status.description,
                        body = errorBody,
                    ),
                )
            }
        } catch (e: ResponseException) {
            val status = e.response.status
            val body = runCatching { e.response.bodyAsText() }.getOrNull()?.ifBlank { null }
            NetworkResult.Failure(NetworkError.Http(status.value, status.description, body))
        } catch (e: IOException) {
            NetworkResult.Failure(NetworkError.Network(e))
        } catch (e: ContentConvertException) {
            NetworkResult.Failure(NetworkError.Serialization(e))
        } catch (e: NoTransformationFoundException) {
            NetworkResult.Failure(NetworkError.Serialization(e))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            NetworkResult.Failure(NetworkError.Unknown(e))
        }
    }
}

public class FilesUploadMultipleClient(
    private val httpClient: HttpClient,
) {
    /**
     * Upload multiple files
     *
     * Parameters:
     *
     * Returns:
     * 	[NetworkResult.Success] with [examples.multipartFormData.models.UploadResponse] if the request
     * was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun uploadMultipleFiles(
        files: List<FileUpload>,
        category: String,
    ): NetworkResult<UploadResponse> {
        val url = """/files/upload-multiple"""

        return try {
            val response =
                httpClient.post(url) {
                    `header`("Accept", "application/json")
                    setBody(
                        MultiPartFormDataContent(
                            formData {
                                files.forEachIndexed { index, fileUpload ->
                                    val filename = fileUpload.filename ?: "files" + "_" + index
                                    append(
                                        "files",
                                        fileUpload.content,
                                        headersOf(
                                            HttpHeaders.ContentDisposition,
                                            """filename="$filename"""",
                                        ),
                                    )
                                }
                                append("category", category.toString())
                            },
                        ),
                    )
                }

            if (response.status.isSuccess()) {
                NetworkResult.Success(response.body())
            } else {
                val errorBody = response.bodyAsText().ifBlank { null }
                NetworkResult.Failure(
                    NetworkError.Http(
                        statusCode = response.status.value,
                        statusDescription = response.status.description,
                        body = errorBody,
                    ),
                )
            }
        } catch (e: ResponseException) {
            val status = e.response.status
            val body = runCatching { e.response.bodyAsText() }.getOrNull()?.ifBlank { null }
            NetworkResult.Failure(NetworkError.Http(status.value, status.description, body))
        } catch (e: IOException) {
            NetworkResult.Failure(NetworkError.Network(e))
        } catch (e: ContentConvertException) {
            NetworkResult.Failure(NetworkError.Serialization(e))
        } catch (e: NoTransformationFoundException) {
            NetworkResult.Failure(NetworkError.Serialization(e))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            NetworkResult.Failure(NetworkError.Unknown(e))
        }
    }
}
