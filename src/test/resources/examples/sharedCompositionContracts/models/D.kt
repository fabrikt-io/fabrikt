package examples.sharedCompositionContracts.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kotlin.Int
import kotlin.String

public data class D(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  @get:Size(min = 2)
  override val id: String,
  @param:JsonProperty("description")
  @get:JsonProperty("description")
  override val description: String? = null,
  @param:JsonProperty("label")
  @get:JsonProperty("label")
  @get:NotNull
  override val label: String,
  @param:JsonProperty("count")
  @get:JsonProperty("count")
  public val count: Int? = null,
) : ACompositeExtra, BComposite
