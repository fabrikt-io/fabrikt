package examples.composedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

public data class ComposedTaggedHolder(
  @param:JsonProperty("value")
  @get:JsonProperty("value")
  @get:NotNull
  @get:Valid
  public val `value`: ComposedTaggedCombined,
  @param:JsonProperty("values")
  @get:JsonProperty("values")
  public val values: List<@Valid ComposedTaggedCombined>? = null,
  @param:JsonProperty("byKey")
  @get:JsonProperty("byKey")
  public val byKey: Map<String, @Valid ComposedTaggedCombined?>? = null,
)
