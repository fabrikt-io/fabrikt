package examples.arrays.models

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ContainsArrayOfArraysAbsentObjectTypeInArray(
  @SerialName("some-array-prop")
  public val someArrayProp: String? = null,
)
