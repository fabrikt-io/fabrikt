package examples.directionalModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class PetNestedRequest(
    @param:JsonProperty("password")
    @get:JsonProperty("password")
    public val password: String,
)
