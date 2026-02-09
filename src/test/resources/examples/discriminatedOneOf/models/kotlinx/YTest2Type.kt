package examples.discriminatedOneOf.models

import kotlin.String
import kotlin.collections.Map
import kotlinx.serialization.SerialName

public enum class YTest2Type(
  public val `value`: String,
) {
  @SerialName("Y2")
  Y2("Y2"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, YTest2Type> = entries.associateBy(YTest2Type::value)

    public fun fromValue(`value`: String): YTest2Type? = mapping[value]
  }
}
