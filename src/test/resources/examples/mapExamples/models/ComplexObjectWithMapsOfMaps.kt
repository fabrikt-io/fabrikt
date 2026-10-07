package examples.mapExamples.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

public data class ComplexObjectWithMapsOfMaps(
  @param:JsonProperty("list-others")
  @get:JsonProperty("list-others")
  public val listOthers: List<@Valid BasicObject>? = null,
  @param:JsonProperty("map-of-maps")
  @get:JsonProperty("map-of-maps")
  public val mapOfMaps: Map<String, Map<String, @Valid BasicObject?>?>? = null,
)
