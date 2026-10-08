package examples.modelSuffix.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

public data class ComplexObjectWithMapsOfMapsDto(
  @param:JsonProperty("list-others")
  @get:JsonProperty("list-others")
  public val listOthers: List<@Valid BasicObjectDto>? = null,
  @param:JsonProperty("map-of-maps")
  @get:JsonProperty("map-of-maps")
  public val mapOfMaps: Map<String, Map<String, @Valid BasicObjectDto?>?>? = null,
)
