package examples.multipartFormData.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import javax.validation.constraints.NotNull
import kotlin.String

public data class UploadResponse(
    /**
     * Unique identifier for the uploaded file
     */
    @param:JsonProperty("id")
    @get:JsonProperty("id")
    @get:NotNull
    public val id: String,
)
