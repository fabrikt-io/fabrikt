package examples.arrays.models

import jakarta.validation.Valid
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ContainsArrayOfArrays(
  @SerialName("array_of_arrays")
  public val arrayOfArrays: List<List<@Valid Something>>? = null,
  @SerialName("absent-object-type-in-array")
  public val absentObjectTypeInArray:
      List<@Valid ContainsArrayOfArraysAbsentObjectTypeInArray>? = null,
  @SerialName("a-nullable-array")
  public val aNullableArray: List<String?>? = null,
)
