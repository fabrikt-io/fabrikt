package examples.directionalModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class PetNestedResponse(
    @param:JsonProperty("code")
    @get:JsonProperty("code")
    public val code: String,
)
