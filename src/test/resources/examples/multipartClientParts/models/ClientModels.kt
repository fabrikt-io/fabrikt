package examples.multipartClientParts.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.String
import kotlin.collections.List

public data class Nested(
    @param:JsonProperty("values")
    @get:JsonProperty("values")
    @get:NotNull
    public val values: List<String>,
)

public data class Payload(
    @param:JsonProperty("message")
    @get:JsonProperty("message")
    @get:NotNull
    public val message: String,
    @param:JsonProperty("nested")
    @get:JsonProperty("nested")
    @get:NotNull
    @get:Valid
    public val nested: Nested,
)
