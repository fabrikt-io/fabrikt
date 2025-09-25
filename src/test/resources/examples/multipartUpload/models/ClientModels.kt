package examples.multipartUpload.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import javax.validation.constraints.NotNull
import kotlin.String
import kotlin.collections.List

public data class MediaFileDto(
    @param:JsonProperty("filename")
    @get:JsonProperty("filename")
    @get:NotNull
    public val filename: String,
    @param:JsonProperty("description")
    @get:JsonProperty("description")
    public val description: String? = null,
    @param:JsonProperty("tags")
    @get:JsonProperty("tags")
    public val tags: List<String>? = null,
)

public data class UploadResponse(
    @param:JsonProperty("uploadId")
    @get:JsonProperty("uploadId")
    public val uploadId: String? = null,
    @param:JsonProperty("status")
    @get:JsonProperty("status")
    public val status: String? = null,
    @param:JsonProperty("message")
    @get:JsonProperty("message")
    public val message: String? = null,
)
