package examples.allOfParentOrder.models

import jakarta.validation.constraints.NotNull
import kotlin.Int
import kotlin.String
import kotlin.collections.MutableMap
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement

@SerialName("sibling")
@Serializable
public data class OrderSibling(
  @SerialName("count")
  @get:NotNull
  public val count: Int,
  @SerialName("id")
  @get:NotNull
  override val id: String,
  @Transient
  override val properties: MutableMap<String, JsonElement> = mutableMapOf(),
) : OrderX()
