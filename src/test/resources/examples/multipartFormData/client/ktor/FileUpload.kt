package examples.multipartFormData.client

import kotlin.ByteArray
import kotlin.String

/**
 * Represents a file upload with optional filename.
 *
 * @param content The file content as a byte array
 * @param filename Optional filename to use in the Content-Disposition header
 */
public data class FileUpload(
    public val content: ByteArray,
    public val filename: String? = null,
)
