package examples.discriminatedOneOf.models

import kotlin.String
import kotlin.collections.Map
import kotlinx.serialization.SerialName

public enum class StateBMode(
  public val `value`: String,
) {
  @SerialName("mode1")
  MODE1("mode1"),
  @SerialName("mode2")
  MODE2("mode2"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, StateBMode> = entries.associateBy(StateBMode::value)

    public fun fromValue(`value`: String): StateBMode? = mapping[value]
  }
}
