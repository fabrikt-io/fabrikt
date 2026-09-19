package examples.deleteRequestBody.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String
import kotlin.collections.List

public data class DeleteResourcesRequest(
    @param:JsonProperty("resourceIds")
    @get:JsonProperty("resourceIds")
    @get:NotNull
    public val resourceIds: List<String>,
)
