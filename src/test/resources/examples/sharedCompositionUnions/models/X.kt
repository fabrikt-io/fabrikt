package examples.sharedCompositionUnions.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class X(
  @param:JsonProperty("x")
  @get:JsonProperty("x")
  @get:NotNull
  override val x: String,
) : XComposite
