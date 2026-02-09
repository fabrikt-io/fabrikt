package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class YTest2Type(
  @JsonValue
  public val `value`: String,
) {
  Y2("Y2"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, YTest2Type> = entries.associateBy(YTest2Type::value)

    public fun fromValue(`value`: String): YTest2Type? = mapping[value]
  }
}
