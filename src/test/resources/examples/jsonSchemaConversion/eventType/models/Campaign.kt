package examples.jsonSchemaConversion.eventType.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class Campaign(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  public val id: String,
)
