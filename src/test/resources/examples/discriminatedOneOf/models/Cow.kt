package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class Cow(
  @JsonValue
  public val `value`: String,
) {
  MOO("Moo"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, Cow> = entries.associateBy(Cow::value)

    public fun fromValue(`value`: String): Cow? = mapping[value]
  }
}
