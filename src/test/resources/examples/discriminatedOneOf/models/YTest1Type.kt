package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class YTest1Type(
  @JsonValue
  public val `value`: String,
) {
  Y1("Y1"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, YTest1Type> = entries.associateBy(YTest1Type::value)

    public fun fromValue(`value`: String): YTest1Type? = mapping[value]
  }
}
