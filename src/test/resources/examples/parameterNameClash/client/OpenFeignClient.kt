package examples.parameterNameClash.client

import examples.parameterNameClash.models.SomeObject
import feign.HeaderMap
import feign.Param
import feign.QueryMap
import feign.RequestLine
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Map

@Suppress("unused")
public interface ExampleClient {
    /**
     *
     *
     * @param pathB
     * @param queryB
     */
    @RequestLine("GET /example/{pathB}?b={queryB}")
    public fun getExampleB(
        @Param("pathB") pathB: String,
        @Param("queryB") queryB: String,
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    )

    /**
     *
     *
     * @param bodySomeObject example
     * @param querySomeObject
     */
    @RequestLine("POST /example?someObject={querySomeObject}")
    public fun postExample(
        bodySomeObject: SomeObject?,
        @Param("querySomeObject") querySomeObject: String,
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    )
}
