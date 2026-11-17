package examples.allOfParentOrder.models

import jakarta.validation.constraints.NotNull
import kotlin.OptIn
import kotlin.String
import kotlin.collections.MutableMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement

@SerialName("child")
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = OrderChild.Serializer::class)
public data class OrderChild(
  @SerialName("id")
  @get:NotNull
  override val id: String,
  @SerialName("label")
  @get:NotNull
  public val label: String,
  @Transient
  override val properties: MutableMap<String, JsonElement> = mutableMapOf(),
) : OrderX() {
  public object Serializer : KSerializer<OrderChild> by AdditionalPropertiesSerializer(OrderChild.generatedSerializer(), OrderChild::properties, "kind")
}
