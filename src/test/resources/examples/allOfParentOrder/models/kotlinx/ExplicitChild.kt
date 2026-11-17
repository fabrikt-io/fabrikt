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

@SerialName("explicit")
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = ExplicitChild.Serializer::class)
public data class ExplicitChild(
  @SerialName("id")
  @get:NotNull
  override val id: String,
  @SerialName("label")
  public val label: String? = null,
  @Transient
  public val properties: MutableMap<String, JsonElement> = mutableMapOf(),
) : UnconfiguredParent() {
  public object Serializer : KSerializer<ExplicitChild> by AdditionalPropertiesSerializer(ExplicitChild.generatedSerializer(), ExplicitChild::properties, "kind")
}
