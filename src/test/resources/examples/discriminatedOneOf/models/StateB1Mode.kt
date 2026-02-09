package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class StateB1Mode(
  @JsonValue
  public val `value`: String,
) {
  MODE1("mode1"),
  MODE2("mode2"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, StateB1Mode> = entries.associateBy(StateB1Mode::value)

    public fun fromValue(`value`: String): StateB1Mode? = mapping[value]
  }
}
