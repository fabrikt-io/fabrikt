package examples.sharedCompositionRequestProjection.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kotlin.String

public data class B(
  @param:JsonProperty("secret")
  @get:JsonProperty("secret")
  @get:NotNull
  @get:Size(min = 3)
  override val secret: String,
  @param:JsonProperty("label")
  @get:JsonProperty("label")
  @get:NotNull
  override val label: String = "untitled",
) : Choice, AComposite, BComposite
