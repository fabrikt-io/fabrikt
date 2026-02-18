package examples.externalReferences.targeted.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class ExternalObjectThree(
  @param:JsonProperty("enum")
  @get:JsonProperty("enum")
  @get:NotNull
  public val `enum`: ExternalObjectThreeEnum,
  @param:JsonProperty("description")
  @get:JsonProperty("description")
  @get:NotNull
  public val description: String,
)
