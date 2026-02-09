package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class XTestType(
  @JsonValue
  public val `value`: String,
) {
  X("X"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, XTestType> = entries.associateBy(XTestType::value)

    public fun fromValue(`value`: String): XTestType? = mapping[value]
  }
}
