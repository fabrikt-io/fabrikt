package examples.requestBodiesSchema.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class UpdateWidgetRequest(
  @param:JsonProperty("name")
  @get:JsonProperty("name")
  @get:NotNull
  public val name: String,
  @param:JsonProperty("description")
  @get:JsonProperty("description")
  public val description: String? = null,
)
