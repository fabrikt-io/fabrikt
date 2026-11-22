package examples.additionalProperties.models

import examples.additionalProperties.models.utils.AdditionalPropertiesSerializer
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
@Serializable(with = Result.Serializer::class)
public data class Result(
  @SerialName("message")
  @get:NotNull
  public val message: String,
  @Transient
  public val properties: MutableMap<String, JsonElement> = mutableMapOf(),
) {
  public object Serializer : KSerializer<Result> by AdditionalPropertiesSerializer(Result.generatedSerializer(), Result::properties)
}
