package examples.directionalModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class ReadOnlyRecordResponse(
    @param:JsonProperty("value")
    @get:JsonProperty("value")
    public val `value`: String,
)
