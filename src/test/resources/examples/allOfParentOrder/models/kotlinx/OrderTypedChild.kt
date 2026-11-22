package examples.allOfParentOrder.models

import examples.allOfParentOrder.models.utils.AdditionalPropertiesSerializer
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

@SerialName("typed")
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = OrderTypedChild.Serializer::class)
public data class OrderTypedChild(
  @SerialName("id")
  @get:NotNull
  override val id: String,
  @SerialName("label")
  @get:NotNull
  public val label: String,
  @Transient
  override val properties: MutableMap<String, JsonElement> = mutableMapOf(),
) : OrderX() {
  public object Serializer : KSerializer<OrderTypedChild> by AdditionalPropertiesSerializer(OrderTypedChild.generatedSerializer(), OrderTypedChild::properties, "kind")
}
