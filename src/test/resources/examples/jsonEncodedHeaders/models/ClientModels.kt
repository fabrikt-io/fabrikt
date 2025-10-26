package examples.jsonEncodedHeaders.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import javax.validation.Valid
import kotlin.String

public data class JsonEncodedHeader(
    @param:JsonProperty("headerProp1")
    @get:JsonProperty("headerProp1")
    public val headerProp1: String? = null,
    @param:JsonProperty("headerProp2")
    @get:JsonProperty("headerProp2")
    public val headerProp2: String? = null,
    @param:JsonProperty("nestedProp")
    @get:JsonProperty("nestedProp")
    @get:Valid
    public val nestedProp: NestedProp? = null,
)

public data class NestedProp(
    @param:JsonProperty("nestedProp1")
    @get:JsonProperty("nestedProp1")
    public val nestedProp1: String? = null,
)

public data class QueryParamsResult(
    @param:JsonProperty("title")
    @get:JsonProperty("title")
    public val title: String? = null,
)
