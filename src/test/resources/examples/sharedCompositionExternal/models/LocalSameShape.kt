package examples.sharedCompositionExternal.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kotlin.String

public data class LocalSameShape(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  @get:Size(min = 2)
  public val id: String,
  @param:JsonProperty("label")
  @get:JsonProperty("label")
  @get:NotNull
  @get:Size(min = 1)
  public val label: String,
)
