package examples.jsonSchemaConversion.eventType.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.DecimalMin
import kotlin.Int

public data class Threshold(
  @param:JsonProperty("score")
  @get:JsonProperty("score")
  @get:DecimalMin(
    value = "0",
    inclusive = false,
  )
  public val score: Int? = null,
)
