package examples.mixedTypeUnion.models

import jakarta.validation.constraints.NotNull
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class Subject(
  @SerialName("value")
  @get:NotNull
  public val `value`: JsonElement,
  @SerialName("nullableText")
  public val nullableText: String?,
  @SerialName("requiredText")
  @get:NotNull
  public val requiredText: String,
  @SerialName("mixedObject")
  @get:NotNull
  public val mixedObject: JsonElement,
  @SerialName("referencedValue")
  @get:NotNull
  public val referencedValue: JsonElement,
  @SerialName("nullableMixed")
  public val nullableMixed: JsonElement?,
)
