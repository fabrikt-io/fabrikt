package examples.parameterNameClash.client

/**
 * Configuration for the API.
 * @property basePath The base URL path for the API
 * @property customHeaders A map of custom HTTP headers to include in every request
 */
class ApiConfiguration(
    val basePath: String = "",
    val customHeaders: Map<String, String> = mapOf()
) {
    /**
     * Creates a copy of this configuration with optional overrides.
     * @param basePath The new base path, defaults to the current one
     * @param customHeaders The new custom headers, defaults to the current ones
     * @return A new ApiConfiguration instance
     */
    fun copy(
        basePath: String = this.basePath,
        customHeaders: Map<String, String> = this.customHeaders
    ): ApiConfiguration {
        return ApiConfiguration(basePath, customHeaders)
    }
}
