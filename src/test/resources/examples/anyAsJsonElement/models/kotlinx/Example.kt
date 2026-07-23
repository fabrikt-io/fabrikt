package examples.anyAsJsonElement.models

import jakarta.validation.constraints.NotNull
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
public data class Example(
  @SerialName("anyValue")
  @get:NotNull
  public val anyValue: JsonElement,
  @SerialName("optionalAnyValue")
  public val optionalAnyValue: JsonElement? = null,
  @SerialName("anyList")
  public val anyList: List<JsonElement>? = null,
  @SerialName("anyListNoItems")
  public val anyListNoItems: List<JsonElement>? = null,
  @SerialName("nullableAny")
  public val nullableAny: JsonElement? = null,
  @SerialName("refAny")
  public val refAny: JsonElement? = null,
  @SerialName("oneOfAny")
  public val oneOfAny: JsonElement? = null,
  @SerialName("anyOfAny")
  public val anyOfAny: JsonElement? = null,
  @SerialName("untypedObject")
  public val untypedObject: JsonObject? = null,
)
