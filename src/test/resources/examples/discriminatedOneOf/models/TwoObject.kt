package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class TwoObject(
  @get:JsonProperty("type")
  @get:NotNull
  @param:JsonProperty("type")
  public val type: String = "content_block_location",
) : SomeObjInlinedArray, SomeObjInlinedObject, SomeObjInlinedObjectNoMappings
