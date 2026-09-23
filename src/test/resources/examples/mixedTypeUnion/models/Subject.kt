package examples.mixedTypeUnion.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Any
import kotlin.String

public data class Subject(
  @param:JsonProperty("value")
  @get:JsonProperty("value")
  @get:NotNull
  public val `value`: Any,
  @param:JsonProperty("nullableText")
  @get:JsonProperty("nullableText")
  public val nullableText: String?,
  @param:JsonProperty("requiredText")
  @get:JsonProperty("requiredText")
  @get:NotNull
  public val requiredText: String,
  @param:JsonProperty("mixedObject")
  @get:JsonProperty("mixedObject")
  @get:NotNull
  public val mixedObject: Any,
  @param:JsonProperty("referencedValue")
  @get:JsonProperty("referencedValue")
  @get:NotNull
  public val referencedValue: Any,
  @param:JsonProperty("nullableMixed")
  @get:JsonProperty("nullableMixed")
  public val nullableMixed: Any?,
)
