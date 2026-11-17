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

@SerialName("IntermediateOrder")
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = IntermediateOrder.Serializer::class)
public data class IntermediateOrder(
  @SerialName("id")
  @get:NotNull
  override val id: String,
  @SerialName("intermediateProp")
  public val intermediateProp: String? = null,
  @Transient
  override val properties: MutableMap<String, JsonElement> = mutableMapOf(),
) : RootOrder() {
  public object Serializer : KSerializer<IntermediateOrder> by AdditionalPropertiesSerializer(IntermediateOrder.generatedSerializer(), IntermediateOrder::properties, "kind")
}
