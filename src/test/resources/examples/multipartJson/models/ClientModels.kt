package examples.multipartJson.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.String
import kotlin.collections.List

public data class JsonNested(
    @param:JsonProperty("values")
    @get:JsonProperty("values")
    @get:NotNull
    public val values: List<String>,
)

public data class JsonPayload(
    @param:JsonProperty("message")
    @get:JsonProperty("message")
    @get:NotNull
    public val message: String,
    @param:JsonProperty("note")
    @get:JsonProperty("note")
    public val note: String? = null,
    @param:JsonProperty("nested")
    @get:JsonProperty("nested")
    @get:NotNull
    @get:Valid
    public val nested: JsonNested,
)
