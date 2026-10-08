package examples.arrays.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.String
import kotlin.collections.List

public data class ContainsArrayOfArrays(
  @param:JsonProperty("array_of_arrays")
  @get:JsonProperty("array_of_arrays")
  public val arrayOfArrays: List<List<@Valid Something>>? = null,
  @param:JsonProperty("absent-object-type-in-array")
  @get:JsonProperty("absent-object-type-in-array")
  public val absentObjectTypeInArray: List<@Valid ContainsArrayOfArraysAbsentObjectTypeInArray>? =
      null,
  @param:JsonProperty("a-nullable-array")
  @get:JsonProperty("a-nullable-array")
  public val aNullableArray: List<String?>? = null,
)
