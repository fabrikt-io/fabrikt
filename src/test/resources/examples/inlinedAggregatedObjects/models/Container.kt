package examples.inlinedAggregatedObjects.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.Any
import kotlin.collections.List

public data class Container(
  @param:JsonProperty("aggregationOfOne")
  @get:JsonProperty("aggregationOfOne")
  @get:Valid
  public val aggregationOfOne: SimpleObjOne? = null,
  @param:JsonProperty("aggregationOfMany")
  @get:JsonProperty("aggregationOfMany")
  @get:Valid
  public val aggregationOfMany: ContainerAggregationOfMany? = null,
  @param:JsonProperty("arrayWithAllOfAggregationOfMany")
  @get:JsonProperty("arrayWithAllOfAggregationOfMany")
  @get:Valid
  public val arrayWithAllOfAggregationOfMany: List<ContainerArrayWithAllOfAggregationOfMany>? =
      null,
  @param:JsonProperty("arrayWithAnyOfAggregationOfMany")
  @get:JsonProperty("arrayWithAnyOfAggregationOfMany")
  @get:Valid
  public val arrayWithAnyOfAggregationOfMany: List<ContainerArrayWithAnyOfAggregationOfMany>? =
      null,
  @param:JsonProperty("arrayWithOneOf")
  @get:JsonProperty("arrayWithOneOf")
  public val arrayWithOneOf: List<Any>? = null,
)
