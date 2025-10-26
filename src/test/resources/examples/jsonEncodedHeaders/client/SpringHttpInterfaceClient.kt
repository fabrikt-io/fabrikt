package examples.jsonEncodedHeaders.client

import com.fasterxml.jackson.databind.JsonNode
import examples.multiMediaType.models.ContentType
import examples.multiMediaType.models.QueryResult
import examples.multiMediaType.models.SuccessResponse
import org.springframework.web.bind.`annotation`.RequestHeader
import org.springframework.web.bind.`annotation`.RequestParam
import org.springframework.web.service.`annotation`.HttpExchange
import kotlin.Any
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map

@Suppress("unused")
public interface ExamplePath1Client {
    /**
     * GET example path 1
     *
     * @param explodeListQueryParam
     * @param queryParam2
     * @param acceptHeader
     */
    @HttpExchange(
        url = "/example-path-1",
        method = "GET",
        accept = ["application/vnd.custom.media+xml"],
    )
    public fun getExamplePath1(
        @RequestParam("explode_list_query_param") explodeListQueryParam: List<String>? = null,
        @RequestParam("query_param2") queryParam2: Int? = null,
        @RequestHeader("Accept") acceptHeader: String = "application/vnd.custom.media+xml",
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    ): QueryResult
}

@Suppress("unused")
public interface ExamplePath2Client {
    /**
     * GET example path 1
     *
     * @param explodeListQueryParam
     * @param queryParam2
     * @param accept the content type accepted by the client
     */
    @HttpExchange(
        url = "/example-path-2",
        method = "GET",
        accept = ["application/vnd.custom.media+xml"],
    )
    public fun getExamplePath2(
        @RequestParam("explode_list_query_param") explodeListQueryParam: List<String>? = null,
        @RequestParam("query_param2") queryParam2: Int? = null,
        @RequestHeader("Accept") accept: ContentType? = null,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    ): QueryResult
}

@Suppress("unused")
public interface MultipleResponseSchemasClient {
    /**
     * GET with multiple response content schemas
     *
     * @param accept the content type accepted by the client
     */
    @HttpExchange(
        url = "/multiple-response-schemas",
        method = "GET",
        accept = ["application/json"],
    )
    public fun getMultipleResponseSchemas(
        @RequestHeader("Accept") accept: ContentType? = null,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    ): JsonNode
}

@Suppress("unused")
public interface DifferentSuccessAndErrorResponseSchemaClient {
    /**
     *
     */
    @HttpExchange(
        url = "/different-success-and-error-response-schema",
        method = "GET",
        accept = ["application/json"],
    )
    public fun getDifferentSuccessAndErrorResponseSchema(
        @RequestHeader
        additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam
        additionalQueryParameters: Map<String, Any> = emptyMap(),
    ): SuccessResponse
}
