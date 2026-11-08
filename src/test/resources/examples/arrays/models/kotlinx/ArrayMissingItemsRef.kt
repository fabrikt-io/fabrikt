package examples.arrays.models

import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class ArrayMissingItemsRef(
  @SerialName("broken_ref")
  public val brokenRef: List<JsonElement>? = null,
)
