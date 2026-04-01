package examples.inlinedEnumParameter.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class Categories(
  @JsonValue
  public val `value`: String,
) {
  ELECTRONICS("electronics"),
  CLOTHING("clothing"),
  FOOD("food"),
  TOYS("toys"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, Categories> = entries.associateBy(Categories::value)

    public fun fromValue(`value`: String): Categories? = mapping[value]
  }
}
