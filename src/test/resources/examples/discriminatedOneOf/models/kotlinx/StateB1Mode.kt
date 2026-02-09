package examples.discriminatedOneOf.models

import kotlin.String
import kotlin.collections.Map
import kotlinx.serialization.SerialName

public enum class StateB1Mode(
  public val `value`: String,
) {
  @SerialName("mode1")
  MODE1("mode1"),
  @SerialName("mode2")
  MODE2("mode2"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, StateB1Mode> = entries.associateBy(StateB1Mode::value)

    public fun fromValue(`value`: String): StateB1Mode? = mapping[value]
  }
}
