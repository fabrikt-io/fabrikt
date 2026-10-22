package examples.queryOperation.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class SearchRequest(
    @param:JsonProperty("name")
    @get:JsonProperty("name")
    @get:NotNull
    public val name: String,
)

public data class SearchResult(
    @param:JsonProperty("name")
    @get:JsonProperty("name")
    @get:NotNull
    public val name: String,
)
