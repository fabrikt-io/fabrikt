package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class OneObject(
  /**
   * Type Property
   */
  @get:JsonProperty("type")
  @get:NotNull
  @param:JsonProperty("type")
  public val type: String = "char_location",
) : SomeObjInlinedArray, SomeObjInlinedObject, SomeObjInlinedObjectNoMappings
