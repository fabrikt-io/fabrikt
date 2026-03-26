package examples.inlinedEnumParameter.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class Tags(
  @JsonValue
  public val `value`: String,
) {
  FEATURED("featured"),
  SALE("sale"),
  NEW_ARRIVAL("new_arrival"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, Tags> = entries.associateBy(Tags::value)

    public fun fromValue(`value`: String): Tags? = mapping[value]
  }
}
