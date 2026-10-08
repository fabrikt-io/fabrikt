package examples.inlinedAggregatedObjects.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.Any
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

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
  public val arrayWithAllOfAggregationOfMany: List<@Valid ContainerArrayWithAllOfAggregationOfMany>?
      = null,
  @param:JsonProperty("arrayWithAnyOfAggregationOfMany")
  @get:JsonProperty("arrayWithAnyOfAggregationOfMany")
  public val arrayWithAnyOfAggregationOfMany: List<@Valid ContainerArrayWithAnyOfAggregationOfMany>?
      = null,
  @param:JsonProperty("arrayWithOneOf")
  @get:JsonProperty("arrayWithOneOf")
  public val arrayWithOneOf: List<Any>? = null,
  /**
   * Arbitrary fields attached to the container.
   */
  @param:JsonProperty("aliasedFreeFormMap")
  @get:JsonProperty("aliasedFreeFormMap")
  public val aliasedFreeFormMap: Map<String, Any?>? = null,
  @param:JsonProperty("aliasedTypedMap")
  @get:JsonProperty("aliasedTypedMap")
  public val aliasedTypedMap: Map<String, String?>? = null,
  @param:JsonProperty("aliasedInlineObject")
  @get:JsonProperty("aliasedInlineObject")
  @get:Valid
  public val aliasedInlineObject: ContainerAliasedInlineObject? = null,
  @param:JsonProperty("arrayOfAliasedFreeFormMap")
  @get:JsonProperty("arrayOfAliasedFreeFormMap")
  public val arrayOfAliasedFreeFormMap: List<Map<String, Any?>>? = null,
)
