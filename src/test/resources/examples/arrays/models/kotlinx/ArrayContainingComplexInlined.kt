package examples.arrays.models

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ArrayContainingComplexInlined(
  @SerialName("prop_one")
  public val propOne: String? = null,
)
