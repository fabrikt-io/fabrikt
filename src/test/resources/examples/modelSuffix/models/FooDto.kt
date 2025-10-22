package examples.modelSuffix.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class FooDto(
  @JsonValue
  public val `value`: String,
) {
  X("X"),
  Y("Y"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, FooDto> = entries.associateBy(FooDto::value)

    public fun fromValue(`value`: String): FooDto? = mapping[value]
  }
}
