package examples.additionalProperties.models

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

@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = AdditionalPropertiesSerializer.Serializer::class)
public data class AdditionalPropertiesSerializer(
  @SerialName("id")
  @get:NotNull
  public val id: String,
  @Transient
  public val properties: MutableMap<String, JsonElement> = mutableMapOf(),
) {
  public object Serializer : KSerializer<AdditionalPropertiesSerializer> by examples.additionalProperties.models.utils.AdditionalPropertiesSerializer(AdditionalPropertiesSerializer.generatedSerializer(), AdditionalPropertiesSerializer::properties)
}
