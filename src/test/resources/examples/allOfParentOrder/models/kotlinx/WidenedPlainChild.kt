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

@SerialName("plainSibling")
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = WidenedPlainChild.Serializer::class)
public data class WidenedPlainChild(
  @SerialName("id")
  @get:NotNull
  override val id: String,
  @SerialName("name")
  public val name: String? = null,
  @Transient
  override val properties: MutableMap<String, JsonElement> = mutableMapOf(),
) : WidenedParent() {
  public object Serializer : KSerializer<WidenedPlainChild> by AdditionalPropertiesSerializer(WidenedPlainChild.generatedSerializer(), WidenedPlainChild::properties, "kind")
}
