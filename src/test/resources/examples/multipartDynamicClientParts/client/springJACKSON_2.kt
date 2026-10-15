package examples.multipartDynamicClientParts.client

import examples.multipartDynamicClientParts.models.Payload
import org.springframework.core.io.Resource
import org.springframework.http.HttpEntity
import org.springframework.web.bind.`annotation`.RequestHeader
import org.springframework.web.bind.`annotation`.RequestParam
import org.springframework.web.bind.`annotation`.RequestPart
import org.springframework.web.service.`annotation`.HttpExchange
import kotlin.Any
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
     * @param dynamicParts Binary uploads keyed by declared patternProperties names; register DynamicMultipartArgumentResolver on the proxy builder.
     */
    @HttpExchange(
        url = "/upload",
        method = "POST",
        contentType = "multipart/form-data",
    )
    public fun upload(
        @RequestPart(value = "request", required = true) request: JsonPart<Payload>,
        @RequestPart(value = "optionalRequest", required = false) optionalRequest: JsonPart<Payload>?,
        @RequestPart(value = "json-items", required = false) jsonItems: JsonPart<List<Payload>>?,
        @RequestPart(value = "template", required = true) template: Resource,
        @RequestPart(value = "other-files", required = false) otherFiles: List<Resource>?,
        @RequestPart(value = "label", required = false) label: String?,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
        @DynamicMultipartParts(
            patterns = ["^resource\\..+${'$'}"],
            fixedNames = ["request", "optionalRequest", "json-items", "template", "other-files", "label"],
        ) dynamicParts: Map<String, HttpEntity<out Resource>> = emptyMap(),
    )
}
