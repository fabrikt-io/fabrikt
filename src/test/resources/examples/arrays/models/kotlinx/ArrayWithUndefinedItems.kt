package examples.arrays.models

import kotlin.Any
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ArrayWithUndefinedItems(
  @SerialName("items_array")
  public val itemsArray: List<Any>? = null,
)
