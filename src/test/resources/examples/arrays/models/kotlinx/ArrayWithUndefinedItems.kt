package examples.arrays.models

import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class ArrayWithUndefinedItems(
  @SerialName("items_array")
  public val itemsArray: List<JsonElement>? = null,
)
