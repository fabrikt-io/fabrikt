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

@SerialName("stringChild")
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = TypedStringChild.Serializer::class)
public data class TypedStringChild(
  @SerialName("id")
  @get:NotNull
  override val id: String,
  @SerialName("label")
  public val label: String? = null,
  @Transient
  override val properties: MutableMap<String, JsonElement> = mutableMapOf(),
) : TypedParent() {
  public object Serializer : KSerializer<TypedStringChild> by AdditionalPropertiesSerializer(TypedStringChild.generatedSerializer(), TypedStringChild::properties, "kind")
}
