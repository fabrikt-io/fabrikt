package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class StateB2Mode(
  @JsonValue
  public val `value`: String,
) {
  MODE3("mode3"),
  MODE4("mode4"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, StateB2Mode> = entries.associateBy(StateB2Mode::value)

    public fun fromValue(`value`: String): StateB2Mode? = mapping[value]
  }
}
