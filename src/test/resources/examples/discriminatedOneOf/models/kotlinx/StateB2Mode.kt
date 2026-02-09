package examples.discriminatedOneOf.models

import kotlin.String
import kotlin.collections.Map
import kotlinx.serialization.SerialName

public enum class StateB2Mode(
  public val `value`: String,
) {
  @SerialName("mode3")
  MODE3("mode3"),
  @SerialName("mode4")
  MODE4("mode4"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, StateB2Mode> = entries.associateBy(StateB2Mode::value)

    public fun fromValue(`value`: String): StateB2Mode? = mapping[value]
  }
}
