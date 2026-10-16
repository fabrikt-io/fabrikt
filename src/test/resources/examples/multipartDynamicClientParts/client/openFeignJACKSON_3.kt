package examples.multipartDynamicClientParts.client

import examples.multipartDynamicClientParts.models.Payload
import feign.HeaderMap
import feign.Headers
import feign.Param
import feign.QueryMap
import feign.RequestLine
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map

@Suppress("unused")
public interface UploadClient {
    /**
     *
     *
     * @param request
     * @param optionalRequest
     * @param jsonItems
     * @param template
     * @param otherFiles
     * @param label
     *
     * @param dynamicParts Binary uploads keyed by declared patternProperties names; invalid or fixed names fail before transport.
     */
    @FeignJsonParts(
        "request",
        "optionalRequest",
        "json-items",
    )
    @RequestLine("POST /upload")
    @Headers("Content-Type: multipart/form-data")
    @FeignBinaryParts(
        parameter = "dynamicParts",
        patterns = ["^resource\\..+${'$'}"],
        fixedNames = ["request", "optionalRequest", "json-items", "template", "other-files", "label"],
    )
    public fun upload(
        @Param("request") request: Payload,
        @Param("optionalRequest") optionalRequest: Payload?,
        @Param("json-items") jsonItems: List<Payload>?,
        @Param("template") template: FeignMultipartFile,
        @Param("other-files") otherFiles: List<FeignMultipartFile>?,
        @Param("label") label: String?,
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
        @Param("dynamicParts") dynamicParts: Map<String, FeignMultipartFile> = emptyMap(),
    )
}
