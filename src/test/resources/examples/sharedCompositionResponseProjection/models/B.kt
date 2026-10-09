package examples.sharedCompositionResponseProjection.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kotlin.String

public data class B(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  @get:Size(min = 2)
  override val id: String,
  @param:JsonProperty("label")
  @get:JsonProperty("label")
  @get:NotNull
  override val label: String = "untitled",
) : Choice, AComposite, BComposite
