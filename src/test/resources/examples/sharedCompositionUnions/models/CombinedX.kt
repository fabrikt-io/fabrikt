package examples.sharedCompositionUnions.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kotlin.String

public data class CombinedX(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  @get:Size(min = 2)
  override val id: String,
  @param:JsonProperty("x")
  @get:JsonProperty("x")
  @get:NotNull
  override val x: String,
) : Combined, AComposite, CombinedXComposite, XComposite
