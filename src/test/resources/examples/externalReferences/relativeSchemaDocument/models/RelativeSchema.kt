package examples.externalReferences.relativeSchemaDocument.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List

public data class RelativeSchema(
  @param:JsonProperty("name")
  @get:JsonProperty("name")
  public val name: String? = null,
  @param:JsonProperty("age")
  @get:JsonProperty("age")
  public val age: Int? = null,
  @param:JsonProperty("active")
  @get:JsonProperty("active")
  public val active: Boolean? = null,
  @param:JsonProperty("tags")
  @get:JsonProperty("tags")
  public val tags: List<String>? = null,
  @param:JsonProperty("address")
  @get:JsonProperty("address")
  @get:Valid
  public val address: RelativeSchemaAddress? = null,
)
