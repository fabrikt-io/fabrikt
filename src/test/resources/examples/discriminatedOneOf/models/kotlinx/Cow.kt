package examples.discriminatedOneOf.models

import kotlin.String
import kotlin.collections.Map
import kotlinx.serialization.SerialName

public enum class Cow(
  public val `value`: String,
) {
  @SerialName("Moo")
  MOO("Moo"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, Cow> = entries.associateBy(Cow::value)

    public fun fromValue(`value`: String): Cow? = mapping[value]
  }
}
