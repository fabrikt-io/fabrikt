package examples.modelSuffix.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class ModeParameterDto(
  @JsonValue
  public val `value`: String,
) {
  MODE1("mode1"),
  MODE2("mode2"),
  MODE3("mode3"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, ModeParameterDto> =
        entries.associateBy(ModeParameterDto::value)

    public fun fromValue(`value`: String): ModeParameterDto? = mapping[value]
  }
}
