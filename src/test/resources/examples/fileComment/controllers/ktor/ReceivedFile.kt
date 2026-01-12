//
// This file was generated from an OpenAPI specification by Fabrikt.
// DO NOT EDIT. Any changes will be overwritten the next time the code is generated.
// To update, modify the specification and re-generate.
//
package examples.fileComment.controllers

import io.ktor.http.ContentType
import kotlin.ByteArray
import kotlin.String

/**
 * Wrapper for received file content from multipart uploads.
 *
 * @property content The raw file content as a byte array
 * @property originalFileName The original filename from the Content-Disposition header, if provided
 * @property contentType The content type of the file, if provided
 */
public data class ReceivedFile(
  public val content: ByteArray,
  public val originalFileName: String? = null,
  public val contentType: ContentType? = null,
)
