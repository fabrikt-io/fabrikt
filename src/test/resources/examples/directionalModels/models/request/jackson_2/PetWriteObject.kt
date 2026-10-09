package examples.directionalModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class PetWriteObject(
    @param:JsonProperty("token")
    @get:JsonProperty("token")
    public val token: String,
)
