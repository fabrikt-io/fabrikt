package examples.discriminatedOneOf.models

import kotlin.String
import kotlin.collections.Map
import kotlinx.serialization.SerialName

public enum class XTestType(
  public val `value`: String,
) {
  @SerialName("X")
  X("X"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, XTestType> = entries.associateBy(XTestType::value)

    public fun fromValue(`value`: String): XTestType? = mapping[value]
  }
}
