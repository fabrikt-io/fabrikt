package examples.multipartUpload.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonValue
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.Map

public data class ErrorResponse(
    /**
     * Error message
     */
    @param:JsonProperty("error")
    @get:JsonProperty("error")
    @get:NotNull
    public val error: String,
    /**
     * Error code
     */
    @param:JsonProperty(
        "code",
        required = true,
    )
    @get:JsonProperty("code")
    @get:NotNull
    public val code: Int,
)

public data class FileMetadata(
    /**
     * Original filename
     */
    @param:JsonProperty("name")
    @get:JsonProperty("name")
    @get:NotNull
    public val name: String,
    @param:JsonProperty("category")
    @get:JsonProperty("category")
    @get:NotNull
    public val category: FileMetadataCategory,
    /**
     * MIME type of the file
     */
    @param:JsonProperty("contentType")
    @get:JsonProperty("contentType")
    public val contentType: String? = null,
    /**
     * File size in bytes
     */
    @param:JsonProperty("size")
    @get:JsonProperty("size")
    public val size: Long? = null,
)

public enum class FileMetadataCategory(
    @JsonValue
    public val `value`: String,
) {
    DOCUMENT("document"),
    IMAGE("image"),
    VIDEO("video"),
    OTHER("other"),
    ;

    override fun toString(): String = value

    public companion object {
        private val mapping: Map<String, FileMetadataCategory> =
            entries.associateBy(FileMetadataCategory::value)

        public fun fromValue(`value`: String): FileMetadataCategory? = mapping[value]
    }
}

public data class SimpleUploadResult(
    /**
     * Unique identifier for the uploaded file
     */
    @param:JsonProperty("id")
    @get:JsonProperty("id")
    @get:NotNull
    public val id: String,
    /**
     * Name of the uploaded file
     */
    @param:JsonProperty("filename")
    @get:JsonProperty("filename")
    @get:NotNull
    public val filename: String,
    /**
     * File size in bytes
     */
    @param:JsonProperty(
        "size",
        required = true,
    )
    @get:JsonProperty("size")
    @get:NotNull
    public val size: Long,
    /**
     * Upload timestamp
     */
    @param:JsonProperty("uploadedAt")
    @get:JsonProperty("uploadedAt")
    @get:NotNull
    public val uploadedAt: OffsetDateTime,
)

public data class UploadResult(
    /**
     * Unique identifier for the uploaded file
     */
    @param:JsonProperty("id")
    @get:JsonProperty("id")
    @get:NotNull
    public val id: String,
    /**
     * Original filename
     */
    @param:JsonProperty("originalName")
    @get:JsonProperty("originalName")
    @get:NotNull
    public val originalName: String,
    /**
     * Stored filename on server
     */
    @param:JsonProperty("storedName")
    @get:JsonProperty("storedName")
    @get:NotNull
    public val storedName: String,
    /**
     * URL to access the file
     */
    @param:JsonProperty("url")
    @get:JsonProperty("url")
    @get:NotNull
    public val url: String,
    /**
     * Upload timestamp
     */
    @param:JsonProperty("uploadedAt")
    @get:JsonProperty("uploadedAt")
    @get:NotNull
    public val uploadedAt: OffsetDateTime,
    /**
     * File size in bytes
     */
    @param:JsonProperty(
        "size",
        required = true,
    )
    @get:JsonProperty("size")
    @get:NotNull
    public val size: Long,
)
