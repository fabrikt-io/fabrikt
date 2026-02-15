//
// This file was generated from an OpenAPI specification by Fabrikt.
// DO NOT EDIT. Any changes will be overwritten the next time the code is generated.
// To update, modify the specification and re-generate.
//
package examples.fileComment.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Long
import kotlin.String

public data class Pet(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  public val id: Long? = null,
  @param:JsonProperty("name")
  @get:JsonProperty("name")
  @get:NotNull
  public val name: String,
  @param:JsonProperty("tag")
  @get:JsonProperty("tag")
  @get:NotNull
  public val tag: String,
  @param:JsonProperty("type")
  @get:JsonProperty("type")
  public val type: PetType? = null,
)
