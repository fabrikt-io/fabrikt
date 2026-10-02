package examples.directionalEndpoints.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import examples.directionalEndpoints.models.PetRequest
import examples.directionalEndpoints.models.PetResponse
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MultipartBody
import okhttp3.MultipartBody.Builder
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Map
import kotlin.jvm.Throws

@Suppress("unused")
public class PetsUploadClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     *
     * @param pet
     * @param token
     */
    @Throws(ApiException::class)
    public fun uploadPet(
        pet: PetRequest,
        token: String,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<PetResponse> {
        val httpUrl: HttpUrl =
            "$baseUrl/pets/upload"
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
        multipartBuilder.addFormDataPart("pet", objectMapper.writeValueAsString(pet))
        multipartBuilder.addFormDataPart("token", token.toString())
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
