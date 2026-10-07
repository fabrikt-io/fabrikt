package examples.composedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

public data class ComposedDeducedHolder(
  @param:JsonProperty("value")
  @get:JsonProperty("value")
  @get:NotNull
  @get:Valid
  public val `value`: ComposedDeducedCombined,
  @param:JsonProperty("values")
  @get:JsonProperty("values")
  @get:Valid
  public val values: List<ComposedDeducedCombined>? = null,
  @param:JsonProperty("byKey")
  @get:JsonProperty("byKey")
  @get:Valid
  public val byKey: Map<String, ComposedDeducedCombined?>? = null,
)
