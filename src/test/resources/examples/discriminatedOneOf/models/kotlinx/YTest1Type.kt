package examples.discriminatedOneOf.models

import kotlin.String
import kotlin.collections.Map
import kotlinx.serialization.SerialName

public enum class YTest1Type(
  public val `value`: String,
) {
  @SerialName("Y1")
  Y1("Y1"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, YTest1Type> = entries.associateBy(YTest1Type::value)

    public fun fromValue(`value`: String): YTest1Type? = mapping[value]
  }
}
