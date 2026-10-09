package examples.sharedCompositionUnions.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Int

public data class Y(
  @param:JsonProperty(
    "y",
    required = true,
  )
  @get:JsonProperty("y")
  @get:NotNull
  override val y: Int,
) : YComposite
