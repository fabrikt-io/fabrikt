package examples.parameterNameClash.client

import examples.parameterNameClash.models.SomeObject
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RequestHeader
import org.springframework.web.bind.`annotation`.RequestParam
import org.springframework.web.service.`annotation`.HttpExchange
import kotlin.Any
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Map

@Suppress("unused")
public interface ExampleClient {
    /**
     * Returns 100% of the matching records
     * The merchant key must be prefixed by `key%3D`.
     * A literal %S is prose here, not a format specifier.
     *
     * @param pathB Identifier, percent-encoded (%3D is '=')
     * @param queryB
     */
    @HttpExchange(
        url = "/example/{b}",
        method = "GET",
    )
    public fun getExampleB(
        @PathVariable("b") pathB: String,
        @RequestParam("b") queryB: String,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )

    /**
     *
     *
     * @param bodySomeObject example
     * @param querySomeObject
     */
    @HttpExchange(
        url = "/example",
        method = "POST",
        contentType = "application/json",
    )
    public fun postExample(
        @RequestBody bodySomeObject: SomeObject?,
        @RequestParam("someObject") querySomeObject: String,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )
}
