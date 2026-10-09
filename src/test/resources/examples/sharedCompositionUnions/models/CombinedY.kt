package examples.sharedCompositionUnions.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kotlin.Int
import kotlin.String

public data class CombinedY(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  @get:Size(min = 2)
  override val id: String,
  @param:JsonProperty(
    "y",
    required = true,
  )
  @get:JsonProperty("y")
  @get:NotNull
  override val y: Int,
) : Combined, AComposite, CombinedYComposite, YComposite
