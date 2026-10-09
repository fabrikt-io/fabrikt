package examples.sharedCompositionExternal.models

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
  @get:Size(min = 1)
  override val label: String,
  @param:JsonProperty("b")
  @get:JsonProperty("b")
  @get:NotNull
  override val b: String,
) : Mixed, Shared, AComposite, BaseComposite, BComposite
