package examples.inLinedObject.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid

public data class FirstInlineObject(
  @param:JsonProperty("generation")
  @get:JsonProperty("generation")
  @get:Valid
  public val generation: FirstInlineObjectGeneration? = null,
)
