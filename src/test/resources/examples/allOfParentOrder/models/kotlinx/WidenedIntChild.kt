package examples.allOfParentOrder.models

import jakarta.validation.constraints.NotNull
import kotlin.Int
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

@SerialName("intChild")
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = WidenedIntChild.Serializer::class)
public data class WidenedIntChild(
  @SerialName("id")
  @get:NotNull
  override val id: String,
  @SerialName("count")
  public val count: Int? = null,
  @Transient
  override val properties: MutableMap<String, JsonElement> = mutableMapOf(),
) : WidenedParent() {
  public object Serializer : KSerializer<WidenedIntChild> by AdditionalPropertiesSerializer(WidenedIntChild.generatedSerializer(), WidenedIntChild::properties, "kind")
}
