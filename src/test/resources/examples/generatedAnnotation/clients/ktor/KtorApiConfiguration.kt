package com.example.client

import javax.`annotation`.processing.Generated
import kotlin.String
import kotlin.collections.Map

/**
 * Configuration for the API.
 * @property basePath The base URL path for the API
 * @property customHeaders A map of custom HTTP headers to include in every request
 */
@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    date = "2001-07-04T19:08:56.235Z",
    comments = "Generated with Fabrikt v27.0.1",
)
public class ApiConfiguration(
    public val basePath: String = "",
    public val customHeaders: Map<String, String> = mapOf(),
) {
    /**
     * Creates a copy of this configuration with optional overrides.
     * @param basePath The new base path, defaults to the current one
     * @param customHeaders The new custom headers, defaults to the current ones
     * @return A new ApiConfiguration instance
     */
    public fun copy(
        basePath: String = this.basePath,
        customHeaders: Map<String, String> =
            this.customHeaders,
    ): ApiConfiguration = ApiConfiguration(basePath, customHeaders)
}
