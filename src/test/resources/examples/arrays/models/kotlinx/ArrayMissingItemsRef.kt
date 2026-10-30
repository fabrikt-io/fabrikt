package examples.arrays.models

import kotlin.Any
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ArrayMissingItemsRef(
  @SerialName("broken_ref")
  public val brokenRef: List<Any>? = null,
)
